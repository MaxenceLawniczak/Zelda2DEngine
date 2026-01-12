package entity;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.GamePanel;

public class Projectile extends Entity{

	protected Entity user;
	
	public Projectile(GamePanel gp) {
		super(gp);
	}
	
	public void set(int worldX, int worldY, String direction, boolean alive, Entity user) {
		this.worldX = worldX;
		this.worldY = worldY;
		this.direction = direction;
		this.alive = alive;
		this.user = user;
		this.life = this.maxLife;
		
	}

	@SuppressWarnings("unused")
	public void update() {
		
		if(user == gp.getPlayer()) {
			int monIndex = gp.getCChecker().checkEntity(this, gp.getMonTab());
			if(monIndex != 999) {
				gp.getPlayer().damageMonster(monIndex, attack);
				alive = false;
			}
		}
		if(user != gp.getPlayer()) {
			boolean contactPlayer = gp.getCChecker().checkPlayer(this);
			if(!gp.getPlayer().isInvincible() && contactPlayer) {
				damagePlayer(attack);
				alive = false;
			}
		}
		
		ArrayList<Entity> inventory = gp.getPlayer().getInventory();
		int arrowIndex = gp.getPlayer().getArrowIndex();
		BufferedImage image;
		if (inventory.get(arrowIndex).getAmount() >= 0) {
			switch(direction) {
			case "Up":
				image = up1;
				worldY -= speed; break;
			case "Down":
				image = down1;
				worldY += speed; break;
			case "Left":
				image = left1;
				worldX -= speed; break;
			case "Right":
				image = right1;
				worldX += speed; break;
			}
		}
		
		life --;
		collisionOn = false;
		
		gp.getCChecker().checkTile(this);
		if(life<=0 || collisionOn) {
			alive = false;
		}
		
	}
}
