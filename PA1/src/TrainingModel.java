import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.json.*;

/**
 * Created by Sofonias on 2/22/2015.
 * This class contains full models for all upspeak/downspeak n-grams
 * These models contain all n-gram data (lemma, pos,...) and can be fully serialized and parsed from files
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

    public static String DIRECTORY = "models";

    public static void main(String[] args) throws IOException {
        TrainingModel t = new TrainingModel();
        t.serialize(DIRECTORY);
    }

    /**
     * Creates all new models from the emails by DataProcessor. It requires DataProcessor to have been initialized beforehand
     */
    public TrainingModel() throws IOException{
        if (!DataProcessor.data_set.containsKey("up_train") || !DataProcessor.data_set.containsKey("down_train"))
            new DataProcessor(true, false, false);
        createNewNGramModels();
    }

    /**
     * Parses a set of models from a json file saved by the serialize method above
     */
    public TrainingModel(String directory) throws FileNotFoundException {
        JsonArray arr;

        arr = Json.createReader(new StringReader(readStringFromFile(directory + "/upUnigram.json"))).readArray();
        for (int i = 0; i < arr.size(); i++) {
            Unigram u = new Unigram(arr.getJsonObject(i));
            upUnigramTokens += u.count;
            upUnigramModel.put(u.key, u);
        }

        arr = Json.createReader(new StringReader(readStringFromFile(directory + "/downUnigram.json"))).readArray();
        for (int i = 0; i < arr.size(); i++) {
            Unigram u = new Unigram(arr.getJsonObject(i));
            downUnigramTokens += u.count;
            downUnigramModel.put(u.key, u);
        }

        arr = Json.createReader(new StringReader(readStringFromFile(directory + "/upBigram.json"))).readArray();
        for (int i = 0; i < arr.size(); i++) {
            Bigram b = new Bigram(arr.getJsonObject(i), upUnigramModel);
            upBigramTokens += b.count;
            upBigramModel.put(b.key, b);
        }

        arr = Json.createReader(new StringReader(readStringFromFile(directory + "/downBigram.json"))).readArray();
        for (int i = 0; i < arr.size(); i++) {
            Bigram b = new Bigram(arr.getJsonObject(i), downUnigramModel);
            downBigramTokens += b.count;
            downBigramModel.put(b.key, b);
        }

        arr = Json.createReader(new StringReader(readStringFromFile(directory + "/upTrigram.json"))).readArray();
        for (int i = 0; i < arr.size(); i++) {
            Trigram t = new Trigram(arr.getJsonObject(i), upUnigramModel);
            upTrigramTokens += t.count;
            upTrigramModel.put(t.key, t);
        }

        arr = Json.createReader(new StringReader(readStringFromFile(directory + "/downTrigram.json"))).readArray();
        for (int i = 0; i < arr.size(); i++) {
            Trigram t = new Trigram(arr.getJsonObject(i), downUnigramModel);
            downTrigramTokens += t.count;
            downTrigramModel.put(t.key, t);
        }
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
        writeStringToFile(directory + "/upUnigram.json", getUnigramJsonString(upUnigramModel));
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

        // Upspeak Training Models
        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("down_train")) {

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

        // Downspeak Training Models
        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("up_train")) {

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
