/**
 * Created with IntelliJ IDEA.
 * User: vesha
 * Date: 2/10/15
 * Time: 5:27 PM
 */
public class Bigram {
    public String key; // The unique key for accessing this NGram in maps
    public int count = 1;

    public Unigram token1;
    public Unigram token2;

    Bigram (Unigram word1, Unigram word2) {
        token1 = word1;
        token2 = word2;
        key = token1.lemma + " " + token2.lemma;
    }

    @Override public String toString() {
        return token1.lemma + " " + token2.lemma + " " + count;
    }
}
