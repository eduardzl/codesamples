package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.model.interactions.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates snippet by given text and set of highlights.
 *
 * @author NShunewich
 */
public class SnippetsGenerator {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Getter
	@Setter
	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	@Setter
	private WordTokinizer wordTokinizer;

	@Autowired
	@Getter
	@Setter
	private SnippetsConfiguration snippetsConfig;

	private Comparator<Utterance> dateComparator = new Comparator<Utterance>() {
		@Override
		public int compare(Utterance u1, Utterance u2) {
			if (u1.getDate() != null && u2.getDate() != null) {
				return u1.getDate().compareTo(u2.getDate());
			} else {
				return 0;
			}
		}
	};


	/**
	 * Builds snippet for each of the interactions in the collection.
	 *
	 * @param searchContext Search context object with request parameters
	 * @param interactions  a list of interactions to operate
	 * @param needsReduce   SetToTure if snippets needs to be cut
	 */
	public void buildSnippets(SearchInteractionsContext searchContext, List<Interaction> interactions, boolean needsReduce) {

		if (!CollectionUtils.isEmpty(interactions)) {
			interactions.parallelStream().forEach(interaction -> {
				if (interaction.getUtterances() != null) {
					this.buildSnippets(searchContext, interaction, needsReduce);
				}
			});
		}
	}

