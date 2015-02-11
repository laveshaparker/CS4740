import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Created with IntelliJ IDEA.
 * User: vesha
 */
public class DataProcessor {

    public static final Unigram start_tag = new Unigram("<s>", "<s>", "");
    public static final Unigram end_tag = new Unigram("</s>", "</s>", "");
    public static String model = "lib/stanford-postagger-2015-01-30/models/english-left3words-distsim.tagger";
    public static String test = "data/smalltest.txt";
    public static String train = "data/training.txt";
    public static String validate = "data/validation.txt";

    public static TreeMap<String, ArrayList<ArrayList<Unigram>>> data_set = new TreeMap<>();

    private static StanfordCoreNLP pipeline;

    DataProcessor() throws FileNotFoundException {
        // creates a StanfordCoreNLP object, with POS tagging, tokenization, named entity recognition and parsing
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        pipeline = new StanfordCoreNLP(props);

        data_set.put("up_train", new ArrayList<>());
        data_set.put("down_train", new ArrayList<>());
        data_set.put("up_validation", new ArrayList<>());
        data_set.put("down_validation", new ArrayList<>());

        // What I'm using to test my code as I go. Comment this out and uncomment the next two lines to run this properly.
        process(test, "train");

        // process(train, "train");
        // process(validate, "validation");
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
     * @todo Add support for data_type = "test"
     * @todo Add some sort of handling for extremely long sentences. Currently getting OOM warnings from the parser.
     * @throws FileNotFoundException
     */
    private void process(String source_file, String data_type) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(source_file)).useDelimiter("\\*\\*.*?\\*\\*");

        String speak_type, email_content;

        while (sc.hasNext()) {
            String str = sc.next().replaceAll("\\s+", "");
            speak_type = str.equalsIgnoreCase("DOWNSPEAK") ? UnsmoothedNGram.DOWN : UnsmoothedNGram.UP;
            email_content = sc.next();

            processEmailContent(makeKey(speak_type, data_type), email_content);
        }

    }

    /**
     * @todo Add newlines to sentence end delimiters so that the salutation and greetings are not considered part of the email.
     * @todo Process all proper nouns as <PRN>
     * @todo Save part of speech for each <UNK>
     * @param key, the key used to index into the data_set map. Corresponds to the set that needs to be updated
     * @param email_content, The content of a given email
     */
    private void processEmailContent(String key, String email_content) {

        Annotation document = new Annotation(email_content);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        String word, pos, ne;
        int idx;

        for(CoreMap sentence: sentences) {
            // Add sentence to appropriate data set, including start and end tags.
            data_set.get(key).add(new ArrayList<>());
            idx = data_set.get(key).size() - 1;
            data_set.get(key).get(idx).add(start_tag);

            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                word = token.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
                // this is the POS tag of the token
                pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                data_set.get(key).get(idx).add(new Unigram(word, pos, ne));
            }
            data_set.get(key).get(idx).add(end_tag);

            // Uncomment if you want to see how the sentence is constructed.
            // System.out.println(data_set.get(key).get(idx).toString());
        }
    }
}
