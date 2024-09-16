package oleksii.bashuk.hash.signs.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyHashFunction implements HashFunction {
    private final MessageDigest messageDigest;

    public MyHashFunction(String name) throws NoSuchAlgorithmException {
        this.messageDigest = MessageDigest.getInstance(name);
    }

    public String getName() {
        return messageDigest.getAlgorithm();
    }

    public MyHash hashString(String string) {
        return hashString(string, 1);
    }

    public MyHash hashString(String string, int iter) {
        return hash(string.getBytes(StandardCharsets.UTF_8), iter);
    }

    public MyHash hash(byte[] bytes) {
        return hash(bytes, 1);
    }

    public MyHash hash(byte[] bytes, int iter) {
        for (int i = 0; i < iter; ++i) {
            bytes = messageDigest.digest(bytes);
        }

        return new MyHash(bytes, this);
    }

    public MyHash valueOfString(String string) {
        return valueOf(string.getBytes(StandardCharsets.UTF_8));
    }

    public MyHash valueOf(byte[] bytes) {
        return new MyHash(bytes, this);
    }

    public int bitSize() {
        return messageDigest.getDigestLength() * 8;
    }

    public static class MyHash implements Hash {
        private final byte[] hash;
        private final MyHashFunction myHashFunction;

        public MyHash(byte[] hash,
                      MyHashFunction myHashFunction) {
            this.hash = hash;
            this.myHashFunction = myHashFunction;
        }

        public String getHexdigests() {
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }

        public byte[] getBytes() {
            return hash;
        }

        public MyHash updateByString(String string) {
            return updateByBytes(string.getBytes(StandardCharsets.UTF_8));
        }

        public MyHash updateByBytes(byte[] bytes) {
            return concatByBytes(bytes).update();
        }

        public MyHash update() {
            return myHashFunction.hash(hash);
        }

        public MyHash update(int iter) {
            return myHashFunction.hash(hash, iter);
        }

        public MyHash concatByBytes(byte[] bytes) {
            byte[] combined = new byte[hash.length + bytes.length];
            for (int i = 0; i < combined.length; ++i) {
                combined[i] = i < hash.length ? hash[i] : bytes[i - hash.length];
            }
            return new MyHash(combined, myHashFunction);
        }

        public MyHash concat(Hash hash) {
            return concatByBytes(hash.getBytes());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MyHash other = (MyHash) obj;
            return java.util.Arrays.equals(hash, other.hash);
        }
    }
}
