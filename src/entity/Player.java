package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyHandler;
import main.UtilityTool;
import object.OBJ_Arrow;
import object.OBJ_ArrowInv;
import object.OBJ_Boko_Bow;
import object.OBJ_HealPot;
import object.OBJ_Key;
import object.OBJ_Knight_Sword;
import object.OBJ_Weak_Shield;
import object.OBJ_Wood_Shield;
import object.OBJ_Tree_Branch;

public class Player extends Entity{
	
	private KeyHandler kH;
	
	private BufferedImage up[] = new BufferedImage[2];
	private BufferedImage down[] = new BufferedImage[2];
	private BufferedImage left[] = new BufferedImage[2];
	private BufferedImage right[] = new BufferedImage[2];
	
	private final int screenX;
	private final int screenY;
	private int newScreenX;
	private int newScreenY;
	
	private boolean attackCanceled = false;
	private ArrayList<Entity>inventory = new ArrayList<Entity>();
	private final int maxInventorySize = 40;
	private final int maxInventoryPage = maxInventorySize / 20;
	
	private final int maxAmount = 500;
	
	public Player(GamePanel gp, KeyHandler kH) {
		
		super(gp);
		
		type = 0;
		this.kH=kH;
		
		screenX = gp.getScreenWidth()/2 - (gp.getTileSize()/2);
		screenY = gp.getScreenHeight()/2 - (gp.getTileSize()/2);
		
		solidArea = new Rectangle();
		solidArea.x = gp.getTileSize()/4;
		solidArea.y = gp.getTileSize()/2;
		solidAreaDefaultX = solidArea.x;
		solidAreaDefaultY = solidArea.y;
		solidArea.width = gp.getTileSize()/2;
		solidArea.height = (gp.getTileSize()/2) - 2;
		
		attackArea.width = gp.getTileSize() - 5;
		attackArea.height = gp.getTileSize() - 5;
		
		attackUp = new BufferedImage[3];
		attackDown = new BufferedImage[3];
		attackLeft = new BufferedImage[3];
		attackRight = new BufferedImage[3];
		
		setDefaultValues();
		getPlayerImage();
		getPlayerAttackImage();
		setItems();
	}
	
	public void setDefaultPositions() {
		worldX = gp.getTileSize() * 2;
		worldY = gp.getTileSize() * 1; 
		direction = "Down";
	}
	
	public void restoreLife() {
		life = maxLife;
		invincible = false;
	}
	
	public void setDefaultValues() {
		
		worldX = gp.getTileSize() * 2;
		worldY = gp.getTileSize() * 1; 
		speed = 4;
		direction = "Down";
		
		//Stats Player
		maxLife = 12;
		life = maxLife;
		strength = 1;
		dexterity = 1;
		argent = 0;
		
		projectile = new OBJ_Arrow(gp);
		currentWeapon = new OBJ_Tree_Branch(gp);
		currentShield = new OBJ_Weak_Shield(gp);
		currentBow = new OBJ_Boko_Bow(gp);
		attack = getAttackV();
		defense = getDefenseV();
	}
	
	public void setItems() {
		
		inventory.clear();
		inventory.add(currentWeapon);
		inventory.add(currentShield);
		inventory.add(currentBow);
		inventory.add(new OBJ_ArrowInv(gp , 500));
		inventory.add(new OBJ_HealPot(gp));
	}
	
	private int getDefenseV() {
		return defense = dexterity * currentShield.getDefenseValue();
	}

	private int getAttackV() {
		return attack = strength * currentWeapon.getAttackValue();
	}

	public int getDefense() {
		return defense;
	}

	public int getAttack() {
		return attack;
	}
	
	public void getPlayerImage() {
		for(int i = 0 ; i < up.length ; i ++) {
			if(i<10) {
				up[i] = setup("/player/LinkUp"+i, gp.getTileSize(), gp.getTileSize());
				down[i] = setup("/player/LinkDown"+i, gp.getTileSize(), gp.getTileSize());
				left[i] = setup("/player/LinkLeft"+i, gp.getTileSize(), gp.getTileSize());
				right[i] = setup("/player/LinkRight"+i, gp.getTileSize(), gp.getTileSize());
			}
		}
	}
	
