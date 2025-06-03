package oleksii.bashuk.hash.signs.signature.mts;

import oleksii.bashuk.hash.signs.common.HashMessage;
import oleksii.bashuk.hash.signs.hash.HashFunction;
import oleksii.bashuk.hash.signs.hash.HashFunction.Hash;
import oleksii.bashuk.hash.signs.signature.Signature;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class NarySTBS implements Signature {

    private final HashFunction hashFunction;
    private final Signature ots;
    private final int arity;
    private final boolean useProxyNode;

    public NarySTBS(HashFunction hashFunction,
                    Signature ots,
                    int arity,
                    boolean useProxyNode) {
        this.hashFunction = hashFunction;
        this.ots = ots;
        this.arity = arity;
        this.useProxyNode = useProxyNode;
    }

    public Pair<SecKey, PubKey> gen() {
        Pair<SecKey, PubKey> keyPair = ots.gen();
        Node head = new Node(null, keyPair.getLeft(), null);
        NarySTBSSecKey sk = new NarySTBSSecKey(head, head, head);
        NarySTBSPubKey pk = new NarySTBSPubKey(keyPair.getRight());
        return Pair.of(sk, pk);
    }

    public PubKey buildPubKeyFromSecKey(SecKey sk) {
        return _buildPubKeyFromSecKey((NarySTBSSecKey) sk);
    }

    public NarySTBSPubKey _buildPubKeyFromSecKey(NarySTBSSecKey sk) {
        return (NarySTBSPubKey) ots.buildPubKeyFromSecKey(sk);
    }

    public Sign sign(SecKey sk, Message msg) {
        return _sign((NarySTBSSecKey) sk, (HashMessage) msg);
    }

    public NarySTBSSign _sign(NarySTBSSecKey sk, HashMessage msg) {
        Node curNode = sk.nextForSign;
        for (int i = 0; i < arity; i++) {
            Pair<SecKey, PubKey> keyPair = ots.gen();
            curNode.children.add(new Node(curNode, keyPair.getLeft(), keyPair.getRight()));
        }

        curNode.children.get(0).previous = sk.last;
        sk.last.next = curNode.children.get(0);
        sk.last = curNode.children.get(arity - 1);
        for (int i = 1; i < arity; i++) {
            curNode.children.get(i).previous = curNode.children.get(i - 1);
            curNode.children.get(i - 1).next = curNode.children.get(i);
        }

        if (useProxyNode) {
            Pair<SecKey, PubKey> keyPair = ots.gen();
            curNode.children.add(new Node(curNode, keyPair.getLeft(), keyPair.getRight()));
        }

        Hash hash = hashFunction.hash(new byte[0], 0);
        for (Node child: curNode.children) {
            hash.concat(child.pk.hash);
        }

        if (useProxyNode) {
            curNode.children.get(arity).sign = ots.sign(curNode.children.get(arity).sk, new HashMessage(msg.value()));
        } else {
            hash.concat(msg.value());
        }
        curNode.msg = msg;
        curNode.sign = ots.sign(curNode.sk, new HashMessage(hash));

        NarySTBSSign sign = new NarySTBSSign(new ArrayList<Node>());
        while (curNode != null) {
            Node pkNode = new Node();
            pkNode.sign =
            new ArrayList<Node>();
        }

        sk.nextForSign = curNode.next;
    }

    public boolean vrfy(PubKey pk, Sign sign, Message msg) {
        return _vrfy((NarySTBSPubKey) pk, (NarySTBSSign) sign, (HashMessage) msg);
    }

    public boolean _vrfy(NarySTBSPubKey pk, NarySTBSSign sign, HashMessage msg) {

    }

    public static class NarySTBSSecKey implements SecKey {
        public Node head;
        public Node last;
        public Node nextForSign;

        public NarySTBSSecKey(Node head, Node last, Node nextForSign) {
            this.head = head;
            this.last = last;
            this.nextForSign = nextForSign;
        }
    }

    public record NarySTBSPubKey(PubKey pk) implements PubKey {}

    public record NarySTBSSign(List<Node> chainNodes) implements Sign {}

    private static class Node {
        public Node parent;
        public Node previous;
        public Node next;
        public List<Node> children;
        public SecKey sk;
        public PubKey pk;
        public Sign sign;
        public Message msg;

        public Node() {
            this(null, null, null, null, null, null, null, null);
        }

        public Node(Node parent, SecKey sk, PubKey pk) {
            this(parent, null, null, new ArrayList<Node>(),
                    sk, pk, null, null);
        }

        public Node(Node parent, Node previous, Node next, List<Node> children,
             SecKey sk, PubKey pk, Sign sign, Message msg) {
            this.parent = parent;
            this.previous = previous;
            this.next = next;
            this.children = children;
            this.sk = sk;
            this.pk = pk;
            this.sign = sign;
            this.msg = msg;
        }
    }

}
