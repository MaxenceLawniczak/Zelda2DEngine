package main;

import entity.Entity;
import tile_interactive.InteractiveTile;

public class CollisionChecker {
	
	private GamePanel gp;
	
	public CollisionChecker(GamePanel gp) {
		this.gp = gp;
	}
	
	public void checkTile(Entity entity) {
	    int entityLeftWorldX = entity.getWorldX() + entity.getSolidArea().x;
	    int entityRightWorldX = entity.getWorldX() + entity.getSolidArea().x + entity.getSolidArea().width;
	    int entityTopWorldY = entity.getWorldY() + entity.getSolidArea().y;
	    int entityBotWorldY = entity.getWorldY() + entity.getSolidArea().y + entity.getSolidArea().height;

	    int entityLeftCol = entityLeftWorldX / gp.getTileSize();
	    int entityRightCol = entityRightWorldX / gp.getTileSize();
	    int entityTopRow = entityTopWorldY / gp.getTileSize();
	    int entityBotRow = entityBotWorldY / gp.getTileSize();

	    int tileNum1, tileNum2;

	    switch (entity.getDirection()) {
	        case "Up":
	            entityTopRow = (entityTopWorldY - entity.getSpeed()) / gp.getTileSize();
	            entityTopRow = Math.max(0, Math.min(entityTopRow, gp.getMaxWorldRows() - 1)); // Correction pour éviter les indices hors limites
	            tileNum1 = gp.getTileM().getMapTileNum(entityLeftCol, entityTopRow);
	            tileNum2 = gp.getTileM().getMapTileNum(entityRightCol, entityTopRow);

	            if (gp.getTileM().getTile(tileNum1).isCollision() || gp.getTileM().getTile(tileNum2).isCollision()) {
	                entity.setCollisionOn(true);
	            }
	            break;

	        case "Down":
	            entityBotRow = (entityBotWorldY + entity.getSpeed()) / gp.getTileSize();
	            entityBotRow = Math.max(0, Math.min(entityBotRow, gp.getMaxWorldRows() - 1)); // Correction pour éviter les indices hors limites
	            tileNum1 = gp.getTileM().getMapTileNum(entityLeftCol, entityBotRow);
	            tileNum2 = gp.getTileM().getMapTileNum(entityRightCol, entityBotRow);

	            if (gp.getTileM().getTile(tileNum1).isCollision() || gp.getTileM().getTile(tileNum2).isCollision()) {
	                entity.setCollisionOn(true);
	            }
	            break;

	        case "Left":
	            entityLeftCol = (entityLeftWorldX - entity.getSpeed()) / gp.getTileSize();
	            entityLeftCol = Math.max(0, Math.min(entityLeftCol, gp.getMaxWorldCols() - 1)); // Correction pour éviter les indices hors limites
	            tileNum1 = gp.getTileM().getMapTileNum(entityLeftCol, entityTopRow);
	            tileNum2 = gp.getTileM().getMapTileNum(entityLeftCol, entityBotRow);

	            if (gp.getTileM().getTile(tileNum1).isCollision() || gp.getTileM().getTile(tileNum2).isCollision()) {
	                entity.setCollisionOn(true);
	            }
	            break;

	        case "Right":
	            entityRightCol = (entityRightWorldX + entity.getSpeed()) / gp.getTileSize();
	            entityRightCol = Math.max(0, Math.min(entityRightCol, gp.getMaxWorldCols() - 1)); // Correction pour éviter les indices hors limites
	            tileNum1 = gp.getTileM().getMapTileNum(entityRightCol, entityTopRow);
	            tileNum2 = gp.getTileM().getMapTileNum(entityRightCol, entityBotRow);

	            if (gp.getTileM().getTile(tileNum1).isCollision() || gp.getTileM().getTile(tileNum2).isCollision()) {
	                entity.setCollisionOn(true);
	            }
	            break;
	    }
	}
	
