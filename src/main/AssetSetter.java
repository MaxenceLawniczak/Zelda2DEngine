package main;

import entity.NPC_OldMan;
import object.OBJ_ArrowInv;
import object.OBJ_Chest;
import object.OBJ_Door;
import object.OBJ_Heart;
import object.OBJ_Key;
import monster.MON_Octo;
import object.OBJ_RubisGreen;
import tile_interactive.IT_BlockedPath;
import tile_interactive.IT_Bush;
import tile_interactive.IT_Grass;
import tile_interactive.IT_HeavyRock;
import tile_interactive.IT_Hole;
import tile_interactive.IT_Light;
import tile_interactive.IT_Rock;
import tile_interactive.IT_WallLight;

public class AssetSetter {
	
	private GamePanel gp;
	
	public AssetSetter(GamePanel gp) {
		this.gp = gp;
	}
	
	public void setObject() {
		
		int mapNum = 0;
		int i = 0;
		
		int[][] chest = {
				{29, 44},{44, 8}
		    };
		    
		    for (int[] coord : chest) {
		        OBJ_Chest chestTile = new OBJ_Chest(gp);
		        chestTile.setWorldX(gp.getTileSize() * coord[0]);
		        chestTile.setWorldY(gp.getTileSize() * coord[1]);
		        gp.setObj(mapNum,chestTile, i);
		        i++;
		    }
	}
	
	public void setMonster() {
		int mapNum = 0;
		int i = 0;
		MON_Octo octo1 = new MON_Octo(gp);
	    octo1.setWorldX(gp.getTileSize() * 12);
	    octo1.setWorldY(gp.getTileSize() * 10);
	    gp.setMon(mapNum, octo1, i);
	    /*
	    i++;
	    
	    MON_Octo octo2 = new MON_Octo(gp);
	    octo2.setWorldX(gp.getTileSize() * 15);
	    octo2.setWorldY(gp.getTileSize() * 41);
	    gp.setMon(mapNum, octo2, i);
	    
	    i++;
	    
	    MON_Octo octo3 = new MON_Octo(gp);
	    octo3.setWorldX(gp.getTileSize() * 15);
	    octo3.setWorldY(gp.getTileSize() * 6);
	    gp.setMon(mapNum, octo3, i);*/
	}
	
	public void setNPC() {
		
		int mapNum = 0;
		int i = 0;
		
		int[][] npcCoords = {
				/*{30, 35}*/
		    };
		    
		    for (int[] coord : npcCoords) {
		        NPC_OldMan npc = new NPC_OldMan(gp);
		        npc.setWorldX(coord[0]);
		        npc.setWorldY(coord[1]);
		        gp.setNPC(mapNum, npc, i);
		        i++;
		    }
		
	}
	
	public void setInteractiveTiles() {
		int mapNum = 0;
		int i = 0;
		
		int[][] blockedCoords = {
				{30, 35}
		    };
		    
		    for (int[] coord : blockedCoords) {
		        IT_BlockedPath blockedTile = new IT_BlockedPath(gp, coord[0], coord[1]);
		        gp.setItile(mapNum,blockedTile, i);
		        i++;
		    }
		
		int[][] grassCoords = {
				{42, 1},{43, 1},{44, 1},{45, 1},{46, 1},{47, 1},{48, 1},
				{42, 2},{43, 2},{44, 2},{45, 2},{46, 2},{47, 2},{48, 2},
				{43, 3},{44, 3},{45, 3},{46, 3},{47, 3},{48, 3},
				{45, 4},{46, 4},{47, 4},{48, 4},
				{28, 33},{29, 33},{30, 33},{31, 33},{32, 33},{33, 33},
				{28, 32},{29, 32},{30, 32},{31, 32},
				{28, 31},{29, 31}
		    };
		    
		    for (int[] coord : grassCoords) {
		        IT_Grass grassTile = new IT_Grass(gp, coord[0], coord[1]);
		        gp.setItile(mapNum,grassTile, i);
		        i++;
		    }
		    
		int[][] bushCoords = {
				{46,15},
				{30,28},{31,28},{32,28},
				{33,29},{34,29},{35,29},
			};
			    
			    for (int[] coord : bushCoords) {
			        IT_Bush bushTile = new IT_Bush(gp, coord[0], coord[1]);
			        gp.setItile(mapNum,bushTile, i);
			        i++;
			    }
			    
		int[][] heavyRockCoords = {
						
				    };
				    
				    for (int[] coord : heavyRockCoords) {
				        IT_HeavyRock rockTile = new IT_HeavyRock(gp, coord[0], coord[1]);
				        gp.setItile(mapNum,rockTile, i);
				        i++;
				    }
				    
		int[][] rockCoords = {
							{35,38},{34,39}
					    };
					    
					    for (int[] coord : rockCoords) {
					        IT_Rock rockTile = new IT_Rock(gp, coord[0], coord[1]);
					        gp.setItile(mapNum,rockTile, i);
					        i++;
					    }
					    
		int[][] holeCoords = {
							{34,38}
						};
						    
						    for (int[] coord : holeCoords) {
						        IT_Hole holeTile = new IT_Hole(gp, coord[0], coord[1]);
						        gp.setItile(mapNum,holeTile, i);
						        i++;
						    }
		
		mapNum ++;
		Object[][] wallLightData = {
			{18, 0, "up"}, {22, 0, "up"}, {21, 49, "down"}, {30, 49, "down"}
		};

		for (Object[] data : wallLightData) {
			int col = (int) data[0];
			int row = (int) data[1];
			String direction = (String) data[2];
						    	    
			IT_WallLight light = new IT_WallLight(gp, col, row, direction);
			gp.setItile(mapNum, light, i);
			i++;
		}

		int[][] lightCoords = {
			    {21, 45},
			    {23, 46},
			    {28, 46},
			    {30, 45}
			};
		for (int[] coord : lightCoords) {
		    IT_Light lightTile = new IT_Light(gp, coord[0], coord[1]);
		    gp.setItile(mapNum, lightTile, i);
		    i++;
		}

	}
}
