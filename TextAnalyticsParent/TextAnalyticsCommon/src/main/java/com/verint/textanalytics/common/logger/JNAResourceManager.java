package com.verint.textanalytics.common.logger;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * JNA resource manager for log4net Verint logger appender.
 * 
 * @author EZlotnik
 *
 */
public class JNAResourceManager extends AbstractManager {
    private static final JNAManagerFactory FACTORY = new JNAManagerFactory();

    private IWriteLogMessage nativeLibraryProxy;

    /**
     * Constructor.
     * 
     * @param name
     *            - resource name
     * @param nativeLibraryProxy
     *            nativeLibraryProxy
     */
    public JNAResourceManager(String name, IWriteLogMessage nativeLibraryProxy) {
        super(name);

        this.nativeLibraryProxy = nativeLibraryProxy;
    }
    
    /**
     * static method to get instance.
     * 
     * @param name
     *            name
     * @param nativeResourceName64
     *            - ddl name for x64
     * @param nativeResouceName86
     *            - dll name for x86
     * @return resource manager
     */
    public static JNAResourceManager getJNAManager(String name, String nativeResourceName64, String nativeResouceName86) {
        LOGGER.debug("Allocating JNAResourceManager for VerintSDKLoggerAppender");

        return getManager(name, FACTORY, new FactoryData(nativeResourceName64, nativeResouceName86));
    }

    

    /** Sends event.
     * @param layout  layout
     * @param event event.
     */
    public void sendEvent(final Layout<?> layout, final LogEvent event) {
        byte[] eventBytes = layout.toByteArray(event);
        this.nativeLibraryProxy.WriteLogMessage(new String(eventBytes, StandardCharsets.UTF_8));
    }

    
    /** Interface of JNA.
     * @author EZlotnik
     *
     */
    
    private interface IWriteLogMessage extends Library {
        /* BEGIN GENERATED CODE */
        void WriteLogMessage(String message);
        /* END GENERATED CODE */
    }

    /** JNA internal class.
     * @author EZlotnik
     *
     */
    private static class FactoryData {
        private String nativeLibraryName64;
        private String nativeLibraryName86;

        public FactoryData(String nativeLibraryName64, String nativeLibraryName86) {
            this.nativeLibraryName64 = nativeLibraryName64;
            this.nativeLibraryName86 = nativeLibraryName86;
        }
    }

    /** JNAManagerFactory class.
     * @author EZlotnik
     *
     */
    private static class JNAManagerFactory implements ManagerFactory<JNAResourceManager, FactoryData> {
        @Override
        public JNAResourceManager createManager(final String name, final FactoryData data) {

            String nativeLibraryName = "";
            IWriteLogMessage jnaLibraryProxy = null;

            try {

                if (Platform.is64Bit()) {
                    nativeLibraryName = data.nativeLibraryName64;
                } else {
                    nativeLibraryName = data.nativeLibraryName86;
                }

                jnaLibraryProxy = (IWriteLogMessage) Native.loadLibrary(nativeLibraryName, IWriteLogMessage.class);

            } catch (final Exception e) {
                LOGGER.error(
                        String.format("Could not create JNA proxy for native library %s for VerintSDKLoggerAppender ", nativeLibraryName),
                        e);
            }

            return new JNAResourceManager(name, jnaLibraryProxy);
        }
    }

}
