/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 16:03
 */

package ro.isdc.wro.extensions.processor.support.jsdoctoolkit;

import java.io.File;
import java.io.IOException;

/**
 * JsDocToolkitOutputHandler
 *
 * @author Ed Slocombe
 */
public interface JsDocToolkitOutputHandler
{
	/**
	 * Takes the specified JavaScript documentation as HTML files, contained
	 * within the specified directory and does something sensible with it.
	 * <p/>
	 * One important thing to note is that the supplied output directory will
	 * be guaranteed to exist up until the end of the application's life cycle
	 * but may be <strong>deleted at the end</strong>, hence you may wish to
	 * copy the contents to a more permanent location.
	 */
	public void handleOutput(File generatedDir) throws IOException;
}
