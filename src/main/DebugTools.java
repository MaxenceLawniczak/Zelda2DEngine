package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import entity.Entity;
import entity.Player;
import main.EntityFactory.Kind;
import main.MapData.Placement;

/**
 * Mode debug (touche T). Tant qu'il est actif :
 *   G : god mode (aucun dégât)          V : fly mode (traverse les murs, vitesse x2)
 *   H : affiche les hitbox              I : donne tous les objets
 *   N : la caméra suit le PNJ/monstre suivant (B : retour au joueur)
 *   Flèches haut/bas : PV actuels +/- 1  Flèches gauche/droite : PV max -/+ 1 cœur
 *   Page préc./suiv. : map précédente/suivante   R : recharge la map depuis les fichiers
 *   Clic sur la grande minimap (M) : téléportation
 * God mode et fly mode ne fonctionnent que tant que le mode debug est actif.
 */
public class DebugTools {

	private final GamePanel gp;

	private boolean enabled = false;
	private boolean godMode = false;
	private boolean flyMode = false;
	private boolean showHitboxes = false;
	private Entity focus = null; // Entité suivie par la caméra (null = joueur)

	// Petit message temporaire en bas de l'écran ("God mode : ON"...)
	private String message = "";
	private int messageCounter = 0;

	private final Font font = new Font("Consolas", Font.PLAIN, 13);
	private final Font fontBold = new Font("Consolas", Font.BOLD, 13);

	public DebugTools(GamePanel gp) {
		this.gp = gp;
	}

	// ===================== Interrupteurs =====================

	public void toggle() {
		enabled = !enabled;
		if (!enabled) focus = null;
		showMessage("Mode debug : " + (enabled ? "ON (F1 pour l'aide)" : "OFF"));
	}

	public void toggleGodMode() {
		godMode = !godMode;
		showMessage("God mode : " + onOff(godMode));
	}

	public void toggleFlyMode() {
		flyMode = !flyMode;
		showMessage("Fly mode : " + onOff(flyMode));
	}

	public void toggleHitboxes() {
		showHitboxes = !showHitboxes;
		showMessage("Hitbox : " + onOff(showHitboxes));
	}

	public boolean isEnabled() { return enabled; }
	public boolean isGodMode() { return enabled && godMode; }
	public boolean isFlyMode() { return enabled && flyMode; }
	public boolean isShowHitboxes() { return enabled && showHitboxes; }

	private static String onOff(boolean b) {
		return b ? "ON" : "OFF";
	}

	public void showMessage(String text) {
		message = text;
		messageCounter = 120;
	}

	// ===================== Actions =====================

	/** Donne un exemplaire de chaque objet que le joueur ne possède pas encore. */
	public void giveAllItems() {
		Player player = gp.getPlayer();
		int added = 0;
		for (Kind kind : EntityFactory.inventoryItems()) {
			Entity item = EntityFactory.createItem(gp, kind.id);
			if (item == null) continue;

			boolean alreadyOwned = false;
			for (Entity e : player.getInventory()) {
				if (e.getClass() == item.getClass()) {
					alreadyOwned = true;
					break;
				}
			}
			if (!alreadyOwned && player.giveItem(item)) added++;
		}
		// Flèches au maximum
		int arrowIndex = player.getArrowIndex();
		if (arrowIndex != -1) {
			Entity arrows = player.getInventory().get(arrowIndex);
			arrows.setAmount(arrows.getMaxAmount());
		}
		player.setArgent(999);
		showMessage(added + " objet(s) ajouté(s), flèches et rubis au max");
	}

	public void changeLife(int delta) {
		Player p = gp.getPlayer();
		p.setLife(Math.max(1, Math.min(p.getMaxLife(), p.getLife() + delta)));
		showMessage("PV : " + p.getLife() + " / " + p.getMaxLife());
	}

	public void changeMaxLife(int delta) {
		Player p = gp.getPlayer();
		// Entre 1 et 20 cœurs (1 cœur = 4 PV)
		p.setMaxLife(Math.max(4, Math.min(80, p.getMaxLife() + delta)));
		p.setLife(Math.min(p.getLife(), p.getMaxLife()));
		showMessage("PV max : " + p.getMaxLife() + " (" + p.getMaxLife() / 4 + " cœurs)");
	}

