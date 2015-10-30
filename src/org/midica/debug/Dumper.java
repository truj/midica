/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.debug;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 * Contains a method to dump the content of a complex data structure as a JSON string.
 * 
 * Requires XStream:
 * http://x-stream.github.io/download.html
 * 
 * @author Jan Trukenmüller
 */
public class Dumper {
	
	/**
	 * Returns a JSON string from the given data structure.
	 * 
	 * @param o  The data structure to be dumped.
	 * 
	 * @return the JSON string.
	 */
	public static String dump( Object o ) {
		if ( null == o )
			return "null";
		
		XStream dumper = new XStream( new JsonHierarchicalStreamDriver() );
		return dumper.toXML( o );
	}
	
	/**
	 * Prints a JSON string from the given data structure to STDOUT.
	 * 
	 * @param o  The data structure to be dumped.
	 */
	public static void print( Object o ) {
		System.out.println( dump(o) );
	}
	
	/**
	 * Prints the given object (or it's toString() result) to STDOUT.
	 * 
	 * @param o  The data structure to be dumped.
	 */
	public static void printString( Object o ) {
		System.out.println( o );
	}
}
