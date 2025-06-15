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
        // Generate child nodes for next node for sign
        Node curNode = sk.nextForSign;
        for (int i = 0; i < arity; i++) {
            Pair<SecKey, PubKey> keyPair = ots.gen();
            curNode.children.add(new Node(curNode, keyPair.getLeft(), keyPair.getRight()));
        }

        // Connect newly generated nodes
        curNode.children.get(0).previous = sk.last;
        sk.last.next = curNode.children.get(0);
        sk.last = curNode.children.get(arity - 1);
        for (int i = 1; i < arity; i++) {
            curNode.children.get(i).previous = curNode.children.get(i - 1);
            curNode.children.get(i - 1).next = curNode.children.get(i);
        }

        // Generate proxy node if needed, and sign the msg by the proxy node
        if (useProxyNode) {
            Pair<SecKey, PubKey> keyPair = ots.gen();
            curNode.children.add(new Node(curNode, keyPair.getLeft(), keyPair.getRight()));
            curNode.children.get(arity).msg = msg;
            curNode.children.get(arity).sign = ots.sign(curNode.children.get(arity).sk, new HashMessage(msg.value()));
        } else {
            // Otherwise, just add msg to the node
            curNode.msg = msg;
        }

        // Create common hash of child nodes (their pub keys)
        Hash hash = prepareHashForSign(curNode);

        curNode.sign = ots.sign(curNode.sk, new HashMessage(hash));

        // Creating signature chain
        NarySTBSSign sign = new NarySTBSSign(new ArrayList<Node>());
        Node lastCurNode = null;
        Node lastSignNode = null;
        while (curNode != null) {
            Node signNode = new Node();
            signNode.sign = curNode.sign;
            signNode.pk = curNode.pk;
            if (!useProxyNode) {
                signNode.msg = curNode.msg;
            }
            signNode.children = new ArrayList<Node>();
            for (Node child: curNode.children) {
                if (child == lastCurNode) {
                    lastSignNode.parent = signNode;
                    signNode.children.add(lastSignNode);
                } else {
                    Node signChild = new Node(signNode, null, child.pk);
                    if (useProxyNode && curNode == sk.nextForSign && child.previous == null) {
                        signChild.sign = child.sign;
                        signChild.msg = child.msg;
                    }
                    signNode.children.add(signChild);
                }
            }
            sign.chainNodes.add(signNode);
            lastCurNode = curNode;
            lastSignNode = signNode;
            curNode = curNode.parent;
        }

        sk.nextForSign = sk.nextForSign.next;
        return sign;
    }

    public boolean vrfy(PubKey pk, Sign sign, Message msg) {
        return _vrfy((NarySTBSPubKey) pk, (NarySTBSSign) sign, (HashMessage) msg);
    }

    public boolean _vrfy(NarySTBSPubKey pk, NarySTBSSign sign, HashMessage msg) {
        Node curNode = sign.chainNodes().get(0);
        if (!msg.value().equals(useProxyNode ? curNode.children.get(arity).msg.value() : curNode.msg.value())) {
            return false;
        }
        sign.chainNodes.get(sign.chainNodes.size() - 1).pk = pk.headPk;

        if (useProxyNode) {
            Node proxyNode = curNode.children.get(arity);
            if (!ots.vrfy(proxyNode.pk, proxyNode.sign, proxyNode.msg)) {
                sign.chainNodes.get(sign.chainNodes.size() - 1).pk = null;
                return false;
            }
        }

        Node lastNode = curNode.children.get(0);
        while (curNode != null) {
            boolean lastNodeIsChild = false;
            for (Node child: curNode.children) {
                if (child == lastNode) {
                    lastNodeIsChild = true;
                    break;
                }
            }
            if (!lastNodeIsChild) {
                sign.chainNodes.get(sign.chainNodes.size() - 1).pk = null;
                return false;
            }

            Hash hash = prepareHashForSign(curNode);
            if (!ots.vrfy(curNode.pk, curNode.sign, new HashMessage(hash))) {
                sign.chainNodes.get(sign.chainNodes.size() - 1).pk = null;
                return false;
            }

            lastNode = curNode;
            curNode = curNode.parent;
        }

        sign.chainNodes.get(sign.chainNodes.size() - 1).pk = null;
        return true;
    }

    private Hash prepareHashForSign(Node node) {
        // Create common hash of child nodes (their pub keys)
        Hash hash = hashFunction.hash(new byte[0], 0);
        for (Node child: node.children) {
            hash.concat(child.pk.getHash());
        }

        if (!useProxyNode) {
            // Otherwise, concat common hash with the msg hash
            hash.concat(node.msg.value());
        }
        return hash;
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

    public record NarySTBSPubKey(PubKey headPk) implements PubKey {
        public Hash getHash() {
            return headPk.getHash();
        }
    }

    public record NarySTBSSign(List<Node> chainNodes) implements Sign {}

    private static class Node {
        public Node parent;
        public Node previous;
        public Node next;
        public List<Node> children;
        public SecKey sk;
        public PubKey pk;
        public Sign sign;
        public HashMessage msg;

        public Node() {
            this(null, null, null, null, null, null, null, null);
        }

        public Node(Node parent, SecKey sk, PubKey pk) {
            this(parent, null, null, new ArrayList<Node>(),
                    sk, pk, null, null);
        }

        public Node(Node parent, Node previous, Node next, List<Node> children,
             SecKey sk, PubKey pk, Sign sign, HashMessage msg) {
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
