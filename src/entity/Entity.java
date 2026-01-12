package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

public class Entity {

	//Références et position
	protected GamePanel gp;
	protected int worldX, worldY;
	protected int speed;
	protected int amount;
	protected int maxAmount;
	protected int value;
	
	//Images et Sprites
	protected BufferedImage up1, up2, up3;
	protected BufferedImage down1, down2, down3;
	protected BufferedImage left1, left2, left3;
	protected BufferedImage right1, right2, right3;
	protected BufferedImage attackUp[];
	protected BufferedImage attackDown[];
	protected BufferedImage attackLeft[];
	protected BufferedImage attackRight[];
	protected BufferedImage deathAnimation[];
	
	//Etat du personnage
	protected String direction = "Down";
	protected boolean collisionOn = false;
	protected boolean attacking = false;
	protected boolean invincible = false;
	protected boolean alive = true;
	protected boolean dying = false;
	protected boolean hpBarOn = false;
	
	//Zones de collision et attaque
	protected Rectangle solidArea;
	protected Rectangle attackArea = new Rectangle(0, 0, 0, 0);
	protected int solidAreaDefaultX, solidAreaDefaultY;
	
	//Counters
	protected int spriteCounter = 0;
	protected int spriteNum = 1;
	protected int invincibleCounter = 0;
	protected int dyingCounter = 0;
	protected int actionLockCounter = 0;
	protected int hpBarCounter = 0;
	protected int shotAvailableCounter = 0;
	
	//Dialogue
	protected String dialogues[] = new String[20];
	protected int dialogueIndex = 0;
	
	//For Objects
	protected BufferedImage imageF,image1,imageH,image3,imageE;
	protected String name;
	
	//Player Stats
	protected int maxLife;
	protected int life;
	protected int strength;
	protected int dexterity;
	protected int attack;
	protected int defense;
	protected int argent;
	protected Entity currentWeapon;
	protected Entity currentShield;
	protected Entity currentBow;
	protected Projectile projectile;
	
	//Type
	protected int type;
	protected final int typePlayer = 0;
	protected final int typeNPC = 1;
	protected final int typeMonster = 2;
	protected final int typeBranch = 3;
	protected final int typeSword = 4;
	protected final int typeWeakShield = 5;
	protected final int typeConsumable = 6;
	protected final int typeBokoBow = 7;
	protected final int typeArrow = 8;
	protected final int typePickUpOnly = 9;
	protected final int typePotionSoin = 10;
	protected final int typeChest = 11;

	
	//Item attributes
	protected int attackValue;
	protected int defenseValue;
	protected String description = "";
	protected int useCost;
	
	public Entity(GamePanel gp) {
		this.gp = gp;
		solidArea = new Rectangle(0,0,gp.getTileSize(),gp.getTileSize());
	}
	
	public void setAction() {}
	public void damageReaction() {}
	public void update() {
		
		setAction();
		
		collisionOn = false;
		gp.getCChecker().checkTile(this);
		gp.getCChecker().checkObject(this, false);
		gp.getCChecker().checkEntity(this, gp.getNPCTab());
		gp.getCChecker().checkEntity(this, gp.getMonTab());
		boolean contactPlayer = gp.getCChecker().checkPlayer(this);
		
		if(this.type == typeMonster && contactPlayer) {
			damagePlayer(attack);
		}
		
		if(!collisionOn) {
			switch(direction) {
			case "Up": worldY -= speed; break;
			case "Down": worldY += speed; break;
			case "Left": worldX -= speed; break;
			case "Right": worldX += speed; break;
			}
		}
		spriteCounter ++;
		if(spriteCounter > 15) {
			if(spriteNum == 1) 
				spriteNum = 2;
			else if(spriteNum == 2)
				spriteNum = 3;
			else if(spriteNum == 3)
				spriteNum = 4;
			else if(spriteNum == 4)
				spriteNum = 1;
			spriteCounter = 0;
		}
		
		if(invincible) {
			invincibleCounter++;
			if(invincibleCounter > 60) {
				invincibleCounter = 0;
				invincible = false;
			}
		}
	}
	
