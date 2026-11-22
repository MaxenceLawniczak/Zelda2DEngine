package entity;

import main.GamePanel;

public class Projectile extends Entity{

	protected Entity user;
	protected boolean piercing = false; // Traverse les ennemis (arc de voyageur)

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

	public void setPiercing(boolean piercing) {
		this.piercing = piercing;
	}
	
	public Entity getUser() {
		return user;
	}
	
	/** Renvoyé par un bouclier : le projectile repart dans l'autre sens et touche les monstres. */
	public void reflect(Entity newUser) {
		user = newUser;
		piercing = false;
		life = maxLife;
		switch (direction) {
		case "Up": direction = "Down"; break;
		case "Down": direction = "Up"; break;
		case "Left": direction = "Right"; break;
		default: direction = "Left"; break;
		}
	}

	@Override
	public void update() {

		// Tiré par le joueur : touche les monstres
		if(user == gp.getPlayer()) {
			int monIndex = gp.getCChecker().checkEntity(this, gp.getMonTab());
			if(monIndex != 999) {
				gp.getPlayer().damageMonster(monIndex, attack);
				if (!piercing) {
					alive = false;
					return;
				}
			}
		}
		// Tiré par un monstre : touche le joueur
		else {
			boolean contactPlayer = gp.getCChecker().checkPlayer(this);
			if(!gp.getPlayer().isInvincible() && contactPlayer) {
				damagePlayer(attack);
				// Un bouclier peut avoir renvoyé le projectile : il repart vers les monstres
				if (user != gp.getPlayer()) alive = false;
				return;
			}
		}

		switch(direction) {
		case "Up":    worldY -= speed; break;
		case "Down":  worldY += speed; break;
		case "Left":  worldX -= speed; break;
		case "Right": worldX += speed; break;
		}

		life --;
		collisionOn = false;

		gp.getCChecker().checkTile(this);
		if(life <= 0 || collisionOn) {
			alive = false;
		}
	}
}
