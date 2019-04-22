/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.midica.config.Dict;
import org.midica.file.CharsetUtils;
import org.midica.ui.model.IMessageType;
import org.midica.ui.model.SingleMessage;
import org.midica.ui.model.MessageTreeNode;
import org.midica.ui.model.MidicaTreeModel;

import com.sun.media.sound.MidiUtils;

/**
 * This class is used to classify a message.
 * 
 * @author Jan Trukenmüller
 */
public class MessageClassifier {
	
	// node sorting
	private static final String MSG_LVL_1_SORT_VOICE   = "1";
	private static final String MSG_LVL_1_SORT_SYS_COM = "2";
	private static final String MSG_LVL_1_SORT_SYS_RT  = "3";
	private static final String MSG_LVL_1_SORT_META    = "4";
	
	/**
	 * Private constructor because this class is only used statically.
	 */
	private MessageClassifier() {
	}
	
	/**
	 * Retrieves general information from short messages.
	 * 
	 * Creates a path and adds it to the given tree model.
	 * Creates message details and adds them to the given messages list.
	 * Adds the leaf node of the created path to the created message.
	 * 
	 * @param msg           Short message
	 * @param tick          Tickstamp
	 * @param trackNum      Track number (beginning with 0).
	 * @param msgNum        Number of the message inside the track.
	 * @param messages      List of messages where the processed message will be added.
	 * @param msgTreeModel  Tree model where the created leaf note will be added.
	 * @throws ReflectiveOperationException if the message cannot be added to the tree model.
	 */
	public static void processShortMessage(ShortMessage msg, long tick, int trackNum, int msgNum,
		ArrayList<SingleMessage> messages, MidicaTreeModel msgTreeModel) throws ReflectiveOperationException {
		
		// prepare data structures
		ArrayList<String[]>      path            = new ArrayList<>();
		HashMap<Integer, Object> details         = new HashMap<>();
		HashMap<Integer, Object> distinctDetails = new HashMap<>();
		int    msgLength = msg.getLength();
		byte[] message   = msg.getMessage();
		int    cmd       = msg.getCommand();
		int    channel   = msg.getChannel();
		int    data1     = msg.getData1();
		int    data2     = msg.getData2();
		
		// level 1 node
		int     statusInt     = msg.getStatus();
		byte    statusByte    = (byte) statusInt;
		byte    statusBitmask = (byte) 0b1111_1000;
		boolean isSystemMsg   = false;
		if (((byte) 0b1111_1000) == (statusByte & statusBitmask)) {
			
			// system realtime: 1111_1XXX
			isSystemMsg      = true;
			String[] msgLvl1 = { MSG_LVL_1_SORT_SYS_RT, Dict.get(Dict.MSG1_SYSTEM_REALTIME), null };
			path.add( msgLvl1 );
		}
		else if (((byte) 0b1111_0000) == (statusByte & statusBitmask)) {
			
			// system common: 1111_0XXX
			isSystemMsg      = true;
			String[] msgLvl1 = { MSG_LVL_1_SORT_SYS_COM, Dict.get(Dict.MSG1_SYSTEM_COMMON), null };
			path.add( msgLvl1 );
		}
		else {
			// voice
			String[] msgLvl1 = { MSG_LVL_1_SORT_VOICE, Dict.get(Dict.MSG1_VOICE), null };
			path.add( msgLvl1 );
		}
		
		// level 2 nodes for system common & system realtime
		if (isSystemMsg) {
			String   statusID   = String.format( "%02X", statusByte );
			String   statusStr  = (statusByte & 0xFF) + "";
			String   statusText = getLvl2SystemMsgTxtByStatusByte( statusInt );
			String[] msgLvl2    = { statusID, statusText, statusStr };
			path.add( msgLvl2 );
		}
		
		// level 2 and 3 nodes for voice messages
		else {
			String   commandID   = String.format( "%02X", cmd );
			String   commandStr  = cmd + "";
			String   commandText = getLvl2VoiceMsgTxtByCommand( cmd );
			String[] msgLvl2     = { commandID, commandText, commandStr };
			path.add( msgLvl2 );
			
			// level 3 and 4 nodes (controllers)
			if (0xB0 == cmd) { // control change
				
				// There must be at least one data byte after the status byte.
				// Otherwise: unknown controller.
				if (msgLength > 1) {
					details.put( IMessageType.OPT_CONTROLLER, (byte) data1 );
				}
				else {
					// invalid data --> ctrlTxt: unknown
					data1 = -1;
				}
				
				// level 3
				byte    data1Byte    = (byte) data1;
				byte    ctrlBitmask  = (byte) 0b1101_1111; // 3rd bit: MSB or LSB
				boolean isRpnOrNrpn  = false;
				boolean isDataChange = false;
				if (data1 >= 0x62 && data1 <= 0x65) {
					// RPN or NRPN
					isRpnOrNrpn        = true;
					ctrlBitmask        = (byte) 0b1111_1110; // last bit: MSB or LSB
					String paramNumStr = String.format( "%02X", data2 );
					if (data1 >= 0x64) {
						distinctDetails.put( IMessageType.OPT_RPN, paramNumStr );
					}
					else {
						distinctDetails.put( IMessageType.OPT_NRPN, paramNumStr );
					}
				}
				else if ( 0x06 == data1       // data entry MSB
				       || 0x26 == data1       // data entry LSB
				       || 0x60 == data1       // data button increment
				       || 0x61 == data1 ) {   // data button decrement
					isDataChange = true;
				}
				byte     ctrlPart = (byte) ( data1Byte & ctrlBitmask ); // same like the MSB
				String   ctrlID   = String.format( "%02X", ctrlPart );
				String[] ctrlTxt  = getLvl34ControllerMsgTxtByData1( data1 );
				String   ctrlNum  = null;
				if (null == ctrlTxt[1]) {
					// no MSB/LSB or unknown
					ctrlID  = String.format( "%02X", data1 );
					ctrlNum = data1 + "";
				}
				String[] msgLvl3 = { ctrlID, ctrlTxt[0], ctrlNum };
				path.add( msgLvl3 );
				
				// level 4 (MSB or LSB)
				if (ctrlTxt[1] != null) {
					String   data1ID   = String.format("%02X", data1);
					String   msbLsbStr = data1 + "";
					String[] msgLvl4   = { data1ID, ctrlTxt[1], msbLsbStr };
					path.add( msgLvl4 );
				}
				
				// level 5 for (N)RPN (only MSB or LSB number)
				if (isRpnOrNrpn) {
					String [] msgLvl5 = {
						String.format("%02X", data2),        // ID
						"0x" + String.format("%02X", data2), // text
						data2 + ""                           // number
					};
					path.add( msgLvl5 );
				}
				
				// level 4 and 5 data entry/increment/decrement for RPN or NRPN
				if (isDataChange) {
					
					// get MSB, LSB and type (RPN/NRPN)
					Byte[]  paramMsbLsb = SequenceAnalyzer.getChannelParamMsbLsbType( (byte) channel, tick );
					byte msb  = paramMsbLsb[ 0 ];
					byte lsb  = paramMsbLsb[ 1 ];
					byte type = paramMsbLsb[ 2 ];
					
					// Put a new level 4 node (param name) between controller
					// name and MSB/LSB. So the old level 4 becomes level 5.
					String[] paramFields = getLvl4RpnNrpnDataTxt( msb, lsb, type );
					String   paramID     = String.format( "%02X%02X", msb, lsb );
					String   paramTxt    = paramFields[ 0 ];
					String   paramStr    = paramFields[ 1 ];
					String[] msgLvl4     = { paramID, paramTxt, paramStr };
					path.add( 3, msgLvl4 ); // index 3 == level 4
				}
			}
		}
		
		// get general details
		if (isSystemMsg) {
			details.put( IMessageType.OPT_STATUS_BYTE, String.format("%02X", statusByte) );
		}
		else {
			// status byte contains command and channel
			byte cmdByte     = (byte) ( cmd & 0b1111_0000 );
			String cmdHex    = String.format( "%01X", cmdByte );
			cmdHex           = cmdHex.substring( 0, 1 );
			String statusStr = cmdHex + "0-" + cmdHex + "F";
			details.put( IMessageType.OPT_STATUS_BYTE, statusStr );
			distinctDetails.put( IMessageType.OPT_CHANNEL, channel );
		}
		details.put( IMessageType.OPT_LENGTH,  msgLength );
		details.put( IMessageType.OPT_TICK,    tick      );
		details.put( IMessageType.OPT_MSG_NUM, msgNum    );
		distinctDetails.put( IMessageType.OPT_TRACK, trackNum );
		
		// add message to the data structures
		MessageTreeNode leaf          = (MessageTreeNode) msgTreeModel.add(path);
		SingleMessage   singleMessage = new SingleMessage();
		messages.add(singleMessage);
		
		// add details to the leaf node and the message details
		for (Entry<Integer, Object> detailEntry : details.entrySet()) {
			leaf.setOption(          detailEntry.getKey(), detailEntry.getValue() );
			singleMessage.setOption( detailEntry.getKey(), detailEntry.getValue() );
		}
		for (Entry<Integer, Object> detailEntry : distinctDetails.entrySet()) {
			leaf.setDistinctOption(  detailEntry.getKey(), (Comparable<?>) detailEntry.getValue() );
			singleMessage.setOption( detailEntry.getKey(), detailEntry.getValue() );
		}
		singleMessage.setOption( IMessageType.OPT_STATUS_BYTE, String.format("%02X", statusByte) );
		singleMessage.setOption( IMessageType.OPT_LEAF_NODE, leaf );
		singleMessage.setOption( IMessageType.OPT_MESSAGE, message );
	}
	
