/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 17:07
 */

package ro.isdc.wro.extensions.processor.support.processorconfig.valueresolvers;

import ro.isdc.wro.extensions.processor.support.processorconfig.ConfigPropertyKey;

import javax.annotation.Nullable;

/**
 * ConfigValueResolver
 *
 * @author Ed Slocombe
 */
public interface ConfigPropertyValueResolver
{
	public static final String PROPERTY_KEY_PREFIX_DELIMITER = ".";


	/**
	 * Attempts to resolve the configuration property for the key specified.
	 * <p/>
	 * <tt>null</tt> is returned if the property is not resolvable given the
	 * current context, or the property is not specified using the
	 * implementation's method of specifying values.
	 */
	public <T> T resolve(ConfigPropertyKey<T> propertyKey, @Nullable String propertyKeyPrefix);
}
