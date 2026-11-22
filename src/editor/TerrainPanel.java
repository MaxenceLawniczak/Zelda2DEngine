package editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/** Onglet "Terrains" : eau, chemins... peints au pinceau avec bords automatiques (outil Terrain, A). */
public class TerrainPanel extends JPanel {

	private final DefaultListModel<Terrain> listModel = new DefaultListModel<>();
	private final JList<Terrain> list = new JList<>(listModel);

	public TerrainPanel(Runnable onSelect) {
		super(new BorderLayout(0, 6));
		setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		add(new JLabel("<html><b>Outil Terrain (A)</b> : peins la zone, les bords se posent tout seuls.<br>"
				+ "Clic gauche : peindre · Clic droit : effacer<br>Maj + glisser : rectangle · Taille du pinceau : 1-9<br><br>"
				+ "Les terrains se règlent dans res/tiles/terrains.txt</html>"), BorderLayout.NORTH);

		for (Terrain t : Terrain.load()) listModel.addElement(t);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean selected, boolean focus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(l, value, index, selected, focus);
				Terrain t = (Terrain) value;
				label.setIcon(new ImageIcon(preview(t, 14)));
				label.setText(t.name);
				label.setIconTextGap(10);
				label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
				return label;
			}
		});
		list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) onSelect.run();
		});
		if (!listModel.isEmpty()) list.setSelectedIndex(0);
		add(new JScrollPane(list), BorderLayout.CENTER);
	}

	public Terrain getSelected() {
		return list.getSelectedValue();
	}

	/** Aperçu : une petite zone 3x3 du terrain au milieu de ses bords. */
	private static BufferedImage preview(Terrain t, int cell) {
		int[][] layout = {
				{ t.edges[0b0001], t.edges[0b0011], t.edges[0b0011], t.edges[0b0010] },
				{ t.edges[0b0101], t.base, t.base, t.edges[0b1010] },
				{ t.edges[0b0101], t.base, t.base, t.edges[0b1010] },
				{ t.edges[0b0100], t.edges[0b1100], t.edges[0b1100], t.edges[0b1000] } };
		BufferedImage img = new BufferedImage(4 * cell, 4 * cell, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 4; c++) {
				int tile = layout[r][c] >= 0 ? layout[r][c] : t.outside;
				g.drawImage(EditorImages.tile(tile), c * cell, r * cell, cell, cell, null);
			}
		}
		g.dispose();
		return img;
	}
}
