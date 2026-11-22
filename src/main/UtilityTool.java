package main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class UtilityTool {

	// Cache des images déjà chargées : une même image n'est lue sur le disque qu'une seule fois
	private static final Map<String, BufferedImage> cache = new HashMap<>();

	// Dossier "res" du projet (quand on lance depuis Eclipse, le dossier de travail est la racine du projet)
	public static File RES_DIR = new File("res");

	public BufferedImage scaleImage(BufferedImage original, int width, int height) {

		// Toujours en INT_ARGB : c'est le format que Java2D dessine le plus vite
		// (les PNG indexés / ABGR d'origine sont beaucoup plus lents à afficher)
		BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = scaledImage.createGraphics();
		g2.drawImage(original, 0, 0, width, height, null);
		g2.dispose();

		return scaledImage;
	}

	/**
	 * Charge une image depuis le dossier res (ex : "/player/walk/LinkDown0", sans le .png)
	 * et la redimensionne. Renvoie null (avec un message clair) si l'image n'existe pas,
	 * au lieu de faire planter le jeu.
	 */
	public static BufferedImage loadImage(String imagePath, int width, int height) {
		BufferedImage image = loadImageOptional(imagePath, width, height);
		if (image == null) {
			System.err.println("Image introuvable : " + imagePath + ".png");
		}
		return image;
	}

	/** Comme loadImage, mais sans message d'erreur si l'image n'existe pas (images facultatives). */
	public static BufferedImage loadImageOptional(String imagePath, int width, int height) {

		String key = imagePath + "@" + width + "x" + height;
		if (cache.containsKey(key)) {
			return cache.get(key);
		}

		BufferedImage image = null;
		try (InputStream is = UtilityTool.class.getResourceAsStream(imagePath + ".png")) {
			if (is != null) {
				image = new UtilityTool().scaleImage(ImageIO.read(is), width, height);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		cache.put(key, image);
		return image;
	}

	/**
	 * Ouvre un fichier de données (map, collisions...). On lit d'abord le fichier
	 * dans le dossier res/ du projet s'il existe : comme ça les modifications faites
	 * avec l'éditeur de map sont visibles tout de suite (touche R en jeu), sans
	 * attendre qu'Eclipse recopie les fichiers dans bin/. Sinon on lit dans le classpath (.jar).
	 * Renvoie null si le fichier n'existe nulle part.
	 */
	public static InputStream openResource(String path) {
		File file = new File(RES_DIR, path);
		if (file.isFile()) {
			try {
				return new FileInputStream(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return UtilityTool.class.getResourceAsStream(path);
	}

	public static boolean resourceExists(String path) {
		return new File(RES_DIR, path).isFile() || UtilityTool.class.getResource(path) != null;
	}
}
