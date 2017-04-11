import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by home on 4/10/2017.
 */
public class OpenNLPStarter {

    public String[] tokinize(String text) {
        String[] tokens = null;

        if (!StringUtils.isEmpty(text)) {
            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

            tokens = tokenizer.tokenize(text);

            if (tokens != null) {
                for (String token : tokens) {
                    System.out.println(token);
                }
            }
        }

        return tokens;
    }
}
