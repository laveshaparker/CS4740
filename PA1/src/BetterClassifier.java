import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Sofonias on 2/23/2015.
 */
public class BetterClassifier {

    private static int DOWNSPEAK = 0;
    private static int UPSPEAK = 1;

    public static void main(String[] args) throws IOException {

        TrainingModel t = new TrainingModel(TrainingModel.DIRECTORY);

        new DataProcessor(false, true, false);

        double correct = 0;
        double total = 0;

        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("up_validation")) {
            int prediction = classify(t, sentence);
            if (prediction == UPSPEAK) correct++;
            total++;
        }

        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("down_validation")) {
            int prediction = classify(t, sentence);
            if (prediction == DOWNSPEAK) correct++;
            total++;
        }

        System.out.println(correct / total);

    }

    public static int classify(TrainingModel t, ArrayList<Unigram> emailList) {
        double downspeakProbabilty = 0;
        double upspeakProbabilty = 0;

        for (Unigram u : emailList) {
            upspeakProbabilty += Math.log(Perplexity.getUnigramProbabilityWithLaplace(u,t.upUnigramModel,t.upUnigramTokens));
            downspeakProbabilty += Math.log(Perplexity.getUnigramProbabilityWithLaplace(u,t.downUnigramModel,t.downUnigramTokens));
        }

        if (upspeakProbabilty > downspeakProbabilty) return UPSPEAK;
        return DOWNSPEAK;
    }

}
