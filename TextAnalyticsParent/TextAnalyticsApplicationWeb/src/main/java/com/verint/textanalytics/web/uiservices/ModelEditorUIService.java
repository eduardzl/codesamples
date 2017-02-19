package com.verint.textanalytics.web.uiservices;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.verint.textanalytics.bl.applicationservices.ModelEditorService;

/**
 * Model Editor UI Service.
 * 
 * @author imor
 *
 */
public class ModelEditorUIService extends BaseUIService {

	@Autowired
	private ViewModelConverter viewModelConverter;

	@Autowired
	private ModelEditorService modelEditorService;

	/**
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param tenantID
	 *            tenantID
	 * @return categories retrieve Models Tree
	 */

	public com.verint.textanalytics.web.viewmodel.ModelsTree retrieveModelsTree(String i360FoundationToken, String tenantID) {

		return viewModelConverter.modelsTreeConverter(modelEditorService.retrieveModelsTree(tenantID));
	}

	/**
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @return Entry<String, String>
	 */
	public Entry<String, String> getModelEditorHeaderToken(String i360FoundationToken) {

		return modelEditorService.getModelEditorHeaderToken(i360FoundationToken);
	}

}
