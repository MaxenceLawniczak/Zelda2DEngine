package editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

/** Onglet "Salles" : choix du jeu de murs utilisé par l'outil Salle (W). */
public class WallSetPanel extends JPanel {

	private final DefaultListModel<WallSet> listModel = new DefaultListModel<>();
	private final JList<WallSet> list = new JList<>(listModel);
	private final JCheckBox fillInside = new JCheckBox("Remplir l'intérieur avec le sol", true);
	private final JCheckBox flip = new JCheckBox("Tracé ouvert : intérieur à gauche", false);
	private final IntSupplier selectedTile;
	private final Runnable onSelect;

	public WallSetPanel(IntSupplier selectedTile, Runnable onSelect) {
		super(new BorderLayout(0, 6));
		this.selectedTile = selectedTile;
		this.onSelect = onSelect;
		setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		JLabel help = new JLabel("<html><b>Salle (W)</b> : rectangle entouré de murs.<br><b>Tracé (T)</b> : clique des points, "
				+ "chaque clic trace un trait depuis le précédent. Reclique le 1er point pour fermer la forme, "
				+ "Entrée / double-clic / clic droit pour finir.</html>");
		add(help, BorderLayout.NORTH);

		for (WallSet w : WallSet.load()) listModel.addElement(w);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean selected, boolean focus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(l, value, index, selected, focus);
				WallSet w = (WallSet) value;
				label.setIcon(new ImageIcon(w.preview(14)));
				label.setText(w.name);
				label.setIconTextGap(10);
				label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
				return label;
			}
		});
		list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) onSelect.run();
		});
		list.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) edit(false);
			}
		});
		if (!listModel.isEmpty()) list.setSelectedIndex(0);
		add(new JScrollPane(list), BorderLayout.CENTER);

		JPanel south = new JPanel(new BorderLayout());
		JPanel options = new JPanel(new GridLayout(2, 1));
		options.add(fillInside);
		flip.setToolTipText("Tracés ouverts : met l'intérieur (le côté du sol) à gauche du trait au lieu de la droite");
		options.add(flip);
		south.add(options, BorderLayout.NORTH);
		JPanel buttons = new JPanel(new GridLayout(2, 2, 4, 4));
		buttons.add(button("Nouveau", () -> edit(true)));
		buttons.add(button("Modifier", () -> edit(false)));
		buttons.add(button("Dupliquer", this::duplicate));
		buttons.add(button("Supprimer", this::delete));
		south.add(buttons, BorderLayout.CENTER);
		add(south, BorderLayout.SOUTH);
	}

	private JButton button(String text, Runnable action) {
		JButton b = new JButton(text);
		b.setFocusable(false);
		b.addActionListener(e -> action.run());
		return b;
	}

	public WallSet getSelected() {
		return list.getSelectedValue();
	}

	public boolean isFillInside() {
		return fillInside.isSelected();
	}

	public boolean isFlip() {
		return flip.isSelected();
	}

	private void saveAll() {
		try {
			WallSet.save(java.util.Collections.list(listModel.elements()));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Impossible d'enregistrer les jeux de murs :\n" + e.getMessage());
		}
		list.repaint();
	}

	private void duplicate() {
		WallSet w = getSelected();
		if (w == null) return;
		WallSet copy = w.copy();
		copy.name = w.name + " (copie)";
		listModel.addElement(copy);
		list.setSelectedValue(copy, true);
		saveAll();
	}

	private void delete() {
		WallSet w = getSelected();
		if (w == null || listModel.size() <= 1) return;
		if (JOptionPane.showConfirmDialog(this, "Supprimer le jeu de murs \"" + w.name + "\" ?", "Supprimer",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
		listModel.removeElement(w);
		list.setSelectedIndex(0);
		saveAll();
	}

	/** Fenêtre de création / modification d'un jeu de murs, avec aperçu en direct. */
	private void edit(boolean createNew) {
		WallSet original = createNew ? null : getSelected();
		WallSet w = original != null ? original.copy() : new WallSet();
		if (createNew) {
			w.name = "Nouveau jeu de murs";
			w.floor = 184;
		}

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		JTextField name = new JTextField(w.name, 24);
		JRadioButton piecesMode = new JRadioButton("8 pièces (coins + côtés)", !w.isPattern());
		JRadioButton patternMode = new JRadioButton("Motif répété (ex : arbres 2x2)", w.isPattern());
		ButtonGroup group = new ButtonGroup();
		group.add(piecesMode);
		group.add(patternMode);

		// 8 pièces + 4 coins intérieurs + "sous le haut" = 13 réglages
		final int count = 13;
		JSpinner[] pieceSpinners = new JSpinner[count];
		JLabel[] pieceImages = new JLabel[count];
		JPanel piecesPanel = new JPanel(new GridBagLayout());
		piecesPanel.setBorder(BorderFactory.createTitledBorder("Pièces"));
		JLabel previewLabel = new JLabel();
		JSpinner floor = new JSpinner(new SpinnerNumberModel(w.floor, -1, 9999, 1));
		JLabel floorImage = new JLabel();
		JTextField patternField = new JTextField(patternText(w), 18);
		JSpinner patternW = new JSpinner(new SpinnerNumberModel(w.patternW, 1, 8, 1));
		JSpinner patternH = new JSpinner(new SpinnerNumberModel(w.patternH, 1, 8, 1));

		Runnable refresh = () -> {
			for (int i = 0; i < count; i++) {
				int value = (Integer) pieceSpinners[i].getValue();
				if (i < 8) w.pieces[i] = value;
				else if (i < 12) w.inner[i - 8] = value;
				else w.under = value;
				pieceImages[i].setIcon(value >= 0 ? new ImageIcon(EditorImages.tile(value)) : null);
			}
			w.floor = (Integer) floor.getValue();
			floorImage.setIcon(w.floor >= 0 ? new ImageIcon(EditorImages.tile(w.floor)) : null);
			if (patternMode.isSelected()) {
				w.patternW = (Integer) patternW.getValue();
				w.patternH = (Integer) patternH.getValue();
				w.pattern = parsePattern(patternField.getText(), w.patternW * w.patternH);
			} else {
				w.pattern = null;
			}
			previewLabel.setIcon(new ImageIcon(w.preview(24)));
		};

		for (int i = 0; i < count; i++) {
			int index = i;
			int value = i < 8 ? w.pieces[i] : i < 12 ? w.inner[i - 8] : w.under;
			String label = i < 8 ? WallSet.PIECE_NAMES[i] : i < 12 ? WallSet.INNER_NAMES[i - 8] + " (-1 = auto)" : "Sous le haut (falaise, -1 = rien)";
			pieceSpinners[i] = new JSpinner(new SpinnerNumberModel(value, i < 8 ? 0 : -1, 9999, 1));
			pieceImages[i] = new JLabel();
			pieceSpinners[i].addChangeListener(e -> refresh.run());
			JButton take = new JButton("← tile choisie");
			take.setFocusable(false);
			take.addActionListener(e -> pieceSpinners[index].setValue(selectedTile.getAsInt()));
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = i;
			c.insets = new Insets(1, 3, 1, 3);
			c.anchor = GridBagConstraints.WEST;
			piecesPanel.add(new JLabel(label), c);
			c.gridx = 1;
			piecesPanel.add(pieceSpinners[i], c);
			c.gridx = 2;
			piecesPanel.add(pieceImages[i], c);
			c.gridx = 3;
			piecesPanel.add(take, c);
		}

		JPanel patternPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		patternPanel.setBorder(BorderFactory.createTitledBorder("Motif (ligne par ligne)"));
		patternPanel.add(new JLabel("Largeur"));
		patternPanel.add(patternW);
		patternPanel.add(new JLabel("Hauteur"));
		patternPanel.add(patternH);
		patternPanel.add(new JLabel("Tiles"));
		patternPanel.add(patternField);
		patternW.addChangeListener(e -> refresh.run());
		patternH.addChangeListener(e -> refresh.run());
		patternField.addActionListener(e -> refresh.run());
		patternField.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override public void focusLost(java.awt.event.FocusEvent e) { refresh.run(); }
		});

		JPanel floorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		floorPanel.add(new JLabel("Sol (-1 = ne pas toucher) :"));
		floorPanel.add(floor);
		floorPanel.add(floorImage);
		JButton takeFloor = new JButton("← tile choisie");
		takeFloor.setFocusable(false);
		takeFloor.addActionListener(e -> floor.setValue(selectedTile.getAsInt()));
		floorPanel.add(takeFloor);
		floor.addChangeListener(e -> refresh.run());
		piecesMode.addActionListener(e -> refresh.run());
		patternMode.addActionListener(e -> refresh.run());

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.add(new JLabel("Nom :"));
		top.add(name);
		top.add(piecesMode);
		top.add(patternMode);

		JPanel center = new JPanel(new BorderLayout());
		center.add(piecesPanel, BorderLayout.CENTER);
		center.add(patternPanel, BorderLayout.SOUTH);

		JPanel right = new JPanel(new BorderLayout());
		right.setBorder(BorderFactory.createTitledBorder("Aperçu"));
		right.add(previewLabel, BorderLayout.CENTER);
		right.setPreferredSize(new Dimension(260, 260));

		panel.add(top, BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);
		panel.add(floorPanel, BorderLayout.SOUTH);
		refresh.run();

		int result = JOptionPane.showConfirmDialog(this, panel, createNew ? "Nouveau jeu de murs" : "Modifier le jeu de murs",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;
		refresh.run();
		w.name = name.getText().trim().isEmpty() ? "Sans nom" : name.getText().trim();

		if (original == null) {
			listModel.addElement(w);
		} else {
			listModel.set(listModel.indexOf(original), w);
		}
		list.setSelectedValue(w, true);
		saveAll();
		onSelect.run();
	}

	private static String patternText(WallSet w) {
		if (w.pattern == null) return "15 16 17 18";
		StringBuilder sb = new StringBuilder();
		for (int t : w.pattern) sb.append(sb.length() == 0 ? "" : " ").append(t);
		return sb.toString();
	}

	private static int[] parsePattern(String text, int size) {
		int[] p = new int[size];
		String[] parts = text.trim().split("[\\s,;]+");
		for (int i = 0; i < size; i++) {
			try {
				p[i] = i < parts.length ? Integer.parseInt(parts[i]) : 0;
			} catch (NumberFormatException e) {
				p[i] = 0;
			}
		}
		return p;
	}

	public List<WallSet> all() {
		return java.util.Collections.list(listModel.elements());
	}
}
