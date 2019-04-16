/**
 * 
 */
package extending.aoi.point_at_ii;

import java.util.Collection;

import artofillusion.LayoutWindow;
import artofillusion.Scene;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.PanelDialog;
import buoy.widget.BComboBox;
import buoy.widget.ColumnContainer;

/**
 * @author Timothy Fish
 *
 */
public class PointAtIIObjectSelectionWindow {
	/**
	 * Class to setup the layout of the PointAtObjectSelectionWindow.
	 */
	private class PointAtDialogPanel extends ColumnContainer {
		private BComboBox theObjectsList;
		Collection<ObjectInfo> theObjects;
		
		/**
		 * Constructor - handles widget placement
		 */
		PointAtDialogPanel(Collection<ObjectInfo> objects){
			super(); // Call the ColumnContainer constructor
			
			theObjects = objects;
						
			theObjectsList = new BComboBox();
			// Set the display text. Note that getName() and getString() are very different.
			for(ObjectInfo object : objects ){
              theObjectsList.add(object.getName());
			}
			
			add(theObjectsList);	
			
		}

		public ObjectInfo getPointee() {
			int selectedIndex =  theObjectsList.getSelectedIndex();
			if(selectedIndex >= 0){
				return (ObjectInfo)theObjects.toArray()[selectedIndex];
			}
			else{
				return null;
			}
		}

	}

	private ObjectInfo pointee;
	private final String windowTitle = "Point At Options";
	private PointAtDialogPanel thePanel;
	
	/**
	 * Displays window for the user to select the pointee.
	 * @param parent
	 */
	public PointAtIIObjectSelectionWindow(LayoutWindow parent){
		Scene scene = parent.getScene();
		
		pointee = null;
		
		Collection<ObjectInfo> selectableObjects = scene.getAllObjects();
		thePanel = new PointAtDialogPanel(selectableObjects);

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
}
