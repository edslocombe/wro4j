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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

/**
 * This is a PrintStream used for diverting JSDoc Toolkit output.
 * I found that the extensive information coming out of JSDoc Toolkit
 * was cluttering the standard MVN output. This PrintStream is an
 * attempt to keep the output clear.
 * When running mvn with the -X flag this output should be vissible,
 * therefor most output is diverted to log.debug() and only particularly
 * usefull information is diverted to log.info() or log.error().
 * 
 * @author mreuring
 */
public class JSDocInfoStream extends PrintStream {
	
	public static final String OUTPUT_ERROR = "ERROR:";
	public static final String OUTPUT_WARN = ">> WARNING:";

	private Logger log;
	private PrintStream out;
	private final Pattern regexp = Pattern.compile("(\\d+) source files? found");

	/**
	 * Construct a PrintStream that will intercept some messages and log them
	 * to the mvn output.
	 * 
	 * @param os An OutputStream for diverting JSDoc messages.
	 * @param log The mvn log, where 'interresting' messages are logged to.
	 * @param out The 'real' System.out, which will be used for logging to mvn.
	 */
	public JSDocInfoStream(OutputStream os, Logger log, PrintStream out) {
		super(os);
		this.log = log;
		this.out = out;
	}
	
	/**
	 * Intercepted println, assumes a Throwable is being sent to this
	 * PrintStream and logs an error with log.
	 */
	public void println(Object y) {
		System.setOut(out);

		try {
			log.error("", (Throwable)y);
		} catch (ClassCastException cce) {
			//This is the risk, but I wouldn't know what else might be thrown at this
		}

		System.setOut(this);
		super.println(y);
	}
	
	/**
	 * Intercepted println, logs the accual println as info to mvn.
	 */
	public void println(String y) {
		System.setOut(out);

		log.info(y);

		System.setOut(this);
		super.println(y);
	}
	
	/**
	 * Intercepted print, logs lines starting with 'ERROR:' as an error and
	 * may log particularly interresting lines to info.
	 * All other lines are logged as debug, running mvn with -X will show all
	 * output from JSDoc Toolkit.
	 */
	public void print(String y) {
		System.setOut(out);

		if (y.startsWith(OUTPUT_ERROR)) {
			log.error("JSDoc Toolkit:" + y.substring(OUTPUT_ERROR.length()));
		} else if (y.startsWith(OUTPUT_WARN)){
			log.warn("JSDoc Toolkit:" + y.substring(OUTPUT_WARN.length()));
		} else {
			Matcher m = regexp.matcher(y);
			if (m.find()) { //Test for line that tells us how many files were found.
				log.info(m.group(0) + '.');
				if ("0".equals(m.group(1))) {
					log.info("A common reason why no files are found is when recursion is set too low, please see documentation.");
				}
			} else log.debug(y);
		}

		System.setOut(this);
		super.print(y);
	}

}
