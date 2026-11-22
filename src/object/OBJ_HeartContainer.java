package object;

import entity.Entity;
import main.GamePanel;
import main.Sprites;

/** Réceptacle de cœur : +1 cœur maximum et soin complet. */
public class OBJ_HeartContainer extends Entity{

	public OBJ_HeartContainer(GamePanel gp) {
		super(gp);
		type = typePickUpOnly;
		name = "Réceptacle de cœur";
		down1 = Sprites.heartContainer(gp.getTileSize());
	}

	@Override
	public void use(Entity entity) {
		entity.setMaxLife(entity.getMaxLife() + 4);
		entity.setLife(entity.getMaxLife());
		gp.ui.showNotification("Réceptacle de cœur ! Vie maximum +1 cœur");
	}
}
