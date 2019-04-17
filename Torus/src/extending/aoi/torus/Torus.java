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

/**
 * Torus Object
 */
package extending.aoi.torus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;

import artofillusion.Property;
import artofillusion.RenderingMesh;
import artofillusion.Scene;
import artofillusion.WireframeMesh;
import artofillusion.animation.Keyframe;
import artofillusion.animation.PoseTrack;
import artofillusion.math.BoundingBox;
import artofillusion.math.Vec2;
import artofillusion.math.Vec3;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;
import artofillusion.object.TriangleMesh;
import artofillusion.texture.Texture;
import artofillusion.texture.TextureMapping;
import artofillusion.ui.ComponentsDialog;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.Translate;
import artofillusion.ui.ValueField;
import buoy.widget.Widget;

/**
 * @author Timothy Fish
 *
 */
public class Torus extends Object3D {
	private static final double HALF = 0.5;
	private double majorRadius;
	private double minorRadius;

	private BoundingBox bounds;
	private RenderingMesh cachedMesh;
	private WireframeMesh cachedWire;

	private static final int SEGMENTS = 16;
	private static double sine[], cosine[];

	private static final String majorRadiusTitle = "Radius";
	private static final String minorRadiusTitle = "Thickness";
	private static final String editWindowTitle = "Edit Torus";
	static final short CURRENT_VERSION = 1; // Version number of the torus object. 

	// Constants used instead of enum here because AOI uses integer
	// values as the index were these are used.
	private static final int enumMajorRadius = 0;
	private static final int enumMinorRadius = 1;
	private static final Property PROPERTIES[] = new Property [] {
		new Property(Translate.text(majorRadiusTitle), 0.0, Double.MAX_VALUE, 1.0),
		new Property(Translate.text(minorRadiusTitle), 0.0, Double.MAX_VALUE, 1.0)
	};

	/**
	 * Fast sine and cosine handling for the wireframe mesh.
	 */
	static
	{
		sine = new double [SEGMENTS];
		cosine = new double [SEGMENTS];
		for (int i = 0; i < SEGMENTS; i++)
		{
			sine[i] = Math.sin(i*2.0*Math.PI/SEGMENTS);
			cosine[i] = Math.cos(i*2.0*Math.PI/SEGMENTS);
		}
	}

	/**
	 * Constructor
	 */
	public Torus(double majorRadius, double minorRadius) {
		this.majorRadius = majorRadius;
		this.minorRadius = minorRadius;

		bounds = new BoundingBox(
				-majorRadius-minorRadius,
				majorRadius+minorRadius,
				-minorRadius, minorRadius, 
				-majorRadius-minorRadius,
				majorRadius+minorRadius
		);
	}


	/**
	 * Sets the values in this object to match those in the object
	 * passed through the parameter.
	 */
	@Override
	public void copyObject(Object3D obj) {
		Torus t = (Torus) obj;

		setSize(2.0*(t.majorRadius+t.minorRadius), 2.0*t.minorRadius, 2.0*(t.majorRadius+t.minorRadius));

		copyTextureAndMaterial(obj);
		cachedMesh = null;
		cachedWire = null;
	}

	/**
	 * Returns a new object that is a duplicate of this one.
	 */
	@Override
	public Object3D duplicate() {
		Torus obj = new Torus(majorRadius, minorRadius);
		obj.copyTextureAndMaterial(this);
		return obj;
	}

	/**
	 * Returns the outer bounds of the object.
	 */
	@Override
	public BoundingBox getBounds() {
		return bounds;
	}

