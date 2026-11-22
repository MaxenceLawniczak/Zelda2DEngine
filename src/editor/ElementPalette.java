package editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import main.EntityFactory;
import main.EntityFactory.Kind;
import main.MapData;
import main.MapData.Placement;

/** Choix de l'élément à poser (objets, monstres, PNJ, tiles interactives, événements) et de ses options. */
public class ElementPalette extends JPanel {

	private static final String[] CATEGORY_IDS = { MapData.OBJECT, MapData.MONSTER, MapData.NPC, MapData.TILE, MapData.EVENT };
	private static final String[] CATEGORY_LABELS = { "Objets et coffres", "Monstres", "PNJ", "Tiles interactives", "Événements" };
	static final String[] DIRECTIONS = { "up", "down", "left", "right" };
	static final String[] EVENT_DIRECTIONS = { "any", "up", "down", "left", "right" };

	private final JComboBox<String> categoryBox = new JComboBox<>(CATEGORY_LABELS);
	private final DefaultListModel<Kind> listModel = new DefaultListModel<>();
	private final JList<Kind> list = new JList<>(listModel);

	// Options
	private final JPanel optionsPanel = new JPanel(new GridBagLayout());
	private final JComboBox<String> lootBox = new JComboBox<>(lootChoices());
	private final JComboBox<String> wallDirBox = new JComboBox<>(DIRECTIONS);
	private final JComboBox<String> eventDirBox = new JComboBox<>(EVENT_DIRECTIONS);
	private final JSpinner targetMap = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
	private final JSpinner targetCol = new JSpinner(new SpinnerNumberModel(0, 0, EditorModel.COLS - 1, 1));
	private final JSpinner targetRow = new JSpinner(new SpinnerNumberModel(0, 0, EditorModel.ROWS - 1, 1));
	private final javax.swing.JTextField paramsField = new javax.swing.JTextField(18);

	public ElementPalette(Runnable onSelect) {
		super(new BorderLayout(0, 6));
		setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		add(categoryBox, BorderLayout.NORTH);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new KindRenderer());
		list.setVisibleRowCount(10);
		add(new JScrollPane(list), BorderLayout.CENTER);

		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		add(optionsPanel, BorderLayout.SOUTH);

