/**
    Drop to Floor Plugin from Chapter 3 of the book "Extending Art of Illusion: Scripting 3D Scene Creation"
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
package extending.aoi.drop_to_floor;

import artofillusion.LayoutWindow;
import buoy.widget.BMenuItem;

/**
 * Thread to check for object selection after Drop To Floor menu item created
 * @author Timothy Fish
 *
 */
public class MenuItemActivator extends Thread {
	private BMenuItem theMenuItem;
	private LayoutWindow theLayout;
	private static final long inverseRefreshRate = 250; // refresh menuItem every quarter-second

	/**
	 * Constructor
	 * @param menuItem Non-null menu item 
	 * @param layout non-null layout window
	 */
	public MenuItemActivator(BMenuItem menuItem, LayoutWindow layout) {
		// Assumption: theMenuItem && theLayout are not null
		theMenuItem = menuItem;
		theLayout = layout;
	}

	/**
	 * Method that does checks the selection status.
	 */
	@Override
	public void run() {
		while(theMenuItem != null && theLayout != null){ // do while menu item exists
			// Check for object selection
			if(objectsAreSelected()){
				theMenuItem.setEnabled(true);
			}
			else{
				theMenuItem.setEnabled(false);
			}
			try {
				// Sleep most of the time so processor can do other things
				// Updating every quarter-second is more than sufficient since
				// the user can't select the menu item that quickly after object selection.
				Thread.sleep(inverseRefreshRate );
			} catch (InterruptedException e) {
				// This should never happen.
				e.printStackTrace();
			}
		}		
	}

	/**
	 * Function to determine if there is at least one object selected.
	 * @return true if there are objects selected, false if not
	 */
	private boolean objectsAreSelected() {
		return !theLayout.getSelectedObjects().isEmpty();
	}

}