	/**
	 * Retrieves general information from meta messages.
	 * 
	 * Creates a path and adds it to the given tree model.
	 * Creates message details and adds them to the given messages list.
	 * Adds the leaf node of the created path to the created message.
	 * 
	 * @param msg              Meta message
	 * @param tick             Tickstamp
	 * @param trackNum         Track number (beginning with 0).
	 * @param msgNum           Number of the message inside the track.
	 * @param messages         List of messages where the processed message will be added.
	 * @param msgTreeModel     Tree model where the created leaf note will be added.
	 * @param chosenCharset    Charset that has been chosen with the file chooser.
	 * @param midiFileCharset  The last charset from a charset switch inside of a lyrics message.
	 * @throws ReflectiveOperationException if the message cannot be added to the tree model.
	 */
	public static void processMetaMessage(MetaMessage msg, long tick, int trackNum, int msgNum,
		ArrayList<SingleMessage> messages, MidicaTreeModel msgTreeModel,
		String chosenCharset, String midiFileCharset) throws ReflectiveOperationException {
		
		// prepare data structures for the message tree
		ArrayList<String[]>      path            = new ArrayList<>();
		HashMap<Integer, Object> details         = new HashMap<>();
		HashMap<Integer, Object> distinctDetails = new HashMap<>();
		int    type      = msg.getType();
		int    msgLength = msg.getLength();
		byte[] message   = msg.getMessage();
		byte[] content   = msg.getData();
		String text      = null;
		
		// TEMPO
		if (MidiListener.META_SET_TEMPO == type) {
			int mpq = MidiUtils.getTempoMPQ( msg );
			int bpm = (int) MidiUtils.convertTempo( mpq );
			distinctDetails.put( IMessageType.OPT_TEMPO_MPQ, mpq );
			distinctDetails.put( IMessageType.OPT_TEMPO_BPM, bpm );
		}
		
		// level 1 node
		String[] msgLvl1 = { MSG_LVL_1_SORT_META, Dict.get(Dict.MSG1_META), null };
		path.add( msgLvl1 );
		
		// level 2 node
		String   typeID  = String.format( "%02X", type );
		String   typeTxt = getLvl2MetaText( type );
		String   typeStr = type + "";
		String[] msgLvl2 = { typeID, typeTxt, typeStr };
		path.add( msgLvl2 );
		
		// level 3 node - vendor for sequencer specific messages
		if (MidiListener.META_SEQUENCER_SPECIFIC == type) {
			String vendorID  = "FF"; // sort invalid vendor IDs to the end
			String vendorHex = "-";
			String vendorStr = "-";
			
			// vendor byte: 1st 1 or 3 bytes after: [status byte, type byte, length byte]
			if (msgLength >= 4) {
				int vendorByte = (byte) content[ 0 ];
				
				// 3-byte vendor ID
				if (0x00 == vendorByte) {
					if (msgLength >= 6) {
						vendorID  = String.format( "3-%02X%02X%02X", vendorByte, content[1], content[2] );
						vendorHex = String.format( "%02X %02X %02X", vendorByte, content[1], content[2] );
						vendorStr = String.format(   "%02X%02X%02X", vendorByte, content[1], content[2] );
					}
				}
				
				// 1-byte vendor ID
				else {
					vendorID  = String.format( "1-0000%02X", vendorByte );
					vendorHex = String.format( "%02X", vendorByte );
					vendorStr = vendorHex;
				}
			}
			String   vendorTxt = getVendorName( vendorStr );
			String[] msgLvl4   = { vendorID, vendorTxt, "0x" + vendorStr };
			path.add( msgLvl4 );
			details.put( IMessageType.OPT_VENDOR_ID,   vendorHex );
			details.put( IMessageType.OPT_VENDOR_NAME, vendorTxt );
		}
		
		// get general details
		details.put( IMessageType.OPT_STATUS_BYTE, "FF"       ); // status byte for META
		details.put( IMessageType.OPT_LENGTH,      msgLength  );
		details.put( IMessageType.OPT_TICK,        tick       );
		details.put( IMessageType.OPT_META_TYPE,   type       );
		boolean msgContainsText = type >= 0x01 && type <= 0x0F;
		if (msgContainsText) { // get texts from text-based messages
			if (null == text) {
				text = CharsetUtils.getTextFromBytes( content, chosenCharset, midiFileCharset );
			}
			distinctDetails.put( IMessageType.OPT_TEXT, text );
		}
		details.put( IMessageType.OPT_MSG_NUM, msgNum );
		distinctDetails.put( IMessageType.OPT_TRACK, trackNum );
		
		// add message to the data structures
		MessageTreeNode leaf          = (MessageTreeNode) msgTreeModel.add(path);
		SingleMessage   singleMessage = new SingleMessage();
		messages.add(singleMessage);
		
		// add details to the leaf node and the message details
		for (Entry<Integer, Object> detailEntry : details.entrySet()) {
			leaf.setOption(          detailEntry.getKey(), detailEntry.getValue() );
			singleMessage.setOption( detailEntry.getKey(), detailEntry.getValue() );
		}
		for (Entry<Integer, Object> detailEntry : distinctDetails.entrySet()) {
			leaf.setDistinctOption(  detailEntry.getKey(), (Comparable<?>) detailEntry.getValue() );
			singleMessage.setOption( detailEntry.getKey(), detailEntry.getValue() );
		}
		singleMessage.setOption( IMessageType.OPT_LEAF_NODE, leaf );
		singleMessage.setOption( IMessageType.OPT_MESSAGE, message );
	}
	
	/**
	 * Retrieves general information from SysEx messages.
	 * 
	 * Creates a path and adds it to the given tree model.
	 * Creates message details and adds them to the given messages list.
	 * Adds the leaf node of the created path to the created message.
	 * 
	 * @param msg           SysEx message
	 * @param tick          Tickstamp
	 * @param trackNum      Track number (beginning with 0).
	 * @param msgNum        Number of the message inside the track.
	 * @param messages      List of messages where the processed message will be added.
	 * @param msgTreeModel  Tree model where the created leaf note will be added.
	 * @throws ReflectiveOperationException if the message cannot be added to the tree model.
	 */
	public static void processSysexMessage(SysexMessage msg, long tick, int trackNum, int msgNum,
		ArrayList<SingleMessage> messages, MidicaTreeModel msgTreeModel) throws ReflectiveOperationException {
		
		// prepare data structures
		ArrayList<String[]>      path            = new ArrayList<>();
		HashMap<Integer, Object> details         = new HashMap<>();
		HashMap<Integer, Object> distinctDetails = new HashMap<>();
		int    msgLength = msg.getLength();
		byte[] message   = msg.getMessage();
		byte[] content   = msg.getData();
		
		// level 1 node - All SysEx messages are system common messages
		String[] msgLvl1 = { MSG_LVL_1_SORT_SYS_COM, Dict.get(Dict.MSG1_SYSTEM_COMMON), null };
		path.add( msgLvl1 );
		
		// level 2 node
		String   statusID  = String.format( "%02X", 0xF0 ); // 0xF0: status byte for SysEx
		String   statusTxt = Dict.get( Dict.MSG2_SC_SYSEX );
		String   statusStr = 0xF0 + "";
		String[] msgLvl2 = { statusID, statusTxt, statusStr };
		path.add( msgLvl2 );
		
		// level 3 node
		byte    vendorByte    = (byte) 0xFF; // invalid
		boolean isUniversal   = false;
		boolean isRealTime    = false;
		boolean isEducational = false;
		if (msgLength > 1) {
			vendorByte = content[ 0 ];
			
			// universal, non real time
			if (0x7E == vendorByte) {
				isUniversal        = true;
				String   vendorID  = "7E";
				String   vendorTxt = Dict.get( Dict.MSG3_SX_NON_RT_UNIVERSAL );
				String   vendorStr = vendorByte + "";
				String[] msgLvl3   = { vendorID, vendorTxt, vendorStr };
				path.add( msgLvl3 );
				details.put( IMessageType.OPT_VENDOR_ID,   vendorID  );
				details.put( IMessageType.OPT_VENDOR_NAME, vendorTxt );
			}
			
			// universal, real time
			else if (0x7F == vendorByte) {
				isUniversal        = true;
				isRealTime         = true;
				String   vendorID  = "7F";
				String   vendorTxt = Dict.get( Dict.MSG3_SX_RT_UNIVERSAL );
				String   vendorStr = vendorByte + "";
				String[] msgLvl3   = { vendorID, vendorTxt, vendorStr };
				path.add( msgLvl3 );
				details.put( IMessageType.OPT_VENDOR_ID,   vendorID  );
				details.put( IMessageType.OPT_VENDOR_NAME, vendorTxt );
			}
			
			// educational
			else if (0x7D == vendorByte) {
				isEducational      = true;
				String   vendorID  = "FE";  // sort educational after universal
				String   vendorTxt = Dict.get( Dict.MSG3_SX_EDUCATIONAL );
				String   vendorStr = vendorByte + "";
				String[] msgLvl3   = { vendorID, vendorTxt, vendorStr };
				path.add( msgLvl3 );
				details.put( IMessageType.OPT_VENDOR_ID,   "7D"      );
				details.put( IMessageType.OPT_VENDOR_NAME, vendorTxt );
			}
			
			// vendor specific
			else {
				String   vendorID  = "FF"; // sort vendor-specific after educational
				String   vendorTxt = Dict.get( Dict.MSG3_SX_VENDOR );
				String[] msgLvl3   = { vendorID, vendorTxt, null };
				path.add( msgLvl3 );
			}
		}
		
		// level 4 node for normal vendors (vendor name)
		if (! isUniversal && ! isEducational) {
			String vendorID  = null;
			String vendorHex = null;
			String vendorStr = null;
			
			// 3-byte vendor ID
			if (0x00 == vendorByte) {
				if (msgLength >= 4) {
					vendorID  = String.format( "3-%02X%02X%02X", vendorByte, content[1], content[2] );
					vendorHex = String.format( "%02X %02X %02X", vendorByte, content[1], content[2] );
					vendorStr = String.format(   "%02X%02X%02X", vendorByte, content[1], content[2] );
				}
			}
			
			// single-byte vendor ID
			else {
				vendorID  = String.format( "1-0000%02X", vendorByte );
				vendorHex = String.format( "%02X", vendorByte );
				vendorStr = vendorHex;
			}
			
			// invalid (no vendor ID found)
			if (null == vendorID || null == vendorHex || null == vendorStr) {
				vendorID  = "9-000000"; // sort invalid messages to the end
				vendorHex = "-";
				vendorStr = "-";
			}
			String   vendorTxt = getVendorName( vendorStr );
			String[] msgLvl4   = { vendorID, vendorTxt, "0x" + vendorStr };
			path.add( msgLvl4 );
			details.put( IMessageType.OPT_VENDOR_ID,   vendorHex );
			details.put( IMessageType.OPT_VENDOR_NAME, vendorTxt );
		}
		
		// level 4 and 5 nodes for universal vendors
		if (isUniversal) {
			
			// get channel and sub id 1 and 2
			int  restLength   = msgLength - 2; // without status byte and vendor byte
			byte sysExChannel = (byte) 0xFF; // invalid
			byte subId1       = (byte) 0xFF; // invalid
			byte subId2       = (byte) 0xFF; // invalid
			if (restLength >= 1) {
				sysExChannel = content[ 1 ];
			}
			if (restLength >= 2) {
				subId1 = content[ 2 ];
			}
			if (restLength >= 3) {
				subId2 = content[ 3 ];
			}
			
			// level 4 node
			String   mainTypeID  = String.format( "%02X", subId1 );
			String   mainTypeStr = ((byte) 0xFF) == subId1 ? "-" : subId1 + "";
			String[] texts       = getLvl45UniversalSysexTxt( isRealTime, subId1, subId2 );
			String[] msgLvl4     = { mainTypeID, texts[0], mainTypeStr };
			path.add( msgLvl4 );
			
			// level 5 node
			if (texts.length >= 3) {
				String   subTypeID  = String.format( "%02X", subId2 );
				String   subTypeStr = texts[ 2 ];
				if (! "-".equals(subTypeStr)) {  // real number?
					subTypeStr = subId2 + "";    // decimal instead of hex
				}
				String[] msgLvl5    = { subTypeID, texts[1], subTypeStr };
				path.add( msgLvl5 );
			}
			
			// add channel to details
			String sysExChannelID = "0x" + String.format( "%02X", sysExChannel );
			if (0x7F == sysExChannel) {
				sysExChannelID += " (" + Dict.get( Dict.BROADCAST_MSG ) + ")";
			}
			else if (((byte) 0xFF) == sysExChannel) {
				sysExChannelID = "-";
			}
			distinctDetails.put( IMessageType.OPT_SYSEX_CHANNEL, sysExChannelID );
			
			// add Sub-IDs to details
			String sub1Str = ((byte) 0xFF) == subId1 ? "-" : String.format( "0x%02X", subId1 );
			String sub2Str = "-";
			if (texts.length > 2 && ! "-".equals(texts[2])) {
				sub2Str = "0x" + texts[ 2 ];
			}
			details.put( IMessageType.OPT_SUB_ID_1, sub1Str );
			details.put( IMessageType.OPT_SUB_ID_2, sub2Str );
		}
		
		// get general details
		details.put( IMessageType.OPT_STATUS_BYTE,   "F0"      ); // status byte for SysEx
		details.put( IMessageType.OPT_LENGTH,        msgLength );
		details.put( IMessageType.OPT_TICK,          tick      );
		details.put( IMessageType.OPT_MSG_NUM,       msgNum    );
		distinctDetails.put( IMessageType.OPT_TRACK, trackNum  );
		
		// add message to the data structures
		MessageTreeNode leaf          = (MessageTreeNode) msgTreeModel.add(path);
		SingleMessage   singleMessage = new SingleMessage();
		messages.add(singleMessage);
		
		// add details to the leaf node and the message details
		for (Entry<Integer, Object> detailEntry : details.entrySet()) {
			leaf.setOption(          detailEntry.getKey(), detailEntry.getValue() );
			singleMessage.setOption( detailEntry.getKey(), detailEntry.getValue() );
		}
		for (Entry<Integer, Object> detailEntry : distinctDetails.entrySet()) {
			leaf.setDistinctOption(  detailEntry.getKey(), (Comparable<?>) detailEntry.getValue() );
			singleMessage.setOption( detailEntry.getKey(), detailEntry.getValue() );
		}
		singleMessage.setOption( IMessageType.OPT_LEAF_NODE, leaf );
		singleMessage.setOption( IMessageType.OPT_MESSAGE, message );
	}
	
