/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.midica.ui.painter.BackgroundPainter;
import org.midica.ui.painter.BorderPainter;
import org.midica.ui.widget.MidicaButton;
import org.midica.ui.widget.MidicaFileChooser;

/**
 * This class handles the look and feel of the GUI.
 * 
 * If possible, Nimbus look and feel is used and configured with custom colors.
 * 
 * If Nimbus is not possible, Metal is tried as a backup.
 * 
 * @author Jan Trukenmüller
 */
public class Laf {
	
	private static final String lafNameNimbus = "Nimbus";
	private static final String lafNameMetal  = "Metal";
	
	/** **true**, if nimbus look and feel is used. */
	public static boolean isNimbus = false;
	
	/** **true**, if metal look and feel is used. */
	public static boolean isMetal = false;
	
	/** Height of each text field. */
	public static int textFieldHeight = 19;
	
	// inset values
	private static final int iIn  = 2;  // Pixels used for insets between components (inner insets)
	private static final int iOut = 10; // Pixels used for insets between a component and the window border (outer insets)
	
	// insets
	public static final Insets INSETS_N   = new Insets( iOut, iIn,  iIn,  iIn  ); // north
	public static final Insets INSETS_NE  = new Insets( iOut, iIn,  iIn,  iOut ); // north-east
	public static final Insets INSETS_E   = new Insets( iIn,  iIn,  iIn,  iOut ); // east
	public static final Insets INSETS_SE  = new Insets( iIn,  iIn,  iOut, iOut ); // south-east
	public static final Insets INSETS_S   = new Insets( iIn,  iIn,  iOut, iIn  ); // south
	public static final Insets INSETS_SW  = new Insets( iIn,  iOut, iOut, iIn  ); // south-west
	public static final Insets INSETS_W   = new Insets( iIn,  iOut, iIn,  iIn  ); // west
	public static final Insets INSETS_NW  = new Insets( iOut, iOut, iIn,  iIn  ); // north-west
	public static final Insets INSETS_ALL = new Insets( iOut, iOut, iOut, iOut ); // all directions are borders
	public static final Insets INSETS_IN  = new Insets( iIn,  iIn,  iIn,  iIn  ); // no direction is border
	public static final Insets INSETS_NWE = new Insets( iOut, iOut, iIn,  iOut ); // north + west + east is border
	public static final Insets INSETS_SWE = new Insets( iIn,  iOut, iOut, iOut ); // south + west + east is border
	public static final Insets INSETS_WNS = new Insets( iOut, iOut, iOut, iIn  ); // west + north + south is border
	public static final Insets INSETS_ENS = new Insets( iOut, iIn,  iOut, iOut ); // east + north + south is border
	public static final Insets INSETS_WE  = new Insets( iIn,  iOut, iIn,  iOut ); // west + east is border
	
	public static       Insets INSETS_CHANNEL_BUTTON         = new Insets(  0,  0,  0,  0 );
	public static final Insets INSETS_TICK_TIME              = new Insets( -1,  2, -1,  2 );
	public static final Insets INSETS_FLOW_LBL_NIMBUS        = new Insets( -6, -6, -6, -6 );
	public static final Insets INSETS_LBL_IMPORT_EXPORT      = new Insets(  0,  2,  0,  2 );
	public static       Insets INSETS_BTN_EXPAND_COLLAPSE    = new Insets(  0,  0,  0,  0 );
	public static       Insets INSETS_MSG_FILTER_CBX_LBL     = new Insets(  0,  0,  0,  0 );
	public static       Insets INSETS_MSG_FILTER_FROM_TO_LBL = new Insets(  0,  0,  0,  0 );
	public static final Insets INSETS_ZERO                   = new Insets(  0,  0,  0,  0 );
	public static final Insets INSETS_FILTER_ICON_W_LBL_H    = new Insets(  0,  0,  0, 15 );
	public static final Insets INSETS_FILTER_ICON_W_LBL_V    = new Insets(  0,  0, 15,  0 );
	
	public static int PLAYER_CH_LBL_INSTR_WIDTH   = 110;
	public static int PLAYER_CH_LBL_COMMENT_WIDTH = 180;
	
