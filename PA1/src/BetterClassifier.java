import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Sofonias on 2/23/2015.
 */
public class BetterClassifier {

    private static final int DOWNSPEAK = 0;
    private static final int UPSPEAK = 1;

    public static void main(String[] args) throws IOException {

        TrainingModel t = new TrainingModel(TrainingModel.DIRECTORY);

        new DataProcessor(false, true, false);

        double correct = 0;
        double total = 0;

        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("up_validation")) {
            int prediction1 = classifyWithUnigrams(t, sentence); // Prediction using unigrams only
            int prediction2 = classifyWithBigrams(t, sentence); // Prediction using bigrams only
            int prediction3 = classifyWithTrigrams(t, sentence); // Prediction using trigrams only
            if (/*prediction1 + prediction2 +*/ prediction3 == UPSPEAK) correct++; // upspeak
            total++;
        }

        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("down_validation")) {
            //int prediction1 = classifyWithUnigrams(t, sentence);
            //int prediction2 = classifyWithBigrams(t, sentence);
            int prediction3 = classifyWithTrigrams(t, sentence);
            if (/*prediction1 + prediction2 +*/ prediction3 == DOWNSPEAK ) correct++; // downspeak
            total++;
        }

        System.out.println("Averaged accuracy: " + correct / total);
    }

    public static int classifyWithUnigrams(TrainingModel t, ArrayList<Unigram> emailList) {
        double downspeakProbabilty = Math.log((double)t.downUnigramTokens / ((double)t.downUnigramTokens + (double)t.upUnigramTokens));
        double upspeakProbabilty = Math.log((double)t.upUnigramTokens / ((double)t.downUnigramTokens + (double)t.upUnigramTokens));

        for (Unigram u : emailList) {
            upspeakProbabilty += Math.log(Perplexity.getUnigramConditionalProbabilityWithLaplace(u, t.upUnigramModel, t.upUnigramTokens));
            downspeakProbabilty += Math.log(Perplexity.getUnigramConditionalProbabilityWithLaplace(u, t.downUnigramModel, t.downUnigramTokens));
        }

        if (upspeakProbabilty > downspeakProbabilty) return UPSPEAK;
        return DOWNSPEAK;
    }

    public static int classifyWithBigrams(TrainingModel t, ArrayList<Unigram> emailList) {
        double downspeakProbabilty = Math.log((double)t.downBigramTokens / ((double)t.downBigramTokens + (double)t.upBigramTokens));
        double upspeakProbabilty = Math.log((double)t.upBigramTokens / ((double)t.downBigramTokens + (double)t.upBigramTokens));

        for (int i = 1; i < emailList.size(); i++) {
            Unigram u1 = emailList.get(i - 1);
            Unigram u2 = emailList.get(i);

            upspeakProbabilty += Math.log(Perplexity.getBigramConditionalProbabilityWithLaplace(u1, u2, t.upUnigramModel, t.upBigramModel));
            downspeakProbabilty += Math.log(Perplexity.getBigramConditionalProbabilityWithLaplace(u1, u2, t.downUnigramModel, t.downBigramModel));
        }

        if (upspeakProbabilty > downspeakProbabilty) return UPSPEAK;
        return DOWNSPEAK;
    }

    public static int classifyWithTrigrams(TrainingModel t, ArrayList<Unigram> emailList) {
        double downspeakProbabilty = Math.log((double)t.downTrigramTokens / ((double)t.downTrigramTokens + (double)t.upTrigramTokens));
        double upspeakProbabilty = Math.log((double)t.upTrigramTokens / ((double)t.downTrigramTokens + (double)t.upTrigramTokens));

        for (int i = 2; i < emailList.size(); i++) {
            Unigram u1 = emailList.get(i - 2);
            Unigram u2 = emailList.get(i - 1);
            Unigram u3 = emailList.get(i);

            upspeakProbabilty += Math.log(Perplexity.getTrigramConditionalProbabilityWithLaplace(u1, u2, u3, t.upUnigramModel, t.upBigramModel, t.upTrigramModel));
            downspeakProbabilty += Math.log(Perplexity.getTrigramConditionalProbabilityWithLaplace(u1, u2, u3, t.downUnigramModel, t.downBigramModel, t.downTrigramModel));
        }

        if (upspeakProbabilty > downspeakProbabilty) return UPSPEAK;
        return DOWNSPEAK;
    }

}
