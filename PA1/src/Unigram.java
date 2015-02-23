import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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
    public String lemma;

    Unigram (String word, String part_of_speech, String named_entity_recognition, String lem) {
        pos = part_of_speech;
        token = word;
        ner = named_entity_recognition;
        count = 1;
        lemma = lem;
        key = lemma;
    }

    Unigram (JsonObject obj) {
        key = obj.getString("key");
        count = obj.getInt("count");
        pos = obj.getString("pos");
        token = obj.getString("token");
        ner = obj.getString("ner");
        lemma = obj.getString("lemma");
    }

    @Override public String toString() {
        return lemma + " " + count;
    }

    public JsonObjectBuilder asJson() {
        return Json.createObjectBuilder()
                .add("key", key)
                .add("count", count)
                .add("pos", pos)
                .add("token", token)
                .add("ner", ner)
                .add("lemma", lemma);
    }
}