	/**
	 * Returns the level 2 message name of system common and system realtime
	 * messages by status byte.
	 * 
	 * @param status Status byte of the message.
	 * @return Level 2 Message type.
	 */
	private static final String getLvl2SystemMsgTxtByStatusByte(int status) {
		
		// system common
		if (0xF1 == status)
			return Dict.get( Dict.MSG2_SC_MIDI_TIME_CODE );
		else if (0xF2 == status)
			return Dict.get( Dict.MSG2_SC_SONG_POS_POINTER );
		else if (0xF3 == status)
			return Dict.get( Dict.MSG2_SC_SONG_SELECT );
		else if (0xF6 == status)
			return Dict.get( Dict.MSG2_SC_TUNE_REQUEST );
		else if (0xF7 == status)
			return Dict.get( Dict.MSG2_SC_END_OF_SYSEX );
		
		// system realtime
		else if (0xF8 == status)
			return Dict.get( Dict.MSG2_SR_TIMING_CLOCK );
		else if (0xFA == status)
			return Dict.get( Dict.MSG2_SR_START );
		else if (0xFB == status)
			return Dict.get( Dict.MSG2_SR_CONTINUE );
		else if (0xFC == status)
			return Dict.get( Dict.MSG2_SR_STOP );
		else if (0xFE == status)
			return Dict.get( Dict.MSG2_SR_ACTIVE_SENSING );
		else if (0xFF == status)
			return Dict.get( Dict.MSG2_SR_SYSTEM_RESET );
		
		// fallback
		else
			return Dict.get( Dict.UNKNOWN );
	}
	
	/**
	 * Returns the level 2 message name of META messages.
	 * 
	 * @param type  Second byte of the message.
	 * @return Level 2 META message type.
	 */
	private static final String getLvl2MetaText(int type) {
		
		if (0x00 == type)
			return Dict.get( Dict.MSG2_M_SEQUENCE_NUMBER );
		if (0x01 == type)
			return Dict.get( Dict.MSG2_M_TEXT );
		if (0x02 == type)
			return Dict.get( Dict.MSG2_M_COPYRIGHT );
		if (0x03 == type)
			return Dict.get( Dict.MSG2_M_TRACK_NAME );
		if (0x04 == type)
			return Dict.get( Dict.MSG2_M_INSTRUMENT_NAME );
		if (0x05 == type)
			return Dict.get( Dict.MSG2_M_LYRICS );
		if (0x06 == type)
			return Dict.get( Dict.MSG2_M_MARKER );
		if (0x07 == type)
			return Dict.get( Dict.MSG2_M_CUE_POINT );
		if (0x20 == type)
			return Dict.get( Dict.MSG2_M_CHANNEL_PREFIX );
		if (0x2F == type)
			return Dict.get( Dict.MSG2_M_END_OF_SEQUENCE );
		if (0x51 == type)
			return Dict.get( Dict.MSG2_M_SET_TEMPO );
		if (0x54 == type)
			return Dict.get( Dict.MSG2_M_SMPTE_OFFSET );
		if (0x58 == type)
			return Dict.get( Dict.MSG2_M_TIME_SIGNATURE );
		if (0x59 == type)
			return Dict.get( Dict.MSG2_M_KEY_SIGNATURE );
		if (0x7F == type)
			return Dict.get( Dict.MSG2_M_SEQUENCER_SPEC );
		
		// fallback
		return Dict.get( Dict.UNKNOWN );
	}
	
	/**
	 * Returns the level 2 message name of channel voice messages
	 * by message command.
	 * 
	 * The command contains the first 4 bits of the status byte.
	 * 
	 * @param cmd  Command of the voice message.
	 * @return Level 2 Message type.
	 */
	private static final String getLvl2VoiceMsgTxtByCommand(int cmd) {
		
		if (0x80 == cmd)
			return Dict.get( Dict.MSG2_V_NOTE_OFF );
		else if (0x90 == cmd)
			return Dict.get( Dict.MSG2_V_NOTE_ON );
		else if (0xA0 == cmd)
			return Dict.get( Dict.MSG2_V_POLY_PRESSURE );
		else if (0xB0 == cmd)
			return Dict.get( Dict.MSG2_V_CONTROL_CHANGE );
		else if (0xC0 == cmd)
			return Dict.get( Dict.MSG2_V_PROGRAM_CHANGE );
		else if (0xD0 == cmd)
			return Dict.get( Dict.MSG2_V_CHANNEL_PRESSURE );
		else if (0xE0 == cmd)
			return Dict.get( Dict.MSG2_V_PITCH_BEND );
		
		// fallback
		else
			return Dict.get( Dict.UNKNOWN );
	}
	
