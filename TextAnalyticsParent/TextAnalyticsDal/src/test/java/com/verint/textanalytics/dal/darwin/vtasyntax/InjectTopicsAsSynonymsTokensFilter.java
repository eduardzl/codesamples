package com.verint.textanalytics.dal.darwin.vtasyntax;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;

import java.io.IOException;

/**
 * Created by EZlotnik on 9/20/2016.
 */
public class InjectTopicsAsSynonymsTokensFilter extends TokenFilter {
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute positionIncrementAttr = addAttribute(PositionIncrementAttribute.class);
	private final PayloadAttribute payloadAttr = addAttribute(PayloadAttribute.class);

	/**
	 * Create a new InjectTopicsAsSynonymsTokensFilter, that injects a topics as tokens synonyms.
	 *
	 * @param in TokenStream to filter
	 */
	public InjectTopicsAsSynonymsTokensFilter(TokenStream in) {
		super(in);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			char[] buffer = termAtt.buffer();
			int length = termAtt.length();
			int positionIncrement = positionIncrementAttr.getPositionIncrement();
			String payload;
			if (payloadAttr != null && payloadAttr.getPayload() != null) {
				payload = payloadAttr.getPayload().toString();
			}

			System.out.println(String.format("Token term - %s", new String(buffer)));
			System.out.println(String.format("Token position incremenent - %s", positionIncrement));

			return true;
		} else
			return false;
	}
}
