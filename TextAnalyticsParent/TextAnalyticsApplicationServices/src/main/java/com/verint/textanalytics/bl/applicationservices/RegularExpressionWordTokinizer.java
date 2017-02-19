package com.verint.textanalytics.bl.applicationservices;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.interactions.SnippetWord;

/**
 * Tokinizer based on regular expression.
 * 
 * @author EZlotnik
 *
 */
public class RegularExpressionWordTokinizer implements WordTokinizer {
	private Pattern wordPattern;

	/**
	 * C'tor.
	 */
	public RegularExpressionWordTokinizer() {
		wordPattern = Pattern.compile("[\\w\'-]+", Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
	}

	/**
	 * Builds a list of words by parsing the given string. In addition
	 * calculates a distance between two highlights (in words).
	 * 
	 * @param text
	 *            text to be tokinized
	 * @return list of words as found in text parameter
	 */
	public List<SnippetWord> buildWordsList(String text) {
		List<SnippetWord> words = new ArrayList<SnippetWord>();

		if (!StringUtils.isNullOrBlank(text)) {

			Matcher matcher = wordPattern.matcher(text);
			int index = 0;

			SnippetWord currentWord;

			while (matcher.find()) {

				currentWord = new SnippetWord();
				currentWord.setIndex(index);
				currentWord.setWord(text.substring(matcher.start(), matcher.end()).toString());
				currentWord.setStart(matcher.start());
				currentWord.setEnd(matcher.end());

				words.add(currentWord);

				index++;
			}
		}

		return words;
	}
}
