/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 16:01
 */

package ro.isdc.wro.extensions.processor.support.jsdoctoolkit;

import ro.isdc.wro.extensions.processor.support.processorconfig.ConfigPropertyKey;
import ro.isdc.wro.extensions.processor.support.processorconfig.ConfigSupport;

import java.net.URL;

/**
 * JsDocToolkitConfig
 *
 * @author Ed Slocombe
 */
public class JsDocToolkitConfig extends ConfigSupport
{
	public static final String CONFIG_KEY = "jsDocToolKit";


	/**
	 * The URL to the <tt>run.js</tt> file of the JSDoc Toolkit file.
	 */
	public static final ConfigPropertyKey<URL> PROP_JSDOC_TOOLKIT_DIR = new ConfigPropertyKey<URL>(
			"jsDocToolkitPath",
			URL.class,
			true,
			JsDocToolkitConfig.class.getResource("windgazer/app-2.4.0"));

	/**
	 * The URL to the JSDoc Toolkit template files.
	 */
	public static final ConfigPropertyKey<URL> PROP_TEMPLATE_DIR = new ConfigPropertyKey<URL>(
			"templateDir",
			URL.class,
			true,
			JsDocToolkitConfig.class.getResource("templates"));

	/**
	 * The output handler implementation that should be used.
	 */
	public static final ConfigPropertyKey<JsDocToolkitOutputHandler> PROP_OUTPUT_HANDLER = new ConfigPropertyKey<JsDocToolkitOutputHandler>(
			"outputHandler",
			JsDocToolkitOutputHandler.class,
			true,
			new LoggingJsDocToolkitOutputHandler());

	/**
	 * Whether all functions should be documented, regardless of annotations.
	 */
	public static final ConfigPropertyKey<Boolean> PROP_ALL_FUNCTIONS = new ConfigPropertyKey<Boolean>(
			"allFunctions",
			Boolean.class,
			true,
			false);

	/**
	 * Whether to include symbols tagged as private, underscored and inner symbols.
	 */
	public static final ConfigPropertyKey<Boolean> PROP_INCLUDE_PRIVATE = new ConfigPropertyKey<Boolean>(
			"includePrivate",
			Boolean.class,
			true,
			false);


	public JsDocToolkitConfig()
	{
		super(CONFIG_KEY,
				PROP_JSDOC_TOOLKIT_DIR,
				PROP_TEMPLATE_DIR,
				PROP_OUTPUT_HANDLER,
				PROP_ALL_FUNCTIONS,
				PROP_INCLUDE_PRIVATE);
	}
}
