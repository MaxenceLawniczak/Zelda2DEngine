package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

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
	protected boolean collisionOn = false; // L'entité est en train de heurter quelque chose (recalculé à chaque frame)
	protected boolean collision = false;   // L'entité est un obstacle solide (coffre, porte...)
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
	protected final int typeShield = 5;
	protected final int typeConsumable = 6;
	protected final int typeBow = 7;
	protected final int typeArrow = 8;
	protected final int typePickUpOnly = 9;
	protected final int typePotionSoin = 10;
	protected final int typeChest = 11;
	protected final int typeDoor = 12;
	protected final int typeSign = 13;

	
	//Item attributes
	protected int attackValue;
	protected int defenseValue;
	protected String description = "";
	protected int useCost;
	
	// Armes : durée (en frames) des 3 étapes de l'animation d'attaque.
	// Plus c'est petit, plus l'arme frappe vite.
	protected int motion1Duration = 5;
	protected int motion2Duration = 10;
	protected int motion3Duration = 15;

	// ===== Équipements : durabilité et spécificité =====
	protected int maxDurability = 0;   // 0 = incassable
	protected int durability = 0;
	protected String specialName = "";        // Ex : "Coup critique"
	protected String specialDescription = ""; // Ex : "25 % de chance de faire double dégâts"

	// ===== Effets d'état (monstres) =====
	protected int stunCounter = 0;      // Étourdi : ne bouge plus
	protected int burnCounter = 0;      // En feu : perd des PV régulièrement
	protected int bleedCounter = 0;     // Saigne : perd des PV régulièrement
	protected int knockbackCounter = 0; // Repoussé
	protected String knockbackDirection = "Down";
	
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
		gp.getCChecker().checkEntity(this, gp.getItileTab());
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
			// 2 ou 3 images d'animation selon l'entité (avant : 4 étapes pour 3 images -> clignotement)
			int frames = up3 != null ? 3 : 2;
			spriteNum = spriteNum % frames + 1;
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
		// Le joueur gère lui-même invincibilité, bouclier, défense et god mode
		gp.getPlayer().receiveDamage(attack, this);
	}
	
	public void draw(Graphics2D g2) {
	    BufferedImage image = null;
	    int screenX = gp.worldToScreenX(worldX);
	    int screenY = gp.worldToScreenY(worldY);

	    if (isOnScreen() && alive) {
	        image = getSpriteForDirection();
	        
	        //Barre d'HP
	        if(type == typeMonster && hpBarOn) {
	        	
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
	        if (!dying) drawStatus(g2, screenX, screenY);
	    }
	}

	protected boolean isOnScreen() {
	    int screenX = gp.worldToScreenX(worldX);
	    int screenY = gp.worldToScreenY(worldY);
	    int tileSize = gp.getTileSize();

	    return screenX > -tileSize && screenX < gp.getScreenWidth() + tileSize &&
	           screenY > -tileSize && screenY < gp.getScreenHeight() + tileSize;
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

	// Avance l'animation de mort (appelé par GamePanel.update, pas pendant le dessin)
	public void updateDying() {
	    dyingCounter++;
	    if (dyingCounter >= 30) {
	        // Fin de l'animation : GamePanel s'occupe du drop et de la suppression
	        dying = false;
	        alive = false;
	    }
	}

	public void dyingAnimation(Graphics2D g2, int screenX, int screenY) {
	    // Clignotement sur 30 frames : alterne entre opaque et semi-transparent
	    float alpha = (dyingCounter % 10 < 5) ? 1.0f : 0.5f;
	    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
	    g2.drawImage(getSpriteForDirection(), screenX, screenY, null);
	}

	public BufferedImage setup(String imagePath, int width, int height) {
	    return UtilityTool.loadImage(imagePath, width, height);
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
		return collision;
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

	            // Positionnement des objets (les petits objets sont centrés sur la case)
	            if (droppedItem.type == typePickUpOnly) {
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

	/** Infos affichées quand on focus cette entité en mode debug (touche N). */
	public java.util.List<String> getDebugInfo() {
		java.util.List<String> info = new java.util.ArrayList<>();
		info.add(getClass().getSimpleName() + (name != null ? " - " + name : ""));
		info.add("Pos : col " + (worldX / gp.getTileSize()) + ", row " + (worldY / gp.getTileSize())
				+ "  (" + worldX + ", " + worldY + ")");
		if (maxLife > 0) info.add("PV : " + life + " / " + maxLife);
		info.add("Direction : " + direction + "   Vitesse : " + speed);
		if (attack > 0 || defense > 0) info.add("Attaque : " + attack + "   Défense : " + defense);
		StringBuilder state = new StringBuilder("État :");
		if (alive) state.append(" vivant");
		if (dying) state.append(" mourant");
		if (invincible) state.append(" invincible(").append(invincibleCounter).append(")");
		if (attacking) state.append(" attaque");
		if (collisionOn) state.append(" collision");
		info.add(state.toString());
		info.add("Action lock : " + actionLockCounter + "   Tir : " + shotAvailableCounter);
		return info;
	}

	public boolean isPickUpOnly() {
		return type == typePickUpOnly;
	}
	
	public boolean isMonster() {
		return type == typeMonster;
	}
	
	public boolean isWeapon() {
		return type == typeSword || type == typeBranch;
	}
	
	public boolean isShield() {
		return type == typeShield;
	}
	
	public boolean isBow() {
		return type == typeBow;
	}

	public Rectangle getAttackArea() {
		return attackArea;
	}
	
	public int getMotion1Duration() {
		return motion1Duration;
	}
	
	public int getMotion2Duration() {
		return motion2Duration;
	}
	
	public int getMotion3Duration() {
		return motion3Duration;
	}
	
	public boolean isAttacking() {
		return attacking;
	}
	
	public int getValue() {
		return value;
	}

	// ===================== Durabilité =====================

	public boolean isUnbreakable() {
		return maxDurability <= 0;
	}

	public int getDurability() {
		return durability;
	}

	public int getMaxDurability() {
		return maxDurability;
	}

	/** Chance (0 à 1) de perdre de la durabilité à chaque utilisation. */
	public float wearChance() {
		return 1f;
	}

	/** Use l'objet. Renvoie true s'il vient de se briser. */
	public boolean wear(int amount) {
		if (isUnbreakable() || Math.random() >= wearChance()) return false;
		durability = Math.max(0, durability - amount);
		return durability == 0;
	}

	public String getSpecialName() {
		return specialName;
	}

	public String getSpecialDescription() {
		return specialDescription;
	}

	// ===================== Spécificités (redéfinies par chaque arme / bouclier / arc) =====================

	/** Arme : modifie les dégâts d'un coup (critique, ignorer la défense, riposte...). */
	public int modifyDamage(Player player, Entity target, int damage) { return damage; }
	/** Arme : appelé quand un coup touche un monstre (killed = le monstre est vaincu). */
	public void onHit(Player player, Entity target, boolean killed) {}
	/** Arme : appelé au début de chaque coup (ex : rayon de la Lame sacrée). */
	public void onAttack(Player player) {}
	/** Arme : peut-elle briser cette tile interactive ? */
	public boolean canBreakTile(tile_interactive.InteractiveTile tile) { return tile.isDestructible(); }
	/** Arme : appelé quand on coupe une tile (herbe, buisson...). */
	public void onTileCut(Player player, tile_interactive.InteractiveTile tile) {}
	/** Bouclier : de quels côtés protège-t-il ? (front = devant, side = côtés, back = derrière) */
	public boolean blocksFrom(String side) { return side.equals("front"); }
	/** Bouclier : appelé quand une attaque est bloquée. */
	public void onBlock(Player player, Entity attacker, boolean perfect) {}
	/** Bouclier : appelé à chaque frame pendant qu'on se protège. */
	public void whileGuarding(Player player, int guardFrames) {}
	/** Bouclier : garde-t-on sa vitesse normale en se protégeant ? (sinon on avance 2x moins vite) */
	public boolean canMoveWhileGuarding() { return false; }
	/** Arc : chance de ne pas consommer de flèche. */
	public float arrowSaveChance() { return 0f; }
	/** Arc : les flèches traversent-elles les ennemis ? */
	public boolean piercingArrows() { return false; }
	/** Appelé quand l'objet se brise. */
	public void onBreak(Player player) {}

	// ===================== Effets d'état =====================

	public void stun(int frames) {
		stunCounter = Math.max(stunCounter, frames);
	}

	public void burn(int frames) {
		burnCounter = Math.max(burnCounter, frames);
	}

	public void bleed(int frames) {
		bleedCounter = Math.max(bleedCounter, frames);
	}

	public void knockback(String dir, int frames) {
		knockbackDirection = dir;
		knockbackCounter = frames;
	}

	public boolean isStunned() {
		return stunCounter > 0;
	}

	/** Inflige des dégâts directs (feu, saignement, épines...). Renvoie true si l'entité meurt. */
	public boolean takeDamage(int damage, Color color) {
		if (dying || !alive || damage <= 0) return false;
		life -= damage;
		hpBarOn = true;
		hpBarCounter = 0;
		gp.addFloatingText("-" + damage, worldX + gp.getTileSize() / 2, worldY, color);
		if (life <= 0) {
			life = 0;
			dying = true;
			return true;
		}
		return false;
	}

	/**
	 * Fait avancer les effets d'état. Renvoie true si l'entité ne peut pas agir cette frame
	 * (repoussée ou étourdie).
	 */
	public boolean updateStatus() {
		if (burnCounter > 0) {
			burnCounter--;
			if (burnCounter % 45 == 0) takeDamage(1, new Color(255, 140, 30));
		}
		if (bleedCounter > 0) {
			bleedCounter--;
			if (bleedCounter % 60 == 0) takeDamage(1, new Color(200, 20, 30));
		}
		if (dying) return true;
		if (knockbackCounter > 0) {
			knockbackCounter--;
			String savedDirection = direction;
			int savedSpeed = speed;
			direction = knockbackDirection;
			speed = gp.getTileSize() / 6;
			collisionOn = false;
			gp.getCChecker().checkTile(this);
			gp.getCChecker().checkObject(this, false);
			gp.getCChecker().checkEntity(this, gp.getItileTab());
			if (!collisionOn) {
				switch (direction) {
				case "Up": worldY -= speed; break;
				case "Down": worldY += speed; break;
				case "Left": worldX -= speed; break;
				default: worldX += speed; break;
				}
			} else {
				knockbackCounter = 0; // Contre un mur : on s'arrête
			}
			direction = savedDirection;
			speed = savedSpeed;
			updateInvincibility();
			return true;
		}
		if (stunCounter > 0) {
			stunCounter--;
			updateInvincibility();
			return true;
		}
		return false;
	}

	protected void updateInvincibility() {
		if (invincible) {
			invincibleCounter++;
			if (invincibleCounter > 60) {
				invincibleCounter = 0;
				invincible = false;
			}
		}
	}

	/** Petits effets visuels : étoiles (étourdi), flammes (feu), gouttes (saignement). */
	protected void drawStatus(Graphics2D g2, int sx, int sy) {
		int ts = gp.getTileSize();
		long t = System.nanoTime() / 50_000_000L;
		if (stunCounter > 0) {
			for (int i = 0; i < 3; i++) {
				double a = t * 0.3 + i * Math.PI * 2 / 3;
				int x = (int) (sx + ts / 2 + Math.cos(a) * ts / 3);
				int y = (int) (sy - ts / 6 + Math.sin(a) * ts / 8);
				g2.setColor(Color.YELLOW);
				g2.fillPolygon(new int[] { x, x + 3, x, x - 3 }, new int[] { y - 4, y, y + 4, y }, 4);
			}
		}
		if (burnCounter > 0) {
			for (int i = 0; i < 3; i++) {
				int x = sx + ts / 5 + i * ts / 4;
				int h = ts / 4 + (int) ((t + i * 3) % 4) * 3;
				int y = sy + ts - ts / 6;
				g2.setColor(new Color(255, 120, 0, 200));
				g2.fillPolygon(new int[] { x - 5, x + 5, x }, new int[] { y, y, y - h }, 3);
				g2.setColor(new Color(255, 230, 80, 220));
				g2.fillPolygon(new int[] { x - 2, x + 2, x }, new int[] { y, y, y - h / 2 }, 3);
			}
		}
		if (bleedCounter > 0 && (t / 4) % 2 == 0) {
			g2.setColor(new Color(200, 20, 30));
			g2.fillOval(sx + ts / 3, sy + ts / 2 + (int) (t % 6), 5, 7);
			g2.fillOval(sx + ts * 2 / 3, sy + ts / 3 + (int) ((t + 3) % 6), 4, 6);
		}
	}

	/** Paramètres lus dans le fichier de la map (texte d'un panneau, drapeau d'une porte...). */
	public void configure(String[] args) {}

	public void use(Entity entity) {
		
	}

	public void checkDrop() {
		
	}
}
