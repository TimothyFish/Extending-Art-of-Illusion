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

import java.util.Collection;

import buoy.widget.BMenu;
import buoy.widget.BMenuItem;
import buoy.widget.Shortcut;
import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.UndoRecord;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import artofillusion.object.MeshVertex;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;
import artofillusion.object.TriangleMesh;
import artofillusion.ui.Translate;

/**
 * Plugin that can be used to drop the selected objects to the floor
 * of the scene. The floor is defined as 0 along the Y axis.
 * @author Timothy Fish
 *
 */
public class DropToFloorPlugin implements Plugin {

	private LayoutWindow layout;

	/* (non-Javadoc)
	 * @see artofillusion.Plugin#processMessage(int, java.lang.Object[])
	 */
	@Override
	public void processMessage(int message, Object[] args) {
		switch (message) {
		case Plugin.SCENE_WINDOW_CREATED:
			layout = (LayoutWindow) args[0];
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
			Shortcut shortcut = new Shortcut('D', Shortcut.SHIFT_MASK | Shortcut.DEFAULT_MASK); 
			// Use a shortcut because we'll use this feature frequently.
			// We'll use Ctrl+Shift+D because AOI already uses Ctrl+D for editing keyframes.
			BMenuItem menuItem = Translate.menuItem("Drop to Floor", this, "dropMenuAction", shortcut);

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
		new MenuItemActivator(menuItem, layout).start();
	}


	/**
	 * Action code for the Drop to Floor menu item.
	 */
	@SuppressWarnings("unused")
	private void dropMenuAction(){
		

		// Get each selected object in turn.
		Collection<ObjectInfo> Objects = layout.getSelectedObjects();
		
		// if no selected objects, just return;
		if(Objects.isEmpty()){
			return;
		}
		
		// Create a new UndoRecord. This will be built as we
		// make changes, so we can go back to the previous state
		// of anything we changed.
		UndoRecord undo = new UndoRecord(layout, true);
		
		for(ObjectInfo obj : Objects){
			// Find the lowest point of the object.
			double newY = 0 - getLowestPointOfObjectGroup(obj).y;

			// Reposition the Object
			repositionObjectAndChildren(obj, newY, undo);

		}

		// Inform the layout window that we're changed something.
		layout.updateImage();

		// Tell the layout window that it can store what we've said
		// is the previous state of the object.
		layout.setUndoRecord(undo);
	}

	/**
	 * Reposition the object and non-selected children so that the group is on 
	 * the floor, where "floor" is defined as the plane where Y = 0.
	 * The non-selected children will move with the parent, but the selected
	 * children will be repositioned to the floor.
	 * @param obj the object to move
	 * @param undo 
	 * @param lowY lowest value of Y for the group
	 */
	private void repositionObjectAndChildren(ObjectInfo obj, double newY, UndoRecord undo) {

		// Get the children that aren't selected. These will move the same amount as
		// the parent object. The selected children will be placed on floor.
		ObjectInfo[] notSelectedChildren = getNotSelectedChildren(obj);
		for(ObjectInfo childObj : notSelectedChildren){

			double distanceChildFromParent = getDistanceY(obj, childObj);
			double childNewY = newY + distanceChildFromParent;
			repositionObjectAndChildren(childObj, childNewY, undo);
		}

		// Reposition the object itself.
		repositionObject(obj, newY, undo);	
	}


	/**
	 * Calculates the distance between the origins of two objects in the Y direction.
	 * @param obj
	 * @param childObj
	 * @return
	 */
	private double getDistanceY(ObjectInfo obj, ObjectInfo childObj) {
		Vec3 objPoint = obj.getCoords().getOrigin();
		Vec3 childPoint = childObj.getCoords().getOrigin();
		double distanceChildFromParent = (childPoint.y - objPoint.y);
		return distanceChildFromParent;
	}


	/**
	 * Reposition the object so that it is on the floor, where "floor" is 
	 * defined as the plane where Y = 0.
	 * @param obj object to move
	 * @param newY lowest value of Y for the object origin
	 * @param undo 
	 */
	private void repositionObject(ObjectInfo obj, double newY, UndoRecord undo) {
		// Take a snapshot of the object for the undo record before we mess it up.
		undo.addCommand(UndoRecord.COPY_COORDS, new Object [] {obj.getCoords(), obj.getCoords().duplicate()});

		// Note that the Origin of a CooridnateSystem is in global
		// coordinates.
		CoordinateSystem coords = obj.getCoords();
		Vec3 orig = coords.getOrigin();
		orig.y = newY;

		coords.setOrigin(orig);	
		obj.setCoords(coords);
	}


	/**
	 * Returns the collective lowest point of an object and it's
	 * non-selected children.
	 * @param obj
	 * @return
	 */
	private Vec3 getLowestPointOfObjectGroup(ObjectInfo obj) {

		// Start with object lowest point.
		Vec3 lowestPoint = getObjectLowestPoint(obj);

		ObjectInfo[] notSelectedChildren = getNotSelectedChildren(obj);

		// Check non-selected children for a lower point. 
		for(ObjectInfo childObj : notSelectedChildren){
			double distanceChildFromParent = getDistanceY(obj, childObj);
			Vec3 candidateLowestPoint = getLowestPointOfObjectGroup(childObj);
			candidateLowestPoint.y += distanceChildFromParent;
			if(candidateLowestPoint.y < lowestPoint.y){
				lowestPoint = candidateLowestPoint;
			}
		}

		return lowestPoint;
	}


	/**
	 * Returns the lowest point of an object relative to the scene coordinates
	 * @param obj the object
	 * @return lowest point of the object in the scene
	 */
	private Vec3 getObjectLowestPoint(ObjectInfo obj) {
		Vec3 currentLowest = new Vec3(0, obj.getBounds().maxy, 0);
		if(obj.getObject().canConvertToTriangleMesh() != Object3D.CANT_CONVERT){
			TriangleMesh mesh = obj.getObject().convertToTriangleMesh(0.001);
			MeshVertex[] vertices = mesh.getVertices();

			for(int vertexNum = 0; vertexNum < mesh.getVertices().length; vertexNum++){
				Vec3 point = new Vec3(vertices[vertexNum].r);
				obj.getCoords().fromLocal().transformDirection(point);

				if(point.y < currentLowest.y){
					currentLowest = point;
				}
			}
		}
		else{
			Vec3 point = new Vec3(0, 0, 0);
			obj.getCoords().fromLocal().transformDirection(point);
			currentLowest = point;
		}
		return currentLowest;
	}

	/**
	 * Return the number of non-selected children the object has.
	 * @param obj
	 * @return
	 */
	private ObjectInfo[] getNotSelectedChildren(ObjectInfo obj) {
		// Get the children.
		ObjectInfo[] children = obj.getChildren();

		// Determine the number of selected children.
		int selectedCount = 0;
		for(ObjectInfo childObj : children){
			if(!childObj.selected){
				selectedCount++;
			}
		}

		// Allocate an array to hold the non-selected children.
		ObjectInfo[] notSelectedChildren = new ObjectInfo[selectedCount];

		// Put the non-selected children in the array.
		int i=0;
		for(ObjectInfo childObj : children){
			if(!childObj.selected){
				notSelectedChildren[i] = childObj;
				i++;
			}
		}

		// Return the array.
		return notSelectedChildren;
	}

}
