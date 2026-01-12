package main;

import java.awt.Rectangle;

import entity.Player;

public class EventHandler {

    private GamePanel gp;
    private Rectangle[][][] eventRect;
    private int eventRectDefaultX, eventRectDefaultY;

    public EventHandler(GamePanel gp) {
        this.gp = gp;

        int maxMap = gp.getMaxMap();
        int maxWorldCols = gp.getMaxWorldCols();
        int maxWorldRows = gp.getMaxWorldRows();

        eventRect = new Rectangle[maxMap][maxWorldCols][maxWorldRows];

        for (int map = 0; map < maxMap; map++) {
            for (int col = 0; col < maxWorldCols; col++) {
                for (int row = 0; row < maxWorldRows; row++) {
                    eventRect[map][col][row] = new Rectangle();
                    eventRect[map][col][row].x = 0;
                    eventRect[map][col][row].y = 0;
                    eventRect[map][col][row].width = gp.getTileSize();
                    eventRect[map][col][row].height = gp.getTileSize();
                }
            }
        }

        eventRectDefaultX = 0;
        eventRectDefaultY = 0;
    }

    public void checkEvent() {
        // Exemples de coordonnées d'événements spécifiques
        if (hit(0, 27, 16, "Any")) {
            damagePit(gp.getDialogueState());
        }

        if (hit(0, 23, 12, "Up")) {
            healingPool(gp.getDialogueState());
        }
        
        if(hit(0,30,35,"Up")) {
            teleport(1,25,48);
        }

        if(hit(1,24,49,"Down") || hit(1,25,49,"Down") || hit(1,26,49,"Down") || hit(1,27,49,"Down")) {
        	teleport(0,30,36);
        }
    }

    private void damagePit(int gameState) {
        gp.setGameState(gameState);
        gp.ui.setCurrentDialogue("Vous tombez dans un trou !");
        gp.getPlayer().setLife(gp.getPlayer().getLife() - 1);
    }

    private void healingPool(int gameState) {
        if (gp.getkH().isEnter()) {
            gp.setGameState(gameState);
            gp.getPlayer().setAttackCanceled(true);

            if (gp.getPlayer().getLife() != gp.getPlayer().getMaxLife()) {
                gp.getPlayer().setLife(gp.getPlayer().getMaxLife());
                gp.ui.setCurrentDialogue("Vous avez bu et guéri vos blessures.");
            } else {
                gp.ui.setCurrentDialogue("Vous êtes déjà en pleine forme.");
            }

            gp.getkH().setEnter(false);
        }
    }

    public boolean hit(int map, int eventCol, int eventRow, String reqDirection) {
        boolean hit = false;

        Player player = gp.getPlayer();

        // Coordonnées absolues de la hitbox du joueur
        int playerLeftWorldX = player.getWorldX() + player.getSolidArea().x;
        int playerRightWorldX = playerLeftWorldX + player.getSolidArea().width;
        int playerTopWorldY = player.getWorldY() + player.getSolidArea().y;
        int playerBottomWorldY = playerTopWorldY + player.getSolidArea().height;

        Rectangle rect = eventRect[map][eventCol][eventRow];
        rect.x = eventCol * gp.getTileSize() + eventRectDefaultX;
        rect.y = eventRow * gp.getTileSize() + eventRectDefaultY;

        int rectLeftX = rect.x;
        int rectRightX = rect.x + rect.width;
        int rectTopY = rect.y;
        int rectBottomY = rect.y + rect.height;

        // Collision simple
        boolean intersects =
                playerRightWorldX > rectLeftX &&
                playerLeftWorldX < rectRightX &&
                playerBottomWorldY > rectTopY &&
                playerTopWorldY < rectBottomY;

        if (intersects) {
            if (reqDirection.equals("Any") || reqDirection.equals(player.getDirection())) {
                hit = true;
            }
        }

        // Reset les valeurs de la hitbox pour éviter des bugs ultérieurs
        rect.x = eventRectDefaultX;
        rect.y = eventRectDefaultY;

        return hit;
    }
    
    public void teleport(int map, int col, int row) {
    	gp.setCurrentMap(map);
    	gp.getPlayer().setWorldX(gp.getTileSize() * col);
    	gp.getPlayer().setWorldY(gp.getTileSize() * row);
    }
}
