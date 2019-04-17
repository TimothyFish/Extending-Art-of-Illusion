/**
    Tile Module Plugin from Chapter 8 of the book "Extending Art of Illusion: Scripting 3D Scene Creation"
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
package extending.aoi.tile;

import java.awt.Point;

import artofillusion.math.RGBColor;
import artofillusion.procedural.IOPort;
import artofillusion.procedural.Module;
import artofillusion.procedural.PointInfo;
import artofillusion.ui.Translate;

/**
 * Procedural Module for a texture that is made up of tiles with grout.
 * @author Timothy Fish
 *
 */
public class TileModule extends Module {
	private  boolean colorOk;
	private  double lastBlur;
	private  PointInfo point;
	private RGBColor color;

	// The following constants serve as array indexes and are in the order
	// that each port was created in the constructor.
	// Input Ports
	private static final int X_PORT = 0;
	private static final int Y_PORT = 1;
	private static final int Z_PORT = 2;
	private static final int COLOR_1_PORT = 3;
	private static final int COLOR_2_PORT = 4;
	private static final int MORTAR_WIDTH_PORT = 5;
	private static final int MORTAR_COLOR_PORT = 6;
	// Output Ports
	private static final int COLOR_PORT = 0;

	// Default colors
	private static final RGBColor COLOR1DEFAULT = new RGBColor(0.0, 0.0, 1.0); // Blue
	private static final RGBColor COLOR2DEFAULT = new RGBColor(1.0, 1.0, 1.0); // White
	private static final RGBColor MORTARCOLORDEFAULT = new RGBColor(1.0, 1.0, 0.0); // Yellow
	private static final double MORTARWIDTHDEFAULT = 0.05; // 5% of tile width

	public TileModule(){
		this(new Point(0,0));
	}

