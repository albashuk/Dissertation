package oleksii.bashuk.hash.signs;

import oleksii.bashuk.hash.signs.common.HashMessage;
import oleksii.bashuk.hash.signs.hash.HashFunction.*;
import oleksii.bashuk.hash.signs.hash.MyHashFunction;
import oleksii.bashuk.hash.signs.hash.MyHashFunction.*;
import oleksii.bashuk.hash.signs.measure.GWOTSMeasures;
import oleksii.bashuk.hash.signs.measure.MTSMeasures;
import oleksii.bashuk.hash.signs.realisation.gwots.Simulation;
import oleksii.bashuk.hash.signs.signature.Signature.*;
import oleksii.bashuk.hash.signs.signature.mts.NarySTBS;
import oleksii.bashuk.hash.signs.signature.mts.NarySTBS.*;
import oleksii.bashuk.hash.signs.signature.ots.Lamport;
import oleksii.bashuk.hash.signs.signature.ots.Lamport.*;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS.*;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
//        debugMyHash();
//        debugWOTSBlock();
//        debugGWOTSSimulation();
//        debugGWOTSPlusSimulation();
//        debugLamport();
//        debugNarySTBS();
        measureNarySTBS();
//        measureGWOTS();
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

    private static void debugLamport() {
        int seedKey = 123;

        MyHashFunction myHashFunction;
        try {
            myHashFunction = new MyHashFunction("SHA-512");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }

        Lamport lamport = new Lamport(myHashFunction, seedKey);

        Pair<SecKey, PubKey> key = lamport.gen();
        LamportSecKey sk = (LamportSecKey) key.getLeft();
        LamportPubKey pk = (LamportPubKey) key.getRight();

        long seed = seedKey + System.currentTimeMillis();
        Random random = new Random(seed);
        byte[] randomValue = new byte[32];
        random.nextBytes(randomValue);
        HashMessage msg = new HashMessage(myHashFunction.hash(randomValue));

        LamportSign sign = (LamportSign) lamport.sign(sk, msg);

//        random.nextBytes(randomValue);
//        msg = new LamportMessage(myHashFunction.hash(randomValue));

        System.out.println("Result: " + lamport.vrfy(pk, sign, msg));
    }

    private static void debugNarySTBS() {
        int seedKey = 123;
        int arity = 4;
        boolean useProxy = false;
        boolean measure = true;
        int iterations = 24;

        MyHashFunction myHashFunction;
        try {
            myHashFunction = new MyHashFunction("SHA-512");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }

        Lamport lamport = new Lamport(myHashFunction, seedKey);
        NarySTBS narySTBS = new NarySTBS(myHashFunction, lamport, arity, useProxy, measure);

        Pair<SecKey, PubKey> key = narySTBS.gen();
        NarySTBSSecKey sk = (NarySTBSSecKey) key.getLeft();
        NarySTBSPubKey pk = (NarySTBSPubKey) key.getRight();

        long seed = seedKey + System.currentTimeMillis();
        Random random = new Random(seed);
        byte[] randomValue = new byte[32];

        HashMessage msg = null;
        NarySTBSSign sign = null;
        for (int i = 0; i < iterations; i++) {
            random.nextBytes(randomValue);
            msg = new HashMessage(myHashFunction.hash(randomValue));
            sign = (NarySTBSSign) narySTBS.sign(sk, msg);
            System.out.println("Result " + (i + 1) + ": " + narySTBS.vrfy(pk, sign, msg) + " " + sign.chainNodes.size());
        }

//        NarySTBSSign sign = (NarySTBSSign) narySTBS.sign(sk, msg);

        random.nextBytes(randomValue);
        msg = new HashMessage(myHashFunction.hash(randomValue));

        System.out.println("Corrupted result: " + narySTBS.vrfy(pk, sign, msg));

        if (measure) {
            narySTBS.getMeasures().saveToFile("debug_narySTBS");
            MTSMeasures measures = MTSMeasures.loadFromFile("data/debug_narySTBS_SHA-512_4_without_proxy.txt");
        }
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

    private static void measureNarySTBS() {
        int seedKey = 123;
        int arity = 2;
        boolean useProxy = false;
        int iterations = 100000;
        long deadline = (long) (2 * 60 * 60 * 1e9);

        MyHashFunction myHashFunction;
        try {
            myHashFunction = new MyHashFunction("SHA-512");
        } catch (Exception ex) {
            System.out.println("Wrong hash function name");
            return;
        }

        Lamport lamport = new Lamport(myHashFunction, seedKey);
        NarySTBS narySTBS = new NarySTBS(myHashFunction, lamport, arity, useProxy, true);

        Pair<SecKey, PubKey> key = narySTBS.gen();
        NarySTBSSecKey sk = (NarySTBSSecKey) key.getLeft();
        NarySTBSPubKey pk = (NarySTBSPubKey) key.getRight();

        long seed = seedKey + System.currentTimeMillis();
        Random random = new Random(seed);
        byte[] randomValue = new byte[32];

        HashMessage msg = null;
        NarySTBSSign sign = null;
        long begin = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            random.nextBytes(randomValue);
            msg = new HashMessage(myHashFunction.hash(randomValue));
            sign = (NarySTBSSign) narySTBS.sign(sk, msg);
            boolean r = narySTBS.vrfy(pk, sign, msg);
            if ((i + 1) % (iterations / 1000) == 0) {
                long t = System.nanoTime() - begin;
                long h = t / (((long) (1e9)) * 60 * 60);
                long m = t / (((long) (1e9)) * 60) - h * 60;
                long s = t / (((long) (1e9))) - h * 60 * 60 - m * 60;
                System.out.println("Processed  " + ((i + 1) / (iterations / 100.0)) + "%.  " +
                        "The last vrfy result for  i=" + (i + 1) + ": " + r + ".  " +
                        "Tree height:  " + sign.chainNodes.size() + ".  " +
                        "Time from the beginning:  " + h + " h,  " + m + " m,  " + s + "s.");
            }
            if (System.nanoTime() - begin > deadline) {
                break;
            }
        }

        narySTBS.getMeasures().saveToFile("narySTBS");
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
}