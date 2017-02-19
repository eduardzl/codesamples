package com.verint.textanalytics.dal.modelEditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lombok.val;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verint.textanalytics.common.exceptions.ModelEditorErrorCode;
import com.verint.textanalytics.common.exceptions.ModelEditorException;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.modelEditor.Domain;
import com.verint.textanalytics.model.modelEditor.Language;
import com.verint.textanalytics.model.modelEditor.ModelsTree;

/***
 * 
 * @author imor
 *
 */
public class ConfigServiceResponseConvertor {

	private Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * @param response
	 *            The response that represent the stored searches
	 * @return SavedSearchesRepository
	 */
	public ModelsTree convertModelsTreeResponse(String response) {

		logger.debug("convertModelsTreeResponse - FROM - {}", () -> response);

		val res = new ModelsTree();
		res.setLanguages(new ArrayList<Language>());

		try {

			if (!StringUtils.isNullOrBlank(response)) {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(response);

				res.setLanguages(new ArrayList<Language>());

				Iterator<Entry<String, JsonNode>> nodeIterator = rootNode.fields();
				Language language;
				Domain domain;
				Map.Entry<String, JsonNode> entry;
				Iterator<Entry<String, JsonNode>> languageIterator;

				while (nodeIterator.hasNext()) {

					entry = (Map.Entry<String, JsonNode>) nodeIterator.next();

					language = new Language();
					language.setName(entry.getKey());
					language.setDomains(new ArrayList<Domain>());

					languageIterator = entry.getValue().fields();
					while (languageIterator.hasNext()) {

						entry = (Map.Entry<String, JsonNode>) languageIterator.next();

						domain = new Domain();
						domain.setName(entry.getKey());

						language.getDomains().add(domain);
					}

					res.getLanguages().add(language);
				}
			}

		} catch (Exception ex) {
			throw new ModelEditorException(ex, ModelEditorErrorCode.RetrieveModelsTreeParsingError).put("Response", response);
		}

		logger.debug("convertModelsTreeResponse - TO - {}", () -> JSONUtils.getObjectJSON(res));

		return res;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// Privates Methods 

}