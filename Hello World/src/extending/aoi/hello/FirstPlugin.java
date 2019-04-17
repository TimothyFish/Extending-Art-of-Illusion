/**
    Hello World Plugin from Chapter 1 of the book "Extending Art of Illusion: Scripting 3D Scene Creation"
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
