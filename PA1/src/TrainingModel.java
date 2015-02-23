import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.json.*;

/**
 * Created by Sofonias on 2/22/2015.
 */
public class TrainingModel {

    public TreeMap<String, Unigram> upUnigramModel = new TreeMap<>();
    public TreeMap<String, Unigram> downUnigramModel = new TreeMap<>();
    public TreeMap<String, Bigram> upBigramModel = new TreeMap<>();
    public TreeMap<String, Bigram> downBigramModel = new TreeMap<>();
    public TreeMap<String, Trigram> upTrigramModel = new TreeMap<>();
    public TreeMap<String, Trigram> downTrigramModel = new TreeMap<>();

    public int upUnigramTokens = 0;
    public int downUnigramTokens = 0;
    public int upBigramTokens = 0;
    public int downBigramTokens = 0;
    public int upTrigramTokens = 0;
    public int downTrigramTokens = 0;

    public static String dir = "models";

    public static void main(String args[]) throws java.io.FileNotFoundException {
//        Unigram u1 = new Unigram("Sofonias", "Pro", "uehh", "Sofo");
//        Unigram u2 = new Unigram("Alec", "Ver", "uhh", "Alexander the ");
//        Unigram u3 = new Unigram("La", "Adj", "um", "Lavish - a");
//
//        Bigram b1 = new Bigram(u1, u2);
//        Bigram b2 = new Bigram(u2, u3);
//
//        Trigram t1 = new Trigram(u1, u2, u3);
//
//        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
//        arrBuilder
//                .add(u1.asJson())
//                .add(u2.asJson())
//                .add(u3.asJson())
//                .add(b1.asJson())
//                .add(b2.asJson())
//                .add(t1.asJson());
//
//        JsonArray arr = arrBuilder.build();
//
//        System.out.println(arr.toString());
//
//        arr = Json.createReader(new StringReader(arr.toString())).readArray();
//        System.out.print("df");
//        TrainingModel m = new TrainingModel();
//        m.serialize(dir);

        TrainingModel m2 = new TrainingModel(dir);

    }

    /**
     * Creates all new models from the emails by DataProcessor
     */
    public TrainingModel() throws FileNotFoundException{
        new DataProcessor();
        createNewNGramModels();
    }

    /**
     * Parses a set of models from a json file saved by the serialize method above
     */
    public TrainingModel(String directory) throws FileNotFoundException {
        System.out.println(readStringFromFile(directory + "/upUnigrams.json"));
    }

    public String readStringFromFile(String fileName) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    /**
     * Serializes a representation of this set of models to a json file for later use
     */
    public void serialize(String directory) throws FileNotFoundException {
        writeStringToFile(directory + "/upUnigrams.json", getUnigramJsonString(upUnigramModel));
        writeStringToFile(directory + "/downUnigram.json", getUnigramJsonString(downUnigramModel));
        writeStringToFile(directory + "/upBigram.json", getBigramJsonString(upBigramModel));
        writeStringToFile(directory + "/downBigram.json", getBigramJsonString(downBigramModel));
        writeStringToFile(directory + "/upTrigram.json", getTrigramJsonString(upTrigramModel));
        writeStringToFile(directory + "/downTrigram.json", getTrigramJsonString(downTrigramModel));
    }

    public void writeStringToFile(String fileName, String str) throws java.io.FileNotFoundException {
        PrintWriter writer = new PrintWriter(fileName);
        writer.write(str);
        writer.close();
    }

    private String getUnigramJsonString(TreeMap<String, Unigram> model) {
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

        for (String key : model.keySet()) {
            arrBuilder.add(model.get(key).asJson());
        }

        return arrBuilder.build().toString();
    }

    private String getBigramJsonString(TreeMap<String, Bigram> model) {
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

        for (String key : model.keySet()) {
            arrBuilder.add(model.get(key).asJson());
        }

        return arrBuilder.build().toString();
    }

    private String getTrigramJsonString(TreeMap<String, Trigram> model) {
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

        for (String key : model.keySet()) {
            arrBuilder.add(model.get(key).asJson());
        }

        return arrBuilder.build().toString();
    }

    /**
     * Iterates through all of the data in DataProcessor.data_set and calculates the unigram,
     * bigram, and trigram counts of each of those sentences.
     */
    private void createNewNGramModels() {

        Unigram unigram;
        Bigram bigram;
        Trigram trigram;

        // Upspeak Training Model
        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("up_train")) {

            upUnigramTokens += sentence.size();
            upBigramTokens += Math.max(sentence.size() - 1, 0);
            upTrigramTokens += Math.max(sentence.size() - 2, 0);

            for (int idx = 0; idx < sentence.size(); idx++) {
                unigram = sentence.get(idx);
                unigram.count = (upUnigramModel.containsKey(unigram.key) ? (upUnigramModel.get(unigram.key).count + 1) : 1);
                upUnigramModel.put(unigram.key, unigram);

                if (idx + 1 < sentence.size()) {
                    // Update bigrams
                    bigram = new Bigram(sentence.get(idx), sentence.get(idx + 1));
                    bigram.count = (upBigramModel.containsKey(bigram.key) ? (upBigramModel.get(bigram.key).count + 1) : 1);
                    upBigramModel.put(bigram.key, bigram);
                }

                if (idx + 2 < sentence.size()) {
                    // Update trigrams
                    trigram = new Trigram(sentence.get(idx), sentence.get(idx + 1), sentence.get(idx + 2));
                    trigram.count = (upTrigramModel.containsKey(trigram.key) ? (upTrigramModel.get(trigram.key).count + 1) : 1);
                    upTrigramModel.put(trigram.key, trigram);
                }
            }
        }

        // Downspeak Training Model
        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("down_train")) {

            downUnigramTokens += sentence.size();
            downBigramTokens += Math.max(sentence.size() - 1, 0);
            downTrigramTokens += Math.max(sentence.size() - 2, 0);

            for (int idx = 0; idx < sentence.size(); idx++) {
                unigram = sentence.get(idx);
                unigram.count = (downUnigramModel.containsKey(unigram.key) ? (downUnigramModel.get(unigram.key).count + 1) : 1);
                downUnigramModel.put(unigram.key, unigram);

                if (idx + 1 < sentence.size()) {
                    // Update bigrams
                    bigram = new Bigram(sentence.get(idx), sentence.get(idx + 1));
                    bigram.count = (downBigramModel.containsKey(bigram.key) ? (downBigramModel.get(bigram.key).count + 1) : 1);
                    downBigramModel.put(bigram.key, bigram);
                }

                if (idx + 2 < sentence.size()) {
                    // Update trigrams
                    trigram = new Trigram(sentence.get(idx), sentence.get(idx + 1), sentence.get(idx + 2));
                    trigram.count = (downTrigramModel.containsKey(trigram.key) ? (downTrigramModel.get(trigram.key).count + 1) : 1);
                    downTrigramModel.put(trigram.key, trigram);
                }
            }
        }
    }



}
