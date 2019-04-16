package extending.aoi.tracker;

import artofillusion.LayoutWindow;
import artofillusion.object.ObjectInfo;
import buoy.widget.BMenuItem;

public class TrackerMenuItemActivator extends Thread {
	private BMenuItem theMenuItem;
	private LayoutWindow theLayout;
	private static final long inverseRefreshRate = 250; // refresh menuItem every quarter-second

	/**
	 * Constructor
	 * @param menuItem Non-null menu item 
	 * @param layout non-null layout window
	 */
	public TrackerMenuItemActivator(BMenuItem menuItem, LayoutWindow layout) {
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
			if(onlyTrackersAreSelected()){
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
	 * Function to determine if at least one object is selected.
	 * @return true if there are objects selected, false if not
	 */
	private boolean onlyTrackersAreSelected() {
		int numSelected = theLayout.getSelectedObjects().size();
		final int properSize = 1;
		boolean ret = false;
		if(numSelected >= properSize){
			for(ObjectInfo obj : theLayout.getSelectedObjects()){
				if(obj.getObject() instanceof Tracker){
					// must have a tracker to return true
					ret = true;
				}
				else{
					// return false if any are not Trackers
					ret = false;
					break; 
				}
			}
		}
		return ret;
	}
}
