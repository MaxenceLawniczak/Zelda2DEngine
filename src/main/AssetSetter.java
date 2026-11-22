package main;

import entity.Entity;
import main.MapData.Placement;
import object.OBJ_Chest;
import tile_interactive.InteractiveTile;

/**
 * Place les objets, monstres, PNJ et tiles interactives de chaque map.
 * Tout est décrit dans res/maps/mapXX_data.txt (modifiable avec l'éditeur de map).
 */
public class AssetSetter {

	private GamePanel gp;
	private MapData[] mapData;

	public AssetSetter(GamePanel gp) {
		this.gp = gp;
		loadMapData();
	}

	public void loadMapData() {
		mapData = new MapData[gp.getMaxMap()];
		for (int map = 0; map < gp.getMaxMap(); map++) {
			mapData[map] = MapData.load(map + 1);
		}
	}

	public MapData getMapData(int map) {
		return mapData[map];
	}

	/** Relit le fichier d'une map et recrée tout son contenu (touche R en debug). */
	public void reloadMap(int map) {
		mapData[map] = MapData.load(map + 1);
		setObject(map);
		setNPC(map);
		setMonster(map);
		setInteractiveTiles(map);
	}

	public void setObject() {
		for (int map = 0; map < gp.getMaxMap(); map++) setObject(map);
	}

	public void setMonster() {
		for (int map = 0; map < gp.getMaxMap(); map++) setMonster(map);
	}

	public void setNPC() {
		for (int map = 0; map < gp.getMaxMap(); map++) setNPC(map);
	}

	public void setInteractiveTiles() {
		for (int map = 0; map < gp.getMaxMap(); map++) setInteractiveTiles(map);
	}

	private void setObject(int map) {
		int i = 0;
		int max = gp.getObjTab().length;
		for (Placement p : mapData[map].get(MapData.OBJECT)) {
			if (i >= max) { tooMany("objets", map); break; }
			Entity obj = EntityFactory.createItem(gp, p.id);
			if (obj == null) continue;

			// Contenu du coffre / paramètres (texte d'un panneau, drapeau d'une porte...)
			if (obj instanceof OBJ_Chest && p.args.length > 0) {
				Entity loot = EntityFactory.createItem(gp, p.args[0]);
				if (loot != null) loot.configure(java.util.Arrays.copyOfRange(p.args, 1, p.args.length));
				((OBJ_Chest) obj).setLoot(loot);
			} else {
				obj.configure(p.args);
			}

			// Les petits objets (cœurs, rubis) sont centrés sur la case
			int offset = obj.isPickUpOnly() ? gp.getTileSize() / 4 : 0;
			obj.setWorldX(gp.getTileSize() * p.col + offset);
			obj.setWorldY(gp.getTileSize() * p.row + offset);
			gp.setObj(map, obj, i++);
		}
		while (i < max) gp.setObj(map, null, i++);
	}

	private void setMonster(int map) {
		int i = 0;
		int max = gp.getMonTab().length;
		for (Placement p : mapData[map].get(MapData.MONSTER)) {
			if (i >= max) { tooMany("monstres", map); break; }
			Entity mon = EntityFactory.create(gp, MapData.MONSTER, p.id);
			if (mon == null) continue;
			mon.setWorldX(gp.getTileSize() * p.col);
			mon.setWorldY(gp.getTileSize() * p.row);
			gp.setMon(map, mon, i++);
		}
		while (i < max) gp.setMon(map, null, i++);
	}

	private void setNPC(int map) {
		int i = 0;
		int max = gp.getNPCTab().length;
		for (Placement p : mapData[map].get(MapData.NPC)) {
			if (i >= max) { tooMany("PNJ", map); break; }
			Entity npc = EntityFactory.create(gp, MapData.NPC, p.id);
			if (npc == null) continue;
			npc.configure(p.args);
			npc.setWorldX(gp.getTileSize() * p.col);
			npc.setWorldY(gp.getTileSize() * p.row);
			gp.setNPC(map, npc, i++);
		}
		while (i < max) gp.setNPC(map, null, i++);
	}

	private void setInteractiveTiles(int map) {
		int i = 0;
		int max = gp.getItileTab().length;
		for (Placement p : mapData[map].get(MapData.TILE)) {
			if (i >= max) { tooMany("tiles interactives", map); break; }
			InteractiveTile tile = EntityFactory.createTile(gp, p.id, p.col, p.row, p.args);
			if (tile == null) continue;
			gp.setItile(map, tile, i++);
		}
		while (i < max) gp.setItile(map, null, i++);
	}

	private void tooMany(String what, int map) {
		System.err.println("Trop de " + what + " sur la map " + (map + 1) + " : les derniers sont ignorés.");
	}
}
