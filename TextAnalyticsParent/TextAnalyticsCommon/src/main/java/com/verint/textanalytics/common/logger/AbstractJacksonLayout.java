package com.verint.textanalytics.common.logger;

import java.nio.charset.Charset;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.util.Strings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Jackson layout.
 * @author EZlotnik
 *
 */
abstract class AbstractJacksonLayout extends AbstractStringLayout {

	protected static final String DEFAULT_EOL = "\r\n";
	protected static final String COMPACT_EOL = Strings.EMPTY;
	private static final long serialVersionUID = 1L;

	protected final String eol;
	protected final ObjectWriter objectWriter;
	protected final boolean compact;
	protected final boolean complete;

	protected AbstractJacksonLayout(final ObjectWriter objectWriter, final Charset charset, final boolean compact, final boolean complete, final boolean eventEol) {
		super(charset);
		this.objectWriter = objectWriter;
		this.compact = compact;
		this.complete = complete;
		this.eol = compact && !eventEol ? COMPACT_EOL : DEFAULT_EOL;
	}

	/**
	 * Formats a {@link org.apache.logging.log4j.core.LogEvent}.
	 * 
	 * @param event
	 *            The LogEvent.
	 * @return The XML representation of the LogEvent.
	 */
	@Override
	public String toSerializable(final LogEvent event) {
		try {
			return this.objectWriter.writeValueAsString(event) + eol;
		} catch (final JsonProcessingException e) {
			// Should this be an ISE or IAE?
			LOGGER.error(e);
			return Strings.EMPTY;
		}
	}

}
