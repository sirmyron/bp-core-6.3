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

package de.hybris.platform.financialfacades.strategies;

import de.hybris.platform.commercefacades.insurance.data.InsurancePolicyListingData;
import de.hybris.platform.core.model.user.UserModel;

import java.util.List;


public interface CustomerInsurancePolicyStrategy
{
	List<InsurancePolicyListingData> getPolicyDataForUID(String uid);

	List<InsurancePolicyListingData> getPolicyDataForUser(UserModel user);

	List<InsurancePolicyListingData> getPolicyDataForCurrentCustomer();

}
