/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 16:09
 */

package ro.isdc.wro.extensions.processor.support.processorconfig;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * ConfigPropertyKey
 *
 * @author Ed Slocombe
 */
public class ConfigPropertyKey<T>
{
	public final static boolean DEFAULT_REQUIRED_VALUE = false;

	private final String key;
	private final Class<T> valueType;
	private final boolean required;

	private T defaultValue;

	// TODO - how do I get this? @Inject doesn't seem like it'd be that helpful
	private UriLocatorFactory uriLocatorFactory;


	//
	// Constructors:
	//


	public ConfigPropertyKey(String key, Class<T> valueType)
	{
		this(key, valueType, DEFAULT_REQUIRED_VALUE);
	}

    public ConfigPropertyKey(String key, Class<T> valueType, boolean required)
	{
		this(key, valueType, required, null);
	}

	public ConfigPropertyKey(String key, Class<T> valueType, boolean required, @Nullable T defaultValue)
	{
		this.key = key;
		this.valueType = valueType;
		this.required = required;
		this.defaultValue = defaultValue;
	}


	/**
	 * Returns the config property's key.
	 */
	public String getKey()
	{
		return key;
	}


	/**
	 * Returns the class that this property's value must be
	 * an instance of.
	 */
	public Class<?> getValueType()
	{
		return valueType;
	}


	/**
	 * Returns <tt>true</tt> if this property must have a value.
	 * <p/>
	 * Defaults to {@link #DEFAULT_REQUIRED_VALUE} if left unspecified.
	 */
	public boolean isRequired()
	{
		return required;
	}


	/**
	 * Returns the default value for this property, or <tt>null</tt> if not
	 * applicable.
	 */
	public T getDefaultValue()
	{
		return defaultValue;
	}


	public void setDefaultValue(T defaultValue)
	{
		this.defaultValue = defaultValue;
	}


	/**
	 * Returns the value string specified as an instance of the required
	 * type for this property key.
	 * <p/>
	 * Null is returned if the <tt>valueAsString</tt> parameter is <tt>null</tt>
	 * or, for some instances, an empty string.
	 * <p/>
	 * An exception is thrown if the value cannot be converted to the
	 * required type.
	 *
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public T convertToRequiredType(String valuesAsString)
	{
		// TODO - maybe configure converters one day...

		T ret = null;

		if (valuesAsString != null)
		{
			Exception convertException = null;

			try
			{
				if (valueType.equals(String.class))
				{
					ret = (T) valuesAsString;
				}
				else if (valueType.equals(Boolean.class))
				{
					ret = (T) new Boolean("true".equalsIgnoreCase(valuesAsString));
				}
				else if (valueType.equals(Integer.class))
				{
					ret = (T) new Integer(Integer.parseInt(valuesAsString));
				}
				else if (valueType.equals(Double.class))
				{
					ret = (T) new Double(Double.parseDouble(valuesAsString));
				}
				else if (valueType.equals(InputStream.class))
				{
					if (uriLocatorFactory != null)
					{
						ret = (T) uriLocatorFactory.locate(valuesAsString);
					}
					else
					{
						throw new RuntimeException("Attempting to convert a InputStream value for property " + this +
								" but the " + UriLocatorFactory.class.getSimpleName() + " instance is null");
					}
				}
			}
			catch (Exception e)
			{
				convertException = e;
			}

			if (ret == null)
			{
				throw new IllegalArgumentException("Cannot convert string value '" + valuesAsString + "' for property key " +
						this + " into a " + valueType.getSimpleName(), convertException);
			}
		}

		return ret;
	}


	/**
	 * @iheritDoc
	 */
	@Override
	public String toString()
	{
		return key;
	}


	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		ConfigPropertyKey that = (ConfigPropertyKey) o;

		return key.equals(that.key);

	}


	@Override
	public int hashCode()
	{
		return key.hashCode();
	}
}
