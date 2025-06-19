package oleksii.bashuk.hash.signs.common;

import oleksii.bashuk.hash.signs.hash.HashFunction.Hash;
import oleksii.bashuk.hash.signs.signature.Signature;

public class HashMessage implements Signature.Message {
    public final Hash value;
    public HashMessage(Hash value) {
        this.value = value;
    }
}