	public void getPlayerAttackImage() {
		for(int i = 0 ; i < attackUp.length ; i ++) {
			attackUp[i] = setup("/player/LinkAttackUp"+i, gp.getTileSize()*2, gp.getTileSize() * 2);
			attackDown[i] = setup("/player/LinkAttackDown"+i, gp.getTileSize()*2, gp.getTileSize() * 2);
			attackLeft[i] = setup("/player/LinkAttackLeft"+i, gp.getTileSize() * 2, gp.getTileSize()*2);
			attackRight[i] = setup("/player/LinkAttackRight"+i, gp.getTileSize() * 2, gp.getTileSize()*2);
		}
	}
	
	public void selectItem() {
		 
		int itemIndex = gp.ui.getItemIndexOnSlot(gp.ui.getInventoryIndex());
		
		if(itemIndex < inventory.size()) {
			
			Entity selectedItem = inventory.get(itemIndex);
			
			if(selectedItem.type == typeSword || selectedItem.type == typeBranch) {
				
				currentWeapon = selectedItem;
				attack = getAttackV();
			}
			if(selectedItem.type == typeWeakShield) {
				
				currentShield = selectedItem;
				defense = getDefenseV();
			}
			if(selectedItem.type == typeBokoBow) {
				currentBow = selectedItem;
			}
			if(selectedItem.type == typePotionSoin && life < maxLife) {
				life += selectedItem.value;
				if(life > maxLife) {
					life = maxLife;
				}
				inventory.remove(itemIndex);
			}
		}
	}
	
	public void update() {
	    
	    if (attacking) {
	        attacking();
	    } else if (kH.isUp() || kH.isDown() || kH.isLeft() || kH.isRight() || kH.isEnter()) {
	        if (kH.isUp()) direction = "Up";
	        if (kH.isDown()) direction = "Down";
	        if (kH.isLeft()) direction = "Left";
	        if (kH.isRight()) direction = "Right";

	        // Check tile collision
	        collisionOn = false;
	        gp.getCChecker().checkTile(this);

	        // Check object collision
	        int objIndex = gp.getCChecker().checkObject(this, true);
	        pickUpObject(objIndex);

	        // Check NPC collision
	        int npcIndex = gp.getCChecker().checkEntity(this, gp.getNPCTab());
	        interactNPC(npcIndex);

	        // Check Monster collision
	        int monIndex = gp.getCChecker().checkEntity(this, gp.getMonTab());
	        contactMonster(monIndex);

	        // Check Interactive Tiles
	        int iTileIndex = gp.getCChecker().checkEntity(this, gp.getItileTab());

	        // Check Event
	        gp.getEHandler().checkEvent();


	        // Mouvement si collisionOn est false et si les limites ne sont pas atteintes
	     // Si le scroll est bloqué, le joueur se déplace sur l'écran au lieu du monde
	        if (!collisionOn && !kH.isEnter()) {
	            if (kH.isUp()) {
                    worldY -= speed;
	            }
	            
	            if (kH.isDown()) {
                    worldY += speed;
	            }

	            if (kH.isLeft()) {
                    worldX -= speed;
	            }

	            if (kH.isRight()) {
                    worldX += speed;
	            }
	        }


	        if (gp.getkH().isEnter() && !attackCanceled) {
	            attacking = true;
	            spriteCounter = 0;
	        }

	        attackCanceled = false;
	        gp.getkH().setEnter(false);

	        // Animation du sprite
	        spriteCounter++;
	        if (spriteCounter > 10) {
	            spriteNum = (spriteNum == 1) ? 2 : 1; // Alterne entre 1 et 2
	            spriteCounter = 0;
	        }
	    }

	    // Gestion du tir à l'arc
	    if (gp.getkH().isShot() && !projectile.isAlive() && shotAvailableCounter == 30) {
	        if (currentBow != null) {
	            int arrowIndex = getArrowIndex(); // Récupère l'index des flèches dans l'inventaire

	            if (arrowIndex != -1 && inventory.get(arrowIndex).getAmount() > 0) { 
	                // Vérifie qu'il y a des flèches avant de tirer
	                projectile.set(worldX, worldY, direction, true, this);
	                gp.getProjList().add(projectile);

	                shotAvailableCounter = 0;

	                // Réduit le nombre de flèches
	                inventory.get(arrowIndex).setAmount(inventory.get(arrowIndex).getAmount() - 1);
	            }
	        }
	    }

	    // Gestion de l'invincibilité temporaire
	    if (invincible) {
	        invincibleCounter++;
	        if (invincibleCounter > 60) {
	            invincibleCounter = 0;
	            invincible = false;
	        }
	    }

	    // Recharge du tir
	    if (shotAvailableCounter < 30) {
	        shotAvailableCounter++;
	    }
	    if (life <= 0) {
	    	gp.setGameState(gp.getGameOverState());
	    }
	}