	/**
	 * Builds snippet for single interaction and set it in Snippet collection.
	 *
	 * @param searchContext Search context object with request parameters
	 * @param interaction   Operated interaction.
	 * @param needsReduce   SetToTure if snippets needs to be cut
	 */
	public void buildSnippets(SearchInteractionsContext searchContext, Interaction interaction, boolean needsReduce) {

		try {
			final Integer logTextMaxLength = 40;

			interaction.setSnippets(new ArrayList<Snippet>());

			List<Utterance> utterances = interaction.getUtterances();
			if (!CollectionUtils.isEmpty(utterances)) {

				// @formatter:off
				AtomicInteger i = new AtomicInteger(0);

				// sort utterances by DateTime descending
				// and then generated snippets
				utterances.stream()
				          .sorted(dateComparator)
						  .forEachOrdered(utterance -> {

							  // collecting highlights from different resources (term, topic)
							  List<BaseHighlight> operatedHighlights = utterance.getMergedHighlighting();
							  sortHighlights(operatedHighlights);

							  logger.trace("New utterance: '{}'", () -> {

								  // log output
								  Integer logTextLength = logTextMaxLength;
								  if (utterance.getText().length() - 1 < logTextMaxLength) {
									  logTextLength = utterance.getText().length() - 1;
								  }

								  return utterance.getText().substring(0, logTextLength);
							  });

							  Boolean isNextUtteranceHasHighlight = this.hasHighlight(interaction.getUtterances(), i.get() + 1);
							  Boolean isLastUtterance = this.isLastUtterance(interaction.getUtterances(), i.get());

							  List<Snippet> snippets = this.generate(utterance.getText(), operatedHighlights, isNextUtteranceHasHighlight || isLastUtterance, utterance.getId());

							  logger.trace("created snippets per current utterance: {}", () -> snippets.size());

							  interaction.getSnippets().addAll(snippets);

							  i.getAndIncrement();
						  });
				// @formatter:on
			}


			logger.trace("Total Number of Snippets created for current Interaction: {}", interaction.getSnippets().size());

			if (needsReduce) {
				// Reduce snippets to max length
				List<Snippet> dilutedSnippets = this.diluteSnippetByLength(interaction.getSnippets());
				if (dilutedSnippets.size() == 0) {
					interaction.setSnippets(this.createNoHighlightsSnippet(interaction));
				} else {
					interaction.setSnippets(dilutedSnippets);
				}
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.InteractionSnippetsBuildError));
		}
	}

	/**
	 * Executes snippet generation.
	 * 
	 * @param text
	 *            The text containing highlights
	 * @param highlights
	 *            Set of highlights, which must be ordered acceding by start
	 *            position.
	 * @param isLastUtterance
	 *            Indicates if the operated utterance is last for getting the
	 *            whole right side of string.
	 * @param utteranceId
	 *            The id of the utterance that inckudes this snippet.
	 * 
	 * @return generated list of snippets
	 * @throws Exception
	 *             exception in the case of failure.
	 */
	public List<Snippet> generate(String text, List<BaseHighlight> highlights, Boolean isLastUtterance, String utteranceId) {
		List<Snippet> snippets = new ArrayList<Snippet>();

		try {
			Snippet snippet = null;
			SnippetWord currentWord, firstHighlightedWord = null, lastHighlightedWord = null;

			Boolean isHighlightingStarted = false, isLastHighlight = false;
			int snippetStartPos = 0, snippetEndPos = 0;
			int followingWords = 0;
			int currentIndex;

			Boolean isSnippetStarted = false;

			if (highlights != null && highlights.size() > 0) {
				logger.trace("Number of highlights: '{}'", highlights.size());

				List<SnippetWord> words = wordTokinizer.buildWordsList(text);
				this.linkHighlightsToWords(words, highlights);

				// Run through all the words
				for (currentIndex = 0; currentIndex < words.size(); currentIndex++) {
					currentWord = words.get(currentIndex);

					if (currentWord.getIsHighlight()) {

						isLastHighlight = currentWord.getIsLastHighlight();

						if (!isSnippetStarted) {
							// Initialize new snippet
							snippet = startNewSnippet();
							snippet.setUtteranceId(utteranceId);
							snippetStartPos = calculateStartPosition(words, currentIndex);
							isSnippetStarted = true;
						}

						if (!isHighlightingStarted) {
							isHighlightingStarted = true;
							firstHighlightedWord = currentWord;
						}

						lastHighlightedWord = currentWord;

						followingWords = 0;
					}

					if (isSnippetStarted) {

						// NOT last word, highlighed and HAS NO the same content with the next word
						// In this case, generate a highlight with different content
						if (!isLastWord(words, currentIndex) && isHighlightingStarted && isNextWordIsHighlightWithDifferentContent(words, currentIndex)) {
							addSnippetHighlightMultipleWords(snippet, snippetStartPos, firstHighlightedWord, words.get(currentIndex));
							isHighlightingStarted = false;
							continue;
						}

						if (isHighlightingStarted  && currentWord.getIsLastHighlightingWord()) {
							addSnippetHighlightMultipleWords(snippet, snippetStartPos, firstHighlightedWord, lastHighlightedWord);
							isHighlightingStarted = false;
						}

						if (!currentWord.getIsHighlight() || isLastWord(words, currentIndex)) {

							snippetEndPos = currentWord.getEnd();
							followingWords++;

							if (isNextHighlightMerged(words, currentIndex))
								continue;

							if (isEndPosition(followingWords) || (isLastHighlight && isLastUtterance)) {
								// Create snippet and add it to collection

								if (isLastHighlight && isLastUtterance)
									snippetEndPos = text.length();

								snippet = buildSnippetBlock(snippet, text, snippetStartPos, snippetEndPos);

								snippets.add(snippet);
								isSnippetStarted = false;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		return snippets;
	}

	/**
	 * Function reduces a snippets by the maximum length defined in the
	 * configuration.
	 * 
	 * @param snippets
	 *            Initial full snippets array
	 * @return reduced snippets collection
	 */
	public List<Snippet> diluteSnippetByLength(List<Snippet> snippets) {
		int snippetLength = 0;
		List<Snippet> dilutedSnippets = new ArrayList<Snippet>();
		int maxLength = configurationManager.getApplicationConfiguration().getInteractionSnippetsFullTextMaxLength();

		for (Snippet snippet : snippets) {
			snippetLength += snippet.getText().length();
			if (snippetLength < maxLength) {
				dilutedSnippets.add(snippet);
			}
		}
		return dilutedSnippets;
	}

	private Boolean isLastUtterance(List<Utterance> utterances, int index) {
		Boolean isLastUtterance = true;

		if (utterances.size() == 1 || index == utterances.size() - 1) {
			isLastUtterance = true;
		}

		return isLastUtterance;
	}

	private Boolean hasHighlight(List<Utterance> utterances, int index) {

		Boolean isLastUtterance = true;
		Utterance utt;
		Boolean hasMergedHighlight = false;

		for (int j = index; j < utterances.size(); j++) {
			utt = utterances.get(j);
			hasMergedHighlight = utt.getMergedHighlighting() != null && utt.getMergedHighlighting().size() > 0;

			if (hasMergedHighlight) {
				// Check if there is at least utterance having highlights
				isLastUtterance = false;
				break;
			}
		}

		return isLastUtterance;
	}

	private List<Snippet> createNoHighlightsSnippet(Interaction interaction) {
		List<Snippet> snippets = null;

		List<Utterance> utterances = interaction.getUtterances();
		if (!CollectionUtils.isEmpty(utterances)) {

			Utterance utterance = interaction.getUtterances().get(0);
			String text = utterance.getText();

			// The long utterance must be cut accordingly to the max length
			if (text.length() > configurationManager.getApplicationConfiguration().getInteractionSnippetsFullTextMaxLength()) {
				text = text.substring(0, configurationManager.getApplicationConfiguration().getInteractionSnippetsFullTextMaxLength() - 1);
			}

			Snippet snippet = new Snippet();
			snippet.setText(text);

			snippets = new ArrayList<>();
			snippets.add(snippet);
		}

		return snippets;
	}

	private int calculateStartPosition(List<SnippetWord> words, int currentIndex) {
		int snippetStartPos;
		int startIndex = currentIndex - snippetsConfig.getMaxPrecedingWords();

		if (startIndex < 0)
			snippetStartPos = 0;
		else
			snippetStartPos = words.get(startIndex).getStart();

		return snippetStartPos;
	}

	private Boolean isEndPosition(int followingWords) {
		if (followingWords < snippetsConfig.getMaxFollowingWords())
			return false;
		else
			return true;
	}

	private Boolean isNextHighlightMerged(List<SnippetWord> words, int currentWordIndex) {
		int nextHighlightWordIndex = getNextHighlightWordPosition(words, currentWordIndex + 1);

		if (nextHighlightWordIndex == -1)
			return false;

		if (nextHighlightWordIndex - currentWordIndex < snippetsConfig.getMaxPrecedingWords() + snippetsConfig.getMaxFollowingWords())
			return true;
		else
			return false;
	}

	private Snippet startNewSnippet() {
		Snippet snippet = new Snippet();
		return snippet;
	}

	private void addSnippetHighlightMultipleWords(Snippet snippet, int snippetStartPos, SnippetWord firstHighlightedWord, SnippetWord lastHighlightedWord) {
		BaseHighlight snippetHighlight;
		List<HighlightContent> content;

		int start, end;

		start = firstHighlightedWord.getStart() - snippetStartPos;
		end = lastHighlightedWord.getEnd() - snippetStartPos;
		content = new ArrayList<HighlightContent>();

		// TODO: Remember content from all snippets
		if (firstHighlightedWord.getHighlight().getContents() != null) {
			content.addAll(firstHighlightedWord.getHighlight().getContents());
		}

		snippetHighlight = new BaseHighlight(start, end, content);

		snippet.getSnippetHighlights().add(snippetHighlight);
	}

	private Snippet buildSnippetBlock(Snippet snippet, String text, int snippetStartPos, int snippetEndPos) {
		snippet.setText(text.substring(snippetStartPos, snippetEndPos));
		snippet.setEnd(snippetEndPos);
		snippet.setStart(snippetStartPos);
		return snippet;
	}

	private void sortHighlights(List<BaseHighlight> mergedHighlight) {
		Collections.sort(mergedHighlight, new Comparator<BaseHighlight>() {
			@Override
			public int compare(BaseHighlight o1, BaseHighlight o2) {
				return o1.getStarts() - o2.getStarts();
			}
		});
	}

	private void linkHighlightsToWords(List<SnippetWord> words, List<BaseHighlight> highlights) {
		List<SnippetWord> inRangeWords = new ArrayList<SnippetWord>();
		BaseHighlight highlight, wordHighlight;
		List<HighlightContent> hContents, wContents;
		Boolean isContentExist;

		for (int i = 0; i < highlights.size(); i++) {
			highlight = highlights.get(i);
			inRangeWords = getInRangeWords(words, highlight);

			// for each inRange words
			for (int hWordsIndex = 0; hWordsIndex < inRangeWords.size(); hWordsIndex++) {
				SnippetWord word = inRangeWords.get(hWordsIndex);
				if (!word.getIsHighlight()) {
					word.setIsHighlight(true);
					wordHighlight = new BaseHighlight();
					wordHighlight.setStarts(highlight.getStarts());
					wordHighlight.setEnds(highlight.getEnds());
					if (highlight.getContents() != null) {
						wordHighlight.setContents(new ArrayList<HighlightContent>());
						wordHighlight.getContents().addAll(highlight.getContents());
					}
					word.setHighlight(wordHighlight);
				} else {
					hContents = highlight.getContents();
					wContents = word.getHighlight().getContents();

					if (hContents != null)
						for (HighlightContent hc : hContents) {
							isContentExist = false;
							for (HighlightContent wc : wContents) {
								if ((hc.getType().name().equals("Term") && hc.getType().name().equals(wc.getType().name()))
										|| (hc.getType().name().equals(wc.getType().name()) && hc.getData().equals(wc.getData()))) {
									isContentExist = true;
									break;
									}
								}
							if (isContentExist == false)
								wContents.add(hc);
							}
					else {
						// Highlight without content -> term highlight
						HighlightContent hc = new HighlightContent();
						hc.setType(HighlightType.Term);
						hc.setData(word.getWord());
						wContents.add(hc);
					}
				}

				word.setIsLastHighlightingWord(hWordsIndex == inRangeWords.size() - 1);
				if (i == highlights.size() - 1) {
					word.setIsLastHighlight(true);
				}
			}
		}
	}

	private List<SnippetWord> getInRangeWords(List<SnippetWord> words, BaseHighlight highlight) {
		List<SnippetWord> inRangeWords = new ArrayList<SnippetWord>();
		int wordStart, wordEnd;
		int hlStart = highlight.getStarts();
		int hlEnd = highlight.getEnds();

		for (SnippetWord word : words) {
			wordStart = word.getStart();
			wordEnd = word.getEnd();

			if (wordEnd < hlStart || wordStart > hlEnd)
				continue;
			else
				inRangeWords.add(word);
		}

		return inRangeWords;
	}

	private int getNextHighlightWordPosition(List<SnippetWord> words, int startFrom) {
		SnippetWord word;
		int pos = -1;
		for (int i = startFrom; i < words.size(); i++) {
			word = words.get(i);
			if (word.getHighlight() != null) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	/**
	 *
	 * @param words words
	 * @param currentIndex currentIndex
	 * @return isLastWord isLastWord
	 */
	private Boolean isLastWord(List<SnippetWord> words, int currentIndex) {
		return currentIndex == words.size() - 1;
	}

	/**
	 *
	 * @param words words
	 * @param currentIndex currentIndex
	 * @return isNextWordIsHighlightWithDifferentContent isNextWordIsHighlightWithDifferentContent
	 */
	public Boolean isNextWordIsHighlightWithDifferentContent(List<SnippetWord> words, int currentIndex) {

		if (isLastWord(words, currentIndex))
			return false;

		SnippetWord currentWord = words.get(currentIndex);
		SnippetWord nextWord = words.get(currentIndex + 1);
		HighlightContent currentWordContent, nextWordContent;

		if (currentWord.getHighlight() == null || nextWord.getHighlight() == null)
			return false;

		if (currentWord.getHighlight().getContents() == null || nextWord.getHighlight().getContents() == null)
			return false;

		if (currentWord.getHighlight().getContents().size() != nextWord.getHighlight().getContents().size())
			return true;

		for (int i = 0; i < currentWord.getHighlight().getContents().size(); i++) {
			currentWordContent = currentWord.getHighlight().getContents().get(i);
			for (int j = 0; j < nextWord.getHighlight().getContents().size(); j++) {
				nextWordContent = nextWord.getHighlight().getContents().get(i);
				if (!currentWordContent.equals(nextWordContent))
					return true;
			}
		}

		return false;
	}
}
