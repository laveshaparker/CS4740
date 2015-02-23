import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.TreeMap;

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
        key = token1.lemma + " " + token2.lemma + " " + token3.lemma;
    }

    Trigram (JsonObject obj, TreeMap<String, Unigram> unigrams) {
        key = obj.getString("key");
        count = obj.getInt("count");
        token1 = unigrams.get(obj.getString("token1"));
        token2 = unigrams.get(obj.getString("token2"));
        token3 = unigrams.get(obj.getString("token3"));
    }

    @Override public String toString() {
        return token1.lemma + " " + token2.lemma + " "  + token3.lemma + " " + count;
    }

    public JsonObjectBuilder asJson() {
        return Json.createObjectBuilder()
                .add("key", key)
                .add("count", count)
                .add("token1", token1.key)
                .add("token2", token2.key)
                .add("token3", token3.key);
    }
}
