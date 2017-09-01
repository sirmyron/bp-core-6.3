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
package de.hybris.platform.b2ctelcofacades.converters.populator;

import de.hybris.platform.b2ctelcofacades.data.BundleTabData;
import de.hybris.platform.b2ctelcofacades.data.FrequencyTabData;
import de.hybris.platform.b2ctelcofacades.product.FrequencyTabDataComparator;
import de.hybris.platform.b2ctelcoservices.model.DeviceModel;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.converters.populator.AbstractProductPopulator;
import de.hybris.platform.commercefacades.product.converters.populator.ProductClassificationPopulator;
import de.hybris.platform.commercefacades.product.converters.populator.ProductDescriptionPopulator;
import de.hybris.platform.commercefacades.product.converters.populator.ProductPricePopulator;
import de.hybris.platform.commercefacades.product.converters.populator.ProductStockPopulator;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.configurablebundlefacades.data.BundleTemplateData;
import de.hybris.platform.configurablebundleservices.bundle.BundleRuleService;
import de.hybris.platform.configurablebundleservices.bundle.BundleTemplateService;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.entitlementfacades.product.converters.populator.ProductEntitlementCollectionPopulator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.subscriptionfacades.data.TermOfServiceFrequencyData;
import de.hybris.platform.subscriptionfacades.product.converters.populator.SubscriptionProductBasicPopulator;
import de.hybris.platform.subscriptionfacades.product.converters.populator.SubscriptionProductPricePlanPopulator;
import de.hybris.platform.subscriptionservices.enums.TermOfServiceFrequency;
import de.hybris.platform.subscriptionservices.price.SubscriptionCommercePriceService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hybris.platform.subscriptionservices.subscription.SubscriptionProductService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates the {@link ProductData} with the device and plan for guided selling bundle information to be used by front
 * end.
 *
 * @param <SOURCEPRODUCT> source class
 * @param <TARGETPRODUCT> target class
 * @param <SOURCETEMPLATE> bundle template
 */
