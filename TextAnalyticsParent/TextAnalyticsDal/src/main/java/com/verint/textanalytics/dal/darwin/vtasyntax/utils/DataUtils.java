package com.verint.textanalytics.dal.darwin.vtasyntax.utils;

/**
 * Created by EZlotnik on 3/16/2016.
 */
public final class DataUtils {

	private DataUtils() {

	}

	/**
	 * Escaping special characters for Solr request.
	 * @param s string to be escaped
	 * @return an escaped string
	 */
	public static String escapeCharsForSolrQuery(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {

			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				sb.append('\\');
				sb.append(c);
			} else {
				// These characters are part of the query syntax and must be escaped
				switch (c) {
					case '\\':
					case '+':
					case '-':
					case '!':
					case '(':
					case ')':
					case ':':
					case '^':
					case '[':
					case ']':
					case '\"':
					case '{':
					case '}':
						// case '~':  '~' '*' and '?' are not encoded thus allowing wild-card search
						// case '*':
						// case '?':
					case '|':
					case '&':
					case ';':
					case '/':
						sb.append('\\');
						sb.append(c);
						break;
					default:
						sb.append(c);
						break;
				}
			}
		}

		return sb.toString();
	}
}
