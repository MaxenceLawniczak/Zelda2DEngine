package object;

import entity.Projectile;
import main.GamePanel;

public class OBJ_Arrow extends Projectile{
	
	private GamePanel gp;
	
	public OBJ_Arrow(GamePanel gp) {
		super(gp);
		this.gp = gp;
		
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
		down1 = setup("/projectile/ArrowDown", gp.getTileSize(),gp.getTileSize());
		up1 = setup("/projectile/ArrowUp", gp.getTileSize(),gp.getTileSize());
		left1 = setup("/projectile/ArrowLeft", gp.getTileSize(),gp.getTileSize());
		right1 = setup("/projectile/ArrowRight", gp.getTileSize(),gp.getTileSize());
	}
}
