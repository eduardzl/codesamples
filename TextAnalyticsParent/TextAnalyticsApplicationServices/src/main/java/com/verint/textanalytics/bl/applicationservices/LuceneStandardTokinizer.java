package com.verint.textanalytics.bl.applicationservices;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.interactions.SnippetWord;

/**
 * Tokenizer based on Lucene Standard.
 * 
 * @author EZlotnik
 *
 */
public class LuceneStandardTokinizer implements WordTokinizer {
	/**
	 * Builds a list of words by parsing the given string. In addition
	 * calculates a distance between two highlights (in words).
	 * 
	 * @param text
	 *            text to be tokinized
	 * @return list of words as found in text parameter
	 * @throws Exception
	 *             exception in the case of failure
	 */
	public List<SnippetWord> buildWordsList(String text) throws Exception {
		List<SnippetWord> words = new ArrayList<SnippetWord>();

		StandardTokenizer tokenizer = null;
		StringReader stringReader = null;

		try {
			if (!StringUtils.isNullOrBlank(text)) {
				stringReader = new StringReader(text);
				tokenizer = new StandardTokenizer();
				tokenizer.setReader(stringReader);
				tokenizer.reset();

				int index = 0, starOffset, endOffset;
				SnippetWord currentWord;

				while (tokenizer.incrementToken()) {
					CharTermAttribute termAttribute = tokenizer.getAttribute(CharTermAttribute.class);
					OffsetAttribute offsetAttribute = tokenizer.getAttribute(OffsetAttribute.class);
					starOffset = offsetAttribute.startOffset();
					endOffset = offsetAttribute.endOffset();

					currentWord = new SnippetWord();
					currentWord.setIndex(index);
					currentWord.setWord(termAttribute.toString());
					currentWord.setStart(starOffset);
					currentWord.setEnd(endOffset);

					words.add(currentWord);

					index++;
				}
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		} finally {
			if (stringReader != null) {
				stringReader.close();
			}

			try {
				if (tokenizer != null) {
					tokenizer.end();
					tokenizer.close();
				}
			} catch (Exception ex) {
				throw new Exception(ex);
				// was unable to close the tokinizer
			}
		}

		return words;
	}
}