	/**
	 * Returns a mesh for viewing in wireframe mode or for when
	 * the object won't be rendered in the scene. Cameras and lights,
	 * for example, have only a wireframe mesh. A Torus needs both
	 * a wireframe mesh and a rendering mesh.
	 */
	@Override
	public WireframeMesh getWireframeMesh() {
		final int segments = SEGMENTS;
		Vec3 vert[];
		int from[], to[];
		// if we've already created a wireframe mesh, us that one
		if (cachedWire != null) {  return cachedWire; }

		// Wireframe meshes are made up of a set of vertices with lines
		// drawn between them. The vert array stores each vertex and the
		// from and to arrays define lines to draw. The most simple being
		// the one line wireframe mesh, which would have two vertices
		// vert[0], vert[1] and from[0] == 0, to[0] == 1. A triangle
		// would have vert[0], vert[1], and vert[2], with from[0] == 0,
		// to[0] == 1, from[1] == 1, to[1] == 2, from[2] == 2, to[2] == 0.
		// They are used for wireframe displays and normal displays of 
		// hidden objects, like cameras and lights.
		vert = new Vec3 [segments*segments]; 
		from = new int [2*segments*segments];  
		to = new int [2*segments*segments];
		// create large circle
		double x;
		double y;
		double z;
		for(int majorIndex = 0; majorIndex < segments; majorIndex++){
			// create small circle
			for(int minorIndex = 0; minorIndex < segments; minorIndex++){
				// create new vertex
				x = (majorRadius+(minorRadius*cosine[minorIndex]))*(cosine[majorIndex]);
				y = (minorRadius*sine[minorIndex]);
				z = (majorRadius+(minorRadius*cosine[minorIndex]))*(sine[majorIndex]);

				vert[majorIndex*segments+minorIndex] = new Vec3(x, y, z);

				// draw small circles
				from[majorIndex*segments+minorIndex] = majorIndex*segments+minorIndex;
				to[majorIndex*segments+minorIndex] = majorIndex*segments+minorIndex+1;

				// connect the circles
				from[segments*segments+minorIndex+segments*majorIndex] = 0+minorIndex+segments*majorIndex;
				if(majorIndex < segments-1){
					to[segments*segments+minorIndex+segments*majorIndex] = segments+minorIndex+segments*majorIndex;
				}
				else{
					to[segments*segments+minorIndex+segments*majorIndex] = minorIndex;
				}
			}
			// link back to the first one
			to[majorIndex*segments+segments-1] = majorIndex*segments+0;
		}

		return (cachedWire = new WireframeMesh(vert, from, to));
	}

	/**
	 * Sets the size of the object such that the bounding box matches
	 * the sizes when this function returns.
	 */
	@Override
	public void setSize(double xsize, double ysize, double zsize) {
		minorRadius = ysize/2.0;
		majorRadius = xsize/2.0 - minorRadius;

		bounds = new BoundingBox(
				-majorRadius-minorRadius,
				majorRadius+minorRadius,
				-minorRadius, minorRadius, 
				-majorRadius-minorRadius,
				majorRadius+minorRadius
		);

		cachedMesh = null;
		cachedWire = null;
	}

	/**
	 * Returns the ability for the object to convert the internal
	 * representation of the object to a triangle mesh. In this case
	 * it is APPROXIMATELY because the internal representation is
	 * the radius of two circles.
	 */
	public int canConvertToTriangleMesh()
	{
		return APPROXIMATELY;
	}
	
	/**
	 * Return a TriangleMesh which reproduces the shape of this object. 
	 * This method returns a TriangleMesh which reproduces the object 
	 * to within the specified tolerance. That is, no point on the mesh
	 * is further than tol from the corresponding point on the original 
	 * surface.
	 */
	public TriangleMesh convertToTriangleMesh(double tol)
	{
		Vec3 vertices[];

		TriangleMesh mesh;
		int faces[][];

		final int segments = findSegmentCount(tol);
		final int numberOfVertices = segments*segments;

		// Find the list of faces.
		vertices = new Vec3[numberOfVertices];
		faces = new int [2 * numberOfVertices][];

		// Each face is an array of 3 vertex numbers in counter-clockwise
		// order when viewed from the outside.


		double x;
		double y;
		double z;
		int faceCount = 0;
		// large circle		
		for(int majorIndex = 0; majorIndex < segments; majorIndex++){
			double majorTheta = (2*Math.PI*majorIndex)/segments; 
			// small circle
			for(int minorIndex = 0; minorIndex < segments; minorIndex++){

				double minorTheta = (2*Math.PI*minorIndex)/segments;
				// create new vertex
				x = (majorRadius+(minorRadius*Math.cos(minorTheta)))*(Math.cos(majorTheta));
				y = (minorRadius*Math.sin(minorTheta));
				z = (majorRadius+(minorRadius*Math.cos(minorTheta)))*(Math.sin(majorTheta));

				int v0 = majorIndex*segments+minorIndex;
				vertices[v0] = new Vec3(x, y, z);
				int v1;
				int v2;

				// for each vertex define two faces

				// first face 
				if(minorIndex+1 < segments){
					v1 = v0 + 1;
				}
				else{
					v1 = v0 - (segments-1);
				}
				v2 = v0 + segments;
				if(v2 >= numberOfVertices){
					v2 -= numberOfVertices;
				}

				faces[faceCount] = new int[3];
				faces[faceCount][0] = v0;
				faces[faceCount][1] = v1;
				faces[faceCount][2] = v2;
				faceCount++;

				// second face
				v2 = v1;
				v1 = v2 - segments;
				if(v1 < 0){
					v1 += numberOfVertices;
				}

				faces[faceCount] = new int[3];
				faces[faceCount][0] = v0;
				faces[faceCount][1] = v1;
				faces[faceCount][2] = v2;
				faceCount++;

			}
		}        

		mesh = new TriangleMesh(vertices, faces);
		mesh.copyTextureAndMaterial(this);
		return mesh;
	}

