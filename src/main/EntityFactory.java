package main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import entity.Entity;
import entity.NPC_OldMan;
import entity.NPC_Villager;
import monster.MON_Octo;
import object.*;
import tile_interactive.*;

/**
 * Catalogue de tout ce qu'on peut placer sur une map, identifié par un nom court
 * ("katana", "octo", "grass"...). Utilisé par les fichiers mapXX_data.txt,
 * l'éditeur de map, le contenu des coffres et le mode debug (give all).
 *
 * Pour ajouter un nouvel objet : créer sa classe puis l'ajouter ici avec une ligne.
 */
public final class EntityFactory {

	public interface TileConstructor {
		InteractiveTile create(GamePanel gp, int col, int row, String[] args);
	}

	/** Description d'un élément plaçable (sert aussi à l'éditeur, qui n'a pas de GamePanel). */
	public static class Kind {
		public final String category;  // MapData.OBJECT, MONSTER, NPC, TILE, EVENT
		public final String id;
		public final String label;     // Nom affiché dans l'éditeur
		public final String iconPath;  // Image affichée dans l'éditeur (null = carré de couleur)
		public final Color color;      // Couleur de secours / des événements
		public final boolean inventoryItem; // Peut aller dans l'inventaire (give all, coffres)
		Function<GamePanel, Entity> constructor;
		TileConstructor tileConstructor;
		public String argHint = null; // Paramètres libres (texte, drapeau...) : explication pour l'éditeur

		Kind args(String hint) {
			argHint = hint;
			return this;
		}

		Kind(String category, String id, String label, String iconPath, Color color, boolean inventoryItem) {
			this.category = category;
			this.id = id;
			this.label = label;
			this.iconPath = iconPath;
			this.color = color;
			this.inventoryItem = inventoryItem;
		}
	}

	private static final Map<String, Kind> KINDS = new LinkedHashMap<>();

	private static void item(String id, String label, String icon, Function<GamePanel, Entity> c) {
		Kind k = new Kind(MapData.OBJECT, id, label, icon, Color.YELLOW, true);
		k.constructor = c;
		KINDS.put(MapData.OBJECT + ":" + id, k);
	}

	private static void entity(String category, String id, String label, String icon, Function<GamePanel, Entity> c) {
		Kind k = new Kind(category, id, label, icon, category.equals(MapData.MONSTER) ? Color.RED : Color.CYAN, false);
		k.constructor = c;
		KINDS.put(category + ":" + id, k);
	}

	private static void tile(String id, String label, String icon, TileConstructor c) {
		Kind k = new Kind(MapData.TILE, id, label, icon, Color.GREEN, false);
		k.tileConstructor = c;
		KINDS.put(MapData.TILE + ":" + id, k);
	}

	private static void event(String id, String label, Color color) {
		KINDS.put(MapData.EVENT + ":" + id, new Kind(MapData.EVENT, id, label, null, color, false));
	}

