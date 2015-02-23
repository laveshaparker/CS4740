import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Created with IntelliJ IDEA.
 * User: vesha
 * @todo Try to interpret numbers as type, order of magnitude, percentage, numerical value
 * @todo Add all numbers as a number tag
 * @todo Ignore all hyphens in emails.
 */
public class DataProcessor {

    public static final Unigram start_tag = new Unigram("<s>", "<s>", "", "<s>");
    public static final Unigram end_tag = new Unigram("</s>", "</s>", "", "</s>");
    public static String model = "lib/stanford-postagger-2015-01-30/models/english-left3words-distsim.tagger";
    public static String test = "data/test.txt";
    public static String train = "data/training.txt";
    public static String validate = "data/validation.txt";


    public static TreeMap<String, ArrayList<ArrayList<Unigram>>> data_set = new TreeMap<>();

    public static TreeMap<String, ArrayList<ArrayList<Unigram>>> test_set = new TreeMap<>();

    private static StanfordCoreNLP pipeline;

    DataProcessor() throws FileNotFoundException {
        // creates a StanfordCoreNLP object, with POS tagging, tokenization, named entity recognition and parsing
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        // Splits along newlines
        props.put("ssplit.newlineIsSentenceBreak", "always");

        pipeline = new StanfordCoreNLP(props);

        data_set.put("up_train", new ArrayList<>());
        data_set.put("down_train", new ArrayList<>());
        data_set.put("up_validation", new ArrayList<>());
        data_set.put("down_validation", new ArrayList<>());

        // What I'm using to test my code as I go. Comment this out and uncomment the next two lines to run this properly.
//        process("data/smalltest.txt", "train", true);

        process(train, "train", false);
        process(validate, "validation", false);
        process(test, "test", true);
    }

    /**
     * @param args, do not provide any arguments.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
        new DataProcessor();
    }

    public String makeKey(String speak_type, String data_type) {
        return speak_type + "_" + data_type;
    }


    /**
     * Calls appropriate methods to extract email data, tokenize
     * sentences, and output tokenized sentences.
     * @param source_file, the file from which to read emails.
     * @param data_type, acceptable input: "validation", "train"
     * @param is_test_file, true if run with test file, false otherwise.
     * @throws FileNotFoundException
     */
    private void process(String source_file, String data_type, Boolean is_test_file) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(source_file)).useDelimiter("\\*\\*.*?\\*\\*");

        String speak_type, email_content;

        while (sc.hasNext()) {
            String str = sc.next().replaceAll("\\s+", "");

            if (sc.hasNext()) {
                email_content = sc.next();

                if (is_test_file) {
                    processEmailContent(str, email_content, is_test_file);
                } else {
                    speak_type = str.equalsIgnoreCase("DOWNSPEAK") ? UnsmoothedNGram.DOWN : UnsmoothedNGram.UP;
                    processEmailContent(makeKey(speak_type, data_type), email_content, is_test_file);
                }
            }
        }
    }

    /**
     * @todo Process all proper nouns as <PRN>
     * @todo Save part of speech for each <UNK>
     * @param key, the key used to index into the data_set map. Corresponds to the set that needs to be updated
     * @param email_content, The content of a given email
     * @param is_test_file, true if run with test file, false otherwise.
     */
    private void processEmailContent(String key, String email_content, Boolean is_test_file) {

        Annotation document = new Annotation(email_content);
        pipeline.annotate(document);

        if (is_test_file) {test_set.put(key, new ArrayList<>());}

        ArrayList<ArrayList<Unigram>> to_add_to = is_test_file ? test_set.get(key) : data_set.get(key);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        String word, pos, ne;
        int idx;

        for(CoreMap sentence: sentences) {
            // Add sentence to appropriate data set, including start and end tags.
            to_add_to.add(new ArrayList<>());
            idx = to_add_to.size() - 1;
            to_add_to.get(idx).add(start_tag);

            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                word = token.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
                // this is the POS tag of the token
                pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class); //ne.equals("0") ||

                to_add_to.get(idx).add(new Unigram(word, pos, ne, lemma));
            }
            to_add_to.get(idx).add(end_tag);

            // Uncomment if you want to see how the sentence is constructed.
            // System.out.println(to_add_to.get(idx).toString());
        }
    }
}
