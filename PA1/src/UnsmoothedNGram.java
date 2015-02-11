import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: vesha
 */
public class UnsmoothedNGram {

    public static final String UNIGRAM = "unigram";
    public static final String BIGRAM = "bigram";
    public static final String TRIGRAM = "trigram";

    public static final String DOWN = "down";
    public static final String UP = "up";

    public static TreeMap<String, SortedMap<String, Unigram>> unigram_models = new TreeMap<>();
    public static TreeMap<String, SortedMap<String, Bigram>> bigram_models = new TreeMap<>();
    public static TreeMap<String, SortedMap<String, Trigram>> trigram_models = new TreeMap<>();

    UnsmoothedNGram() throws FileNotFoundException {
        unigram_models.put(UP, new TreeMap<>());
        unigram_models.put(DOWN, new TreeMap<>());
        bigram_models.put(UP, new TreeMap<>());
        bigram_models.put(DOWN, new TreeMap<>());
        trigram_models.put(UP, new TreeMap<>());
        trigram_models.put(DOWN, new TreeMap<>());

        new DataProcessor();
        getNGrams();
    }

    public static void main(String[] args) throws Exception {
        new UnsmoothedNGram();
        outputToFile("out/unsmoothedNGrams.txt");
    }

    /**
     * Iterates through all of the data in DataProcessor.data_set and calculates the unigram,
     * bigram, and trigram counts of each of those sentences.
     */
    private void getNGrams() {

        Unigram unigram;
        Bigram bigram;
        Trigram trigram;
        String key;

        for (String k : DataProcessor.data_set.keySet()) {

            key = k.charAt(0) == 'd' ? DOWN : UP;

            for (ArrayList<Unigram> sentence : DataProcessor.data_set.get(k)) {
                for (int idx = 0; idx < sentence.size(); idx++) {
                    unigram = sentence.get(idx);
                    unigram.count = (unigram_models.get(key).containsKey(unigram.key) ? (unigram_models.get(key).get(unigram.key).count + 1) : 1);
                    unigram_models.get(key).put(unigram.key, unigram);

                    if (idx + 1 < sentence.size()) {
                        // Update bigrams
                        bigram = new Bigram(sentence.get(idx), sentence.get(idx + 1));
                        bigram.count = (bigram_models.get(key).containsKey(bigram.key) ? (bigram_models.get(key).get(bigram.key).count + 1) : 1);
                        bigram_models.get(key).put(bigram.key, bigram);
                    }

                    if (idx + 2 < sentence.size()) {
                        // Update trigrams
                        trigram = new Trigram(sentence.get(idx), sentence.get(idx + 1), sentence.get(idx + 2));
                        trigram.count = (trigram_models.get(key).containsKey(trigram.key) ? (trigram_models.get(key).get(trigram.key).count + 1) : 1);
                        trigram_models.get(key).put(trigram.key, trigram);
                    }
                }
            }
        }
    }

    /**
     * Computes the conditional probability of an N-gram as (# N-gram/#(N-1)-gram)
     * @param NGram, the phrase in question. Delimited by spaces (punctuation counts!).
     * @param N, UnsmoothedNGram.UNIGRAM, UnsmoothedNGram.BIGRAM, or UnsmoothedNGram.TRIGRAM
     * @param up_down, UnsmoothedNGram.DOWN or UnsmoothedNGram.UP
     * @return conditional probability of NGram
     */
    public double findConditionalProbability(String NGram, String N, String up_down) throws Exception {
        int count = 0;
        int nMinus1Count;
        Bigram bigram;
        Trigram trigram;
        switch (N) {
            case UNIGRAM:
                switch (up_down) {
                    case DOWN:
                        count  = unigram_models.get(DOWN).get(NGram).count;
                        nMinus1Count = unigram_models.get(DOWN).size(); // total # of tokens in the corpus
                        break;
                    case UP:
                        count += unigram_models.get(UP).get(NGram).count;
                        nMinus1Count = unigram_models.get(UP).size(); // total # of tokens in the corpus
                        break;
                    default:
                        throw new Exception("Invalid value for up_down");
                }
                break;
            case BIGRAM:
                switch (up_down) {
                    case DOWN:
                        bigram = bigram_models.get(DOWN).get(NGram);
                        count  = bigram.count;
                        nMinus1Count = unigram_models.get(DOWN).get(bigram.token1.key).count;
                        break;
                    case UP:
                        bigram = bigram_models.get(UP).get(NGram);
                        count  = bigram.count;
                        nMinus1Count = unigram_models.get(UP).get(bigram.token1.key).count;
                        break;
                    default:
                        throw new Exception("Invalid value for up_down");
                }
                break;
            case TRIGRAM:
                switch (up_down) {
                    case DOWN:
                        trigram = trigram_models.get(DOWN).get(NGram);
                        count  = trigram.count;
                        nMinus1Count = bigram_models.get(DOWN).get(new Bigram(trigram.token1, trigram.token2).key).count;
                        break;
                    case UP:
                        trigram = trigram_models.get(UP).get(NGram);
                        count  = trigram.count;
                        nMinus1Count = bigram_models.get(UP).get(new Bigram(trigram.token1, trigram.token2).key).count;
                        break;
                    default:
                        throw new Exception("Invalid value for up_down");
                }
                break;
            default:
                throw new Exception("Invalid value for N.");
        }
        return (double) count / nMinus1Count;
    }

    /**
     * Writes the contents of each of the N-grams in this class to a file.
     * @param filename, the file that is to be written to. Note that any existing file of the same name will be overwritten.
     * @return true if the file was successfully written to, false otherwise.
     */
    public static Boolean outputToFile(String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            unigramsToFile(writer);
            bigramsToFile(writer);
            trigramsToFile(writer);
            writer.close();
            return true;
        } catch (Exception e) {
            System.out.println("Failed to output to file. Error: " + e.getMessage());
            return false;
        }
    }

    private static void unigramsToFile(PrintWriter writer) {
        for (String key : unigram_models.keySet())  {
            for (Unigram unigram : unigram_models.get(key).values()) {
                writer.println(unigram.toString());
            }
        }
    }

    private static void bigramsToFile(PrintWriter writer) {
        for (String key : bigram_models.keySet())  {
            for (Bigram bigram : bigram_models.get(key).values()) {
                writer.println(bigram.toString());
            }
        }
    }

    private static void trigramsToFile(PrintWriter writer) {
        for (String key : trigram_models.keySet())  {
            for (Trigram trigram : trigram_models.get(key).values()) {
                writer.println(trigram.toString());
            }
        }
    }
}