	private void attacking() {
	    if (spriteNum > attackUp.length) {
	        spriteNum = 1;
	    }
	    
	    spriteCounter++;
	    
	    if (spriteCounter <= 5) {
	        spriteNum = 1;
	    } else if (spriteCounter <= 10) {
	        spriteNum = 2;
	        executeAttack();
	    } else if (spriteCounter <= 15) {
	        spriteNum = 3;
	        executeAttack();
	        
	        int iTileIndex = gp.getCChecker().checkEntity(this, gp.getItileTab());
	        damageInteractiveTile(iTileIndex);
	    } else {
	        spriteNum = 1;
	        spriteCounter = 0;
	        attacking = false;
	    }
	}

	private void executeAttack() {
	    // Sauvegarde des valeurs de position et de la zone solide
	    int currentWorldX = worldX;
	    int currentWorldY = worldY;
	    int solidAreaWidth = solidArea.width;
	    int solidAreaHeight = solidArea.height;
	    
	    // Ajustement de la position pour l'attaque
	    switch (direction) {
	        case "Up": worldY -= attackArea.height; break;
	        case "Down": worldY += attackArea.height; break;
	        case "Left": worldX -= attackArea.width; break;
	        case "Right": worldX += attackArea.width; break;
	    }
	    
	    // Mise à jour de la zone de collision pour l'attaque
	    solidArea.width = attackArea.width;
	    solidArea.height = attackArea.height;
	    
	    // Vérification des collisions avec les monstres
	    int monsterIndex = gp.getCChecker().checkEntity(this, gp.getMonTab());
	    damageMonster(monsterIndex, attack);
	    
	    int iTileIndex = gp.getCChecker().checkEntity(this, gp.getItileTab());
        damageInteractiveTile(iTileIndex);
	    
	    // Réinitialisation des valeurs d'origine
	    worldX = currentWorldX;
	    worldY = currentWorldY;
	    solidArea.width = solidAreaWidth;
	    solidArea.height = solidAreaHeight;
	}

	private void damageInteractiveTile(int i) {
		
		if (i != 999 && gp.getItile(i).isDestructible()) {
			
			gp.getItile(i).checkDrop();
			gp.setItile(gp.getCurrentMap(), null, i);
			
		}
	}

	public void damageMonster(int i, int attack) {
		
		if(i != 999) {
			
			if(!(gp.getMon(i).invincible)) {
				
				int damage = attack - gp.getMon(i).getDefense();
				
				if(damage < 0) 
					damage = 0;
				
				
				gp.getMon(i).life -= damage;
				
				gp.getMon(i).invincible = true;
				gp.getMon(i).damageReaction();
				
				if(gp.getMon(i).life <= 0) {
					gp.getMon(i).dying = true;
				}
			}
		}
	}

	private void contactMonster(int i) {
		
		if(i != 999) {
			if(!invincible) {
				
				int damage = gp.getMon(i).getAttack() - defense;
				
				if(damage < 0) 
					damage = 0;
				
				
				life -= damage;
				invincible = true; 
			}
		}
	}

	private void interactNPC(int i) {
		
		if(gp.getkH().isEnter()) {
			if(i != 999) {
				attackCanceled = true;
				gp.setGameState(gp.getDialogueState());
				gp.getNPC(i).speak();
			}
		}
	}