	// c o l o r s
	private static final Color COLOR_NIMBUS_BASE     = new Color(  0,  142,  36 );
	private static final Color COLOR_PRIMARY_LIGHT   = new Color( 176, 241, 200 );
	private static final Color COLOR_PRIMARY         = new Color(  78, 223, 131 );
	private static final Color COLOR_PRIMARY_DARK    = new Color(  28, 153,  74 );
	private static final Color COLOR_SECONDARY_LIGHT = new Color( 255, 223, 153 );
	private static final Color COLOR_SECONDARY       = new Color( 255, 174,   0 );
	private static final Color COLOR_SECONDARY_DARK  = new Color( 153, 104,   0 );
	public  static final Color COLOR_BORDER          = new Color(  23, 121, 186 );
	private static final Color COLOR_BORDER_LIGHT    = new Color( 148, 204, 242 );
	private static final Color COLOR_ALERT           = new Color( 255,   0,   0 );
	public  static final Color COLOR_PANEL           = new Color( 238, 238, 238 );
	private static final Color COLOR_WHITE           = new Color( 255, 255, 255 );
	private static final Color COLOR_BLACK           = new Color(   0,   0,   0 );
	private static final Color COLOR_INACTIVE        = new Color( 228, 228, 228 );
	
	// button colors
	private static final Color COLOR_BUTTON_PRIMARY    = COLOR_SECONDARY;
	private static final Color COLOR_BUTTON_SEC_NIMBUS = new Color( 44, 174, 92 );
	
	// text field background colors
	public  static final Color COLOR_NORMAL = COLOR_WHITE;
	public  static final Color COLOR_OK     = new Color( 200, 255, 200 );
	public  static final Color COLOR_ERROR  = new Color( 255, 150, 150 );
	
	// table header and column colors
	public  static final Color COLOR_TABLE_HEADER_BG         = COLOR_SECONDARY;
	public  static final Color COLOR_TABLE_HEADER_SORTED_BG  = new Color( 120, 60, 0 );
	public  static final Color COLOR_TABLE_HEADER_TXT        = COLOR_BLACK;
	public  static final Color COLOR_TABLE_CELL_SELECTED     = COLOR_SECONDARY_LIGHT;      // table row is currently selected
	public  static final Color COLOR_TABLE_CELL_CATEGORY     = COLOR_PRIMARY_LIGHT;
	public  static final Color COLOR_TABLE_CELL_CAT_SELECTED = COLOR_PRIMARY;              // category row is currently selected
	public  static final Color COLOR_TABLE_CELL_FUTURE       = new Color( 255, 255, 150 ); // future notes for the note history
	public  static final Color COLOR_TABLE_CELL_PAST         = COLOR_WHITE;                // current or past notes for the note history
	private static final Color COLOR_TABLE_GRID              = COLOR_BORDER;
	
	// message table and tree colors
	public  static final Color COLOR_MSG_TABLE_HEADER_BG     = COLOR_TABLE_HEADER_BG;
	public  static final Color COLOR_MSG_TABLE_HEADER_TXT    = COLOR_TABLE_HEADER_TXT;
	public  static final Color COLOR_MSG_TABLE               = COLOR_SECONDARY_LIGHT;
	public  static final Color COLOR_MSG_TABLE_SELECTED      = COLOR_SECONDARY; // new Color( 255, 243, 220 );
	public  static final Color COLOR_MSG_TABLE_SELECTED_TXT  = COLOR_BLACK;
	public  static final Color COLOR_MSG_TREE                = COLOR_PRIMARY_LIGHT;
	private static final Color COLOR_MSG_TREE_SELECTED_BG    = COLOR_PRIMARY;
	private static final Color COLOR_MSG_TREE_SELECTED_TXT   = COLOR_BLACK;
	public  static final Color COLOR_MSG_DEFAULT             = UIManager.getColor( "Panel.background" );
	public  static final Color COLOR_MSG_TABLE_GRID          = COLOR_SECONDARY;
	
	// string filter layer for tables
	public static final Color COLOR_TBL_FILTER_LAYER_BACKGROUND       = COLOR_SECONDARY_LIGHT;
	public static final Color COLOR_TBL_FILTER_ICON_BORDER_INACTIVE   = COLOR_BORDER;
	public static final Color COLOR_TBL_FILTER_ICON_BORDER_ACTIVE     = COLOR_ALERT;
	public static final Color COLOR_TBL_FILTER_ICON_HOVER_BG_INACTIVE = COLOR_SECONDARY;
	public static final Color COLOR_TBL_FILTER_ICON_HOVER_BG_ACTIVE   = COLOR_PRIMARY_DARK;
	
	// tooltip colors
	private static final Color COLOR_TOOLTIP_FOREGROUND = COLOR_BLACK;
	private static final Color COLOR_TOOLTIP_BACKGROUND = COLOR_SECONDARY_LIGHT;
	
