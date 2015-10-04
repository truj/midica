/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

/**
 * This class can be extended by categorizable list element classes.
 * An object of a derived class can either be a category or a plain element which belongs
 * to a category.
 * 
 * @author Jan Trukenmüller
 */
public abstract class CategorizedElement {
	
	/** Indicates if the element is a category (true) or a plain syntax element (false) */
	protected boolean category;
	
	/**
	 * Indicates if the element is a category or a plain element.
	 * 
	 * @return true, if the element is a category -- otherwise: false
	 */
	public boolean isCategory() {
		return category;
	}
}
