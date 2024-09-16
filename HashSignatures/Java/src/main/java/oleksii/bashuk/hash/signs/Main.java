package oleksii.bashuk.hash.signs;

import oleksii.bashuk.hash.signs.hash.HashFunction.*;
import oleksii.bashuk.hash.signs.hash.MyHashFunction;
import oleksii.bashuk.hash.signs.hash.MyHashFunction.*;
import oleksii.bashuk.hash.signs.measure.GWOTSMeasures;
import oleksii.bashuk.hash.signs.realisation.gwots.Simulation;
import oleksii.bashuk.hash.signs.signature.Signature.*;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS.*;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
//        debugMyHash();
//        debugWOTSBlock();
//        debugGWOTSSimulation();
//        debugGWOTSPlusSimulation();
        measureGWOTS();
    }

    private static void debugMyHash() {
        MyHash myHash = new MyHash(longToBytes(1023), null);
        System.out.println(myHash.getHexdigests());

        MyHashFunction myHashFunction;
        try {
            myHashFunction = new MyHashFunction("SHA-256");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }

        String test = "My name";
        byte[] testb = test.getBytes(StandardCharsets.UTF_8);
        myHash = myHashFunction.hash(testb);
        System.out.println(myHash.getHexdigests());

        myHash = myHashFunction.hashString("My name");
        System.out.println(myHash.getHexdigests());
        myHash = myHashFunction.hashString("My name");
        System.out.println(myHash.getHexdigests());
        myHash = myHashFunction.hashString("My name ");
        System.out.println(myHash.getHexdigests());

        try {
            myHashFunction = new MyHashFunction("SHA-512");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }
        myHash = myHashFunction.hashString("My name");
        System.out.println(myHash.getHexdigests());
        myHash = myHashFunction.hashString("My name");
        System.out.println(myHash.getHexdigests());
        myHash = myHashFunction.hashString("My name ");
        System.out.println(myHash.getHexdigests());
    }

    private static void debugWOTSBlock() {
        MyHashFunction myHashFunction;
        try {
            myHashFunction = new MyHashFunction("SHA-256");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }

        BlockWOTS blockWOTS = new BlockWOTS(myHashFunction, 8, 123);

        Pair<SecKey, PubKey> key = blockWOTS.gen();
        BlockWOTSSecKey sk = (BlockWOTSSecKey) key.getLeft();
        BlockWOTSPubKey pk = (BlockWOTSPubKey) key.getRight();
        BlockWOTSPubKey hashedPubKey = new BlockWOTSPubKey(pk.value().update());

        byte b = longToBytes(8)[Long.BYTES - 1];
        BlockWOTSMessage msg = new BlockWOTSMessage(b);

        BlockWOTSSign sign = blockWOTS.sign(sk, msg);
        Hash hash = sign.value().update(8);

        boolean result = blockWOTS.vrfy(hashedPubKey, sign, msg);
        System.out.println("end");
    }

    private static void debugGWOTSSimulation() {
        MyHashFunction myHashFunction;
        try {
            myHashFunction = new MyHashFunction("SHA-512");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }

        Simulation.run(myHashFunction, 9, 3, 5, true);
    }

    private static void debugGWOTSPlusSimulation() {
        MyHashFunction myHashFunction;
        try {
            myHashFunction = new MyHashFunction("SHA-512");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }

        Simulation.runPlus(myHashFunction, 9, 3, 5, true);
    }

    private static void measureGWOTS() {
        MyHashFunction myHashFunction;
        try {
            myHashFunction = new MyHashFunction("SHA-512");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }

        GWOTSMeasures measures = Simulation.runPlus(myHashFunction, 8, 64, 1000, false);
        if (measures != null) {
            measures.saveToFile("gwots_plus");
        }
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
}