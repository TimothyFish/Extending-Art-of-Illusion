/**
 * Thread to control the active status of the Place on menu item.
 */
package extending.aoi.place_on;

import artofillusion.LayoutWindow;
import buoy.widget.BMenuItem;

/**
 * @author Timothy Fish
 *
 */
public class PlaceOnMenuItemActivator extends Thread {
	private BMenuItem theMenuItem;
	private LayoutWindow theLayout;
	private static final long inverseRefreshRate = 250; // refresh menuItem every quarter-second

	/**
	 * Constructor
	 * @param menuItem Non-null menu item 
	 * @param layout non-null layout window
	 */
	public PlaceOnMenuItemActivator(BMenuItem menuItem, LayoutWindow layout) {
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
			if(anObjectIsSelected()){
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
	 * Function to determine if one and only one object is selected.
	 * @return true if there are objects selected, false if not
	 */
	private boolean anObjectIsSelected() {
		int numSelected = theLayout.getSelectedObjects().size();
		final int properSize = 1;
		return (numSelected == properSize);
	}

}