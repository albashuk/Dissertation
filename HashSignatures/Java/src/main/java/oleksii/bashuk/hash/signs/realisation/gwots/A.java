package oleksii.bashuk.hash.signs.realisation.gwots;

import oleksii.bashuk.hash.signs.hash.HashFunction;
import oleksii.bashuk.hash.signs.hash.HashFunction.Hash;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS.*;
import oleksii.bashuk.hash.signs.realisation.gwots.communication.*;

import java.nio.ByteBuffer;

import static oleksii.bashuk.hash.signs.Utils.getBlockFromByteArray;

public class A {

    private final HashFunction hashFunction;
    private final BlockWOTS signProtocol;
    private final int bitSizeOfBlock;
    private final int index;

    private BlockWOTSSecKey sk;
    private BlockWOTSPubKey pk;
    private BlockWOTSPubKey pk0;
    private int t;

    public A(HashFunction hashFunction,
             BlockWOTS signProtocol,
             BlockWOTSSecKey sk,
             BlockWOTSPubKey pk,
             int bitSizeOfBlock,
             int index) {
        this.hashFunction = hashFunction;
        this.signProtocol = signProtocol;
        this.sk = sk;
        this.pk = pk;
        this.pk0 = pk;
        this.bitSizeOfBlock = bitSizeOfBlock;
        this.index = index;
        this.t = 0;
    }

    public int getIndex() { return index; }

    public AResponse signMessage(BRequest bRequest) {
        BlockWOTSSecKey newSk = new BlockWOTSSecKey(sk.value().updateByBytes(bRequest.msg()));
        BlockWOTSPubKey newPk = (BlockWOTSPubKey) signProtocol.buildPubKeyFromSecKey(newSk);

        int blockValue = getBlockFromByteArray(hashFunction.valueOf(bRequest.msg()).concat(newPk.value().update()).update().getBytes(),
                bitSizeOfBlock, index);
        BlockWOTSMessage msg = new BlockWOTSMessage(blockValue);
        BlockWOTSSign sign = signProtocol.sign(sk, msg);

        sk = newSk;
        pk = newPk;
        t++;

        return new AResponse(index, sign, new BlockWOTSPubKey(newPk.value().update()));
    }

    public AResponsePlus signMessagePlus(BRequestPlus bRequestPlus) {
        Hash check = hashFunction.valueOf(ByteBuffer.allocate(4).putInt(t).array())
                .concatByBytes(ByteBuffer.allocate(4).putInt(index).array())
                .concatByBytes(ByteBuffer.allocate(4).putInt(bRequestPlus.indexB()).array())
                .concatByBytes(bRequestPlus.msg())
                .concat(pk0.value().update())
                .concat(pk.value().update())
                .update();

        if (!bRequestPlus.conf().equals(check)) {
            return null;
        }

        BlockWOTSSecKey newSk = new BlockWOTSSecKey(sk.value().updateByBytes(bRequestPlus.msg()));
        BlockWOTSPubKey newPk = (BlockWOTSPubKey) signProtocol.buildPubKeyFromSecKey(newSk);

        int blockValue = getBlockFromByteArray(check.concat(newPk.value().update()).update().getBytes(),
                bitSizeOfBlock, index);
        BlockWOTSMessage msg = new BlockWOTSMessage(blockValue);
        BlockWOTSSign sign = signProtocol.sign(sk, msg);

        BlockWOTSPubKey lastPk = pk;
        sk = newSk;
        pk = newPk;
        t++;

        return new AResponsePlus(index, sign, new BlockWOTSPubKey(lastPk.value().update()), new BlockWOTSPubKey(newPk.value().update()));
    }
}
