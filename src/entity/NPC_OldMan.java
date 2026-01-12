package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import main.GamePanel;

public class NPC_OldMan extends Entity{
	
	public NPC_OldMan(GamePanel gp) {
	    super(gp);
	    
	    type = 1;
	    direction = "Down";
	    speed = 1;
	    
		solidArea.x = 0;
		solidArea.y = gp.getTileSize()/2;
		solidArea.width = gp.getTileSize();
		solidArea.height = gp.getTileSize()/2;
		solidAreaDefaultX = solidArea.x;
		solidAreaDefaultY = solidArea.y;
	    
	    getImage();
	    setDialogue();
	}
	
	public void getImage() {
	    up1 = setup("/npc/OldManUp0", gp.getTileSize(),gp.getTileSize());
	    up2 = setup("/npc/OldManUp1", gp.getTileSize(),gp.getTileSize());
	    up3 = setup("/npc/OldManUp2", gp.getTileSize(),gp.getTileSize());
	    
	    down1 = setup("/npc/OldManDown0", gp.getTileSize(),gp.getTileSize());
	    down2 = setup("/npc/OldManDown1", gp.getTileSize(),gp.getTileSize());
	    down3 = setup("/npc/OldManDown2", gp.getTileSize(),gp.getTileSize());
	    
	    left1 = setup("/npc/OldManLeft0", gp.getTileSize(),gp.getTileSize());
	    left2 = setup("/npc/OldManLeft1", gp.getTileSize(),gp.getTileSize());
	    left3 = setup("/npc/OldManLeft2", gp.getTileSize(),gp.getTileSize());
	    
	    right1 = setup("/npc/OldManRight0", gp.getTileSize(),gp.getTileSize());
	    right2 = setup("/npc/OldManRight1", gp.getTileSize(),gp.getTileSize());
	    right3 = setup("/npc/OldManRight2", gp.getTileSize(),gp.getTileSize());
	}
	
	public void setDialogue() {
		
		dialogues[0] = "Bonjour, gamin.";
		dialogues[1] = "Alors, tu es venu sur \ncette île pour trouver \nle trésor ?";
		dialogues[2] = "J'étais autrefois un \ngrand mage, mais \nmaintenant...";
		dialogues[3] = "Je suis un peu trop \nvieux pour partir à \nl'aventure.";
		dialogues[4] = "Eh bien, bonne chance \nà toi.";
		
	}
	
	public void setAction() {
		
		actionLockCounter ++;
		
		if(actionLockCounter == 120) {
			Random rand = new Random();
			int i = rand.nextInt(100)+1;
			
			if(i <= 25) {
				direction = "Up";
			}
			if(i > 25 && i <= 50) {
				direction = "Down";
			}
			if(i > 50 && i <= 75) {
				direction = "Left";
			}
			if(i > 75) {
				direction = "Right";
			}
			actionLockCounter = 0;
		}
	}
	
	public void speak() {
		
		if(dialogues[dialogueIndex] == null) {
			dialogueIndex = 0;
		}
		gp.ui.setCurrentDialogue(dialogues[dialogueIndex]);
		dialogueIndex++;
		
		switch(gp.getPlayer().getDirection()) {
		case "Up":
			direction = "Down";
			break;
		case "Down":
			direction = "Up";
			break;
		case "Left":
			direction = "Right";
			break;
		case "Right":
			direction = "Left";
			break;
		}
	}
}
