/**
 * Created with IntelliJ IDEA.
 * User: vesha
 * Date: 2/10/15
 * Time: 5:24 PM
 */
public class Unigram {

    public String key; // The unique key for accessing this NGram
    public int count;
    public String pos;
    public String token;
    public String ner; // Named Entity Recognition tag.

    Unigram (String word, String part_of_speech, String named_entity_recognition) {
        pos = part_of_speech;
        token = word;
        ner = named_entity_recognition;
        count = 1;
        key = token;
    }

    @Override public String toString() {
        return token + " " + count;
    }
}
