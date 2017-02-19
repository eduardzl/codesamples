package com.verint.textanalytics.common.logger;

import java.io.Serializable;

import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Appender for sending logs over Http.
 * @author EZlotnik
 *
 */
@Plugin(name = "Http", category = "Core", elementType = "appender", printObject = true)
public class HttpAppender extends AbstractAppender {
	private final HttpResourceManager manager;

	/* BEGIN GENERATED CODE */
	//@formatter:off
	public HttpAppender(String name, Layout layout, Filter filter, boolean ignoreException, HttpResourceManager manager) {
		super(name, filter, layout, ignoreException);

		this.manager = manager;
	}


	/**
	 * function to allocate appender.
	 *
	 * @param name             name of appender
	 * @param httpEndpointUrl  url to request
	 * @param layout           layout
	 * @param filter           filter
	 * @param ignoreExceptions ignore exceptions
	 * @return returns an appender
	 */
	@PluginFactory
	public static HttpAppender createAppender(@PluginAttribute("name") String name, 
											  @PluginAttribute("httpEndpointUrl") String httpEndpointUrl, 
											  @PluginAttribute("dirName") String dirName,
											  @PluginAttribute("batchSize") Integer batchSize,
											  @PluginElement("Layout") Layout<? extends Serializable> layout, 
											  @PluginElement("Filters") Filter filter, 
											  @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
		/* END GENERATED CODE */
		//@formatter:on
		LOGGER.debug("HttpAppender createAppender method invoked by log4j");

		if (name == null) {
			LOGGER.error("No name provided for HttpAppender");
			return null;
		}

		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}

		if (filter == null) {
			filter = ThresholdFilter.createFilter(null, null, null);
		}

		final HttpResourceManager manager = HttpResourceManager.getHttpResourceManager(name, httpEndpointUrl, dirName, batchSize);
		if (manager == null) {
			return null;
		}

		return new HttpAppender(name, layout, filter, ignoreExceptions, manager);
	}

	@Override
	public boolean isFiltered(final LogEvent event) {
		final boolean filtered = super.isFiltered(event);
		if (filtered) {
			manager.sendEvent(getLayout(), event);
		}
		return filtered;
	}

	@Override
	public void append(final LogEvent event) {
		this.manager.sendEvent(getLayout(), event);
	}
}
