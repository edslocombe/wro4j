/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 25/07/12 - 15:28
 */

package ro.isdc.wro.extensions.processor.support.jsdoctoolkit;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JsDocGenerateInvokerTest
 *
 * @author Ed Slocombe
 */
public class JsDocGenerateInvokerTest
{
	@Test
	public void testGenerateJsDoc() throws Exception
	{
		JsDocToolkitConfig config = new JsDocToolkitConfig();
	 	JsDocGenerateInvoker invoker = new JsDocGenerateInvoker(config);
		String jsToDocument = IOUtils.toString(getClass().getResourceAsStream("Button.js"));

		final List<File> classDocFiles = new ArrayList<File>();

		config.setPropertyValue(JsDocToolkitConfig.PROP_OUTPUT_HANDLER, new JsDocToolkitOutputHandler()
		{
			@Override
			public void handleOutput(File jsDocOutputDir) throws IOException
			{
				classDocFiles.addAll(JsDocToolkitUtils.getJsClassFiles(jsDocOutputDir));
			}
		});

		invoker.generateJsDoc(jsToDocument);

		Assert.assertEquals(1, classDocFiles.size());
		Assert.assertEquals("Button.html", classDocFiles.get(0).getName());

//		FileUtils.copyFile(classDocFiles.get(0), new File("out" + File.separator + "Button.html"));
	}
}
