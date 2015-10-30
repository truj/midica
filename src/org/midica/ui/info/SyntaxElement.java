/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.info;


/**
 * Objects of this class represent either a MidicaPL syntax element (keyword or symbol)
 * or a category of syntax elements.
 * 
 * @author Jan Trukenmüller
 */
public class SyntaxElement extends CategorizedElement {
	
	/** Syntax identifier (or category name if the element is a category) */
	public String name;
	/** Keyword (ignored if it's a category) */
	public String keyword;
	/** Description of the keyword (ignored if it's a category) */
	public String description;
	
	/**
	 * Creates a new syntax element or syntax category with the given parameters
	 * as it's properties.
	 * 
	 * @param name         syntax identifier (or category name if the element is a category)
	 * @param description  description of the keyword (ignored if it's a category)
	 * @param keyword      MidicaPL keyword or symbol (ignored if it's a category)
	 * @param category     true if a category shall be created -- false if it's a normal
	 *                     syntax element
	 */
	public SyntaxElement( String name, String description, String keyword, boolean category ) {
		this.name        = name;
		this.keyword     = keyword;
		this.description = description;
		super.category   = category;
	}
}
