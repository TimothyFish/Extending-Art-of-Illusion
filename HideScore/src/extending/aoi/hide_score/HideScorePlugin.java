/**
    Hide Score Plugin from Chapter 2 of the book "Extending Art of Illusion: Scripting for 3D Artists"
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
package extending.aoi.hide_score;

import javax.swing.SwingUtilities;

import artofillusion.LayoutWindow;
import artofillusion.Plugin;

public class HideScorePlugin implements Plugin {

	private LayoutWindow layout;

	@Override
	public void processMessage(int message, Object[] args) {
		switch (message) {
		case Plugin.SCENE_WINDOW_CREATED:
			layout = (LayoutWindow) args[0];
			hideAnimationScore(layout);
			break;
		}

	}

	private void hideAnimationScore(final LayoutWindow layout) {
		// Get the object that controls the animation score
		// Hide the score

		SwingUtilities.invokeLater(new Runnable() {

			// This method will be scheduled to run as a separate thread.
			public void run() {
				// Hide the Score Window
				layout.setScoreVisible(false);

			}
		});
	}

}
