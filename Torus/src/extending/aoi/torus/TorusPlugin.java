/**
    Torus Plugin from Chapter 6 of the book "Extending Art of Illusion: Scripting for 3D Artists"
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
 * Torus Plugin
 */
package extending.aoi.torus;

import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.ui.ToolPalette;

/**
 * @author Timothy Fish
 *
 */
public class TorusPlugin implements Plugin {

	private LayoutWindow layout;
	private ToolPalette toolPalette;
	private CreateTorusTool theTorusTool;

	public TorusPlugin(){
		layout = null;
		toolPalette = null;
		theTorusTool = null;
	}
	/**
	 * Entry point for the plugin.
	 */
	@Override
	public void processMessage(int message, Object[] args) {
		switch (message) {
		case Plugin.SCENE_WINDOW_CREATED:
			// Do initialization first.
			layout = (LayoutWindow) args[0];
		    toolPalette = layout.getToolPalette();	
			theTorusTool = new CreateTorusTool(layout);
			
			// Add the Torus button to the Tool Palette
			toolPalette.addTool(theTorusTool);

			break;
		}
	}
}
