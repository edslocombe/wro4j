/**
 * Copyright 2008 Martin 'Windgazer' Reurings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.isdc.wro.extensions.processor.support.jsdoctoolkit.windgazer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;

import com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;

/** 
 * This is the JSDoc generator. It's a wrapper that diverts the standard
 * output to the logger and allows you to run JSDoc Toolkit from within
 * a java environment instead of command-line.<br />
 * It is expected that any Mojo making use of this generator by default
 * exposes all the options outlined below.<br />
 * Developers have access to all the 'options' that JSDoc has and
 * they can be overridden using mavens' plugin configuration.<br />
 * <br />
 * The JSDoc Toolkit usage:<br />
 *<pre>USAGE: java -jar app/js.jar app/jsdoc.js [OPTIONS] <SRC_DIR> <SRC_FILE> ...
 *
 *OPTIONS:
 *  -t=<PATH> or --template=<PATH>
 *          Required. Use this template to format the output.
 *
 *  -d=<PATH> or --directory=<PATH>
 *          Output to this directory (defaults to js_docs_out).
 *
 *  -r=<DEPTH> or --recurse=<DEPTH>
 *          Descend into src directories.
 *
 *  -x=<EXT>[,EXT]... or --ext=<EXT>[,EXT]...
 *          Scan source files with the given extension/s (defaults to js).
 *
 *  -a or --allfunctions
 *          Include all functions, even undocumented ones.
 *
 *  -A or --Allfunctions
 *          Include all functions, even undocumented, underscored ones.
 *
 *  -p or --private
 *          Include symbols tagged as private.
 *
 *  -h or --help
 *          Show this message and exit.
 *</pre>
 *<br />
 *As of 2.0.1, usage is
 *<pre>USAGE: java -jar jsrun.jar app/run.js [OPTIONS] <SRC_DIR> <SRC_FILE> ...
 *
 *OPTIONS:
 *  -a or --allfunctions
 *          Include all functions, even undocumented ones.
 *
 *  -c or --conf
 *          Load a configuration file.
 *
 *  -d=<PATH> or --directory=<PATH>
 *          Output to this directory (defaults to "out").
 *
 *  -D="myVar:My value" or --define="myVar:My value"
 *          Multiple. Define a variable, available in JsDoc as JSDOC.opt.D.myVar
 *
 *  -e=<ENCODING> or --encoding=<ENCODING>
 *          Use this encoding to read and write files.
 *
 *  -h or --help
 *          Show this message and exit.
 *
 *  -n or --nocode
 *          Ignore all code, only document comments with @name tags.
 *
 *  -o=<PATH> or --out=<PATH>
 *          Print log messages to a file (defaults to stdout).
 *
 *  -p or --private
 *          Include symbols tagged as private, underscored and inner symbols.
 *
 *  -r=<DEPTH> or --recurse=<DEPTH>
 *          Descend into src directories.
 *
 *  -s or --suppress
 *          Suppress source code output.
 *
 *  -t=<PATH> or --template=<PATH>
 *          Required. Use this template to format the output.
 *
 *  -T or --test
 *          Run all unit tests and exit.
 *
 *  -v or --verbose
 *          Provide verbose feedback about what is happening.
 *
 *  -x=<EXT>[,EXT]... or --ext=<EXT>[,EXT]...
 *          Scan source files with the given extension/s (defaults to js).
 *</pre>
 *<br />
 *The default JSDoc-plugin configuration, help is exluded (of course) and the long-names of the properties are used:<br />
 *<pre>
 *&lt;plugin>
 *	&lt;groupId>nl.windgazer&lt;/groupId>
 *	&lt;artifactId>jsdoc-plugin&lt;/artifactId>
 *	&lt;version>[VERSION]&lt;/version>
 *	&lt;configuration>
 *		&lt;template>jsdoc&lt;/template> &lt;!-- Alternatives are not pre-packaged since version 2.x -->
 *		&lt;directory>${project.build.directory}/jsdoc&lt;/directory>
 *		&lt;recurse>1&lt;/recurse>
 *		&lt;ext>js&lt;/ext>
 *		&lt;allfunctions>true&lt;/allfunctions>
 *		&lt;Allfunctions>false&lt;/Allfunctions>
 *		&lt;privateOption>false&lt;/privateOption>
 *		&lt;srcDir>${project.warSourceDirectory}&lt;/srcDir>
 *	&lt;/configuration>
 *&lt;/plugin>
 *</pre>
 *Support for templates has been implemented as of version 2.3.0.1-RC1. You can either copy a template to 
 *[basedir]/src/main/templates/[templatename] and set the apopriate setting to 'templatename' or you can provide a 
 *fully qualified path to your template, anywhere on disc, or using maven variables, as with any property setting.<br />
 *Currently no support has been added for some of the new settings exposed in JSDoc Toolkit 2, none are essential.
 * @author mreuring
 *
 * @author Ed Slocombe: Modifications made to enable configuration of the JSDoc Toolkit version.
 */