	public void pickUpObject(int i) {
		
		if(i != 999) {
			
			//PickUp Only
			
			if(gp.getObj(i).getType() == typePickUpOnly) {
				
				gp.getObj(i).use(this);
				gp.setObj(gp.getCurrentMap(), null, i);
			}
			//Inventory Items
			else if (gp.getObj(i).getType() != typeChest){
				
				if(gp.getObj(i).getName() == "Flèche") {
					if((inventory.get(getArrowIndex()).getAmount()) < inventory.get(getArrowIndex()).getMaxAmount()) {
						inventory.get(getArrowIndex()).setAmount(inventory.get(getArrowIndex()).getAmount() +1);
						gp.setObj(gp.getCurrentMap(),null,i);
					}
					
				} else if(inventory.size() < maxInventorySize) {
					inventory.add(gp.getObj(i));
					gp.setObj(gp.getCurrentMap(),null,i);
				}
			}
		}
	}
	
	public void draw(Graphics2D g2) {
		
		BufferedImage image = null;
		int tempScreenX = screenX;
		int tempScreenY = screenY;
		
	    switch(direction) {
	    case "Up":
	        if(!attacking) {
	            image = up[spriteNum - 1];
	        } else {
	            if(spriteNum > attackUp.length) {
	            	spriteNum = 1;
	            	tempScreenY = screenY - gp.getTileSize();
	            } else {
	            	tempScreenY = screenY - gp.getTileSize();
	            	image = attackUp[spriteNum - 1];
	            }
	        }
	        break;
        case "Down":
        	if(!attacking) 
        		image = down[spriteNum - 1];
        	else {
	            if(spriteNum > attackDown.length) {
	            	spriteNum = 1;
	            	tempScreenX = screenX - gp.getTileSize();
	            } else {
	            	tempScreenX = screenX - gp.getTileSize();
	            	image = attackDown[spriteNum - 1];
	            }
	            
        	}
            break;
        case "Left":
            if(!attacking) {
                image = left[spriteNum - 1];
            } else {
	            if(spriteNum > attackLeft.length) {
	            	spriteNum = 1;
	            	tempScreenX = screenX - gp.getTileSize();
	            	tempScreenY = screenY - gp.getTileSize();
	            } else {
	            	tempScreenX = screenX - gp.getTileSize();
	            	tempScreenY = screenY - gp.getTileSize();
	            	image = attackLeft[spriteNum - 1];
	            }
            }
            break;
        case "Right":
        	if(!attacking) 
        		image = right[spriteNum - 1];
        	else {
	            if(spriteNum > attackRight.length) {
	            	tempScreenY = screenY - gp.getTileSize();
	            	spriteNum = 1;
	            } else 
	            	tempScreenY = screenY - gp.getTileSize();
		            image = attackRight[spriteNum - 1];
	            
        	}
            break;
	    }
	
	    if(invincible) {
	    	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
	    }
	g2.drawImage(image, tempScreenX, tempScreenY, null);
	
	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}

	public BufferedImage getImage() {
	    BufferedImage image = null;
	    switch(direction) {
	    case "Up":
			image = up[spriteNum - 1];
            break;
        case "Down":
        	image = down[spriteNum - 1];
            break;
        case "Left":
        	image = left[spriteNum - 1];
            break;
        case "Right":
        	image = right[spriteNum - 1];
            break;
	    }
	    return image;
	}
	
	public BufferedImage getImage(String direction, int i) {
	    BufferedImage image = null;
	    switch(direction) {
	    case "Up":
			image = up[i];
            break;
        case "Down":
        	image = down[i];
            break;
        case "Left":
        	image = left[i];
            break;
        case "Right":
        	image = right[i];
            break;
	    }
	    return image;
	}
	
	public int getScreenX() {
		return screenX;
	}

	public int getScreenY() {
		return screenY;
	}
	
	public boolean isInvincible() {
		return invincible;
	}
	
	public int getMaxInventoryPage() {
		return maxInventoryPage;
	}
	
	public void setInvincible(boolean b) {
		invincible = b;
	}

	public void setAttackCanceled(boolean b) {
		attackCanceled = b;
	}
	
	public ArrayList<Entity> getInventory() {
		return inventory;
	}
	
	public int getMaxInventorySize() {
		return maxInventorySize;
	}
	
	public Entity getCurrentBow() {
		return currentBow;
	}
	
	public int getArrowIndex() {
	    for (int i = 0; i < inventory.size(); i++) {
	        if (inventory.get(i) instanceof OBJ_ArrowInv) {
	            return i;  // On renvoie directement l'index dès qu'on trouve une flèche
	        }
	    }
	    return -1;  // Si aucune flèche trouvée
	}
}
