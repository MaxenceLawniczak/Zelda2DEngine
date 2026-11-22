package entity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import main.GamePanel;
import main.KeyHandler;
import main.MapData;
import object.OBJ_Arrow;
import object.OBJ_ArrowInv;
import object.OBJ_Chest;
import object.OBJ_Door;
import object.OBJ_Sign;
import object.OBJ_Weak_Shield;
import object.OBJ_Tree_Branch;

public class Player extends Entity{

	private KeyHandler kH;

	private BufferedImage up[] = new BufferedImage[2];
	private BufferedImage down[] = new BufferedImage[2];
	private BufferedImage left[] = new BufferedImage[2];
	private BufferedImage right[] = new BufferedImage[2];

	private final int screenX;
	private final int screenY;

	private boolean attackCanceled = false;
	private ArrayList<Entity>inventory = new ArrayList<Entity>();
	private final int maxInventorySize = 40;
	private final int maxInventoryPage = maxInventorySize / 20;

	// Zone d'attaque à mains nues (si l'arme n'en définit pas)
	private final Rectangle defaultAttackArea;

	// Bouclier (touche Espace)
	private boolean guarding = false;
	private int blockEffectCounter = 0; // Petit flash quand une attaque est bloquée
	private int guardFrames = 0;        // Depuis combien de frames le bouclier est levé (parade parfaite)
	private boolean riposteReady = false; // Bouclier de la Garde Royale : prochain coup x2
	private boolean swingWorn = false;  // L'arme s'est déjà usée pendant ce coup
	private Projectile beam = null;     // Rayon de la Lame sacrée
	private int keys = 0;               // Petites clés
	private int pushCounter = 0;        // Temps passé à pousser un rocher

	public Player(GamePanel gp, KeyHandler kH) {

		super(gp);

		type = typePlayer;
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

		defaultAttackArea = new Rectangle(0, 0, gp.getTileSize() - 5, gp.getTileSize() - 5);

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
		// Point d'apparition défini dans res/maps/map01_data.txt (spawn), sinon (2, 1)
		MapData data = gp.getASetter() != null ? gp.getASetter().getMapData(0) : null;
		int col = data != null && data.hasSpawn() ? data.spawnCol : 2;
		int row = data != null && data.hasSpawn() ? data.spawnRow : 1;
		worldX = gp.getTileSize() * col;
		worldY = gp.getTileSize() * row;
		direction = "Down";
	}

	public void restoreLife() {
		life = maxLife;
		invincible = false;
	}

	public void setDefaultValues() {

		setDefaultPositions();
		speed = 4;

		//Stats Player
		maxLife = 12;
		life = maxLife;
		strength = 1;
		dexterity = 1;
		argent = 0;

		projectile = new OBJ_Arrow(gp);
		currentWeapon = new OBJ_Tree_Branch(gp);
		currentShield = new OBJ_Weak_Shield(gp);
		currentBow = null; // L'arc se trouve plus tard dans l'aventure
		applyEquipment();
	}

	public void setItems() {

		inventory.clear();
		keys = 0;
		inventory.add(currentWeapon);
		inventory.add(currentShield);

	}

	/** Recalcule l'attaque, la défense et la portée selon l'équipement. */
	private void applyEquipment() {
		attack = currentWeapon != null ? strength * currentWeapon.getAttackValue() : strength;
		defense = currentShield != null ? dexterity * currentShield.getDefenseValue() : 0;

		Rectangle weaponArea = currentWeapon != null ? currentWeapon.getAttackArea() : null;
		Rectangle area = weaponArea != null && weaponArea.width > 0 ? weaponArea : defaultAttackArea;
		attackArea.width = area.width;
		attackArea.height = area.height;
	}

	public int getDefense() {
		return defense;
	}

	public int getAttack() {
		return attack;
	}

