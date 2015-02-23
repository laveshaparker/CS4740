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

    TrainingModel trainingModel;

    public static void main(String[] args) throws Exception {
        new Perplexity();
    }

    Perplexity() throws FileNotFoundException {
        try {
            trainingModel = new TrainingModel(TrainingModel.DIRECTORY);
            new DataProcessor(false, true, false);
        }
        catch (FileNotFoundException e) {
            new DataProcessor();
            trainingModel = new TrainingModel();
        }

        computePerplexitiesWithLaplace();
    }

    private double getUnigramProbabilityWithLaplace(String key, SortedMap<String, Unigram> model, double totalTokens) {
        return (model.containsKey(key) ? (double)model.get(key).count  + 1.0 : 1.0) / (totalTokens + (double)model.size());
    }

    private double getBigramProbabilityWithLaplace(String key1, String key2, SortedMap<String, Bigram> model, double totalTokens) {
        String key = key1 + " " + key2;
        return (model.containsKey(key) ? (double)model.get(key).count + 1.0 : 1.0) / (totalTokens + (double)model.size());
    }

    private double getTrigramProbabilityWithLaplace(String key1, String key2, String key3, SortedMap<String, Trigram> model, double totalTokens) {
        String key = key1 + " " + key2 + " " + key3;
        return (model.containsKey(key) ? (double)model.get(key).count + 1.0 : 1.0) / (totalTokens + (double)model.size());
    }

    private void computePerplexitiesWithLaplace() {

        double upNumberTokens = 0;
        double upUnigramPerplexity = 0.0;
        double upBigramPerplexity = 0.0;
        double upTrigramPerplexity = 0.0;

        double tmpU = 0.0;
        double tmpB = 0.0;
        double tmpT = 0.0;

        //Up Validation

        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("up_validation")) {
            upNumberTokens += sentence.size() - 1;
            /* Start at index one because index 0 always contains <s>, which we are given to assume has probability = 1 */
            for (int idx = 1; idx < sentence.size(); idx++) {
                /* Compute Unigram perplexity */
                tmpU = Math.log(getUnigramProbabilityWithLaplace(sentence.get(idx).key, trainingModel.upUnigramModel, trainingModel.upUnigramTokens));
                upUnigramPerplexity -= tmpU;

                /* Compute bigram perplexity (idx starts at one)*/
                tmpB = Math.log(getBigramProbabilityWithLaplace(sentence.get(idx - 1).key, sentence.get(idx).key, trainingModel.upBigramModel, trainingModel.upBigramTokens));
                upBigramPerplexity -= (tmpU + tmpB);

                /* Compute trigram perplexity */
                if (idx >= 2 ) {
                    tmpT = Math.log(getTrigramProbabilityWithLaplace(sentence.get(idx - 2).key, sentence.get(idx - 1).key, sentence.get(idx).key, trainingModel.upTrigramModel, trainingModel.upTrigramTokens));
                    upTrigramPerplexity -= (tmpU + tmpB + tmpT);
                }
            }
        }

        System.out.println("Upspeak Unigram Perplexity: " + upUnigramPerplexity / upNumberTokens);
        System.out.println("Upspeak Bigram Perplexity: " + upBigramPerplexity / upNumberTokens);
        System.out.println("Upspeak Trigram Perplexity: " + upTrigramPerplexity / upNumberTokens);

        // Down validation

        double downNumberTokens = 0;
        double downUnigramPerplexity = 0.0;
        double downBigramPerplexity = 0.0;
        double downTrigramPerplexity = 0.0;

        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("down_validation")) {
            downNumberTokens += sentence.size() - 1;
            /* Start at index one because index 0 always contains <s>, which we are given to assume has probability = 1 */
            for (int idx = 1; idx < sentence.size(); idx++) {
                /* Compute Unigram perplexity */
                tmpU = Math.log(getUnigramProbabilityWithLaplace(sentence.get(idx).key, trainingModel.downUnigramModel, trainingModel.downUnigramTokens));
                downUnigramPerplexity -= tmpU;

                /* Compute bigram perplexity (idx starts at one)*/
                tmpB = Math.log(getBigramProbabilityWithLaplace(sentence.get(idx - 1).key, sentence.get(idx).key, trainingModel.downBigramModel, trainingModel.downBigramTokens));
                downBigramPerplexity -= (tmpU + tmpB);

                /* Compute trigram perplexity */
                if (idx >= 2 ) {
                    tmpT = Math.log(getTrigramProbabilityWithLaplace(sentence.get(idx - 2).key, sentence.get(idx - 1).key, sentence.get(idx).key, trainingModel.downTrigramModel, trainingModel.downTrigramTokens));
                    downTrigramPerplexity -= (tmpU + tmpB + tmpT);
                }
            }
        }

        System.out.println("Downspeak Unigram Perplexity: " + downUnigramPerplexity/ downNumberTokens);
        System.out.println("Downspeak Bigram Perplexity: " + downBigramPerplexity/ downNumberTokens);
        System.out.println("Downspeak Trigram Perplexity: " + downTrigramPerplexity / downNumberTokens);
    }
}
