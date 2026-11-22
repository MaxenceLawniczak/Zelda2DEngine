package editor;

import java.awt.image.BufferedImage;

import main.UtilityTool;

/** Images utilisées par l'éditeur (chargées une seule fois). */
public class EditorImages {
	static final int SIZE = 32;

	static BufferedImage tile(int id) {
		return UtilityTool.loadImageOptional(String.format("/tiles/tile%04d", id), SIZE, SIZE);
	}

	static BufferedImage icon(String path) {
		return path == null ? null : UtilityTool.loadImageOptional(path, SIZE, SIZE);
	}
}
