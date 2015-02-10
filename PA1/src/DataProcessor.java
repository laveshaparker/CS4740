import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author vesha
 *
 */
public class DataProcessor {

    public static String model = "lib/stanford-postagger-2015-01-30/models/english-left3words-distsim.tagger";
    public static String test = "data/smalltest.txt";
    public static String train = "data/training.txt";
    public static String validate = "data/validation.txt";

    public static TreeMap<String, TreeSet<String>> data_set = new TreeMap<String, TreeSet<String>>();

    private static StanfordCoreNLP pipeline;

    DataProcessor() throws FileNotFoundException {
        // creates a StanfordCoreNLP object, with POS tagging, tokenization, and parsing
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        pipeline = new StanfordCoreNLP(props);

        data_set.put("up_train", new TreeSet<String>());
        data_set.put("down_train", new TreeSet<String>());
        data_set.put("up_validation", new TreeSet<String>());
        data_set.put("down_validation", new TreeSet<String>());
        
        process(train, "train");
        process(validate, "validation");
    }

    /**
     * @param args, do not provide any arguments.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
        DataProcessor dp = new DataProcessor();
    }


    /**
     * Calls appropriate methods to extract email data, tokenize
     * sentences, and output tokenized sentences.
     * @param source_file, the file from which to read emails.
     * @param data_type, acceptable input: "validation", "train"
     * @todo Add support for data_type = "test"
     * @throws FileNotFoundException
     */
    private void process(String source_file, String data_type) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(source_file)).useDelimiter("\\*\\*.*?\\*\\*");

        String speak_type; // "down" or "up"
        String email_content;

        while (sc.hasNext()) {

            speak_type = sc.next().equalsIgnoreCase("DOWNSPEAK") ? "down" : "up";
            email_content = sc.next();

            processEmailContent(speak_type + "_" + data_type, email_content);
        }

    }

    /**
     * @todo Add newlines to sentence end delimiters so that the salutation and greetings are not considered part of the email.
     * @param key, the key used to index into the data_set map. Corresponds to the set that needs to be updated
     * @param email_content, The content of a given email
     */
    private void processEmailContent(String key, String email_content) {

        Annotation document = new Annotation(email_content);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // Add sentence to appropriate data set, adding start and end tags.
            data_set.get(key).add("<s> " + sentence.toString() + " </s>");

            // Uncomment if you want to see how the sentences are stored
            // System.out.println("<s> " + sentence.toString() + " </s>");
        }
    }
}
