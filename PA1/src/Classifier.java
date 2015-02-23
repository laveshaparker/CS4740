import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Classifier {

    public static final String UNIGRAM = "unigram";
    public static final String BIGRAM = "bigram";
    public static final String TRIGRAM = "trigram";

    public static final String DOWN = "down";
    public static final String UP = "up";

    public static TreeMap<String, SortedMap<String, Unigram>> unigram_models = new TreeMap<>();
    public static TreeMap<String, SortedMap<String, Bigram>>  bigram_models  = new TreeMap<>();
    public static TreeMap<String, SortedMap<String, Trigram>> trigram_models = new TreeMap<>();

    public static int unigram_tokens_down;
    public static int bigram_tokens_down;
    public static int trigram_tokens_down;
    public static int unigram_tokens_up;
    public static int bigram_tokens_up;
    public static int trigram_tokens_up;

    public static void main(String[] args) throws Exception {
        new Classifier();
    }

    Classifier() throws FileNotFoundException, UnsupportedEncodingException {
        //used for access to unsmoothed models
        TrainingModel t = new TrainingModel(TrainingModel.DIRECTORY);
        unigram_models.put(DOWN, t.downUnigramModel);
        unigram_models.put(UP, t.upUnigramModel);
        bigram_models.put(DOWN, t.downBigramModel);
        bigram_models.put(UP, t.upBigramModel);
        trigram_models.put(DOWN, t.downTrigramModel);
        trigram_models.put(UP, t.upTrigramModel);

        unigram_tokens_down = t.downUnigramTokens;
        bigram_tokens_down = t.downBigramTokens;
        trigram_tokens_down = t.downTrigramTokens;
        unigram_tokens_up = t.upUnigramTokens;
        bigram_tokens_up = t.upBigramTokens;
        trigram_tokens_up = t.upTrigramTokens;

        System.out.println("Starting classify");
        classify(DataProcessor.test_set);
        System.out.println("Hurray this worked");

    }

    /**
     * This method reads in the final file and parses it similar to data_set
     * in DataProcessor. The results are stored and analyzed using the various
     * methods in this class, and the output classification is written to a file.
     *
     * @todo processing of incoming emails into N-gram models
     * @todo compute/compare logs of probabilities instead of actual
     * @todo more testing
     */
    private void classify(TreeMap<String, ArrayList<ArrayList<Unigram>>> parsed_file) throws FileNotFoundException, UnsupportedEncodingException {
        //create output file
        PrintWriter writer = new PrintWriter("out/test_classified.txt", "UTF-8");
        writer.println("Id,Prediction");

        //process emails into ArrayList<ArrayList<N-Gram>>
        for (String k : parsed_file.keySet()) {
            //System.out.println("this is: " + k);
            Unigram unigram;
            Bigram bigram;
            Trigram trigram;
            String key;

            ArrayList<ArrayList<Unigram>> data_to_classify_uni = new ArrayList<ArrayList<Unigram>>();
            ArrayList<ArrayList<Bigram>>  data_to_classify_bi  = new ArrayList<ArrayList<Bigram>>();
            ArrayList<ArrayList<Trigram>> data_to_classify_tri = new ArrayList<ArrayList<Trigram>>();

            for (ArrayList<Unigram> sentence : parsed_file.get(k)) {
                ArrayList<Unigram> next_sentence_unigram = new ArrayList<Unigram>();
                ArrayList<Bigram>  next_sentence_bigram  = new ArrayList<Bigram>();
                ArrayList<Trigram> next_sentence_trigram = new ArrayList<Trigram>();

                for (int idx = 0; idx < sentence.size(); idx++) {
                    unigram = sentence.get(idx);
                    unigram.count = 1;
                    next_sentence_unigram.add(unigram);

                    if (idx + 1 < sentence.size()) {
                        bigram = new Bigram(sentence.get(idx), sentence.get(idx + 1));
                        bigram.count = 1;
                        next_sentence_bigram.add(bigram);
                    }

                    if (idx + 2 < sentence.size()) {
                        trigram = new Trigram(sentence.get(idx), sentence.get(idx + 1), sentence.get(idx + 2));
                        trigram.count = 1;
                        next_sentence_trigram.add(trigram);
                    }
                }

                data_to_classify_uni.add(next_sentence_unigram);
                data_to_classify_bi.add(next_sentence_bigram);
                data_to_classify_tri.add(next_sentence_trigram);
            }

            //now there are uni/bi/trigram models of the email
            //although these models are not complete, each
            //has count of 1, and just appears n times

            //pre-calculate the good-turing data of the training set
            ArrayList<HashMap<Integer, Integer>> gt_down = getGoodTuringData(unigram_models.get(DOWN), bigram_models.get(DOWN), trigram_models.get(DOWN));
            ArrayList<HashMap<Integer, Integer>> gt_up   = getGoodTuringData(unigram_models.get(UP), bigram_models.get(UP), trigram_models.get(UP));

            //add-one smoothing probabilities
            double addu_email_down = 1;
            double addu_email_up   = 1;
            //good-turing probabilities
            double gtu_email_down = 1;
            double gtu_email_up   = 1;

            for (ArrayList<Unigram> email : data_to_classify_uni){

                for (Unigram word : email) {
                    //calculate add-one
                    addu_email_down *= addOne(word.key, "", "").get(0);
                    addu_email_up   *= addOne(word.key, "", "").get(1);

                    //calculate good-turing
                    double count_down  = (unigram_models.get(DOWN).containsKey(word.key) ? (unigram_models.get(DOWN).get(word.key).count + 1) : 1);
                    double gt_down_n   = (gt_down.get(0).containsKey(count_down - 1) ? (gt_down.get(0).get(count_down - 1)) : 1);
                    double gt_down_n1  = (gt_down.get(0).containsKey(count_down) ? (gt_down.get(0).get(count_down)) : 1);
                    gtu_email_down    *= Math.pow(count_down * gt_down_n1 / gt_down_n / unigram_tokens_down, word.count);

                    double count_up    = (unigram_models.get(UP).containsKey(word.key) ? (unigram_models.get(UP).get(word.key).count + 1) : 1);
                    double gt_up_n     = (gt_up.get(0).containsKey(count_up - 1) ? (gt_up.get(0).get(count_up - 1)) : 1);
                    double gt_up_n1    = (gt_up.get(0).containsKey(count_up) ? (gt_up.get(0).get(count_up)) : 1);
                    gtu_email_up      *= Math.pow(count_up * gt_up_n1 / gt_up_n / unigram_tokens_up, word.count);
                }

            }

            //add-one smoothing probabilities
            double addb_email_down = 1;
            double addb_email_up   = 1;
            //good-turing probabilities
            double gtb_email_down = 1;
            double gtb_email_up   = 1;

            for (ArrayList<Bigram> email : data_to_classify_bi) {

                for (Bigram word : email) {
                    //calculate add-one
                    addb_email_down *= addOne(word.token1.token, word.token2.token, "").get(2);
                    addb_email_up   *= addOne(word.token1.token, word.token2.token, "").get(3);

                    //calculate good-turing
                    double count_down  = (bigram_models.get(DOWN).containsKey(word.key) ? (bigram_models.get(DOWN).get(word.key).count + 1) : 1);
                    double gt_down_n   = (gt_down.get(1).containsKey(count_down - 1) ? (gt_down.get(1).get(count_down - 1)) : 1);
                    double gt_down_n1  = (gt_down.get(1).containsKey(count_down) ? (gt_down.get(1).get(count_down)) : 1);
                    gtb_email_down    *= Math.pow(count_down * gt_down_n1 / gt_down_n / bigram_tokens_down, word.count);

                    double count_up    = (bigram_models.get(UP).containsKey(word.key) ? (bigram_models.get(UP).get(word.key).count + 1) : 1);
                    double gt_up_n     = (gt_up.get(1).containsKey(count_up - 1) ? (gt_up.get(1).get(count_up - 1)) : 1);
                    double gt_up_n1    = (gt_up.get(1).containsKey(count_up) ? (gt_up.get(1).get(count_up)) : 1);
                    gtb_email_up      *= Math.pow(count_up * gt_up_n1 / gt_up_n / bigram_tokens_up, word.count);
                }

            }

            //add-one smoothing probabilities
            double addt_email_down = 1;
            double addt_email_up   = 1;
            //good-turing probabilities
            double gtt_email_down = 1;
            double gtt_email_up   = 1;

            for (ArrayList<Trigram> email : data_to_classify_tri) {

                for (Trigram word : email) {
                    //calculate add-one
                    addt_email_down *= addOne(word.token1.token, word.token2.token, word.token3.token).get(4);
                    addt_email_up   *= addOne(word.token1.token, word.token2.token, word.token3.token).get(5);

                    //calculate good-turing
                    double count_down  = (trigram_models.get(DOWN).containsKey(word.key) ? (trigram_models.get(DOWN).get(word.key).count + 1) : 1);
                    double gt_down_n   = (gt_down.get(2).containsKey(count_down - 1) ? (gt_down.get(2).get(count_down - 1)) : 1);
                    double gt_down_n1  = (gt_down.get(2).containsKey(count_down) ? (gt_down.get(2).get(count_down)) : 1);
                    gtt_email_down    *= Math.pow(count_down * gt_down_n1 / gt_down_n / trigram_tokens_down, word.count);

                    double count_up    = (trigram_models.get(UP).containsKey(word.key) ? (trigram_models.get(UP).get(word.key).count + 1) : 1);
                    double gt_up_n     = (gt_up.get(2).containsKey(count_up - 1) ? (gt_up.get(2).get(count_up - 1)) : 1);
                    double gt_up_n1    = (gt_up.get(2).containsKey(count_up) ? (gt_up.get(2).get(count_up)) : 1);
                    gtt_email_up      *= Math.pow(count_up * gt_up_n1 / gt_up_n / trigram_tokens_up, word.count);
                }

            }

            //determine up or down
            int score = 0;
            System.out.println(gtb_email_up + " vs " + gtb_email_down);
            if (gtb_email_up > gtb_email_down) score += 1;
            if (gtt_email_up > gtt_email_down) score += 1;
            if (addt_email_up > addt_email_down) score += 1;
            if (score > 1) writer.println(k + ",1");
            else writer.println(k + ",0");

        }
        writer.close();

    }

    /**
     * Returns hashmaps where the keys are the occurrences of an n-gram,
     * and the corresponding value is the number of times any n-gram
     * occurred that many times throughout the corpus
     */
    public static ArrayList<HashMap<Integer, Integer>> getGoodTuringData(SortedMap<String, Unigram>  up_down_u, SortedMap<String, Bigram>  up_down_b, SortedMap<String, Trigram>  up_down_t) {
        ArrayList<HashMap<Integer, Integer>> goodTuringCompilation = new ArrayList<HashMap<Integer, Integer>>();

        //Analyze the unigram data
        HashMap<Integer, Integer> goodTuringUniValues = new HashMap<Integer, Integer>();
        for (String k : up_down_u.keySet()) {
            int n = up_down_u.get(k).count;
            int n_of_occurrences = (goodTuringUniValues.containsKey(n) ? (goodTuringUniValues.get(n) + 1) : 1);
            goodTuringUniValues.put(n, n_of_occurrences);
        }
        goodTuringCompilation.add(goodTuringUniValues);

        //Analyze the bigram data
        HashMap<Integer, Integer> goodTuringBiValues = new HashMap<Integer, Integer>();
        for (String k : up_down_b.keySet()) {
            int n = up_down_b.get(k).count;
            int n_of_occurrences = (goodTuringBiValues.containsKey(n) ? (goodTuringBiValues.get(n) + 1) : 1);
            goodTuringBiValues.put(n, n_of_occurrences);
        }
        goodTuringCompilation.add(goodTuringBiValues);

        //Analyze the trigram data
        HashMap<Integer, Integer> goodTuringTriValues = new HashMap<Integer, Integer>();
        for (String k : up_down_t.keySet()) {
            int n = up_down_t.get(k).count;
            int n_of_occurrences = (goodTuringTriValues.containsKey(n) ? (goodTuringTriValues.get(n) + 1) : 1);
            goodTuringTriValues.put(n, n_of_occurrences);
        }
        goodTuringCompilation.add(goodTuringTriValues);

        return goodTuringCompilation;
    }

    /**
     * Returns the occurrences in an array list of the form:
     * [down unigram, up unigram, down bigram, up bigram, down trigram, up trigram]
     *
     * Add one smoothing is implemented for all three N-gram models, unlike
     * other smoothing methods.
     */
    private ArrayList<Double> addOne(String word1, String word2, String word3){
        ArrayList<Double> return_vals = new ArrayList<Double>();

        //adds unigram probabilities
        double downu_count = (unigram_models.get(DOWN).containsKey(word1) ? (unigram_models.get(DOWN).get(word1).count + 1) : 1);
        double downu_chance = downu_count / (unigram_models.get(DOWN).size() + unigram_tokens_down);
        return_vals.add(downu_chance);
        double upu_count = (unigram_models.get(UP).containsKey(word1) ? (unigram_models.get(UP).get(word1).count + 1) : 1);
        double upu_chance = upu_count / (unigram_models.get(UP).size() + unigram_tokens_up);
        return_vals.add(upu_chance);

        //adds bigram probabilities
        double downb_count = (bigram_models.get(DOWN).containsKey(word1 + " " + word2) ? (bigram_models.get(DOWN).get(word1 + " " + word2).count + 1) : 1);
        double downb_chance = downb_count / (downu_count + unigram_tokens_down);
        return_vals.add(downb_chance);
        double upb_count = (bigram_models.get(UP).containsKey(word1 + " " + word2) ? (bigram_models.get(UP).get(word1 + " " + word2).count + 1) : 1);
        double upb_chance = upb_count / (upu_count + unigram_tokens_up);
        return_vals.add(upb_chance);

        //adds trigram probabilities
        double downt_count = (trigram_models.get(DOWN).containsKey(word1 + " " + word2 + " " + word3) ? (trigram_models.get(DOWN).get(word1 + " " + word2 + " " + word3).count + 1) : 1);
        double downt_chance = downt_count / (downb_count + unigram_tokens_down);
        return_vals.add(downt_chance);
        double upt_count = (trigram_models.get(UP).containsKey(word1 + " " + word2 + " " + word3) ? (trigram_models.get(UP).get(word1 + " " + word2 + " " + word3).count + 1) : 1);
        double upt_chance = upt_count / (upb_count + unigram_tokens_up);
        return_vals.add(upt_chance);

        return return_vals;
    }
}


