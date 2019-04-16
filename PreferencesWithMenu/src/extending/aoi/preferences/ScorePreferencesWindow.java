/**
 * 
 */
package extending.aoi.preferences;

import java.awt.Insets;

import extending.aoi.preferences.ScorePreferences.HideShow;
import artofillusion.ui.PanelDialog;
import buoy.widget.BCheckBox;
import buoy.widget.BFrame;
import buoy.widget.BTabbedPane;
import buoy.widget.FormContainer;
import buoy.widget.LayoutInfo;
import buoy.widget.Widget;

/**
 * Animation Score Preferences window used to get preferences for 
 * the animation score. 
 * @author Timothy Fish
 *
 */
public class ScorePreferencesWindow {

	private final String tabName = "Score";
	private final String visibilityCheckBoxName = "Show Animation Score at Start Up";
	private final String prefsTitle = "Animation Score Preferences";
	private ScorePreferences preferences;
	private BCheckBox scoreBox;
	private static int lastTab;

	/**
	 * 
	 */
	public ScorePreferencesWindow(BFrame parent, ScorePreferences prefs) {
		preferences = prefs;
		BTabbedPane tabs = new BTabbedPane();
	    tabs.add(createScorePanel(), tabName );
	    
	    tabs.setSelectedTab(lastTab);
	    // TODO: Investigate why we have this loop. It doesn't look like it does anything.
	    boolean done = false;
	    while (!done)
	    {
	      PanelDialog dlg = new PanelDialog(parent, prefsTitle, tabs);
	      lastTab = tabs.getSelectedTab();
	      if (!dlg.clickedOk())
	        return;
	      done = true;
	    }
	    
	    if(scoreBox.getState() == true){ // checked
	      preferences.setPreferenceScoreVisibility(HideShow.show);
	    }
	    else{ // not checked
	      preferences.setPreferenceScoreVisibility(HideShow.hide);
	    }
	    preferences.savePreferences();
	}

	private Widget createScorePanel() {
		boolean scoreChecked = preferences.getPreferenceScoreVisibility() == HideShow.hide ? false : true;
		scoreBox = new BCheckBox(visibilityCheckBoxName, scoreChecked);
	    // Layout the panel.

	    FormContainer panel = new FormContainer(2, 12);
	    panel.setColumnWeight(1, 1.0);
//	    LayoutInfo labelLayout = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 0, 2, 5), null);
//	    LayoutInfo widgetLayout = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH, new Insets(2, 0, 2, 0), null);
	    LayoutInfo centerLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(2, 0, 2, 0), null);

	    panel.add(scoreBox, 0, 0, 2, 1, centerLayout);
	    return panel;
	}


}
