package oleksii.bashuk.hash.signs;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static int[] bytesToInts(byte[] bytes, int sizeInBits) {
        BigInteger bigInt = new BigInteger(1, bytes);
        int totalBits = bytes.length * 8;
        int[] response = new int[(totalBits - 1) / sizeInBits + 1];

        for (int i = 0; i < totalBits; i += sizeInBits) {
            response[i] = bigInt.shiftRight(totalBits - (i + sizeInBits)).and(BigInteger.valueOf((1L << sizeInBits) - 1)).intValueExact();
        }
        return response;
    }

    public static int getBlockFromByteArray(byte[] bytes, int sizeInBits, int blockNumber) {
        BigInteger bigInt = new BigInteger(1, bytes);
        int totalBits = bytes.length * 8;
        int i = blockNumber * sizeInBits;
        return bigInt.shiftRight(totalBits - (i + sizeInBits)).and(BigInteger.valueOf((1L << sizeInBits) - 1)).intValueExact();
    }

    public static String generateFileName(String filename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String dateTime = LocalDateTime.now().format(formatter);
        return Paths.get("data", "history", filename + "_" + dateTime + ".txt").toString();
    }
}
