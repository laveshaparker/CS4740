import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * Created by Sofonias on 2/23/2015.
 */
public class GoodTuring {

    public static void main(String[] args) throws IOException {
        TrainingModel t = new TrainingModel(TrainingModel.DIRECTORY);
        GoodTuring gt = new GoodTuring(t);

        System.out.println(gt.getFrequency(gt.downBigramRegression, 0));
        System.out.println(gt.getFrequency(gt.upUnigramRegression, 1));
        System.out.println(gt.getFrequency(gt.upUnigramRegression, 2));
        System.out.println(gt.getFrequency(gt.upUnigramRegression, 3));
        System.out.println(gt.getFrequency(gt.upUnigramRegression, 4));
        System.out.println(gt.getFrequency(gt.upUnigramRegression, 5));
        System.out.println(gt.getFrequency(gt.upUnigramRegression, 10000));

    }

    public SimpleRegression upUnigramRegression;
    public SimpleRegression downUnigramRegression;
    public SimpleRegression upBigramRegression;
    public SimpleRegression downBigramRegression;
    public SimpleRegression upTrigramRegression;
    public SimpleRegression downTrigramRegression;

    public GoodTuring(TrainingModel t) {
        HashMap<Integer, Integer> upUnigramFrequency = new HashMap<Integer, Integer>();
        upUnigramRegression = new SimpleRegression();

        HashMap<Integer, Integer> downUnigramFrequency = new HashMap<Integer, Integer>();
        downUnigramRegression = new SimpleRegression();

        HashMap<Integer, Integer> upBigramFrequency = new HashMap<Integer, Integer>();
        upBigramRegression = new SimpleRegression();

        HashMap<Integer, Integer> downBigramFrequency = new HashMap<Integer, Integer>();
        downBigramRegression = new SimpleRegression();

        HashMap<Integer, Integer> upTrigramFrequency = new HashMap<Integer, Integer>();
        upTrigramRegression = new SimpleRegression();

        HashMap<Integer, Integer> downTrigramFrequency = new HashMap<Integer, Integer>();
        downTrigramRegression = new SimpleRegression();

        // Linear regression for upspeak unigrams
        for (String key : t.upUnigramModel.keySet()) {
            int count = t.upUnigramModel.get(key).count;
            upUnigramFrequency.put(
                    count,
                    upUnigramFrequency.containsKey(count) ? upUnigramFrequency.get(count) + 1: 1
            );
        }
        for (Integer key : upUnigramFrequency.keySet()) {
            upUnigramRegression.addData(Math.log(key), Math.log(upUnigramFrequency.get(key)));
        }

        // Linear regression for downspeak unigrams
        for (String key : t.downUnigramModel.keySet()) {
            int count = t.downUnigramModel.get(key).count;
            downUnigramFrequency.put(
                    count,
                    downUnigramFrequency.containsKey(count) ? downUnigramFrequency.get(count) + 1: 1
            );
        }
        for (Integer key : downUnigramFrequency.keySet()) {
            downUnigramRegression.addData(Math.log(key), Math.log(downUnigramFrequency.get(key)));
        }

        // Lienar regression for upspeak bigrams
        for (String key : t.upBigramModel.keySet()) {
            int count = t.upBigramModel.get(key).count;
            upBigramFrequency.put(
                    count,
                    upBigramFrequency.containsKey(count) ? upBigramFrequency.get(count) + 1: 1
            );
        }
        for (Integer key : upBigramFrequency.keySet()) {
            upBigramRegression.addData(Math.log(key), Math.log(upBigramFrequency.get(key)));
        }

        // Linear regression for downspeak bigrams
        for (String key : t.downBigramModel.keySet()) {
            int count = t.downBigramModel.get(key).count;
            downBigramFrequency.put(
                    count,
                    downBigramFrequency.containsKey(count) ? downBigramFrequency.get(count) + 1: 1
            );
        }
        for (Integer key : downBigramFrequency.keySet()) {
            downBigramRegression.addData(Math.log(key), Math.log(downBigramFrequency.get(key)));
        }

        // Linear regression for upspeak trigrams
        for (String key : t.upTrigramModel.keySet()) {
            int count = t.upTrigramModel.get(key).count;
            upTrigramFrequency.put(
                    count,
                    upTrigramFrequency.containsKey(count) ? upTrigramFrequency.get(count) + 1: 1
            );
        }
        for (Integer key : upTrigramFrequency.keySet()) {
            upTrigramRegression.addData(Math.log(key), Math.log(upTrigramFrequency.get(key)));
        }

        // Linear regression for downspeak trigrams
        for (String key : t.downTrigramModel.keySet()) {
            int count = t.downTrigramModel.get(key).count;
            downTrigramFrequency.put(
                    count,
                    downTrigramFrequency.containsKey(count) ? downTrigramFrequency.get(count) + 1: 1
            );
        }
        for (Integer key : downTrigramFrequency.keySet()) {
            downTrigramRegression.addData(Math.log(key), Math.log(downTrigramFrequency.get(key)));
        }
    }

    public double getFrequency(SimpleRegression r, double count) {
        if (count == 0) return 1;
        return Math.exp(r.predict(Math.log(count)));
    }

    public double getSmoothedCount(SimpleRegression r, double count) {
        return (count + 1) * getFrequency(r, count + 1) / getFrequency(r, count);
    }



}
