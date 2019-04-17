/**
    Tracker Plugin from Chapter 7 of the book "Extending Art of Illusion: Scripting 3D Scene Creation"
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
package extending.aoi.tracker;

import java.util.Collection;

import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.UndoRecord;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.ToolPalette;
import artofillusion.ui.Translate;
import buoy.widget.BMenu;
import buoy.widget.BMenuItem;

/**
 * @author Timothy Fish
 *
 */
public class TrackerPlugin implements Plugin {

	private LayoutWindow layout;
	private ToolPalette toolPalette;
	private CreateTrackerTool theTrackerTool;
	private ObjectInfo pointee;
	private boolean xLock;
	private boolean yLock;
	private boolean zLock;
		
	public TrackerPlugin(){
		layout = null;
		toolPalette = null;
		theTrackerTool = null;
		
	}
	/**
	 * Entry point for the plugin.
	 */
	@Override
	public void processMessage(int message, Object[] args) {
		switch (message) {
		case Plugin.SCENE_WINDOW_CREATED:
			// Do initialization first.
			layout = (LayoutWindow) args[0];
		    toolPalette = layout.getToolPalette();	
			theTrackerTool = new CreateTrackerTool(layout);
			
			// Add the Tracker button to the Tool Palette
			toolPalette.addTool(theTrackerTool);

			BMenu objectMenu = layout.getObjectMenu();
			// Add menu item to the Object menu
			// Locate position after "Convert To Actor..."
			int posConvertToActor = 0; 
			for(int i = 0; i < objectMenu.getChildCount(); i++){
				BMenuItem menuIter = null;
				menuIter = (BMenuItem) objectMenu.getChild(i);
				if(menuIter.getText().equalsIgnoreCase("Convert To Actor...")){
					posConvertToActor = i;
					break;
				}
			}
			posConvertToActor++; // select position after Convert To Actor or top if not found

			// Create menu item that will call drop to floor code
			BMenuItem menuItem = Translate.menuItem("Track Object...", this, "trackMenuAction");

			// Insert menu item
			objectMenu.add(menuItem, posConvertToActor);

			// Disable menu item (until object selected)
			controlEnableDisable(menuItem);


			break;
		}

	}
	
	/**
	 * Enables/disables the menu item based when conditions change that 
	 * determine whether the menu item has an object to work on or not.
	 * @param menuItem
	 */
	private void controlEnableDisable(BMenuItem menuItem) {

		// Default to disabled, so we can't select it without a selection
		menuItem.setEnabled(false);

		// Schedule thread to set the enabled/disable status of the menuItem
		new TrackerMenuItemActivator(menuItem, layout).start();
	}

	/**
	 * Action code for the Tracker menu item.
	 */
	@SuppressWarnings("unused")
	private void trackMenuAction(){

		Collection<ObjectInfo> objects = layout.getSelectedObjects();
		if(!objects.isEmpty()){
			// Create a new UndoRecord. This will be built as we
			// make changes, so we can go back to the previous state
			// of anything we changed.
			UndoRecord undo = new UndoRecord(layout, true);

			if(setPointeeAndLocksFromUserInput()){
				for(ObjectInfo obj : objects){
					// Only works for Trackers
					if(obj.getObject() instanceof Tracker){
						Tracker t = (Tracker)obj.getObject();
						if(obj != this.pointee){ // don't point something at itself
							Tracker.pointAt(obj, pointee);
							t.lockXAxis = this.xLock;
							t.lockYAxis = this.yLock;
							t.lockZAxis = this.zLock;
						}
					}
				}


			}
			else{
				// Stop tracking an object.
				for(ObjectInfo obj : objects){
					// Only works for Trackers
					if(obj.getObject() instanceof Tracker){
						Tracker.pointAt(obj, null);
					}
				}
			}
			layout.updateImage();

			// Tell the layout window that it can store what we've said
			// is the previous state of the object.
			layout.setUndoRecord(undo);
		}
	}
	/**
	 * Displays a window for the user to select whether he wants objA to
	 * point at objB or objB to point at objA.
	 */
	private boolean setPointeeAndLocksFromUserInput() {
		Collection<ObjectInfo> objects = layout.getSelectedObjects();
		Tracker theFirstTracker = null; 
		for(ObjectInfo obj : objects){
			// Find first selected tracker
			if(obj.getObject() instanceof Tracker){
				theFirstTracker = (Tracker) obj.getObject();
				break;
			}
		}
		TrackerObjectSelectionWindow window = 
			new TrackerObjectSelectionWindow(layout, 
					                         theFirstTracker.getTargetId(),
					                         theFirstTracker.lockXAxis,
					                         theFirstTracker.lockYAxis,
					                         theFirstTracker.lockZAxis);

		pointee = window.getPointee();
		xLock = window.getXLocked();
		yLock = window.getYLocked();
		zLock = window.getZLocked();
		
		return (pointee != null);
	}
}