	public TileModule(Point position)
	{
		super(Translate.text("Tile"), 
				new IOPort [] {
			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, new String [] {"X", "(X)"}), 
			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, new String [] {"Y", "(Y)"}),
			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, new String [] {"Z", "(Z)"}),
			new IOPort(IOPort.COLOR, IOPort.INPUT, IOPort.TOP, new String [] {"Color 1", "(Blue)"}),
			new IOPort(IOPort.COLOR, IOPort.INPUT, IOPort.TOP, new String [] {"Color 2", "(White)"}),
			new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.BOTTOM, new String [] {"Mortar Width", "(0.05)"}),
			new IOPort(IOPort.COLOR, IOPort.INPUT, IOPort.BOTTOM, new String [] {"Mortar Color", "(Yellow)"})	    			
		}, 
		new IOPort [] {
			new IOPort(IOPort.COLOR, IOPort.OUTPUT, IOPort.RIGHT, new String [] {"Color"})
		}, 
		position);
		color = new RGBColor(0.0,0.0, 0.0);
	}

	/* New point, so the color will need to be recalculated. */

	public void init(PointInfo p)
	{
		colorOk = false;
		point = p;
	}

	public void getColor(int which, RGBColor c, double blur)
	{
		// If change needed, return last color.
		if (colorOk && blur == lastBlur)
		{
			c.copy(color);
			return;
		}
		colorOk = true;
		lastBlur = blur;

		RGBColor color1 = COLOR1DEFAULT.duplicate();
		if(linkFrom[COLOR_1_PORT] != null){
			linkFrom[COLOR_1_PORT].getColor(linkFromIndex[COLOR_1_PORT], color1, blur); 
		}

		RGBColor color2 = COLOR2DEFAULT.duplicate();
		if(linkFrom[COLOR_2_PORT] != null){
			linkFrom[COLOR_2_PORT].getColor(linkFromIndex[COLOR_2_PORT], color2, blur); 
		}

		RGBColor mortarColor = MORTARCOLORDEFAULT.duplicate();
		if(linkFrom[MORTAR_COLOR_PORT] != null){
			linkFrom[MORTAR_COLOR_PORT].getColor(linkFromIndex[MORTAR_COLOR_PORT], mortarColor, blur); 
		}

		double mortarWidth = MORTARWIDTHDEFAULT;
		if(linkFrom[MORTAR_WIDTH_PORT] != null){
			mortarWidth = linkFrom[MORTAR_WIDTH_PORT].getAverageValue(linkFromIndex[MORTAR_WIDTH_PORT], blur);
		}

		// Retrieve the point information

		// size of point relative to texture, with room for blur
		double xsize = (linkFrom[X_PORT] == null) ? 0.5*point.xsize+blur : linkFrom[X_PORT].getValueError(linkFromIndex[X_PORT], blur);
		double ysize = (linkFrom[Y_PORT] == null) ? 0.5*point.ysize+blur : linkFrom[Y_PORT].getValueError(linkFromIndex[Y_PORT], blur);
		double zsize = (linkFrom[Z_PORT] == null) ? 0.5*point.zsize+blur : linkFrom[Z_PORT].getValueError(linkFromIndex[Z_PORT], blur);
		if (xsize >= 0.5 || ysize >= 0.5 || zsize >= 0.5)
		{
			c.setRGB((color1.red+color2.red)/2.0, (color1.green+color2.green)/2.0, (color1.blue+color2.blue)/2.0);
			return;
		}

		// location of point
		double x = (linkFrom[X_PORT] == null) ? point.x : linkFrom[X_PORT].getAverageValue(linkFromIndex[X_PORT], blur);
		double y = (linkFrom[Y_PORT] == null) ? point.y : linkFrom[Y_PORT].getAverageValue(linkFromIndex[Y_PORT], blur);
		double z = (linkFrom[Z_PORT] == null) ? point.z : linkFrom[Z_PORT].getAverageValue(linkFromIndex[Z_PORT], blur);

		double xIntPart = Math.rint(x);
		double yIntPart = Math.rint(y);
		double zIntPart = Math.rint(z);
		double xFloatPart = 0.5-Math.abs(x-xIntPart);
		double yFloatPart = 0.5-Math.abs(y-yIntPart);
		double zFloatPart = 0.5-Math.abs(z-zIntPart);
		int i = (int) xIntPart, j = (int) yIntPart, k = (int) zIntPart;

		double value = ((i+j+k)&1) == 0 ? 1.0 : 0.0;
		double error;

		if (xFloatPart > xsize && yFloatPart > ysize && zFloatPart > zsize)
		{
			if(mortarWidth>0.0){
				if(xFloatPart < xsize+mortarWidth/2.0 
						|| yFloatPart < ysize+mortarWidth/2.0 
						|| zFloatPart < zsize+mortarWidth/2.0){
					c.copy(mortarColor);
					colorOk = true;
					return;
				}
			}

			error = 0.0;
			//c.copy(mortarColor);
			c.setRGB(color2.red*value+(1.0-value)*color1.red,
					color2.green*value +(1.0-value)*color1.green,
					color2.blue*value + (1.0-value)*color1.blue);
			colorOk = true;
			return;
		}
		else{
			double e1 = xFloatPart/xsize, e2 = yFloatPart/ysize, e3 = zFloatPart/zsize;
			if (e1 < e2 && e1 < e3)
			{
				error = 0.5-0.5*e1;
			}
			else if (e2 < e1 && e2 < e3)
			{
				error = 0.5-0.5*e2;
			}
			else
			{
				error = 0.5-0.5*e3;
			}
			value = error*(1.0-value) + (1.0-error)*value;
		}
		if(which == COLOR_PORT){
			if(mortarWidth > 0.0){
				c.copy(mortarColor);
			}
			else{
				c.copy(color1);
			}
			colorOk = true;
		}
	}

	public Module duplicate()
	{
		TileModule mod = new TileModule(new Point(bounds.x, bounds.y));

		mod.color.copy(color);
		return mod;
	}
}