	public int checkObject(Entity entity, boolean player) {
		
		int index = 999;
		
		for(int i = 0 ; i < gp.getObjTab().length ; i++) {
			
			if (gp.getObj(i) != null) {

				entity.getSolidArea().x = entity.getWorldX() + entity.getSolidArea().x;
				entity.getSolidArea().y = entity.getWorldY() + entity.getSolidArea().y;
				
				gp.getObj(i).getSolidArea().x = gp.getObj(i).getWorldX() + gp.getObj(i).getSolidArea().x;
				gp.getObj(i).getSolidArea().y = gp.getObj(i).getWorldY() + gp.getObj(i).getSolidArea().y;
			
				switch(entity.getDirection()) {
				case "Up": entity.getSolidArea().y -= entity.getSpeed(); break;
				case "Down": entity.getSolidArea().y += entity.getSpeed(); break;
				case "Left": entity.getSolidArea().x -= entity.getSpeed(); break;
				case "Right": entity.getSolidArea().x += entity.getSpeed(); break;
				}
				
				if(entity.getSolidArea().intersects(gp.getObj(i).getSolidArea())) {
					if(gp.getObj(i).isCollision())
						entity.setCollisionOn(true);
					if(player)
						index = i;
				}
				
				entity.getSolidArea().x = entity.getSolidAreaDefaultX();
				entity.getSolidArea().y = entity.getSolidAreaDefaultY();
				gp.getObj(i).getSolidArea().x = gp.getObj(i).getSolidAreaDefaultX();
				gp.getObj(i).getSolidArea().y = gp.getObj(i).getSolidAreaDefaultY();
			}
		}
		
		return index;
	}
	
	//NPC ou Monstres
	public int checkEntity(Entity entity, Entity[] target) {
	    int index = 999;

	    if (target == null) {
	        return index;
	    }

	    for (int i = 0; i < target.length; i++) {
	        if (target[i] != null) {
	            entity.getSolidArea().x = entity.getWorldX() + entity.getSolidArea().x;
	            entity.getSolidArea().y = entity.getWorldY() + entity.getSolidArea().y;

	            target[i].getSolidArea().x = target[i].getWorldX() + target[i].getSolidArea().x;
	            target[i].getSolidArea().y = target[i].getWorldY() + target[i].getSolidArea().y;

	            switch (entity.getDirection()) {
	                case "Up":    entity.getSolidArea().y -= entity.getSpeed(); break;
	                case "Down":  entity.getSolidArea().y += entity.getSpeed(); break;
	                case "Left":  entity.getSolidArea().x -= entity.getSpeed(); break;
	                case "Right": entity.getSolidArea().x += entity.getSpeed(); break;
	            }

	            if (entity.getSolidArea().intersects(target[i].getSolidArea())) {
	                if (target[i] != entity) {
	                    if (!(target[i] instanceof InteractiveTile) || ((InteractiveTile) target[i]).isSolid()) {
	                        entity.setCollisionOn(true);
	                    }
	                    index = i;
	                }
	            }

	            // Reset solid areas
	            entity.getSolidArea().x = entity.getSolidAreaDefaultX();
	            entity.getSolidArea().y = entity.getSolidAreaDefaultY();
	            target[i].getSolidArea().x = target[i].getSolidAreaDefaultX();
	            target[i].getSolidArea().y = target[i].getSolidAreaDefaultY();
	        }
	    }

	    return index;
	}

	
	//Entité avec Player
	public boolean checkPlayer(Entity entity) {
	    boolean contactPlayer = false;

	    // Mise à jour des coordonnées de la zone solide pour l'entité et le joueur
	    entity.getSolidArea().x = entity.getWorldX() + entity.getSolidArea().x;
	    entity.getSolidArea().y = entity.getWorldY() + entity.getSolidArea().y;

	    gp.getPlayer().getSolidArea().x = gp.getPlayer().getWorldX() + gp.getPlayer().getSolidArea().x;
	    gp.getPlayer().getSolidArea().y = gp.getPlayer().getWorldY() + gp.getPlayer().getSolidArea().y;

	    // Déplacement de l'entité selon sa direction
	    switch(entity.getDirection()) {
	        case "Up": entity.getSolidArea().y -= entity.getSpeed(); break;
	        case "Down": entity.getSolidArea().y += entity.getSpeed(); break;
	        case "Left": entity.getSolidArea().x -= entity.getSpeed(); break;
	        case "Right": entity.getSolidArea().x += entity.getSpeed(); break;
	    }

	    // Vérification de la collision avec le joueur
	    // (les dégâts sont gérés par l'entité elle-même via damagePlayer)
	    if(entity.getSolidArea().intersects(gp.getPlayer().getSolidArea())) {
	        entity.setCollisionOn(true);
	        contactPlayer = true;
	    }

	    // Réinitialisation des zones solides après la vérification
	    entity.getSolidArea().x = entity.getSolidAreaDefaultX();
	    entity.getSolidArea().y = entity.getSolidAreaDefaultY();
	    gp.getPlayer().getSolidArea().x = gp.getPlayer().getSolidAreaDefaultX();
	    gp.getPlayer().getSolidArea().y = gp.getPlayer().getSolidAreaDefaultY();

	    return contactPlayer;
	}
}