	static {
		// ===== Armes =====
		item("tree_branch", "Branche d'arbre", "/objects/weapons/TreeBranch", OBJ_Tree_Branch::new);
		item("wooden_sword", "Épée en bois", "/objects/weapons/WoodenSword", OBJ_Wooden_Sword::new);
		item("rapier", "Rapière", "/objects/weapons/Rapier", OBJ_Rapier::new);
		item("knight_sword", "Épée de chevalier", "/objects/weapons/KnightSword", OBJ_Knight_Sword::new);
		item("scimitar", "Cimeterre", "/objects/weapons/Scimitar", OBJ_Scimitar::new);
		item("katana", "Katana", "/objects/weapons/Katana", OBJ_Katana::new);
		item("long_sword", "Épée longue", "/objects/weapons/LongSword", OBJ_Long_Sword::new);
		item("royal_guard_sword", "Épée de la Garde Royale", "/objects/weapons/RoyalGuardSword", OBJ_Royal_Guard_Sword::new);
		item("heavy_sword", "Épée lourde", "/objects/weapons/HeavySword", OBJ_Heavy_Sword::new);
		item("master_sword", "Lame sacrée", "/objects/weapons/MasterSword", OBJ_Master_Sword::new);
		// ===== Boucliers =====
		item("weak_shield", "Petit bouclier", "/objects/shields/WeakShield", OBJ_Weak_Shield::new);
		item("old_shield", "Vieux bouclier", "/objects/shields/OldShield", OBJ_Old_Shield::new);
		item("wood_shield", "Bouclier en bois", "/objects/shields/BigShield", OBJ_Wood_Shield::new);
		item("soldier_shield", "Bouclier de soldat", "/objects/shields/SoldierShield", OBJ_Soldier_Shield::new);
		item("knight_shield", "Bouclier de chevalier", "/objects/shields/KnightShield", OBJ_Knight_Shield::new);
		item("reinforced_shield", "Bouclier renforcé", "/objects/shields/ReinforcedShield", OBJ_Reinforced_Shield::new);
		item("eyed_shield", "Bouclier à l'œil", "/objects/shields/EyedShield", OBJ_Eyed_Shield::new);
		item("sun_shield", "Bouclier solaire", "/objects/shields/SunShield", OBJ_Sun_Shield::new);
		item("royal_guard_shield", "Bouclier de la Garde Royale", "/objects/shields/RyalGuardShield", OBJ_Royal_Guard_Shield::new);
		item("hylian_shield", "Bouclier du héros", "/objects/shields/HylianShield", OBJ_Hylian_Shield::new);
		// ===== Arcs et consommables =====
		item("boko_bow", "Arc Boko", "/objects/bows/BokoBow", OBJ_Boko_Bow::new);
		item("normal_bow", "Arc de voyageur", "/objects/bows/NormalBow", OBJ_Normal_Bow::new);
		item("arrows", "Flèches (x10)", "/objects/inventory/ArrowInventory", gp -> new OBJ_ArrowInv(gp, 10));
		item("heal_potion", "Potion de soin", "/objects/inventory/PotionSoin", OBJ_HealPot::new);
		// ===== Objets posés au sol (ramassés directement) =====
		Kind heart = new Kind(MapData.OBJECT, "heart", "Cœur", "/objects/pickups/HeartF", Color.PINK, false);
		heart.constructor = OBJ_Heart::new;
		KINDS.put("object:heart", heart);
		Kind rupee = new Kind(MapData.OBJECT, "rupee_green", "Rubis vert", "/objects/money/Rubis1", Color.GREEN, false);
		rupee.constructor = OBJ_RubisGreen::new;
		KINDS.put("object:rupee_green", rupee);
		Kind chest = new Kind(MapData.OBJECT, "chest", "Coffre", "/objects/containers/Chest", Color.ORANGE, false);
		chest.constructor = OBJ_Chest::new;
		KINDS.put("object:chest", chest);
		Kind key = new Kind(MapData.OBJECT, "key", "Petite clé", null, new Color(255, 210, 60), false);
		key.constructor = OBJ_Key::new;
		KINDS.put("object:key", key);
		Kind door = new Kind(MapData.OBJECT, "door", "Porte fermée à clé", "/tiles/tile0193", new Color(150, 80, 200), false)
				.args("Drapeau accompli à l'ouverture (facultatif)");
		door.constructor = OBJ_Door::new;
		KINDS.put("object:door", door);
		Kind sign = new Kind(MapData.OBJECT, "sign", "Panneau", null, new Color(170, 110, 50), false)
				.args("Texte du panneau (// = page suivante)");
		sign.constructor = OBJ_Sign::new;
		KINDS.put("object:sign", sign);
		Kind heartContainer = new Kind(MapData.OBJECT, "heart_container", "Réceptacle de cœur", "/objects/pickups/HeartF", Color.PINK, false);
		heartContainer.constructor = OBJ_HeartContainer::new;
		KINDS.put("object:heart_container", heartContainer);
		Kind crystal = new Kind(MapData.OBJECT, "crystal", "Éclat du Cristal d'Aube (fin)", null, new Color(140, 225, 255), false);
		crystal.constructor = OBJ_Crystal::new;
		KINDS.put("object:crystal", crystal);

		// ===== Monstres et PNJ =====
		entity(MapData.MONSTER, "octo", "Octorok", "/monster/octo/OctoDown0", MON_Octo::new);
		entity(MapData.MONSTER, "octo_blue", "Octorok bleu (robuste)", "/monster/octo/OctoDown0", gp -> new MON_Octo(gp, MON_Octo.BLUE));
		entity(MapData.MONSTER, "octo_gold", "Octorok doré (rapide)", "/monster/octo/OctoDown0", gp -> new MON_Octo(gp, MON_Octo.GOLD));
		entity(MapData.NPC, "oldman", "Aldric, l'ermite", "/player/walk/LinkDown0", NPC_OldMan::new);
		entity(MapData.NPC, "villager", "Villageois", "/player/walk/LinkDown1", NPC_Villager::new);
		KINDS.get("npc:villager").args("Nom : texte // page suivante...");
		entity(MapData.NPC, "villager_red", "Villageois (rouge)", "/player/walk/LinkDown1", gp -> new NPC_Villager(gp, 0.99f, 0.6f));
		KINDS.get("npc:villager_red").args("Nom : texte // page suivante...");
		entity(MapData.NPC, "villager_gold", "Villageois (jaune)", "/player/walk/LinkDown1", gp -> new NPC_Villager(gp, 0.12f, 0.7f));
		KINDS.get("npc:villager_gold").args("Nom : texte // page suivante...");
		entity(MapData.NPC, "villager_grey", "Villageois (gris)", "/player/walk/LinkDown1", gp -> new NPC_Villager(gp, 0.6f, 0.08f));
		KINDS.get("npc:villager_grey").args("Nom : texte // page suivante...");

		// ===== Tiles interactives =====
		tile("grass", "Herbe", "/tiles_interactive/Grass", (gp, c, r, a) -> new IT_Grass(gp, c, r));
		tile("bush", "Buisson", "/tiles_interactive/Bush", (gp, c, r, a) -> new IT_Bush(gp, c, r));
		tile("rock", "Rocher", "/tiles_interactive/Rock", (gp, c, r, a) -> new IT_Rock(gp, c, r));
		tile("heavy_rock", "Gros rocher", "/tiles_interactive/HeavyRock", (gp, c, r, a) -> new IT_HeavyRock(gp, c, r));
		tile("hole", "Trou", "/tiles_interactive/Hole", (gp, c, r, a) -> new IT_Hole(gp, c, r));
		tile("blocked_path", "Passage bloqué", "/tiles_interactive/BlockedPath", (gp, c, r, a) -> new IT_BlockedPath(gp, c, r));
		tile("light", "Lumière", "/tiles_interactive/lights/Light0", (gp, c, r, a) -> new IT_Light(gp, c, r));
		tile("gate", "Barrière (s'ouvre avec un drapeau)", null, (gp, c, r, a) -> new IT_Gate(gp, c, r, a.length > 0 ? a[0] : ""));
		KINDS.get("tile:gate").args("Drapeau qui l'ouvre (a+b = les deux)");
		tile("push_rock", "Rocher à pousser", "/tiles_interactive/Rock", (gp, c, r, a) -> new IT_PushRock(gp, c, r));
		tile("plate", "Dalle (rocher dessus)", null, (gp, c, r, a) -> new IT_Plate(gp, c, r, a.length > 0 ? a[0] : "dalles"));
		KINDS.get("tile:plate").args("Drapeau accompli quand toutes ses dalles sont enfoncées");
		tile("torch", "Torche (à frapper)", "/tiles_interactive/lights/Light0",
				(gp, c, r, a) -> new IT_Torch(gp, c, r, a.length > 0 ? a[0] : "torches", a.length > 1 ? Integer.parseInt(a[1]) : 0));
		KINDS.get("tile:torch").args("Drapeau [ordre 1, 2, 3...]");
		tile("wall_light", "Lumière murale", "/tiles_interactive/lights/WallLightUp0",
				(gp, c, r, a) -> new IT_WallLight(gp, c, r, a.length > 0 ? a[0] : "up"));

		// ===== Événements =====
		event("pit", "Trou (dégâts)", new Color(200, 40, 40));
		event("heal", "Source de soin", new Color(40, 160, 255));
		event("teleport", "Téléporteur", new Color(190, 60, 255));
		event("message", "Message (une seule fois)", new Color(240, 220, 90));
		KINDS.get("event:message").args("Texte affiché (// = page suivante)");
	}

