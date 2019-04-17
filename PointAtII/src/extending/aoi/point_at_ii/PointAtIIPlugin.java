/**
    Point At 2 Plugin from Chapter 4 of the book "Extending Art of Illusion: Scripting 3D Scene Creation"
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
package extending.aoi.point_at_ii;

import java.util.Collection;

import buoy.widget.BMenu;
import buoy.widget.BMenuItem;
import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.UndoRecord;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import artofillusion.object.Light;
import artofillusion.object.ObjectInfo;
import artofillusion.object.SceneCamera;
import artofillusion.ui.Translate;
import java.lang.Math;

import extending.aoi.point_at_ii.PointAtIIMenuItemActivator;
import extending.aoi.point_at_ii.PointAtIIObjectSelectionWindow;

/**
 * @author Timothy Fish
 *
 */
public class PointAtIIPlugin implements Plugin {

	private LayoutWindow layout;
	private ObjectInfo pointee;
	public static final int numObjectsRequired = 1;

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
			BMenuItem menuItem = Translate.menuItem("Point At II...", this, "pointAtMenuAction");

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
		new PointAtIIMenuItemActivator(menuItem, layout).start();
	}

	/**
	 * Action code for the Point At menu item.
	 */
	@SuppressWarnings("unused")
	private void pointAtMenuAction(){

		Collection<ObjectInfo> objects = layout.getSelectedObjects();
		if(!objects.isEmpty()){
			// Create a new UndoRecord. This will be built as we
			// make changes, so we can go back to the previous state
			// of anything we changed.
			UndoRecord undo = new UndoRecord(layout, true);

			if(setPointeeFromUserInput()){

				for(ObjectInfo obj : objects){
					if(obj != this.pointee){ // don't point something at itself
						pointPointerAtPointee(obj, undo);
					}
				}

				layout.updateImage();

				// Tell the layout window that it can store what we've said
				// is the previous state of the object.
				layout.setUndoRecord(undo);
			}
			else{
				// We should never get here because the menu item is disabled.
				System.err.println("ERROR: PointAtIIPlugin - number of selected objects not equal "+numObjectsRequired);
			}
		}
	}

	/**
	 * Rotates the pointer to point at the pointee.
	 * @param pointer
	 * @param undo
	 */
	private void pointPointerAtPointee(ObjectInfo pointer, UndoRecord undo) {

		// Take a snapshot of the object for the undo record before we mess it up.
		undo.addCommand(UndoRecord.COPY_COORDS, new Object [] {pointer.getCoords(), pointer.getCoords().duplicate()});

		
		Vec3 locA = pointer.getCoords().getOrigin();
		CoordinateSystem orgA = pointer.getCoords().duplicate();
		
		Vec3 locB = pointee.getCoords().getOrigin();
		Vec3 BminusA = locB.minus(locA);

		double x = BminusA.x;
		double y = BminusA.y;
		double z = BminusA.z;

		double xRot = 0;
		double yRot = 0;
		double zRot = 0;

		if(!specialPointerObject(pointer)){
			// Normal Setup
			xRot = calculateXRotate(y, z);
			yRot = calculateYRotate();
			zRot = calculateZRotate(x, y, z);
		}
		else if(specialPointerObject(pointer)){
			// Handle special objects, like cameras.
			// note that a SceneCamera is not the same thing as a Camera

			// Set Object to known state.
			pointer.getCoords().setOrientation(0,0,0);
			
			xRot = calculateXRotateSpecial(x, y, z);
			yRot = calculateYRotateSpecial(x, y, z);
			zRot = calculateZRotateSpecial()*0.0;
		}

		pointer.getCoords().setOrientation(xRot, yRot, zRot);
		rotateChildren(pointer, pointer, orgA, undo);
	}

	/**
	 * @param parent 
	 * @param undo
	 * @param orgA Original coordinate systems of pointer
	 */
	private void rotateChildren(ObjectInfo parent, ObjectInfo pointer, CoordinateSystem orgA, UndoRecord undo) {
		for(ObjectInfo child : parent.getChildren()){
			undo.addCommand(UndoRecord.COPY_COORDS, new Object [] {child.getCoords(), child.getCoords().duplicate()});
			CoordinateSystem coords = child.getCoords();

			coords.transformCoordinates(orgA.toLocal()); // set rotation center at pointer origin
			coords.transformCoordinates(pointer.getCoords().fromLocal()); // move everything back, but to the new location
			
			rotateChildren(child, pointer, orgA, undo);
		}
	}

	/**
	 * Returns true iff the object is a SceneCamera or a Light and false otherwise.
	 * @return
	 */
	private boolean specialPointerObject(ObjectInfo pointer) {
		boolean ret = false;

		if((pointer.getObject() instanceof SceneCamera)){
			ret = true;
		}
		else if((pointer.getObject() instanceof Light)){
			ret = true;
		}
		else{
			ret = false;
		}

		return ret;
	}

	/**
	 * Calculates the second rotation.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private double calculateZRotate(double x, double y, double z) {
		double sqrtYYZZ = Math.sqrt(y*y + z*z);
		double ret = 0; // return value;
		if(sqrtYYZZ != 0){

			ret = Math.toDegrees(Math.atan(x/sqrtYYZZ));

			if(sqrtYYZZ < 0){
				// angle is on the other size
				ret += 180.0; 
			}
		}
		// Handle differently because the angle gives two possible directions.
		else if(x > 0){
			ret = 90.0;
		}
		else if(x < 0){
			ret = -90.0;
		}
		return ret;
	}

	/**
	 * Returns a valid angle for Y.
	 * @return
	 */
	private double calculateYRotate() {
		// Zero is as good as any value.
		return 0.0;
	}

	/**
	 * Calculates rotation that will put direction vector in the XY plane. 
	 * @param y
	 * @param z
	 * @return
	 */
	private double calculateXRotate(double y, double z) {
		double ret = 0;

		if(y != 0){
			ret = Math.toDegrees(Math.atan(-z/y));

			if(y < 0){
				ret += 180.0;
			}
		}
		// Handle differently because the angle gives two possible directions.
		else if(z > 0){
			ret = -90.0;
		}
		else if(z < 0){
			ret = 90.0;
		}
		return  ret;
	}

	/**
	 * Calculates the first rotation.
	 * This function is for special object like cameras and lights.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private double calculateYRotateSpecial(double x, double y, double z) {
		double ret = 0;

		if(z != 0){
			ret = Math.toDegrees(Math.atan(-x/z));

			if(z < 0){
				ret += 180.0;
			}
		}
		// Handle differently because the angle gives two possible directions.
		else if(x > 0){
			ret = -90.0;
		}
		else if(x < 0){
			ret = 90.0;
		}
		return  ret;
	}

	/**
	 * Returns the Special Z rotation.
	 * This function is for special object like cameras and lights.
	 * @return
	 */
	private double calculateZRotateSpecial() {
		// We can set it to 0.0 because any value of Z can be used.
		return 0.0;
	}

	/**
	 * Calculates rotation that will put direction vector in the XZ plane.
	 * This function is for special object like cameras and lights. 
	 * @param y
	 * @param z
	 * @return
	 */
	private double calculateXRotateSpecial(double x, double y, double z) {
		double sqrtXXZZ = Math.sqrt(x*x + z*z);
		double ret = 0; // return value;
		if(sqrtXXZZ != 0){

			ret = Math.toDegrees(Math.atan(y/sqrtXXZZ));

			if(sqrtXXZZ < 0){
				// angle is on the other size
				ret += 180.0; 
			}
		}
		// Handle differently because the angle gives two possible directions.
		else if(y > 0){
			ret = 90.0;
		}
		else if(y < 0){
			ret = -90.0;
		}
		return ret;
	}


	/**
	 * Displays a window for the user to select whether he wants objA to
	 * point at objB or objB to point at objA.
	 */
	private boolean setPointeeFromUserInput() {

		PointAtIIObjectSelectionWindow window = new PointAtIIObjectSelectionWindow(layout);

		pointee = window.getPointee();
		
		return (pointee != null);

	}
}
