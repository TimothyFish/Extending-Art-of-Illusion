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
