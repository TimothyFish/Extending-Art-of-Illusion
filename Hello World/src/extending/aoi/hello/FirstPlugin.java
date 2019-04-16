package extending.aoi.hello;

import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.ui.MessageDialog;

public class FirstPlugin implements Plugin {
	@Override
	public void processMessage(int msg, Object[] args) {
		switch (msg) {
		case Plugin.SCENE_WINDOW_CREATED:
			LayoutWindow layout = (LayoutWindow) args[0];
			new MessageDialog(layout, "Hello World!");
			break;
		}
	}
}
