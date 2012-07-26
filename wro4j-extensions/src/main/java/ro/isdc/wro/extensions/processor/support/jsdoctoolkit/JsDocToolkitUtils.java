/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 26/07/12 - 09:19
 */

package ro.isdc.wro.extensions.processor.support.jsdoctoolkit;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * JsDocToolkitUtils
 *
 * @author Ed Slocombe
 */
public class JsDocToolkitUtils
{
	/**
	 * Returns all JavaScript class documentation files contained by the
	 * specified output directory.
	 */
	public static Set<File> getJsClassFiles(File jsDocOutputDir)
	{
		File symbols = new File(jsDocOutputDir, "symbols");
		Set<File> jsClasses = new HashSet<File>();

		if (symbols.isDirectory())
		{
			for (File f : symbols.listFiles())
			{
				if (f.getName().endsWith(".html") && !f.getName().equals("_global_.html"))
				{
					jsClasses.add(f);
				}
			}
		}

		return jsClasses;
	}
}
