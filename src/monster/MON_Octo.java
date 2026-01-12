package monster;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import entity.Entity;
import main.GamePanel;
import main.UtilityTool;
import object.OBJ_Heart;
import object.OBJ_Rock;
import object.OBJ_RubisGreen;

public class MON_Octo extends Entity {

    public MON_Octo(GamePanel gp) {
        super(gp);

        type = typeMonster;
        name = "Octorock";
        speed = 1;
        maxLife = 3;
        life = maxLife;
        attack = 2;
        defense = 0;
        
        projectile = new OBJ_Rock(gp);
        
        deathAnimation = new BufferedImage[4];

        solidArea.x = gp.getTileSize()/4 + 3;
        solidArea.y = gp.getTileSize()/4 + 3;
        solidArea.width = gp.getTileSize() - 6;
        solidArea.height = gp.getTileSize() - 6;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage() {
        up1 = setup("/monster/OctoUp0", gp.getTileSize(),gp.getTileSize());
        up2 = setup("/monster/OctoUp1", gp.getTileSize(),gp.getTileSize());
        down1 = setup("/monster/OctoDown0", gp.getTileSize(),gp.getTileSize());
        down2 = setup("/monster/OctoDown1", gp.getTileSize(),gp.getTileSize());
        left1 = setup("/monster/OctoLeft0", gp.getTileSize(),gp.getTileSize());
        left2 = setup("/monster/OctoLeft1", gp.getTileSize(),gp.getTileSize());
        right1 = setup("/monster/OctoRight0", gp.getTileSize(),gp.getTileSize());
        right2 = setup("/monster/OctoRight1", gp.getTileSize(),gp.getTileSize());
    }
    
    @Override
    public BufferedImage setup(String imagePath, int width, int height) {
        UtilityTool uTool = new UtilityTool();
        BufferedImage image = null;
        
        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
            image = uTool.scaleImage(image, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void setAction() {
        // Si le monstre est en train de mourir, on ne met pas à jour ses actions
        if (dying) {
            return; // Sortir de la méthode si le monstre est en train de mourir
        }

        actionLockCounter++;

        if (actionLockCounter == 120) {
            Random rand = new Random();
            int i = rand.nextInt(100) + 1;

            if (i <= 25) {
                direction = "Up";
            }
            if (i > 25 && i <= 50) {
                direction = "Down";
            }
            if (i > 50 && i <= 75) {
                direction = "Left";
            }
            if (i > 75) {
                direction = "Right";
            }
            actionLockCounter = 0;
            
        }
        
        int i = new Random().nextInt(100)+1;
        
        if(i > 99 && !projectile.isAlive() && shotAvailableCounter == 30) {
        	projectile.set(worldX,  worldY,  direction,  true,  this);
        	gp.getProjList().add(projectile);
        	shotAvailableCounter = 0;
        }
        
        if (shotAvailableCounter < 30) {
            shotAvailableCounter++;
        }
        
    }

    @Override
    public void update() {
        if (dying) {
            // Si le monstre est en train de mourir, on ne fait rien (pas de mise à jour de position)
            return; 
        }

        if (alive) {
            setAction();  // Effectuer les actions du MON_Octo

            collisionOn = false;
            gp.getCChecker().checkTile(this);
            gp.getCChecker().checkObject(this, false);
            gp.getCChecker().checkPlayer(this);

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
                if (spriteNum == 1) spriteNum = 2;
                else if (spriteNum == 2) spriteNum = 1;
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
    }
    
    public void damageReaction() {
    	
    	actionLockCounter = 0;
    	direction = gp.getPlayer().getDirection();
    }
    
    public void checkDrop() {
        int i = new Random().nextInt(100) + 1;

        // Set monster drop
        if (i < 25) {
            dropItem(new OBJ_RubisGreen(gp));
        } else if (i >= 25 && i < 50) {
            dropItem(new OBJ_Heart(gp));
        }
    }
}
