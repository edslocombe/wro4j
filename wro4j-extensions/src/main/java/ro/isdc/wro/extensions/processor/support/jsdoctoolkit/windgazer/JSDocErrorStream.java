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

import org.slf4j.Logger;

import java.io.OutputStream;
import java.io.PrintStream;


/**
 * In an attempt to keep mvn output uncluttered this PrintStream is
 * intended to divert all errors from JSDoc Toolkit to a seperate
 * OutputStream.
 * Throwables are redirected to standard output using log.debug, which
 * is expected to have it's own diverted PrintStream. 
 * @author mreuring
 */
public class JSDocErrorStream extends PrintStream {

	private Logger log;
	private PrintStream out;
	private PrintStream alt;

	/**
	 * This is the constructor for creating a PrintStream for diverting
	 * errors from JSDoc Toolkit to a seperate OutputStream.
	 * 
	 * @param os The OutputStream to which all errors are diverted.
	 * @param log The mvn log to which throwables can be debugged.
	 * @param out The original System.out, as it is assumed to be diverted as well.
	 * @param alt The alternate System.out, which is assumed to be in place by default.
	 */
	public JSDocErrorStream(OutputStream os, Logger log, PrintStream out, PrintStream alt) {
		super(os);
		this.log = log;
		this.out = out;
	}
	
	/**
	 * Diverted println, expected to receive a Throwable and logging it to debug.
	 */
	public void println(Object y) {
		if (log.isDebugEnabled()) {
			System.setOut(out);
			try {
				log.debug("", (Throwable)y);
			} catch (ClassCastException cce) {
				//This is the risk, but I wouldn't know what else might be thrown at this
			}
			System.setOut(alt);
		}
		
		super.println(y);
	}

}
