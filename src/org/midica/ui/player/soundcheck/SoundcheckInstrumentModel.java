/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.midica.config.Dict;
import org.midica.file.read.SoundfontParser;
import org.midica.ui.model.MidicaTableModel;
import org.midica.ui.tablesorter.BankNumber;
import org.midica.ui.tablesorter.OptionalNumber;

/**
 * This class provides the model for the instruments table in the soundcheck
 * window.
 * 
 * The instruments from the currently loaded soundbank are displayed only if
 * they support the selected channel.
 * 
 * If chromatic instruments are supported, the according sub categories are
 * displayed as well.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckInstrumentModel extends MidicaTableModel {
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<HashMap<String, String>> instrumentList = new ArrayList<HashMap<String, String>>();
	
	private TreeMap<Integer, String> programToSubCat = new TreeMap<Integer, String>();
	private HashMap<String, Boolean> isSubCatUsed    = new HashMap<String, Boolean>();
	
	/**
	 * Creates a model for the instruments table.
	 */
	public SoundcheckInstrumentModel() {
		
		// table header
		columnNames = new String[ 4 ];
		columnNames[ 0 ] = Dict.get( Dict.SNDCHK_COL_PROGRAM     );
		columnNames[ 1 ] = Dict.get( Dict.SNDCHK_COL_BANK        );
		columnNames[ 2 ] = Dict.get( Dict.SNDCHK_COL_NAME_SB     );
		columnNames[ 3 ] = Dict.get( Dict.SNDCHK_COL_NAME_SYNTAX );
		
		columnClasses = new Class[ 4 ];
		columnClasses[ 0 ] = OptionalNumber.class;
		columnClasses[ 1 ] = BankNumber.class;
		columnClasses[ 2 ] = String.class;
		columnClasses[ 3 ] = String.class;
		
		// init sub categories for chromatic instruments
		initSubCategories();
	}
	
	/**
	 * Builds up the structure of available soundbank instruments and/or
	 * drumkits that support the currently selected channel.
	 * 
	 * This is called whenever the channel selection changes.
	 */
	public void initList() {
		
		// (re-)initialize
		instrumentList.clear();
		for (String subCatName : isSubCatUsed.keySet())
			isSubCatUsed.put(subCatName, false);
		
		// get selected channel
		int channel = SoundcheckController.getChannel();
		
		// get instruments from the soundbank
		ArrayList<HashMap<String, String>> sbInstruments = SoundfontParser.getSoundbankInstruments();
		
		// process instruments
		HashMap<String, String> currentMainCategory = null;
		String currentMainCategoryType = null;
		INSTRUMENT:
		for (HashMap<String, String> sbInstr : sbInstruments) {
			
			// main category
			if (sbInstr.containsKey("category")) {
				
				// Do not yet add the category to the list because we don't yet
				// know if it contains instruments that are supported for the
				// current channel.
				currentMainCategory     = sbInstr;
				currentMainCategoryType = sbInstr.get("type");
				continue INSTRUMENT;
			}
			
			// instrument supported by the selected channel?
			boolean isSupported = isChannelSupported(channel, sbInstr);
			if (! isSupported) {
				
				// unknown category: probably java version > 8
				if ("category_unknown".equals(currentMainCategoryType)) {
					// don't ignore - otherwise the soundcheck will not work at all
				}
				else {
					continue INSTRUMENT;
				}
			}
			
			// add last remembered main category, if not yet done
			if (currentMainCategory != null) {
				instrumentList.add(currentMainCategory);
				currentMainCategory = null;
			}
			
			// add sub category, if chromatic and not yet done
			if ("chromatic".equals(sbInstr.get("type"))) {
				
				// get sub category name
				Integer program    = Integer.parseInt(sbInstr.get("program"));
				String  subCatName = programToSubCat.get(program);
				
				// sub category not yet added?
				if (! isSubCatUsed.get(subCatName)) {
					// create sub category, add it, and mark it as added
					HashMap<String, String> subCat = new HashMap<String, String>();
					subCat.put( "category", "sub"       );
					subCat.put( "name",      subCatName );
					instrumentList.add(subCat);
					isSubCatUsed.put(subCatName, true);
				}
			}
			
			// add instrument or drumkit
			instrumentList.add(sbInstr);
		}
		
		// tell the table that it's data has changed.
		super.fireTableDataChanged();
	}
	
	/**
	 * Determins if the given channel is supported by the given instrument.
	 * 
	 * @param channel  MIDI channel
	 * @param instr    Instrument structure as it is parsed from the soundbank.
	 * @return   **true** if the channel is supported; otherwise **false**.
	 */
	private static boolean isChannelSupported(int channel, HashMap<String, String> instr) {
		
		String[] channels = instr.get("channels_long").split(",");
		for (String chan : channels)
			if (Integer.parseInt(chan) == channel)
				return true;
		
		return false;
	}
	
	@Override
	public int getRowCount() {
		if (null == instrumentList)
			return 0;
		return instrumentList.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		
		// get row structure
		HashMap<String, String> rowObj = instrumentList.get(row);
		
		// category?
		if (rowObj.containsKey("category")) {
			
			if (0 == col) {
				return new OptionalNumber("");
			}
			
			else if (1 == col) {
				return new BankNumber(-1, "");
			}
			
			// display sub category in the syntax column
			else if (3 == col) {
				if ("sub".equals(rowObj.get("category")))
					return rowObj.get("name");
			}
			
			// display main category in the soundbank name column
			else if (2 == col && "category".equals(rowObj.get("category"))) {
				return rowObj.get("name");
			}
			
			return ""; // fallback
		}
		
		// instrument or drumkit
		if (0 == col) {
			// program
			return new OptionalNumber(rowObj.get("program"));
		}
		else if (1 == col) {
			// bank
			int    fullBankNum = Integer.parseInt(rowObj.get("bank"));
			String display     = rowObj.get("bank_msb") + Dict.getSyntax(Dict.SYNTAX_BANK_SEP) + rowObj.get("bank_lsb");
			return new BankNumber(fullBankNum, display);
		}
		else if (2 == col) {
			// soundbank name
			return rowObj.get("name");
		}
		else if (3 == col) {
			// syntax
			return rowObj.get("syntax");
		}
		
		// fallback
		return "";
	}
	
	@Override
	public ArrayList<HashMap<String, ?>> getCategorizedHashMapRows() {
		return new ArrayList<HashMap<String, ?>>(instrumentList);
	}
	
	/**
	 * Returns the current list of instruments to be displayed in the table.
	 * 
	 * @return instruments and categories
	 */
	public ArrayList<HashMap<String, String>> getInstruments() {
		return instrumentList;
	}
	
	/**
	 * Initializes the data structures for sub categories
	 * of chromatic instruments.
	 */
	private void initSubCategories() {
		
		// program --> sub category name
		programToSubCat.clear();
		for (int i = 0; i <= 7; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_PIANO));
		for (int i = 8; i <= 15; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_CHROM_PERC));
		for (int i = 16; i <= 23; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_ORGAN));
		for (int i = 24; i <= 31; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_GUITAR));
		for (int i = 32; i <= 39; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_BASS));
		for (int i = 40; i <= 47; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_STRINGS));
		for (int i = 48; i <= 55; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_ENSEMBLE));
		for (int i = 56; i <= 63; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_BRASS));
		for (int i = 64; i <= 71; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_REED));
		for (int i = 72; i <= 79; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_PIPE));
		for (int i = 80; i <= 87; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_SYNTH_LEAD));
		for (int i = 88; i <= 95; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_SYNTH_PAD));
		for (int i = 96; i <= 103; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_SYNTH_EFFECTS));
		for (int i = 104; i <= 111; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_ETHNIC));
		for (int i = 112; i <= 119; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_PERCUSSIVE));
		for (int i = 120; i <= 127; i++)
			programToSubCat.put(i, Dict.get(Dict.INSTR_CAT_SOUND_EFFECTS));
		
		// sub category usage
		isSubCatUsed.clear();
		for (String subCatName : programToSubCat.values())
			if (! isSubCatUsed.containsKey(subCatName))
				isSubCatUsed.put(subCatName, false);
	}
}
