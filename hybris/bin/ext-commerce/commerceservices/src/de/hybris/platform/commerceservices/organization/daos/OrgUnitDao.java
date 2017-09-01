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
package de.hybris.platform.commerceservices.organization.daos;

import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.search.dao.PagedGenericDao;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.QuoteState;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.EmployeeModel;

import java.util.Set;


/**
 * Interface for {@link OrgUnitModel} related paged data access.
 */
public interface OrgUnitDao<T extends OrgUnitModel> extends PagedGenericDao<T>
{
	/**
	 * Find all unit members of a given type.
	 *
	 * @param unit
	 *           the unit to find all members for
	 * @param memberType
	 *           the model type of the members to find
	 * @param pageableData
	 *           paging information
	 * @return paged search results
	 */
	<U extends PrincipalModel> SearchPageData<U> findMembersOfType(OrgUnitModel unit, Class<U> memberType,
			PageableData pageableData);

	/**
	 * Returns all {@link QuoteModel} instances associated with at least one of the given employee's units.
	 *
	 * @param employee
	 *           an employee who is a member of an organizational unit
	 * @param pageableData
	 *           paging information
	 * @return {@link SearchPageData} containing the paged search result
	 */
	SearchPageData<QuoteModel> findQuotesForEmployee(EmployeeModel employee, Set<QuoteState> states, PageableData pageableData);
}