public class JSDocGenerator {
	private File baseDir = (System.getProperty("basedir")!=null)?new File(System.getProperty("basedir")):null;
	
	/**
     * The temporary directory where JSDoc toolkit is stored while running.
     * This is not a JSDoc property, and won't be found in the JSDoc documentation.
     */
	private File tempDir;
	private File filteredDir = null;

	/**
	 * The URL path to the directory that contains the JSDOc Toolkit executable run.js and its dependencies.
	 */
	private URL toolkitDirPath;

	/**
     * Template used to format the output. Only 'jsdoc' is packaged since version 2.0.0 of JsDoc Toolkit. <br />
     * upport for templates has been implemented as of version 2.3.0.1-RC1 of this plugin. You can either copy a template to 
     * [basedir]/src/main/templates/[templatename] and set the apopriate setting to 'templatename' or you can provide a 
     * fully qualified path to your template, anywhere on disc, or using maven variables, as with any property setting.<br />
     * Alternatively there is the theoretical possibility of packaging your template in a jar file within the correct 'package'.
     * This has not been tested, so it remain a theoretical possibility until somebody has taken the effort to test and report
     * it back to me :).<br />
     * This property represents: -t=&lt;PATH> or --template=&lt;PATH>
     */
	private String template;
	
	/**
     * The target directory where you want the generated doc to end up. This
     * property represents: -d=&lt;PATH> or --directory=&lt;PATH>.
     */
	private File directory;
	
	/**
	 * The level of recursion. Default is 1.
	 */
	private int recurse;
	
	/**
	 * The extension for the javascript files, default is 'js'.
	 */
	private String extension;
	
	/**
	 * Include all functions in the JSDoc, including undocumented ones.
	 */
	private boolean allFunctions;

	/**
	 * Include all functions in the JSDoc, including undocumented and underscored ones.
	 */
	private boolean AllFunctions;
	
	/**
	 * Include symbols tagged as private.
	 */
	private boolean privateOption;
	
	/**
	 * The directory where we may find the source-files we're making jsdocs for.
	 * This property represents: &lt;SRC_DIR>
	 */
	private File srcDir;

	private Logger log;

	/**
	 * A list of regular expressions for files to be excluded from processing. Especially useful if
	 * your project includes 3rd party code that you don't want to end up in your own documentation.
	 */
	private String[] exclude;

	public JSDocGenerator(URL jsDocToolkitDir, File tempDir, File target, File src, String[] exclude, String template, String extension, int recurse, boolean allFunctions, boolean AllFunctions, boolean privateOption, Logger log) throws JSDocException {
		URL envUrl = null;
		try {
			envUrl = new URL(System.getProperty("jsdoc.jsDocToolkitDir"));
		}
		catch (MalformedURLException e) { }
		this.toolkitDirPath = (envUrl!=null)?envUrl:jsDocToolkitDir;
		String env = System.getProperty("jsdoc.tempdir");
		this.tempDir = (env!=null&&baseDir!=null)?new File(baseDir, env):tempDir;
		env = System.getProperty("jsdoc.directory");
		this.directory = (env!=null&&baseDir!=null)?new File(baseDir, env):target;
		env = System.getProperty("jsdoc.srcdir");
		this.srcDir = (env!=null&&baseDir!=null)?new File(baseDir, env):src;
		env = System.getProperty("jsdoc.template");
		this.template = (env!=null)?env:template;
		env = System.getProperty("jsdoc.extension");
		this.extension = (env!=null)?env:extension;
		env = System.getProperty("jsdoc.recurse");
		this.recurse = (env!=null)?Integer.valueOf(env):recurse;
		env = System.getProperty("jsdoc.allfunctions");
		this.allFunctions = (env!=null)?Boolean.valueOf(env):allFunctions;
		this.AllFunctions = AllFunctions;
		env = System.getProperty("jsdoc.privateoption");
		this.privateOption = (env!=null)?Boolean.valueOf(env):privateOption;
		this.log = log;
		
		this.exclude = exclude;
		
		prepToolkit();
	}

