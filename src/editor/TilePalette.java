package editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;


/** Palette des tiles : clic gauche = choisir, clic droit = activer/désactiver la collision. */
public class TilePalette extends JPanel implements Scrollable {

	private static final int CELL = 36;   // Taille d'une case de la palette
	private static final int COLUMNS = 6;

	private final EditorModel model;
	private final Runnable onChange;
	private int selected = 0;
	private int hovered = -1;

	public TilePalette(EditorModel model, Runnable onChange) {
		this.model = model;
		this.onChange = onChange;
		setBackground(new Color(40, 40, 46));
		int rows = (model.tileCount + COLUMNS - 1) / COLUMNS;
		setPreferredSize(new Dimension(COLUMNS * CELL + 4, rows * CELL + 4));
		javax.swing.ToolTipManager.sharedInstance().registerComponent(this);
		javax.swing.ToolTipManager.sharedInstance().setInitialDelay(300);

		MouseAdapter mouse = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int tile = tileAt(e.getX(), e.getY());
				if (tile < 0) return;
				if (SwingUtilities.isRightMouseButton(e)) {
					model.toggleCollision(tile);
				} else {
					selected = tile;
				}
				repaint();
				onChange.run();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				int tile = tileAt(e.getX(), e.getY());
				if (tile != hovered) {
					hovered = tile;
					setToolTipText(tile < 0 ? null : "Tile " + tile + (model.collisions.contains(tile) ? " (bloque le joueur)" : "")
							+ " - clic droit : collision");
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hovered = -1;
				repaint();
			}
		};
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
	}

	private int tileAt(int x, int y) {
		int col = (x - 2) / CELL;
		int row = (y - 2) / CELL;
		if (col < 0 || col >= COLUMNS || x < 2 || y < 2) return -1;
		int tile = row * COLUMNS + col;
		return tile < model.tileCount ? tile : -1;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int tile) {
		selected = tile;
		scrollRectToVisible(cellBounds(tile));
		repaint();
	}

	private Rectangle cellBounds(int tile) {
		return new Rectangle(2 + (tile % COLUMNS) * CELL, 2 + (tile / COLUMNS) * CELL, CELL, CELL);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		Rectangle clip = g2.getClipBounds();
		for (int tile = 0; tile < model.tileCount; tile++) {
			Rectangle r = cellBounds(tile);
			if (clip != null && !clip.intersects(r)) continue;
			BufferedImage img = EditorImages.tile(tile);
			g2.drawImage(img, r.x + 2, r.y + 2, CELL - 4, CELL - 4, null);
			if (model.collisions.contains(tile)) {
				g2.setColor(new Color(255, 40, 40, 200));
				g2.setStroke(new BasicStroke(2));
				g2.drawLine(r.x + CELL - 12, r.y + 4, r.x + CELL - 4, r.y + 12);
				g2.drawLine(r.x + CELL - 4, r.y + 4, r.x + CELL - 12, r.y + 12);
			}
			if (tile == selected) {
				g2.setColor(Color.YELLOW);
				g2.setStroke(new BasicStroke(3));
				g2.drawRect(r.x + 1, r.y + 1, CELL - 3, CELL - 3);
			} else if (tile == hovered) {
				g2.setColor(Color.WHITE);
				g2.setStroke(new BasicStroke(1));
				g2.drawRect(r.x + 1, r.y + 1, CELL - 3, CELL - 3);
			}
		}
	}

	@Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
	@Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return CELL; }
	@Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return CELL * 5; }
	@Override public boolean getScrollableTracksViewportWidth() { return true; }
	@Override public boolean getScrollableTracksViewportHeight() { return false; }
}
