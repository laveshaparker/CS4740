import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

    public static final String DOWN = "down";
    public static final String UP = "up";

    public static void main(String[] args) throws Exception {
        TrainingModel t = new TrainingModel(TrainingModel.DIRECTORY);
        System.out.println(findConditionalProbability(t, "<s>", UNIGRAM, UP));
        System.out.println(findConditionalProbability(t, "<s>", UNIGRAM, DOWN));
        System.out.println(findConditionalProbability(t, "<s> I", BIGRAM, DOWN));
        System.out.println(findConditionalProbability(t, "<s> I", BIGRAM, UP));
    }

    /**
     * Computes the conditional probability of an N-gram as (# N-gram/#(N-1)-gram)
     * @param NGram, the phrase in question. Delimited by spaces (punctuation counts!). Must be lemmatized.
     * @param N, UnsmoothedNGram.UNIGRAM, UnsmoothedNGram.BIGRAM, or UnsmoothedNGram.TRIGRAM
     * @param up_down, UnsmoothedNGram.DOWN or UnsmoothedNGram.UP
     * @return conditional probability of NGram
     */
    public static double findConditionalProbability(TrainingModel trainingModel, String NGram, String N, String up_down) throws Exception {
        int count = 0;
        int nMinus1Count;

        Bigram bigram;
        Trigram trigram;

        switch (N) {
            case UNIGRAM:
                switch (up_down) {
                    case DOWN:
                        count  = trainingModel.downUnigramModel.get(NGram).count;
                        nMinus1Count = trainingModel.downUnigramTokens; // total # of tokens in the corpus
                        break;
                    case UP:
                        count = trainingModel.upUnigramModel.get(NGram).count;
                        nMinus1Count = trainingModel.upUnigramTokens; // total # of tokens in the corpus
                        break;
                    default:
                        throw new Exception("Invalid value for up_down");
                }
                break;
            case BIGRAM:
                switch (up_down) {
                    case DOWN:
                        bigram = trainingModel.downBigramModel.get(NGram);
                        count  = bigram.count;
                        nMinus1Count = trainingModel.downUnigramModel.get(bigram.token1.key).count;
                        break;
                    case UP:
                        bigram = trainingModel.upBigramModel.get(NGram);
                        count  = bigram.count;
                        nMinus1Count = trainingModel.upUnigramModel.get(bigram.token1.key).count;
                        break;
                    default:
                        throw new Exception("Invalid value for up_down");
                }
                break;
            case TRIGRAM:
                switch (up_down) {
                    case DOWN:
                        trigram = trainingModel.downTrigramModel.get(NGram);
                        count  = trigram.count;
                        nMinus1Count = trainingModel.downBigramModel.get(new Bigram(trigram.token1, trigram.token2).key).count;
                        break;
                    case UP:
                        trigram = trainingModel.upTrigramModel.get(NGram);
                        count  = trigram.count;
                        nMinus1Count = trainingModel.upBigramModel.get(new Bigram(trigram.token1, trigram.token2).key).count;
                        break;
                    default:
                        throw new Exception("Invalid value for up_down");
                }
                break;
            default:
                throw new Exception("Invalid value for N.");
        }
        return (double) count / nMinus1Count;
    }
}
