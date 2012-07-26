/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 25/07/12 - 10:33
 */

package ro.isdc.wro.extensions.processor.support.jsdoctoolkit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * LoggingJsDocToolkitOutputHandler
 *
 * @author Ed Slocombe
 */
public class LoggingJsDocToolkitOutputHandler implements JsDocToolkitOutputHandler
{
	private final static Logger LOG = Logger.getLogger(LoggingJsDocToolkitOutputHandler.class);


	/**
	 * Takes the specified JavaScript documentation as HTML and prints it to the log.
	 */
	@Override
	public void handleOutput(File generatedDir) throws IOException
	{
		Set<File> classDocFiles = JsDocToolkitUtils.getJsClassFiles(generatedDir);

		for (File f : classDocFiles)
		{
			LOG.info("JSDoc generated:\n" + FileUtils.readFileToString(f));
		}
	}
}