	/** La caméra passe au PNJ / monstre suivant, puis revient au joueur. */
	public void cycleFocus() {
		List<Entity> targets = new ArrayList<>();
		for (Entity e : gp.getNPCTab()) if (e != null) targets.add(e);
		for (Entity e : gp.getMonTab()) if (e != null && e.isAlive()) targets.add(e);

		if (targets.isEmpty()) {
			focus = null;
			showMessage("Aucun PNJ / monstre sur cette map");
			return;
		}
		int index = targets.indexOf(focus);
		if (index + 1 >= targets.size()) {
			focus = null;
			showMessage("Caméra : joueur");
		} else {
			focus = targets.get(index + 1);
			showMessage("Caméra : " + focus.getClass().getSimpleName() + " (" + (index + 2) + "/" + targets.size() + ")");
		}
	}

	public void resetFocus() {
		focus = null;
		showMessage("Caméra : joueur");
	}

	/** Entité suivie par la caméra (le joueur par défaut, ou si la cible a disparu). */
	public Entity getCameraTarget() {
		if (!enabled || focus == null) return gp.getPlayer();
		boolean stillHere = false;
		for (Entity e : gp.getNPCTab()) if (e == focus) stillHere = true;
		for (Entity e : gp.getMonTab()) if (e == focus && e.isAlive()) stillHere = true;
		if (!stillHere) focus = null;
		return focus != null ? focus : gp.getPlayer();
	}

	public void changeMap(int delta) {
		int map = gp.getCurrentMap();
		// Cherche la prochaine map qui existe vraiment
		for (int i = 0; i < gp.getMaxMap(); i++) {
			map = (map + delta + gp.getMaxMap()) % gp.getMaxMap();
			if (gp.getTileM().mapExists(map)) break;
		}
		MapData data = gp.getASetter().getMapData(map);
		int col = data.hasSpawn() ? data.spawnCol : gp.getPlayer().getWorldX() / gp.getTileSize();
		int row = data.hasSpawn() ? data.spawnRow : gp.getPlayer().getWorldY() / gp.getTileSize();
		gp.teleportPlayer(map, col, row);
		showMessage("Map " + (map + 1));
	}

	public void update() {
		if (messageCounter > 0) messageCounter--;
	}

	// ===================== Dessin =====================

