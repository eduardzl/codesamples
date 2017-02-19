package com.verint.textanalytics.common.utils;

import java.io.IOException;
import java.net.URL;

/**
 * File System utilities.
 * @author EZlotnik
 *
 */
public final class FileUtils {

	private FileUtils() {

	}

	/**
	 * Reads a resource as Text.
	 * @param resourceUrl
	 *            resource url
	 * @return resource as text
	 * @throws IOException
	 *             exception might be thrown
	 */
	public static String getResourceTextData(URL resourceUrl) throws IOException {
		return propel.core.utils.FileUtils.getResourceTextData(resourceUrl);
	}

	/**
	 * Reads file as String.
	 * @param fileAbsolutePath
	 *            file absolute path
	 * @return file content as string
	 * @throws IOException
	 *             IO exception
	 */
	public static String readFileToEnd(String fileAbsolutePath) throws IOException {
		return propel.core.utils.FileUtils.readFileToEnd(fileAbsolutePath);
	}
}
