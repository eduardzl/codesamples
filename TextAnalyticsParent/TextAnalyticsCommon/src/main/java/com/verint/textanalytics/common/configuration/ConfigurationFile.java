package com.verint.textanalytics.common.configuration;

import static com.verint.textanalytics.common.constants.TAConstants.Environment.*;

import java.nio.file.Paths;
import java.util.Map;

import lombok.Getter;

import org.apache.logging.log4j.*;

import com.verint.textanalytics.common.utils.ExceptionUtils;

/**
 * Represent a Configuration File.
 * 
 * @author imor
 *
 */
public class ConfigurationFile {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Getter
	private String path;

	@Getter
	private FilePathType pathType;

	@Getter
	private ConfigurationSource configurationSource;

	@Getter
	private boolean isMandatory;

	/**
	 * ConfigurationFile constructor.
	 * 
	 * @param path
	 *            Path to the file
	 * @param pathType
	 *            File Path type: AbsolutePath, SoftwareDirRelativePath,
	 *            DataDirRelativePath, ClassPath
	 * @param configurationSource
	 *            Configuration Source type: PropertiesConfiguration,
	 *            XMLConfiguration
	 * @param isMandatory
	 *            true if the Configuration file is mandatory
	 */
	public ConfigurationFile(String path, FilePathType pathType, ConfigurationSource configurationSource, boolean isMandatory) {
		this.path = path;
		this.pathType = pathType;
		this.configurationSource = configurationSource;
		this.isMandatory = isMandatory;
	}

	@Override
	public String toString() {
		return String.format("[File Path Type = %s], [Configuration Source = %s], [File Path = %s]", this.pathType.toString(), this.configurationSource.toString(), this.path);
	}

	/**
	 * @return The file full path
	 */
	public String resolveFullPath() {
		String fileFullPath = "";

		if (this.path == null || this.path.isEmpty()) {
			fileFullPath = "";
		} else {
			try {
				Map<String, String> env = System.getenv();

				switch (this.pathType) {

					case ClassPath:
						fileFullPath = this.path;
						break;

					case AbsolutePath:
						fileFullPath = this.path;
						break;

					case SoftwareDirRelativePath:
						fileFullPath = Paths.get(env.get(IMPACT360_SOFTWARE_DIR), this.path).toString();
						break;

					case DataDirRelativePath:
						fileFullPath = Paths.get(env.get(IMPACT360_DATA_DIR), this.path).toString();
						break;
					default:
						fileFullPath = this.path;
						break;
				}
			} catch (Exception ex) {
				logger.error("Fail on resolve Full Path for Configuration File. ConfigurationFile = [{}]. Error - {}", this.toString(), ExceptionUtils.getStackTrace(ex));
			}
		}

		logger.debug("Resolve Full Path for Configuration File finish successfully. ConfigurationFile = [{}], Full Path=[{}]", this.toString(), fileFullPath);

		return fileFullPath;
	}
}
