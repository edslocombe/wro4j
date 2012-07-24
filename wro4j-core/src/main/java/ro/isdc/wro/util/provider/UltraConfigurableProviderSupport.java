/*
 * (c) 2012 Copyright Detica Limited. ALL RIGHTS RESERVED. This document is copyright of Detica
 * Limited and / or its affiliated companies. Detica, the Detica logo and / or Detica products
 * referenced herein are trademarks of Detica Limited and/or its affiliated companies and may be
 * registered in certain jurisdictions. Other company names, marks, products, logos and symbols
 * referenced herein may be the trademarks or registered trademarks of their owners.
 *
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 10:05
 */

package ro.isdc.wro.util.provider;

import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UltraConfigurableProviderSupport
 *
 * A Spring configurable {@link ConfigurableProvider} implementation.
 * <p/>
 * Simplifies the configuration of default settings.
 *
 * @author Ed Slocombe
 */
public class UltraConfigurableProviderSupport implements ConfigurableProvider
{
	private final Map<String, HashStrategy> hashStrategies = new HashMap<String, HashStrategy>();
	private final Map<String, UriLocator> locators = new HashMap<String, UriLocator>();
	private final Map<String, NamingStrategy> namingStrategies = new HashMap<String, NamingStrategy>();
	private final Map<String, ResourcePreProcessor> preProcessors = new LinkedHashMap<String, ResourcePreProcessor>();
	private final Map<String, ResourcePostProcessor> postProcessors = new LinkedHashMap<String, ResourcePostProcessor>();


	// ================
	// Hash strategies:
	// ================


	/**
	 * The {@link HashStrategy}s that can be used to compute ETags & cache keys.
	 *
	 * @return the {@link HashStrategy} implementations to contribute. The key
	 *         represents the alias.
	 */
	public Map<String, HashStrategy> provideHashStrategies()
	{
		return new HashMap<String, HashStrategy>(hashStrategies);
	}


	/**
	 * Sets the hashing strategies; alias > strategy.
	 */
	public void setHashStrategies(Map<String, HashStrategy> hashStrategies)
	{
		this.hashStrategies.clear();

		addAllHashStrategies(hashStrategies);
	}


	/**
	 * Adds the specified hashing strategies to the existing map contents; alias > strategy.
	 */
	public void addAllHashStrategies(Map<String, HashStrategy> hashStrategies)
	{
		this.hashStrategies.putAll(hashStrategies);
	}


	// =============
	// URI locators:
	// =============


	/**
	 * The {@link UriLocator}s available that can used during processing.
	 *
	 * @return the locators to contribute. The key represents the locator alias.
	 */
	public Map<String, UriLocator> provideLocators()
	{
		return new HashMap<String, UriLocator>(locators);
	}


	/**
	 * Sets the URI locators available for use; alias > URI locator.
	 */
	public void setUriLocators(Map<String, UriLocator> locators)
	{
		this.locators.clear();

		addAllUriLocators(locators);
	}


	/**
	 * Adds the specified URI locators to the existing map contents; alias > URI locator.
	 */
	public void addAllUriLocators(Map<String, UriLocator> locators)
	{
		this.locators.putAll(locators);
	}


	// ================
	// Naming strategy:
	// ================


	/**
	 * The {@link NamingStrategy}s that can be used to rename bundles (for build time solution only).
	 *
	 * @return the {@link NamingStrategy} implementations to contribute. The key represents the namingStrategy alias.
	 */
	public Map<String, NamingStrategy> provideNamingStrategies()
	{
		return new HashMap<String, NamingStrategy>(namingStrategies);
	}


	/**
	 * Sets the naming strategies; alias > naming strategy.
	 */
	public void setNamingStrategies(Map<String, NamingStrategy> namingStrategies)
	{
		this.namingStrategies.clear();

		addAllNamingStrategies(namingStrategies);
	}


	/**
	 * Adds the specified naming strategy to the existing map contents; alias > naming strategy.
	 */
	public void addAllNamingStrategies(Map<String, NamingStrategy> namingStrategies)
	{
		this.namingStrategies.putAll(namingStrategies);
	}


	// ==============
	// PreProcessors:
	// ==============


	/**
	 * Returns the collection of pre processors that can be used during processing.
	 *
	 * @return the preProcessors to contribute. The key represents the processor alias.
	 */
	public Map<String, ResourcePreProcessor> providePreProcessors()
	{
		return new LinkedHashMap<String, ResourcePreProcessor>(preProcessors);
	}


	/**
	 * Sets the pre processors; alias > pre processor.
	 */
	public void setPreProcessors(Map<String, ResourcePreProcessor> preProcessors)
	{
		this.preProcessors.clear();

		addAllPreProcessors(preProcessors);
	}


	/**
	 * Adds the pre processors to the existing map contents; alias > pre processor.
	 */
	public void addAllPreProcessors(Map<String, ResourcePreProcessor> preProcessors)
	{
		this.preProcessors.putAll(preProcessors);
	}


	// ===============
	// PostProcessors:
	// ===============


	/**
	 * Returns the collection of post processors that can be used during processing.
	 *
	 * @return the postProcessors to contribute. The key represents the processor alias.
	 */
	public Map<String, ResourcePostProcessor> providePostProcessors()
	{
		return new LinkedHashMap<String, ResourcePostProcessor>(postProcessors);
	}


	/**
	 * Sets the post processors; alias > post processor.
	 */
	public void setPostProcessors(Map<String, ResourcePostProcessor> postProcessors)
	{
		this.postProcessors.clear();

		addAllPostProcessors(postProcessors);
	}


	/**
	 * Adds the specified post processors to the existing map contents; alias > post processor.
	 */
	public void addAllPostProcessors(Map<String, ResourcePostProcessor> postProcessors)
	{
		this.postProcessors.putAll(postProcessors);
	}


	// =====================


	/**
	 * An optional method to be called when you wish to only define additional <em>or overriding</em>
	 * provider configuration, and are not interested in defining all of the standard providers
	 * as well.
	 * <p/>
	 * Defaults are provided by the {@link DefaultConfigurableProvider} class.
	 */
	public void initWithDefaults()
	{
		DefaultConfigurableProvider defaultProvider = new DefaultConfigurableProvider();

		putAllInMapIfKeyAbsent(this.hashStrategies, defaultProvider.provideHashStrategies());
		putAllInMapIfKeyAbsent(this.locators, defaultProvider.provideLocators());
		putAllInMapIfKeyAbsent(this.namingStrategies, defaultProvider.provideNamingStrategies());
		putAllInMapIfKeyAbsent(this.preProcessors, defaultProvider.providePreProcessors());
		putAllInMapIfKeyAbsent(this.postProcessors, defaultProvider.providePostProcessors());
	}


	/**
	 * Helper method that performs a {@link Map#putAll} without overriding existing
	 * values in the map.
	 */
	private <K, V> void putAllInMapIfKeyAbsent(Map<K, V> target, Map<K, V> source)
	{
		Map<K, V> tmp = new LinkedHashMap<K, V>(source);

		tmp.keySet().removeAll(target.keySet());

		target.putAll(tmp);
	}
}