	/**
	 * Finds a number of segments that will give us a TriangleMesh
	 * that has no vertex that is greater than tol away from the
	 * ideal point location.
	 * @param tol
	 * @return
	 */
	private int findSegmentCount(double tol) {
		// Note that the greatest deviations from the tolerance
		// will be at the mid-point of a line between two points
		// drawn on the largest circle in the torus, so we need
		// only to find a number of segments that will keep the
		// points on the outside close enough that their mid-point
		// will be within the tolerance.

		double radius = majorRadius+minorRadius;
		// The first point is on the x axis, at the farthest point.
		Vec2 p0 = new Vec2(radius, 0);
		Vec2 p1 = new Vec2();
		Vec2 pM = new Vec2(); // mid-point of p0 and p1
		Vec2 pC = new Vec2(); // point on circle nearest pM
		int segs = 3 - 1; // number of segments, 3 so that torus never flat
		double distanceFromCircle;

		// Calculate new p1 and segs until distanceFromCircle <= tol 
		do{
			segs++;
			double theta = 2*Math.PI/segs;
			double halfTheta = theta/2;

			p1.set(Math.cos(theta)*radius, Math.sin(theta)*radius);
			pM = p1.plus(p0).times(HALF);
			pC.set(Math.cos(halfTheta)*radius, Math.sin(halfTheta)*radius);
			distanceFromCircle = pM.distance(pC);
		}while(distanceFromCircle > tol);

		return segs*3; // multiply by 3 because default tol doesn't give us a smooth curve
	}

	/**
	 * Creates and returns the mesh that is displayed during normal editing or
	 * when the scene is rendered.
	 */
	public RenderingMesh getRenderingMesh(double tol, boolean interactive, ObjectInfo info)
	{

		if (interactive && cachedMesh != null)
			return cachedMesh;

		RenderingMesh mesh = convertToTriangleMesh(tol).getRenderingMesh(tol, interactive, info);
		if (interactive)
			cachedMesh = mesh;

		return mesh;
	}

	/**
	 * Sets the texture for the object.
	 */
	public void setTexture(Texture tex, TextureMapping mapping)
	{
		super.setTexture(tex, mapping);
		cachedMesh = null;
		cachedWire = null;
	}

	/**
	 * Tells the system if the object can be edited or not.
	 */
	public boolean isEditable()
	{
		return true;
	}

	/**
	 * Defines the edit window for editing the object.
	 */
	public void edit(EditingWindow parent, ObjectInfo info, Runnable callback)
	{
		
		ValueField xField = new ValueField(majorRadius, ValueField.POSITIVE, 5);
		ValueField yField = new ValueField(minorRadius, ValueField.POSITIVE, 5);

		ComponentsDialog dlg = new ComponentsDialog(parent.getFrame(), 
				Translate.text(editWindowTitle), new Widget [] {xField, yField},
				new String [] {Translate.text(majorRadiusTitle), Translate.text(minorRadiusTitle)});
		if (!dlg.clickedOk())
			return;

		setSize(2.0*xField.getValue()+2.0*yField.getValue(), 2.0*yField.getValue(), 2.0*xField.getValue()+2.0*yField.getValue());
		callback.run();
	}

