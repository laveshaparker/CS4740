import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: vesha
 * @todo Output to file ngram count pairs
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
        getNgrams();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new UnsmoothedNGram();
        System.out.println(unigram_models.get(DOWN).toString());
    }

    /**
     * Iterates through all of the data in DataProcessor.data_set and calculates the unigram,
     * bigram, and trigram counts of each of those sentences.
     */
    private void getNgrams() {

        Unigram unigram;
        Bigram bigram;
        Trigram trigram;
        String key;

        for (String k : DataProcessor.data_set.keySet()) {

            key = k.charAt(0) == 'd' ? DOWN : UP;

            for (ArrayList<Unigram> sentence : DataProcessor.data_set.get(k)) {
                for (int idx = 0; idx < sentence.size(); idx++) {
                    unigram = sentence.get(idx);
                    unigram.count = unigram_models.get(key).containsKey(unigram.key) ? unigram_models.get(key).get(unigram.key).count + 1 : 1;
                    unigram_models.get(key).put(unigram.key, unigram);

                    if (idx + 1 < sentence.size()) {
                        // Update bigrams
                        bigram = new Bigram(sentence.get(idx), sentence.get(idx + 1));
                        bigram.count = bigram_models.get(key).containsKey(bigram.key) ? bigram_models.get(key).get(bigram.key).count + 1 : 1;
                        bigram_models.get(key).put(bigram.key, bigram);
                    }

                    if (idx + 2 < sentence.size()) {
                        // Update trigrams
                       trigram = new Trigram(sentence.get(idx), sentence.get(idx + 1), sentence.get(idx + 2));
                        trigram.count = trigram_models.get(key).containsKey(trigram.key) ? trigram_models.get(key).get(trigram.key).count + 1 : 1;
                        trigram_models.get(key).put(trigram.key, trigram);
                    }
                }
            }
        }
    }

    /**
     * @todo Complete this method.
     * @param NGram, the phrase in question
     * @param N, "unigram", "bigram", or "trigram"
     * @return
     */
    public Double findConditionalProbability(String NGram, String N, String up_down) throws Exception {
        Double prob = 0.0;
        Integer count;
        switch (N) {
            case UNIGRAM:
                count  = unigram_models.get(DOWN).get(NGram).count;
                count += unigram_models.get(UP).get(NGram).count;
                break;
            case BIGRAM:
                count  = bigram_models.get(DOWN).get(NGram).count;
                count += bigram_models.get(UP).get(NGram).count;
                break;
            case TRIGRAM:
                count  = trigram_models.get(DOWN).get(NGram).count;
                count += trigram_models.get(UP).get(NGram).count;
                break;
            default:
                throw new Exception("Invalid value for N.");
        }
        return 1.0;   // Just a stub for now so it'll compile
    }
}