public abstract class
		AbstractProductBundleTabsPopulator<SOURCEPRODUCT extends ProductModel,
		TARGETPRODUCT extends ProductData, SOURCETEMPLATE extends BundleTemplateModel>
		extends AbstractProductPopulator<SOURCEPRODUCT, TARGETPRODUCT>
{

	private static final Logger LOG = Logger.getLogger(AbstractProductBundleTabsPopulator.class);

	private CommonI18NService commonI18NService;
	private BundleTemplateService bundleTemplateService;
	private BundleRuleService bundleRuleService;
	private Converter<BundleTemplateModel, BundleTemplateData> bundleTemplateConverter;
	private Converter<ProductModel, ProductData> productConverter;
	private PriceDataFactory priceDataFactory;
	private ProductPricePopulator<ProductModel, ProductData> productPricePopulator;
	private Converter<TermOfServiceFrequency, TermOfServiceFrequencyData> termOfServiceFrequencyConverter;
	private ProductDescriptionPopulator<ProductModel, ProductData> productDescriptionPopulator;
	private ProductClassificationPopulator<ProductModel, ProductData> productClassificationPopulator;
	private SubscriptionProductBasicPopulator<ProductModel, ProductData> subscriptionProductBasicPopulator;
	private ProductEntitlementCollectionPopulator<ProductModel, ProductData> subscriptionProductEntitlementPopulator;
	private SubscriptionCommercePriceService commercePriceService;
	private SubscriptionProductPricePlanPopulator<ProductModel, ProductData> subscriptionProductPricePopulator;
	private ProductStockPopulator<ProductModel, ProductData> productStockPopulator;
	private SubscriptionProductService subscriptionProductService;

	@Override
	public void populate(final SOURCEPRODUCT productModel, final TARGETPRODUCT productData) throws ConversionException
	{
		// iterate over all components, which represent the package tabs in the frontend
		final Map<String, BundleTabData> bundleTabsMap = new HashMap<String, BundleTabData>();
		for (final SOURCETEMPLATE sourceComponent : getComponents(productModel))
		{
			final SOURCETEMPLATE parentBundleTemplate = (SOURCETEMPLATE) sourceComponent.getParentTemplate();

			final SOURCETEMPLATE targetComponent = getTargetComponent(sourceComponent);

			final BundleTabData bundleTabData;
			if (bundleTabsMap.containsKey(parentBundleTemplate.getId()))
			{
				bundleTabData = bundleTabsMap.get(parentBundleTemplate.getId());
			}
			else
			{
				bundleTabData = new BundleTabData();
				bundleTabsMap.put(parentBundleTemplate.getId(), bundleTabData);
			}

			bundleTabData.setParentBundleTemplate(getBundleTemplateConverter().convert(parentBundleTemplate));
			bundleTabData.setSourceComponent(getBundleTemplateConverter().convert(sourceComponent));
			bundleTabData.setTargetComponent(getBundleTemplateConverter().convert(targetComponent));

			final Map<String, FrequencyTabData> frequencyTabsMap = getStringFrequencyTabDataMap(bundleTabData);

			// iterate over all products per bundle tab
			for (final ProductModel targetProductModel : getProducts(productModel, sourceComponent, targetComponent))
			{
				if (!getSubscriptionProductService().isSubscription(targetProductModel))
				{
					LOG.error("Product '" + targetProductModel.getCode() + "' is not a subscription product. Ignoring it.");
					continue;
				}
				final TermOfServiceFrequencyData termOfServiceFrequency = getTermOfServiceFrequencyConverter().convert(
								 targetProductModel.getSubscriptionTerm().getTermOfServiceFrequency());

				final int termOfServiceNumber = targetProductModel.getSubscriptionTerm().getTermOfServiceNumber() == null ? 0
						: targetProductModel.getSubscriptionTerm().getTermOfServiceNumber().intValue();

				// The list of Plans is split by its terms and conditions number and frequency,
				// which lead to the frequency tabs in the frontend
				final FrequencyTabData frequencyTab;
				final String frequencyString = termOfServiceFrequency.getCode() + ":" + termOfServiceNumber;

				if (frequencyTabsMap.containsKey(frequencyString))
				{
					frequencyTab = frequencyTabsMap.get(frequencyString);
				}
				else
				{
					frequencyTab = buildFrequencyTab(termOfServiceFrequency, termOfServiceNumber);
					frequencyTabsMap.put(frequencyString, frequencyTab);
				}

				// the related product is populated with specific information
				final ProductData subscriptionProductData = getProductConverter().convert(targetProductModel);

				getSubscriptionProductBasicPopulator().populate(targetProductModel, subscriptionProductData);
				getSubscriptionProductEntitlementPopulator().populate(targetProductModel, subscriptionProductData);
				getProductStockPopulator().populate(targetProductModel, subscriptionProductData);

				callPopulators(sourceComponent, targetComponent, productModel, productData, targetProductModel,
						subscriptionProductData);

				frequencyTab.getProducts().add(subscriptionProductData);
			}

			final List<FrequencyTabData> sortedFrequencies = new ArrayList<FrequencyTabData>(frequencyTabsMap.values());
			Collections.sort(sortedFrequencies, new FrequencyTabDataComparator());
			Collections.reverse(sortedFrequencies);
			bundleTabData.setFrequencyTabs(sortedFrequencies);

		}
		final List<BundleTabData> bundleTabs = new ArrayList<BundleTabData>(bundleTabsMap.values());

		if (productModel instanceof DeviceModel)
		{
			productData.setBundleTabs(bundleTabs);
		}
		else if (getSubscriptionProductService().isSubscription(productModel))
		{
			productData.setBundleTabs(bundleTabs);
		}

		// populate the information about pre-selected tabs
		changePreselectedFlags(bundleTabs, productModel.getCode());
	}

	protected Map<String, FrequencyTabData> getStringFrequencyTabDataMap(final BundleTabData bundleTabData)
	{
		final Map<String, FrequencyTabData> frequencyTabsMap = new HashMap<>();
		final List<FrequencyTabData> frequencyTabList = bundleTabData.getFrequencyTabs();
		if (CollectionUtils.isNotEmpty(frequencyTabList))
		{
			for (final FrequencyTabData frequencyTabData : frequencyTabList)
			{
				frequencyTabsMap.put(
						frequencyTabData.getTermOfServiceFrequency().getCode() + ":" + frequencyTabData.getTermOfServiceNumber(),
						frequencyTabData);
			}
		}
		return frequencyTabsMap;
	}

	/**
	 * Resolves the components to be used for populating the bundle tabs.
	 *
	 * @param productModel
	 *           the {@link ProductModel} for which the components should be searched for
	 * @return a list of component {@link BundleTemplateModel}s which are used to populating the bundle tabs
	 */
	protected abstract Collection<SOURCETEMPLATE> getComponents(SOURCEPRODUCT productModel);

	/**
	 * Resolves the target component {@link BundleTemplateModel} for a source component {@link BundleTemplateModel}.
	 *
	 * @param sourceComponent
	 *           The source component {@link BundleTemplateModel} for which the target component should be found.
	 * @return the target component {@link BundleTemplateModel}
	 */
	protected abstract SOURCETEMPLATE getTargetComponent(final SOURCETEMPLATE sourceComponent);

	/**
	 * Resolves the list of {@link ProductModel}s for the given <code>productModel</code>.
	 *
	 * @param productModel
	 *           the {@link ProductModel} for which the list of {@link ProductModel}s should be found
	 * @param sourceComponent
	 *           the source component {@link BundleTemplateModel} for which the list of {@link ProductModel}s should be
	 *           found
	 * @param targetComponent
	 *           the target component {@link BundleTemplateModel} for which the list of {@link ProductModel}s should be
	 *           found
	 * @return the list of {@link ProductModel}s matching the parameters
	 */
	protected abstract List<ProductModel> getProducts(final SOURCEPRODUCT productModel, final SOURCETEMPLATE sourceComponent,
			final SOURCETEMPLATE targetComponent);

	/**
	 * Hook to call additional populators for different purposes.
	 */
	protected abstract void callPopulators(final SOURCETEMPLATE sourceComponent, final SOURCETEMPLATE targetComponent,
			final SOURCEPRODUCT productModel, final TARGETPRODUCT productData,
			final ProductModel subscriptionProductModel, final ProductData subscriptionProductData);

	protected FrequencyTabData buildFrequencyTab(final TermOfServiceFrequencyData termOfServiceFrequency,
			final int termOfServiceNumber)
	{
		FrequencyTabData frequencyTab;
		frequencyTab = new FrequencyTabData();
		frequencyTab.setTermOfServiceFrequency(termOfServiceFrequency);
		frequencyTab.setTermOfServiceNumber(termOfServiceNumber);
		frequencyTab.setProducts(new ArrayList<ProductData>());
		return frequencyTab;
	}



	protected boolean changePreselectedFlags(final List<BundleTabData> bundleTabs, final String productCode)
	{
		boolean found = false;
		for (final BundleTabData bundleTab : bundleTabs)
		{
			for (final FrequencyTabData frequencyTab : bundleTab.getFrequencyTabs())
			{
				for (final ProductData product : frequencyTab.getProducts())
				{
					if (productCode.equals(product.getCode()))
					{
						if (!found)
						{
							bundleTab.setPreselected(true);
						}
						frequencyTab.setPreselected(true);
						product.setPreselected(true);
						found = true;
					}
				}
			}
		}

		return found;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	protected BundleTemplateService getBundleTemplateService()
	{
		return bundleTemplateService;
	}

	@Required
	public void setBundleTemplateService(final BundleTemplateService bundleTemplateService)
	{
		this.bundleTemplateService = bundleTemplateService;
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

	protected Converter<ProductModel, ProductData> getProductConverter()
	{
		return productConverter;
	}

	@Required
	public void setProductConverter(final Converter<ProductModel, ProductData> productConverter)
	{
		this.productConverter = productConverter;
	}

	protected PriceDataFactory getPriceDataFactory()
	{
		return priceDataFactory;
	}

	@Required
	public void setPriceDataFactory(final PriceDataFactory priceDataFactory)
	{
		this.priceDataFactory = priceDataFactory;
	}

	protected ProductPricePopulator<ProductModel, ProductData> getProductPricePopulator()
	{
		return productPricePopulator;
	}

	@Required
	public void setProductPricePopulator(final ProductPricePopulator<ProductModel, ProductData> productPricePopulator)
	{
		this.productPricePopulator = productPricePopulator;
	}

	protected Converter<TermOfServiceFrequency, TermOfServiceFrequencyData> getTermOfServiceFrequencyConverter()
	{
		return termOfServiceFrequencyConverter;
	}

	@Required
	public void setTermOfServiceFrequencyConverter(
			final Converter<TermOfServiceFrequency, TermOfServiceFrequencyData> termOfServiceFrequencyConverter)
	{
		this.termOfServiceFrequencyConverter = termOfServiceFrequencyConverter;
	}

	@Required
	public void setProductDescriptionPopulator(
			final ProductDescriptionPopulator<ProductModel, ProductData> productDescriptionPopulator)
	{
		this.productDescriptionPopulator = productDescriptionPopulator;
	}

	protected ProductDescriptionPopulator<ProductModel, ProductData> getProductDescriptionPopulator()
	{
		return productDescriptionPopulator;
	}

	@Required
	public void setProductClassificationPopulator(
			final ProductClassificationPopulator<ProductModel, ProductData> productClassificationPopulator)
	{
		this.productClassificationPopulator = productClassificationPopulator;
	}

	protected ProductClassificationPopulator<ProductModel, ProductData> getProductClassificationPopulator()
	{
		return productClassificationPopulator;
	}

	protected Converter<BundleTemplateModel, BundleTemplateData> getBundleTemplateConverter()
	{
		return bundleTemplateConverter;
	}

	@Required
	public void setBundleTemplateConverter(final Converter<BundleTemplateModel, BundleTemplateData> bundleTemplateConverter)
	{
		this.bundleTemplateConverter = bundleTemplateConverter;
	}

	protected SubscriptionProductBasicPopulator<ProductModel, ProductData> getSubscriptionProductBasicPopulator()
	{
		return subscriptionProductBasicPopulator;
	}

	@Required
	public void setSubscriptionProductBasicPopulator(
			final SubscriptionProductBasicPopulator<ProductModel, ProductData> subscriptionProductBasicPopulator)
	{
		this.subscriptionProductBasicPopulator = subscriptionProductBasicPopulator;
	}

	protected ProductEntitlementCollectionPopulator<ProductModel, ProductData> getSubscriptionProductEntitlementPopulator()
	{
		return subscriptionProductEntitlementPopulator;
	}

	@Required
	public void setSubscriptionProductEntitlementPopulator(
			final ProductEntitlementCollectionPopulator<ProductModel, ProductData> subscriptionProductEntitlementPopulator)
	{
		this.subscriptionProductEntitlementPopulator = subscriptionProductEntitlementPopulator;
	}

	protected SubscriptionCommercePriceService getCommercePriceService()
	{
		return commercePriceService;
	}

	@Required
	public void setCommercePriceService(final SubscriptionCommercePriceService commercePriceService)
	{
		this.commercePriceService = commercePriceService;
	}

	protected SubscriptionProductPricePlanPopulator<ProductModel, ProductData> getSubscriptionProductPricePopulator()
	{
		return subscriptionProductPricePopulator;
	}

	@Required
	public void setSubscriptionProductPricePopulator(
			final SubscriptionProductPricePlanPopulator<ProductModel, ProductData> subscriptionProductPricePopulator)
	{
		this.subscriptionProductPricePopulator = subscriptionProductPricePopulator;
	}

	protected ProductStockPopulator<ProductModel, ProductData> getProductStockPopulator()
	{
		return productStockPopulator;
	}

	@Required
	public void setProductStockPopulator(final ProductStockPopulator<ProductModel, ProductData> productStockPopulator)
	{
		this.productStockPopulator = productStockPopulator;
	}

    protected SubscriptionProductService getSubscriptionProductService()
    {
        return subscriptionProductService;
    }

    @Required
    public void setSubscriptionProductService(final SubscriptionProductService subscriptionProductService)
    {
        this.subscriptionProductService = subscriptionProductService;
    }
}
