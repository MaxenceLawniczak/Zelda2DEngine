package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import entity.Entity;

public class UI {
    
    private BufferedImage HUDImg, characterScreen, optionsScreen;
	private GamePanel gp;
    private Graphics2D g2;
    private BufferedImage heartF, heart1, heartH, heart3, heartE;
    private BufferedImage[] numbers = new BufferedImage[10]; // Chiffres du compteur de rubis
    private Font customFont;
    private DialogueBox dialogueBox;
    private BufferedImage keyIcon;
    private TitleScreen titleScreen;
private int commandNum = 0;
    private int inventoryIndex = 0;
    
    private int slotCol = 0, slotRow = 0;
    
    public UI(GamePanel gp) {
        
        this.gp = gp;
        
        try {
        	InputStream fontStream = getClass().getResourceAsStream("/fonts/ZeldaDX.ttf");
        	if (fontStream != null) {
        		customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(20f);
        		fontStream.close();
            } else {
                throw new IOException("Font file not found in classpath");
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            customFont = new Font("Arial", Font.PLAIN, 20);
        }
        
        dialogueBox = new DialogueBox(gp, customFont);
        titleScreen = new TitleScreen(gp, customFont);

        //HUD
        int heartSize = gp.getTileSize() * 7 / 16; // 14 px en échelle 2, grandit avec l'échelle
        heartF = setup("/HUD/hearts/HeartHudF", heartSize, heartSize);
        heart1 = setup("/HUD/hearts/HeartHudMinus1", heartSize, heartSize);
        heartH = setup("/HUD/hearts/HeartHudH", heartSize, heartSize);
        heart3 = setup("/HUD/hearts/HeartHudMinus3", heartSize, heartSize);
        heartE = setup("/HUD/hearts/HeartHudE", heartSize, heartSize);
        HUDImg = setup("/HUD/screens/HUD",gp.getTileSize()*20,gp.getTileSize()*2);
        characterScreen = setup("/HUD/screens/CharacterScreen",gp.getTileSize()*20,gp.getTileSize()*16);
        optionsScreen = setup("/HUD/screens/OptionsScreen",gp.getTileSize()*20,gp.getTileSize()*16);
        
        // Chiffres chargés une seule fois (avant : relus sur le disque à chaque frame)
        for (int i = 0; i < numbers.length; i++) {
        	numbers[i] = setup("/HUD/numbers/Num0" + i, 20, 20);
        }
    }
    
    public BufferedImage setup(String imagePath, int width, int height) {
        return UtilityTool.loadImage(imagePath, width, height);
    }
    
    public void draw(Graphics2D g2) {
        this.g2 = g2;
        
        g2.setFont(customFont.deriveFont(20f));
        g2.setColor(Color.WHITE);
        
        //TitleState
        if (gp.getGameState() == gp.getTitleState()) {
        	drawTitleScreen();
        }
        //Intro (histoire)
        if (gp.getGameState() == gp.getIntroState()) {
        	gp.getIntro().draw(g2);
        }
        //PlayState
        if (gp.getGameState() == gp.getPlayState()) {
        	drawPlayerLife();
        	drawLocationBanner();
        	drawNotification();
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
    	y += (int) (gp.getTileSize() * 1.5);
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
            
            if (gp.getPlayer().getInventory().get(i).getMaxAmount() > 0) { // Objet empilable (flèches...)
                g2.setColor(new Color(176, 166, 116));
                g2.setFont(customFont.deriveFont(15f));
                int amount = gp.getPlayer().getInventory().get(i).getAmount();
                int textOffset = amount >= 100 ? 33 : amount >= 10 ? 25 : 10;
                g2.drawString("x" + amount, slotX + (gp.getTileSize()*2 - textOffset), slotY + gp.getTileSize()*2);
            }
            
            BufferedImage image = gp.getPlayer().getInventory().get(i).getDown1();
            g2.drawImage(image, 2 + slotX + gp.getTileSize()/2, 2 + slotY + gp.getTileSize()/2, gp.getTileSize() + (gp.getTileSize()/2), gp.getTileSize() + (gp.getTileSize()/2), null);
            drawDurabilityBar(gp.getPlayer().getInventory().get(i), slotX + gp.getTileSize() / 2 + 4, slotY + gp.getTileSize() * 2 + 2, gp.getTileSize() + gp.getTileSize() / 2 - 4);
            
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
        	Entity item = gp.getPlayer().getInventory().get(itemIndex);
            for (String line : item.getDescription().split("\n")) {
                g2.drawString(line, textX, textY);
                textY += 40;
            }
            // Statistiques de l'objet
            g2.setColor(new Color(110, 85, 45));
            for (String line : getItemStats(item)) {
            	g2.drawString(line, textX, textY);
            	textY += 32;
            }
            // Spécificité de l'objet
            if (!item.getSpecialName().isEmpty()) {
            	textY += 8;
            	g2.setColor(new Color(170, 90, 20));
            	g2.drawString("* " + item.getSpecialName(), textX, textY);
            	textY += 30;
            	g2.setColor(new Color(110, 85, 45));
            	for (String line : DialogueBox.wrap(item.getSpecialDescription(), g2.getFontMetrics(), gp.getTileSize() * 8)) {
            		g2.drawString(line, textX, textY);
            		textY += 28;
            	}
            }
        }
        g2.drawString(nbPage, pageCenterX, pageTextY);
    }

    // ===== Nom du lieu affiché en arrivant sur une map =====
    private String locationName = null;
    private int locationCounter = 0;

    public void showLocation(String name) {
    	if (name == null || name.isEmpty()) return;
    	locationName = name;
    	locationCounter = 240;
    }

    private void drawLocationBanner() {
    	if (locationCounter <= 0 || locationName == null) return;
    	locationCounter--;
    	// Apparition puis disparition en fondu
    	float alpha = Math.min(1f, Math.min((240 - locationCounter) / 40f, locationCounter / 40f));
    	g2.setFont(customFont.deriveFont(Font.BOLD, gp.getTileSize() * 0.75f));
    	int textWidth = g2.getFontMetrics().stringWidth(locationName);
    	int x = (gp.getScreenWidth() - textWidth) / 2;
    	int y = gp.getTileSize() * 4;
    	g2.setColor(new Color(0, 0, 0, (int) (150 * alpha)));
    	g2.fillRoundRect(x - gp.getTileSize(), y - gp.getTileSize(), textWidth + gp.getTileSize() * 2, (int) (gp.getTileSize() * 1.5), 30, 30);
    	g2.setColor(new Color(255, 230, 160, (int) (255 * alpha)));
    	g2.drawString(locationName, x, y);
    }

    /** Lignes de stats affichées sous la description d'un objet dans l'inventaire. */
    private java.util.List<String> getItemStats(Entity item) {
    	java.util.List<String> stats = new java.util.ArrayList<>();
    	if (item.isWeapon()) {
    		stats.add("Attaque : " + item.getAttackValue());
    		int duration = item.getMotion3Duration();
    		stats.add("Vitesse : " + (duration <= 12 ? "rapide" : duration <= 15 ? "normale" : duration <= 18 ? "lente" : "très lente"));
    		int reach = item.getAttackArea().width * 100 / gp.getTileSize();
    		stats.add("Portée : " + (reach >= 120 ? "longue" : reach >= 100 ? "moyenne" : "courte"));
    	} else if (item.isShield()) {
    		stats.add("Défense : " + item.getDefenseValue());
    		stats.add("Espace : se protéger (en marchant)");
    	} else if (item.isBow()) {
    		stats.add("Dégâts des flèches : " + item.getAttackValue());
    	}
    	if (item.isWeapon() || item.isShield() || item.isBow()) {
    		stats.add(item.isUnbreakable() ? "Durabilité : incassable" : "Durabilité : " + item.getDurability() + " / " + item.getMaxDurability());
    	}
    	return stats;
    }

    private void drawPlayerLife() {
        // Afficher l'image du HUD en haut à gauche
    	int valX = 0;
    	int valY = 0;
    	g2.drawImage(HUDImg, valX, valY, null);

    	// Bouclier et arme équipés dans les deux emplacements du HUD
    	// (le HUD fait 160 px de large à l'origine, agrandi pour remplir l'écran)
    	int hudPixel = gp.getScreenWidth() / 160;
    	Entity shield = gp.getPlayer().getCurrentShield();
    	Entity weapon = gp.getPlayer().getCurrentWeapon();
    	drawEquipmentSlot(shield, 12 * hudPixel, hudPixel, hudPixel, shield != null ? shield.getDefenseValue() : 0);
    	drawEquipmentSlot(weapon, 52 * hudPixel, hudPixel, hudPixel, weapon != null ? weapon.getAttackValue() : 0);
    	
    	//Afficher les rubis dans l'inventaire
    	int RubX = (gp.getTileSize() * 10) + gp.getTileSize() / 2;
    	int RubY = gp.getTileSize() + 10;
    	
    	// Le compteur n'a que 3 chiffres : on le bloque entre 0 et 999
    	int argent = Math.max(0, Math.min(999, gp.getPlayer().getArgent()));
    	int cent = argent / 100;
    	int dix = (argent / 10) % 10;
    	int uni = argent % 10;
    	
    	g2.drawImage(numbers[cent], RubX, RubY, null);
    	g2.drawImage(numbers[dix], RubX + 24, RubY, null);
    	g2.drawImage(numbers[uni], RubX + 48, RubY, null);

    	// Petites clés
    	if (gp.getPlayer().getKeys() > 0) {
    		int kx = gp.getTileSize() * 17 + gp.getTileSize() / 2, ky = gp.getTileSize() + 4;
    		if (keyIcon == null) keyIcon = Sprites.key(gp.getTileSize() * 2 / 3);
    		g2.drawImage(keyIcon, kx, ky, null);
    		g2.setFont(customFont.deriveFont(Font.BOLD, gp.getTileSize() * 0.5f));
    		g2.setColor(new Color(60, 50, 30));
    		g2.drawString("x" + gp.getPlayer().getKeys(), kx + gp.getTileSize() * 2 / 3, ky + gp.getTileSize() / 2);
    	}
    	
        // Dessiner la vie maximale (cœurs vides), 10 cœurs par ligne
        int heartCount = gp.getPlayer().getMaxLife() / 4;
        for (int h = 0; h < heartCount; h++) {
            g2.drawImage(heartE, heartX(h), heartY(h), null); // Cœur vide
        }

        // Dessiner la vie actuelle
        int life = gp.getPlayer().getLife();
        int i = 0;
        int h = 0;
        while (i < life) {
            if (life - i >= 4) {
                g2.drawImage(heartF, heartX(h), heartY(h), null); // Cœur plein
                i += 4;
            } else if (life - i == 3) {
                g2.drawImage(heart1, heartX(h), heartY(h), null); // Trois quarts de cœur
                i += 3;
            } else if (life - i == 2) {
                g2.drawImage(heartH, heartX(h), heartY(h), null); // Demi-cœur
                i += 2;
            } else {
                g2.drawImage(heart3, heartX(h), heartY(h), null); // Quart de cœur
                i += 1;
            }
            h++; // Cœur suivant
        }
    }

    private int heartX(int heartIndex) {
    	return gp.getTileSize() * 13 + (heartIndex % 10) * (gp.getTileSize() / 2);
    }

    private int heartY(int heartIndex) {
    	return 10 + (heartIndex / 10) * (gp.getTileSize() * 9 / 16);
    }

    /** Petite barre de durabilité (verte -> jaune -> rouge). Rien pour les objets incassables. */
    private void drawDurabilityBar(Entity item, int x, int y, int width) {
    	if (item == null || item.isUnbreakable() || !(item.isWeapon() || item.isShield() || item.isBow())) return;
    	float ratio = item.getDurability() / (float) item.getMaxDurability();
    	Color color = ratio > 0.5f ? new Color(80, 200, 80) : ratio > 0.2f ? new Color(230, 190, 40) : new Color(220, 50, 40);
    	g2.setColor(new Color(40, 30, 20));
    	g2.fillRect(x - 1, y - 1, width + 2, 6);
    	g2.setColor(color);
    	g2.fillRect(x, y, Math.max(1, (int) (width * ratio)), 4);
    }

    // ===== Notifications (objet brisé...) en bas de l'écran =====
    private String notification = null;
    private int notificationCounter = 0;

    public void showNotification(String text) {
    	notification = text;
    	notificationCounter = 180;
    }

    private void drawNotification() {
    	if (notificationCounter <= 0 || notification == null) return;
    	notificationCounter--;
    	float alpha = Math.min(1f, notificationCounter / 30f);
    	g2.setFont(customFont.deriveFont(Font.BOLD, gp.getTileSize() * 0.5f));
    	int w = g2.getFontMetrics().stringWidth(notification) + gp.getTileSize();
    	int x = (gp.getScreenWidth() - w) / 2;
    	int y = gp.getScreenHeight() - gp.getTileSize() * 2;
    	g2.setColor(new Color(0, 0, 0, (int) (180 * alpha)));
    	g2.fillRoundRect(x, y, w, (int) (gp.getTileSize() * 0.9), 20, 20);
    	g2.setColor(new Color(255, 200, 120, (int) (255 * alpha)));
    	g2.drawString(notification, x + gp.getTileSize() / 2, y + (int) (gp.getTileSize() * 0.62));
    }

    /** Dessine l'icône d'un objet équipé dans un emplacement du HUD, avec sa valeur. */
    private void drawEquipmentSlot(Entity item, int x, int y, int hudPixel, int value) {
    	if (item == null || item.getDown1() == null) return;
    	int size = hudPixel * 13;
    	g2.drawImage(item.getDown1(), x + hudPixel, y + hudPixel / 2, size, size, null);
    	drawDurabilityBar(item, x + size + hudPixel * 2, y + hudPixel * 12, hudPixel * 10);
    	if (value > 0) {
    		g2.setFont(customFont.deriveFont(Font.BOLD, hudPixel * 5f));
    		g2.setColor(new Color(60, 50, 30));
    		g2.drawString("L-" + value, x + size + hudPixel * 2, y + hudPixel * 11);
    	}
    }

	private void drawTitleScreen() {
		titleScreen.draw(g2, commandNum);
	}

	private void drawDialogueScreen() {
		dialogueBox.draw(g2);
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
    
    /** Dialogue d'une seule page sans nom (coffres, événements...). */
    public void setCurrentDialogue(String currentDialogue) {
    	dialogueBox.start(null, currentDialogue);
    }

    public String getCurrentDialogue() {
    	return dialogueBox.getCurrentPage();
    }

    public Font getCustomFont() {
    	return customFont;
    }
    
    public DialogueBox getDialogueBox() {
    	return dialogueBox;
    }

    public TitleScreen getTitleScreen() {
    	return titleScreen;
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
