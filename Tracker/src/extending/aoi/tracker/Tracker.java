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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.List;
import java.util.concurrent.Semaphore;

import artofillusion.Scene;
import artofillusion.WireframeMesh;
import artofillusion.math.BoundingBox;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import artofillusion.object.NullObject;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;

/**
 * Special null object that tracks the movement of another object.
 * @author Timothy Fish
 *
 */
public class Tracker extends NullObject {
	private static Semaphore pointSemaphore = new Semaphore(1, true);
	private static WireframeMesh mesh;
	private static final int PLUG_IN_VERSION = 1;
	private static final int NO_TARGET = -1;
	private static final long INVERSE_FREQUENCY = 1000/60; // millis/hertz

	private ObjectInfo target = null;
	private ObjectInfo trackerInfo = null;
	private int targetId = NO_TARGET;
	private Scene theScene = null;
	private TrackingThread theThread = null;

	public boolean lockXAxis = false;
	public boolean lockYAxis = false;
	public boolean lockZAxis = false;

	// Create a wire frame that looks like that of a NullObject, but
	// it has a pointer along the Y axis to indicate the direction
	// the Tracker is pointing.
	static
	{
		Vec3 vert[];
		double r = 0.25;
		int from[];
		int to[];

		new BoundingBox(-0.25, 0.25, -0.25, 0.25, -0.25, 0.25);
		vert = new Vec3 [10];
		from = new int [7];
		to = new int [7];
		vert[0] = new Vec3(r, 0.0, 0.0);
		vert[1] = new Vec3(-r, 0.0, 0.0);
		vert[2] = new Vec3(0.0, r, 0.0);
		vert[3] = new Vec3(0.0, -r, 0.0);
		vert[4] = new Vec3(0.0, 0.0, r);
		vert[5] = new Vec3(0.0, 0.0, -r);
		vert[6] = new Vec3(r*0.1, r*0.5, 0.0);
		vert[7] = new Vec3(-r*0.1, r*0.5, 0.0);
		vert[8] = new Vec3(0.0, r*0.5, r*0.1);
		vert[9] = new Vec3(0.0, r*0.5, -r*0.1);
		from[0] = 0;
		to[0] = 1;
		from[1] = 2;
		to[1] = 3;
		from[2] = 4;
		to[2] = 5;

		from[3] = 2;
		to[3] = 6;
		from[4] = 2;
		to[4] = 7;
		from[5] = 2;
		to[5] = 8;
		from[6] = 2;
		to[6] = 9;
		mesh = new WireframeMesh(vert, from, to);
	}

	/**
	 * Thread that rotates the object when other objects update.
	 * @author Timothy
	 *
	 */
	protected class TrackingThread extends Thread {

		/**
		 * constructor
		 */
		public TrackingThread() {
		}

		/**
		 * Operating function of the thread.Points the tracker
		 * in the right direction when an object moves.
		 */
		public void run(){
			// Track target while there is a target to track.
			while(true){

				// delay for a little while between each frame
				try {
					sleep(INVERSE_FREQUENCY);
				} catch (InterruptedException e) {
					// Should never happen, so print an error.
					e.printStackTrace();
				}

				obtainSemaphore();

				// Note that we won't track the object until one
				// frame after we have a valid target.

				if(target != null && target.getId() == targetId && trackerInfo != null){
					pointAtTarget(trackerInfo, target);
				}
				// Find object indicated by targetId
				else if(target == null || target.getId() != targetId){
					if(targetId != NO_TARGET){
						target = findObject(targetId);
					}
				}
				else{
					// trackerInfo isn't set, so do setup
					initialize();
				}

				releaseSemaphore();
			}
		}

		/**
		 * Release Semaphore so another Tracker can manipulate objects. 
		 */
		private void releaseSemaphore() {
			Tracker.pointSemaphore.release();
		}

