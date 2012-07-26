/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 16:07
 */

package ro.isdc.wro.extensions.processor.support.processorconfig;

import ro.isdc.wro.extensions.processor.support.processorconfig.valueresolvers.ConfigPropertyValueResolver;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ConfigSupport
 *
 * @author Ed Slocombe
 */
public class ConfigSupport
{
	private final List<ConfigPropertyValueResolver> valueResolvers = new ArrayList<ConfigPropertyValueResolver>();
	private final Set<ConfigPropertyKey<?>> expectedPropertyKeys = new HashSet<ConfigPropertyKey<?>>();
	private final Map<ConfigPropertyKey<?>, Object> properties = new HashMap<ConfigPropertyKey<?>, Object>();

	protected final String configSetKey;


	/**
	 * Constructs a new configuration collection with the specified key.
	 * <p/>
	 * A key cannot be null but can be an empty string. The key will be used
	 * as a prefix to property keys when resolving values.
	 * <p/>
	 * The <tt>expectedPropertyKeys</tt> parameter is that when we have
	 * required properties, those expected property keys with a required,
	 * but missing value will cause validation to fail. They can be also used
	 * to specify default values.
	 * <p/>
	 * Any properties added, whose property key belongs to the specified
	 * 'expected' collection, as well as additional unspecified property keys
	 * will be accepted.
	 */
	public ConfigSupport(String configSetKey, @Nullable Collection<ConfigPropertyKey<?>> expectedPropertyKeys)
	{
		this.configSetKey = configSetKey;

		if (expectedPropertyKeys != null)
		{
			this.expectedPropertyKeys.addAll(expectedPropertyKeys);
		}
	}


	/**
	 * Constructs a new configuration collection with the specified key.
	 * <p/>
	 * A potentially easier shorthand constructor.
	 *
	 * @see #ConfigSupport(String, java.util.Collection)
	 */
	public ConfigSupport(String configSetKey, ConfigPropertyKey<?>... expectedPropertyKeys)
	{
		this.configSetKey = configSetKey;

		if (expectedPropertyKeys.length > 0)
		{
			Collections.addAll(this.expectedPropertyKeys, expectedPropertyKeys);
		}
	}


	/**
	 * Adds the specified value resolver to the <em>ordered list</em> of value
	 * resolvers that are used in an attempt to resolve a property's value
	 * when requested.
	 * <p/>
	 * When looking up a property's value, once a non-null value is returned
	 * from a value resolver, all succeeding resolvers are ignored.
	 */
	public void addValueResolver(ConfigPropertyValueResolver valueResolver)
	{
		this.valueResolvers.add(valueResolver);
	}


	/**
	 * Sets the specified list of value resolvers that are used in an attempt
	 * to resolve a property's value when requested.
	 *
	 * @see #addValueResolver(ConfigPropertyValueResolver)
	 */
	public void setValueResolvers(List<ConfigPropertyValueResolver> valueResolvers)
	{
		this.valueResolvers.clear();
		this.valueResolvers.addAll(valueResolvers);
	}


	/**
	 * Sets the expected property keys for this config collection.
	 * <p/>
	 *
	 */
	public void setExpectedPropertyKeys()
	{
		this.expectedPropertyKeys.clear();

	}


	/**
	 * Returns <tt>true</tt> if the config support object is
	 * valid, i.e. has all required properties and values are all
	 * of a valid type.
	 */
	public boolean isValid()
	{
		return getInvalidProperties().isEmpty();
	}


	/**
	 * Returns the set of invalid properties, where properties are invalid when they
	 * are either expected and required, or their value doesn't match the required type.
	 * <p/>
	 * Note: If no invalid properties are present an empty set is returned.
	 */
	public Set<ConfigPropertyKey<?>> getInvalidProperties()
	{
		Set<ConfigPropertyKey<?>> ret = new HashSet<ConfigPropertyKey<?>>();
		Set<ConfigPropertyKey<?>> propertiesToCheck = new HashSet<ConfigPropertyKey<?>>();

		propertiesToCheck.addAll(properties.keySet());
		propertiesToCheck.addAll(expectedPropertyKeys);

		for (ConfigPropertyKey<?> key : propertiesToCheck)
		{
			if (key.isRequired() && getPropertyValue(key) == null)
			{
				ret.add(key);
			}
		}

		return ret;
	}


	/**
	 * Returns the config object's value for the specified key or
	 * <tt>null</tt> if there is no set value and no default value.
	 */	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(ConfigPropertyKey<T> key)
	{
		T ret = (T) properties.get(key);

		if (ret == null)
		{
			for (int i = 0, valueResolversSize = valueResolvers.size(); i < valueResolversSize && ret == null; i++)
			{
				ConfigPropertyValueResolver valResolver = valueResolvers.get(i);

				ret = valResolver.resolve(key, configSetKey);
			}
		}

		if (ret == null)
		{
			ret = key.getDefaultValue();
		}

		return ret;
	}


	/**
	 * Sets the <em>overriding</em> value of the specified property key.
	 * <p/>
	 * An {@link IllegalArgumentException} is thrown if the value specified
	 * is <tt>null</tt> and the property key requires a value.
	 * <p/>
	 * If a non-null property value is set, no value resolvers will be used.
	 *
	 * @throws IllegalArgumentException
	 */
	public <T> void setPropertyValue(ConfigPropertyKey<T> key, T value)
	{
		if (key.isRequired() && value == null)
		{
			throw new IllegalArgumentException("Null is not a valid value for the required property " + key);
		}

		properties.put(key, value);
	}


	/**
	 * Sets all of the specified properties as <em>overriding</em> values.
	 * <p/>
	 * If a non-null property value is set, no value resolvers will be used.
	 *
	 * @see #setPropertyValue(ConfigPropertyKey, Object)
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public void setPropertyValues(Map<ConfigPropertyKey, Object> properties)
	{
		for (ConfigPropertyKey key : properties.keySet())
		{
			Object value = properties.get(key);

			if (value.getClass().isAssignableFrom(key.getValueType()))
			{
				setPropertyValue(key, value);
			}
			else
			{
				throw new IllegalArgumentException("Value of property " + key + " must be an instance of "
							+ key.getValueType().getSimpleName());
			}
		}
	}
}