	private Logger getLog() {
		return log;
	}

    /**
     * Copy an InputStream to an OutputStream.
     * Thank you ludovic.claude54 for providing me with a good update
     * for my crappy piece of code :)
     * (http://code.google.com/p/jsdoctk-plugin/issues/detail?id=1&can=1) 
     * 
     * @param inputStream
     * @param outputStream
     * @return
     * @throws IOException
     */
	private boolean copyStreams(InputStream inputStream, OutputStream outputStream) throws IOException {
		int copied = IOUtils.copy(inputStream, outputStream);
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        return (copied > 0);
	}

	/**
	 * Copy the JSDoc Toolkit from the jar into a working directory.
	 * 
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 */
	private boolean copyToolkit(JarURLConnection from, File to) throws IOException {
		Enumeration<JarEntry> entries = from.getJarFile().entries();
		//Run through the jar-file and extract all the toolkit related files
		while (entries.hasMoreElements()) {
			JarEntry je = entries.nextElement();
			String name = je.getName();
			if (name.startsWith("jsdoc_toolkit")) {
				if (name.endsWith("/")) {
					//create directories
					String[] dirs = name.split("/");
					File dir = tempDir;
					if (!dir.exists()) {
						getLog().debug("Creating directory '" + dir.getAbsolutePath() + "'");
						dir.mkdir();
						dir.deleteOnExit(); //Automated cleanup
					}
					for (int i = 1; i < dirs.length; i++) {
						dir = new File(dir, dirs[i]);
						if (!dir.exists()) {
							getLog().debug("Creating directory '" + dir.getAbsolutePath() + "'");
							dir.mkdir();
							dir.deleteOnExit(); //Automated cleanup
						}
					}
				}
				else {
					//copy file, assumption is made that directories have been created (due to the order of entries in a jar)
					name = name.substring(name.indexOf("/") + 1);
					File f = new File(tempDir, name);
					if (!f.exists()) {
						getLog().debug("Copy '" + je.getName() + "' to '" + f.getAbsolutePath() + "'");
						InputStream is = getClass().getClassLoader().getResourceAsStream(je.getName()); //'name' String has been modified
						FileOutputStream fo = new FileOutputStream(f);
						copyStreams(is, fo);
						f.deleteOnExit(); //Automated cleanup
					}
				}
			}
		}
		return true;
	}
	
	private boolean copyFile(final File from, final File to, final String[] filters) throws IOException {
		if (from.isDirectory()) {
			for (File f : from.listFiles(new FileFilter(){

				public boolean accept(File pathname) {
					if (!pathname.isDirectory() && !pathname.getAbsolutePath().endsWith(".js")) return false;
					for (String filter:filters) {
						if (pathname.getAbsolutePath().contains(filter)) return false;
					}
					return true;
				}
				
			})) {
				copyFile(f, to, filters);
			}
		} else {
			final File toFile = new File(to, from.getName());
			if (!toFile.exists()) {
				InputStream is = new FileInputStream(from);
				FileOutputStream fo = new FileOutputStream(toFile);
				copyStreams(is, fo);
			}
		}
		return true;
	}
	
	private void prepToolkit() throws JSDocException {
		System.setProperty("jsdoc.dir", tempDir.getAbsolutePath()); //Set JSDoc Toolkit working directory

		//Copy JSDoc Toolkit to a temporary working directory
		getLog().info("Copying JSDoc Toolkit to temporary directory.");

		try {
			URL resource = toolkitDirPath;

			if (resource.getProtocol().startsWith("file")) {
				//In case of an exploded jar, don't bother copying, just set the tempDir
				File fileLoc = new File(resource.getFile());
				tempDir = fileLoc.getParentFile();
			} else {
				URLConnection uc = resource.openConnection();
				JarURLConnection juc = (JarURLConnection)uc;
				tempDir.mkdirs();
				tempDir.deleteOnExit();
				if (!copyToolkit(juc, tempDir)) new JSDocException("JSDoc-plugin failed to copy the toolkit.");
				else getLog().info("JSDoc Toolkit copied to temporary directory, execution will commense shortly.");
			}
		} catch (IOException e) {
			throw new JSDocException("Failed to obtain JSDoc Toolkit, couldn't read the jar-file.", e);
		}
		
//		if (exclude!=null && exclude.length > 0) {
//			filteredDir = new File(tempDir, "filtered");
//			filteredDir.mkdirs();
//			filteredDir.deleteOnExit();
//			try {
//				copyFile(srcDir, filteredDir, exclude);
//			} catch (IOException e) {
//				throw new JSDocException("Failed to filter the files.", e);
//			}
//		}
	}
	
