/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.commerceservices.search.solrfacetsearch.populators;

import de.hybris.platform.commerceservices.model.solrsearch.config.SolrSortFieldModel;
import de.hybris.platform.commerceservices.search.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SearchQueryPageableData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchRequest;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.OrderField.SortOrder;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;


/**
 */
public class SearchSortPopulator<FACET_SEARCH_CONFIG_TYPE, INDEXED_PROPERTY_TYPE> implements
		Populator<SearchQueryPageableData<SolrSearchQueryData>, SolrSearchRequest<FACET_SEARCH_CONFIG_TYPE, IndexedType, INDEXED_PROPERTY_TYPE, SearchQuery, IndexedTypeSort>>
{
	private static final Logger LOG = Logger.getLogger(SearchSortPopulator.class);
	protected static final String SOLR_SCORE_FIELD = "score";

	@Override
	public void populate(final SearchQueryPageableData<SolrSearchQueryData> source,
			final SolrSearchRequest<FACET_SEARCH_CONFIG_TYPE, IndexedType, INDEXED_PROPERTY_TYPE, SearchQuery, IndexedTypeSort> target)
	{
		// Work out the selected sort option

		final IndexedType commerceIndexedType = target.getIndexedType();

		// Try to get the sort from the pageableData
		if (target.getPageableData() != null && target.getPageableData().getSort() != null
				&& !target.getPageableData().getSort().isEmpty())
		{
			target.setCurrentSort(commerceIndexedType.getSortsByCode().get(target.getPageableData().getSort()));
		}

		// Fall-back to the last sort used in the searchQueryData
		if (target.getCurrentSort() == null && target.getSearchQueryData().getSort() != null
				&& !target.getSearchQueryData().getSort().isEmpty())
		{
			target.setCurrentSort(commerceIndexedType.getSortsByCode().get(target.getSearchQueryData().getSort()));
		}


		if (target.getCurrentSort() == null)
		{
			// Fallback to first available sort

			final List<IndexedTypeSort> sorts = getFilteredSorts(target.getIndexedType());
			if (sorts != null && !sorts.isEmpty())
			{
				target.setCurrentSort(sorts.get(0));
			}

		}

		if (target.getCurrentSort() != null)
		{
			boolean scoreSort = false;

			// Set the sort options on the query
			for (final SolrSortFieldModel sortFieldModel : target.getCurrentSort().getSort().getFields())
			{
				if (SOLR_SCORE_FIELD.equalsIgnoreCase(sortFieldModel.getFieldName()))
				{
					scoreSort = true;
				}
				target.getSearchQuery().addSort(sortFieldModel.getFieldName(),
						sortFieldModel.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING);
			}

			// If we are not already sorting by the relevance score then add it as a final ordering clause
			if (!scoreSort)
			{
				target.getSearchQuery().addSort(SOLR_SCORE_FIELD, SortOrder.DESCENDING);
			}
		}
	}

	/*
	 * Returns a filtered list of sorts depending on the current query and indexed type.
	 */
	protected List<IndexedTypeSort> getFilteredSorts(final IndexedType indexedType)
	{
		if (indexedType != null)
		{
			final List<IndexedTypeSort> sorts = indexedType.getSorts();
			if (sorts != null && !sorts.isEmpty())
			{
				final List<IndexedTypeSort> existingSorts = new ArrayList<>(sorts);

				for (final IndexedTypeSort sort : sorts)
				{
					filterIndexedSorts(indexedType, existingSorts, sort);
				}
				return existingSorts;
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Filters indexed sorts.
	 *
	 * @param indexedType
	 * @param existingSorts
	 * @param sort
	 */
	protected void filterIndexedSorts(final IndexedType indexedType, final List<IndexedTypeSort> existingSorts,
			final IndexedTypeSort sort)
	{
		final List<SolrSortFieldModel> sortFields = sort.getSort().getFields();
		if (sortFields != null && !sortFields.isEmpty())
		{
			for (final SolrSortFieldModel sortField : sortFields)
			{
				if (!sortFieldExists(sortField.getFieldName(), indexedType))
				{
					existingSorts.remove(sort);
				}
			}
		}
	}


	/**
	 * Method checks if the field with the given <code>sortFieldName</code> exists in the index configuration of the
	 * <code>indexedType</code>.
	 * 
	 * @param sortFieldName
	 * @param indexedType
	 * @return true if exists
	 */
	protected boolean sortFieldExists(final String sortFieldName, final IndexedType indexedType)
	{
		// Special case for 'score' field which is always available as a sort field
		if (SOLR_SCORE_FIELD.equalsIgnoreCase(sortFieldName))
		{
			return true;
		}

		if (indexedType != null)
		{
			if (indexedType.getIndexedProperties().get(sortFieldName) != null)
			{
				return true;
			}
			else
			{
				LOG.warn("Cannot use [" + sortFieldName
						+ "] as sort field as it doesn't exist in the solr index. Please review you SolrIndexType ["
						+ indexedType.getCode() + "] instance");
			}
		}
		return false;
	}
}
