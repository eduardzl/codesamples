package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
/* BEGIN GENERATED CODE */
/**
 * Document as view model.
 *
 * @author EZlotnik
 */
@SuppressWarnings("all")
public class InteractionCIV {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String language;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<UtteranceCIV> utterances;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String meta_dt_interactionStartTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String meta_dt_interactionEndTime;

	/*
	@Getter
	@Setter
	@Accessors(chain = true)
	private String meta_dt_employeeStartTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String meta_dt_customerStartTime;
*/

	@Getter
	@Setter
	@Accessors(chain = true)
	private long meta_l_handleTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int Meta_i_numberOfRobotMessages;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int Meta_i_employeesMessages;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int Meta_l_avgEmployeeResponseTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int Meta_i_customerMessages;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int Meta_l_avgCustomerResponseTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int Meta_i_numberOfAttachments;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Integer> meta_ss_employeeIDs;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> meta_ss_employeesNames;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> meta_ss_employeeTimeZone;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Integer> meta_ss_customerIDs;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> meta_ss_customerNames;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> meta_ss_customerTimeZone;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String subject;

}
/* END GENERATED CODE */