package oleksii.bashuk.hash.signs.measure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static oleksii.bashuk.hash.signs.Utils.generateFileName;

public class MTSMeasures {
    private final String hashFunctionName;
    private final int arity;
    private final boolean useProxyNode;

    private final List<Long> genTime;
    private final List<Long> signTime;
    private final List<Long> signCreationTime;
    private final List<Long> vrfyTime;
    private final List<Long> signSize;

    public MTSMeasures(String hashFunctionName,
                       int arity,
                       boolean useProxyNode) {
        this(hashFunctionName, arity, useProxyNode,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public MTSMeasures(String hashFunctionName,
                       int arity,
                       boolean useProxyNode,
                       List<Long> genTime,
                       List<Long> signTime,
                       List<Long> signCreationTime,
                       List<Long> vrfyTime,
                       List<Long> signSize) {
        this.hashFunctionName = hashFunctionName;
        this.arity = arity;
        this.useProxyNode = useProxyNode;
        this.genTime = genTime;
        this.signTime = signTime;
        this.signCreationTime = signCreationTime;
        this.vrfyTime = vrfyTime;
        this.signSize = signSize;
    }

    public void addGenTime(long genTime) {
        this.genTime.add(genTime);
    }
    public void addSignTime(long signTime) {
        this.signTime.add(signTime);
    }
    public void addSignCreationTime(long signCreationTime) {
        this.signCreationTime.add(signCreationTime);
    }
    public void addVrfyTime(long vrfyTime) {
        this.vrfyTime.add(vrfyTime);
    }
    public void addSignSize(long signSize) {
        this.signSize.add(signSize);
    }

    public List<Long> getGenTime() {
        return genTime;
    }
    public List<Long> getSignTime() {
        return signTime;
    }
    public List<Long> getSignCreationTimeTime() {
        return signCreationTime;
    }
    public List<Long> getVrfyTime() {
        return vrfyTime;
    }
    public List<Long> getSignSize() {
        return signSize;
    }

    public void saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(generateFileName(
                filename + "_" + hashFunctionName + "_" + arity + "_" + (useProxyNode ? "with_proxy" : "without_proxy"))))) {
            writer.write("Configuration: " + hashFunctionName + " " + arity + " " + useProxyNode);
            writer.newLine();
            writeMeasure(writer, genTime);
            writeMeasure(writer, signTime);
            writeMeasure(writer, signCreationTime);
            writeMeasure(writer, vrfyTime);
            writeMeasure(writer, signSize);
            System.out.println("Result is saved into the file: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMeasure(BufferedWriter writer, List<Long> measure) throws IOException {
        for (Long m : measure) {
            writer.write(m.toString() + " ");
        }
        writer.newLine();
    }

    public static MTSMeasures loadFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            if ((line = reader.readLine()) == null) {
                throw new IOException();
            }

            String[] parts = line.split(":");
            String[] configs = parts[1].trim().split("\\s+");
            String hashFunctionName = configs[0];
            int arity = Integer.parseInt(configs[1]);
            boolean useProxyNode = Boolean.parseBoolean(configs[2]);
            List<Long> genTime = readMeasure(reader);
            List<Long> signTime = readMeasure(reader);
            List<Long> signCreationTime = readMeasure(reader);
            List<Long> vrfyTime = readMeasure(reader);
            List<Long> signSize = readMeasure(reader);

            return new MTSMeasures(hashFunctionName, arity, useProxyNode,
                    genTime, signTime, signCreationTime, vrfyTime, signSize);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Long> readMeasure(BufferedReader reader) throws IOException {
        List<Long> measure = new ArrayList<>();
        String line;
        if ((line = reader.readLine()) == null) {
            throw new IOException();
        }

        String[] values = line.split("\\s+");
        for (String value: values) {
            measure.add(Long.parseLong(value));
        }

        return measure;
    }
}
