import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Created with IntelliJ IDEA.
 * User: vesha
 */
public class DataProcessor {
    // For lack of a better place: constants for unique POS tags.
    public static final String STARTTAG = "<s>";
    public static final String ENDTAG = "</s>";

    public static final String PHONENUMBER = "phonenumber";
    public static final String TIMETAG = "time";
    public static final String DATETAG = "date";
    public static final String ZIPCODE = "zipcode";
    public static final String GENERALNUMBER = "general_number";

    public static String model = "lib/stanford-postagger-2015-01-30/models/english-left3words-distsim.tagger";
    public static String test = "data/test.txt";
    public static String train = "data/training.txt";
    public static String validate = "data/validation.txt";

    public static TreeMap<String, ArrayList<ArrayList<Unigram>>> data_set = new TreeMap<>();

    public static TreeMap<String, ArrayList<ArrayList<Unigram>>> test_set = new TreeMap<>();

    private static StanfordCoreNLP pipeline;


    /**
     * Initalizes and loads all datasets
     * @throws IOException
     */
    DataProcessor() throws IOException {
        if (pipeline == null) {
            // creates a StanfordCoreNLP object, with POS tagging, tokenization, named entity recognition and parsing
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
            // Splits along newlines
            props.put("ssplit.newlineIsSentenceBreak", "always");

            pipeline = new StanfordCoreNLP(props);
        }

        // Should always reset the data sets because new data is loaded each time DataProcessor is instanciated
        data_set.put("up_train", new ArrayList<>());
        data_set.put("down_train", new ArrayList<>());
        data_set.put("up_validation", new ArrayList<>());
        data_set.put("down_validation", new ArrayList<>());

        // What I'm using to test my code as I go. Comment this out and uncomment the next two lines to run this properly.
        process(train, "train", false);
        process(validate, "validation", false);
        process(test, "test", true);
    }

    /**
     * Initializes and loads only the specified data sets
     * @param processTrain
     * @param processValidation
     * @param processTest
     * @throws FileNotFoundException
     */
    DataProcessor(boolean processTrain, boolean processValidation, boolean processTest) throws IOException {
        if (pipeline == null) {
            // creates a StanfordCoreNLP object, with POS tagging, tokenization, named entity recognition and parsing
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
            // Splits along newlines
            props.put("ssplit.newlineIsSentenceBreak", "always");
            props.put("ptb3Escaping", false);

            pipeline = new StanfordCoreNLP(props);
        }

        if (processTrain) {
            data_set.put("up_train", new ArrayList<>());
            data_set.put("down_train", new ArrayList<>());

            process(train, "train", false);
        }

        if (processValidation) {
            data_set.put("up_validation", new ArrayList<>());
            data_set.put("down_validation", new ArrayList<>());

            process(validate, "validation", false);
        }

        if (processTest) {
            process(test, "test", true);
        }

    }

    /**
     * @param args, do not provide any arguments.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws IOException {
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
    private void process(String source_file, String data_type, Boolean is_test_file) throws IOException {
        String regexString =  "(.*?)" + Pattern.quote("**START**") + "(.*?)" + Pattern.quote("**EOM**") ;
        Pattern pattern = Pattern.compile(regexString, Pattern.DOTALL | Pattern.MULTILINE);

        byte[] encoded = Files.readAllBytes(Paths.get(source_file));

        // Get all text from source_file and pattern match on that.
        Matcher matcher = pattern.matcher(new String(encoded, "UTF-8"));

        String speak_type, email_content;

        while (matcher.find()) {
            String str = matcher.group(1).replaceAll("\\s+", "");

            email_content = matcher.group(2).toLowerCase();

            if (is_test_file) {
                processEmailContent(str, email_content, is_test_file);
            } else {
                speak_type = str.equalsIgnoreCase("DOWNSPEAK") ? UnsmoothedNGram.DOWN : UnsmoothedNGram.UP;
                processEmailContent(makeKey(speak_type, data_type), email_content, is_test_file);
            }
        }
    }

    /**
     * @param key, the key used to index into the data_set map. Corresponds to the set that needs to be updated
     * @param email_content, The content of a given email
     * @param is_test_file, true if run with test file, false otherwise.
     */
    private void processEmailContent(String key, String email_content, Boolean is_test_file) {

        Annotation document = new Annotation(email_content);
        pipeline.annotate(document);

        if (is_test_file) {
            test_set.put(key, new ArrayList<>());
        }

        ArrayList<ArrayList<Unigram>> to_add_to = is_test_file ? test_set.get(key) : data_set.get(key);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        String word, pos, ner, lemma;
        int idx;

        for (CoreMap sentence : sentences) {
            // Add sentence to appropriate data set, including start and end tags.
            to_add_to.add(new ArrayList<>());
            idx = to_add_to.size() - 1;
            to_add_to.get(idx).add(new Unigram(STARTTAG, STARTTAG, "", STARTTAG));

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                // this is the text of the token
                word = token.get(CoreAnnotations.TextAnnotation.class).toLowerCase();

                String uniquePOS = uniquePOS(word);

                if (uniquePOS.isEmpty()) {
                    // this is the POS tag of the token
                    pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    // this is the NER label of the token
                    ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                    lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                } else {
                    pos = uniquePOS;
                    lemma = uniquePOS;
                    ner = "";
                }

                to_add_to.get(idx).add(new Unigram(word, pos, ner, lemma));
            }
            to_add_to.get(idx).add(new Unigram(ENDTAG, ENDTAG, "", ENDTAG));

        }
    }

    private String uniquePOS(String word) {
        // HH:MM
        String time = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        // MM/DD/YYYY
        String date = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)";
        // 5 or 9 digit zipcode
        String zipcode = "^\\d{5}(-\\d{4})?$";
        // XXX-XXX-XXXX
        String phonenumber1 = "\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}";
        //XXXXXXXXXX (phone number with no separation)
        String phonenumber2 = "\\d{10}";
        // (XXX) XXX-XXX
        String phonenumber3 = "\\(\\d{3}\\)\\s\\d{3}-\\d{4}";
        // Any general number. Sort of a catchall.
        String general_number = "(?!$)[\\+-]?([1-9]\\d{0,2}|0)?(\\,\\d{3})*(\\.\\d+)?";

        if (word.matches(time)) {
            return TIMETAG;
        } else if (word.matches(date)) {
            return DATETAG;
        } else if (word.matches(zipcode)) {
            return ZIPCODE;
        } else if (word.matches(phonenumber1) || word.matches(phonenumber2) || word.matches(phonenumber3)) {
            return PHONENUMBER;
        } else if (word.matches(general_number)) {
            return GENERALNUMBER;
        }

        return "";
    }

}