	// scrollbar colors
	private static final Color COLOR_SCROLLBAR_TRACK    = COLOR_SECONDARY_LIGHT;
	private static final Color COLOR_SCROLLBAR_SCRL_BTN = COLOR_SECONDARY;
	
	// checkbox colors
	private static final Color COLOR_CHECKBOX        = new Color( 200, 200, 200 );
	private static final Color COLOR_CHECKBOX_BORDER = COLOR_PRIMARY;
	
	// links
	public  static final Color COLOR_LINK = COLOR_PRIMARY_DARK;
	
	// split pane dividers
	private static final Color COLOR_DIVIDER = COLOR_SECONDARY_LIGHT;
	
	// split pane dividers
	private static final Color COLOR_TREE_COLLAPSE_EXPAND = COLOR_SECONDARY;
	
	// color surrounding focused widgets
	private static final Color COLOR_FOCUS = COLOR_SECONDARY;
	
	// player colors
	public static final Color  COLOR_PL_TICKS             = new Color(  50, 100, 255 );
	public static final Color  COLOR_PL_TIME              = new Color(   0,   0,   0 );
	public static final Color  COLOR_PL_CH_PROGRAM_NUMBER = new Color(   0,   0,   0 );
	public static final Color  COLOR_PL_CH_BANK_NUMBER    = new Color(  50, 100, 255 );
	public static final Color  COLOR_PL_CH_INSTRUMENT     = new Color(   0,   0,   0 );
	public static final Color  COLOR_PL_CH_COMMENT        = new Color(  50, 100, 255 );
	public static final String COLOR_KAR_1_PAST           = "ff0000"; // karaoke, 1st voice
	public static final String COLOR_KAR_1_FUTURE         = "000000"; // karaoke, 1st voice
	public static final String COLOR_KAR_2_PAST           = "00aa00"; // karaoke, 2nd voice
	public static final String COLOR_KAR_2_FUTURE         = "0000ff"; // karaoke, 2nd voice
	
	// transpose colors (main window)
	public static final Color COLOR_TRANSPOSE_DEFAULT = COLOR_BORDER;
	public static final Color COLOR_TRANSPOSE_CHANGED = COLOR_ALERT;
	
	private static final HashMap<String, Object> custom           = new HashMap<>();
	private static final HashMap<String, Object> customButtonPrim = new HashMap<>();
	private static final HashMap<String, Object> customButtonSec  = new HashMap<>();
	private static final UIDefaults buttonSecDefaults  = new UIDefaults();
	private static final UIDefaults buttonPrimDefaults = new UIDefaults();
	
	private static Font BOLD_LABEL_FONT = null;
	
	/**
	 * Initializes the look and feel on startup. This includes:
	 * 
	 * - Choosing which look and feel to use
	 * - Fine-tuning some GUI properties
	 */
	public static void init() {
		
		// switch to nimbus
		isNimbus = switchLaf(lafNameNimbus);
		if (isNimbus) {
			isNimbus                      = true;
			textFieldHeight               = 22;
			INSETS_BTN_EXPAND_COLLAPSE    = new Insets( -10, -10, -10, -10 );
			INSETS_MSG_FILTER_CBX_LBL     = new Insets(   0,   7,   0,   0 );
			INSETS_MSG_FILTER_FROM_TO_LBL = new Insets(   2,   7,   0,   0 );
			INSETS_CHANNEL_BUTTON         = new Insets(  -2,  -8,  -2,  -8 );
			PLAYER_CH_LBL_INSTR_WIDTH     = 110;
			PLAYER_CH_LBL_COMMENT_WIDTH   = 200;
		}
		else {
			// backup: switch to metal
			isMetal = switchLaf(lafNameMetal);
		}
		
		// create LAF specific custom settings
		if (isNimbus)
			initNimbus();
		else if (isMetal)
			initMetal();
		else
			return;
		
		// apply the custom settings globally
		applyCustomDefaults();
		
		// bold font
		Font labelFont = UIManager.getFont("Label.font");
		if (labelFont != null) {
			String fontName = labelFont.getFontName();
			int    fontSize = labelFont.getSize();
			BOLD_LABEL_FONT = new Font(fontName, Font.BOLD, fontSize);
		}
	}
	
