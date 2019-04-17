/**
    Torus Plugin from Chapter 6 of the book "Extending Art of Illusion: Scripting 3D Scene Creation"
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
package extending.aoi.torus;

import java.awt.Point;

import artofillusion.Camera;
import artofillusion.LayoutWindow;
import artofillusion.Scene;
import artofillusion.UndoRecord;
import artofillusion.ViewerCanvas;
import artofillusion.animation.PositionTrack;
import artofillusion.animation.RotationTrack;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.EditingTool;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.Translate;
import buoy.event.WidgetMouseEvent;

/**
 * @author Timothy Fish
 *
 */
public class CreateTorusTool extends EditingTool {
	private static final double majorRadius = 2;
	private static final double minorRadius = 0.5;
	static int counter = 1;
	private Point clickPoint;
	private ObjectInfo objInfo;

	/**
	 * @param win
	 */
	public CreateTorusTool(EditingWindow win) {
		super(win);
		
		// Name is the icon name without an extension.
		// extensions.xml defines the theme.xml file and the theme.xml file defines the Torus theme
		// which contains the createTorus button.
		initButton("Torus:createTorus"); 
		
	}
	
	  public void activate()
	  {
	    super.activate();
	    theWindow.setHelpText(Translate.text("Create Torus: Width controls radius and height controls thickness."));
	  }
	
	  public int whichClicks()
	  {
	    return ALL_CLICKS;
	  }
	  
	  public String getToolTipText()
	  {
	    return Translate.text("Create Torus");
	  }

	  public void mousePressed(WidgetMouseEvent e, ViewerCanvas view)
	  {
	    clickPoint = e.getPoint();
	  }
	  
	  public void mouseDragged(WidgetMouseEvent e, ViewerCanvas view)
	  {
	    if (objInfo == null)
	    {
	      // Create the torus.
	      
	      Scene theScene = ((LayoutWindow) theWindow).getScene();
	      objInfo = new ObjectInfo(new Torus(majorRadius, minorRadius), new CoordinateSystem(), "Torus "+counter);
	      counter++;
	      objInfo.addTrack(new PositionTrack(objInfo), 0);
	      objInfo.addTrack(new RotationTrack(objInfo), 1);
	      UndoRecord undo = new UndoRecord(theWindow, false);
	      int sel[] = ((LayoutWindow) theWindow).getSelectedIndices();
	      ((LayoutWindow) theWindow).addObject(objInfo, undo);
	      undo.addCommand(UndoRecord.SET_SCENE_SELECTION, new Object [] {sel});
	      theWindow.setUndoRecord(undo);
	      ((LayoutWindow) theWindow).setSelection(theScene.getNumObjects()-1);
	    }
	    
	    // Determine the size and position for the torus.
	    
	    Camera cam = view.getCamera();
	    Point dragPoint = e.getPoint();
	    Vec3 v1;
	    Vec3 v2;
	    Vec3 v3;
	    Vec3 orig;
	    Vec3 xdir;
	    Vec3 ydir;
	    Vec3 zdir;
	    double xsize;
	    double ysize;
	    double zsize;
	    
	    v1 = cam.convertScreenToWorld(clickPoint, Camera.DEFAULT_DISTANCE_TO_SCREEN);
	    v2 = cam.convertScreenToWorld(new Point(dragPoint.x, clickPoint.y), Camera.DEFAULT_DISTANCE_TO_SCREEN);
	    v3 = cam.convertScreenToWorld(dragPoint, Camera.DEFAULT_DISTANCE_TO_SCREEN);
	    orig = v1.plus(v3).times(0.5);
	    if (dragPoint.x < clickPoint.x)
	      xdir = v1.minus(v2);
	    else
	      xdir = v2.minus(v1);
	    if (dragPoint.y < clickPoint.y)
	      ydir = v3.minus(v2);
	    else
	      ydir = v2.minus(v3);
	    xsize = xdir.length();
	    ysize = ydir.length();
	    xdir = xdir.times(1.0/xsize);
	    ydir = ydir.times(1.0/ysize);
	    zdir = xdir.cross(ydir);
	    zsize = Math.min(xsize, ysize);

	    // Update the size and position, and redraw the display.
	    
	    ((Torus) objInfo.getObject()).setSize(xsize, ysize, zsize);
	    objInfo.getCoords().setOrigin(orig);
	    objInfo.getCoords().setOrientation(zdir, ydir);
	    objInfo.clearCachedMeshes();
	    theWindow.updateImage();
	  }
	  
	  public void mouseReleased(WidgetMouseEvent e, ViewerCanvas view)
	  {
	    objInfo = null;
	    theWindow.setModified();
	  }
}
