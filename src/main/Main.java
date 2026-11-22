package main;

import javax.swing.JFrame;

public class Main {

	/**
	 * Lancement normal : écran titre.
	 * Lancement depuis l'éditeur de map : --map <numéro> --pos <col> <row>
	 * (démarre directement en jeu, sur cette map, à cette position).
	 */
	public static void main(String[]args) {

		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setTitle("Zelda");

		GamePanel gp = new GamePanel();
		window.add(gp);

		window.pack();

		window.setLocationRelativeTo(null);
		window.setVisible(true);

		gp.setupGame();

		int map = -1, col = 0, row = 0;
		for (int i = 0; i < args.length; i++) {
			try {
				if (args[i].equals("--map") && i + 1 < args.length) {
					map = Integer.parseInt(args[++i]) - 1;
				} else if (args[i].equals("--pos") && i + 2 < args.length) {
					col = Integer.parseInt(args[++i]);
					row = Integer.parseInt(args[++i]);
				}
			} catch (NumberFormatException e) {
				System.err.println("Argument invalide : " + args[i]);
			}
		}
		if (map >= 0 && map < gp.getMaxMap()) {
			gp.startAt(map, col, row);
			window.setTitle("Zelda - test de la map " + (map + 1));
		}

		gp.startGameThread();
	}
}
