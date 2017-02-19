package com.verint.textanalytics.bl.applicationservices;

import java.util.List;

import com.verint.textanalytics.model.interactions.SnippetWord;

/**
 * Interface for tokinizer of text for snippet generation.
 * 
 * @author EZlotnik
 *
 */
public interface WordTokinizer {

	/**
	 * Generates list of words from text.
	 * 
	 * @param text
	 *            text to be tokinized
	 * @return list of words
	 * @throws Exception
	 *             Exception in the case of failure
	 */
	List<SnippetWord> buildWordsList(String text) throws Exception;
}
