package com.verint.textanalytics.dal.darwin.vtasyntax;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * Created by EZlotnik on 9/20/2016.
 */
public class InjectTopicsAsSynonymsTokensFilterFactory extends TokenFilterFactory {

	public InjectTopicsAsSynonymsTokensFilterFactory(Map<String, String> args) {
		super(args);

		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
	}

	@Override
	public InjectTopicsAsSynonymsTokensFilter create(TokenStream input) {
		return new InjectTopicsAsSynonymsTokensFilter(input);
	}
}
