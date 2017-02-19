package com.verint.textanalytics.common.diagnostics;

/**
 * @author EZlotnik
 */
public enum OperationType {

	// @formatter:off
	SearchInteractionsPage("SearchInteractionsPage"),
	HighlightInSearchInteractions("HighlightsInSearchInteractions_"),
		
	CreateTenant("CreateTenant"),
	DeleteTenant("DeleteTenant"),
	
	FacetedSearch("FacetedSearch"),
	SentimentFacet("SentimentFacet"),

	EntitiesFacet("EntitiesFacet"),
	EntitiesFacetWithStats("EntitiesFacetWithStats"),
	EntitiesFacetLeavesOnlyWithStats("EntitiesFacetLeavesOnlyWithStats"),
	EntitiesChildrenFacetWithStats("EntitiesChildrenFacetWithStats"),
	EntitiesChildrenFacet("EntitiesChildrenFacet"),
	EntitiesChildrenFacetLeavesOnlyWithStats("EntitiesChildrenFacetLeavesOnlyWithStats"),

	RelationsFacet("RelationsFacet"),
	RelationsFacetWithStats("RelationsFacetWithStats"),
	RelationsFacetLeavesOnlyWithStats("RelationsFacetLeavesOnlyWithStats"),
	RelationsChildrenFacetWithStats("RelationsChildrenFacetWithStats"),
	RelationsChildrenFacet("RelationsChildrenFacet"),
	RelationsChildrenFacetLeavesOnlyWithStats("RelationsChildrenFacetLeavesOnlyWithStats"),

	CategoriesFacet("CategoriesFacet"),
	CategoriesFacetWithMetrics("CategoriesFacetWithMetrics"),

	EntitiesTrends("EntitiesTrends"),
	RelationsTrends("RelationsTrends"),
	KeyTermsTrends("KeyTermTrends"),
	CategoriesTrends("CategoriesTrends"),

	EntityInteractionsDailyVolumeSeries("EntityInteractionsDailyVolumeSeries"),
	RelationInteractionsDailyVolumeSeries("RelationInteractionsDailyVolumeSeries"),
	KeyTermInteractionsDailyVolumeSeries("KeyTermInteractionsDailyVolumeSeries"),
	ThemeInteractionsDailyVolumeSeries("ThemeInteractionsDailyVolumeSeries"),
	CategoryInteractionsDailyVolumeSeries("CategoryInteractionsDailyVolumeSeries"),
	UnknownTrendInteractionsDailyVolumeSeries("UnknownTrendInteractionsDailyVolumeSeries"),
	
	CurrentResultSetInteractionsQuantity("CurrentResultSetInteractionsQuantity"),
	InteractionsTotalQuantity("InteractionsTotalQuantity"),
	
	InteractionPreview("InteractionPreview"),
	HighlightInInteractionReview("HighlightsInInteractionReview"),
	InteractionHighlightsForSpeaker("InteractionHighlightsForSpeaker"),
	
	InteractionsDailyVolumeSeries("InteractionsDailyVolumeSeries"),
	TermsAutoCompleteSuggestions("TemsAutoCompleteSuggestions"),
	CurrentResultSetMetrics("CurrentResultSetMetrics"),

	// Entities and Relations Sentiment
	EntitiesSentiment("EntitiesSentiment"),
	RelationsSentiments("RelationsSentiment"),
	EntitiesChildrenSentiment("EntitiesChildrenSentiment"),
	RelationsChildrenSentiment("RelationsChildrenSentiment"),

	CheckSourceTypeForChannel("CheckSourceTypeForChannel"),
	SolrSuggestions("SolrSuggestions"),
	SolrCollectionStatus("SolrCollectionStatus"),

	GetCategoriesFile("GetCategoriesFileFromConfigServer"),
	GetSavedSearchesFile("GetSavedSerchesFileFromConfigServer"),
	GetCategoriesReprocessingStates("GetCategoriesReprocessingStates"),
	WriteCategoriesFile("WriteCategoriesFileToConfigServer"),
	WriteSavedSearchesFile("WriteSavedSerchesFileToConfigServer"),

	GetChannelPropFile("GetChannelFileFromConfigServer"),


	RetriveOntologyModelsTree("rRetriveOntologyModelsTree"),
	InvokeCategoryReprocessing("InvokeCategoryReprocessing"),
	TextElementMetrics("TextElementMetrics"),

	// REST operations
	SearchInteractionsPageRest("SearchInteractionsPageRest"),
	FacetedSearchRest("FacetedSearchRest"),
	SentimentFacetRest("SentimentFacetRest"),

	EntitiesFacetRest("EntitiesFacetRest"),
	EntitiesFacetWithStatsRest("EntitiesFacetWithStatsRest"),
	EntitiesFacetLeavesOnlyWithStatsRest("EntitiesFacetLeavesOnlyWithStatsRest"),
	RelationsFacetRest("RelationsFacetRest"),
	RelationsFacetWithStatsRest("RelationsFacetWithStatsRest"),
	RelationsFacetLeavesOnlyWithStatsRest("RelationsFacetLeavesOnlyWithStatsRest"),
	ThemesFacetWithStatsRest("ThemesFacetWithStatsRest"),
	ThemesFacetLeavesOnlyWithStatsRest("ThemesFacetLeavesOnlyWithStatsRest"),

	CategoriesFacetRest("CategoriesFacetRest"),
	CategoriesFacetWithMetricsRest("CategoriesFacetWithMetricsRest"),

	EntitiesTrendsRest("EntitiesTrendsRest"),
	RelationsTrendsRest("RelationsTrendsRest"),
	KeyTermsTrendsRest("KeyTermTrendsRest"),
	CategoriesTrendsRest("CategoriesTrendsRest"),
	ThemesTrendsRest("ThemesTrendsRest"),

	EntityInteractionsDailyVolumeSeriesRest("EntityInteractionsDailyVolumeSeriesRest"),
	RelationInteractionsDailyVolumeSeriesRest("RelationInteractionsDailyVolumeSeriesRest"),
	KeyTermInteractionsDailyVolumeSeriesRest("KeyTermInteractionsDailyVolumeSeriesRest"),
	ThemeInteractionsDailyVolumeSeriesRest("ThemeInteractionsDailyVolumeSeriesRest"),
	CategoryInteractionsDailyVolumeSeriesRest("CategoryInteractionsDailyVolumeSeriesRest"),
	UnknownTrendInteractionsDailyVolumeSeriesRest("UnknownTrendInteractionsDailyVolumeSeriesRest"),

	CurrentResultSetQuantityRest("CurrentResultSetQuantityRest"),

	InteractionPreviewRest("InteractionPreviewRest"),

	InteractionsDailyVolumeSeriesRest("InteractionsDailyVolumeSeriesRest"),
	TermsAutoCompleteSuggestionsRest("TemsAutoCompleteSuggestionsRest"),
	CurrentResultSetMetricsRest("CurrentResultSetMetricsRest"),
	TextElementMetricsRest("TextElementMetricsRest"),
	CategoryMetricsRest("CategoryMetricsRest");

	// @formatter:on

	private String operationType;

	/**
	 * Constructor.
	 *
	 * @param operationType
	 */
	OperationType(String operationType) {
		this.operationType = operationType;
	}
}