		categoryBox.addActionListener(e -> fillList());
		list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				updateOptions();
				onSelect.run();
			}
		});
		fillList();
	}

	private static String[] lootChoices() {
		List<String> choices = new ArrayList<>();
		choices.add("(vide)");
		for (Kind k : EntityFactory.inventoryItems()) choices.add(k.id);
		choices.add("key");
		choices.add("heart_container");
		choices.add("crystal");
		return choices.toArray(new String[0]);
	}

	private void fillList() {
		listModel.clear();
		for (Kind k : EntityFactory.kinds(CATEGORY_IDS[categoryBox.getSelectedIndex()])) listModel.addElement(k);
		if (!listModel.isEmpty()) list.setSelectedIndex(0);
		updateOptions();
	}

	private void updateOptions() {
		optionsPanel.removeAll();
		Kind k = list.getSelectedValue();
		if (k == null) {
			optionsPanel.add(new JLabel("-"));
		} else if (k.id.equals("chest")) {
			addRow(optionsPanel, 0, "Contenu :", lootBox);
		} else if (k.id.equals("wall_light")) {
			addRow(optionsPanel, 0, "Direction :", wallDirBox);
		} else if (k.category.equals(MapData.EVENT)) {
			addRow(optionsPanel, 0, "Direction requise :", eventDirBox);
			if (k.id.equals("teleport")) {
				addRow(optionsPanel, 1, "Map cible :", targetMap);
				addRow(optionsPanel, 2, "Colonne cible :", targetCol);
				addRow(optionsPanel, 3, "Ligne cible :", targetRow);
			}
			if (k.argHint != null) addParams(optionsPanel, 1, k.argHint);
		} else if (k.argHint != null) {
			addParams(optionsPanel, 0, k.argHint);
		} else {
			optionsPanel.add(new JLabel("Aucune option"));
		}
		optionsPanel.revalidate();
		optionsPanel.repaint();
	}

	private void addParams(JPanel panel, int row, String hint) {
		addRow(panel, row, "Paramètres :", paramsField);
		paramsField.setToolTipText(hint);
		JLabel help = new JLabel("<html><i>" + hint + "</i></html>");
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = row + 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		panel.add(help, c);
	}

	private static String[] splitParams(String text) {
		text = text.trim();
		return text.isEmpty() ? new String[0] : text.split("\\s+");
	}

	static void addRow(JPanel panel, int row, String label, Component field) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = row;
		c.insets = new Insets(2, 2, 2, 2);
		c.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel(label), c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(field, c);
	}

	public Kind getSelectedKind() {
		return list.getSelectedValue();
	}

	/** Crée l'élément sélectionné (avec ses options) à la case donnée. */
	public Placement createPlacement(int col, int row) {
		Kind k = list.getSelectedValue();
		if (k == null) return null;
		String[] args = new String[0];
		if (k.id.equals("chest") && lootBox.getSelectedIndex() > 0) {
			args = new String[] { (String) lootBox.getSelectedItem() };
		} else if (k.id.equals("wall_light")) {
			args = new String[] { (String) wallDirBox.getSelectedItem() };
		} else if (k.id.equals("teleport")) {
			args = new String[] { (String) eventDirBox.getSelectedItem(),
					String.valueOf(targetMap.getValue()), String.valueOf(targetCol.getValue()), String.valueOf(targetRow.getValue()) };
		} else if (k.category.equals(MapData.EVENT)) {
			String[] params = k.argHint != null ? splitParams(paramsField.getText()) : new String[0];
			args = new String[params.length + 1];
			args[0] = (String) eventDirBox.getSelectedItem();
			System.arraycopy(params, 0, args, 1, params.length);
		} else if (k.argHint != null) {
			args = splitParams(paramsField.getText());
		}
		return new Placement(k.category, k.id, col, row, args);
	}

	/** Prépare un téléporteur vers une case précise (utilisé par "Choisir la destination"). */
	public void setTeleportTarget(int map, int col, int row) {
		targetMap.setValue(map);
		targetCol.setValue(col);
		targetRow.setValue(row);
	}

	/** Boîte de dialogue pour modifier un élément déjà posé. Renvoie true si modifié. */
	public static boolean editPlacement(Component parent, Placement p) {
		JPanel panel = new JPanel(new GridBagLayout());
		JComboBox<String> loot = new JComboBox<>(lootChoices());
		JComboBox<String> dir = new JComboBox<>(p.category.equals(MapData.EVENT) ? EVENT_DIRECTIONS : DIRECTIONS);
		JSpinner map = new JSpinner(new SpinnerNumberModel(p.argInt(1, 1), 1, 99, 1));
		JSpinner col = new JSpinner(new SpinnerNumberModel(p.argInt(2, 0), 0, EditorModel.COLS - 1, 1));
		JSpinner row = new JSpinner(new SpinnerNumberModel(p.argInt(3, 0), 0, EditorModel.ROWS - 1, 1));

		if (p.id.equals("chest")) {
			loot.setSelectedItem(p.arg(0, "(vide)"));
			addRow(panel, 0, "Contenu du coffre :", loot);
		} else if (p.id.equals("wall_light") || p.category.equals(MapData.EVENT)) {
			dir.setSelectedItem(p.arg(0, p.category.equals(MapData.EVENT) ? "any" : "up"));
			addRow(panel, 0, "Direction :", dir);
			if (p.id.equals("teleport")) {
				addRow(panel, 1, "Map cible :", map);
				addRow(panel, 2, "Colonne cible :", col);
				addRow(panel, 3, "Ligne cible :", row);
			}
		} else if (hintOf(p) != null) {
			// Paramètres libres (texte d'un panneau, drapeau...)
			javax.swing.JTextField field = new javax.swing.JTextField(String.join(" ", p.args), 30);
			addRow(panel, 0, "Paramètres :", field);
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = 1;
			c.gridwidth = 2;
			panel.add(new JLabel("<html><i>" + hintOf(p) + "</i></html>"), c);
			if (JOptionPane.showConfirmDialog(parent, panel, "Modifier : " + p.id, JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return false;
			p.args = splitParams(field.getText());
			return true;
		} else {
			return false; // Rien à modifier
		}
		javax.swing.JTextField eventText = new javax.swing.JTextField(
				p.args.length > 1 && !p.id.equals("teleport") ? String.join(" ", java.util.Arrays.copyOfRange(p.args, 1, p.args.length)) : "", 30);
		if (p.category.equals(MapData.EVENT) && hintOf(p) != null) addRow(panel, 4, "Texte :", eventText);

		int result = JOptionPane.showConfirmDialog(parent, panel, "Modifier : " + p.id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return false;

		if (p.id.equals("chest")) {
			p.args = loot.getSelectedIndex() > 0 ? new String[] { (String) loot.getSelectedItem() } : new String[0];
		} else if (p.id.equals("teleport")) {
			p.args = new String[] { (String) dir.getSelectedItem(), String.valueOf(map.getValue()), String.valueOf(col.getValue()), String.valueOf(row.getValue()) };
		} else {
			String[] params = p.category.equals(MapData.EVENT) && hintOf(p) != null ? splitParams(eventText.getText()) : new String[0];
			p.args = new String[params.length + 1];
			p.args[0] = (String) dir.getSelectedItem();
			System.arraycopy(params, 0, p.args, 1, params.length);
		}
		return true;
	}

	private static String hintOf(Placement p) {
		Kind k = EntityFactory.getKind(p.category, p.id);
		return k != null ? k.argHint : null;
	}

	public static boolean isEditable(Placement p) {
		return p.id.equals("chest") || p.id.equals("wall_light") || p.category.equals(MapData.EVENT) || hintOf(p) != null;
	}

	/** Affiche chaque élément avec son icône. */
	private static class KindRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
			Kind k = (Kind) value;
			label.setText(k.label + "  (" + k.id + ")");
			label.setIcon(new ImageIcon(iconFor(k, 24)));
			return label;
		}
	}

	/** Icône d'un élément ; carré de couleur avec l'initiale si pas d'image. */
	static Image iconFor(Kind k, int size) {
		BufferedImage img = EditorImages.icon(k.iconPath);
		if (img != null) return img.getScaledInstance(size, size, Image.SCALE_FAST);
		BufferedImage square = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = square.createGraphics();
		g.setColor(k.color);
		g.fillRoundRect(1, 1, size - 2, size - 2, 6, 6);
		g.setColor(Color.BLACK);
		g.drawString(k.id.substring(0, 1).toUpperCase(), size / 2 - 4, size / 2 + 5);
		g.dispose();
		return square;
	}
}
