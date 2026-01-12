package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_RubisGreen extends Entity{

	public OBJ_RubisGreen(GamePanel gp) {
		super(gp);

		type = typePickUpOnly;
		name = "Rubis Vert";
		value = 1;
		down1 = setup("/money/Rubis1", gp.getTileSize()/2,gp.getTileSize()/2);
	}
	
	public void use(Entity entity) {
		
		gp.getPlayer().setArgent(gp.getPlayer().getArgent() + value);
		
	}
}
