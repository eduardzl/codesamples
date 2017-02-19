package com.verint.textanalytics.dal.darwin.vtasyntax;


import static org.junit.Assert.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.pattern.PatternReplaceCharFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceCharFilterFactory;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.*;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by EZlotnik on 2/24/2016.
 */
public class CustomLuceneAnalyzerTest {

	@Test
	public void createCustomeLuceneAnalyzer() {
		TokenStream tokenStream = null;
		String term;
		try {
			Analyzer analyzer = CustomAnalyzer.builder()
			                                  .withTokenizer(StandardTokenizerFactory.class)
			                                  .addTokenFilter(DelimitedPayloadTokenFilterFactory.class, "delimiter", "|", "encoder", "identity")
			                                  .addTokenFilter(InjectTopicsAsSynonymsTokensFilterFactory.class, new HashMap<String, String>())
			                                  .build();

			String text = "Buy car|__TL0RldmljZS9Nb2JpbGUgcGhvbmU with no more then 100 kilometers";
			tokenStream = analyzer.tokenStream("text_en_total", new StringReader(text));
			tokenStream.reset();

			int position = 0;
			while (tokenStream.incrementToken()) {
				CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
				PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
				PositionLengthAttribute positionLengthAttribute = tokenStream.getAttribute(PositionLengthAttribute.class);
				OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
				TypeAttribute typeAttribute = tokenStream.getAttribute(TypeAttribute.class);


				// add position increment to calculate the position
				// for <common gram> token, the position increment is 0

				term = termAttribute.toString();
				System.out.println(String.format("Term - %s", term));
			}
		} catch (Exception ex) {
			System.out.println(ex);
		} finally {

			try {
				if (tokenStream != null) {
					tokenStream.close();
				}
			} catch (Exception ex) {
				;
			}
		}

	}
}
