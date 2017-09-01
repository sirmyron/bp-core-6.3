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
package de.hybris.platform.chineselogisticfacades.delivery.populator;


import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.chineselogisticfacades.delivery.data.DeliveryTimeSlotData;
import de.hybris.platform.chineselogisticservices.model.DeliveryTimeSlotModel;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.CartModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


@UnitTest
public class CartDeliveryTimeSlotPopulatorTest
{
	private static final String TIME_SLOT_CODE = "0001";
	private static final String TIME_SLOT_NAME = "testname";

	@Mock
	private CartModel source;
	@Mock
	private DeliveryTimeSlotModel deliveryTimeSlot;

	private CartDeliveryTimeSlotPopulator populator;
	private CartData target;

	@Before
	public void prepare()
	{
		MockitoAnnotations.initMocks(this);

		populator = new CartDeliveryTimeSlotPopulator();
		target = new CartData();

		BDDMockito.given(source.getDeliveryTimeSlot()).willReturn(deliveryTimeSlot);
		BDDMockito.given(deliveryTimeSlot.getCode()).willReturn(TIME_SLOT_CODE);
		BDDMockito.given(deliveryTimeSlot.getName()).willReturn(TIME_SLOT_NAME);
	}

	@Test
	public void testPopulate()
	{
		populator.populate(source, target);
		final DeliveryTimeSlotData deliveryTimeSlotData = target.getDeliveryTimeSlot();
		Assert.assertNotNull(deliveryTimeSlotData);
		Assert.assertEquals(TIME_SLOT_CODE, deliveryTimeSlotData.getCode());
		Assert.assertEquals(TIME_SLOT_NAME, deliveryTimeSlotData.getName());
	}
}