	public void getPlayerImage() {
		for(int i = 0 ; i < up.length ; i ++) {
			up[i] = setup("/player/walk/LinkUp"+i, gp.getTileSize(), gp.getTileSize());
			down[i] = setup("/player/walk/LinkDown"+i, gp.getTileSize(), gp.getTileSize());
			left[i] = setup("/player/walk/LinkLeft"+i, gp.getTileSize(), gp.getTileSize());
			right[i] = setup("/player/walk/LinkRight"+i, gp.getTileSize(), gp.getTileSize());
		}
	}

	public void getPlayerAttackImage() {
		for(int i = 0 ; i < attackUp.length ; i ++) {
			attackUp[i] = setup("/player/attack/LinkAttackUp"+i, gp.getTileSize()*2, gp.getTileSize() * 2);
			attackDown[i] = setup("/player/attack/LinkAttackDown"+i, gp.getTileSize()*2, gp.getTileSize() * 2);
			attackLeft[i] = setup("/player/attack/LinkAttackLeft"+i, gp.getTileSize() * 2, gp.getTileSize()*2);
			attackRight[i] = setup("/player/attack/LinkAttackRight"+i, gp.getTileSize() * 2, gp.getTileSize()*2);
		}
	}

	public void selectItem() {

		int itemIndex = gp.ui.getItemIndexOnSlot(gp.ui.getInventoryIndex());

		if(itemIndex < inventory.size()) {

			Entity selectedItem = inventory.get(itemIndex);

			if(selectedItem.type == typeSword || selectedItem.type == typeBranch) {
				currentWeapon = selectedItem;
			}
			if(selectedItem.type == typeShield) {
				currentShield = selectedItem;
			}
			if(selectedItem.type == typeBow) {
				currentBow = selectedItem;
			}
			if(selectedItem.type == typePotionSoin && life < maxLife) {
				life += selectedItem.value;
				if(life > maxLife) {
					life = maxLife;
				}
				inventory.remove(itemIndex);
			}
			applyEquipment();
		}
	}

	/**
	 * Ajoute un objet à l'inventaire (coffres, mode debug...).
	 * Les flèches s'ajoutent au stock existant. Renvoie false si l'inventaire est plein.
	 */
	public boolean giveItem(Entity item) {
		if (item == null) return false;

		if (item.type == typeArrow) {
			int arrowIndex = getArrowIndex();
			if (arrowIndex != -1) {
				Entity arrows = inventory.get(arrowIndex);
				arrows.setAmount(Math.min(arrows.getMaxAmount(), arrows.getAmount() + Math.max(1, item.getAmount())));
				return true;
			}
		}
		if (inventory.size() >= maxInventorySize) {
			return false;
		}
		inventory.add(item);
		// Premier objet de ce type : il est équipé tout de suite
		if (item.isWeapon() && currentWeapon == null) currentWeapon = item;
		if (item.isShield() && currentShield == null) currentShield = item;
		if (item.isBow() && currentBow == null) currentBow = item;
		applyEquipment();
		return true;
	}

