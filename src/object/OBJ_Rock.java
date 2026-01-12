package object;

import entity.Projectile;
import main.GamePanel;

public class OBJ_Rock extends Projectile{
	
	private GamePanel gp;
	
	public OBJ_Rock(GamePanel gp) {
		super(gp);
		this.gp = gp;
		
		name = "Caillou";
		speed = 10;
		maxLife = 80;
		life = maxLife;
		attack = 2;
		useCost = 1;
		alive = false;
		getImage();
	}
	
	public void getImage() {
		down1 = setup("/projectile/Rock", gp.getTileSize(),gp.getTileSize());
		up1 = setup("/projectile/Rock", gp.getTileSize(),gp.getTileSize());
		left1 = setup("/projectile/Rock", gp.getTileSize(),gp.getTileSize());
		right1 = setup("/projectile/Rock", gp.getTileSize(),gp.getTileSize());
	}
}
