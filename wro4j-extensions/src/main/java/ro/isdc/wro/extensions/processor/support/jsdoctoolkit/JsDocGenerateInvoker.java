/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 16:02
 */

package ro.isdc.wro.extensions.processor.support.jsdoctoolkit;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.jsdoctoolkit.windgazer.JSDocGenerator;
import ro.isdc.wro.extensions.processor.support.processorconfig.ConfigSupport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * JsDocGenerateInvoker
 *
 * @author Ed Slocombe
 */
public class JsDocGenerateInvoker
{
	private final static Logger LOG = LoggerFactory.getLogger(JsDocGenerateInvoker.class);

	private final ConfigSupport config;


	public JsDocGenerateInvoker(ConfigSupport config)
	{
		this.config = config;
	}


	public void generateJsDoc(String jsToDocument) throws IOException
	{
		JsDocToolkitOutputHandler outputHandler = config.getPropertyValue(JsDocToolkitConfig.PROP_OUTPUT_HANDLER);
		File tmpDir = null;

		try
		{
			tmpDir = Files.createTempDir();
			File processingTmpDir = new File(tmpDir, "process-tmp");
			File outputTmpDir = new File(tmpDir, "jsdoctk-out-tmp");
			File inputFileTmpFile = new File(tmpDir, "jsdoctk-in-tmp.js");

			if (processingTmpDir.mkdir() && outputTmpDir.mkdir() && inputFileTmpFile.createNewFile())
			{
				FileUtils.write(inputFileTmpFile, jsToDocument);

				URL jsDocToolkitDir = config.getPropertyValue(JsDocToolkitConfig.PROP_JSDOC_TOOLKIT_DIR);
				String templateDir = config.getPropertyValue(JsDocToolkitConfig.PROP_TEMPLATE_DIR).getFile();
				boolean includeAllFunctions = config.getPropertyValue(JsDocToolkitConfig.PROP_ALL_FUNCTIONS);
				boolean includePrivate = config.getPropertyValue(JsDocToolkitConfig.PROP_INCLUDE_PRIVATE);

				JSDocGenerator generator = new JSDocGenerator(
						jsDocToolkitDir, processingTmpDir, outputTmpDir, inputFileTmpFile,
						new String[]{}, templateDir, "js", 1, includeAllFunctions, false, includePrivate, LOG);

				generator.generateReport(Locale.getDefault());

				outputHandler.handleOutput(outputTmpDir);
			}
			else
			{
				LOG.warn("Failed to create working directory and input/output file under temporary directory " + tmpDir);
			}
		}
		catch (Exception e)
		{
			throw new WroRuntimeException("Failed to generate JSDoc", e);
		}
		finally
		{
			if (tmpDir != null)
			{
				tmpDir.deleteOnExit();
			}
		}
	}

}
