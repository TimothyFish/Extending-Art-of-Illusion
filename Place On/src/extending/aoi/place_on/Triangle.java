/**
 * A simple class to hold the points of a triangle and to 
 * compute their normal.
 */
package extending.aoi.place_on;

import artofillusion.math.Vec3;

/**
 * @author Timothy Fish
 *
 */
public class Triangle {
	private Vec3 p0;
	private Vec3 p1;
	private Vec3 p2;
	private Vec3 normal;
	
	public Triangle(final Vec3 p0, final Vec3 p1, final Vec3 p2){
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		this.setNormal();
	}

	private void setNormal() {
		Vec3 n = p1.minus(p0).cross(p2.minus(p0));
		this.normal = n.times(1/Math.sqrt(n.dot(n)));
	}
	
	public Vec3 getNormal(){
		return normal;
	}

	public void setP0(Vec3 p0) {
		this.p0 = p0;
		this.setNormal();
	}

	public Vec3 getP0() {
		return p0;
	}

	public void setP1(Vec3 p1) {
		this.p1 = p1;
		this.setNormal();
	}

	public Vec3 getP1() {
		return p1;
	}

	public void setP2(Vec3 p2) {
		this.p2 = p2;
		this.setNormal();
	}

	public Vec3 getP2() {
		return p2;
	}
	

}