	/**
	 * Switch to the given look and feel.
	 * 
	 * @param lafName  name of the look and feel we want to switch to.
	 * @return **true** on success, **false** on error.
	 */
	private static final boolean switchLaf(String lafName) {
		
		// switch to the requested look and feel
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (lafName.equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					return true;
				}
			}
		}
		catch (Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * Recreates a new nimbus look and feel and initializes it
	 * with the given colors, if not yet done.
	 * 
	 * Does nothing, if the given colors are already used.
	 * 
	 * @param colorBase      color used for "nimbusBase"
	 * @param colorBlueGrey  color used for "nimbusBlueGrey"
	 * 
	 * @return **true** on success, **false** on error.
	 */
	private static final boolean recreateNimbusConfig(Color colorBase, Color colorBlueGrey) {
		
		// only recreate if something has changed
		boolean mustRecreate = colorBase != custom.get("nimbusBase");
		mustRecreate         = mustRecreate || ( colorBlueGrey != custom.get("nimbusBlueGrey") );
		if ( ! mustRecreate )
			return true;
		
		// switch to the requested look and feel
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
			
			// initialize colors
			custom.put( "nimbusBase",     colorBase     );
			custom.put( "nimbusBlueGrey", colorBlueGrey );
			return true;
		}
		catch (UnsupportedLookAndFeelException e) {
			return false;
		}
	}
	
	/**
	 * Adjust custom configuration for the Nimbus look and feel.
	 */
	private static final void initNimbus() {
		
		// nimbusBlueGrey affects too many things.
		// For example, we want to have
		// - neutral button borders --> nimbusBlueGrey = panel color; but:
		// - orange table headers   --> nimbusBlueGrey = orange
		// Both together doesn't work easily.
		// Trick:
		// 1. Create a temporary config
		// 2. Save the painters
		// repeat 1.-2. as often as needed
		// 3. Create the final config (and use the saved painters there)
		
		// 1. create temporary config (for table headers)
		custom.put( "nimbusBase",     COLOR_CHECKBOX        );
		custom.put( "nimbusBlueGrey", COLOR_CHECKBOX_BORDER );
		applyCustomDefaults();
		
		// 2. save painters
		Object cbxDisSelPainter      = UIManager.get("CheckBox[Disabled+Selected].iconPainter");
		Object cbxDisPainter         = UIManager.get("CheckBox[Disabled].iconPainter");
		Object cbxEnPainter          = UIManager.get("CheckBox[Enabled].iconPainter");
		Object cbxFoMoSelPainter     = UIManager.get("CheckBox[Focused+MouseOver+Selected].iconPainter");
		Object cbxFoMoPainter        = UIManager.get("CheckBox[Focused+MouseOver].iconPainter");
		Object cbxFoPrSelPainter     = UIManager.get("CheckBox[Focused+Pressed+Selected].iconPainter");
		Object cbxFoPrPainter        = UIManager.get("CheckBox[Focused+Pressed].iconPainter");
		Object cbxFoSelPainter       = UIManager.get("CheckBox[Focused+Selected].iconPainter");
		Object cbxFoPainter          = UIManager.get("CheckBox[Focused].iconPainter");
		Object cbxMoSelPainter       = UIManager.get("CheckBox[MouseOver+Selected].iconPainter");
		Object cbxMoPainter          = UIManager.get("CheckBox[MouseOver].iconPainter");
		Object cbxPrSelPainter       = UIManager.get("CheckBox[Pressed+Selected].iconPainter");
		Object cbxPrPainter          = UIManager.get("CheckBox[Pressed].iconPainter");
		Object cbxSelPainter         = UIManager.get("CheckBox[Selected].iconPainter");
		
		// 1. create temporary config (for table headers)
		recreateNimbusConfig(COLOR_TABLE_HEADER_SORTED_BG, COLOR_TABLE_HEADER_BG);
		applyCustomDefaults();
		
		// 2. save painters (table header and checkbox)
		Object tblRenEnBgPainter     = UIManager.get("TableHeader:\"TableHeader.renderer\"[Enabled].backgroundPainter");
		Object tblRenDisBgPainter    = UIManager.get("TableHeader:\"TableHeader.renderer\"[Disabled].backgroundPainter");
		Object tblRenDisSoBgPainter  = UIManager.get("TableHeader:\"TableHeader.renderer\"[Disabled+Sorted].backgroundPainter");
		Object tblRenEnFoSoBgPainter = UIManager.get("TableHeader:\"TableHeader.renderer\"[Enabled+Focused+Sorted].backgroundPainter");
		Object tblRenEnFoBgPainter   = UIManager.get("TableHeader:\"TableHeader.renderer\"[Enabled+Focused].backgroundPainter");
		Object tblRenEnSoBgPainter   = UIManager.get("TableHeader:\"TableHeader.renderer\"[Enabled+Sorted].backgroundPainter");
		Object tblRenMoBgPainter     = UIManager.get("TableHeader:\"TableHeader.renderer\"[MouseOver].backgroundPainter");
		Object tblRenPrBgPainter     = UIManager.get("TableHeader:\"TableHeader.renderer\"[Pressed].backgroundPainter");
		Object tblHdAscSortPainter   = UIManager.get("TableHeader[Enabled].ascendingSortIconPainter");
		Object tblHdDescSortPainter  = UIManager.get("TableHeader[Enabled].descendingSortIconPainter");
		
		// 1. create temporary config (for scroll bar arrow buttons)
		recreateNimbusConfig(COLOR_NIMBUS_BASE, COLOR_SCROLLBAR_SCRL_BTN);
		applyCustomDefaults();
		
		// 2. save painters
		Object scrlBtnFgEnPainter  = UIManager.get("ScrollBar:\"ScrollBar.button\"[Enabled].foregroundPainter");
		Object scrlBtnFgDisPainter = UIManager.get("ScrollBar:\"ScrollBar.button\"[Disabled].foregroundPainter");
		Object scrlBtnFgMsPainter  = UIManager.get("ScrollBar:\"ScrollBar.button\"[MouseOver].foregroundPainter");
		Object scrlBtnFgPrPainter  = UIManager.get("ScrollBar:\"ScrollBar.button\"[Pressed].foregroundPainter");
		
		// 1. create temporary config (for tree collapse/expand icons)
		recreateNimbusConfig(COLOR_NIMBUS_BASE, COLOR_TREE_COLLAPSE_EXPAND);
		applyCustomDefaults();
		
		// 2. save painters
		Object treeEnColIconPainter = UIManager.get("Tree[Enabled].collapsedIconPainter");
		Object treeEnSelIconPainter = UIManager.get("Tree[Enabled].expandedIconPainter");
		
		// 1. create temporary config (for tree collapse/expand icons)
		recreateNimbusConfig(COLOR_NIMBUS_BASE, COLOR_TREE_COLLAPSE_EXPAND);
		applyCustomDefaults();
		
		// 2. save painters
		Object menuDisArrPainter              = UIManager.get("Menu[Disabled].arrowIconPainter");
		Object menuEnSelArrPainter            = UIManager.get("Menu[Enabled+Selected].arrowIconPainter");
		Object menuEnArrPainter               = UIManager.get("Menu[Enabled].arrowIconPainter");
		Object menuEnSelBgPainter             = UIManager.get("Menu[Enabled+Selected].backgroundPainter");
		Object menBarMenSelBgPainter          = UIManager.get("MenuBar:Menu[Selected].backgroundPainter");
		Object menBarEnBgPainter              = UIManager.get("MenuBar[Enabled].backgroundPainter");
		Object menBarEnBorderPainter          = UIManager.get("MenuBar[Enabled].borderPainter");
		Object popMenDisBgPainter             = UIManager.get("PopupMenu[Disabled].backgroundPainter");
		Object popMenEnBgPainter              = UIManager.get("PopupMenu[Enabled].backgroundPainter");
		Object radioMenItDisSelChkIconPainter = UIManager.get("RadioButtonMenuItem[Disabled+Selected].checkIconPainter");
		Object radioMenItEnSelChkIconPainter  = UIManager.get("RadioButtonMenuItem[Enabled+Selected].checkIconPainter");
		Object radioMenItMoSelBgPainter       = UIManager.get("RadioButtonMenuItem[MouseOver+Selected].backgroundPainter");
		Object radioMenItMoSelChkIconPainter  = UIManager.get("RadioButtonMenuItem[MouseOver+Selected].checkIconPainter");
		Object radioMenItMoBgPainter          = UIManager.get("RadioButtonMenuItem[MouseOver].backgroundPainter");
		Object chkBxMenItMoBgPainter          = UIManager.get("CheckBoxMenuItem[MouseOver].backgroundPainter");
		Object menItMoBgPainter               = UIManager.get("MenuItem[MouseOver].backgroundPainter");
		
		// 1. create temporary config (for scroll bar background)
		recreateNimbusConfig(COLOR_NIMBUS_BASE, COLOR_SCROLLBAR_TRACK);
		applyCustomDefaults();
		
		// 2. save painters
		Object scrlEnabPainter  = UIManager.get("ScrollBar:ScrollBarTrack[Enabled].backgroundPainter");
		Object scrlDisabPainter = UIManager.get("ScrollBar:ScrollBarTrack[Disabled].backgroundPainter");
		
		// 1. create temporary config (for split pane dividers)
		recreateNimbusConfig(COLOR_NIMBUS_BASE, COLOR_DIVIDER);
		applyCustomDefaults();
		
		// 2. save painters
		Object dividerEnVertFgPainter = UIManager.get("SplitPane:SplitPaneDivider[Enabled+Vertical].foregroundPainter");
		Object dividerEnBgPainter     = UIManager.get("SplitPane:SplitPaneDivider[Enabled].backgroundPainter");
		Object dividerEnFgPainter     = UIManager.get("SplitPane:SplitPaneDivider[Enabled].foregroundPainter");
		Object dividerFocBgPainter    = UIManager.get("SplitPane:SplitPaneDivider[Focused].backgroundPainter");
		
		// 3. create final config
		recreateNimbusConfig(COLOR_NIMBUS_BASE, COLOR_PANEL);
		custom.put( "control",        COLOR_PANEL       );
		custom.put( "nimbusFocus",    COLOR_FOCUS       );
		
		// tooltip background
		custom.put( "info", COLOR_TOOLTIP_BACKGROUND );
		
		// menu (right click in a file chooser)
		custom.put( "Menu[Disabled].arrowIconPainter",                           menuDisArrPainter              );
		custom.put( "Menu[Enabled+Selected].arrowIconPainter",                   menuEnSelArrPainter            );
		custom.put( "Menu[Enabled].arrowIconPainter",                            menuEnArrPainter               );
		custom.put( "Menu[Enabled+Selected].backgroundPainter",                  menuEnSelBgPainter             );
		custom.put( "MenuBar:Menu[Selected].backgroundPainter",                  menBarMenSelBgPainter          );
		custom.put( "MenuBar[Enabled].backgroundPainter",                        menBarEnBgPainter              );
		custom.put( "MenuBar[Enabled].borderPainter",                            menBarEnBorderPainter          );
		custom.put( "PopupMenu[Disabled].backgroundPainter",                     popMenDisBgPainter             );
		custom.put( "PopupMenu[Enabled].backgroundPainter",                      popMenEnBgPainter              );
		custom.put( "RadioButtonMenuItem[Disabled+Selected].checkIconPainter",   radioMenItDisSelChkIconPainter );
		custom.put( "RadioButtonMenuItem[Enabled+Selected].checkIconPainter",    radioMenItEnSelChkIconPainter  );
		custom.put( "RadioButtonMenuItem[MouseOver+Selected].backgroundPainter", radioMenItMoSelBgPainter       );
		custom.put( "RadioButtonMenuItem[MouseOver+Selected].checkIconPainter",  radioMenItMoSelChkIconPainter  );
		custom.put( "RadioButtonMenuItem[MouseOver].backgroundPainter",          radioMenItMoBgPainter          );
		custom.put( "CheckBoxMenuItem[MouseOver].backgroundPainter",             chkBxMenItMoBgPainter          );
		custom.put( "MenuItem[MouseOver].backgroundPainter",                     menItMoBgPainter               );
		
		// selections
		custom.put( "nimbusSelectedText",                     COLOR_BLACK                 );
		custom.put( "nimbusSelectionBackground",              COLOR_SECONDARY_LIGHT       );
		custom.put( "nimbusSelection",                        COLOR_SECONDARY_LIGHT       );
		custom.put( "Table[Enabled+Selected].textForeground", COLOR_BLACK                 );
		custom.put( "Table[Enabled+Selected].textBackground", COLOR_SECONDARY_LIGHT       );
		custom.put( "List[Selected].textBackground",          COLOR_SECONDARY_LIGHT       );
		custom.put( "List[Selected].textForeground",          COLOR_BLACK                 );
		custom.put( "TextArea[Selected].textBackground",      COLOR_PRIMARY_DARK          );
		custom.put( "TextArea[Selected].textForeground",      COLOR_SECONDARY_LIGHT       );
		custom.put( "Tree.selectionForeground",               COLOR_MSG_TREE_SELECTED_TXT );
		
		// trees
		BackgroundPainter treeBgPainter = new BackgroundPainter( COLOR_SECONDARY_LIGHT, COLOR_MSG_TREE_SELECTED_BG );
		custom.put( "Tree:TreeCell[Enabled+Selected].backgroundPainter", treeBgPainter        );
		custom.put( "Tree:TreeCell[Focused+Selected].backgroundPainter", treeBgPainter        );
		custom.put( "Tree[Enabled+Selected].collapsedIconPainter",       null                 );
		custom.put( "Tree[Enabled+Selected].expandedIconPainter",        null                 );
		custom.put( "Tree[Enabled].collapsedIconPainter",                treeEnColIconPainter );
		custom.put( "Tree[Enabled].expandedIconPainter",                 treeEnSelIconPainter );
		
		// borders
		custom.put( "nimbusBorder",             COLOR_BORDER );
		custom.put( "TitledBorder.titleColor",  COLOR_BORDER );
		
		// sliders
		custom.put( "Slider.tickColor",  COLOR_BORDER );
		custom.put( "Slider.paintThumbArrowShape", true );
		BackgroundPainter trackPainter = new BackgroundPainter( COLOR_SECONDARY_LIGHT, COLOR_SECONDARY );
		custom.put( "Slider:SliderTrack[Enabled].backgroundPainter", trackPainter );
		
		// tables
		custom.put("Table.showGrid",          true);
		custom.put("Table.gridColor",         COLOR_TABLE_GRID);
		custom.put("Table.alternateRowColor", COLOR_PANEL);
		custom.put("TableHeader:\"TableHeader.renderer\"[Enabled].backgroundPainter",                tblRenEnBgPainter);
		custom.put("TableHeader:\"TableHeader.renderer\"[Disabled].backgroundPainter",               tblRenDisBgPainter);
		custom.put("TableHeader:\"TableHeader.renderer\"[Disabled+Sorted].backgroundPainter",        tblRenDisSoBgPainter);
		custom.put("TableHeader:\"TableHeader.renderer\"[Enabled+Focused+Sorted].backgroundPainter", tblRenEnFoSoBgPainter);
		custom.put("TableHeader:\"TableHeader.renderer\"[Enabled+Focused].backgroundPainter",        tblRenEnFoBgPainter);
		custom.put("TableHeader:\"TableHeader.renderer\"[Enabled+Sorted].backgroundPainter",         tblRenEnSoBgPainter);
		custom.put("TableHeader:\"TableHeader.renderer\"[MouseOver].backgroundPainter",              tblRenMoBgPainter);
		custom.put("TableHeader:\"TableHeader.renderer\"[Pressed].backgroundPainter",                tblRenPrBgPainter);
		custom.put("TableHeader[Enabled].ascendingSortIconPainter",                                  tblHdAscSortPainter);
		custom.put("TableHeader[Enabled].descendingSortIconPainter",                                 tblHdDescSortPainter);
		
		// scrollbars
		custom.put("ScrollBar:ScrollBarTrack[Enabled].backgroundPainter",         scrlEnabPainter);
		custom.put("ScrollBar:ScrollBarTrack[Disabled].backgroundPainter",        scrlDisabPainter);
		custom.put("ScrollBar:\"ScrollBar.button\"[Enabled].foregroundPainter",   scrlBtnFgEnPainter);
		custom.put("ScrollBar:\"ScrollBar.button\"[Disabled].foregroundPainter",  scrlBtnFgDisPainter);
		custom.put("ScrollBar:\"ScrollBar.button\"[MouseOver].foregroundPainter", scrlBtnFgMsPainter);
		custom.put("ScrollBar:\"ScrollBar.button\"[Pressed].foregroundPainter",   scrlBtnFgPrPainter);
		
		// dividers for split panes
		custom.put("SplitPane:SplitPaneDivider[Enabled+Vertical].foregroundPainter", dividerEnVertFgPainter);
		custom.put("SplitPane:SplitPaneDivider[Enabled].backgroundPainter",          dividerEnBgPainter);
		custom.put("SplitPane:SplitPaneDivider[Enabled].foregroundPainter",          dividerEnFgPainter);
		custom.put("SplitPane:SplitPaneDivider[Focused].backgroundPainter",          dividerFocBgPainter);
		
		// checkboxes
		custom.put("CheckBox[Disabled+Selected].iconPainter",          cbxDisSelPainter);
		custom.put("CheckBox[Disabled].iconPainter",                   cbxDisPainter);
		custom.put("CheckBox[Enabled].iconPainter",                    cbxEnPainter);
		custom.put("CheckBox[Focused+MouseOver+Selected].iconPainter", cbxFoMoSelPainter);
		custom.put("CheckBox[Focused+MouseOver].iconPainter",          cbxFoMoPainter);
		custom.put("CheckBox[Focused+Pressed+Selected].iconPainter",   cbxFoPrSelPainter);
		custom.put("CheckBox[Focused+Pressed].iconPainter",            cbxFoPrPainter);
		custom.put("CheckBox[Focused+Selected].iconPainter",           cbxFoSelPainter);
		custom.put("CheckBox[Focused].iconPainter",                    cbxFoPainter);
		custom.put("CheckBox[MouseOver+Selected].iconPainter",         cbxMoSelPainter);
		custom.put("CheckBox[MouseOver].iconPainter",                  cbxMoPainter);
		custom.put("CheckBox[Pressed+Selected].iconPainter",           cbxPrSelPainter);
		custom.put("CheckBox[Pressed].iconPainter",                    cbxPrPainter);
		custom.put("CheckBox[Selected].iconPainter",                   cbxSelPainter);
		
		// inactive (disabled) elements
		custom.put("TextField[Disabled].textForeground",    COLOR_PRIMARY_DARK);
		custom.put("TextField[Disabled].backgroundPainter", new BackgroundPainter(COLOR_INACTIVE));
		custom.put("TextField[Disabled].borderPainter",     new BorderPainter(COLOR_PRIMARY_DARK));
		custom.put("Button[Disabled].backgroundPainter",    new BackgroundPainter(COLOR_INACTIVE, COLOR_PRIMARY_DARK));
		custom.put("Button[Disabled].textForeground",       COLOR_PRIMARY_DARK);
		
		// specific customizations
		Set<String> keys = custom.keySet();
		for (String key : keys) {
			Object value = custom.get(key);
			customButtonPrim.put(key, value);
			customButtonSec.put(key, value);
		}
		
		// secondary buttons and toggle buttons
		customButtonSec.put("Button.background",       COLOR_BUTTON_SEC_NIMBUS);
		customButtonSec.put("ToggleButton.background", COLOR_BUTTON_SEC_NIMBUS);
		
		// primary buttons
		customButtonPrim.put("Button.background", COLOR_BUTTON_PRIMARY);
	}
	
	/**
	 * Adjust custom configuration for the Metal look and feel.
	 */
	private static final void initMetal() {
		
		// tooltip colors
		custom.put("ToolTip.foreground", COLOR_TOOLTIP_FOREGROUND);
		custom.put("ToolTip.background", COLOR_TOOLTIP_BACKGROUND);
	}
	
	/**
	 * Applies the LAF specific custom settings to the UIManager and it's default settings.
	 */
	private static final void applyCustomDefaults() {
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		Set<String> keys = custom.keySet();
		for (String key : keys) {
			Object value = custom.get(key);
			UIManager.put(key, value);
			defaults.put(key, value);
		}
		
		// primary buttons
		keys = customButtonPrim.keySet();
		for (String key : keys) {
			Object value = customButtonPrim.get(key);
			buttonPrimDefaults.put(key, value);
		}
		
		// secondary buttons
		keys = customButtonSec.keySet();
		for (String key : keys) {
			Object value = customButtonSec.get(key);
			buttonSecDefaults.put(key, value);
		}
	}
	
	/**
	 * Applies the LAF specific custom settings to the given component.
	 * 
	 * @param component  The component to be applied.
	 */
	public static final void applyLaf(JComponent component) {
		Set<String> keys = custom.keySet();
		for (String key : keys) {
			Object value = custom.get(key);
			component.putClientProperty(key, value);
		}
	}
	
	/**
	 * Changes the button color.
	 * Primary buttons will get a different color.
	 * 
	 * This is called from:
	 * 
	 * - the constructor of a {@link MidicaButton}
	 * - the {@link MidicaFileChooser}
	 * 
	 * @param button     the button
	 * @param isPrimary  **true**, if it's a primary button
	 */
	public static final void applyLafToButton(AbstractButton button, boolean isPrimary) {
		
		if (!isNimbus)
			return;
		
		button.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		if (isPrimary) {
			button.putClientProperty("Nimbus.Overrides", buttonPrimDefaults);
		}
		else {
			button.putClientProperty("Nimbus.Overrides", buttonSecDefaults);
		}
		SwingUtilities.updateComponentTreeUI(button);
	}
	
	/**
	 * Gives the given component a bold font.
	 * 
	 * @param component  The component to make bold.
	 */
	public static void makeBold(JComponent component) {
		if (BOLD_LABEL_FONT != null) {
			component.setFont(BOLD_LABEL_FONT);
		}
	}
}