	public void update() {

		boolean fly = gp.getDebug().isFlyMode();

		// Bouclier levé : on ne bouge plus et on n'attaque plus, mais on peut se tourner
		guarding = kH.isGuard() && currentShield != null && !attacking;
		guardFrames = guarding ? guardFrames + 1 : 0;
		if (guarding) currentShield.whileGuarding(this, guardFrames);
if (blockEffectCounter > 0) blockEffectCounter--;

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

	        // Check object collision (ramassage et coffres)
	        int objIndex = gp.getCChecker().checkObject(this, true);
	        pickUpObject(objIndex);

	        // Check NPC collision
	        int npcIndex = gp.getCChecker().checkEntity(this, gp.getNPCTab());
	        interactNPC(npcIndex);

	        // Check Monster collision
	        int monIndex = gp.getCChecker().checkEntity(this, gp.getMonTab());
	        contactMonster(monIndex);

	        // Check Interactive Tiles (bloque le joueur, sauf l'herbe et les dalles)
	        int iTileIndex = gp.getCChecker().checkEntity(this, gp.getItileTab());
	        pushRock(iTileIndex);

	        // Check Event
	        gp.getEHandler().checkEvent();

	        // Fly mode (debug) : on traverse tout
	        if (fly) {
	        	collisionOn = false;
	        }

	        // Mouvement si aucune collision
	        // Bouclier levé : on avance deux fois moins vite (sauf bouclier léger)
	        boolean slowed = guarding && !currentShield.canMoveWhileGuarding();
	        if (!collisionOn && !kH.isEnter()) {
	        	int moveSpeed = fly ? speed * 2 : slowed ? speed / 2 : speed;
	            if (kH.isUp())    worldY -= moveSpeed;
	            if (kH.isDown())  worldY += moveSpeed;
	            if (kH.isLeft())  worldX -= moveSpeed;
	            if (kH.isRight()) worldX += moveSpeed;

	            if (fly) clampToWorld();
	        }

	        if (gp.getkH().isEnter() && !attackCanceled && !guarding) {
	            attacking = true;
	            swingWorn = false;
	            if (currentWeapon != null) currentWeapon.onAttack(this);
spriteCounter = 0;
	        }

	        attackCanceled = false;
	        gp.getkH().setEnter(false);

	        // Animation de marche (ralentie quand on avance moins vite avec le bouclier)
	        spriteCounter++;
	        if (spriteCounter > (slowed ? 20 : 10)) {
	            spriteNum = (spriteNum == 1) ? 2 : 1; // Alterne entre 1 et 2
	            spriteCounter = 0;
	        }
	    }

