import java.util.ArrayList;
import java.util.SortedMap;

public class Perplexity {

    public static void main(String[] args) throws Exception {
        TrainingModel t = new TrainingModel(TrainingModel.DIRECTORY);
        new DataProcessor(false, true, true);
        findPerplexitiesWithLaplace(t);
    }

    public static double getUnigramConditionalProbabilityWithLaplace(Unigram u, SortedMap<String, Unigram> model, double totalTokens) {
        return (model.containsKey(u.key) ? (double)model.get(u.key).count  + 1.0 : 1.0) / (totalTokens + (double)model.size());
    }

    public static double getBigramConditionalProbabilityWithLaplace(Unigram u1, Unigram u2, SortedMap<String, Unigram> unigramModel, SortedMap<String, Bigram> bigramModel) {
        Bigram b = new Bigram(u1, u2);
        return (
                    bigramModel.containsKey(b.key) ? (double)bigramModel.get(b.key).count + 1.0 : 1.0
                ) / (
                    (double)unigramModel.size() + (unigramModel.containsKey(u1.key) ? unigramModel.get(u1.key).count: 0)
                );
    }

    public static double getTrigramConditionalProbabilityWithLaplace(Unigram u1, Unigram u2, Unigram u3, SortedMap<String, Unigram> unigramModel, SortedMap<String, Bigram> bigramModel, SortedMap<String, Trigram> trigramModel) {
        Trigram t = new Trigram(u1, u2, u3);
        Bigram b = new Bigram(u1, u2);
        return (
                trigramModel.containsKey(t.key) ? (double)trigramModel.get(t.key).count + 1.0 : 1.0
                ) / (
                    (double)unigramModel.size() + (bigramModel.containsKey(b.key) ? bigramModel.get(b.key).count: 0)
                );
    }

    public static void findPerplexitiesWithLaplace(TrainingModel trainingModel) {

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
                upUnigramPerplexity -= Math.log(getUnigramConditionalProbabilityWithLaplace(sentence.get(idx), trainingModel.upUnigramModel, trainingModel.upUnigramTokens));

                /* Compute bigram perplexity (idx starts at one)*/
                upBigramPerplexity -= Math.log(
                        getBigramConditionalProbabilityWithLaplace(
                                sentence.get(idx - 1),
                                sentence.get(idx),
                                trainingModel.upUnigramModel,
                                trainingModel.upBigramModel
                        )
                );

                /* Compute trigram perplexity */
                if (idx >= 2 ) {
                    upTrigramPerplexity -= Math.log(
                            getTrigramConditionalProbabilityWithLaplace(
                                    sentence.get(idx - 2),
                                    sentence.get(idx - 1),
                                    sentence.get(idx),
                                    trainingModel.upUnigramModel,
                                    trainingModel.upBigramModel,
                                    trainingModel.upTrigramModel
                            )
                    );
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
                downUnigramPerplexity -= Math.log(getUnigramConditionalProbabilityWithLaplace(sentence.get(idx), trainingModel.downUnigramModel, trainingModel.downUnigramTokens));

                /* Compute bigram perplexity (idx starts at one)*/
                downBigramPerplexity -= Math.log(
                        getBigramConditionalProbabilityWithLaplace(
                                sentence.get(idx - 1),
                                sentence.get(idx),
                                trainingModel.downUnigramModel,
                                trainingModel.downBigramModel
                        )
                );

                /* Compute trigram perplexity */
                if (idx >= 2 ) {
                    downTrigramPerplexity -= Math.log(
                            getTrigramConditionalProbabilityWithLaplace(
                                    sentence.get(idx - 2),
                                    sentence.get(idx - 1),
                                    sentence.get(idx),
                                    trainingModel.downUnigramModel,
                                    trainingModel.downBigramModel,
                                    trainingModel.downTrigramModel
                            )
                    );
                }
            }
        }

        System.out.println("Downspeak Unigram Perplexity: " + downUnigramPerplexity/ downNumberTokens);
        System.out.println("Downspeak Bigram Perplexity: " + downBigramPerplexity/ downNumberTokens);
        System.out.println("Downspeak Trigram Perplexity: " + downTrigramPerplexity / downNumberTokens);
    }
}
