package oleksii.bashuk.hash.signs.hash;

public interface HashFunction {

    String getName();
    Hash hashString(String string);
    Hash hashString(String string, int iter);
    Hash hash(byte[] bytes);
    Hash hash(byte[] bytes, int iter);
    Hash valueOfString(String string);
    Hash valueOf(byte[] bytes);
    int bitSize();

    interface Hash {
        String getHexdigests();
        byte[] getBytes();
        Hash updateByString(String string);
        Hash updateByBytes(byte[] bytes);
        Hash update();
        Hash update(int iter);
        Hash concatByBytes(byte[] bytes);
        Hash concat(Hash hash);
    }
}
