import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by Sofonias on 2/23/2015.
 */
public class BetterClassifier {

    private static final int DOWNSPEAK = 0;
    private static final int UPSPEAK = 1;

    public static void main(String[] args) throws IOException {

        TrainingModel t = new TrainingModel(TrainingModel.DIRECTORY);
        GoodTuring gt = new GoodTuring(t);
        classifyTestSets(t, gt);
    }

    private static void classifyTestSets(TrainingModel t, GoodTuring gt) throws IOException {
        new DataProcessor(false, false, true);

        //create output file
        PrintWriter writer = new PrintWriter("out/test_classified.txt", "UTF-8");
        writer.println("Id,Prediction");

        for (String emailNumber : DataProcessor.test_set.keySet()) {

            int upSpeakCount = 0;
            int downSpeakCount = 0;

            for (ArrayList<Unigram> sentence : DataProcessor.test_set.get(emailNumber)) {
                int prediction1 = classifyWithUnigrams(t, gt, sentence); // Prediction using unigrams only
                int prediction2 = classifyWithBigrams(t, gt, sentence); // Prediction using bigrams only
                int prediction3 = classifyWithTrigrams(t, gt, sentence); // Prediction using trigrams only
                if (prediction1 + prediction2 + prediction3 > 1) upSpeakCount++;
                else downSpeakCount++;
            }

            int classification = upSpeakCount > downSpeakCount ? UPSPEAK : DOWNSPEAK;

            writer.println(emailNumber + "," + classification);
        }

        writer.close();
    }

    private static void classifyValidationSets(TrainingModel t, GoodTuring gt) throws IOException {
        new DataProcessor(false, true, false);

        double correct = 0;
        double total = 0;

        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("up_validation")) {
            int prediction1 = classifyWithUnigrams(t, gt, sentence); // Prediction using unigrams only
            int prediction2 = classifyWithBigrams(t, gt, sentence); // Prediction using bigrams only
            int prediction3 = classifyWithTrigrams(t, gt, sentence); // Prediction using trigrams only
            if (prediction1 + prediction2 + prediction3 > 1) correct++; // upspeak
            total++;
        }

        for (ArrayList<Unigram> sentence : DataProcessor.data_set.get("down_validation")) {
            int prediction1 = classifyWithUnigrams(t, gt, sentence);
            int prediction2 = classifyWithBigrams(t, gt, sentence);
            int prediction3 = classifyWithTrigrams(t, gt, sentence);
            if (prediction1 + prediction2 + prediction3 <= 1 ) correct++; // downspeak
            total++;
        }

        System.out.println("Averaged accuracy: " + correct / total);
    }

    private static int classifyWithUnigrams(TrainingModel t, GoodTuring gt, ArrayList<Unigram> emailList) {
        double downspeakProbabilty = 0;
        double upspeakProbabilty = 0;

        for (Unigram u : emailList) {
            upspeakProbabilty += Math.log(Perplexity.getUnigramConditionalProbabilityWithLaplace(u, t.upUnigramModel, t.upUnigramTokens));
            downspeakProbabilty += Math.log(Perplexity.getUnigramConditionalProbabilityWithLaplace(u, t.downUnigramModel, t.downUnigramTokens));
            upspeakProbabilty += Math.log(
                    gt.getSmoothedUnigramProbability(
                            t.upUnigramTokens,
                            gt.upUnigramRegression,
                            t.upUnigramModel.containsKey(u.key) ? t.upUnigramModel.get(u.key).count : 0
                    )
            );
            downspeakProbabilty += Math.log(
                    gt.getSmoothedUnigramProbability(
                            t.downUnigramTokens,
                            gt.downUnigramRegression,
                            t.downUnigramModel.containsKey(u.key) ? t.downUnigramModel.get(u.key).count : 0
                    )
            );
        }

        if (upspeakProbabilty > downspeakProbabilty) return UPSPEAK;
        return DOWNSPEAK;
    }

    private static int classifyWithBigrams(TrainingModel t, GoodTuring gt, ArrayList<Unigram> emailList) {
        double downspeakProbabilty = 0;
        double upspeakProbabilty = 0;

        for (int i = 1; i < emailList.size(); i++) {
            Unigram u1 = emailList.get(i - 1);
            Unigram u2 = emailList.get(i);
            Bigram b = new Bigram(u1, u2);

            upspeakProbabilty += Math.log(
                    gt.getSmoothedBigramProbability(
                            gt.upBigramRegression,
                            t.upBigramModel.containsKey(b.key) ? t.upBigramModel.get(b.key).count : 0,
                            gt.upUnigramRegression,
                            t.upBigramModel.containsKey(u1.key) ? t.upUnigramModel.get(u1.key).count : 0
                    )
            );
            downspeakProbabilty += Math.log(
                    gt.getSmoothedBigramProbability(
                            gt.downBigramRegression,
                            t.downBigramModel.containsKey(b.key) ? t.downBigramModel.get(b.key).count : 0,
                            gt.downUnigramRegression,
                            t.downBigramModel.containsKey(u1.key) ? t.downUnigramModel.get(u1.key).count : 0
                    )
            );
        }

        if (upspeakProbabilty > downspeakProbabilty) return UPSPEAK;
        return DOWNSPEAK;
    }

    private static int classifyWithTrigrams(TrainingModel t, GoodTuring gt, ArrayList<Unigram> emailList) {
        double downspeakProbabilty = 0;
        double upspeakProbabilty = 0;

        for (int i = 2; i < emailList.size(); i++) {
            Unigram u1 = emailList.get(i - 2);
            Unigram u2 = emailList.get(i - 1);
            Unigram u3 = emailList.get(i);
            Trigram r = new Trigram(u1, u2, u3);
            Bigram b = new Bigram(u1, u2);

            upspeakProbabilty += Math.log(
                    gt.getSmoothedTrigramProbability(
                            gt.upTrigramRegression,
                            t.upTrigramModel.containsKey(b.key) ? t.upTrigramModel.get(b.key).count : 0,
                            gt.upBigramRegression,
                            t.upBigramModel.containsKey(u1.key) ? t.upBigramModel.get(u1.key).count : 0
                    )
            );
            downspeakProbabilty += Math.log(
                    gt.getSmoothedTrigramProbability(
                            gt.downTrigramRegression,
                            t.downTrigramModel.containsKey(b.key) ? t.downTrigramModel.get(b.key).count : 0,
                            gt.downBigramRegression,
                            t.downBigramModel.containsKey(u1.key) ? t.downBigramModel.get(u1.key).count : 0
                    )
            );
        }

        if (upspeakProbabilty > downspeakProbabilty) return UPSPEAK;
        return DOWNSPEAK;
    }

}
