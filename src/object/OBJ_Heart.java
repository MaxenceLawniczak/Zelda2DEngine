package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Heart extends Entity{
	
	public OBJ_Heart(GamePanel gp) {
		super(gp);
		
		type = typePickUpOnly;
		name = "Coeur";
		value = 4;
		down1 = setup("/objects/pickups/HeartF", gp.getTileSize()/2,gp.getTileSize()/2);
	}
	
	public void use(Entity entity) {
		entity.setLife(entity.getLife() + value);
		
		if(entity.getLife() > entity.getMaxLife()) {
			entity.setLife(entity.getMaxLife());
		}
	}
}
