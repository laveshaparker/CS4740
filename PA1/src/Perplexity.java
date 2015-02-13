import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class Perplexity {

    public static final String UNIGRAM = "unigram";
    public static final String BIGRAM = "bigram";
    public static final String TRIGRAM = "trigram";

    public static final String DOWN = "down";
    public static final String UP = "up";

    public static TreeMap<String, SortedMap<String, Unigram>> unigram_models = new TreeMap<>();
    public static TreeMap<String, SortedMap<String, Bigram>> bigram_models = new TreeMap<>();
    public static TreeMap<String, SortedMap<String, Trigram>> trigram_models = new TreeMap<>();

    public static String[] trainingSets = new String[]{"up_train", "down_train"};
    public static String[] validationSets = new String[]{"up_validation", "down_validation"};

    public static int unigram_tokens = 0;
    public static int bigram_tokens = 0;
    public static int trigram_tokens = 0;

    public static double up_unigram_perplexity = 1;
    public static double down_unigram_perplexity = 1;
    public static double up_bigram_perplexity = 1;
    public static double down_bigram_perplexity = 1;
    public static double up_trigram_perplexity = 1;
    public static double down_trigram_perplexity = 1;

    public static void main(String[] args) throws Exception {
        new Perplexity();
    }

    Perplexity() throws FileNotFoundException {
        unigram_models.put(UP, new TreeMap<>());
        unigram_models.put(DOWN, new TreeMap<>());
        bigram_models.put(UP, new TreeMap<>());
        bigram_models.put(DOWN, new TreeMap<>());
        trigram_models.put(UP, new TreeMap<>());
        trigram_models.put(DOWN, new TreeMap<>());

        new DataProcessor();
        getNGrams();
        computePerplexities();
        System.out.println(up_unigram_perplexity);
        System.out.println(down_unigram_perplexity);
        System.out.println(up_bigram_perplexity);
        System.out.println(down_bigram_perplexity);
        System.out.println(up_trigram_perplexity);
        System.out.println(down_trigram_perplexity);
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

        for (String k : trainingSets) {

            key = k.charAt(0) == 'd' ? DOWN : UP;

            for (ArrayList<Unigram> sentence : DataProcessor.data_set.get(k)) {

                unigram_tokens += sentence.size();
                bigram_tokens += Math.max(sentence.size() - 1, 0);
                trigram_tokens += Math.max(sentence.size() - 2, 0);

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

    private double getUnigramProbability(String token, String speak_type) {
        String key = token;
        return
                (
                        (unigram_models.get(speak_type).containsKey(key) ? (double)unigram_models.get(speak_type).get(key).count : 0) + 1.0
                ) /
                (
                        (double)unigram_tokens + (double)unigram_models.get(speak_type).size()
                );
    }

    private double getBigramProbability(String token1, String token2, String speak_type) {
        String key = token1 + " " + token2;
        return getUnigramProbability(token1, speak_type) *
                (
                        (bigram_models.get(speak_type).containsKey(key) ? (double)bigram_models.get(speak_type).get(key).count : 0) + 1.0
                )
                /
                (
                        (double)bigram_tokens + (double)bigram_models.get(speak_type).size()
                );
    }

    private double getTrigramProbability(String token1, String token2, String token3, String speak_type) {
        String key = token1 + " " + token2 + " " + token3;
        return getBigramProbability(token1, token2, speak_type) *
                (
                        (trigram_models.get(speak_type).containsKey(key) ? (double)trigram_models.get(speak_type).get(token1 + " " + token2 + " " + token3).count : 0) + 1.0
                )
                /
                (
                        (double)trigram_tokens + (double)unigram_models.get(speak_type).size()
                );
    }

    private void computePerplexities() {

        Unigram token;
        String speakType;
        double tmpUnigramPerplexity;
        double tmpBigramPerplexity;
        double tmpTrigramPerplexity;

        for (String k : validationSets) {

            speakType = k.charAt(0) == 'd' ? DOWN : UP;
            tmpUnigramPerplexity = 1;
            tmpBigramPerplexity = 1;
            tmpTrigramPerplexity = 1;

            for (ArrayList<Unigram> sentence : DataProcessor.data_set.get(k)) {
                /* Start at index one because index 0 always contains <s>, which we are given to assume has probability = 1 */
                for (int idx = 1; idx < sentence.size(); idx++) {
                    /* Compute Unigram perplexity */
                    tmpUnigramPerplexity *= (1.0 / getUnigramProbability(sentence.get(idx).key, speakType));

                    /* Compute bigram perplexity (idx starts at one)*/
                    tmpBigramPerplexity *= (1.0 / getBigramProbability(sentence.get(idx - 1).key, sentence.get(idx).key, speakType));

                    /* Compute trigram perplexity */
                    if (idx >= 2 ) {
                        tmpBigramPerplexity *= (1.0 / getTrigramProbability(sentence.get(idx - 2).key, sentence.get(idx - 1).key, sentence.get(idx).key, speakType));
                    }
                }
            }

            if (speakType.equals(DOWN)) {
                down_unigram_perplexity *= tmpUnigramPerplexity;
                down_bigram_perplexity *= tmpBigramPerplexity;
                down_trigram_perplexity *= tmpTrigramPerplexity;
            } else {
                up_unigram_perplexity *= tmpUnigramPerplexity;
                up_bigram_perplexity *= tmpBigramPerplexity;
                up_trigram_perplexity *= tmpTrigramPerplexity;
            }

        }
    }
}
