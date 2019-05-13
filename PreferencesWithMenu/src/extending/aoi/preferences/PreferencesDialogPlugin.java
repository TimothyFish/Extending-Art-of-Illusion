/**
    Preferences Dialog Plugin from Chapter 2 of the book "Extending Art of Illusion: Scripting for 3D Artists"
    Copyright (C) 2019, 2011  Timothy Fish

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package extending.aoi.preferences;

import javax.swing.SwingUtilities;

import buoy.widget.BMenu;
import buoy.widget.BMenuItem;

import extending.aoi.preferences.ScorePreferences.HideShow;
import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.ui.Translate;

public class PreferencesDialogPlugin implements Plugin {

	/**
	 * Runnable thread that will set the visibility of the score window after
	 * Art of Illusion has had time to initialize.
	 * 
	 * @author Timothy Fish
	 * 
	 */
	private class HideScoreRunnable implements Runnable {

		private HideShow visibility;

		/**
		 * Constructor
		 * 
		 * @param visibility
		 *            Set whether the score should show or hide.
		 * 
		 */
		public HideScoreRunnable(HideShow visibility) {
			this.visibility = visibility;
		}

		@Override
		/*
		 * This function is called by the JVE when it is ready to start the
		 * thread.
		 */
		public void run() {
			// Show or Hide the Score Window
			switch (visibility) {
			case hide:
				layout.setScoreVisible(false);
				break;
			case show:
				layout.setScoreVisible(true);
				break;
			}
		}
	}

	private LayoutWindow layout; // local reference to the main layout window
	
	/**
	 * The firstTimeCalled flag is used to prevent AOI from calling this plugin more
	 * than once. Without this check, the animation score will remain hidden, even
	 * if the user selects the Show Score menu item.
	 */
	private boolean firstTimeCalled;

	private ScorePreferences preferences;

	// private HideScorePreferences preferences;

	/**
	 * Constructor
	 */
	public PreferencesDialogPlugin() {
		layout = null;
		firstTimeCalled = true;
		preferences = null;
	}

	@Override
	/*
	 * This is the main function of a plugin. Art of Illusion calls this
	 * function at various times to pass control to the plugin. In this case, it
	 * will read the preferences file and either show or hide the animation
	 * score based on the user's preferences.
	 * 
	 * @param message Provides execution state information.
	 * 
	 * @param args Generic variable to pass state dependent information.
	 */
	public void processMessage(int message, Object[] args) {
		BMenuItem menuItem;
		switch (message) {
		case Plugin.SCENE_WINDOW_CREATED:
			layout = (LayoutWindow) args[0];
			
			BMenu animationMenu = layout.getAnimationMenu();
			// Add the Animation Score Preferences menu item to the Animation Menu
			animationMenu.addSeparator();
			menuItem = Translate.menuItem("Score Preferences...", this, "scoreMenuAction");
	        animationMenu.add(menuItem);	
			
			if (firstTimeCalled) {
				// It is possible for the scene window to be created more than once
				// after AOI is started.
				// Use score visibility preference first time. After that use normal user input.
				preferences = new ScorePreferences();
				HideShow preferenceHide = preferences.getPreferenceScoreVisibility();
				setAnimationScoreVisibility(preferenceHide);
				firstTimeCalled = false;
			}
			break;
		}
	}

	/**
	 * Sets the animation score visibility after Art of Illusion has time to
	 * initialize.
	 * 
	 * @param visibility
	 *            Whether the function should show or hide the animation score.
	 */
	private void setAnimationScoreVisibility(HideShow visibility) {
		// Create a Runnable to set the animation score visibility.
		HideScoreRunnable scoreVisibilitySetter = new HideScoreRunnable(visibility);

		// Schedule the Runnable to happen later.
		SwingUtilities.invokeLater(scoreVisibilitySetter);
	}
	
	@SuppressWarnings("unused")
	/**
	 * Action code for the Score Preferences menu item.
	 */
	private void scoreMenuAction(){
		new ScorePreferencesWindow(layout, preferences);
	}


}
