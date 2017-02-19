package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
/* BEGIN GENERATED CODE */
/**
 * @author EZlotnik Describes a document stored in index
 */
@SuppressWarnings("all")
public class UtteranceCIV {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String id;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SpeakerType meta_s_speaker = SpeakerType.unknown;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int speakerId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String text;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String plainText;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String date;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String language;
}
/* END GENERATED CODE */