/**
    Preferences Dialog Plugin from Chapter 2 of the book "Extending Art of Illusion: Scripting 3D Scene Creation"
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import artofillusion.ApplicationPreferences;


/**
 * The ScorePreferences class is modeled after the ApplicationPreferences class.
 * It is the means by which user preferences for the visibility of the animation
 * score can be stored and retrieved when AOI loads.
 * @author Timothy Fish
 *
 */
public class ScorePreferences {
	
	/**
	 * Enumerated type to pass information about whether the score should show
	 * or hide when the run() function is called.
	 * 
	 * @author Timothy Fish
	 * 
	 */
	public enum HideShow {
		hide, show
	}

	private HideShow preferenceScoreVisibility;
	private Properties properties;
	private final String ScoreVisibilityName = "animationScoreVisibility";
	private final String visibleStr = "show";
	private final String hiddenStr = "hide";
	private final String prefFileName = "scoreprefs";
	
	/**
	 * Constructor 
	 */
	public ScorePreferences(){
		// Set the hide/show preference to show. (AOI default)
		setPreferenceScoreVisibility(HideShow.show);
		
		// Open the Score Preferences file in the default location
	    File f = new File(ApplicationPreferences.getPreferencesDirectory(), prefFileName);
	    if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException ex) {
				// if exception, print error information
				ex.printStackTrace();
			}
		}

	    try
	      {
			// if no problem, load the preferences from the file
	        InputStream in = new BufferedInputStream(new FileInputStream(f));
	        loadPreferences(in);
	        in.close();
	      }
	    catch (IOException ex)
	      {
			// if exception, print error information
	        ex.printStackTrace();
	      }
	}
	
	  /** 
	   * Load the preferences from an InputStream. 
	   */
	  private void loadPreferences(InputStream in) throws IOException
	  {
	    properties = new Properties();
	    properties.load(in);
	    parsePreferences();
	  }
	  
	  /**
	   * Save any changed preferences to disk. 
	   */
	  public void savePreferences()
	  {
	    // copy visibility setting into properties

		if(this.preferenceScoreVisibility == HideShow.hide){
	        properties.put(ScoreVisibilityName, hiddenStr);
		}
		else{
			properties.put(ScoreVisibilityName, visibleStr);
		}
			
	    // Write the preferences to a file.

	    File f = new File(ApplicationPreferences.getPreferencesDirectory(), prefFileName);
	    try
	      {
	        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
	        properties.store(out, "Art of Illusion Animation Score Preferences File");
	        out.close();
	      }
	    catch (IOException ex)
	      {
	        ex.printStackTrace();
	      }
	  }

	  /** 
	   * Parse the properties loaded from the preferences file. 
	   */
	private void parsePreferences() {
		// Parse value for animationScoreVisibility.
		this.preferenceScoreVisibility = parseHideShowProperty(ScoreVisibilityName, HideShow.show);
		
	}

	/**
	 * Get a boolean value from the properties read from preference file.
	 * @param name Property Name
	 * @param defaultVal Value to use if value left blank.
	 * @return Value of property as a HideShow enumerated type value.
	 */
	private HideShow parseHideShowProperty(String name, HideShow defaultVal) {
		// Get the property from the pair.
	    String prop = properties.getProperty(name);
	       
	    // Set the default return value.
	    HideShow retVal = defaultVal;
	    	    
	    // If the property has a value, get the boolean representation and return it.
	    if (prop != null){
	    	if(prop.compareToIgnoreCase(hiddenStr) == 0){
	    		retVal = HideShow.hide;
	    	}
	    	else if(prop.compareToIgnoreCase(visibleStr) == 0){
	    		retVal = HideShow.show;
	    	}
	    	else{
	    		// do nothing, retVal is already defaultVal
	    	}
	    }
	    return retVal;
	}

	/**
	 * Set the user's preference for whether the animation score should be
	 * visible or not.
	 * @param preferenceScoreVisibility
	 */
	public void setPreferenceScoreVisibility(final HideShow preferenceScoreVisibility) {
		this.preferenceScoreVisibility = preferenceScoreVisibility;
	}

	/**
	 * Get the user's preference for whether the animation score should be
	 * visible or not.
	 * @return
	 */
	public HideShow getPreferenceScoreVisibility() {
		return preferenceScoreVisibility;
	}
}
