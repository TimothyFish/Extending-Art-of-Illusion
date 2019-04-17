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
import artofillusion.Scene;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.PanelDialog;
import buoy.widget.BCheckBox;
import buoy.widget.BComboBox;
import buoy.widget.BLabel;
import buoy.widget.ColumnContainer;

/**
 * Window that the user sees when he chooses to have the tracker
 * point at an object.
 * @author Timothy Fish
 *
 */
public class TrackerObjectSelectionWindow {
	/**
	 * Class to setup the layout of the TrackerObjectSelectionWindow.
	 */
	private class TrackDialogPanel extends ColumnContainer {
		private BComboBox theObjectsList;
		Collection<ObjectInfo> theObjects;
		private BCheckBox theXCheckBox;
		private BCheckBox theYCheckBox;
		private BCheckBox theZCheckBox;
		private BLabel theObjectsListLabel;
		
		/**
		 * Constructor - handles widget placement
		 */
		TrackDialogPanel(Collection<ObjectInfo> objects, int targetId, boolean xChecked, boolean yChecked, boolean zChecked){
			super(); // Call the ColumnContainer constructor
			
			theObjects = objects;
			
			theObjectsListLabel = new BLabel();
			theObjectsListLabel.setText("Target");
			add(theObjectsListLabel);
			
			theObjectsList = new BComboBox();
			theObjectsList.add("No Object");
			int targetIndex = 0;
			int index = 0;
			// Set the display text. Note that getName() and getString() are very different.
			for(ObjectInfo object : objects ){
				index++;
				theObjectsList.add(object.getName());
				if(object.getId() == targetId){
					targetIndex = index;
				}
			}
			theObjectsList.setSelectedIndex(targetIndex);
			add(theObjectsList);
			
			theXCheckBox = new BCheckBox();
			theXCheckBox.setText("Lock X Axis");
			theXCheckBox.setState(xChecked);
			add(theXCheckBox);
			
			theYCheckBox = new BCheckBox();
			theYCheckBox.setText("Lock Y Axis");
			theYCheckBox.setState(yChecked);
			add(theYCheckBox);
			
			theZCheckBox = new BCheckBox();
			theZCheckBox.setText("Lock Z Axis");
			theZCheckBox.setState(zChecked);
			add(theZCheckBox);
		}

		/**
		 * Return the ObjectInfo for the object the user selected.
		 * @return
		 */
		public ObjectInfo getPointee() {
			// The "No Object" choice places one more item in the list
			// than the Array has. So remove it and either return
			// the object in the array or null if No Object selected.
			int selectedIndex =  theObjectsList.getSelectedIndex()-1;
			if(selectedIndex >= 0){
				return (ObjectInfo)theObjects.toArray()[selectedIndex];
			}
			else{
				return null;
			}
		}

	}

	private ObjectInfo pointee;
	private final String windowTitle = "Tracker Options";
	private TrackDialogPanel thePanel;
	
	/**
	 * Displays window for the user to select the pointee.
	 * @param parent
	 */
	public TrackerObjectSelectionWindow(LayoutWindow parent, int targetId, boolean xLocked, boolean yLocked, boolean zLocked){
		Scene scene = parent.getScene();
		
		pointee = null;
		
		Collection<ObjectInfo> selectableObjects = scene.getAllObjects();
		thePanel = new TrackDialogPanel(selectableObjects, targetId, xLocked, yLocked, zLocked);

		PanelDialog dlg = new PanelDialog(parent, windowTitle , thePanel);
		
		if (dlg.clickedOk()){
			// code for what to do after user accepts
			pointee = thePanel.getPointee();
		}
		else{
			// User clicked cancel, so everything should remain unchanged.
			;
		}
	}

	/**
	 * Returns the object we're to point at. Returns null if none selected.
	 * @return
	 */
	public ObjectInfo getPointee() {
		return pointee;
	}

	public boolean getXLocked() {
		return thePanel.theXCheckBox.getState();
	}

	public boolean getYLocked() {
		return thePanel.theYCheckBox.getState();
	}

	public boolean getZLocked() {
		return thePanel.theZCheckBox.getState();
	}
}
