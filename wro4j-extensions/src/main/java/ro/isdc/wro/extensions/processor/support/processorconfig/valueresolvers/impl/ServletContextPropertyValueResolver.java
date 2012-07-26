/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 17:14
 */

package ro.isdc.wro.extensions.processor.support.processorconfig.valueresolvers.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.support.processorconfig.ConfigPropertyKey;
import ro.isdc.wro.extensions.processor.support.processorconfig.valueresolvers.AbstractConfigPropertyResolver;

import javax.annotation.Nullable;
import javax.servlet.FilterConfig;

/**
 * ServletContextPropertyValueResolver
 *
 * @author Ed Slocombe
 */
public class ServletContextPropertyValueResolver extends AbstractConfigPropertyResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(ServletContextPropertyValueResolver.class);


	/**
	 * @inheritDoc
	 */
	@Override
	public <T> T resolve(ConfigPropertyKey<T> propertyKey, @Nullable String propertyKeyPrefix)
	{
		T ret = null;
		String fullKey = getFullPropertyKey(propertyKey, propertyKeyPrefix);

		FilterConfig filterConfig = Context.get().getFilterConfig();
		String valuesAsString = filterConfig.getInitParameter(fullKey);

		if (valuesAsString != null)
		{
			try
			{
				ret = propertyKey.convertToRequiredType(valuesAsString);
			}
			catch (IllegalArgumentException e)
			{
				LOG.warn("Failed to resolve property value for property key " + propertyKey +
							" (full property key: '" + fullKey + "')", e);
			}
		}

		return ret;
	}
}
