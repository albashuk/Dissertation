package oleksii.bashuk.hash.signs.measure;

import oleksii.bashuk.hash.signs.measure.record.GWOTSMeasureRecord;
import oleksii.bashuk.hash.signs.measure.record.MeasureRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static oleksii.bashuk.hash.signs.Utils.generateFileName;

public class GWOTSMeasures {
    private ArrayList<GWOTSMeasureRecord> measures;
    private String hashFunctionName;
    private Integer bitSizeOfBlock;
    private Integer numberOfA;
    private Integer numberOfCommunicationRounds;

    public GWOTSMeasures() {
        this(null, null, null, null);
    }

    public GWOTSMeasures(String filename) {
        loadFromFile(filename);
    }

    public GWOTSMeasures(String hashFunctionName,
                         Integer bitSizeOfBlock,
                         Integer numberOfA,
                         Integer numberOfCommunicationRounds) {
        this.measures = new ArrayList<>();
        this.hashFunctionName = hashFunctionName;
        this.bitSizeOfBlock = bitSizeOfBlock;
        this.numberOfA = numberOfA;
        this.numberOfCommunicationRounds = numberOfCommunicationRounds;
    }

    public void addMeasure(MeasureRecord measure) {
        measures.add((GWOTSMeasureRecord) measure);
    }

    public GWOTSMeasureRecord getRecord(int index) {
        return measures.get(index);
    }

    public ArrayList<GWOTSMeasureRecord> getRecords() {
        return measures;
    }

    public String getHashFunctionName() {
        return hashFunctionName;
    }

    public int getBitSizeOfBlock() {
        return bitSizeOfBlock;
    }

    public int getNumberOfA() {
        return numberOfA;
    }

    public int size() {
        return numberOfCommunicationRounds;
    }

    public void saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(generateFileName(
                filename + "_" + hashFunctionName + "_" + bitSizeOfBlock + "_" + numberOfA + "_" + numberOfCommunicationRounds)))) {
            writer.write("Configuration: " + hashFunctionName + " " + bitSizeOfBlock + " " + numberOfA + " " + numberOfCommunicationRounds);
            writer.newLine();
            for (int i = 0; i < measures.size(); i++) {
                writer.write("Iteration " + (i + 1) + ": " + measures.get(i).toString());
                writer.newLine();
            }
            System.out.println("Result is saved into the file: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();

            if (line != null) {
                String[] parts = line.split(":");
                String[] configs = parts[1].trim().split(" ");
                hashFunctionName = configs[0];
                bitSizeOfBlock = Integer.parseInt(configs[1]);
                numberOfA = Integer.parseInt(configs[2]);
                numberOfCommunicationRounds = Integer.parseInt(configs[3]);
            }

            measures = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                int iteration = Integer.parseInt(parts[0].replace("Iteration ", "").trim());
                String[] data = parts[1].trim().split(" ");

                double buildRequest = Double.parseDouble(data[0]);
                List<Double> responses = new ArrayList<>(data.length - 2);
                for (int i = 1; i < data.length - 1; i++) {
                    responses.add(Double.parseDouble(data[i]));
                }
                double checkResponses = Double.parseDouble(data[data.length - 1]);

                measures.add(new GWOTSMeasureRecord(buildRequest, responses, checkResponses));
            }

            if (measures.size() != numberOfCommunicationRounds) {
                System.out.println("Data is not full");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
