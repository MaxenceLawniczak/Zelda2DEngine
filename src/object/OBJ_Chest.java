package object;

import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

public class OBJ_Chest extends Entity{
	
	public OBJ_Chest(GamePanel gp) {
		super(gp);
		
		type = typeChest;
		name = "Coffre";
		up1 = setup("/objects/chest", gp.getTileSize(), gp.getTileSize());
		down1 = setup("/objects/chest", gp.getTileSize(), gp.getTileSize());
		left1 = setup("/objects/chest", gp.getTileSize(), gp.getTileSize());
		right1 = setup("/objects/chest", gp.getTileSize(), gp.getTileSize());
		collisionOn = true;
	}
	
	@Override
	public void draw(Graphics2D g2) {
	    super.draw(g2); 
	}

}