	public static Kind getKind(String category, String id) {
		return KINDS.get(category + ":" + id);
	}

	public static List<Kind> kinds(String category) {
		List<Kind> list = new ArrayList<>();
		for (Kind k : KINDS.values()) {
			if (k.category.equals(category)) list.add(k);
		}
		return list;
	}

	/** Tous les objets qu'on peut avoir dans l'inventaire (give all, contenu des coffres). */
	public static List<Kind> inventoryItems() {
		List<Kind> list = new ArrayList<>();
		for (Kind k : KINDS.values()) {
			if (k.inventoryItem) list.add(k);
		}
		return list;
	}

	/** Crée un objet, monstre ou PNJ. Renvoie null (avec un message) si l'id est inconnu. */
	public static Entity create(GamePanel gp, String category, String id) {
		Kind k = getKind(category, id);
		if (k == null || k.constructor == null) {
			System.err.println("Élément inconnu : " + category + " " + id);
			return null;
		}
		return k.constructor.apply(gp);
	}

	public static Entity createItem(GamePanel gp, String id) {
		return create(gp, MapData.OBJECT, id);
	}

	public static InteractiveTile createTile(GamePanel gp, String id, int col, int row, String[] args) {
		Kind k = getKind(MapData.TILE, id);
		if (k == null || k.tileConstructor == null) {
			System.err.println("Tile interactive inconnue : " + id);
			return null;
		}
		return k.tileConstructor.create(gp, col, row, args);
	}

	private EntityFactory() {}
}