	public int getDefense() {
		return defense;
	}

	public int getAttack() {
		return attack;
	}
	
	public void damagePlayer(int attack) {
		if(gp.getPlayer().isInvincible()) {
			int damage = attack - gp.getPlayer().getDefense();
			
			if(damage < 0) 
				damage = 0;
			
			
			gp.getPlayer().setLife(gp.getPlayer().getLife() - damage);
			gp.getPlayer().setInvincible(true);
		}
		if (shotAvailableCounter < 30) {
			shotAvailableCounter ++;
		}
	}
	
	public void draw(Graphics2D g2) {
	    BufferedImage image = null;
	    int screenX = worldX - gp.getPlayer().getWorldX() + gp.getPlayer().getScreenX();
	    int screenY = worldY - gp.getPlayer().getWorldY() + gp.getPlayer().getScreenY();

	    if (isOnScreen() && alive) {
	        image = getSpriteForDirection();
	        
	        //Barre d'HP
	        if(type == 2 && hpBarOn) { 
	        	
	        	double oneScale = (double)gp.getTileSize()/maxLife;
	        	double hpBarValue = oneScale * life;
	        	
	        	g2.setColor(new Color(35,35,35));
	        	g2.fillRect(screenX-1, screenY - 11, gp.getTileSize() + 2, 7);
	        	
	        	g2.setColor(new Color(255,0,30));
	        	g2.fillRect(screenX, screenY - 10, (int)hpBarValue, 5);
	        	
	        	hpBarCounter ++;
	        	
	        	if(hpBarCounter > 600) {
	        		hpBarCounter = 0;
	        		hpBarOn = false;
	        	}
	        }
	       

	        if (invincible) {
	        	hpBarOn = true;
	        	hpBarCounter = 0;
	            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
	        }

	        if (dying) {
	            dyingAnimation(g2, screenX, screenY);
	        } else {
	            g2.drawImage(image, screenX, screenY, null);
	        }

	        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	    }
	}

	private boolean isOnScreen() {
	    int playerX = gp.getPlayer().getWorldX();
	    int playerY = gp.getPlayer().getWorldY();
	    int screenX = gp.getPlayer().getScreenX();
	    int screenY = gp.getPlayer().getScreenY();
	    int tileSize = gp.getTileSize();

	    return worldX > playerX - (screenX + tileSize) &&
	           worldX < playerX + (screenX + tileSize) &&
	           worldY > playerY - (screenY + tileSize) &&
	           worldY < playerY + (screenY + tileSize);
	}

	private BufferedImage getSpriteForDirection() {
	    BufferedImage[] sprites = switch (direction) {
	        case "Up" -> new BufferedImage[]{up1, up2, up3};
	        case "Down" -> new BufferedImage[]{down1, down2, down3};
	        case "Left" -> new BufferedImage[]{left1, left2, left3};
	        case "Right" -> new BufferedImage[]{right1, right2, right3};
	        default -> null;
	    };
	    return (sprites != null) ? sprites[(spriteNum - 1) % sprites.length] : null;
	}

	public void dyingAnimation(Graphics2D g2, int screenX, int screenY) {
	    if (dyingCounter < 30) { // Clignotement sur 30 frames
	        float alpha = (dyingCounter % 10 < 5) ? 1.0f : 0.5f; // Alterne entre 1.0f et 0.5f
	        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
	        g2.drawImage(getSpriteForDirection(), screenX, screenY, null);
	        dyingCounter++;
	    } else {
	        // Fin de l'animation de mort
	        dying = false;
	        alive = false;
	        gp.removeMonster(this); // Suppression du monstre via une méthode à implémenter dans GamePanel
	    }
	}

