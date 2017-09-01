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
package de.hybris.platform.chinesepspalipayservices.dao;

import de.hybris.platform.core.model.order.OrderModel;


/**
 * Provide method to find order model with given params
 */
public interface AlipayOrderDao
{


	/**
	 * Get OrderModel by code
	 *
	 * @param code
	 *           The order code of the wanted order
	 * @return OrderModel if found and null otherwise
	 */
	OrderModel findOrderByCode(final String code);
}
