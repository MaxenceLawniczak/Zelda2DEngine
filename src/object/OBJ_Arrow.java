package object;

import entity.Projectile;
import main.GamePanel;

public class OBJ_Arrow extends Projectile{
	
	public OBJ_Arrow(GamePanel gp) {
		super(gp);
		
		name = "Flèche";
		speed = 15;
		maxLife = 80;
		life = maxLife;
		attack = 2;
		useCost = 1;
		alive = false;
		getImage();
	}
	
	public void getImage() {
		down1 = setup("/projectile/arrow/ArrowDown", gp.getTileSize(),gp.getTileSize());
		up1 = setup("/projectile/arrow/ArrowUp", gp.getTileSize(),gp.getTileSize());
		left1 = setup("/projectile/arrow/ArrowLeft", gp.getTileSize(),gp.getTileSize());
		right1 = setup("/projectile/arrow/ArrowRight", gp.getTileSize(),gp.getTileSize());
	}
}
