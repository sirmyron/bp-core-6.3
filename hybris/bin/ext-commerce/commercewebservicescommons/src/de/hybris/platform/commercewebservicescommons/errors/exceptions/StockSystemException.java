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
package de.hybris.platform.commercewebservicescommons.errors.exceptions;

import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceException;


/**
 * Exception thrown if stock system is not enabled
 */
public class StockSystemException extends WebserviceException
{
	public static final String NOT_ENABLED = "notEnabled";
	private static final String TYPE = "StockSystemError";
	private static final String SUBJECT_TYPE = "site";

	public StockSystemException(final String message)
	{
		super(message);
	}

	public StockSystemException(final String message, final String reason)
	{
		super(message, reason);
	}

	public StockSystemException(final String message, final String reason, final Throwable cause)
	{
		super(message, reason, cause);
	}

	public StockSystemException(final String message, final String reason, final String subject)
	{
		super(message, reason, subject);
	}

	public StockSystemException(final String message, final String reason, final String subject, final Throwable cause)
	{
		super(message, reason, subject, cause);
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public String getSubjectType()
	{
		return SUBJECT_TYPE;
	}
}
