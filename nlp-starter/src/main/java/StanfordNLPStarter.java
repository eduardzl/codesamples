import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.StringReader;

/**
 * Created by home on 4/10/2017.
 */
public class StanfordNLPStarter {

    public void tokinize() {
        String paragraph = "Let's pause, \nand then ++ reflect.";

        PTBTokenizer ptb = new PTBTokenizer(new StringReader(paragraph), new CoreLabelTokenFactory(),null);

        while (ptb.hasNext()) {
            CoreLabel cl = (CoreLabel) ptb.next();
            System.out.println(String.format("Token : %s", cl.originalText()));
        }
    }
}
