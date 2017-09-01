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

import de.hybris.platform.commerceservices.search.facetdata.ProductCategorySearchPageData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchResponse;
import de.hybris.platform.converters.Populator;

/**
 */
public class SearchResponseCategoryCodePopulator<STATE, ITEM, CATEGORY> implements Populator<SolrSearchResponse, ProductCategorySearchPageData<STATE, ITEM, CATEGORY>>
{
	@Override
	public void populate(final SolrSearchResponse source, final ProductCategorySearchPageData<STATE, ITEM, CATEGORY> target)
	{
		target.setCategoryCode(source.getRequest().getSearchQueryData().getCategoryCode());
	}
}
