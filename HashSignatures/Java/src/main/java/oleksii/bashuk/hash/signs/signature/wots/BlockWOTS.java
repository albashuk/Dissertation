package oleksii.bashuk.hash.signs.signature.wots;

import oleksii.bashuk.hash.signs.hash.HashFunction;
import oleksii.bashuk.hash.signs.hash.HashFunction.Hash;
import oleksii.bashuk.hash.signs.signature.Signature;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;

public class BlockWOTS implements Signature {

    private final static int genByteSize = 16;

    private final HashFunction hashFunction;
    private final int numberOfHashingSteps;
    private final int seedKey;

    public BlockWOTS(HashFunction hashFunction,
                     int bitSizeOfBlock,
                     int seedKey) {
        this.hashFunction = hashFunction;
        this.numberOfHashingSteps = (1 << bitSizeOfBlock);
        this.seedKey = seedKey;
    }

    public Pair<SecKey, PubKey> gen() {
        long seed = seedKey + System.currentTimeMillis();
        Random random = new Random(seed);

        byte[] randomValue = new byte[genByteSize];
        random.nextBytes(randomValue);

        BlockWOTSSecKey secKey = new BlockWOTSSecKey(hashFunction.hash(randomValue));
        BlockWOTSPubKey pubKey = _buildPubKeyFromSecKey(secKey);

        return Pair.of(secKey, pubKey);
    }

    public PubKey buildPubKeyFromSecKey(SecKey secKey) {
        return _buildPubKeyFromSecKey((BlockWOTSSecKey) secKey);
    }

    private BlockWOTSPubKey _buildPubKeyFromSecKey(BlockWOTSSecKey secKey) {
        return new BlockWOTSPubKey(secKey.value.update(numberOfHashingSteps));
    }

    public BlockWOTSSign sign(SecKey secKey, Message msg) {
        return _sign((BlockWOTSSecKey) secKey, (BlockWOTSMessage) msg);
    }

    private BlockWOTSSign _sign(BlockWOTSSecKey sk, BlockWOTSMessage msg) {
        int iter = msg.value & (numberOfHashingSteps - 1);
        return new BlockWOTSSign(sk.value.update(numberOfHashingSteps - iter));
    }

    public boolean vrfy(PubKey hashedPubKey, Sign sign, Message msg) {
        return _vrfy((BlockWOTSPubKey) hashedPubKey, (BlockWOTSSign) sign, (BlockWOTSMessage) msg);
    }

    private boolean _vrfy(BlockWOTSPubKey hashedPubKey, BlockWOTSSign sign, BlockWOTSMessage msg) {
        int iter = msg.value & (numberOfHashingSteps - 1);
        return hashedPubKey.value.equals(sign.value.update(iter + 1));
    }

    public record BlockWOTSSecKey(Hash value) implements SecKey {}

    public record BlockWOTSPubKey(Hash value) implements PubKey {}

    public record BlockWOTSSign(Hash value) implements Sign {}

    public record BlockWOTSMessage(int value) implements Message {}

}
