package com.verint.textanalytics.common.logger;

import java.nio.charset.Charset;
import java.util.*;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.layout.*;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.verint.textanalytics.common.utils.DataUtils;

/**
 * Copy Pasta version of JsonLayout that uses a different JSON writer which adds
 * required logstash "@version" and "@timestamp" fields to the default
 * serialized form.
 *
 * @see org.apache.logging.log4j.core.layout.JsonLayout
 */
@Plugin(name = "LogglyJSONLayout", category = "Core", elementType = "layout", printObject = true)
public class LogglyJSONLayout extends AbstractJacksonLayout {

	static final String CONTENT_TYPE = "application/json";

	private static final long serialVersionUID = 1L;
	private static final Map<String, String> additionalLogAttributes = new HashMap<String, String>();

	private final String username = System.getProperty("user.name").toLowerCase();

	protected LogglyJSONLayout(final boolean locationInfo, final boolean properties, final boolean complete, final boolean compact, boolean eventEol, final Charset charset,
	        final Map<String, String> additionalLogAttributes) {

		super(getWriter(locationInfo, properties, compact), charset, compact, complete, eventEol);

		this.additionalLogAttributes.putAll(additionalLogAttributes);
	}

	private static ObjectWriter getWriter(boolean locationInfo, boolean properties, boolean compact) {

		final SimpleFilterProvider filters = new SimpleFilterProvider();
		final Set<String> except = new HashSet<String>(2);
		if (!locationInfo) {
			except.add("");
		}

		if (!properties) {
			except.add("");
		}

		filters.addFilter(Log4jLogEvent.class.getName(), SimpleBeanPropertyFilter.serializeAllExcept(except));
		final ObjectMapper mapper = new ObjectMapper();
		if (compact) {
			return mapper.writerWithDefaultPrettyPrinter().with(filters);
		} else {
			return mapper.writer().with(filters);
		}
	}

	/**
	 * Returns appropriate JSON header.
	 *
	 * @return a byte array containing the header, opening the JSON array.
	 */
	@Override
	public byte[] getHeader() {
		if (!this.complete) {
			return null;
		}
		final StringBuilder buf = new StringBuilder();
		buf.append('[');
		buf.append(this.eol);
		return getBytes(buf.toString());
	}

	/**
	 * Returns appropriate JSON footer.
	 *
	 * @return a byte array containing the footer, closing the JSON array.
	 */
	@Override
	public byte[] getFooter() {
		if (!this.complete) {
			return null;
		}
		return getBytes(this.eol + ']' + this.eol);
	}

	@Override
	public Map<String, String> getContentFormat() {
		final Map<String, String> result = new HashMap<String, String>();
		result.put("version", "2.0");
		return result;
	}

	@Override
	/**
	 * @return The content type.
	 */
	public String getContentType() {
		return CONTENT_TYPE + "; charset=" + this.getCharset();
	}

	/**
	 * Creates a JSON Layout.
	 *
	 * @param locationInfo
	 *            If "true", includes the location information in the generated
	 *            JSON.
	 * @param properties
	 *            If "true", includes the thread context in the generated JSON.
	 * @param complete
	 *            If "true", includes the JSON header and footer, defaults to
	 *            "false".
	 * @param compact
	 *            If "true", does not use end-of-lines and indentation, defaults
	 *            to "false".
	 * @param eventEol
	 *            If "true", forces an EOL after each log event (even if compact
	 *            is "true"), defaults to "false". This allows one even per
	 *            line, even in compact mode.
	 * @param charset
	 *            The character set to use, if {@code null}, uses "UTF-8".
	 * @param pairs
	 *            pairs
	 * @return A JSON Layout.
	 */
	@PluginFactory
	public static AbstractJacksonLayout createLayout(
	// @formatter:off
            @PluginAttribute(value = "locationInfo", defaultBoolean = false) final boolean locationInfo,
            @PluginAttribute(value = "properties", defaultBoolean = false) final boolean properties,
            @PluginAttribute(value = "complete", defaultBoolean = false) final boolean complete,
            @PluginAttribute(value = "compact", defaultBoolean = false) final boolean compact,
            @PluginAttribute(value = "eventEol", defaultBoolean = false) final boolean eventEol,
            @PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charset,
            @PluginElement("Pairs") final KeyValuePair[] pairs
            // @formatter:on
	) {

		// Unpacke the pairs list
		final Map<String, String> additionalLogAttributesMap = new HashMap<String, String>();
		if (pairs != null && pairs.length > 0) {
			for (final KeyValuePair pair : pairs) {
				final String key = pair.getKey();
				if (key == null) {
					LOGGER.error("A null key is not valid in MapFilter");
					continue;
				}
				final String value = pair.getValue();
				if (value == null) {
					LOGGER.error("A null value for key " + key + " is not allowed in MapFilter");
					continue;
				}
				if (additionalLogAttributesMap.containsKey(key)) {
					LOGGER.error("Duplicate entry for key: {} is forbidden!", key);
				}
				additionalLogAttributesMap.put(key, value);
			}

		}

		return new LogglyJSONLayout(locationInfo, properties, complete, compact, eventEol, charset, additionalLogAttributesMap);

	}

	/**
	 * Creates a JSON Layout using the default settings.
	 *
	 * @return A JSON Layout.
	 */
	public static AbstractJacksonLayout createDefaultLayout() {
		return new LogglyJSONLayout(false, false, false, false, false, Constants.UTF_8, new HashMap<String, String>());
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
			Map<String, Object> r = new LinkedHashMap<>();

			r.put("timestamp", new DateTime(event.getTimeMillis()).toString());
			r.put("hostname", getHostname());
			r.put("username", username);
			r.put("level", event.getLevel().toString());
			r.put("thread", event.getThreadName());
			r.put("loggerFqcn", event.getLoggerFqcn());
			r.put("loggerName", event.getLoggerName());

			Map<String, String> contextMap = event.getContextMap();
			if (contextMap != null) {
				for (String key : contextMap.keySet()) {
					r.put(key, safeToString(contextMap.get(key)));
				}
			}

			r.put("message", safeToString(event.getMessage().getFormattedMessage()));

			return this.objectWriter.writeValueAsString(r) + eol;

		} catch (final JsonProcessingException e) {
			// Should this be an ISE or IAE?
			LOGGER.error(e);
			return Strings.EMPTY;
		}
	}

	private static String getHostname() {
		String hostname;
		try {
			hostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			hostname = "Unknown, " + e.getMessage();
		}
		return hostname;
	}

	/**
	 * LoggingEvent messages can have any type, and we call toString on them. As
	 * the user can define the toString method, we should catch any exceptions.
	 * @param obj
	 * @return
	 */
	private static String safeToString(Object obj) {
		if (obj == null)
			return null;
		try {
			return obj.toString();
		} catch (Throwable t) {
			return "Error getting message: " + t.getMessage();
		}
	}
}