package com.verint.textanalytics.model.facets;

/**
 * Statistic to calculate for single field.
 * @author EZlotnik
 *
 */
public class InteractionLevelFacetQueryStat extends FacetQueryStat {

	/**
	 * @param alias
	 *            alias
	 * @param field
	 *            field
	 * @param function
	 *            function
	 */
	public InteractionLevelFacetQueryStat(String alias, String field, String function) {
		super(alias, field, function);
	}

	@Override
	public String toString() {

		String nl = System.getProperty("line.separator");
		String tab = "\t";
		String tab2 = "\t\t";

		StringBuilder res = new StringBuilder();

		res.append("InteractionLevel_");
		res.append(this.getField());
		res.append(" : {");
		res.append(nl);
		res.append(tab);
		res.append("\"type\" : \"terms\",");
		res.append(nl);
		res.append(tab);
		res.append("\"field\" : \"content_type\",");
		res.append(nl);
		res.append(tab);
		res.append("\"domain\" : {");
		res.append(nl);
		res.append(tab2);
		res.append("\"blockParent\" : \"content_type: PARENT\"");
		res.append(nl);
		res.append(tab);
		res.append("},");
		res.append(nl);
		res.append(tab);
		res.append("\"facet\" : {");
		res.append(tab2);
		res.append(String.format("%s : \"%s(%s)\"", this.getAlias(), this.getFunction(), this.getField()));
		res.append(tab);
		res.append("},");
		res.append(nl);

		return res.toString();
	}
}