		/**
		 * Block until the Semaphore is available so that Trackers aren't
		 * all trying to manipulate objects at the same time.
		 */
		private void obtainSemaphore() {
			// Block while other trackers are working.
			try {
				Tracker.pointSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Finds the object that matches the id
		 * @param id 
		 */
		private ObjectInfo findObject(int id) {
			List<ObjectInfo> theObjects = theScene.getAllObjects();		

			// find id in the list of objects and point at the object
			for(ObjectInfo object : theObjects){
				if(object.getId() == id){
					return object; // found it, so exit loop early
				}
			}
			return null; // not found
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
		 * @param parent 
		 * @param orgA Original coordinate systems of pointer
		 */
		private void rotateChildren(ObjectInfo parent, ObjectInfo pointer, CoordinateSystem orgA) {
			for(ObjectInfo child : parent.getChildren()){
				CoordinateSystem coords = child.getCoords();

				coords.transformCoordinates(orgA.toLocal()); // set rotation center at pointer origin
				coords.transformCoordinates(pointer.getCoords().fromLocal()); // move everything back, but to the new location

				rotateChildren(child, pointer, orgA);
			}
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
		 * Rotates the pointer to point at the target.
		 * @param pointer
		 */
		private void pointAtTarget(ObjectInfo pointer, ObjectInfo target) {

			if(pointer == null || target == null) { return; }

			Vec3 locA = pointer.getCoords().getOrigin();
			CoordinateSystem orgA = pointer.getCoords().duplicate();

			Vec3 locB = target.getCoords().getOrigin();
			Vec3 BminusA = locB.minus(locA);

			double x = BminusA.x;
			double y = BminusA.y;
			double z = BminusA.z;

			// default to current rotation for each axis
			double xRot = pointer.getCoords().getRotationAngles()[0];
			double yRot = pointer.getCoords().getRotationAngles()[1];
			double zRot = pointer.getCoords().getRotationAngles()[2];

			if(!lockXAxis){
				xRot = calculateXRotate(y, z);
			}

			if(!lockYAxis){
				yRot = calculateYRotate();
			}

			if(!lockZAxis){
				zRot = calculateZRotate(x, y, z);
			}

			pointer.getCoords().setOrientation(xRot, yRot, zRot);
			rotateChildren(pointer, pointer, orgA);
		}

	}



	/**
	 * constructor
	 * @param theScene 
	 * @param undo 
	 * 
	 */
	public Tracker(Scene theScene) {
		this.theScene  = theScene;
	}

	/**
	 * Returns a duplicate of the existing object. Sets
	 * the targetId to the same as this object, so the new
	 * Tracker will track the same object as the first. 
	 * @return
	 */
	public Object3D duplicate()
	{
		Tracker t = new Tracker(this.theScene);

		t.lockXAxis = this.lockXAxis;
		t.lockYAxis = this.lockYAxis;
		t.lockZAxis = this.lockZAxis;

		if(this.targetId != NO_TARGET){
			t.pointAt(theScene, this.targetId);
		}
		return t;
	}

	/**
	 * Makes this object like the one passed in. Changes the
	 * targetId so that this object will now track the same object
	 * as the first.
	 * @param obj
	 */
	public void copyObject(Object3D obj)
	{
		if(obj instanceof Tracker){
			Tracker t = (Tracker)obj;

			this.lockXAxis = t.lockXAxis;
			this.lockYAxis = t.lockYAxis;
			this.lockZAxis = t.lockZAxis;

			if(t.targetId != NO_TARGET){
				pointAt(theScene, t.targetId);
			}

		}
	}

	/**
	 * Constructor for reading data from the file.
	 * @param in
	 * @param theScene
	 * @throws IOException
	 * @throws InvalidObjectException
	 */
	public Tracker(DataInputStream in, Scene theScene) throws IOException, InvalidObjectException{
		super(in, theScene);

		this.theScene = theScene;
		short version = in.readShort();
		if (version != PLUG_IN_VERSION){
			throw new InvalidObjectException("");
		}

		int id = in.readInt();
		if(id != NO_TARGET){
			pointAt(theScene, id);
		}

		this.lockXAxis = in.readBoolean();
		this.lockYAxis = in.readBoolean();
		this.lockZAxis = in.readBoolean();
	}

	/**
	 * Points the tracker at the object.
	 * @param theScene Scene that contains the objects.
	 * @param id Id of the object to point at.
	 */
	private void pointAt(Scene theScene, int id) {
		this.theScene = theScene;
		// set the point at id
		targetId = id;

		// if theThread isn't started yet, do it now
		if(theThread == null){
			theThread = new TrackingThread();
			theThread.start();
		}
	}

	/**
	 * Does the things that the constructor can't do yet.
	 * This method should be called after the tracker is assigned
	 * to an ObjectInfo object. 
	 */
	public void initialize() {
		if(theScene == null){ return; }

		List<ObjectInfo> theObjects = theScene.getAllObjects();		

		// find object in the list of objects and set trackerInfo
		for(ObjectInfo object : theObjects){
			if(object.getObject() == this){
				trackerInfo = object; // found it, so exit loop early
			}
		}
	}

	/**
	 * Writes the Tracker to the file.
	 * @param out
	 * @param theScene
	 * @throws IOException
	 */
	public void writeToFile(DataOutputStream out, Scene theScene) throws IOException{
		super.writeToFile(out, theScene);

		// Write Version Info
		out.writeShort(PLUG_IN_VERSION);

		// Write identifying information for object we're pointing at.
		out.writeInt(targetId);

		// Write the axis locks
		out.writeBoolean(lockXAxis);
		out.writeBoolean(lockYAxis);
		out.writeBoolean(lockZAxis);
	}

	/**
	 * Points the tracker at the specified target.
	 * @param tracker
	 * @param target
	 * @param undo
	 */
	public static void pointAt(ObjectInfo tracker, ObjectInfo target){
		if(!(tracker.getObject() instanceof Tracker)) return;

		Tracker t = (Tracker)tracker.getObject();

		if(target != null){
			// begin the tracking thread
			t.pointAt(t.theScene, target.getId());
		}
		else{
			t.clearTarget();
		}


	}

	/**
	 * Sets the Tracker to track nothing.
	 */
	private void clearTarget() {
		this.theThread = null;
		targetId = NO_TARGET;
		target = null;
	}

	public int getTargetId() {
		return this.targetId;
	}

	public WireframeMesh getWireframeMesh()
	{
		return mesh;
	}
}
