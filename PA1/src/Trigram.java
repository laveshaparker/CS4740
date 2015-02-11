/**
 * Created with IntelliJ IDEA.
 * User: vesha
 * Date: 2/10/15
 * Time: 5:31 PM
 */
public class Trigram {
    public String key; // The unique key for accessing this NGram in maps
    public int count = 1;

    public Unigram token1;
    public Unigram token2;
    public Unigram token3;

    Trigram (Unigram word1, Unigram word2, Unigram word3) {
        token1 = word1;
        token2 = word2;
        token3 = word3;
        key = token1.token + " " + token2.token + " " + token3.token;
    }

    @Override public String toString() {
        return token1.token + " " + token2.token + " "  + token3.token + " " + count;
    }
}
