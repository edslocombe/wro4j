/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 25/07/12 - 09:09
 */

package ro.isdc.wro.extensions.processor.support.processorconfig.valueresolvers;

import org.apache.commons.lang3.StringUtils;
import ro.isdc.wro.extensions.processor.support.processorconfig.ConfigPropertyKey;

/**
 * AbstractConfigPropertyResolver
 *
 * @author Ed Slocombe
 */
public abstract class AbstractConfigPropertyResolver implements ConfigPropertyValueResolver
{
	/**
	 * Returns the full property key that should be used to resolve the
	 * property's value.
	 */
	protected String getFullPropertyKey(ConfigPropertyKey<?> key, String prefix)
	{
		String ret = key.getKey();

		if (!StringUtils.isEmpty(prefix))
		{
			ret = prefix + PROPERTY_KEY_PREFIX_DELIMITER + ret;
		}

		return ret;
	}
}