	/**
	 * Returns the level 3 and 4 node names of control change messages.
	 * A level 4 node name is only added for controllers that have an MSB and LSB.
	 * 
	 * - Level 3: the controller
	 * - Level 4: MSB or LSB
	 * 
	 * The returned array consists of:
	 * 
	 * - level 3 text
	 * - level 4 text (or **null**, if the controller is unknown or doesn't have MSB/LSB)
	 * 
	 * @param val First data byte of the message.
	 * @return level 3 and 4 texts
	 */
	private static final String[] getLvl34ControllerMsgTxtByData1(int val) {
		String[] result = { null, null };
		
		// controller + MSB
		if (0x00 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_BANK_SELECT );
			result[ 1 ] = Dict.get( Dict.MSG4_C_BANK_SELECT_MSB );
		}
		else if (0x01 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_MODULATION_WHEEL );
			result[ 1 ] = Dict.get( Dict.MSG4_C_MODULATION_WHEEL_MSB );
		}
		else if (0x02 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_BREATH_CTRL );
			result[ 1 ] = Dict.get( Dict.MSG4_C_BREATH_CTRL_MSB );
		}
		else if (0x04 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_FOOT_CTRL );
			result[ 1 ] = Dict.get( Dict.MSG4_C_FOOT_CTRL_MSB );
		}
		else if (0x05 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_PORTAMENTO_TIME );
			result[ 1 ] = Dict.get( Dict.MSG4_C_PORTAMENTO_TIME_MSB );
		}
		else if (0x06 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_DATA_ENTRY );
			result[ 1 ] = Dict.get( Dict.MSG4_C_DATA_ENTRY_MSB );
		}
		else if (0x07 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_CHANNEL_VOL );
			result[ 1 ] = Dict.get( Dict.MSG4_C_CHANNEL_VOL_MSB );
		}
		else if (0x08 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_BALANCE );
			result[ 1 ] = Dict.get( Dict.MSG4_C_BALANCE_MSB );
		}
		else if (0x0A == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_PAN );
			result[ 1 ] = Dict.get( Dict.MSG4_C_PAN_MSB );
		}
		else if (0x0B == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EXPRESSION );
			result[ 1 ] = Dict.get( Dict.MSG4_C_EXPRESSION_MSB );
		}
		else if (0x0C == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_CTRL_1 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_EFFECT_CTRL_1_MSB );
		}
		else if (0x0D == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_CTRL_2 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_EFFECT_CTRL_2_MSB );
		}
		else if (0x10 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_1 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_GEN_PURP_CTRL_1_MSB );
		}
		else if (0x11 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_2 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_GEN_PURP_CTRL_2_MSB );
		}
		else if (0x12 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_3 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_GEN_PURP_CTRL_3_MSB );
		}
		else if (0x13 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_4 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_GEN_PURP_CTRL_4_MSB );
		}
		
		// controller + LSB
		else if (0x20 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_BANK_SELECT );
			result[ 1 ] = Dict.get( Dict.MSG4_C_BANK_SELECT_LSB );
		}
		else if (0x21 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_MODULATION_WHEEL );
			result[ 1 ] = Dict.get( Dict.MSG4_C_MODULATION_WHEEL_LSB );
		}
		else if (0x22 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_BREATH_CTRL );
			result[ 1 ] = Dict.get( Dict.MSG4_C_BREATH_CTRL_LSB );
		}
		else if (0x24 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_FOOT_CTRL );
			result[ 1 ] = Dict.get( Dict.MSG4_C_FOOT_CTRL_LSB );
		}
		else if (0x25 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_PORTAMENTO_TIME );
			result[ 1 ] = Dict.get( Dict.MSG4_C_PORTAMENTO_TIME_LSB );
		}
		else if (0x26 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_DATA_ENTRY );
			result[ 1 ] = Dict.get( Dict.MSG4_C_DATA_ENTRY_LSB );
		}
		else if (0x27 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_CHANNEL_VOL );
			result[ 1 ] = Dict.get( Dict.MSG4_C_CHANNEL_VOL_LSB );
		}
		else if (0x28 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_BALANCE );
			result[ 1 ] = Dict.get( Dict.MSG4_C_BALANCE_LSB );
		}
		else if (0x2A == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_PAN );
			result[ 1 ] = Dict.get( Dict.MSG4_C_PAN_LSB );
		}
		else if (0x2B == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EXPRESSION );
			result[ 1 ] = Dict.get( Dict.MSG4_C_EXPRESSION_LSB );
		}
		else if (0x2C == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_CTRL_1 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_EFFECT_CTRL_1_LSB );
		}
		else if (0x2D == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_CTRL_2 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_EFFECT_CTRL_2_LSB );
		}
		else if (0x30 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_1 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_GEN_PURP_CTRL_1_LSB );
		}
		else if (0x31 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_2 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_GEN_PURP_CTRL_2_LSB );
		}
		else if (0x32 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_3 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_GEN_PURP_CTRL_3_LSB );
		}
		else if (0x33 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_4 );
			result[ 1 ] = Dict.get( Dict.MSG4_C_GEN_PURP_CTRL_4_LSB );
		}
		
		// controllers without MSB / LSB
		else if (0x40 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_HOLD_PEDAL_1 );
		}
		else if (0x41 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_PORTAMENTO_PEDAL );
		}
		else if (0x42 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOSTENUTO_PEDAL );
		}
		else if (0x43 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOFT_PEDAL );
		}
		else if (0x44 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_LEGATO_PEDAL );
		}
		else if (0x45 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_HOLD_PEDAL_2 );
		}
		else if (0x46 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_1 );
		}
		else if (0x47 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_2 );
		}
		else if (0x48 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_3 );
		}
		else if (0x49 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_4 );
		}
		else if (0x4A == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_5 );
		}
		else if (0x4B == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_6 );
		}
		else if (0x4C == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_7 );
		}
		else if (0x4D == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_8 );
		}
		else if (0x4E == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_9 );
		}
		else if (0x4F == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_SOUND_CTRL_10 );
		}
		else if (0x50 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_5 );
		}
		else if (0x51 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_6 );
		}
		else if (0x52 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_7 );
		}
		else if (0x53 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_GEN_PURP_CTRL_8 );
		}
		else if (0x54 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_PORTAMENTO_CTRL );
		}
		else if (0x58 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_HI_RES_VELO_PRFX );
		}
		else if (0x5B == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_1_DEPTH );
		}
		else if (0x5C == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_2_DEPTH );
		}
		else if (0x5D == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_3_DEPTH );
		}
		else if (0x5E == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_4_DEPTH );
		}
		else if (0x5F == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_EFFECT_5_DEPTH );
		}
		else if (0x60 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_DATA_BUTTON_INCR );
		}
		else if (0x61 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_DATA_BUTTON_DECR );
		}
		
		// RPN / NRPN (with MSB/LSB)
		else if (0x62 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_NRPN );
			result[ 1 ] = Dict.get( Dict.MSG5_C_NRPN_LSB );
		}
		else if (0x63 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_NRPN );
			result[ 1 ] = Dict.get( Dict.MSG5_C_NRPN_MSB );
		}
		else if (0x64 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_RPN );
			result[ 1 ] = Dict.get( Dict.MSG5_C_RPN_LSB );
		}
		else if (0x65 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_RPN );
			result[ 1 ] = Dict.get( Dict.MSG5_C_RPN_MSB );
		}
		
		// more controllers without MSB / LSB
		else if (0x78 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_ALL_SOUND_OFF );
		}
		else if (0x79 == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_ALL_CTRLS_OFF );
		}
		else if (0x7A == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_LOCAL_CTRL );
		}
		else if (0x7B == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_ALL_NOTES_OFF );
		}
		else if (0x7C == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_OMNI_MODE_OFF );
		}
		else if (0x7D == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_OMNI_MODE_ON );
		}
		else if (0x7E == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_MONO_NOTES_OFF );
		}
		else if (0x7F == val) {
			result[ 0 ] = Dict.get( Dict.MSG3_C_POLY_NOTES_OFF );
		}
		
		// fallback
		else {
			result[ 0 ] = Dict.get( Dict.UNKNOWN );
		}
		
		return result;
	}
	
	/**
	 * Returns the level 4 message fields for the tree node of
	 * Data entry/increment/decrement messages.
	 * These messages affect the last set (N)RPN of the given
	 * channel.
	 * 
	 * The following strings are returned:
	 * 
	 * - Main text (Parameter name)
	 * - Parameter number (2 bytes as a comma-separated string).
	 * 
	 * @param msb    The MSB of the (N)RPN.
	 * @param lsb    The LSB of the (N)RPN.
	 * @param type   **1** for RPN, **0** for NRPN, **-1** for none
	 * @return  the strings needed for the tree node as described above.
	 */
	private static final String[] getLvl4RpnNrpnDataTxt(byte msb, byte lsb, byte type) {
		
		// set general values
		String paramTxt = Dict.get( Dict.UNKNOWN );
		String paramStr = String.format( msb + "," + lsb );
		
		// none
		if (-1 == type || 0x7F == msb && 0x7F == lsb) {
			paramTxt = Dict.get( Dict.UNSET );
		}
		
		// RPN
		if (1 == type) {
			if (0x00 == msb) {
				if (0x00 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_PITCH_BEND_SENS );
				else if (0x01 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_MASTER_FINE_TUN );
				else if (0x02 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_MASTER_COARSE_TUN );
				else if (0x03 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_TUN_PROG_CHANGE );
				else if (0x04 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_TUN_BANK_SELECT );
				else if (0x05 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_MOD_DEPTH_RANGE );
				else if (0x06 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_MPE_CONFIG );
			}
			else if (0x3D == msb) {
				if (0x00 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_AZIMUTH_ANGLE );
				else if (0x01 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_ELEVATION_ANGLE );
				else if (0x02 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_GAIN );
				else if (0x03 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_DISTANCE_RATIO );
				else if (0x04 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_MAXIMUM_DISTANCE );
				else if (0x05 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_GAIN_AT_MAX_DIST );
				else if (0x06 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_REF_DISTANCE_RATIO );
				else if (0x07 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_PAN_SPREAD_ANGLE );
				else if (0x08 == lsb)
					paramTxt = Dict.get( Dict.MSG4_RPN_ROLL_ANGLE );
			}
		}
		
		// put everything together
		String[] result = { paramTxt, paramStr };
		return result;
	}
	
	/**
	 * Returns the level 4 and 5 node names of universal SysEx messages.
	 * A level 5 node name is not always available - depending on the
	 * given **Sub-ID 1**.
	 * 
	 * - Level 4: SysEx main type, defined by **Sub-ID 1**.
	 * - Level 5: SysEx sub type, defined by **Sub-ID 2**.
	 * 
	 * The returned array consists of 1 or 3 elements:
	 * 
	 * - 1st element: level 4 text (main type)
	 * - 2nd element: level 5 text (sub type)
	 * - 3rd element: the given **Sub-ID 2** as a Hex value
	 * 
	 * The 2nd and 3rd element are only returned if **Sub-ID 1** allows
	 * a sub type.
	 * 
	 * @param isRealTime  **true** for Real Time messages, **false** for Non Real Time messages.
	 * @param sub1        **Sub-ID 1** (4th byte of the message)
	 * @param sub2        **Sub-ID 2** (5th byte of the message)
	 * @return level 4 and 5 text and Sub-ID 2, as described above.
	 */
	private static final String[] getLvl45UniversalSysexTxt(boolean isRealTime, byte sub1, byte sub2) {
		
		// init fallback
		boolean hasLvl5 = false;
		String  l4Txt   = Dict.get( Dict.UNKNOWN );
		String  l5Txt   = l4Txt;
		
		// invalid sub ID 1 (message too short)
		if (((byte) 0xFF) == sub1) {
			l4Txt = Dict.get( Dict.INVALID_MSG );
		}
		
		// real time
		if (isRealTime) {
			if (0x01 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_MIDI_TIME_CODE );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR1_FULL_MSG  );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR1_USER_BITS );
			}
			else if (0x02 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_MIDI_SHOW_CTRL );
				if      (0x00 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR2_MSC_EXT );
				else if (sub2 <= 0x7F) l5Txt = Dict.get( Dict.MSG5_SXR2_MSC_CMD );
			}
			else if (0x03 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_NOTATION_INFO );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR3_BAR_NUMBER       );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR3_TIME_SIG_IMMED   );
				else if (0x42 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR3_TIME_SIG_DELAYED );
			}
			else if (0x04 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_DEVICE_CTRL );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR4_MASTER_VOLUME     );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR4_MASTER_BALANCE    );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR4_MASTER_FINE_TUN   );
				else if (0x04 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR4_MASTER_COARSE_TUN );
				else if (0x05 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR4_GLOBAL_PARAM_CTRL );
			}
			else if (0x05 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_RT_MTC_CUEING );
				if      (0x00 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_SPECIAL           );
				else if (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_PUNCH_IN_PTS      );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_PUNCH_OUT_PTS     );
				else if (0x05 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_EVT_START_PT      );
				else if (0x06 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_EVT_STOP_PT       );
				else if (0x07 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_EVT_START_PTS_ADD );
				else if (0x08 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_EVT_STOP_PTS_ADD  );
				else if (0x0B == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_CUE_PTS           );
				else if (0x0C == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_CUE_PTS_ADD       );
				else if (0x0E == sub2) l5Txt = Dict.get( Dict.MSG5_SXR5_EVT_NAME_IN_ADD   );
			}
			else if (0x06 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_MACH_CTRL_CMD );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_STOP          );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_PLAY          );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_DEF_PLAY      );
				else if (0x04 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_FAST_FW       );
				else if (0x05 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_REWIND        );
				else if (0x06 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_REC_STROBE    );
				else if (0x07 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_REC_EXIT      );
				else if (0x08 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_REC_PAUSE     );
				else if (0x09 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_PAUSE         );
				else if (0x0A == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_EJECT         );
				else if (0x0B == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_CHASE         );
				else if (0x0C == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_CMD_ERR_RESET );
				else if (0x0D == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_MMC_RESET     );
				else if (0x40 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_WRITE         );
				else if (0x44 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_GOTO          );
				else if (0x47 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR6_SHUTTLE       );
			}
			else if (0x07 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_MACH_CTRL_RES );
				l5Txt   = Dict.get( Dict.MSG5_SXR7_MMC_RES        );
			}
			else if (0x08 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_TUNING_STANDARD );
				if      (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR8_SG_TUN_CH         );
				else if (0x07 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR8_SG_TUN_CH_BNK_SEL );
				else if (0x08 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR8_SO_TUN_1          );
				else if (0x09 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR8_SO_TUN_2          );
			}
			else if (0x09 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_RU_CTRL_DEST_SET );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR9_CHANNEL_PRESSURE  );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR9_POLY_KEY_PRESSURE );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXR9_CTRL              );
			}
			else if (0x0A == sub1 && 0x01 == sub2) {
				l4Txt = Dict.get( Dict.MSG4_SX_RU_KEY_B_INSTR_CTRL );
			}
			else if (0x0B == sub1 && 0x01 == sub2) {
				l4Txt = Dict.get( Dict.MSG4_SX_RU_SCAL_POLY_MIP );
			}
			else if (0x0C == sub1 && 0x00 == sub2) {
				l4Txt = Dict.get( Dict.MSG4_SX_RU_MOB_PHONE_CTRL );
			}
		}
		
		// non real time
		else {
			if (0x01 == sub1) {
				l4Txt = Dict.get( Dict.MSG4_SX_NU_SMPL_DUMP_HDR );
			}
			else if (0x02 == sub1) {
				l4Txt = Dict.get( Dict.MSG4_SX_NU_SMPL_DATA_PKT );
			}
			else if (0x03 == sub1) {
				l4Txt = Dict.get( Dict.MSG4_SX_NU_SMPL_DUMP_REQ );
			}
			else if (0x04 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_MIDI_TIME_CODE );
				if      (0x00 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_SPECIAL           );
				else if (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_PUNCH_IN_PTS      );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_PUNCH_OUT_PTS     );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_DEL_PUNCH_IN_PTS  );
				else if (0x04 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_DEL_PUNCH_OUT_PTS );
				else if (0x05 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_EVT_START_PT      );
				else if (0x06 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_EVT_STOP_PT       );
				else if (0x07 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_EVT_START_PTS_ADD );
				else if (0x08 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_EVT_STOP_PTS_ADD  );
				else if (0x09 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_DEL_EVT_START_PT  );
				else if (0x0A == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_DEL_EVT_STOP_PT   );
				else if (0x0B == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_CUE_PTS           );
				else if (0x0C == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_CUE_PTS_ADD       );
				else if (0x0D == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_DEL_CUE_PT        );
				else if (0x0E == sub2) l5Txt = Dict.get( Dict.MSG5_SXN4_EVT_NAME_IN_ADD   );
			}
			else if (0x05 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_SAMPLE_DUMP_EXT );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN5_LOOP_PTS_TRANSM  );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN5_LOOP_PTS_REQ     );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN5_SMPL_NAME_TRANSM );
				else if (0x04 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN5_SMPL_NAME_REQ    );
				else if (0x05 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN5_EXT_DUMP_HDR     );
				else if (0x06 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN5_EXT_LOOP_PTS_TR  );
				else if (0x07 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN5_EXT_LOOP_PTS_REQ );
			}
			else if (0x06 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_GENERAL_INFO );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN6_IDENTITY_REQ  );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN6_IDENTITY_REPL );
			}
			else if (0x07 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_FILE_DUMP );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN7_HEADER      );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN7_DATA_PACKET );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN7_REQUEST     );
			}
			else if (0x08 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_TUNING_STANDARD );
				if      (0x00 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_BLK_DUMP_REQ      );
				else if (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_BLK_DUMP_REPL     );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_TUNING_DUMP_REQ   );
				else if (0x04 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_KEY_B_TUNING_DMP  );
				else if (0x05 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_SO_TUN_DMP_1      );
				else if (0x06 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_SO_TUN_DMP_2      );
				else if (0x07 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_SG_TUN_CH_BNK_SEL );
				else if (0x08 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_SO_TUN_1          );
				else if (0x09 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN8_SO_TUN_2          );
			}
			else if (0x09 == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_GENERA_MIDI );
				if      (0x00 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN9_GM_DISABLE );
				else if (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN9_GM_1_ON    );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN9_GM_OFF     );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXN9_GM_2_ON    );
			}
			else if (0x0A == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_DOWNLOADABLE_SND );
				if      (0x00 == sub2) l5Txt = Dict.get( Dict.MSG5_SXNA_DLS_ON     );
				else if (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXNA_DLS_OFF    );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXNA_DLS_VA_OFF );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXNA_DLS_VA_ON  );
			}
			else if (0x0B == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_FILE_REF_MSG );
				if      (0x01 == sub2) l5Txt = Dict.get( Dict.MSG5_SXNB_OPEN_FILE      );
				else if (0x02 == sub2) l5Txt = Dict.get( Dict.MSG5_SXNB_SEL_RESEL_CONT );
				else if (0x03 == sub2) l5Txt = Dict.get( Dict.MSG5_SXNB_OPEN_SEL_CONT  );
				else if (0x04 == sub2) l5Txt = Dict.get( Dict.MSG5_SXNB_CLOSE_FILE     );
			}
			else if (0x0C == sub1) {
				hasLvl5 = true;
				l4Txt   = Dict.get( Dict.MSG4_SX_NU_MIDI_VISUAL_CTRL );
				l5Txt   = Dict.get( Dict.MSG5_SXNC_MVC_CMD           );
			}
			else if (0x7B == sub1) {
				l4Txt = Dict.get( Dict.MSG4_SX_NU_END_OF_FILE );
			}
			else if (0x7C == sub1) {
				l4Txt = Dict.get( Dict.MSG4_SX_NU_WAIT );
			}
			else if (0x7D == sub1) {
				l4Txt = Dict.get( Dict.MSG4_SX_NU_CANCEL );
			}
			else if (0x7E == sub1) {
				l4Txt = Dict.get( Dict.MSG4_SX_NU_NAK );
			}
			else if (0x7F == sub1) {
				l4Txt = Dict.get( Dict.MSG4_SX_NU_ACK );
			}
		}
		
		// node 4 and 5 - pack and return the result
		if (hasLvl5) {
			String[] result = { l4Txt, l5Txt, String.format("%02X", sub2) };
			if (((byte) 0xFF) == sub2) {
				// invalid sub ID 2 (message too short)
				result[ 1 ] = Dict.get( Dict.INVALID_MSG );
				result[ 2 ] = "-";
			}
			return result;
		}
		
		// only node 4 - pack and return the result
		else {
			String[] result = { l4Txt };
			return result;
		}
	}
	
	/**
	 * Returns the vendor name for the given vendor number.
	 * 
	 * The given vendor number can be one of the following:
	 * 
	 * - a hex number consisting of 2 digits (one-byte-ID)
	 * - a hex number consisting of 6 digits (3-byte-ID beginning with 00)
	 * - the string "-" for a corrupted message without a valid vendor ID
	 * 
	 * @param vendorNum  The vendor number as described above.
	 * @return the vendor name.
	 */
	private static String getVendorName(String vendorNum) {
		
		// invalid (vendor number not found, message too short)
		if ("-".equals(vendorNum)) {
			return Dict.get( Dict.INVALID_MSG );
		}
		
		// north american group
		if (     "01".equals(vendorNum) ) return "Sequential";
		if (     "02".equals(vendorNum) ) return "IDP";
		if (     "03".equals(vendorNum) ) return "Voyetra/Octave-Plateau";
		if (     "04".equals(vendorNum) ) return "Moog";
		if (     "05".equals(vendorNum) ) return "Passport Designs";
		if (     "06".equals(vendorNum) ) return "Lexicon";
		if (     "07".equals(vendorNum) ) return "Kurzweil";
		if (     "08".equals(vendorNum) ) return "Fender";
		if (     "09".equals(vendorNum) ) return "Gulbransen";
		if (     "0A".equals(vendorNum) ) return "AKG Acoustics";
		if (     "0B".equals(vendorNum) ) return "Voyce Music";
		if (     "0C".equals(vendorNum) ) return "Waveframe Corp";
		if (     "0D".equals(vendorNum) ) return "ADA Signal Processors";
		if (     "0E".equals(vendorNum) ) return "Garfield Electronics";
		if (     "0F".equals(vendorNum) ) return "Ensoniq";
		if (     "10".equals(vendorNum) ) return "Oberheim";
		if (     "11".equals(vendorNum) ) return "Apple Computer";
		if (     "12".equals(vendorNum) ) return "Grey Matter Response";
		if (     "13".equals(vendorNum) ) return "Digidesign";
		if (     "14".equals(vendorNum) ) return "Palm Tree Instruments";
		if (     "15".equals(vendorNum) ) return "JLCooper Electronics";
		if (     "16".equals(vendorNum) ) return "Lowrey";
		if (     "17".equals(vendorNum) ) return "Adams-Smith";
		if (     "18".equals(vendorNum) ) return "Emu Systems";
		if (     "19".equals(vendorNum) ) return "Harmony Systems";
		if (     "1A".equals(vendorNum) ) return "ART";
		if (     "1B".equals(vendorNum) ) return "Baldwin";
		if (     "1C".equals(vendorNum) ) return "Eventide";
		if (     "1D".equals(vendorNum) ) return "Inventronics";
		if (     "1F".equals(vendorNum) ) return "Clarity";
		if ( "000001".equals(vendorNum) ) return "Time Warner Interactive";
		if ( "000002".equals(vendorNum) ) return "Advanced Gravis Comp.";
		if ( "000003".equals(vendorNum) ) return "Media Vision";
		if ( "000004".equals(vendorNum) ) return "Dornes Research Group";
		if ( "000005".equals(vendorNum) ) return "K-Muse";
		if ( "000006".equals(vendorNum) ) return "Stypher";
		if ( "000007".equals(vendorNum) ) return "Digital Music Corp.";
		if ( "000008".equals(vendorNum) ) return "IOTA Systems";
		if ( "000009".equals(vendorNum) ) return "New England Digital";
		if ( "00000A".equals(vendorNum) ) return "Artisyn";
		if ( "00000B".equals(vendorNum) ) return "IVL Technologies";
		if ( "00000C".equals(vendorNum) ) return "Southern Music Systems";
		if ( "00000D".equals(vendorNum) ) return "Lake Butler Sound Company";
		if ( "00000E".equals(vendorNum) ) return "Alesis";
		if ( "00000F".equals(vendorNum) ) return "Sound Creation";
		if ( "000010".equals(vendorNum) ) return "DOD Electronics";
		if ( "000011".equals(vendorNum) ) return "Studer-Editech";
		if ( "000012".equals(vendorNum) ) return "Sonus";
		if ( "000013".equals(vendorNum) ) return "Temporal Acuity Products";
		if ( "000014".equals(vendorNum) ) return "Perfect Fretworks";
		if ( "000015".equals(vendorNum) ) return "KAT";
		if ( "000016".equals(vendorNum) ) return "Opcode";
		if ( "000017".equals(vendorNum) ) return "Rane Corp.";
		if ( "000018".equals(vendorNum) ) return "Anadi Inc.";
		if ( "000019".equals(vendorNum) ) return "KMX";
		if ( "00001A".equals(vendorNum) ) return "Allen & Heath Brenell";
		if ( "00001B".equals(vendorNum) ) return "Peavey Electronics";
		if ( "00001C".equals(vendorNum) ) return "360 Systems";
		if ( "00001D".equals(vendorNum) ) return "Spectrum Design";
		if ( "00001E".equals(vendorNum) ) return "Marquis Music";
		if ( "00001F".equals(vendorNum) ) return "Zeta Systems";
		if ( "000020".equals(vendorNum) ) return "Axxes";
		if ( "000021".equals(vendorNum) ) return "Orban";
		if ( "000022".equals(vendorNum) ) return "Indian Valley Mfg.";
		if ( "000023".equals(vendorNum) ) return "Triton";
		if ( "000024".equals(vendorNum) ) return "KTI";
		if ( "000025".equals(vendorNum) ) return "Breakaway Technologies";
		if ( "000026".equals(vendorNum) ) return "CAE";
		if ( "000027".equals(vendorNum) ) return "Harrison Systems";
		if ( "000028".equals(vendorNum) ) return "Future Lab/Mark Kuo";
		if ( "000029".equals(vendorNum) ) return "Rocktron Corp.";
		if ( "00002A".equals(vendorNum) ) return "PianoDisc";
		if ( "00002B".equals(vendorNum) ) return "Cannon Research Group";
		if ( "00002D".equals(vendorNum) ) return "Rodgers Instrument Corp.";
		if ( "00002E".equals(vendorNum) ) return "Blue Sky Logic";
		if ( "00002F".equals(vendorNum) ) return "Encore Electronics";
		if ( "000030".equals(vendorNum) ) return "Uptown";
		if ( "000031".equals(vendorNum) ) return "Voce";
		if ( "000032".equals(vendorNum) ) return "CTI Audio";
		if ( "000033".equals(vendorNum) ) return "S & S Research";
		if ( "000034".equals(vendorNum) ) return "Broderbund Software";
		if ( "000035".equals(vendorNum) ) return "Allen Organ Co.";
		if ( "000037".equals(vendorNum) ) return "Music Quest";
		if ( "000038".equals(vendorNum) ) return "APHEX";
		if ( "000039".equals(vendorNum) ) return "Gallien Krueger";
		if ( "00003A".equals(vendorNum) ) return "IBM";
		if ( "00003B".equals(vendorNum) ) return "Mark of the Unicorn";
		if ( "00003C".equals(vendorNum) ) return "Hotz Instruments";
		if ( "00003D".equals(vendorNum) ) return "ETA Lighting";
		if ( "00003E".equals(vendorNum) ) return "NSI Corporation";
		if ( "00003F".equals(vendorNum) ) return "Ad Lib";
		if ( "000040".equals(vendorNum) ) return "Richmond Sound Design";
		if ( "000041".equals(vendorNum) ) return "Microsoft";
		if ( "000042".equals(vendorNum) ) return "The Software Toolworks";
		if ( "000043".equals(vendorNum) ) return "Niche/RJMG";
		if ( "000044".equals(vendorNum) ) return "Intone";
		if ( "000045".equals(vendorNum) ) return "Advanced Remote Tech.";
		if ( "000047".equals(vendorNum) ) return "GT Electronics/Groove Tubes";
		if ( "000049".equals(vendorNum) ) return "Timeline Vista";
		if ( "00004A".equals(vendorNum) ) return "Mesa Boogie";
		if ( "00004C".equals(vendorNum) ) return "Sequoia Development";
		if ( "00004D".equals(vendorNum) ) return "Studio Electronics";
		if ( "00004E".equals(vendorNum) ) return "Euphonix";
		if ( "00004F".equals(vendorNum) ) return "InterMIDI";
		if ( "000050".equals(vendorNum) ) return "MIDI Solutions Inc.";
		if ( "000051".equals(vendorNum) ) return "3DO Company";
		if ( "000052".equals(vendorNum) ) return "Lightwave Research";
		if ( "000053".equals(vendorNum) ) return "Micro-W";
		if ( "000054".equals(vendorNum) ) return "Spectral Synthesis";
		if ( "000055".equals(vendorNum) ) return "Lone Wolf";
		if ( "000056".equals(vendorNum) ) return "Studio Technologies";
		if ( "000057".equals(vendorNum) ) return "Peterson EMP";
		if ( "000058".equals(vendorNum) ) return "Atari";
		if ( "000059".equals(vendorNum) ) return "Marion Systems";
		if ( "00005A".equals(vendorNum) ) return "Design Event";
		if ( "00005B".equals(vendorNum) ) return "Winjammer Software";
		if ( "00005C".equals(vendorNum) ) return "AT&T Bell Labs";
		if ( "00005E".equals(vendorNum) ) return "Symetrix";
		if ( "00005F".equals(vendorNum) ) return "MIDI the World";
		if ( "000060".equals(vendorNum) ) return "Desper Products";
		if ( "000061".equals(vendorNum) ) return "Micros 'N MIDI";
		if ( "000062".equals(vendorNum) ) return "Accordians Intl";
		if ( "000063".equals(vendorNum) ) return "EuPhonics";
		if ( "000064".equals(vendorNum) ) return "Musonix";
		if ( "000065".equals(vendorNum) ) return "Turtle Beach Systems";
		if ( "000066".equals(vendorNum) ) return "Mackie Designs";
		if ( "000067".equals(vendorNum) ) return "Compuserve";
		if ( "000068".equals(vendorNum) ) return "BES Technologies";
		if ( "000069".equals(vendorNum) ) return "QRS Music Rolls";
		if ( "00006A".equals(vendorNum) ) return "P.G. Music";
		if ( "00006B".equals(vendorNum) ) return "Sierra Semiconductor";
		if ( "00006C".equals(vendorNum) ) return "EpiGraf Audio Visual";
		if ( "00006D".equals(vendorNum) ) return "Electronics Diversified";
		if ( "00006E".equals(vendorNum) ) return "Tune 1000";
		if ( "00006F".equals(vendorNum) ) return "Advanced Micro Devices";
		if ( "000070".equals(vendorNum) ) return "Mediamation";
		if ( "000071".equals(vendorNum) ) return "Sabine Musical";
		if ( "000072".equals(vendorNum) ) return "Woog Labs";
		if ( "000073".equals(vendorNum) ) return "Micropolis";
		if ( "000074".equals(vendorNum) ) return "Ta Horng Musical Inst.";
		if ( "000075".equals(vendorNum) ) return "eTek Labs (Forte Tech)";
		if ( "000076".equals(vendorNum) ) return "Electro-Voice";
		if ( "000077".equals(vendorNum) ) return "Midisoft";
		if ( "000078".equals(vendorNum) ) return "QSound Labs";
		if ( "000079".equals(vendorNum) ) return "Westrex";
		if ( "00007A".equals(vendorNum) ) return "Nvidia";
		if ( "00007B".equals(vendorNum) ) return "ESS Technology";
		if ( "00007C".equals(vendorNum) ) return "MediaTrix Peripherals";
		if ( "00007D".equals(vendorNum) ) return "Brooktree";
		if ( "00007E".equals(vendorNum) ) return "Otari";
		if ( "00007F".equals(vendorNum) ) return "Key Electronics";
		if ( "000100".equals(vendorNum) ) return "Shure";
		if ( "000101".equals(vendorNum) ) return "AuraSound";
		if ( "000102".equals(vendorNum) ) return "Crystal Semiconductor";
		if ( "000103".equals(vendorNum) ) return "Conexant (Rockwell)";
		if ( "000104".equals(vendorNum) ) return "Silicon Graphics";
		if ( "000105".equals(vendorNum) ) return "M-Audio (Midiman)";
		if ( "000106".equals(vendorNum) ) return "PreSonus";
		if ( "000108".equals(vendorNum) ) return "Topaz Enterprises";
		if ( "000109".equals(vendorNum) ) return "Cast Lighting";
		if ( "00010A".equals(vendorNum) ) return "Microsoft Consumer Division";
		if ( "00010B".equals(vendorNum) ) return "Sonic Foundry";
		if ( "00010C".equals(vendorNum) ) return "Line 6 (Fast Forward)";
		if ( "00010D".equals(vendorNum) ) return "Beatnik Inc";
		if ( "00010E".equals(vendorNum) ) return "Van Koevering Company";
		if ( "00010F".equals(vendorNum) ) return "Altech Systems";
		if ( "000110".equals(vendorNum) ) return "S & S Research";
		if ( "000111".equals(vendorNum) ) return "VLSI Technology";
		if ( "000112".equals(vendorNum) ) return "Chromatic Research";
		if ( "000113".equals(vendorNum) ) return "Sapphire";
		if ( "000114".equals(vendorNum) ) return "IDRC";
		if ( "000115".equals(vendorNum) ) return "Justonic Tuning";
		if ( "000116".equals(vendorNum) ) return "TorComp";
		if ( "000117".equals(vendorNum) ) return "Newtek Inc";
		if ( "000118".equals(vendorNum) ) return "Sound Sculpture";
		if ( "000119".equals(vendorNum) ) return "Walker Technical";
		if ( "00011A".equals(vendorNum) ) return "Digital Harmony (PAVO)";
		if ( "00011B".equals(vendorNum) ) return "InVision Interactive";
		if ( "00011C".equals(vendorNum) ) return "T-Square Design";
		if ( "00011D".equals(vendorNum) ) return "Nemesys Music Technology";
		if ( "00011E".equals(vendorNum) ) return "DBX Professional (Harman Intl)";
		if ( "00011F".equals(vendorNum) ) return "Syndyne Corporation";
		if ( "000120".equals(vendorNum) ) return "Bitheadz";
		if ( "000121".equals(vendorNum) ) return "Cakewalk Music Software";
		if ( "000122".equals(vendorNum) ) return "Analog Devices (Staccato Systems)";
		if ( "000123".equals(vendorNum) ) return "National Semiconductor";
		if ( "000124".equals(vendorNum) ) return "Boom Theory / Adinolfi Alt. Perc.";
		if ( "000125".equals(vendorNum) ) return "Virtual DSP Corporation";
		if ( "000126".equals(vendorNum) ) return "Antares Systems";
		if ( "000127".equals(vendorNum) ) return "Angel Software";
		if ( "000128".equals(vendorNum) ) return "St Louis Music";
		if ( "000129".equals(vendorNum) ) return "Passport Music Software LLC (Gvox)";
		if ( "00012A".equals(vendorNum) ) return "Ashley Audio";
		if ( "00012B".equals(vendorNum) ) return "Vari-Lite";
		if ( "00012C".equals(vendorNum) ) return "Summit Audio";
		if ( "00012D".equals(vendorNum) ) return "Aureal Semiconductor";
		if ( "00012E".equals(vendorNum) ) return "SeaSound LLC";
		if ( "00012F".equals(vendorNum) ) return "U.S. Robotics";
		if ( "000130".equals(vendorNum) ) return "Aurisis Research";
		if ( "000131".equals(vendorNum) ) return "Nearfield Research";
		if ( "000132".equals(vendorNum) ) return "FM7 Inc";
		if ( "000133".equals(vendorNum) ) return "Swivel Systems";
		if ( "000134".equals(vendorNum) ) return "Hyperactive Audio Systems";
		if ( "000135".equals(vendorNum) ) return "MidiLite (Castle Studios Prods)";
		if ( "000136".equals(vendorNum) ) return "Radikal Technologies";
		if ( "000137".equals(vendorNum) ) return "Roger Linn Design";
		if ( "000138".equals(vendorNum) ) return "TC-Helicon Vocal Technologies";
		if ( "000139".equals(vendorNum) ) return "Event Electronics";
		if ( "00013A".equals(vendorNum) ) return "Sonic Network (Sonic Implants)";
		if ( "00013B".equals(vendorNum) ) return "Realtime Music Solutions";
		if ( "00013C".equals(vendorNum) ) return "Apogee Digital";
		if ( "00013D".equals(vendorNum) ) return "Classical Organs, Inc.";
		if ( "00013E".equals(vendorNum) ) return "Microtools Inc";
		if ( "00013F".equals(vendorNum) ) return "Numark Industries";
		if ( "000140".equals(vendorNum) ) return "Frontier Design Group LLC";
		if ( "000141".equals(vendorNum) ) return "Recordare LLC";
		if ( "000142".equals(vendorNum) ) return "Star Labs";
		if ( "000143".equals(vendorNum) ) return "Voyager Sound";
		if ( "000144".equals(vendorNum) ) return "Manifold Labs";
		if ( "000145".equals(vendorNum) ) return "Aviom";
		if ( "000146".equals(vendorNum) ) return "Mixmeister Technology";
		if ( "000147".equals(vendorNum) ) return "Notation Software";
		if ( "000148".equals(vendorNum) ) return "Mercurial Communications";
		if ( "000149".equals(vendorNum) ) return "Wave Arts, Inc";
		if ( "00014A".equals(vendorNum) ) return "Logic Sequencing Devices Inc";
		if ( "00014B".equals(vendorNum) ) return "Axess Electronics";
		if ( "00014C".equals(vendorNum) ) return "Muse Reasearch";
		if ( "00014D".equals(vendorNum) ) return "Open Labs";
		if ( "00014E".equals(vendorNum) ) return "Guillemot R&D Inc";
		if ( "00014F".equals(vendorNum) ) return "Samson Technologies";
		if ( "000150".equals(vendorNum) ) return "Electoronic Theatre Controls";
		if ( "000151".equals(vendorNum) ) return "Blackberry (RIM)";
		if ( "000152".equals(vendorNum) ) return "Mobileer";
		if ( "000153".equals(vendorNum) ) return "Synthogy";
		if ( "000154".equals(vendorNum) ) return "Lynx Studio Technology";
		if ( "000155".equals(vendorNum) ) return "Damage Control Engineering";
		if ( "000156".equals(vendorNum) ) return "Yost Engineering";
		if ( "000157".equals(vendorNum) ) return "Brooks & Forsman Designs";
		if ( "000158".equals(vendorNum) ) return "Infinite Response";
		if ( "000159".equals(vendorNum) ) return "Garritan";
		if ( "00015A".equals(vendorNum) ) return "Plogue Art et Technology";
		if ( "00015B".equals(vendorNum) ) return "RJM Music Technology";
		if ( "00015C".equals(vendorNum) ) return "Custom Solutions Software";
		if ( "00015D".equals(vendorNum) ) return "Sonarcana LLC";
		if ( "00015E".equals(vendorNum) ) return "Centrance";
		if ( "00015F".equals(vendorNum) ) return "Kesumo LLC";
		if ( "000160".equals(vendorNum) ) return "Stanton (Gibson)";
		if ( "000161".equals(vendorNum) ) return "Livid Instruments";
		if ( "000162".equals(vendorNum) ) return "First Act / 745 Media";
		if ( "000163".equals(vendorNum) ) return "Pygraphics";
		if ( "000164".equals(vendorNum) ) return "Panadigm Innovations";
		if ( "000165".equals(vendorNum) ) return "Avedis Zildjian";
		if ( "000166".equals(vendorNum) ) return "Auvital Music";
		if ( "000167".equals(vendorNum) ) return "You Rock Guitar (Inspired Instruments)";
		if ( "000168".equals(vendorNum) ) return "Chris Grigg Designs";
		if ( "000169".equals(vendorNum) ) return "Slate Digital";
		if ( "00016A".equals(vendorNum) ) return "Mixware";
		if ( "00016B".equals(vendorNum) ) return "Social Entropy";
		if ( "00016C".equals(vendorNum) ) return "Source Audio";
		if ( "00016D".equals(vendorNum) ) return "Ernie Ball / Music Man";
		if ( "00016E".equals(vendorNum) ) return "Fishman";
		if ( "00016F".equals(vendorNum) ) return "Custom Audio Electronics";
		if ( "000170".equals(vendorNum) ) return "American Audio/DJ";
		if ( "000171".equals(vendorNum) ) return "Mega Control Systems";
		if ( "000172".equals(vendorNum) ) return "Kilpatrick Audio";
		if ( "000173".equals(vendorNum) ) return "iConnectivity";
		if ( "000174".equals(vendorNum) ) return "Fractal Audio";
		if ( "000175".equals(vendorNum) ) return "NetLogic Microsystems";
		if ( "000176".equals(vendorNum) ) return "Music Computing";
		if ( "000177".equals(vendorNum) ) return "Nektar Technology Inc";
		if ( "000178".equals(vendorNum) ) return "Zenph Sound Innovations";
		if ( "000179".equals(vendorNum) ) return "DJTechTools.com";
		if ( "00017A".equals(vendorNum) ) return "Rezonance Labs";
		if ( "00017B".equals(vendorNum) ) return "Decibel Eleven";
		if ( "00017C".equals(vendorNum) ) return "CNMAT";
		if ( "00017D".equals(vendorNum) ) return "Media Overkill";
		if ( "00017E".equals(vendorNum) ) return "Confusionists";
		if ( "00017F".equals(vendorNum) ) return "moForte";
		if ( "000200".equals(vendorNum) ) return "Miselu";
		if ( "000201".equals(vendorNum) ) return "Amelia's Compass";
		if ( "000202".equals(vendorNum) ) return "Zivix";
		if ( "000203".equals(vendorNum) ) return "Artiphon";
		if ( "000204".equals(vendorNum) ) return "Synclavier Digital";
		if ( "000205".equals(vendorNum) ) return "Light & Sound Control Devices";
		if ( "000206".equals(vendorNum) ) return "Retronyms";
		if ( "000207".equals(vendorNum) ) return "JS Technologies";
		if ( "000208".equals(vendorNum) ) return "Quicco Sound";
		if ( "000209".equals(vendorNum) ) return "A-Designs Audio";
		if ( "00020A".equals(vendorNum) ) return "McCarthy Music";
		if ( "00020B".equals(vendorNum) ) return "Denon DJ";
		if ( "00020C".equals(vendorNum) ) return "Keith Robert Murray";
		if ( "00020D".equals(vendorNum) ) return "Google";
		if ( "00020E".equals(vendorNum) ) return "ISP Technologies";
		if ( "00020F".equals(vendorNum) ) return "Abstrakt Instruments";
		if ( "000210".equals(vendorNum) ) return "Meris";
		if ( "000211".equals(vendorNum) ) return "Sensorpoint";
		if ( "000212".equals(vendorNum) ) return "Hi-Z Labs";
		
		// european group
		if (     "20".equals(vendorNum) ) return "Passac";
		if (     "21".equals(vendorNum) ) return "SIEL";
		if (     "22".equals(vendorNum) ) return "Synthaxe";
		if (     "23".equals(vendorNum) ) return "Stepp";
		if (     "24".equals(vendorNum) ) return "Hohner";
		if (     "25".equals(vendorNum) ) return "Twister";
		if (     "26".equals(vendorNum) ) return "Solton";
		if (     "27".equals(vendorNum) ) return "Jellinghaus MS";
		if (     "28".equals(vendorNum) ) return "Southworth Music Systems";
		if (     "29".equals(vendorNum) ) return "PPG";
		if (     "2A".equals(vendorNum) ) return "JEN";
		if (     "2B".equals(vendorNum) ) return "SSL Limited";
		if (     "2C".equals(vendorNum) ) return "Audio Veritrieb";
		if (     "2D".equals(vendorNum) ) return "Neve";
		if (     "2E".equals(vendorNum) ) return "Soundtracs";
		if (     "2F".equals(vendorNum) ) return "Elka";
		if (     "30".equals(vendorNum) ) return "Dynacord";
		if (     "31".equals(vendorNum) ) return "Viscount";
		if (     "32".equals(vendorNum) ) return "Drawmer";
		if (     "33".equals(vendorNum) ) return "Clavia Digital Instruments";
		if (     "34".equals(vendorNum) ) return "Audio Architecture";
		if (     "35".equals(vendorNum) ) return "GeneralMusic Corp.";
		if (     "36".equals(vendorNum) ) return "Cheetah Marketing";
		if (     "37".equals(vendorNum) ) return "C.T.M.";
		if (     "38".equals(vendorNum) ) return "Simmons";
		if (     "39".equals(vendorNum) ) return "Soundcraft Electronics";
		if (     "3A".equals(vendorNum) ) return "Steinberg";
		if (     "3B".equals(vendorNum) ) return "Wersi";
		if (     "3C".equals(vendorNum) ) return "AVAB";
		if (     "3D".equals(vendorNum) ) return "Digigram";
		if (     "3E".equals(vendorNum) ) return "Waldorf Electronics";
		if (     "3F".equals(vendorNum) ) return "Quasimidi";
		if ( "002000".equals(vendorNum) ) return "Dream";
		if ( "002001".equals(vendorNum) ) return "Strand Lighting";
		if ( "002002".equals(vendorNum) ) return "Amek Systems";
		if ( "002003".equals(vendorNum) ) return "Casa Di Risparmio Di Loreto";
		if ( "002004".equals(vendorNum) ) return "Böhm Electronic";
		if ( "002005".equals(vendorNum) ) return "Syntec Digital Audio";
		if ( "002006".equals(vendorNum) ) return "Trident Audio";
		if ( "002007".equals(vendorNum) ) return "Real World Studio";
		if ( "002008".equals(vendorNum) ) return "Evolution Synthesis";
		if ( "002009".equals(vendorNum) ) return "Yes Technology";
		if ( "00200A".equals(vendorNum) ) return "Audiomatica";
		if ( "00200B".equals(vendorNum) ) return "Bontempi / Farfisa";
		if ( "00200C".equals(vendorNum) ) return "F.B.T. Elettronica";
		if ( "00200D".equals(vendorNum) ) return "MidiTemp";
		if ( "00200E".equals(vendorNum) ) return "LA Audio (Larking Audio)";
		if ( "00200F".equals(vendorNum) ) return "Zero 88 Lighting";
		if ( "002010".equals(vendorNum) ) return "Micon Audio Electronics";
		if ( "002011".equals(vendorNum) ) return "Forefront Technology";
		if ( "002012".equals(vendorNum) ) return "Studio Audio and Video";
		if ( "002013".equals(vendorNum) ) return "Kenton Electronics";
		if ( "002014".equals(vendorNum) ) return "Celco Division of Electrosonic";
		if ( "002015".equals(vendorNum) ) return "ADB";
		if ( "002016".equals(vendorNum) ) return "Marshall Products";
		if ( "002017".equals(vendorNum) ) return "DDA";
		if ( "002018".equals(vendorNum) ) return "BSS Audio";
		if ( "002019".equals(vendorNum) ) return "MA Lighting Technology";
		if ( "00201A".equals(vendorNum) ) return "Fatar";
		if ( "00201B".equals(vendorNum) ) return "QSC Audio";
		if ( "00201C".equals(vendorNum) ) return "Artisan Clasic Organ";
		if ( "00201D".equals(vendorNum) ) return "Orla Spa";
		if ( "00201E".equals(vendorNum) ) return "Pinnacle Audio";
		if ( "00201F".equals(vendorNum) ) return "TC Electronics";
		if ( "002020".equals(vendorNum) ) return "Doepfer Musikelektronik";
		if ( "002021".equals(vendorNum) ) return "Creative Technology Pte";
		if ( "002022".equals(vendorNum) ) return "Minami / Seiyddo";
		if ( "002023".equals(vendorNum) ) return "Goldstar";
		if ( "002024".equals(vendorNum) ) return "Midisoft s.a.s. di M.Cima";
		if ( "002025".equals(vendorNum) ) return "Samick";
		if ( "002026".equals(vendorNum) ) return "Penny and Giles";
		if ( "002027".equals(vendorNum) ) return "Acorn Computer";
		if ( "002028".equals(vendorNum) ) return "LSC Electronics";
		if ( "002029".equals(vendorNum) ) return "Focusrite/Novation";
		if ( "00202A".equals(vendorNum) ) return "Samkyung Mechatronics";
		if ( "00202B".equals(vendorNum) ) return "Medeli Electronics";
		if ( "00202C".equals(vendorNum) ) return "Charlie Lab";
		if ( "00202D".equals(vendorNum) ) return "Blue Chip Music Tech";
		if ( "00202E".equals(vendorNum) ) return "BEE OH Corp";
		if ( "00202F".equals(vendorNum) ) return "LG Semiconductor";
		if ( "002030".equals(vendorNum) ) return "TESI";
		if ( "002031".equals(vendorNum) ) return "EMAGIC";
		if ( "002032".equals(vendorNum) ) return "Behringer";
		if ( "002033".equals(vendorNum) ) return "Access  Music Electronics";
		if ( "002034".equals(vendorNum) ) return "Synoptic";
		if ( "002035".equals(vendorNum) ) return "Hanmesoft Corp";
		if ( "002036".equals(vendorNum) ) return "Terratec Electronic";
		if ( "002037".equals(vendorNum) ) return "Proel SpA";
		if ( "002038".equals(vendorNum) ) return "IBK MIDI";
		if ( "002039".equals(vendorNum) ) return "IRCAM";
		if ( "00203A".equals(vendorNum) ) return "Propellerhead Software";
		if ( "00203B".equals(vendorNum) ) return "Red Sound Systems";
		if ( "00203C".equals(vendorNum) ) return "Elektron ESI";
		if ( "00203D".equals(vendorNum) ) return "Sintefex Audio";
		if ( "00203E".equals(vendorNum) ) return "MAM (Music and More)";
		if ( "00203F".equals(vendorNum) ) return "Amsaro";
		if ( "002040".equals(vendorNum) ) return "CDS Advanced Technology (Lanbox)";
		if ( "002041".equals(vendorNum) ) return "Mode Machines (Touched By Sound)";
		if ( "002042".equals(vendorNum) ) return "DSP Arts";
		if ( "002043".equals(vendorNum) ) return "Phil Rees Music";
		if ( "002044".equals(vendorNum) ) return "Stamer Musikanlagen";
		if ( "002045".equals(vendorNum) ) return "Soundart (Musical Muntaner)";
		if ( "002046".equals(vendorNum) ) return "C-Mexx Software";
		if ( "002047".equals(vendorNum) ) return "Klavis Technologies";
		if ( "002048".equals(vendorNum) ) return "Noteheads";
		if ( "002049".equals(vendorNum) ) return "Algorithmix";
		if ( "00204A".equals(vendorNum) ) return "Skrydstrup R&D";
		if ( "00204B".equals(vendorNum) ) return "Professional Audio Company";
		if ( "00204C".equals(vendorNum) ) return "NewWave Labs (MadWaves / DBTECH)";
		if ( "00204D".equals(vendorNum) ) return "Vermona";
		if ( "00204E".equals(vendorNum) ) return "Nokia";
		if ( "00204F".equals(vendorNum) ) return "Wave Idea";
		if ( "002050".equals(vendorNum) ) return "Hartmann";
		if ( "002051".equals(vendorNum) ) return "Lion's Tracs";
		if ( "002052".equals(vendorNum) ) return "Analogue Systems";
		if ( "002053".equals(vendorNum) ) return "Focal-JMlab";
		if ( "002054".equals(vendorNum) ) return "Ringway Electronics (Chang-Zhou)";
		if ( "002055".equals(vendorNum) ) return "Faith Technologies (Digiplug)";
		if ( "002056".equals(vendorNum) ) return "Showwork";
		if ( "002057".equals(vendorNum) ) return "Manikin Electoronic";
		if ( "002058".equals(vendorNum) ) return "1 Come Tech";
		if ( "002059".equals(vendorNum) ) return "Phonic Corp";
		if ( "00205A".equals(vendorNum) ) return "Dolby Australia (Lake)";
		if ( "00205B".equals(vendorNum) ) return "Silansys Technologies";
		if ( "00205C".equals(vendorNum) ) return "Winbond Electronics";
		if ( "00205D".equals(vendorNum) ) return "Cinetix Medien und Interface";
		if ( "00205E".equals(vendorNum) ) return "A&G Soluzioni Digitali";
		if ( "00205F".equals(vendorNum) ) return "Sequentix Music Systems";
		if ( "002060".equals(vendorNum) ) return "Oram Pro Audio";
		if ( "002061".equals(vendorNum) ) return "Be4";
		if ( "002062".equals(vendorNum) ) return "Infection Music";
		if ( "002063".equals(vendorNum) ) return "Central Music Co. (CME)";
		if ( "002064".equals(vendorNum) ) return "genoQs Machines";
		if ( "002065".equals(vendorNum) ) return "Medialon";
		if ( "002066".equals(vendorNum) ) return "Waves Audio";
		if ( "002067".equals(vendorNum) ) return "Jerash Labs";
		if ( "002068".equals(vendorNum) ) return "Da Fact";
		if ( "002069".equals(vendorNum) ) return "Elby Designs";
		if ( "00206A".equals(vendorNum) ) return "Spectral Audio";
		if ( "00206B".equals(vendorNum) ) return "Arturia";
		if ( "00206C".equals(vendorNum) ) return "Vixid";
		if ( "00206D".equals(vendorNum) ) return "C-Thru Music ";
		if ( "00206E".equals(vendorNum) ) return "Ya Horng Electronic Co";
		if ( "00206F".equals(vendorNum) ) return "SM Pro Audio";
		if ( "002070".equals(vendorNum) ) return "OTO MACHINES";
		if ( "002071".equals(vendorNum) ) return "ELZAB S.A. (G LAB)";
		if ( "002072".equals(vendorNum) ) return "Blackstar Amplification";
		if ( "002073".equals(vendorNum) ) return "M3i Technologies";
		if ( "002074".equals(vendorNum) ) return "Gemalto (Xiring)";
		if ( "002075".equals(vendorNum) ) return "Prostage SL";
		if ( "002076".equals(vendorNum) ) return "Teenage Engineering";
		if ( "002077".equals(vendorNum) ) return "Tobias Erichsen Consulting";
		if ( "002078".equals(vendorNum) ) return "Nixer";
		if ( "002079".equals(vendorNum) ) return "Hanpin Electron Co";
		if ( "00207A".equals(vendorNum) ) return "\"MIDI-hardware\" R.Sowa";
		if ( "00207B".equals(vendorNum) ) return "Beyond Music Industrial";
		if ( "00207C".equals(vendorNum) ) return "Kiss Box B.V.";
		if ( "00207D".equals(vendorNum) ) return "Misa Digital Technologies";
		if ( "00207E".equals(vendorNum) ) return "AI Musics Technology";
		if ( "00207F".equals(vendorNum) ) return "Serato Inc LP";
		if ( "002100".equals(vendorNum) ) return "Limex";
		if ( "002101".equals(vendorNum) ) return "Kyodday (Tokai)";
		if ( "002102".equals(vendorNum) ) return "Mutable Instruments";
		if ( "002103".equals(vendorNum) ) return "PreSonus Software";
		if ( "002104".equals(vendorNum) ) return "Ingenico (Xiring)";
		if ( "002105".equals(vendorNum) ) return "Fairlight Instruments Pty";
		if ( "002106".equals(vendorNum) ) return "Musicom Lab";
		if ( "002107".equals(vendorNum) ) return "Modal Electronics (Modulus/VacoLoco)";
		if ( "002108".equals(vendorNum) ) return "RWA (Hong Kong)";
		if ( "002109".equals(vendorNum) ) return "Native Instruments";
		if ( "00210A".equals(vendorNum) ) return "Naonext";
		if ( "00210B".equals(vendorNum) ) return "MFB";
		if ( "00210C".equals(vendorNum) ) return "Teknel Research";
		if ( "00210D".equals(vendorNum) ) return "Ploytec";
		if ( "00210E".equals(vendorNum) ) return "Surfin Kangaroo Studio";
		if ( "00210F".equals(vendorNum) ) return "Philips Electronics HK";
		if ( "002110".equals(vendorNum) ) return "ROLI";
		if ( "002111".equals(vendorNum) ) return "Panda-Audio";
		if ( "002112".equals(vendorNum) ) return "BauM Software";
		if ( "002113".equals(vendorNum) ) return "Machinewerks Ltd.";
		if ( "002114".equals(vendorNum) ) return "Xiamen Elane Electronics";
		if ( "002115".equals(vendorNum) ) return "Marshall Amplification PLC";
		if ( "002116".equals(vendorNum) ) return "Kiwitechnics";
		if ( "002117".equals(vendorNum) ) return "Rob Papen";
		if ( "002118".equals(vendorNum) ) return "Spicetone OU";
		if ( "002119".equals(vendorNum) ) return "V3Sound";
		if ( "00211A".equals(vendorNum) ) return "IK Multimedia";
		if ( "00211B".equals(vendorNum) ) return "Novalia";
		if ( "00211C".equals(vendorNum) ) return "Modor Music";
		if ( "00211D".equals(vendorNum) ) return "Ableton";
		if ( "00211E".equals(vendorNum) ) return "Dtronics";
		if ( "00211F".equals(vendorNum) ) return "ZAQ Audio";
		if ( "002120".equals(vendorNum) ) return "Muabaobao Education Technology Co";
		
		// japanese group
		if (     "40".equals(vendorNum) ) return "Kawai Musical Instruments";
		if (     "41".equals(vendorNum) ) return "Roland";
		if (     "42".equals(vendorNum) ) return "Korg";
		if (     "43".equals(vendorNum) ) return "Yamaha";
		if (     "44".equals(vendorNum) ) return "Casio Computer";
		if (     "46".equals(vendorNum) ) return "Kamiya Studio";
		if (     "47".equals(vendorNum) ) return "Akai Electric";
		if (     "48".equals(vendorNum) ) return "Victor Company of Japan";
		if (     "49".equals(vendorNum) ) return "Mesosha";
		if (     "4A".equals(vendorNum) ) return "Hoshimo Gakki";
		if (     "4B".equals(vendorNum) ) return "Fujitsu Elect";
		if (     "4C".equals(vendorNum) ) return "Sony";
		if (     "4D".equals(vendorNum) ) return "Nisshin Onpa";
		if (     "4E".equals(vendorNum) ) return "Teac";
		if (     "50".equals(vendorNum) ) return "Mats ushita Electric Industrial";
		if (     "51".equals(vendorNum) ) return "Fostex";
		if (     "52".equals(vendorNum) ) return "Zoom";
		if (     "54".equals(vendorNum) ) return "Matsushita Communication Industrial";
		if (     "55".equals(vendorNum) ) return "Suzuki Musical Inst. Mfg.";
		if (     "56".equals(vendorNum) ) return "Fuji Sound Corporation";
		if (     "57".equals(vendorNum) ) return "Acoustic Technical Laboratory";
		if (     "59".equals(vendorNum) ) return "Faith";
		if (     "5A".equals(vendorNum) ) return "Internet Corporation";
		if (     "5C".equals(vendorNum) ) return "Seekers";
		if (     "5F".equals(vendorNum) ) return "SD Card Association";
		if ( "004000".equals(vendorNum) ) return "Crimson Technology";
		if ( "004001".equals(vendorNum) ) return "Softbank Mobile";
		if ( "004003".equals(vendorNum) ) return "D&M Holdings";
		if ( "004004".equals(vendorNum) ) return "XING";
		if ( "004005".equals(vendorNum) ) return "Pioneer DJ";
		
		// fallback
		return Dict.get( Dict.UNKNOWN );
	}
}
