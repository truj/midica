/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablesorter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.RowFilter;

import org.midica.ui.info.InfoView;
import org.midica.ui.model.MessageTableModel;
import org.midica.ui.model.MessageTreeNode;
import org.midica.ui.model.SingleMessage;

/**
 * This class provides table filtering (and sorting) for message tables.
 * 
 * The sorting functionality is implemented by the parent class.
 * This class adds message table specific filtering.
 * 
 * @author Jan Trukenmüller
 * @param <M> type of the model (MidicaTableModel)
 */
public class MessageTableSorter<M> extends MidicaSorter<M> {
	
	private int channel = -1;
	
	private ArrayList<RowFilter<MessageTableModel, Integer>> andFilters     = new ArrayList<>();
	private ArrayList<RowFilter<MessageTableModel, Integer>> channelFilters = new ArrayList<>();
	
	private HashMap<String, Boolean>   filterBoolean = null;
	private ArrayList<MessageTreeNode> filterNodes   = null;
	private HashSet<Integer>           filterTracks  = null;
	private long                       filterFrom    = 0;
	private long                       filterTo      = 0;
	
	/**
	 * Sets a new set of message filters.
	 * The only filter type which is **not** set here is the string filter.
	 * This is done by setStringFilter() in the parent class.
	 * 
	 * @param filterBoolean  Contains checkbox filters.
	 * @param filterNodes    The selected nodes from the message tree.
	 * @param filterFrom     Minimum tick number.
	 * @param filterTo       Maximum tick number.
	 * @param filterTracks   The tracks to be shown.
	 */
	public void setMessageFilters(HashMap<String, Boolean> filterBoolean,
			ArrayList<MessageTreeNode> filterNodes,
			long filterFrom, long filterTo, HashSet<Integer> filterTracks) {
		
		this.filterBoolean = filterBoolean;
		this.filterNodes   = filterNodes;
		this.filterTracks  = filterTracks;
		this.filterFrom    = filterFrom;
		this.filterTo      = filterTo;
	}
	
	/**
	 * Applies all filters.
	 * This includes the filters set by setMessageFilters() as well as
	 * the string filter set by setStringFilter() in the parent class.
	 */
	public void filter() {
		
		// No "normal" filter set yet?
		// Then this is triggered by the string filter.
		// Quick exit here in order to avoid null pointer exceptions.
		if (null == filterBoolean) {
			
			// string filter set?
			if ( ! filterStr.contentEquals("") ) {
				setRowFilter( RowFilter.regexFilter(filterStr) );
			}
			return;
		}
		
		// empty old filters
		andFilters.clear();
		channelFilters.clear();
		
		// show channel-independent messages?
		boolean isFltrIndep = filterBoolean.get( InfoView.FILTER_CBX_CHAN_INDEP );
		if (isFltrIndep) {
			channelFilters.add(new RowFilter<MessageTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends MessageTableModel, ? extends Integer> entry) {
					Integer channel = (Integer) entry.getModel().getMsg( entry.getIdentifier() ).getOption( SingleMessage.OPT_CHANNEL );
					if (null == channel)
						return true;
					return false;
				}
			});
		}
		
		// show channel messages?
		for ( channel = 0; channel < 16; channel++ ) {
			String name             = InfoView.FILTER_CBX_CHAN_PREFIX + channel;
			boolean mustShowChannel = filterBoolean.get( name );
			if (mustShowChannel) {
				channelFilters.add(new RowFilter<MessageTableModel, Integer>() {
					
					// needs to be copied to be able to use more than one channel filters at a time
					private final int filteredChannel = channel;
					
					@Override
					public boolean include(Entry<? extends MessageTableModel, ? extends Integer> entry) {
						Integer channel = (Integer) entry.getModel().getMsg( entry.getIdentifier() ).getOption( SingleMessage.OPT_CHANNEL );
						if (null == channel)
							return false;
						if (channel == filteredChannel)
							return true;
						return false;
					}
				});
			}
		}
		
		// combine channel and channel-independent filters
		andFilters.add( RowFilter.orFilter(channelFilters) );
		
		// apply limit-ticks filter
		boolean limitTicks = filterBoolean.get( InfoView.FILTER_CBX_LIMIT_TICKS  );
		if (limitTicks) {
			andFilters.add(new RowFilter<MessageTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends MessageTableModel, ? extends Integer> entry) {
					long tick = (long) entry.getModel().getMsg( entry.getIdentifier() ).getOption( SingleMessage.OPT_TICK );
					if (tick < filterFrom)
						return false;
					if (tick > filterTo)
						return false;
					return true;
				}
			});
		}
		
		// apply node filter
		boolean mustFilterNodes = filterBoolean.get( InfoView.FILTER_CBX_NODE );
		if (mustFilterNodes) {
			andFilters.add(new RowFilter<MessageTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends MessageTableModel, ? extends Integer> entry) {
					
					// get leaf node of the message
					MessageTreeNode leaf = (MessageTreeNode) entry.getModel().getMsg( entry.getIdentifier() ).getOption( SingleMessage.OPT_LEAF_NODE );
					
					// check if the leaf node is a descendant of one of the selected nodes
					for ( MessageTreeNode node : filterNodes ) {
						if ( leaf.isNodeAncestor(node) ) {
							return true;
						}
					}
					return false;
				}
			});
		}
		
		// apply limit-tracks filter
		boolean limitTracks = filterBoolean.get( InfoView.FILTER_CBX_LIMIT_TRACKS );
		if (limitTracks) {
			andFilters.add(new RowFilter<MessageTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends MessageTableModel, ? extends Integer> entry) {
					Integer track = (Integer) entry.getModel().getMsg( entry.getIdentifier() ).getOption( SingleMessage.OPT_TRACK );
					if ( filterTracks.contains(track) )
						return true;
					return false;
				}
			});
		}
		
		// apply string filter
		if ( ! filterStr.contentEquals("") ) {
			andFilters.add( RowFilter.regexFilter(filterStr) );
		}
		
		// set resulting filter
		setRowFilter(RowFilter.andFilter(andFilters));
	}
}
