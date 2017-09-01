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
package de.hybris.platform.xyformsservices.helpers;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.xyformsservices.model.YFormDefinitionModel;

import java.util.Collection;
import java.util.List;


/**
 * Provides methods for managing {@link YFormDefinitionModel}s
 */
public interface YFormDefinitionHelper
{

	/**
	 * Returns {@link YFormDefinitionModel}s that are assigned to the categories and their all supercategories
	 *
	 * @param categories
	 * @return List<YFormDefinitionModel>
	 */
	List<YFormDefinitionModel> getAllYFormDefinitions(final Collection<CategoryModel> categories);

	/**
	 * Returns {@link YFormDefinitionModel}s that are assigned to the category and its all supercategories
	 *
	 * @param category
	 * @return List<YFormDefinitionModel>
	 */
	List<YFormDefinitionModel> getAllYFormDefinitions(final CategoryModel category);
}
