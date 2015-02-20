import java.io.File;
import java.io.FileNotFoundException;
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

    Classifier() throws FileNotFoundException {
        //used for access to unsmoothed models
        new UnsmoothedNGram();
        unigram_models = UnsmoothedNGram.unigram_models;
        bigram_models = UnsmoothedNGram.bigram_models;
        trigram_models = UnsmoothedNGram.trigram_models;

        //get total token counts for each model
        for (String k : unigram_models.get(DOWN).keySet()) {
            unigram_tokens_down += unigram_models.get(DOWN).get(k).count;
        }
        for (String k : bigram_models.get(DOWN).keySet()) {
            bigram_tokens_down += bigram_models.get(DOWN).get(k).count;
        }
        for (String k : trigram_models.get(DOWN).keySet()) {
            trigram_tokens_down += trigram_models.get(DOWN).get(k).count;
        }
        for (String k : unigram_models.get(UP).keySet()) {
            unigram_tokens_down += unigram_models.get(UP).get(k).count;
        }
        for (String k : bigram_models.get(UP).keySet()) {
            bigram_tokens_down += bigram_models.get(UP).get(k).count;
        }
        for (String k : trigram_models.get(UP).keySet()) {
            trigram_tokens_down += trigram_models.get(UP).get(k).count;
        }

        classify("training.txt");
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
    private void classify(String source_file) throws FileNotFoundException {
        //process emails into ArrayList<ArrayList<N-Gram>>
        ArrayList<ArrayList<Unigram>> data_to_classify_uni = new ArrayList<ArrayList<Unigram>>();
        ArrayList<ArrayList<Bigram>>  data_to_classify_bi  = new ArrayList<ArrayList<Bigram>>();
        ArrayList<ArrayList<Trigram>> data_to_classify_tri = new ArrayList<ArrayList<Trigram>>();

        //pre-calculate the good-turing data of the training set
        ArrayList<HashMap<Integer, Integer>> gt_down = getGoodTuringData(unigram_models.get(DOWN), bigram_models.get(DOWN), trigram_models.get(DOWN));
        ArrayList<HashMap<Integer, Integer>> gt_up   = getGoodTuringData(unigram_models.get(UP), bigram_models.get(UP), trigram_models.get(UP));

        for (ArrayList<Unigram> email : data_to_classify_uni){
            //add-one smoothing probabilities
            double addu_email_down = 1;
            double addu_email_up   = 1;
            //good-turing probabilities
            double gtu_email_down = 1;
            double gtu_email_up   = 1;

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

        for (ArrayList<Bigram> email : data_to_classify_bi) {
            //add-one smoothing probabilities
            double addb_email_down = 1;
            double addb_email_up   = 1;
            //good-turing probabilities
            double gtb_email_down = 1;
            double gtb_email_up   = 1;

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

        for (ArrayList<Trigram> email : data_to_classify_tri) {
            //add-one smoothing probabilities
            double addt_email_down = 1;
            double addt_email_up   = 1;
            //good-turing probabilities
            double gtt_email_down = 1;
            double gtt_email_up   = 1;

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

        //determine up or down by comparing the
        //various probabilities across N-grams and smoothing methods

        //write to file in format specified in 2.6

    }

    /**
     * Returns hashmaps where the keys are the occurrences of an n-gram,
     * and the corresponding value is the number of times any n-gram
     * occurred that many times throughout the corpus
     */
    private ArrayList<HashMap<Integer, Integer>> getGoodTuringData(SortedMap<String, Unigram>  up_down_u, SortedMap<String, Bigram>  up_down_b, SortedMap<String, Trigram>  up_down_t) {
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
        double downu_chance = downu_count / (unigram_models.get(DOWN).size() + unigram_tokens_down + 1);
        return_vals.add(downu_chance);
        double upu_count = (unigram_models.get(UP).containsKey(word1) ? (unigram_models.get(UP).get(word1).count + 1) : 1);
        double upu_chance = upu_count / (unigram_models.get(UP).size() + unigram_tokens_up + 1);
        return_vals.add(upu_chance);

        //adds bigram probabilities
        double downb_count = (bigram_models.get(DOWN).containsKey(word1 + " " + word2) ? (bigram_models.get(DOWN).get(word1 + " " + word2).count + 1) : 1);
        double downb_chance = downb_count / (bigram_models.get(DOWN).size() + bigram_tokens_down + 1);
        return_vals.add(downb_chance);
        double upb_count = (bigram_models.get(UP).containsKey(word1 + " " + word2) ? (bigram_models.get(UP).get(word1 + " " + word2).count + 1) : 1);
        double upb_chance = upb_count / (bigram_models.get(UP).size() + bigram_tokens_up + 1);
        return_vals.add(upb_chance);

        //adds trigram probabilities
        double downt_count = (trigram_models.get(DOWN).containsKey(word1 + " " + word2 + " " + word3) ? (trigram_models.get(DOWN).get(word1 + " " + word2 + " " + word3).count + 1) : 1);
        double downt_chance = downt_count / (trigram_models.get(DOWN).size() + trigram_tokens_down + 1);
        return_vals.add(downt_chance);
        double upt_count = (trigram_models.get(UP).containsKey(word1 + " " + word2 + " " + word3) ? (trigram_models.get(UP).get(word1 + " " + word2 + " " + word3).count + 1) : 1);
        double upt_chance = upt_count / (trigram_models.get(UP).size() + trigram_tokens_up + 1);
        return_vals.add(upt_chance);

        return return_vals;
    }



}

