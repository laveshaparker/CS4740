import java.util.ArrayList;
import java.util.Random;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class RSG {
  
  public static void main(String[] args){
    
    System.out.println("\n***************UNIGRAM DOWNSPEAK***************");
    for (int i = 1; i <= 50; i++){
      System.out.print(i);
      RSG.genUnigram("../out/unigram_down.txt");
    }
    
    System.out.println("\n***************UNIGRAM UPSPEAK***************");
    for (int i = 1; i <= 50; i++){
      System.out.print(i);
      RSG.genUnigram("../out/unigram_up.txt");
    }
    
    System.out.println("\n***************BIGRAM DOWNSPEAK***************");
    for (int i = 1; i <= 50; i++){
      System.out.print(i);
      RSG.genBigram("../out/bigram_down.txt");
    }
    
    System.out.println("\n***************BIGRAM UPSPEAK***************");
    for (int i = 1; i <= 50; i++){
      System.out.print(i);
      RSG.genBigram("../out/bigram_up.txt");
    }
    
    System.out.println("\n***************TRIGRAMS DOWNSPEAK***************");
    for (int i = 1; i <= 50; i++){
      System.out.print(i);
      RSG.genTrigram("../out/bigram_down.txt", "../out/trigram_down.txt");
    }
    
    System.out.println("\n***************TRIGRAMS UPSPEAK***************");
    for (int i = 1; i <= 50; i++){
      System.out.print(i);
      RSG.genTrigram("../out/bigram_up.txt", "../out/trigram_up.txt");
    }
  }
  
  public static int SENTENCE_MAX = 100;
  public static Random random = new Random();
  
  /*Get initial ArrayList containing n-grams and their counts.*/
  public static ArrayList<String> getInitialArrayList(String filename){
    
    ArrayList<String> arrayList = new ArrayList<String>();
    
    //read in file
    try {
      FileReader fr = new FileReader(filename);
      BufferedReader br = new BufferedReader(fr);
      
      String str;
      
      while((str = br.readLine()) != null){
        
        String word = "";
        int count = 0;
        
        int indexOfLastSpace = str.lastIndexOf(" ");
        String countAsString = str.substring(indexOfLastSpace + 1);
        
        count = Integer.parseInt(countAsString);
        word = str.substring(0,indexOfLastSpace);
        
        //put word into arrayList, count amount of times
        //don't do it if its "<s>" since it's forced at start of randomSentence
        for (int i = 0; i < count && !(word.equals("<s>")); i++){
          arrayList.add(word);
        }
      }
      br.close();
    } catch (IOException e){
      System.out.println("Error while reading in txt file");
    }
    return arrayList;
  }
  
  /* generate sentence based on unigram model*/
  public static void genUnigram(String textFile){
    
    ArrayList<String> arrayList = getInitialArrayList(textFile); //arrayList filled with unigrams
    
    String randomSentence = "<s>";
    String randomWord = "";
    int loopCounter = 0;
    
    //the loop: select new random word, append, and repeat until end of sentence
    while(!(randomSentence.contains("</s>")) && loopCounter <= SENTENCE_MAX){
      int randomIndex = random.nextInt(arrayList.size());
      randomWord = arrayList.get(randomIndex);
      randomSentence += " " + randomWord;
      loopCounter++;
    }
    System.out.println(randomSentence);
  }
  
  /* generate sentence based on bigram model*/
  public static void genBigram(String textFileBigram){
    
    ArrayList<String> arrayListBi = getInitialArrayList(textFileBigram);
    
    String randomSentence = "<s>";
    String previousWord = "<s>";
    String randomWord = ""; 
    int loopCounter = 0;
    
    //the loop: select new random bigram, append the last word, and repeat until end of sentence
    while(!(randomSentence.contains("</s>")) && loopCounter <= SENTENCE_MAX){
      ArrayList<String> arrayListCurrent = new ArrayList<String>(); //inside loop: ArrayList for next possible bigram
      
      //put possible bigrams (that start with "previousWord") into arrayListCurrent, then pick randomWord from these.
      //distributed probability already taken care of in arrayListBi
      for (int i = 0; i < arrayListBi.size(); i++){
        
        if (arrayListBi.get(i).startsWith(previousWord)){
          arrayListCurrent.add(arrayListBi.get(i));
        }
      }
      if (arrayListCurrent.size() == 0) {
        randomSentence += " </s>";
      } else {
        
        int randomIndex = random.nextInt(arrayListCurrent.size());
        randomWord = arrayListCurrent.get(randomIndex); //get random element (bigram)
        randomWord = randomWord.substring(randomWord.lastIndexOf(" ") + 1); //get last word only
        previousWord = randomWord;
        randomSentence += " " + randomWord;
        loopCounter++;
      }
    }
    System.out.println(randomSentence);
  }
  
  /* generate sentence based on trigram model*/
  public static void genTrigram(String textFileBigram, String textFileTrigram){
    
    ArrayList<String> arrayListBi = getInitialArrayList(textFileBigram); 
    ArrayList<String> arrayListTri = getInitialArrayList(textFileTrigram);
    
    String randomSentence = "<s>";
    String previousWord = "<s>";
    String randomWord = "";
    int loopCounter = 0;
    
    //NOTE: first iteration here is identical to the loop in genBigram since it's only for the first word after <s>
    ArrayList<String> arrayListCurrent = new ArrayList<String>(); //ArrayList for next possible bigram
    
    //put possible bigrams (that start with "previousWord") into arrayListCurrent, then pick randomWord from there.
    //distributed probability already taken care of in arrayListBi
    for (int i = 0; i < arrayListBi.size(); i++){
      
      if (arrayListBi.get(i).startsWith(previousWord)){
        arrayListCurrent.add(arrayListBi.get(i));
      }
    }
    
    if (arrayListCurrent.size() == 0) {
      randomSentence += " </s>";
    } else {
      
      int randomIndex = random.nextInt(arrayListCurrent.size());
      randomWord = arrayListCurrent.get(randomIndex); //get random element (bigram)
      randomWord = randomWord.substring(randomWord.lastIndexOf(" ") + 1); //get last word only
      previousWord = randomWord;
      randomSentence += " " + randomWord;
    }
    
    String previousBigram = "";
    
    
    //NOTE: loop for trigrams starts here
    while(!(randomSentence.contains("</s>")) && loopCounter <= SENTENCE_MAX){
      ArrayList<String> arrayListCurrentTri = new ArrayList<String>(); //ArrayList for next possible bigram
      
      //next 3 lines get the last bigram, which will be the first two words of the next trigram selected
      String sentenceExceptLastWord = randomSentence.substring(0, randomSentence.lastIndexOf(" "));
      int secondLastIndexOfSpace = sentenceExceptLastWord.lastIndexOf(" ");
      previousBigram = randomSentence.substring(secondLastIndexOfSpace + 1);
      
      //put possible trigrams (that start with "previousBigram") into arrayListCurrentTri, then pick randomWord from there.
      //distributed probability already taken care of in arrayListBi
      for (int i = 0; i < arrayListTri.size(); i++){
        
        if (arrayListTri.get(i).startsWith(previousBigram)){
          arrayListCurrentTri.add(arrayListTri.get(i));
        }
      }
      if (arrayListCurrentTri.size() == 0) {
        randomSentence += " </s>";
      } else {
        
        int randomIndex = random.nextInt(arrayListCurrentTri.size());
        randomWord = arrayListCurrentTri.get(randomIndex); //get random element (trigram)
        randomWord = randomWord.substring(randomWord.lastIndexOf(" ") + 1); //get last word only
        randomSentence += " " + randomWord;
        loopCounter++;
      }
    }
    System.out.println(randomSentence);
  }
}