	public BufferedImage setup(String imagePath, int width, int height) {
	    UtilityTool uTool = new UtilityTool();
	    BufferedImage image = null;
	    
	    try {
	        image = ImageIO.read(getClass().getResourceAsStream(imagePath +".png"));
	        image = uTool.scaleImage(image,  width, height);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return image;
	}

	public int getWorldX() {
		return worldX;
	}
	
	public int getWorldY() {
		return worldY;
	}
	
	public Rectangle getSolidArea() {
		return solidArea;
	}
	
	public String getDirection() {
		return direction;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public void setCollisionOn(boolean b) {
		collisionOn = b;
	}
	
	public int getSolidAreaDefaultX() {
		return solidAreaDefaultX;
	}
	
	public int getSolidAreaDefaultY() {
		return solidAreaDefaultY;
	}
	
	public void setWorldX(int worldX) {
		this.worldX = worldX;
	}
	
	public void setWorldY(int worldY) {
		this.worldY = worldY;
	}
	
	public void speak() {}

	public int getMaxLife() {
		return maxLife;
	}

	public void setMaxLife(int maxLife) {
		this.maxLife = maxLife;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}
	
	public BufferedImage getImageF() {
		return imageF;
	}
	
	public BufferedImage getImage1() {
		return image1;
	}
	
	public BufferedImage getImageH() {
		return imageH;
	}
	
	public BufferedImage getImage3() {
		return image3;
	}
	
	public BufferedImage getImageE() {
		return imageE;
	}
	
	public boolean isCollision() {
		return collisionOn;
	}

	public boolean isAlive() {
		return alive;
	}
	
	public boolean isDying() {
		return dying;
	}

	public int getDyingCounter() {
		return dyingCounter;
	}
	
	public int getDeathAnimationLength() {
		return deathAnimation.length;
	}

	public int getAttackValue() {
		return attackValue;
	}
	
	public int getDefenseValue() {
		return defenseValue;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getDexterity() {
		return dexterity;
	}

	public void setDexterity(int dexterity) {
		this.dexterity = dexterity;
	}

	public Entity getCurrentWeapon() {
		return currentWeapon;
	}

	public void setCurrentWeapon(Entity currentWeapon) {
		this.currentWeapon = currentWeapon;
	}

	public Entity getCurrentShield() {
		return currentShield;
	}

	public void setCurrentShield(Entity currentShield) {
		this.currentShield = currentShield;
	}

	public void setAttackValue(int attackValue) {
		this.attackValue = attackValue;
	}

	public void setDefenseValue(int defenseValue) {
		this.defenseValue = defenseValue;
	}
	
	public int getArgent() {
		return argent;
	}

	public void setArgent(int argent) {
		this.argent = argent;
	}

	public BufferedImage getDown1() {
		return down1;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public int getAmount() {
		return amount;
	}

	public void setAmount(int i) {
		amount = i;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public int getType() {
		return type;
	}
	
	public void dropItem(Entity droppedItem) {
	    for (int i = 0; i < gp.getObjTab().length; i++) {
	        if (gp.getObj(i) == null) {
	            gp.setObj(gp.getCurrentMap(),droppedItem, i);

	            // Positionnement des objets
	            if (droppedItem.name == "Rubis Vert") {
	                gp.getObj(i).setWorldX(worldX + gp.getTileSize()/4);
	                gp.getObj(i).setWorldY(worldY + gp.getTileSize()/4);
	            } else if(droppedItem.name == "Coeur"){
	            	gp.getObj(i).setWorldX(worldX + gp.getTileSize()/4);
	                gp.getObj(i).setWorldY(worldY + gp.getTileSize()/4);
	            } else {
	            	gp.getObj(i).setWorldX(worldX);
	                gp.getObj(i).setWorldY(worldY);
	            }
	            break;
	        }
	    }
	}

	public void use(Entity entity) {
		
	}

	public void checkDrop() {
		
	}
}
