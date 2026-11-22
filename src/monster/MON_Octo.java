package monster;

import java.awt.image.BufferedImage;
import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.OBJ_Heart;
import object.OBJ_Rock;
import object.OBJ_RubisGreen;

public class MON_Octo extends Entity {

	private final Random rand = new Random();
	private final int variant;      // 0 = normal, 1 = bleu (robuste), 2 = doré (rapide)
	private int shootThreshold = 99; // Chance de tirer à chaque frame (plus petit = plus souvent)

	public static final int NORMAL = 0, BLUE = 1, GOLD = 2;

    public MON_Octo(GamePanel gp) {
    	this(gp, NORMAL);
    }

    public MON_Octo(GamePanel gp, int variant) {
        super(gp);
        this.variant = variant;

        type = typeMonster;
        name = "Octorock";
        speed = 1;
        maxLife = 3;
        attack = 2;
        defense = 0;
        if (variant == BLUE) {
        	name = "Octorock bleu";
        	maxLife = 6;
        	attack = 3;
        	defense = 1;
        	shootThreshold = 97;
        } else if (variant == GOLD) {
        	name = "Octorock doré";
        	speed = 2;
        	maxLife = 5;
        	attack = 3;
        	shootThreshold = 98;
        }
        life = maxLife;

        projectile = new OBJ_Rock(gp);

        deathAnimation = new BufferedImage[4];

        // Hitbox centrée sur le sprite (avant, elle était décalée d'un quart de case hors du sprite)
        solidArea.x = 3;
        solidArea.y = 3;
        solidArea.width = gp.getTileSize() - 6;
        solidArea.height = gp.getTileSize() - 6;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage() {
        up1 = img("/monster/octo/OctoUp0");
        up2 = img("/monster/octo/OctoUp1");
        down1 = img("/monster/octo/OctoDown0");
        down2 = img("/monster/octo/OctoDown1");
        left1 = img("/monster/octo/OctoLeft0");
        left2 = img("/monster/octo/OctoLeft1");
        right1 = img("/monster/octo/OctoRight0");
        right2 = img("/monster/octo/OctoRight1");
    }

    /** Image de l'Octorok, recolorée selon la variante. */
    private BufferedImage img(String path) {
    	BufferedImage base = setup(path, gp.getTileSize(), gp.getTileSize());
    	if (variant == BLUE) return main.Sprites.hueShift(base, 0.6f);
    	if (variant == GOLD) return main.Sprites.hueShift(base, 0.13f);
    	return base;
    }

    @Override
    public void setAction() {
        // Si le monstre est en train de mourir, on ne met pas à jour ses actions
        if (dying) {
            return;
        }

        actionLockCounter++;

        if (actionLockCounter == 120) {
            int i = rand.nextInt(100) + 1;

            if (i <= 25) {
                direction = "Up";
            } else if (i <= 50) {
                direction = "Down";
            } else if (i <= 75) {
                direction = "Left";
            } else {
                direction = "Right";
            }
            actionLockCounter = 0;
        }

        // 1% de chance par frame de tirer un caillou
        int i = rand.nextInt(100) + 1;

        if(i > shootThreshold && !projectile.isAlive() && shotAvailableCounter == 30) {
        	projectile.set(worldX, worldY, direction, true, this);
        	gp.getProjList().add(projectile);
        	shotAvailableCounter = 0;
        }

        if (shotAvailableCounter < 30) {
            shotAvailableCounter++;
        }
    }

    @Override
    public void update() {
        if (dying || !alive) {
            return;
        }
        
        // Étourdi ou repoussé (armes, boucliers) : il ne fait rien d'autre
        if (updateStatus()) {
            return;
        }

        setAction();

        collisionOn = false;
        gp.getCChecker().checkTile(this);
        gp.getCChecker().checkObject(this, false);
        gp.getCChecker().checkEntity(this, gp.getNPCTab());
        gp.getCChecker().checkEntity(this, gp.getMonTab());
        gp.getCChecker().checkEntity(this, gp.getItileTab());
        boolean contactPlayer = gp.getCChecker().checkPlayer(this);

        if (contactPlayer) {
        	damagePlayer(attack);
        }

        if (!collisionOn) {
            switch (direction) {
                case "Up": worldY -= speed; break;
                case "Down": worldY += speed; break;
                case "Left": worldX -= speed; break;
                case "Right": worldX += speed; break;
            }
        }

        spriteCounter++;
        if (spriteCounter > 10) {
            spriteNum = (spriteNum == 1) ? 2 : 1;
            spriteCounter = 0;
        }

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) {
                invincibleCounter = 0;
                invincible = false;
            }
        }
    }

    @Override
    public void damageReaction() {
    	actionLockCounter = 0;
    	direction = gp.getPlayer().getDirection();
    }

    @Override
    public void checkDrop() {
        int i = rand.nextInt(100) + 1;

        // Set monster drop
        if (i < 25) {
            dropItem(new OBJ_RubisGreen(gp));
        } else if (i < 50) {
            dropItem(new OBJ_Heart(gp));
        }
    }
}
