import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Created by home on 4/10/2017.
 */
public class StopWords {
    private String[] defaultStopWords = {"i", "a", "about", "an", "are",
                                        "as", "at", "be", "by", "com", "for", "from", "how", "in", "is", "it",
                                        "of", "on", "or", "that", "the", "this", "to", "was", "what", "when",
                                         "where", "who", "will", "with"
    };

    private Set<String> stopWordsSet = new HashSet<>();

    public StopWords() {
        stopWordsSet.addAll(Arrays.asList(defaultStopWords));
    }

    public String[] removeStopWords(String[] tokens) {
        List<String> tokensList = new ArrayList<>(Arrays.asList(tokens));

        String token;
        Iterator<String> it = tokensList.iterator();
        while (it.hasNext()) {
            token = it.next();
            if (this.stopWordsSet.contains(token)) {
                it.remove();
            }
        }

        return tokensList.toArray(new String[tokensList.size()]);
    }

}
