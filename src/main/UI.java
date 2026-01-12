package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import object.OBJ_Heart;
import object.OBJ_Key;
import entity.Entity;

public class UI {
    
    private BufferedImage HUDImg, characterScreen, optionsScreen;
	private GamePanel gp;
    private Graphics2D g2;
    private BufferedImage heartF, heart1, heartH, heart3, heartE;
    private Font customFont;
    private String currentDialogue = "";
    private int commandNum = 0;
    private int inventoryIndex = 0;
    
    private int slotCol = 0, slotRow = 0;
    
    public UI(GamePanel gp) {
        
        this.gp = gp;
        
        try {
        	InputStream fontStream = getClass().getResourceAsStream("/fonts/ZeldaDX.ttf");
        	if (fontStream != null) {
        		customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(20f);
            } else {
                throw new IOException("Font file not found in classpath");
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            customFont = new Font("Arial", Font.PLAIN, 0);
        }
        
        //HUD
        Entity heart = new OBJ_Heart(gp);
        heartF = setup("/HUD/HeartHudF",14,14);
        heart1 = setup("/HUD/HeartHudMinus1",14,14);
        heartH = setup("/HUD/HeartHudH",14,14);
        heart3 = setup("/HUD/HeartHudMinus3",14,14);
        heartE = setup("/HUD/HeartHudE",14,14);
        HUDImg = setup("/HUD/HUD",gp.getTileSize()*20,gp.getTileSize()*2);
        characterScreen = setup("/HUD/CharacterScreen",gp.getTileSize()*20,gp.getTileSize()*16);
        optionsScreen = setup("/HUD/OptionsScreen",gp.getTileSize()*20,gp.getTileSize()*16);
    }
    
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
    
    public void draw(Graphics2D g2) {
        this.g2 = g2;
        
        g2.setFont(customFont.deriveFont(20f));
        g2.setColor(Color.WHITE);
        
        //TitleState
        if (gp.getGameState() == gp.getTitleState()) {
        	drawTitleScreen();
        }
        //PlayState
        if (gp.getGameState() == gp.getPlayState()) {
        	drawPlayerLife();
        }
        
        //PauseState
        if (gp.getGameState() == gp.getPauseState()) {
        	drawPlayerLife();
        	drawPauseScreen();
        }
        
        //DialogueState
        if(gp.getDialogueState() == gp.getGameState()) {
        	drawPlayerLife();
        	drawDialogueScreen();
        }
        
        //CharacterState
        if(gp.getGameState() == gp.getCharacterState()) {
        	drawCharacterScreen();
        }
        
        //GameOverState
        if(gp.getGameState() == gp.getGameOverState()) {
        	drawGameOverScreen();
        }
    }
    
	private void drawGameOverScreen() {
    	String text = "Game Over";
    	int y = gp.getScreenHeight()/2;
    	Color c = new Color(0,0,0,128);
    	g2.setColor(c);
    	g2.fillRect(0, 0, gp.getMaxScreenCols()*gp.getTileSize(), gp.getMaxScreenRows()*gp.getTileSize());
    	g2.setColor(Color.WHITE);
    	Font modifiedFont = customFont.deriveFont(Font.BOLD, 40f);
    	g2.setFont(modifiedFont);
    	g2.drawString(text, getXForCenteredText(text), y);
		
    	//Retry
    	Font modifiedFont2 = customFont.deriveFont(Font.BOLD, 30f);
    	g2.setFont(modifiedFont2);
    	text = "Retry";
    	int x = getXForCenteredText(text);
    	y += gp.getTileSize() * 3;
    	g2.drawString(text, x, y);
    	if (commandNum == 0) {
    		g2.drawString(">", x-40, y);
    	}
    	
    	//Ecran Titre
    	text = "Quit";
    	x = getXForCenteredText(text);
    	y += gp.getTileSize() * 1.5;
    	g2.drawString(text, x, y);
    	if (commandNum == 1) {
    		g2.drawString(">", x-40, y);
    	}
	}

	public int getItemIndexOnSlot(int inventoryIndex) {
        return slotCol + (slotRow * 4) + (inventoryIndex * 20);
    }

    private void drawCharacterScreen() {
        g2.drawImage(characterScreen, 0, 0, null);
        drawPlayerLife();
        
        // Affichage du nombre de pages
        int maxPages = gp.getPlayer().getMaxInventoryPage();
        String nbPage = (inventoryIndex + 1) + " / " + maxPages;
        int pageCenterX = getXForCenteredText(nbPage, gp.getTileSize() * 6) + gp.getTileSize() * 15;
        int pageTextY = gp.getTileSize() * 14 + gp.getTileSize() / 2;
        
        // Slots d'inventaire sur la partie droite de l'écran (4 colonnes, 5 lignes)
        final int slotXstart = (gp.getTileSize() * 10) - 5;
        final int slotYstart = (gp.getTileSize()/2) + (gp.getTileSize() * 2) + 6;
        int slotX = slotXstart;
        int slotY = slotYstart;
        int slotSize = (gp.getTileSize() * 2) + 8;
        
        int itemsPerPage = 20;
        int startIndex = inventoryIndex * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, gp.getPlayer().getInventory().size());
        
        for (int i = startIndex; i < endIndex; i++) {
            int slotPosition = i - startIndex;
            boolean equipped = gp.getPlayer().getInventory().get(i) == gp.getPlayer().getCurrentWeapon() ||
                               gp.getPlayer().getInventory().get(i) == gp.getPlayer().getCurrentShield() ||
                               gp.getPlayer().getInventory().get(i) == gp.getPlayer().getCurrentBow();
            
            if (equipped) {
                g2.setColor(new Color(176, 166, 116));
                g2.setFont(customFont.deriveFont(15f));
                g2.drawString("E", slotX + (gp.getTileSize()*2 - 5), slotY + gp.getTileSize()*2);
            }
            
            if (gp.getPlayer().getInventory().get(i).getName().equals("Flèche")) {
                g2.setColor(new Color(176, 166, 116));
                g2.setFont(customFont.deriveFont(15f));
                int amount = gp.getPlayer().getInventory().get(i).getAmount();
                int textOffset = amount >= 100 ? 33 : amount >= 10 ? 25 : 10;
                g2.drawString("x" + amount, slotX + (gp.getTileSize()*2 - textOffset), slotY + gp.getTileSize()*2);
            }
            
            BufferedImage image = gp.getPlayer().getInventory().get(i).getDown1();
            g2.drawImage(image, 2 + slotX + gp.getTileSize()/2, 2 + slotY + gp.getTileSize()/2, gp.getTileSize() + (gp.getTileSize()/2), gp.getTileSize() + (gp.getTileSize()/2), null);
            
            slotX += slotSize;
            if ((slotPosition + 1) % 4 == 0) { // Changer de ligne après 4 colonnes
                slotX = slotXstart;
                slotY += slotSize + 2;
            }
        }
        
        // Curseur
        int cursorX = slotXstart + (slotSize * slotCol) + 10;
        int cursorY = slotYstart + (slotSize * slotRow) + 10;
        int cursorWidth = (gp.getTileSize()*2) + 4;
        int cursorHeight = (gp.getTileSize()*2) + 4;
        
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);
        
