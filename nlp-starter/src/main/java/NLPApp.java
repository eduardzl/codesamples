

/**
 * Created by home on 4/10/2017.
 */
public class NLPApp {

    public static void main(String[] args) {
        String paragraph = "Let's pause, \nand then ++ reflect.";

        StopWords stopWords = new StopWords();

        OpenNLPStarter openNLPStarter = new OpenNLPStarter();
        String[] tokens = openNLPStarter.tokinize(paragraph);
        String[] tokensNoStopWords = stopWords.removeStopWords(tokens);

        StanfordNLPStarter stNLPStarter = new StanfordNLPStarter();
        stNLPStarter.tokinize();
    }



}
