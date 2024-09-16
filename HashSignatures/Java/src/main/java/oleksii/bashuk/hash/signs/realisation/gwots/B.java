package oleksii.bashuk.hash.signs.realisation.gwots;

import oleksii.bashuk.hash.signs.hash.HashFunction;
import oleksii.bashuk.hash.signs.hash.HashFunction.Hash;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS.*;
import oleksii.bashuk.hash.signs.realisation.gwots.communication.*;

import java.nio.ByteBuffer;
import java.util.*;

import static oleksii.bashuk.hash.signs.Utils.getBlockFromByteArray;

public class B {

    private final HashFunction hashFunction;
    private final BlockWOTS signProtocol;
    private final int bitSizeOfBlock;
    private final int messageSize;
    private final int seedKey;
    private final int index;

    private HashMap<Integer, BlockWOTSPubKey> journal;
    private HashMap<Integer, BlockWOTSPubKey> journal0;

    public B(HashFunction hashFunction,
             BlockWOTS signProtocol,
             HashMap<Integer, BlockWOTSPubKey> journal,
             int bitSizeOfBlock,
             int messageSize,
             int seedKey,
             int index) {
        this.hashFunction = hashFunction;
        this.signProtocol = signProtocol;
        this.journal = journal;
        this.journal0 = new HashMap<>(journal);
        this.bitSizeOfBlock = bitSizeOfBlock;
        this.messageSize = messageSize;
        this.seedKey = seedKey;
        this.index = index;
    }

    public void addNewAToJournal(int index, BlockWOTSPubKey hashedPubKey) {
        journal.put(index, hashedPubKey);
        journal0.put(index, hashedPubKey);
    }

    public BRequest buildRequest() {
        long seed = seedKey + System.currentTimeMillis();
        Random random = new Random(seed);

        byte[] randomValue = new byte[messageSize];
        random.nextBytes(randomValue);

        return new BRequest(randomValue);
    }

    public List<BRequestPlus> buildRequestPlus(int t) {
        long seed = seedKey + System.currentTimeMillis();
        Random random = new Random(seed);

        byte[] randomValue = new byte[messageSize];
        random.nextBytes(randomValue);

        List<BRequestPlus> requests = new ArrayList<>();

        for (int indexA: journal0.keySet()) {
            Hash conf = hashFunction.valueOf(ByteBuffer.allocate(4).putInt(t).array())
                    .concatByBytes(ByteBuffer.allocate(4).putInt(indexA).array())
                    .concatByBytes(ByteBuffer.allocate(4).putInt(index).array())
                    .concatByBytes(randomValue)
                    .concat(journal0.get(indexA).value())
                    .concat(journal.get(indexA).value())
                    .update();
            requests.add(new BRequestPlus(t, indexA, index, randomValue, conf));
        }

        return requests;
    }

    public boolean checkResponse(BRequest bRequest, AResponse aResponse) {
        int index = aResponse.index();
        BlockWOTSSign sign = aResponse.sign();
        BlockWOTSPubKey hashedNewPubKey = aResponse.hashedNewPubKey();

        int blockValue = getBlockFromByteArray(hashFunction.valueOf(bRequest.msg()).concat(hashedNewPubKey.value()).update().getBytes(),
                bitSizeOfBlock, index);
        BlockWOTSMessage msg = new BlockWOTSMessage(blockValue);
        if (!signProtocol.vrfy(journal.get(index), sign, msg)) {
            return false;
        }

        journal.put(index, hashedNewPubKey);
        return true;
    }

    public boolean checkResponsePlus(BRequestPlus bRequest, AResponsePlus aResponsePlus) {
        int index = aResponsePlus.index();
        BlockWOTSSign sign = aResponsePlus.sign();
        BlockWOTSPubKey hashedLastPubKey = aResponsePlus.hashedLastPubKey();
        BlockWOTSPubKey hashedNewPubKey = aResponsePlus.hashedNewPubKey();

        if (!hashedLastPubKey.value().equals(journal.get(index).value())) {
            return false;
        }

        int blockValue = getBlockFromByteArray(bRequest.conf().concat(hashedNewPubKey.value()).update().getBytes(),
                bitSizeOfBlock, index);
        BlockWOTSMessage msg = new BlockWOTSMessage(blockValue);
        if (!signProtocol.vrfy(journal.get(index), sign, msg)) {
            return false;
        }

        journal.put(index, hashedNewPubKey);
        return true;
    }
}
