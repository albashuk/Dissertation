package oleksii.bashuk.hash.signs.realisation.gwots;

import oleksii.bashuk.hash.signs.hash.HashFunction;
import oleksii.bashuk.hash.signs.measure.GWOTSMeasures;
import oleksii.bashuk.hash.signs.measure.record.GWOTSMeasureRecord;
import oleksii.bashuk.hash.signs.realisation.gwots.communication.*;
import oleksii.bashuk.hash.signs.signature.Signature.*;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Simulation {

    private static final int ttpSeedKey = 3407;
    private static final int bSeedKey = 4102;

    public static GWOTSMeasures run(HashFunction mainHashFunction,
                                    int bitSizeOfBlock,
                                    int numberOfA,
                                    int numberOfCommunicationRounds,
                                    boolean printLogs) {
        int bMessageSize = mainHashFunction.bitSize();
        if ((bMessageSize - 1) / bitSizeOfBlock + 1 < numberOfA) {
            System.out.println("Too many A for this configuration");
            return null;
        }

        GWOTSMeasures measures = new GWOTSMeasures(mainHashFunction.getName(), bitSizeOfBlock, numberOfA, numberOfCommunicationRounds);

        // ======================================================= TTP Part =======================================================

        BlockWOTS signProtocol = new BlockWOTS(mainHashFunction, bitSizeOfBlock, ttpSeedKey);

        B b = new B(mainHashFunction, signProtocol, new HashMap<>(), bitSizeOfBlock, bMessageSize, bSeedKey, 0);

        List<A> G = new ArrayList<>();
        Pair<SecKey, PubKey> key;
        for (int i = 1; i <= numberOfA; ++i) {
            key = signProtocol.gen();
            BlockWOTSSecKey sk = (BlockWOTSSecKey) key.getLeft();
            BlockWOTSPubKey pk = (BlockWOTSPubKey) key.getRight();
            G.add(new A(mainHashFunction, signProtocol, sk, pk, bitSizeOfBlock, i));
            b.addNewAToJournal(i, new BlockWOTSPubKey(pk.value().update()));
        }

        // ================================================== Communication Part ==================================================

        for (int i = 0; i < numberOfCommunicationRounds; ++i) {
            double buildRequestTime;
            List<Double> responseTimes = new ArrayList<>(numberOfA);
            double checkResponsesTime;

            long begin;
            long end;

            // ################################################## Build Requests ##################################################

            begin = System.nanoTime();
            BRequest request = b.buildRequest();
            end = System.nanoTime();

            buildRequestTime = (end - begin)/1e9;
            if (printLogs) {
                System.out.println("Build request time: " + (end - begin)/1e9);
            }

            // ################################################# Process Requests #################################################

            List<AResponse> responses = new ArrayList<>();
            for (A a: G) {
                begin = System.nanoTime();
                AResponse response = a.signMessage(request);
                end = System.nanoTime();
                responses.add(response);

                responseTimes.add((end - begin)/1e9);
                if (printLogs) {
                    System.out.println("Response time: " + (end - begin) / 1e9);
                }
            }

            // ################################################## Check Requests ##################################################

            boolean result = true;
            begin = System.nanoTime();
            for (AResponse response: responses) {
                if (!b.checkResponse(request, response)) {
                    result = false;
                    if (printLogs) {
                        System.out.print(" " + false);
                    }
                } else {
                    if (printLogs) {
                        System.out.print(" " + true);
                    }
                }
            }
            end = System.nanoTime();
            checkResponsesTime = (end - begin)/1e9;

            if (printLogs) {
                System.out.println();
                System.out.println("Response check time: " + (end - begin) / 1e9);
            }

            if (printLogs || !result) {
                System.out.println(i + " : " + result);
            }

            measures.addMeasure(new GWOTSMeasureRecord(buildRequestTime, responseTimes, checkResponsesTime));
        }

        return measures;
    }

    public static GWOTSMeasures runPlus(HashFunction mainHashFunction,
                                        int bitSizeOfBlock,
                                        int numberOfA,
                                        int numberOfCommunicationRounds,
                                        boolean printLogs) {
        int bMessageSize = mainHashFunction.bitSize();
        if ((bMessageSize - 1) / bitSizeOfBlock + 1 < numberOfA) {
            System.out.println("Too many A for this configuration");
            return null;
        }

        GWOTSMeasures measures = new GWOTSMeasures(mainHashFunction.getName(), bitSizeOfBlock, numberOfA, numberOfCommunicationRounds);

        // ======================================================= TTP Part =======================================================

        BlockWOTS signProtocol = new BlockWOTS(mainHashFunction, bitSizeOfBlock, ttpSeedKey);

        B b = new B(mainHashFunction, signProtocol, new HashMap<>(), bitSizeOfBlock, bMessageSize, bSeedKey, 0);

        List<A> G = new ArrayList<>();
        Pair<SecKey, PubKey> key;
        for (int i = 1; i <= numberOfA; ++i) {
            key = signProtocol.gen();
            BlockWOTSSecKey sk = (BlockWOTSSecKey) key.getLeft();
            BlockWOTSPubKey pk = (BlockWOTSPubKey) key.getRight();
            G.add(new A(mainHashFunction, signProtocol, sk, pk, bitSizeOfBlock, i));
            b.addNewAToJournal(i, new BlockWOTSPubKey(pk.value().update()));
        }

        // ================================================== Communication Part ==================================================

        for (int i = 0; i < numberOfCommunicationRounds; ++i) {
            double buildRequestTime;
            List<Double> responseTimes = new ArrayList<>(numberOfA);
            double checkResponsesTime;

            long begin;
            long end;

            // ################################################## Build Requests ##################################################

            begin = System.nanoTime();
            List<BRequestPlus> requests = b.buildRequestPlus(i);
            end = System.nanoTime();

            buildRequestTime = (end - begin)/1e9;
            if (printLogs) {
                System.out.println("Build request time: " + (end - begin)/1e9);
            }

            // ################################################# Process Requests #################################################

            List<AResponsePlus> responses = new ArrayList<>();
            for (int j = 0; j < requests.size(); ++j) {
                begin = System.nanoTime();
                AResponsePlus response = G.get(j).signMessagePlus(requests.get(j));
                end = System.nanoTime();
                responses.add(response);

                responseTimes.add((end - begin)/1e9);
                if (printLogs) {
                    System.out.println("Response time: " + (end - begin) / 1e9);
                }
            }

            // ################################################## Check Requests ##################################################

            boolean result = true;
            begin = System.nanoTime();
            for (int j = 0; j < requests.size(); ++j) {
                if (responses.get(j) == null) {
                    result = false;
                    System.out.print(" null");
                    continue;
                }

                if (!b.checkResponsePlus(requests.get(j), responses.get(j))) {
                    result = false;
                    if (printLogs) {
                        System.out.print(" " + false);
                    }
                } else {
                    if (printLogs) {
                        System.out.print(" " + true);
                    }
                }
            }
            end = System.nanoTime();
            checkResponsesTime = (end - begin)/1e9;

            if (printLogs) {
                System.out.println();
                System.out.println("Response check time: " + (end - begin) / 1e9);
            }

            if (printLogs || !result) {
                System.out.println(i + " : " + result);
            }

            measures.addMeasure(new GWOTSMeasureRecord(buildRequestTime, responseTimes, checkResponsesTime));
        }

        return measures;
    }
}