	protected void runJSDocToolkit(ArrayList<String> argus) throws JSDocException {

		String appDirName = toolkitDirPath.toString().substring(toolkitDirPath.toString().lastIndexOf('/')+1);
		File jsDocApp = new File(tempDir.getAbsolutePath(), appDirName + File.separator + "run.js");

		ArrayList<String> finalArgs = new ArrayList<String>(argus);
		//finalArgs.add(0, tempDir.getAbsolutePath() + "/app/run.js");
		finalArgs.add("-j=" + jsDocApp.getAbsolutePath());
		//finalArgs.add("-j=" + tempDir.getAbsolutePath() + "/app/run.js");

		getLog().debug("Final arguments: " + finalArgs.toString());

		//Divert normal and error output to keep mvn results clean
		PrintStream out = System.out;
		PrintStream err = System.err;
		PrintStream alt = new JSDocInfoStream(new ByteArrayOutputStream(), getLog(), out);
		System.setOut(alt); //Capture all toolkit output
		System.setErr(new JSDocErrorStream(new ByteArrayOutputStream(), getLog(), err, alt)); //Capture any errors ocurring in toolkit and output them as debug

		try {
			Context cx = Context.enter();
			cx.setLanguageVersion(Context.VERSION_1_6);
            Global global = new Global();
            global.init(cx);
            Scriptable argsObj = cx.newArray(global, finalArgs.toArray(new Object[]{}));
            global.defineProperty("arguments", argsObj, ScriptableObject.DONTENUM);
			FileReader reader = new FileReader(jsDocApp);
			cx.evaluateReader(global, reader, jsDocApp.getName(), 1, null);
		} catch (FileNotFoundException e) {
			throw new JSDocException("JsDoc Toolkit startup script could not be located!");
		} catch (IOException e) {
			throw new JSDocException("Failure to read JsDoc Toolkit startup script!");
		} finally {
			System.setOut(out);
			System.setErr(err);
		}
	}

	/**
	 * Generate the JSDoc Toolkit report.
	 */
	public void generateReport(Locale locale) throws JSDocException {
		if (srcDir.exists()) {
	
			getLog().info("Will be generating JSDoc for '" + srcDir.getAbsolutePath() + "'.");
	
			//Setting up options/arguments
			ArrayList<String> args = new ArrayList<String>();
			args.add("-v");
			
			//Check if the template can be found in ./main/templates/[template]
			if (baseDir != null) {
				File packaged = new File(baseDir, "src/main/templates/" + template);
				if (packaged.exists()) template = packaged.getAbsolutePath();
			}
			
			File check = new File(template);
			if (check.exists()) { //Test 'template' for being an actual path to an outside template...
				args.add("-t=" + template.replace("/", File.separator));
			} else {
				args.add("-t=" + tempDir.getAbsolutePath() + "/templates/" + template);
			}
			args.add("-d=" + directory.getAbsolutePath());
			args.add("-r=" + recurse);
			args.add("-x=" + extension);
			if (allFunctions) args.add("-a");
			if (AllFunctions) args.add("-A");
			if (privateOption) args.add("-p");
			//args.add((filteredDir!=null?filteredDir:srcDir).getAbsolutePath());
			if (exclude!=null && exclude.length > 0) {
				for (final String excl : exclude) {
					args.add("--exclude=" + excl);
				}
			}
			args.add(srcDir.getAbsolutePath()); //Set last param to the accual source-dir
	
			runJSDocToolkit(args);

		} else {

			getLog().info("Will not be generating JSDoc for '" + srcDir.getAbsolutePath() + "', source-directory does not exist.");

		}

	}
}
