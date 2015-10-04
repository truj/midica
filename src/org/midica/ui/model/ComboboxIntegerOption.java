///*
// * This Source Code Form is subject to the terms of the
// * Mozilla Public License, v. 2.0. 
// * If a copy of the MPL was not distributed with this file,
// * You can obtain one at http://mozilla.org/MPL/2.0/.
// */
//
//package org.midica.ui.model;
//
//import java.util.ArrayList;
//
///**
// * TODO: this class is not yet used. Maybe delete it.
// * 
// * This class represents options of a combobox.
// * 
// * Each option is either:
// * 
// * - A combination of an integer (value) and an according string (display text); or
// * - A category as a grouping element for other options.
// * 
// * @author Jan Trukenmüller
// */
//public class ComboboxIntegerOption {
//	
//	private int     number;
//	private String  text     = null;
//	private boolean category = false;
//	
//	/**
//	 * 
//	 * TODO: document
//	 * 
//	 * @param number
//	 * @param text
//	 * @param category
//	 */
//	public ComboboxIntegerOption( int number, String text, boolean category ) {
//		this.number   = number;
//		this.category = category;
//		setText( text );
//	}
//	
//	public int getNumber() {
//		return number;
//	}
//	
//	public void setText( String text ) {
//		this.text = text;
//	}
//	
//	public String toString() {
//		return text;
//	}
//	
//	// TODO: reactivate if needed
////	public static ComboboxIntegerOption getOptionByNumber( int number, ArrayList<ComboboxIntegerOption> options ) {
////		for ( ComboboxIntegerOption option : options ) {
////			if ( option.getNumber() == number ) {
////				return option;
////			}
////		}
////		return null;
////	}
//}