	/** 
	 * The following two methods are used for reading and writing files.  The first is a
	 * constructor which reads the necessary data from an input stream.  The other writes
	 * the object's representation to an output stream. 
	 */


	public Torus(DataInputStream in, Scene theScene) throws IOException, InvalidObjectException
	{
		super(in, theScene);

		short version = in.readShort();
		if (version != CURRENT_VERSION){
			throw new InvalidObjectException("Torus data is version "+version+" not "+CURRENT_VERSION+".");
		}
		majorRadius = in.readDouble();
		minorRadius = in.readDouble();
		bounds = new BoundingBox(-(majorRadius+minorRadius), (majorRadius+minorRadius), 
				-minorRadius, minorRadius,
				-(majorRadius+minorRadius), (majorRadius+minorRadius));
	}

	/**
	 * Writes the Torus data to the file.
	 */
	public void writeToFile(DataOutputStream out, Scene theScene) throws IOException
	{
		super.writeToFile(out, theScene);

		out.writeShort(CURRENT_VERSION);
		out.writeDouble(majorRadius);
		out.writeDouble(minorRadius);
	}

	/**
	 * Returns the properties of this object.
	 */
	public Property[] getProperties()
	{
		return (Property []) PROPERTIES.clone();
	}

	/**
	 * Upon request from the user interface, returns the property specified
	 * by the index value.
	 */
	public Object getPropertyValue(int index)
	{
		switch (index)
		{
		case enumMajorRadius:
			return new Double(majorRadius);
		case enumMinorRadius:
			return new Double(minorRadius);
		}
		return null;
	}

	/**
	 * Sets the property value that the used has entered into the property window
	 * on the lower right-hand side (default) of the screen.
	 */
	public void setPropertyValue(int index, Object value)
	{
		double val = ((Double) value).doubleValue();
		if (index == enumMajorRadius){ 
			setSize(2.0*(val+minorRadius), 2.0*minorRadius, 2.0*(val+minorRadius));
		}
		else if (index == enumMinorRadius){
			setSize(2.0*(majorRadius+val), 2.0*val, 2.0*(majorRadius+val));
		}
	}

	/** Return a Keyframe which describes the current pose of this object. */

	public Keyframe getPoseKeyframe()
	{
		return new TorusKeyframe(majorRadius, minorRadius);
	}

	/** Modify this object based on a pose keyframe. */

	public void applyPoseKeyframe(Keyframe k)
	{
		TorusKeyframe key = (TorusKeyframe) k;

		setSize(2.0*(key.majorRadius+key.minorRadius), 
				2.0*key.minorRadius, 
				2.0*(key.majorRadius+key.minorRadius));
	}

	/** This will be called whenever a new pose track is created for this object.  It allows
	      the object to configure the track by setting its graphable values, subtracks, etc. */

	public void configurePoseTrack(PoseTrack track)
	{
		track.setGraphableValues(new String [] {majorRadiusTitle, minorRadiusTitle},
				new double [] {2.0*majorRadius, 2.0*minorRadius},
				new double [][] {{0.0, Double.MAX_VALUE}, {0.0, Double.MAX_VALUE}});
	}

	/** Return an array containing the names of the graphable values for the keyframes
	      returned by getPoseKeyframe(). */

	public String [] getPoseValueNames()
	{
		return new String [] {majorRadiusTitle, minorRadiusTitle};
	}

	/** Get the default list of graphable values for a keyframe returned by getPoseKeyframe(). */

	public double [] getDefaultPoseValues()
	{
		return new double [] {2.0*majorRadius, 2.0*minorRadius};
	}

	/** Get the allowed range for graphable values for keyframes returned by getPoseKeyframe().
	      This returns a 2D array, where elements [n][0] and [n][1] are the minimum and maximum
	      allowed values, respectively, for the nth graphable value. */

	public double[][] getPoseValueRange()
	{
		return new double [][] {{0.0, Double.MAX_VALUE}, {0.0, Double.MAX_VALUE}};
	}

	/** Allow the user to edit a keyframe returned by getPoseKeyframe(). */

