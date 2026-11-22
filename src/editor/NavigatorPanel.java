package editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JViewport;

import main.EntityFactory;
import main.MapData;
import main.MapData.Placement;

/** Mini-carte de toute la map : montre la zone visible, clic/glisser pour s'y déplacer. */
public class NavigatorPanel extends JPanel {

	private static final int CELL = Math.max(2, 240 / EditorModel.COLS); // Navigateur d'environ 240 px
	private final EditorModel model;
	private final MapCanvas canvas;
	private final JViewport viewport;
	private BufferedImage image;

	public NavigatorPanel(EditorModel model, MapCanvas canvas, JViewport viewport) {
		this.model = model;
		this.canvas = canvas;
		this.viewport = viewport;
		setPreferredSize(new Dimension(EditorModel.COLS * CELL + 8, EditorModel.ROWS * CELL + 8));
		setBackground(EditorTheme.BACKGROUND);
		setToolTipText("Clic ou glisser : se déplacer sur la map");

		MouseAdapter mouse = new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) { centerOn(e.getPoint()); }
			@Override public void mouseDragged(MouseEvent e) { centerOn(e.getPoint()); }
		};
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		viewport.addChangeListener(e -> repaint());
	}

	private int originX() { return (getWidth() - EditorModel.COLS * CELL) / 2; }
	private int originY() { return (getHeight() - EditorModel.ROWS * CELL) / 2; }

	private void centerOn(Point p) {
		double col = (p.x - originX()) / (double) CELL;
		double row = (p.y - originY()) / (double) CELL;
		int cell = canvas.cell();
		int x = (int) (col * cell - viewport.getWidth() / 2.0);
		int y = (int) (row * cell - viewport.getHeight() / 2.0);
		x = Math.max(0, Math.min(canvas.getWidth() - viewport.getWidth(), x));
		y = Math.max(0, Math.min(canvas.getHeight() - viewport.getHeight(), y));
		viewport.setViewPosition(new Point(Math.max(0, x), Math.max(0, y)));
	}

	/** À appeler quand la map change (les tiles sont redessinées en mémoire). */
	public void refresh() {
		image = null;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if (image == null) {
			image = new BufferedImage(EditorModel.COLS * CELL, EditorModel.ROWS * CELL, BufferedImage.TYPE_INT_RGB);
			Graphics2D ig = image.createGraphics();
			for (int c = 0; c < EditorModel.COLS; c++) {
				for (int r = 0; r < EditorModel.ROWS; r++) {
					ig.drawImage(EditorImages.tile(model.getTile(c, r)), c * CELL, r * CELL, CELL, CELL, null);
				}
			}
			ig.dispose();
		}
		int ox = originX(), oy = originY();
		g2.drawImage(image, ox, oy, null);

		// Éléments : petits points de couleur
		for (Placement p : model.getData().placements) {
			EntityFactory.Kind k = EntityFactory.getKind(p.category, p.id);
			if (p.category.equals(MapData.TILE)) continue;
			g2.setColor(k != null ? k.color : Color.MAGENTA);
			g2.fillRect(ox + p.col * CELL, oy + p.row * CELL, CELL, CELL);
		}
		if (model.getData().hasSpawn()) {
			g2.setColor(new Color(90, 255, 140));
			g2.fillOval(ox + model.getData().spawnCol * CELL - 2, oy + model.getData().spawnRow * CELL - 2, CELL + 4, CELL + 4);
		}

		// Zone visible
		Rectangle view = viewport.getViewRect();
		double scale = CELL / (double) canvas.cell();
		g2.setColor(EditorTheme.ACCENT);
		g2.setStroke(new BasicStroke(2));
		g2.drawRect(ox + (int) (view.x * scale), oy + (int) (view.y * scale),
				Math.min(EditorModel.COLS * CELL, (int) (view.width * scale)), Math.min(EditorModel.ROWS * CELL, (int) (view.height * scale)));
	}
}
