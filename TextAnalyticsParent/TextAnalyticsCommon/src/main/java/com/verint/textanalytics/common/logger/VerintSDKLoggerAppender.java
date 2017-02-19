package com.verint.textanalytics.common.logger;

import java.io.Serializable;

import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Log4net VerintSDK appender.
 * 
 * @author EZlotnik
 *
 */
@Plugin(name = "VerintSDK", category = "Core", elementType = "appender", printObject = true)
public class VerintSDKLoggerAppender extends AbstractAppender {

    private final JNAResourceManager manager;
    
    /* BEGIN GENERATED CODE */
    public VerintSDKLoggerAppender(String name, Layout layout, Filter filter, boolean ignoreException, JNAResourceManager manager) {
        super(name, filter, layout, ignoreException);

        this.manager = manager;
    }
    /* END GENERATED CODE */
    
    /**
     * function to allocate appender.
     * 
     * @param name
     *            name of appender
     * @param nativeResourceName64
     *            name of x86 dll
     * @param nativeResourceName86
     *            name of x64 dll
     * @param layout
     *            layout
     * @param filter
     *            filter
     * @param ignoreExceptions
     *            ignore exceptions
     * @return returns an appender
     */
    @PluginFactory
    public static VerintSDKLoggerAppender createAppender(@PluginAttribute("name") String name,
            @PluginAttribute("nativeResourceName64") String nativeResourceName64,
            @PluginAttribute("nativeResourceName86") String nativeResourceName86,
            @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filters") Filter filter,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {

        LOGGER.debug("VerintSDKLoggerAppender createAppender method invoked by log4j");

        if (name == null) {
            LOGGER.error("No name provided for VerintSDKLoggerAppender");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        if (filter == null) {
            filter = ThresholdFilter.createFilter(null, null, null);
        }

        final JNAResourceManager manager = JNAResourceManager.getJNAManager(name, nativeResourceName64, nativeResourceName86);
        if (manager == null) {
            return null;
        }

        return new VerintSDKLoggerAppender(name, layout, filter, ignoreExceptions, manager);
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