	/** Hitbox dessinées par-dessus le monde (H). */
	public void drawHitboxes(Graphics2D g2) {
		if (!isShowHitboxes()) return;
		g2.setStroke(new BasicStroke(1));

		// Événements de la map
		for (Placement p : gp.getASetter().getMapData(gp.getCurrentMap()).get(MapData.EVENT)) {
			Kind k = EntityFactory.getKind(MapData.EVENT, p.id);
			Color c = k != null ? k.color : Color.MAGENTA;
			int x = gp.worldToScreenX(p.col * gp.getTileSize());
			int y = gp.worldToScreenY(p.row * gp.getTileSize());
			g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 70));
			g2.fillRect(x, y, gp.getTileSize(), gp.getTileSize());
			g2.setColor(c);
			g2.drawRect(x, y, gp.getTileSize() - 1, gp.getTileSize() - 1);
			g2.setFont(font);
			g2.drawString(p.id, x + 2, y + 12);
		}

		drawBoxes(g2, gp.getItileTab(), new Color(0, 200, 0));
		drawBoxes(g2, gp.getObjTab(), Color.YELLOW);
		drawBoxes(g2, gp.getNPCTab(), Color.CYAN);
		drawBoxes(g2, gp.getMonTab(), Color.RED);
		drawBoxes(g2, gp.getProjList().toArray(new Entity[0]), Color.ORANGE);
		drawBox(g2, gp.getPlayer(), Color.WHITE);

		// Zone touchée par l'arme pendant une attaque
		if (gp.getPlayer().isAttacking()) {
			Rectangle r = gp.getPlayer().getAttackHitbox();
			g2.setColor(new Color(255, 60, 60, 120));
			g2.fillRect(gp.worldToScreenX(r.x), gp.worldToScreenY(r.y), r.width, r.height);
		}
	}

	private void drawBoxes(Graphics2D g2, Entity[] entities, Color color) {
		for (Entity e : entities) {
			if (e != null) drawBox(g2, e, color);
		}
	}

	private void drawBox(Graphics2D g2, Entity e, Color color) {
		Rectangle s = e.getSolidArea();
		int x = gp.worldToScreenX(e.getWorldX() + e.getSolidAreaDefaultX());
		int y = gp.worldToScreenY(e.getWorldY() + e.getSolidAreaDefaultY());
		g2.setColor(color);
		g2.drawRect(x, y, s.width, s.height);
	}

	/** Panneau d'infos + aide + entité suivie (dessiné par-dessus l'UI). */
	public void drawOverlay(Graphics2D g2, int fps, long drawTimeNs) {
		// Message temporaire (même hors debug, ex : "Mode debug : OFF")
		if (messageCounter > 0) {
			g2.setFont(fontBold);
			FontMetrics fm = g2.getFontMetrics();
			int w = fm.stringWidth(message) + 20;
			int x = (gp.getScreenWidth() - w) / 2;
			int y = gp.getScreenHeight() - 40;
			g2.setColor(new Color(0, 0, 0, 180));
			g2.fillRoundRect(x, y, w, 24, 10, 10);
			g2.setColor(Color.WHITE);
			g2.drawString(message, x + 10, y + 16);
		}
		// Pas de panneaux par-dessus la grande carte
		if (!enabled || gp.getMinimap().getMode() == Minimap.FULL) return;

		Player p = gp.getPlayer();
		int col = (p.getWorldX() + p.getSolidArea().x) / gp.getTileSize();
		int row = (p.getWorldY() + p.getSolidArea().y) / gp.getTileSize();
		List<String> lines = new ArrayList<>();
		lines.add("DEBUG   FPS " + fps + "   draw " + (drawTimeNs / 1000) + " µs");
		lines.add("Map " + (gp.getCurrentMap() + 1) + "   col " + col + "  row " + row
				+ "   tile " + gp.getTileM().getMapTileNum(col, row));
		lines.add("PV " + p.getLife() + "/" + p.getMaxLife() + "   ATQ " + p.getAttack() + "   DEF " + p.getDefense());
		lines.add("[G]od " + onOff(godMode) + "  [V]ol " + onOff(flyMode) + "  [H]itbox " + onOff(showHitboxes));
		if (helpVisible) {
			lines.add("[I] tous les objets   [N] focus suivant  [B] joueur");
			lines.add("Flèches haut/bas : PV   gauche/droite : PV max");
			lines.add("PgPréc/PgSuiv : map   [R] recharger   [M] carte");
		}
		drawPanel(g2, lines, 8, gp.getTileSize() * 2 + 8);

		// Infos de l'entité suivie par la caméra
		Entity target = getCameraTarget();
		if (target != p) {
			int sx = gp.worldToScreenX(target.getWorldX());
			int sy = gp.worldToScreenY(target.getWorldY());
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2));
			g2.drawRect(sx - 2, sy - 2, gp.getTileSize() + 4, gp.getTileSize() + 4);

			List<String> info = target.getDebugInfo();
			info.add(0, "FOCUS  (N : suivant, B : joueur)");
			g2.setFont(font);
			int width = 0;
			for (String s : info) width = Math.max(width, g2.getFontMetrics().stringWidth(s));
			drawPanel(g2, info, gp.getScreenWidth() - width - 28, gp.getTileSize() * 2 + 8);
		}
	}

	private boolean helpVisible = true;

	public void toggleHelp() {
		helpVisible = !helpVisible;
	}

	private void drawPanel(Graphics2D g2, List<String> lines, int x, int y) {
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();
		int width = 0;
		for (String s : lines) width = Math.max(width, fm.stringWidth(s));
		int lineHeight = fm.getHeight();
		g2.setColor(new Color(0, 0, 0, 170));
		g2.fillRoundRect(x, y, width + 16, lines.size() * lineHeight + 10, 10, 10);
		for (int i = 0; i < lines.size(); i++) {
			g2.setFont(i == 0 ? fontBold : font);
			g2.setColor(i == 0 ? Color.YELLOW : Color.WHITE);
			g2.drawString(lines.get(i), x + 8, y + 5 + fm.getAscent() + i * lineHeight);
		}
	}
}
