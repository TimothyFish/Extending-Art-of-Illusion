/**
 * Plugin that will drop an object to the first visible, non-special
 * object it encounters below it. A collision is deemed to be when
 * a vertex of the object falls within a triangle of an object below it.
 */
package extending.aoi.place_on;

import java.util.ArrayList;
import java.util.Collection;

import buoy.widget.BMenu;
import buoy.widget.BMenuItem;
import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.UndoRecord;
import artofillusion.math.BoundingBox;
import artofillusion.math.Mat4;
import artofillusion.math.Vec2;
import artofillusion.math.Vec3;
import artofillusion.object.Light;
import artofillusion.object.NullObject;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;
import artofillusion.object.ReferenceImage;
import artofillusion.object.SceneCamera;
import artofillusion.object.TriangleMesh;
import artofillusion.object.TriangleMesh.Face;
import artofillusion.ui.Translate;

/**
 * @author Timothy Fish
 *
 */
public class PlaceOnPlugin implements Plugin {

	private LayoutWindow layout;
	private static final int numObjectsRequired = 1;
	private static final double meshToleranceObject = 0.010;
	private static final double meshToleranceNominee = 0.100;

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
			BMenuItem menuItem = Translate.menuItem("Place On", this, "placeOnMenuAction");

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
		new PlaceOnMenuItemActivator(menuItem, layout).start();
	}

	/**
	 * Action code for the Place On menu item.
	 */
	@SuppressWarnings("unused")
	private void placeOnMenuAction(){

		Collection<ObjectInfo> objects = layout.getSelectedObjects();
		if(objects.toArray().length == numObjectsRequired){
			// Create a new UndoRecord. This will be built as we
			// make changes, so we can go back to the previous state
			// of anything we changed.
			UndoRecord undo = new UndoRecord(layout, true);

			for(ObjectInfo obj : objects){
			  placeObjectOnNextObject(obj, undo);
			}

			layout.updateImage();

			// Tell the layout window that it can store what we've said
			// is the previous state of the object.
			layout.setUndoRecord(undo);
		}
	}
	
	/**
	 * Places the object on the nearest object below it.
	 * @param obj
	 * @param undo
	 */
	private void placeObjectOnNextObject(ObjectInfo obj, UndoRecord undo) {
		if(obj == null) { return; } // quick exit for null object

		// Search bounding boxes for objects that are candidates.
		Collection<ObjectInfo> candidates = findCandidateObjects(obj);

		// For each candidate, find the shortest upward ray that intersects the object.
		double shortestYDistance = Double.MAX_VALUE;

		for(ObjectInfo nominee : candidates){
			// Given two objects, find the collision point if one were to be
			// dropped on top of the other. Return the y distance at that point.

			// define a direction vector
			Vec3 direction = new Vec3(0, -1, 0);
			// Get point on objA, relative to objA origin where collision will occur.
			double collisionDistance = findDistanceToCollisionPoint(obj, nominee, direction);

			if(collisionDistance < shortestYDistance){
				shortestYDistance = collisionDistance;
			}
		}

		// Take a snapshot of the object for the undo record before we mess it up.
		undo.addCommand(UndoRecord.COPY_COORDS, new Object [] {obj.getCoords(), obj.getCoords().duplicate()});

		// Reposition the object so that it rests on the point given by the shortest shortest ray.
		if(shortestYDistance < Double.MAX_VALUE){
			Vec3 origin = obj.getCoords().getOrigin();
			origin.y -= shortestYDistance;
			obj.getCoords().setOrigin(origin);
		}
	}
	
	/**
	 * Returns distance objA must travel in direction given before
	 * it will have its first collision with objB. Returns Double.MAX_VALUE
	 * if no collision is possible.
	 * @param objA
	 * @param objB
	 * @param direction
	 * @return
	 */
	private double findDistanceToCollisionPoint(ObjectInfo objA, ObjectInfo objB,
			Vec3 direction) {

		double ret = Double.MAX_VALUE;
		
		// if we can't do anything, just quit
		if(objA.getObject().canConvertToTriangleMesh() == Object3D.CANT_CONVERT){ return ret; }
		if(objB.getObject().canConvertToTriangleMesh() == Object3D.CANT_CONVERT){ return ret; }
		
		TriangleMesh meshA = objA.getObject().convertToTriangleMesh(meshToleranceObject);
		TriangleMesh meshB = objB.getObject().convertToTriangleMesh(meshToleranceNominee);
		
		// for each face in A, check for collision with each face in B
		for(Face faceA : meshA.getFaces()){
			Mat4 fromA = objA.getCoords().fromLocal();
			// get a triangle from objA, with points in scene coordinates
			Triangle triangleA = convertFaceToTriangle(meshA, faceA, fromA);
			
			for(Face faceB : meshB.getFaces()){
				Mat4 fromB = objB.getCoords().fromLocal();
				Triangle triangleB = convertFaceToTriangle(meshB, faceB, fromB);
				
				double currentCollisionDistance = findTriangleCollisionDistance(triangleA, triangleB, direction);
				if(currentCollisionDistance < ret){
					// check to see if this is the first collision
					ret = currentCollisionDistance;
				}	
			}
		}
		
		return ret;
	}
	/**
	 * Given a face defined in a Triangle Mesh, creates a triangle
	 * with point values transformed from local to by the matrix.
	 * @param mesh
	 * @param face
	 * @param matrix
	 * @return
	 */
	private Triangle convertFaceToTriangle(TriangleMesh mesh, Face face, Mat4 matrix) {
		Vec3 p0 = new Vec3(mesh.getVertexPositions()[face.v1]);
		matrix.transform(p0);
		Vec3 p1 = new Vec3(mesh.getVertexPositions()[face.v2]);
		matrix.transform(p1);
		Vec3 p2 = new Vec3(mesh.getVertexPositions()[face.v3]);
		matrix.transform(p2);
		
		Triangle triangle = new Triangle(p0, p1, p2);
		return triangle;
	}
	
	/**
	 * Given two triangles in the same coordinate system, finds the
	 * distance between them along the direction vector. If triangleA
	 * will not collide with triangleB in that direction, this method
	 * will return Double.MAX_VALUE. 
	 * @param triangleA
	 * @param triangleB
	 * @param direction
	 * @return
	 */
	private double findTriangleCollisionDistance(Triangle triangleA,
			Triangle triangleB, Vec3 direction) {
		
		
		Vec3 b = triangleB.getP0();
		Vec3 normalB = triangleB.getNormal();
		Vec3 normal = normalB;
		double dist0 = 0;
		double dist1 = 0;
		double dist2 = 0;
		double denominator = direction.dot(normal);
		double numerator = 0;
				
		if(denominator == 0) { return Double.MAX_VALUE; } // exit now, direction parallel to plane
		
		// point A0
		Vec3 a0 = triangleA.getP0();
		numerator = a0.minus(b).dot(normal);
		dist0 = Math.abs(numerator/denominator);

		Vec3 P0 = a0.plus(direction.times(dist0));
		
		if(!pointInTriangle(triangleB, P0, direction)){
			dist0 = Double.MAX_VALUE;
		}
		
		// point A1
		Vec3 a1 = triangleA.getP1();		
		numerator = a1.minus(b).dot(normal);
		dist1 = Math.abs(numerator/denominator);
		Vec3 P1 = a1.plus(direction.times(dist1));
		
		if(!pointInTriangle(triangleB, P1, direction)){
			dist1 = Double.MAX_VALUE;
		}
		
		// point A2
		Vec3 a2 = triangleA.getP2();
		numerator = a2.minus(b).dot(normal);
		dist2 = Math.abs(numerator/denominator);
		Vec3 P2 = a2.plus(direction.times(dist2));
				
		if(!pointInTriangle(triangleB, P2, direction)){
			dist2 = Double.MAX_VALUE;
		}

		
		double dist =  Math.min(dist0, Math.min(dist1, dist2));

		// Find intersections between line segments.
		// Intersection will occur directly in line with the
		// intersection where the lines are mapped to the
		// y=0 plane. Check each line of triangleA against
		// each line of triangleB.
		Vec3 b0 = triangleB.getP0();
		Vec3 b1 = triangleB.getP1();
		Vec3 b2 = triangleB.getP2();

		dist = Math.min(dist, findLineCollision(a0, a1, b0, b1));
		dist = Math.min(dist, findLineCollision(a0, a1, b1, b2));
		dist = Math.min(dist, findLineCollision(a0, a1, b2, b0));

		dist = Math.min(dist, findLineCollision(a1, a2, b0, b1));
		dist = Math.min(dist, findLineCollision(a1, a2, b1, b2));
		dist = Math.min(dist, findLineCollision(a1, a2, b2, b0));

		dist = Math.min(dist, findLineCollision(a2, a0, b0, b1));
		dist = Math.min(dist, findLineCollision(a2, a0, b1, b2));
		dist = Math.min(dist, findLineCollision(a2, a0, b2, b0));	  

		return dist;
	}
	
	/**
	 * Finds the distance from segment [a0, a1] to [b0, b1].
	 * @param a0
	 * @param a1
	 * @param b0
	 * @param b1
	 * @return distance to collision
	 */
	private double findLineCollision(Vec3 a0, Vec3 a1, Vec3 b0, Vec3 b1) {
	  Vec2 a = a0.dropAxis(1);
	  Vec2 b = a1.dropAxis(1);
	  Vec2 c = b0.dropAxis(1);
	  Vec2 d = b1.dropAxis(1);
	  double dist = Double.MAX_VALUE;
	  
	  double D = (b.x-a.x)*(d.y-c.y)-(b.y-a.y)*(d.x-c.x);
	  double t0 = 0;
	  double u0 = 0;
	  if (D != 0){
	    t0 = ((c.x-a.x)*(d.y-c.y)-(c.y-a.y)*(d.x-c.x)) / D;
	    u0 = (a.x + (b.x-a.x)*t0-c.x)/(d.x-c.x);
	    if(0<=t0 && t0<=1 && 0<=u0 && u0<=1){
	      // intersection exists
	      Vec3 pA = new Vec3();
	      
	      pA.x = a0.x + (a1.x-a0.x)*t0;
	      pA.y = a0.y + (a1.y-a0.y)*t0;
	      pA.z = a0.z + (a1.z-a0.z)*t0;
	      
	      Vec3 pB = new Vec3();
	      pB.x = b0.x + (b1.x-b0.x)*u0;
        pB.y = b0.y + (b1.y-b0.y)*u0;
        pB.z = b0.z + (b1.z-b0.z)*u0;
        
        dist = Math.abs(pA.distance(pB));
	    }
	  }
	  
    return dist;
  }

  private boolean pointInTriangle(Triangle triangleB, Vec3 P, Vec3 direction) {
		// Transpose triangleB onto the plane perpendicular to direction.
		Mat4 xRotMatrix = Mat4.xrotation(calculateXRotate(direction));
		Mat4 yRotMatrix = Mat4.yrotation(calculateYRotate(direction));
		Mat4 zRotMatrix = Mat4.zrotation(calculateZRotate(direction));
		Mat4 rotationMatrix = xRotMatrix.times(yRotMatrix).times(zRotMatrix);
		
		Triangle projB = new Triangle(
				rotationMatrix.times(triangleB.getP0()), 
				rotationMatrix.times(triangleB.getP1()), 
				rotationMatrix.times(triangleB.getP2())); 
		
		// Transpose P onto the plane perpendicular to direction.
		Vec3 projP = rotationMatrix.times(P);
		
		// Determine if P is on the same side of each edge as the opposite point.
		if(SameSide(projP, projB.getP0(), projB.getP1(), projB.getP2())
		&& SameSide(projP, projB.getP1(), projB.getP2(), projB.getP0())
		&& SameSide(projP, projB.getP2(), projB.getP0(), projB.getP1())){
			return true;
		}
		// else
		return false;
	}
	
	/**
	 * Returns true if the two points are on the same side of the line
	 * given by linePoint1 and linePoint2.
	 * @param point1
	 * @param point2
	 * @param linePoint1
	 * @param linePoint2
	 * @return
	 */
	private boolean SameSide(Vec3 point1, Vec3 point2, Vec3 linePoint1, Vec3 linePoint2) {
		// Two points are on the same side of a line if the dot product of the
		// cross product of each point minus a point on the line and a second
		// point on the line minus the first point is >= 0.
		Vec3 crossPoint1 = (linePoint2.minus(linePoint1)).cross(point1.minus(linePoint1));
		Vec3 crossPoint2 = (linePoint2.minus(linePoint1)).cross(point2.minus(linePoint1));
		if(crossPoint1.dot(crossPoint2) >= 0){
			return true;
		}
		//else
		
		return false;
	}
	
	/**
	 * Calculates the second rotation, which will align the direction
	 * vector with the y axis.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private double calculateZRotate(Vec3 direction) {
		// Note that this is essentially the same code as we used
		// for the Point At Plugin
		double x = direction.x;
		double y = direction.y;
		double z = direction.z;
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
	private double calculateYRotate(Vec3 direction) {
		// Note that this is essentially the same code as we used
		// for the Point At Plugin
		
		// Zero is as good as any value.
		return 0.0;
	}

	/**
	 * Calculates rotation that will put direction vector in the XY plane. 
	 * @param y
	 * @param z
	 * @return
	 */
	private double calculateXRotate(Vec3 direction) {
		// Note that this is essentially the same code as we used
		// for the Point At Plugin
		double y = direction.y;
		double z = direction.z;
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

	private Collection<ObjectInfo> findCandidateObjects(ObjectInfo obj) {
		// Determine is the boundary boxes are close enough to
		// what we're looking for.
		
		ArrayList<ObjectInfo> objects = new ArrayList<ObjectInfo>();
		
		BoundingBox OB = obj.getBounds();
		Vec3 oMax = new Vec3(OB.maxx, OB.maxy, OB.maxz);
		Vec3 oMin = new Vec3(OB.minx, OB.miny, OB.minz);
		obj.getCoords().fromLocal().transform(oMax);
		obj.getCoords().fromLocal().transform(oMin);
		
		for(ObjectInfo candidate : layout.getScene().getAllObjects()){
			
			if(!candidate.isVisible() || isSpecial(candidate)) {
			} // skip this object
			else if(candidate == obj) {
			} // no self collisions
			else{
				objects.add(candidate);
			}
		    
		}
		return objects;
	}

	/**
	 * returns true if the object is a special object, like a camera,
	 * a light, or a null object.
	 */
	private boolean isSpecial(ObjectInfo obj) {
			boolean ret = false;

			if((obj.getObject() instanceof SceneCamera)){
				ret = true;
			}
			else if(obj.getObject() instanceof Light){
				ret = true;
			}
			else if(obj.getObject() instanceof NullObject){
				ret = true;
			}
			else if(obj.getObject() instanceof ReferenceImage){
				ret = true;
			}
			else{
				ret = false;
			}

			return ret;
	}

}