	    // Gestion du tir à l'arc
	    if (gp.getkH().isShot() && !guarding && !attacking && !projectile.isAlive() && shotAvailableCounter == 30) {
	        if (currentBow != null) {
	            int arrowIndex = getArrowIndex(); // Récupère l'index des flèches dans l'inventaire

	            if (arrowIndex != -1 && inventory.get(arrowIndex).getAmount() > 0) {
	                // Les dégâts de la flèche dépendent de l'arc équipé
	                if (currentBow.getAttackValue() > 0) {
	                	projectile.attack = currentBow.getAttackValue();
	                }
	                projectile.set(worldX, worldY, direction, true, this);
	                projectile.setPiercing(currentBow.piercingArrows());
	                gp.getProjList().add(projectile);

	                shotAvailableCounter = 0;

	                // Réduit le nombre de flèches (sauf si l'arc en fait économiser une)
	                if (Math.random() >= currentBow.arrowSaveChance()) {
	                	inventory.get(arrowIndex).setAmount(inventory.get(arrowIndex).getAmount() - 1);
	                } else {
	                	gp.addFloatingText("Flèche récupérée", worldX + gp.getTileSize() / 2, worldY, new Color(200, 255, 200));
	                }
	                Entity bow = currentBow;
	                if (bow.wear(1)) breakItem(bow); else warnIfLow(bow);
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
	    	life = 0;
	    	kH.releaseAll(); // Évite que le joueur reparte tout seul après un "Retry"
	    	gp.ui.setCommandNum(0);
	    	gp.setGameState(gp.getGameOverState());
	    }
	}

	private void clampToWorld() {
		int maxX = (gp.getMaxWorldCols() - 1) * gp.getTileSize();
		int maxY = (gp.getMaxWorldRows() - 1) * gp.getTileSize();
		worldX = Math.max(0, Math.min(maxX, worldX));
		worldY = Math.max(0, Math.min(maxY, worldY));
	}

	private void attacking() {
		// La vitesse de l'attaque dépend de l'arme équipée
		int m1 = currentWeapon != null ? currentWeapon.getMotion1Duration() : 5;
		int m2 = currentWeapon != null ? currentWeapon.getMotion2Duration() : 10;
		int m3 = currentWeapon != null ? currentWeapon.getMotion3Duration() : 15;

	    spriteCounter++;

	    if (spriteCounter <= m1) {
	        spriteNum = 1;
	    } else if (spriteCounter <= m2) {
	        spriteNum = 2;
	        executeAttack();
	    } else if (spriteCounter <= m3) {
	        spriteNum = 3;
	        executeAttack();
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
	    // Tous les monstres dans la zone de l'arme sont touchés
	    Rectangle hitbox = getAttackHitboxAt(currentWorldX, currentWorldY);
	    for (Entity mon : gp.getMonTab()) {
	    	if (mon == null) continue;
	    	Rectangle box = new Rectangle(mon.worldX + mon.solidAreaDefaultX, mon.worldY + mon.solidAreaDefaultY,
	    			mon.solidArea.width, mon.solidArea.height);
	    	if (hitbox.intersects(box)) meleeHit(mon);
	    }

	    int iTileIndex = gp.getCChecker().checkEntity(this, gp.getItileTab());
        damageInteractiveTile(iTileIndex);

	    // Réinitialisation des valeurs d'origine
	    worldX = currentWorldX;
	    worldY = currentWorldY;
	    solidArea.width = solidAreaWidth;
	    solidArea.height = solidAreaHeight;
	}

	/** Zone d'attaque actuelle en coordonnées monde (pour l'affichage des hitbox en debug). */
	public Rectangle getAttackHitbox() {
		int x = worldX + solidAreaDefaultX;
		int y = worldY + solidAreaDefaultY;
		switch (direction) {
	        case "Up": y -= attackArea.height; break;
	        case "Down": y += attackArea.height; break;
	        case "Left": x -= attackArea.width; break;
	        case "Right": x += attackArea.width; break;
	    }
		return new Rectangle(x, y, attackArea.width, attackArea.height);
	}

	private void damageInteractiveTile(int i) {

		if (i == 999 || gp.getItile(i) == null) return;
		tile_interactive.InteractiveTile tile = gp.getItile(i);
		tile.onStrike(this); // Torches...
		// Certaines armes brisent ce que les autres ne peuvent pas (épée lourde : gros rochers)
		boolean breakable = currentWeapon != null ? currentWeapon.canBreakTile(tile) : tile.isDestructible();
		if (breakable) {
			tile.checkDrop();
			if (currentWeapon != null) currentWeapon.onTileCut(this, tile);
			gp.setItile(gp.getCurrentMap(), null, i);
		}
	}

	/** Zone de l'arme si le joueur était à cette position (le coup décale temporairement worldX/Y). */
	private Rectangle getAttackHitboxAt(int x, int y) {
		int savedX = worldX, savedY = worldY;
		worldX = x;
		worldY = y;
		Rectangle r = getAttackHitbox();
		worldX = savedX;
		worldY = savedY;
		return r;
	}

	/** Coup d'épée sur un monstre : dégâts, spécificité de l'arme, usure. */
	private void meleeHit(Entity mon) {
		if (mon.invincible || mon.dying || !mon.alive) return;
		int damage = Math.max(0, attack - mon.getDefense());
		if (riposteReady) {
			damage *= 2;
			riposteReady = false;
			gp.addFloatingText("RIPOSTE !", mon.worldX + gp.getTileSize() / 2, mon.worldY - gp.getTileSize() / 3, new Color(255, 215, 80));
		}
		if (currentWeapon != null) damage = currentWeapon.modifyDamage(this, mon, damage);

		mon.life -= damage;
		mon.invincible = true;
		mon.hpBarOn = true;
		mon.damageReaction();
		gp.addFloatingText("-" + damage, mon.worldX + gp.getTileSize() / 2, mon.worldY, Color.WHITE);
		boolean killed = mon.life <= 0;
		if (killed) mon.dying = true;

		if (currentWeapon != null) {
			Entity weapon = currentWeapon;
			weapon.onHit(this, mon, killed);
			// L'arme ne s'use qu'une fois par coup, même si elle touche plusieurs monstres
			if (!swingWorn) {
				swingWorn = true;
				if (weapon.wear(1)) breakItem(weapon); else warnIfLow(weapon);
			}
		}
	}

	/** Un objet est cassé : il disparaît et on équipe le meilleur objet du même type qui reste. */
	public void breakItem(Entity item) {
		item.onBreak(this);
		inventory.remove(item);
		gp.ui.showNotification("Objet brisé : " + item.getName() + " !");
		gp.addFloatingText("Brisé !", worldX + gp.getTileSize() / 2, worldY - gp.getTileSize() / 3, new Color(255, 90, 90));
		if (item == currentWeapon) currentWeapon = best(item);
		if (item == currentShield) currentShield = best(item);
		if (item == currentBow) currentBow = best(item);
		applyEquipment();
	}

	/** Meilleur objet de l'inventaire du même type que "broken" (ou null). */
	private Entity best(Entity broken) {
		Entity best = null;
		for (Entity e : inventory) {
			boolean sameType = (broken.isWeapon() && e.isWeapon()) || (broken.isShield() && e.isShield()) || (broken.isBow() && e.isBow());
			if (!sameType) continue;
			int value = broken.isShield() ? e.getDefenseValue() : e.getAttackValue();
			int bestValue = best == null ? -1 : (broken.isShield() ? best.getDefenseValue() : best.getAttackValue());
			if (value > bestValue) best = e;
		}
		return best;
	}

	/** Prévient quand un objet est presque cassé. */
	private void warnIfLow(Entity item) {
		if (!item.isUnbreakable() && item.getDurability() == 3 && item.getMaxDurability() > 3) {
			gp.ui.showNotification(item.getName() + " va bientôt se briser !");
		}
	}

	/** Lame sacrée : lance un rayon (un seul à la fois). */
	public void fireBeam() {
		if (beam == null) beam = new object.OBJ_SwordBeam(gp);
		if (beam.isAlive()) return;
		beam.attack = Math.max(1, attack);
		beam.set(worldX, worldY, direction, true, this);
		gp.getProjList().add(beam);
	}

	/** Pousse le rocher contre lequel on marche (il faut rester appuyé un court instant). */
	private void pushRock(int i) {
		boolean moving = kH.isUp() || kH.isDown() || kH.isLeft() || kH.isRight();
		if (i != 999 && moving && gp.getItile(i) instanceof tile_interactive.IT_PushRock) {
			if (++pushCounter >= 15) {
				((tile_interactive.IT_PushRock) gp.getItile(i)).push(direction);
				pushCounter = 0;
			}
		} else {
			pushCounter = 0;
		}
	}

	public void addKey() {
		keys++;
	}

	/** Utilise une clé si on en a une. */
	public boolean useKey() {
		if (keys <= 0) return false;
		keys--;
		return true;
	}

	public int getKeys() {
		return keys;
	}

	public void setRiposteReady(boolean ready) {
		riposteReady = ready;
	}

	public int getGuardFrames() {
		return guardFrames;
	}

	public void damageMonster(int i, int attack) {

		if(i != 999 && gp.getMon(i) != null) {

			if(!gp.getMon(i).invincible && !gp.getMon(i).dying) {

				int damage = attack - gp.getMon(i).getDefense();

				if(damage < 0)
					damage = 0;


				gp.getMon(i).life -= damage;
				gp.getMon(i).hpBarOn = true;
				gp.addFloatingText("-" + damage, gp.getMon(i).worldX + gp.getTileSize() / 2, gp.getMon(i).worldY, Color.WHITE);

				gp.getMon(i).invincible = true;
				gp.getMon(i).damageReaction();

				if(gp.getMon(i).life <= 0) {
					gp.getMon(i).dying = true;
				}
			}
		}
	}

	private void contactMonster(int i) {
		if(i != 999 && gp.getMon(i) != null && !gp.getMon(i).dying) {
			receiveDamage(gp.getMon(i).getAttack(), gp.getMon(i));
		}
	}

	/**
	 * Point d'entrée unique pour les dégâts reçus par le joueur.
	 * Gère le god mode, l'invincibilité, le bouclier et la défense.
	 * Renvoie true si le joueur a vraiment perdu des PV.
	 */
	public boolean receiveDamage(int attackValue, Entity attacker) {
		if (gp.getDebug().isGodMode() || invincible) {
			return false;
		}

		// Bouclier levé face à l'attaque : aucun dégât
		if (attacker != null && isBlocking(attacker)) {
			blockEffectCounter = 10;
			Entity shield = currentShield;
			boolean perfect = guardFrames <= 15; // Bouclier levé au dernier moment
			shield.onBlock(this, attacker, perfect);
			if (!(attacker instanceof Projectile)) attacker.damageReaction(); // Le monstre repart dans l'autre sens
			if (shield.wear(1)) breakItem(shield); else warnIfLow(shield);
			return false;
		}

		// Au moins 1 dégât dès qu'une attaque a de la force, même avec une grosse défense
		int damage = attackValue > 0 ? Math.max(1, attackValue - defense) : 0;
		life -= damage;
		invincible = true;
		return damage > 0;
	}

	/** Le bouclier est-il levé du côté d'où vient l'attaque ? */
	public boolean isBlocking(Entity attacker) {
		if (!guarding) return false;

		String from;
		if (attacker instanceof Projectile) {
			// Un projectile qui va vers le bas arrive par le haut, etc.
			from = opposite(attacker.direction);
		} else {
			int dx = (attacker.worldX + attacker.solidArea.x + attacker.solidArea.width / 2)
					- (worldX + solidArea.x + solidArea.width / 2);
			int dy = (attacker.worldY + attacker.solidArea.y + attacker.solidArea.height / 2)
					- (worldY + solidArea.y + solidArea.height / 2);
			if (Math.abs(dx) > Math.abs(dy)) {
				from = dx < 0 ? "Left" : "Right";
			} else {
				from = dy < 0 ? "Up" : "Down";
			}
		}
		String side = from.equals(direction) ? "front" : from.equals(opposite(direction)) ? "back" : "side";
		return currentShield.blocksFrom(side);
	}

	private static String opposite(String dir) {
		switch (dir) {
		case "Up": return "Down";
		case "Down": return "Up";
		case "Left": return "Right";
		default: return "Left";
		}
	}

	private void interactNPC(int i) {

		if(gp.getkH().isEnter()) {
			// On peut parler un peu à distance (une demi-case devant soi), pas seulement collé au PNJ
			if (i == 999) {
				int savedSpeed = speed;
				speed = gp.getTileSize() / 2 + 4;
				i = gp.getCChecker().checkEntity(this, gp.getNPCTab());
				speed = savedSpeed;
			}
			if(i != 999) {
				attackCanceled = true;
				gp.setGameState(gp.getDialogueState());
				gp.getNPC(i).speak();
			}
		}
	}

	public void pickUpObject(int i) {

		if(i == 999 || gp.getObj(i) == null) {
			return;
		}
		Entity obj = gp.getObj(i);

		//PickUp Only
		if(obj.getType() == typePickUpOnly) {
			obj.use(this);
			gp.setObj(gp.getCurrentMap(), null, i);
		}
		//Porte fermée à clé : s'ouvre en marchant dedans avec une clé
		else if (obj instanceof OBJ_Door) {
			if (((OBJ_Door) obj).tryOpen(this, kH.isEnter())) {
				gp.setObj(gp.getCurrentMap(), null, i);
			}
			if (kH.isEnter()) attackCanceled = true;
		}
		//Panneau : Entrée pour le lire
		else if (obj instanceof OBJ_Sign) {
			if (kH.isEnter()) {
				attackCanceled = true;
				((OBJ_Sign) obj).read();
			}
		}
		//Coffre : on l'ouvre avec Entrée quand on est collé devant
		else if (obj.getType() == typeChest) {
			if (kH.isEnter() && obj instanceof OBJ_Chest) {
				attackCanceled = true;
				((OBJ_Chest) obj).open(this);
			}
		}
		//Inventory Items
		else if (giveItem(obj)) {
			gp.setObj(gp.getCurrentMap(), null, i);
		}
	}

	public void draw(Graphics2D g2) {

		BufferedImage image = null;
		// Position à l'écran (le joueur n'est pas au centre si la caméra suit quelqu'un d'autre en debug)
		int baseX = gp.worldToScreenX(worldX);
		int baseY = gp.worldToScreenY(worldY);
		int tempScreenX = baseX;
		int tempScreenY = baseY;

	    switch(direction) {
	    case "Up":
	        if(!attacking) {
	            image = up[spriteNum - 1];
	        } else {
	            tempScreenY = baseY - gp.getTileSize();
	            image = attackUp[Math.min(spriteNum, attackUp.length) - 1];
	        }
	        break;
        case "Down":
        	if(!attacking) {
        		image = down[spriteNum - 1];
        	} else {
	            tempScreenX = baseX - gp.getTileSize();
	            image = attackDown[Math.min(spriteNum, attackDown.length) - 1];
        	}
            break;
        case "Left":
            if(!attacking) {
                image = left[spriteNum - 1];
            } else {
            	tempScreenX = baseX - gp.getTileSize();
            	tempScreenY = baseY - gp.getTileSize();
            	image = attackLeft[Math.min(spriteNum, attackLeft.length) - 1];
            }
            break;
        case "Right":
        	if(!attacking) {
        		image = right[spriteNum - 1];
        	} else {
            	tempScreenY = baseY - gp.getTileSize();
            	image = attackRight[Math.min(spriteNum, attackRight.length) - 1];
        	}
            break;
	    }

	    if(invincible) {
	    	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
	    }

	    // Face au nord, le bouclier passe derrière le joueur
	    if (guarding && direction.equals("Up")) drawShield(g2, baseX, baseY);

	    g2.drawImage(image, tempScreenX, tempScreenY, null);

	    if (guarding && !direction.equals("Up")) drawShield(g2, baseX, baseY);

	    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}

	private void drawShield(Graphics2D g2, int x, int y) {
		BufferedImage shield = currentShield.getDown1();
		int size = (gp.getTileSize() * 2) / 3;
		int sx, sy;
		switch (direction) {
		case "Up":    sx = x + (gp.getTileSize() - size) / 2; sy = y - size / 2; break; // Dépasse au-dessus de la tête
		case "Down":  sx = x + (gp.getTileSize() - size) / 2; sy = y + gp.getTileSize() / 2 - 2; break;
		case "Left":  sx = x - size / 3; sy = y + gp.getTileSize() / 4; break;
		default:      sx = x + gp.getTileSize() - (2 * size) / 3; sy = y + gp.getTileSize() / 4; break;
		}
		g2.drawImage(shield, sx, sy, size, size, null);

		// Flash blanc quand une attaque est bloquée
		if (blockEffectCounter > 0) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blockEffectCounter / 10f));
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(2));
			int grow = 10 - blockEffectCounter;
			g2.drawOval(sx - grow, sy - grow, size + grow * 2, size + grow * 2);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
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

	@Override
	public List<String> getDebugInfo() {
		List<String> info = super.getDebugInfo();
		info.add("Arme : " + (currentWeapon != null ? currentWeapon.getName() : "-")
				+ "   Bouclier : " + (currentShield != null ? currentShield.getName() : "-"));
		if (guarding) info.add("Bouclier levé");
		return info;
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

	public boolean isGuarding() {
		return guarding;
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