        // Description sur la partie gauche de l'écran
        int textX = gp.getTileSize();
        int textY = 10 + gp.getTileSize()*3;
        g2.setFont(customFont.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(176, 166, 116));
        
        int itemIndex = getItemIndexOnSlot(inventoryIndex);
        if (itemIndex < gp.getPlayer().getInventory().size()) {
            for (String line : gp.getPlayer().getInventory().get(itemIndex).getDescription().split("\n")) {
                g2.drawString(line, textX, textY);
                textY += 40;
            }
        }
        g2.drawString(nbPage, pageCenterX, pageTextY);
    }

    private void drawPlayerLife() {
        // Afficher l'image du HUD en haut à gauche
    	int valX = 0;
    	int valY = 0;
    	g2.drawImage(HUDImg, valX, valY, null);
    	
    	//Afficher les rubis dans l'inventaire
    	int RubX = (gp.getTileSize() * 10) + gp.getTileSize() / 2;
    	int RubY = gp.getTileSize() + 10;
    	
    	BufferedImage centImg;
    	BufferedImage dixImg;
    	BufferedImage uniteImg;
    	
    	int cent, dix, uni;
    	
    	cent = gp.getPlayer().getArgent() /100;
    	dix = (gp.getPlayer().getArgent() /10) - 10 * cent;
    	uni = (gp.getPlayer().getArgent() - (100 * cent) - (10 * dix));
    	
    	centImg = setup("/HUD/Num0"+cent, 20,20);
    	dixImg = setup("/HUD/Num0"+dix, 20,20);
    	uniteImg = setup("/HUD/Num0"+uni, 20,20);
    	
    	g2.drawImage(centImg, RubX, RubY, null);
    	g2.drawImage(dixImg, RubX + 24, RubY, null);
    	g2.drawImage(uniteImg, RubX + 48, RubY, null);
    	
    	//Afficher Inventaire
    	//int shieldX;
    	//int swordX;
    	
        // Position de départ des cœurs
        int x = gp.getTileSize() * 13;
        int y = 10;
        int i = 0;

        // Dessiner la vie maximale (cœurs vides)
        while (i < gp.getPlayer().getMaxLife() / 4) {
            g2.drawImage(heartE, x, y, null); // Cœur vide
            i++;
            x += gp.getTileSize()/2; // Décalage pour le prochain cœur
        }

        // Reset pour dessiner la vie actuelle
        x = gp.getTileSize() * 13;
        y = 10;
        i = 0;
        int life = gp.getPlayer().getLife();

        // Dessiner la vie actuelle
        while (i < life) {
            if (life - i >= 4) {
                g2.drawImage(heartF, x, y, null); // Cœur plein
                i += 4;
            } else if (life - i == 3) {
                g2.drawImage(heart1, x, y, null); // Trois quarts de cœur
                i += 3;
            } else if (life - i == 2) {
                g2.drawImage(heartH, x, y, null); // Demi-cœur
                i += 2;
            } else if (life - i == 1) {
                g2.drawImage(heart3, x, y, null); // Quart de cœur
                i += 1;
            }
            x += gp.getTileSize()/2; // Décalage pour le prochain cœur
        }
    }

	private void drawTitleScreen() {
            	
    	GradientPaint gradient = new GradientPaint(0, 0, new Color(0x001f3d), 0, gp.getScreenHeight(), new Color(0x4b006e));
    	g2.setPaint(gradient);
    	g2.fillRect(0, 0, gp.getMaxScreenCols() * gp.getTileSize(), gp.getMaxScreenRows() * gp.getTileSize());
    	
    	
    	// Title Name
        String title = "The Tales Of Zeldo";

        Font largerFont = customFont.deriveFont(Font.BOLD, 45f); // Agrandir la police à 60px
        g2.setFont(largerFont);

        // Calculer la position pour centrer le titre
        int x = getXForCenteredText(title);
        int y = gp.getScreenHeight() / 4;

        // Ombre
        g2.setColor(Color.BLACK);
        g2.drawString(title, x + 5, y + 5);
        
        // Couleur principale
        g2.setColor(Color.WHITE);
        g2.drawString(title, x, y);
        
        //Image du personnage
        x = (gp.getScreenWidth()/2) - gp.getTileSize() ;
        y += gp.getTileSize() * 2;
        g2.drawImage(gp.getPlayer().getImage("Down", 0), x, y, gp.getTileSize()*2, gp.getTileSize()*2, null);
    
        //Menu
        Font menuFont = customFont.deriveFont(Font.BOLD, 30f);
        g2.setFont(menuFont);
        
        String text1 = "Nouveau";
        x = getXForCenteredText(text1);
        y += gp.getTileSize() * 4;
        g2.drawString(text1,x,y);
        if(commandNum == 0) {
        	g2.drawString(">", x-gp.getTileSize(), y - 3);
        }
        
        String text2 = "Charger";
        x = getXForCenteredText(text2);
        y += gp.getTileSize();
        g2.drawString(text2,x,y);
        if(commandNum == 1) {
        	g2.drawString(">", x-gp.getTileSize(), y - 3);
        }
        
        String text3 = "Quitter";
        x = getXForCenteredText(text3);
        y += gp.getTileSize();
        g2.drawString(text3,x,y);
        if(commandNum == 2) {
        	g2.drawString(">", x-gp.getTileSize(), y - 3);
        }
        
    }

	private void drawDialogueScreen() {
		
		//Fenêtre
    	int x = gp.getTileSize()*2;
    	int y =gp.getTileSize()/2;
    	int width = gp.getScreenWidth() - (gp.getTileSize()*4);
    	int height = gp.getTileSize()*4;
    	
    	drawSubWindow(x,y,width,height);
    	
    	Font dialogueFont = customFont.deriveFont(40f);
    	g2.setFont(dialogueFont);
    	x += gp.getTileSize();
    	y += gp.getTileSize();
    	
    	g2.setColor(Color.WHITE);
    	
    	for(String line : currentDialogue.split("\n")) {
    		g2.drawString(line, x, y);
    		y += 40;
    	}
    	
	}
    
    public void drawSubWindow(int x, int y, int width, int height) {
    	
    	Color c = new Color(0,0,0,210);
    	g2.setColor(c);
    	g2.fillRoundRect(x, y, width, height,35,35);
    	
    	c = new Color(255,255,255,200);
    	g2.setColor(c);
    	g2.setStroke(new BasicStroke(5));
    	g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 35, 35);
    }

	public void drawPauseScreen() {
    	String text = "EN PAUSE";
    	int y = gp.getScreenHeight()/2;
    	Color c = new Color(0,0,0,128);
    	g2.setColor(c);
    	g2.fillRect(0, 0, gp.getMaxScreenCols()*gp.getTileSize(), gp.getMaxScreenRows()*gp.getTileSize());
    	g2.setColor(Color.WHITE);
    	Font modifiedFont = customFont.deriveFont(Font.BOLD, 40f);
    	g2.setFont(modifiedFont);
    	g2.drawString(text, getXForCenteredText(text), y);
    }
    
    public int getXForCenteredText(String txt) {
    	int length = (int)g2.getFontMetrics().getStringBounds(txt, g2).getWidth();
    	int x = gp.getScreenWidth()/2 -length/2;
    	
    	return x;
    }
    
    public int getXForAlignedToRightText(String txt, int tailX) {
    	int length = (int)g2.getFontMetrics().getStringBounds(txt, g2).getWidth();
    	int x = tailX - length;
    	
    	return x;
    }
    
    public int getXForCenteredText(String txt, int width) {
    	int length = (int)g2.getFontMetrics().getStringBounds(txt, g2).getWidth();
    	int x = width/2 -length/2;
    	
    	return x;
    }
    
    public void setSlotCol(int i) {
    	slotCol = i;
    }
    
    public void setSlotRow(int i) {
    	slotRow = i;
    }
    
    public int getSlotCol() {
    	return slotCol;
    }
    
    public int getSlotRow() {
    	return slotRow;
    }
    
    public void setCurrentDialogue(String currentDialogue) {
    	this.currentDialogue = currentDialogue;
    }
    
    public String getCurrentDialogue() {
    	return currentDialogue;
    }
    
    public int getCommandNum() {
    	return commandNum;
    }
    
    public void setCommandNum(int i) {
    	commandNum = i;
    }

	public int getInventoryIndex() {
		if(inventoryIndex < 0)
			inventoryIndex = 0;
		return inventoryIndex;
	}

	public void setInventoryIndex(int inventoryIndex) {
		this.inventoryIndex = inventoryIndex;
	}
}
