package com.verint.textanalytics.model.interactions;

import com.verint.textanalytics.common.utils.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Highlight.
 * 
 * @author imor
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KeyTermHighlight extends BaseHighlight {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String keyterm;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<SentimentHighlight> sentiments;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Position> positions;

	/**
	 * AllArgsConstructor.
	 *
	 * @param keyterm
	 *            keyterm
	 * @param sentiments
	 *            sentiments
	 * @param starts
	 *            starts
	 * @param ends
	 *            ends
	 */
	public KeyTermHighlight(String keyterm, List<SentimentHighlight> sentiments, int starts, int ends) {
		super();

		this.keyterm = keyterm;
		this.sentiments = sentiments;
		this.starts = starts;
		this.ends = ends;
	}

	/**
	 * TermHighlight.
	 */
	public KeyTermHighlight() {
		super();
	}

	public String getKey() {
		return String.format("%d_%d", starts, ends);
	}

	/**
	 * Generates a list of keyterms from current keyterm using the path from
	 * top level parent till current one. For example : from
	 * 3/Device/Iphone/Ipnone 5 -> [ 1/Device, 2/Device/Iphone,
	 * 3/Device/Iphone/Ipnone 5]
	 * @return array of keyterms
	 */
	public List<KeyTermHighlight> getKeyTermsHierarchy() {

		List<KeyTermHighlight> keyterms = new ArrayList<KeyTermHighlight>();

		if (!StringUtils.isNullOrBlank(this.keyterm)) {
			String[] splitedKeyTerm = this.keyterm.split("/");

			if (splitedKeyTerm != null) {
				KeyTermHighlight keyTermHighlightToAdd;
				StringBuilder keyTermName;

				for (int i = 1; i < splitedKeyTerm.length - 1; i++) {
					keyTermHighlightToAdd = new KeyTermHighlight();

					keyTermHighlightToAdd.setEnds(this.getEnds());
					keyTermHighlightToAdd.setStarts(this.getStarts());
					keyTermHighlightToAdd.setSentiments(this.getSentiments());
					keyTermName = new StringBuilder();

					for (int j = 1; j <= i; j++) {
						keyTermName.append("/");
						keyTermName.append(splitedKeyTerm[j]);
					}
					keyTermHighlightToAdd.setKeyterm(keyTermName.toString());
					keyterms.add(keyTermHighlightToAdd);
				}
			}
		}

		return keyterms;
	}
}
