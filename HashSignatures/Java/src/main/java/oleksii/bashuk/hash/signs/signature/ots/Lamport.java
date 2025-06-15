package oleksii.bashuk.hash.signs.signature.ots;

import oleksii.bashuk.hash.signs.common.HashMessage;
import oleksii.bashuk.hash.signs.hash.HashFunction;
import oleksii.bashuk.hash.signs.hash.HashFunction.Hash;
import oleksii.bashuk.hash.signs.signature.Signature;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class Lamport implements Signature {

    private final static int genByteSize = 16;

    private final HashFunction hashFunction;
    private final int seedKey;

    public Lamport(HashFunction hashFunction,
                   int seedKey) {
        this.hashFunction = hashFunction;
        this.seedKey = seedKey;
    }

    public Pair<SecKey, PubKey> gen() {
        long seed = seedKey + System.currentTimeMillis();
        Random random = new Random(seed);

        byte[] randomValue = new byte[genByteSize];
        List<Pair<Hash, Hash>> skValues = new ArrayList<>();
        for (int i = 0; i < hashFunction.bitSize(); i++) {
            random.nextBytes(randomValue);
            Hash left = hashFunction.hash(randomValue);
            random.nextBytes(randomValue);
            Hash right = hashFunction.hash(randomValue);
            skValues.add(Pair.of(left, right));
        }

        LamportSecKey sk = new LamportSecKey(skValues);
        LamportPubKey pk = _buildPubKeyFromSecKey(sk);

        return Pair.of(sk, pk);
    }

    public PubKey buildPubKeyFromSecKey(SecKey sk) {
        return _buildPubKeyFromSecKey((LamportSecKey) sk);
    }

    private LamportPubKey _buildPubKeyFromSecKey(LamportSecKey sk) {
        List<Pair<Hash, Hash>> pkValues = new ArrayList<>();
        for (Pair<Hash, Hash> pair: sk.values) {
            pkValues.add(Pair.of(pair.getLeft().update(), pair.getRight().update()));
        }
        Hash hash = hashFunction.hash(new byte[0], 0);
        for (Pair<Hash, Hash> pair: pkValues) {
            hash = hash.concat(pair.getLeft()).concat(pair.getRight());
        }
        return new LamportPubKey(pkValues, hash);
    }

    public Sign sign(SecKey sk, Message msg) {
        return _sign((LamportSecKey) sk, (HashMessage) msg);
    }

    private LamportSign _sign(LamportSecKey sk, HashMessage msg) {
        BitSet bits = BitSet.valueOf(msg.value().getBytes());
        List<Hash> signValues = new ArrayList<>();
        for (int i = 0; i < bits.length(); i++) {
            signValues.add(!bits.get(i) ? sk.values.get(i).getLeft() : sk.values.get(i).getRight());
        }
        return new LamportSign(signValues);
    }

    public boolean vrfy(PubKey pk, Sign sign, Message msg) {
        return _vrfy((LamportPubKey) pk, (LamportSign) sign, (HashMessage) msg);
    }

    private boolean _vrfy(LamportPubKey pk, LamportSign sign, HashMessage msg) {
        BitSet bits = BitSet.valueOf(msg.value().getBytes());
        for (int i = 0; i < bits.length(); i++) {
            Hash pkValue = !bits.get(i) ? pk.values.get(i).getLeft() : pk.values.get(i).getRight();
            Hash signValue = sign.values.get(i).update();
            if (!pkValue.equals(signValue)) {
                return false;
            }
        }
        return true;
    }

    public record LamportSecKey(List<Pair<Hash, Hash>> values) implements SecKey {}

    public record LamportPubKey(List<Pair<Hash, Hash>> values, Hash hash) implements PubKey {
        public Hash getHash() {
            return hash;
        }
    }

    public record LamportSign(List<Hash> values) implements Sign {}
}
