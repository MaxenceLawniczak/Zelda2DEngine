package main;

import entity.Player;
import main.MapData.Placement;

/**
 * Événements déclenchés quand le joueur marche sur une case (trou, source de soin, téléporteur).
 * Ils sont décrits dans res/maps/mapXX_data.txt (lignes "event ...") et modifiables avec l'éditeur.
 */
public class EventHandler {

    private GamePanel gp;

    // Après un événement, il faut s'éloigner d'une case avant d'en redéclencher un
    // (sinon le trou ferait perdre de la vie à chaque frame)
    private int previousEventX, previousEventY;
    private boolean canTouchEvent = true;

    public EventHandler(GamePanel gp) {
        this.gp = gp;
    }

    public void checkEvent() {

    	// Le joueur s'est-il assez éloigné du dernier événement ?
    	int xDistance = Math.abs(gp.getPlayer().getWorldX() - previousEventX);
    	int yDistance = Math.abs(gp.getPlayer().getWorldY() - previousEventY);
    	if (Math.max(xDistance, yDistance) > gp.getTileSize()) {
    		canTouchEvent = true;
    	}

    	if (!canTouchEvent) {
    		return;
    	}

    	MapData data = gp.getASetter().getMapData(gp.getCurrentMap());
    	for (Placement event : data.get(MapData.EVENT)) {
    		String direction = event.arg(0, "any");
    		if (!hit(gp.getCurrentMap(), event.col, event.row, direction)) {
    			continue;
    		}
    		switch (event.id) {
    		case "pit":
    			damagePit(gp.getDialogueState());
    			return;
    		case "heal":
    			healingPool(gp.getDialogueState());
    			return;
    		case "message":
    			// Message affiché une seule fois (ex : "Une étrange brume flotte ici...")
    			String key = "msg_" + gp.getCurrentMap() + "_" + event.col + "_" + event.row;
    			if (!gp.hasFlag(key)) {
    				gp.setFlag(key);
    				String text = String.join(" ", java.util.Arrays.copyOfRange(event.args, 1, event.args.length));
    				gp.ui.getDialogueBox().start(null, text.split("\\s*//\\s*"));
    				gp.getkH().releaseAll();
    			}
    			eventDone();
    			return;
    		case "teleport":
    			// Les numéros de map dans le fichier commencent à 1
    			teleport(event.argInt(1, 1) - 1, event.argInt(2, 0), event.argInt(3, 0));
    			return;
    		default:
    			System.err.println("Événement inconnu : " + event.id);
    		}
    	}
    }

    private void eventDone() {
    	previousEventX = gp.getPlayer().getWorldX();
    	previousEventY = gp.getPlayer().getWorldY();
    	canTouchEvent = false;
    }

    private void damagePit(int gameState) {
        gp.setGameState(gameState);
        gp.ui.setCurrentDialogue("Vous tombez dans un trou !");
        if (!gp.getDebug().isGodMode()) {
        	gp.getPlayer().setLife(gp.getPlayer().getLife() - 1);
        }
        gp.getkH().releaseAll();
        eventDone();
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
            eventDone();
        }
    }

    /** Le joueur touche-t-il la case (col, row) de cette map en regardant dans la bonne direction ? */
    public boolean hit(int map, int eventCol, int eventRow, String reqDirection) {

        // L'événement n'existe que sur sa propre map
        if (map != gp.getCurrentMap()) {
        	return false;
        }

        Player player = gp.getPlayer();

        // Coordonnées absolues de la hitbox du joueur
        int playerLeftWorldX = player.getWorldX() + player.getSolidArea().x;
        int playerRightWorldX = playerLeftWorldX + player.getSolidArea().width;
        int playerTopWorldY = player.getWorldY() + player.getSolidArea().y;
        int playerBottomWorldY = playerTopWorldY + player.getSolidArea().height;

        int rectLeftX = eventCol * gp.getTileSize();
        int rectRightX = rectLeftX + gp.getTileSize();
        int rectTopY = eventRow * gp.getTileSize();
        int rectBottomY = rectTopY + gp.getTileSize();

        boolean intersects =
                playerRightWorldX > rectLeftX &&
                playerLeftWorldX < rectRightX &&
                playerBottomWorldY > rectTopY &&
                playerTopWorldY < rectBottomY;

        return intersects && (reqDirection.equalsIgnoreCase("any") || reqDirection.equalsIgnoreCase(player.getDirection()));
    }

    public void teleport(int map, int col, int row) {
    	gp.teleportPlayer(map, col, row);
    	eventDone();
    }
}
