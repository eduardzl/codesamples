package com.verint.textanalytics.dal.darwin.vtasyntax.errors;

import com.verint.textanalytics.dal.darwin.vtasyntax.errors.AntlrErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by EZlotnik on 3/20/2016.
 */
@AllArgsConstructor
public class VTASyntaxRecognitionError {

	@Getter
	@Setter
	private AntlrErrorType antlrErrorType;

	@Getter
	@Setter
	private String message;

	@Getter
	@Setter
	private int startIndex;

	@Getter
	@Setter
	private int stopIndex;

	@Getter
	@Setter
	private Exception rootCauseException;
}
