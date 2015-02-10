import java.io.FileNotFoundException;
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

    public static TreeMap<String, SortedMap<String, Integer>> models = new TreeMap<String, SortedMap<String, Integer>>();

    UnsmoothedNGram() throws FileNotFoundException {
        models.put("unigram_up", new TreeMap<String, Integer>());
        models.put("unigram_down", new TreeMap<String, Integer>());
        models.put("bigram_up", new TreeMap<String, Integer>());
        models.put("bigram_down", new TreeMap<String, Integer>());
        models.put("trigram_up", new TreeMap<String, Integer>());
        models.put("trigram_down", new TreeMap<String, Integer>());

        new DataProcessor();
        getUnigrams();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new UnsmoothedNGram();
    }

    /**
     * Iterates through all of the data in DataProcessor.data_set and calculates the unigram,
     * bigram, and trigram counts of each of those sentences.
     * @todo for each sentence, add to unigram, bigram, and trigram counts
     * @todo Figure out how to count certain punctuation as a separate token
     */
    private void getUnigrams() {

        String[] tokens;
        String unigram, bigram, trigram, up_down, key;
        int count;

        for (String k : DataProcessor.data_set.keySet()) {

            up_down = k.charAt(0) == 'd' ? "_down" : "_up";

            for (String sentence : DataProcessor.data_set.get(k)) {
                tokens = sentence.split("\\s+");

                for (int idx = 0; idx < tokens.length; idx++) {
                    unigram = tokens[idx];
                    key = UnsmoothedNGram.UNIGRAM + up_down;
                    count = models.get(key).containsKey(unigram) ? models.get(key).get(unigram) + 1 : 1;
                    models.get(key).put(unigram, count);

                    if (idx + 1 < tokens.length) {
                        // Update bigrams
                        bigram = tokens[idx] + " " + tokens[idx + 1];
                        key = UnsmoothedNGram.BIGRAM + up_down;
                        count = models.get(key).containsKey(bigram) ? models.get(key).get(bigram) + 1 : 1;
                        models.get(key).put(bigram, count);
                    }

                    if (idx + 2 < tokens.length) {
                        // Update trigrams
                        trigram = tokens[idx] + " " + tokens[idx + 1] + " " + tokens[idx + 2];
                        key = UnsmoothedNGram.TRIGRAM + up_down;
                        count = models.get(key).containsKey(trigram) ? models.get(key).get(trigram) + 1 : 1;
                        models.get(key).put(trigram, count);
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
    public Double findConditionalProbability(String NGram, String N) throws Exception {
        Double prob = 0.0;
        Integer count;
        switch (N) {
            case UnsmoothedNGram.UNIGRAM:
                count  = models.get(UnsmoothedNGram.UNIGRAM + "_down").get(NGram);
                count += models.get(UnsmoothedNGram.UNIGRAM + "_up").get(NGram);
                break;
            case UnsmoothedNGram.BIGRAM:
                count  = models.get(UnsmoothedNGram.BIGRAM + "_down").get(NGram);
                count += models.get(UnsmoothedNGram.BIGRAM + "_up").get(NGram);
                break;
            case UnsmoothedNGram.TRIGRAM:
                count  = models.get(UnsmoothedNGram.TRIGRAM + "_down").get(NGram);
                count += models.get(UnsmoothedNGram.TRIGRAM + "_up").get(NGram);
                break;
            default:
                throw new Exception("Invalid value for N.");
        }
        return 1.0;   // Just a stub for now so it'll compile
    }

}