	public void editKeyframe(EditingWindow parent, Keyframe k, ObjectInfo info)
	{
		TorusKeyframe key = (TorusKeyframe) k;
		ValueField xField = new ValueField(2.0*key.majorRadius, ValueField.POSITIVE, 5);
		ValueField yField = new ValueField(2.0*key.minorRadius, ValueField.POSITIVE, 5);
		
		ComponentsDialog dlg = new ComponentsDialog(parent.getFrame(), 
				Translate.text(editWindowTitle), new Widget [] {xField, yField},
				new String [] {Translate.text(majorRadiusTitle), Translate.text(minorRadiusTitle)});
		if (!dlg.clickedOk())
			return;
		key.majorRadius = 0.5*xField.getValue();
		key.minorRadius = 0.5*yField.getValue();
	}

	/** Inner class representing a pose for a torus. */

	public static class TorusKeyframe implements Keyframe
	{
		public double majorRadius;
		public double minorRadius;

		public TorusKeyframe(double majorRadius, double minorRadius)
		{
			this.majorRadius = majorRadius;
			this.minorRadius = minorRadius;
		}

		/** Create a duplicate of this keyframe. */

		public Keyframe duplicate()
		{
			return new TorusKeyframe(majorRadius, minorRadius);
		}

		/** Create a duplicate of this keyframe for a (possibly different) object. */

		public Keyframe duplicate(Object owner)
		{
			return new TorusKeyframe(majorRadius, minorRadius);
		}

		/** Get the list of graphable values for this keyframe. */

		public double [] getGraphValues()
		{
			return new double [] {majorRadius, minorRadius};
		}

		/** Set the list of graphable values for this keyframe. */

		public void setGraphValues(double values[])
		{
			majorRadius = values[enumMajorRadius];
			minorRadius = values[enumMinorRadius];
		}

		/** These methods return a new Keyframe which is a weighted average of this one and one,
	        two, or three others. */

		public Keyframe blend(Keyframe o2, double weight1, double weight2)
		{
			TorusKeyframe k2 = (TorusKeyframe) o2;

			return new TorusKeyframe(weight1*majorRadius+weight2*k2.majorRadius,
					weight1*minorRadius+weight2*k2.minorRadius); 
		}

		/** These methods return a new Keyframe which is a weighted average of this one and one,
        two, or three others. */
		public Keyframe blend(Keyframe o2, Keyframe o3, double weight1, double weight2, double weight3)
		{
			TorusKeyframe k2 = (TorusKeyframe) o2;
			TorusKeyframe k3 = (TorusKeyframe) o3;

			return new TorusKeyframe(weight1*majorRadius+weight2*k2.majorRadius+weight3*k3.majorRadius, 
					weight1*minorRadius+weight2*k2.minorRadius+weight3*k3.minorRadius);
		}

		/** These methods return a new Keyframe which is a weighted average of this one and one,
        two, or three others. */
		public Keyframe blend(Keyframe o2, Keyframe o3, Keyframe o4, double weight1, double weight2, double weight3, double weight4)
		{
			TorusKeyframe k2 = (TorusKeyframe) o2;
			TorusKeyframe k3 = (TorusKeyframe) o3;
			TorusKeyframe k4 = (TorusKeyframe) o4;

			return new TorusKeyframe(
					weight1*majorRadius+weight2*k2.majorRadius+weight3*k3.majorRadius+weight4*k4.majorRadius, 
					weight1*minorRadius+weight2*k2.minorRadius+weight3*k3.minorRadius+weight4*k4.minorRadius
			);
		}

		/** Determine whether this keyframe is identical to another one. */

		public boolean equals(Keyframe k)
		{
			if (!(k instanceof TorusKeyframe))
				return false;
			TorusKeyframe key = (TorusKeyframe) k;
			return (key.majorRadius == majorRadius && key.minorRadius == minorRadius);
		}

		/** Write out a representation of this keyframe to a stream. */

		public void writeToStream(DataOutputStream out) throws IOException
		{
			out.writeDouble(majorRadius);
			out.writeDouble(minorRadius);
			out.writeShort(CURRENT_VERSION);
		}

		/** Reconstructs the keyframe from its serialized representation. */

		public TorusKeyframe(DataInputStream in, Object parent) throws IOException
		{
			this(in.readDouble(), in.readDouble());
			
			short version = in.readShort();
			if (version != CURRENT_VERSION){
				throw new InvalidObjectException("Torus keyframe data is version "+version+" not "+CURRENT_VERSION+".");
			}			
		}
	}
}
