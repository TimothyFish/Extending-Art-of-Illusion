/**
 * 
 */
package extending.aoi.point_at;

import artofillusion.object.ObjectInfo;
import artofillusion.ui.PanelDialog;
import buoy.widget.BFrame;
import buoy.widget.BRadioButton;
import buoy.widget.ColumnContainer;
import buoy.widget.RadioButtonGroup;

/**
 * @author Timothy Fish
 *
 */
public class PointAtObjectSelectionWindow {
	/**
	 * Class to setup the layout of the PointAtObjectSelectionWindow.
	 */
	private class PointAtDialogPanel extends ColumnContainer {
		private RadioButtonGroup theRadioButtonGroup;
		private String objAPointerText;
		private String objBPointerText;
		private BRadioButton objAPointerRadioButton;
		private BRadioButton objBPointerRadioButton;
		
		/**
		 * Constructor - handles widget placement
		 */
		PointAtDialogPanel(ObjectInfo objA, ObjectInfo objB){
			super(); // Call the ColumnContainer constructor
						
			// Set the display text. Note that getName() and getString() are very different.
			objAPointerText = objA.getName() + " points at " + objB.getName();
			objBPointerText = objB.getName() + " points at " + objA.getName();
			
			theRadioButtonGroup = new RadioButtonGroup();
			
			objAPointerRadioButton = new BRadioButton(objAPointerText, true, theRadioButtonGroup);
			objBPointerRadioButton = new BRadioButton(objBPointerText, false, theRadioButtonGroup);
			
			add(objAPointerRadioButton);
			add(objBPointerRadioButton);	
			
		}
		/**
		 * Returns true if objA is the pointer and false if objB is the pointer.
		 * @return
		 */
		public boolean getObjAPointer() {
			return objAPointerRadioButton.getState();
		}

	}

	private ObjectInfo pointer;
	private ObjectInfo pointee;
	private final String windowTitle = "Point At Options";
	private PointAtDialogPanel thePanel;
	
	/**
	 * Displays window for the user to select the object that points
	 * at the pointee.
	 * @param parent
	 * @param objA
	 * @param objB
	 */
	public PointAtObjectSelectionWindow(BFrame parent, ObjectInfo objA, ObjectInfo objB){
		pointer = objA;
		pointee = objB;
		
		thePanel = new PointAtDialogPanel(objA, objB);

		PanelDialog dlg = new PanelDialog(parent, windowTitle , thePanel);
		
		if (dlg.clickedOk()){
			// code for what to do after user accepts
			if(thePanel.getObjAPointer()){
				pointer = objA;
				pointee = objB;
			}
			else{
				pointer = objB;
				pointee = objA;
			}
		}
		else{
			// User clicked cancel, so everything should remain unchanged.
			;
		}
	}

	/**
	 * Returns the object that points.
	 * @return
	 */
	public ObjectInfo getPointer() {
		return pointer;
	}

	/**
	 * Returns the object we're to point at.
	 * @return
	 */
	public ObjectInfo getPointee() {
		return pointee;
	}
}
