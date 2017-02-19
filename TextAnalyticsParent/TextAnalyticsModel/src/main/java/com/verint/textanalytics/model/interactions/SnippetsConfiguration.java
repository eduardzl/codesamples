package com.verint.textanalytics.model.interactions;

// TODO: Move to BL?
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;

import com.verint.textanalytics.common.configuration.ConfigurationManager;

/**
 * Provides configuration properties for building snippet.
 * 
 * @author NShunewich
 *
 */
@NoArgsConstructor
public class SnippetsConfiguration {

	@Getter
	@Setter
	@Autowired
	private ConfigurationManager configurationManager;

	/**
	 * Number of preceding words.
	 */
	@Getter
	@Setter
	private int maxPrecedingWords;
	/**
	 * Number of following words.
	 */
	@Getter
	@Setter
	private int maxFollowingWords;

	/**
	 * Total max length of the snippet set.
	 */
	@Getter
	@Setter
	private int fullTextMaxLength;

	/**
	 * Spring Initialization of the object.
	 */
	public void snippetsConfigurationInit() {

		maxPrecedingWords = configurationManager.getApplicationConfiguration().getInteractionSnippetsMaxPrecedingWords();
		maxFollowingWords = configurationManager.getApplicationConfiguration().getInteractionSnippetsMaxFollowingWords();
		fullTextMaxLength = configurationManager.getApplicationConfiguration().getInteractionSnippetsFullTextMaxLength();

		if (maxPrecedingWords == 0)
			maxPrecedingWords = 5;
		if (maxFollowingWords == 0)
			maxFollowingWords = 5;
		if (fullTextMaxLength == 0)
			fullTextMaxLength = 5;
	}
}
