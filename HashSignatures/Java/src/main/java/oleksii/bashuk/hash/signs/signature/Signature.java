package oleksii.bashuk.hash.signs.signature;


import org.apache.commons.lang3.tuple.Pair;

public interface Signature {

    Pair<SecKey, PubKey> gen();
    PubKey buildPubKeyFromSecKey(SecKey sk);
    Sign sign(SecKey sk, Message msg);
    boolean vrfy(PubKey pk, Sign sign, Message msg);

    interface SecKey {}
    interface PubKey {}
    interface Sign {}
    interface Message {}
}
