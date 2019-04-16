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
