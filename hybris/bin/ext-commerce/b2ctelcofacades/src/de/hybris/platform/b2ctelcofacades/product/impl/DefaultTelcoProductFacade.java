/*
* [y] hybris Platform
*
* Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
*
* This software is the confidential and proprietary information of SAP
* ("Confidential Information"). You shall not disclose such Confidential
* Information and shall use it only in accordance with the terms of the
* license agreement you entered into with SAP.
*
*/
package de.hybris.platform.b2ctelcofacades.product.impl;

import de.hybris.platform.b2ctelcofacades.product.TelcoProductFacade;
import de.hybris.platform.b2ctelcoservices.model.AccessoryModel;
import de.hybris.platform.b2ctelcoservices.services.CompatibilityService;
import de.hybris.platform.catalog.enums.ProductReferenceTypeEnum;
import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.product.data.ProductReferenceData;
import de.hybris.platform.configurablebundleservices.bundle.BundleRuleService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of Telco Product Facade {@link TelcoProductFacade}.
 */
public class DefaultTelcoProductFacade implements TelcoProductFacade
{
	private CompatibilityService compatibilityService;

	private BundleRuleService bundleRuleService;

	private ProductFacade productFacade;

	@Override
	public List<ProductData> getProductReferencesAndFeatureCompatibleProductsForCode(final String code,
			final List<ProductReferenceTypeEnum> referenceTypes, final List<ProductOption> options, final Integer limit,
			final ClassAttributeAssignmentModel classAttributeAssignment, final ComposedTypeModel targetItemType)
	{
		final List<ProductData> productData = new ArrayList<ProductData>();
		int currentCount = 0;

		final List<ProductReferenceData> productReferences = getProductFacade().getProductReferencesForCode(code, referenceTypes,
				options, limit);

		currentCount += productReferences.size();
		for (final ProductReferenceData prdata : productReferences)
		{
			productData.add(prdata.getTarget());
		}
		if (limit != null && currentCount < limit.intValue())
		{
			final List<ProductModel> featureCompatibleProducts = compatibilityService.getFeatureCompatibleProducts(code,
					classAttributeAssignment, targetItemType);

			for (final ProductModel product : featureCompatibleProducts)
			{
				if (limit.intValue() <= currentCount)
				{
					break;
				}
				final ProductData tempProduct = getProductFacade().getProductForOptions(product, options);
				if (!contains(productData, tempProduct))
				{
					productData.add(tempProduct);
					currentCount++;
				}
			}
		}
		return productData;

	}

	@Override
	public List<ProductData> getProductReferencesAndFeatureCompatibleAndVendorCompatibleProductsForCode(final String code,
			final List<ProductReferenceTypeEnum> referenceTypes, final List<ProductOption> options, final Integer limit,
			final ClassAttributeAssignmentModel classAttributeAssigment, final ComposedTypeModel targetItemType)
	{
		final List<ProductData> productData = this.getProductReferencesAndFeatureCompatibleProductsForCode(code, referenceTypes,
				options, limit, classAttributeAssigment, targetItemType);

		int currentCount = productData.size();
		if (limit != null && currentCount < limit.intValue())
		{
			final List<ProductModel> vendorCompatibleProducts = compatibilityService.getAccessoriesForVendorCompatibility(code,
					AccessoryModel._TYPECODE);
			for (final ProductModel product : vendorCompatibleProducts)
			{

				if (limit.intValue() <= currentCount)
				{
					break;
				}
				final ProductData tempProduct = getProductFacade().getProductForOptions(product, options);
				if (!contains(productData, tempProduct))
				{
					productData.add(tempProduct);
					currentCount++;
				}
			}
		}
		return productData;
	}

	/**
	 * Check if the productData is present in list.
	 *
	 * @param productData
	 *           list of existing product data items
	 * @param newProduct
	 *           product data item to be added
	 * @return true if newProduct exists in productData
	 */
	protected boolean contains(final List<ProductData> productData, final ProductData newProduct)
	{
		final Object exists = CollectionUtils.find(productData, new Predicate()
		{

			@Override
			public boolean evaluate(final Object productDataObj)
			{
				final ProductData existingProductData = (ProductData) productDataObj;
				return existingProductData.getCode().equals(newProduct.getCode());
			}
		});
		if (exists != null)
		{
			return true;
		}
		return false;
	}

	protected CompatibilityService getCompatibilityService()
	{
		return compatibilityService;
	}

	@Required
	public void setCompatibilityService(final CompatibilityService compatibilityService)
	{

		this.compatibilityService = compatibilityService;
	}

	protected BundleRuleService getBundleRuleService()
	{
		return bundleRuleService;
	}


	@Required
	public void setBundleRuleService(final BundleRuleService bundleRuleService)
	{
		this.bundleRuleService = bundleRuleService;
	}

	/**
	 * @return the productFacade
	 */
	public ProductFacade getProductFacade()
	{
		return productFacade;
	}

	/**
	 * @param productFacade
	 *           the productFacade to set
	 */
	public void setProductFacade(final ProductFacade productFacade)
	{
		this.productFacade = productFacade;
	}

}
