package com.verint.textanalytics.common.security;

import static com.verint.textanalytics.common.constants.TAConstants.Environment.IMPACT360_DATA_DIR;
import static com.verint.textanalytics.common.constants.TAConstants.Environment.IMPACT360_SOFTWARE_DIR;

import java.nio.file.Paths;
import java.util.Map;

import lombok.Getter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.verint.textanalytics.common.configuration.FilePathType;
import com.verint.textanalytics.common.utils.ExceptionUtils;

/**
 * @author imor
 */
public class ModelEditorSecretFile {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Getter
	private String path;

	@Getter
	private FilePathType pathType;

	/**
	 * ModelEditorSecretFile constructor.
	 * 
	 * @param path
	 *            Path to the file
	 * @param pathType
	 *            File Path type: AbsolutePath, SoftwareDirRelativePath,
	 *            DataDirRelativePath, ClassPath
	 */
	public ModelEditorSecretFile(String path, FilePathType pathType) {
		this.path = path;
		this.pathType = pathType;
	}

	@Override
	public String toString() {
		return String.format("{FilePathType = %s, FilePath = %s}", this.pathType.toString(), this.path);
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
						fileFullPath = Paths.get(env.get(IMPACT360_SOFTWARE_DIR), this.path)
						                    .toString();
						break;

					case DataDirRelativePath:
						fileFullPath = Paths.get(env.get(IMPACT360_DATA_DIR), this.path)
						                    .toString();
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
