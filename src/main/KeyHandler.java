package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Clavier ET souris. Un clic gauche est traité comme une touche dont le code est
 * MouseEvent.BUTTON1 (aucune touche du clavier n'a ce code), ce qui permet de l'utiliser
 * dans les mêmes tests que les touches : if (code == MouseEvent.BUTTON1) ...
 */
public class KeyHandler implements KeyListener, MouseListener{

	private GamePanel gp;
	private boolean up, down, left, right, click;
	private boolean shotKey;
	private boolean guardKey; // Espace : lever le bouclier
	
	public KeyHandler(GamePanel gp) {
		this.gp = gp;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		// Les touches arrivent sur le thread Swing : on attend que la boucle de jeu
		// ait fini sa frame avant de modifier l'état du jeu.
		synchronized (gp.getGameLock()) {
			handleKeyPressed(e.getKeyCode());
		}
	}
	
	private void handleKeyPressed(int code) {
		
		// Pendant un fondu au noir, on ignore les touches
		if(gp.isFading()) {
			return;
		}
		
		//Title
		if(gp.getGameState() == gp.getTitleState()) {
			if(code == KeyEvent.VK_ESCAPE)
				System.exit(0);
			titleState(code);
		}
		
		//Intro
		else if(gp.getGameState() == gp.getIntroState()) {
			introState(code);
		}
		
		//PlayState
		else if(gp.getGameState() == gp.getPlayState()) {
			playState(code);
		}
		//PauseState
		else if(gp.getGameState() == gp.getPauseState()) {
			if(code == KeyEvent.VK_ESCAPE)
				System.exit(0);
			pauseState(code);
		}
		
		//DialogueState
		else if(gp.getGameState() == gp.getDialogueState()) {
			dialogueState(code);
		}
		
		//CharacterState
		else if(gp.getGameState() == gp.getCharacterState()) {
			characterState(code);
		}
		
		//GameOverState
		else if(gp.getGameState() == gp.getGameOverState()) {
			gameOverState(code);
		}
	}
	
	private void gameOverState(int code) {
		if(code == KeyEvent.VK_UP || code == KeyEvent.VK_Z) {
			gp.ui.setCommandNum(gp.ui.getCommandNum()-1);
			if(gp.ui.getCommandNum()<0) {
				gp.ui.setCommandNum(1);
			}
		}
		if(code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
			gp.ui.setCommandNum(gp.ui.getCommandNum()+1);
			if(gp.ui.getCommandNum()>1) {
				gp.ui.setCommandNum(0);
			}
		}
		if(code == MouseEvent.BUTTON1) {
			if(gp.ui.getCommandNum() == 0) {
				gp.setGameState(gp.getPlayState());
				gp.retry();
			}
			if(gp.ui.getCommandNum() == 1) {
				gp.setGameState(gp.getTitleState());
				gp.restart();
				gp.ui.setCommandNum(0);
			}
		}
	}

	public void titleState(int code) {
		if(code == KeyEvent.VK_Z || code == KeyEvent.VK_UP) {
			gp.ui.setCommandNum(gp.ui.getCommandNum() - 1);
			if(gp.ui.getCommandNum() < 0) 
				gp.ui.setCommandNum(2);
		}
		
		if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
			gp.ui.setCommandNum(gp.ui.getCommandNum() + 1);
			if(gp.ui.getCommandNum() > 2) 
				gp.ui.setCommandNum(0);
		}
		
		if(code == MouseEvent.BUTTON1) {
			// Pendant l'animation d'apparition, Entrée l'accélère au lieu de valider
			if(!gp.ui.getTitleScreen().isReady()) {
				gp.ui.getTitleScreen().skipAppear();
				return;
			}
			if(gp.ui.getCommandNum() == 0)
				gp.startNewGame();
			if(gp.ui.getCommandNum() == 1){
				//Plus tard
			}
			if(gp.ui.getCommandNum() == 2)
				System.exit(0);
		}
	}
	
	public void playState(int code) {
		if(code == KeyEvent.VK_Z) {
			up = true;
		}
		
		if(code == KeyEvent.VK_S) {
			down = true;
		}
		
		if(code == KeyEvent.VK_Q) {
			left = true;
		}
		
		if(code == KeyEvent.VK_D) {
			right = true;
		}
		
		if(code == KeyEvent.VK_P) {
			gp.setGameState(gp.getPauseState());
		}
		
		if(code == KeyEvent.VK_C) {
			gp.setGameState(gp.getCharacterState());
		}
		
		if(code == MouseEvent.BUTTON1) {
			click = true;
		}
		
		if(code == KeyEvent.VK_F) {
			shotKey = true;
		}
		
		if(code == KeyEvent.VK_SPACE) {
			guardKey = true;
		}
		
		//Carte : petite -> grande -> cachée
		if(code == KeyEvent.VK_M) {
			gp.getMinimap().cycleMode();
		}
		
		//Debug
		if(code == KeyEvent.VK_T) {
			gp.getDebug().toggle();
		}
		if(gp.getDebug().isEnabled()) {
			debugKeys(code);
		}
		
		if(code == KeyEvent.VK_ESCAPE)
			System.exit(0);
	}
	
	// Touches disponibles uniquement en mode debug (T)
	private void debugKeys(int code) {
		DebugTools debug = gp.getDebug();
		switch (code) {
		case KeyEvent.VK_G: debug.toggleGodMode(); break;
		case KeyEvent.VK_V: debug.toggleFlyMode(); break;
		case KeyEvent.VK_H: debug.toggleHitboxes(); break;
		case KeyEvent.VK_I: debug.giveAllItems(); break;
		case KeyEvent.VK_N: debug.cycleFocus(); break;
		case KeyEvent.VK_B: debug.resetFocus(); break;
		case KeyEvent.VK_F1: debug.toggleHelp(); break;
		case KeyEvent.VK_UP: debug.changeLife(1); break;
		case KeyEvent.VK_DOWN: debug.changeLife(-1); break;
		case KeyEvent.VK_RIGHT: debug.changeMaxLife(4); break;
		case KeyEvent.VK_LEFT: debug.changeMaxLife(-4); break;
		case KeyEvent.VK_PAGE_UP: debug.changeMap(-1); break;
		case KeyEvent.VK_PAGE_DOWN: debug.changeMap(1); break;
		//Recharge la map actuelle depuis les fichiers (pratique avec l'éditeur de map ouvert)
		case KeyEvent.VK_R: gp.reloadCurrentMap(); break;
		default: break;
		}
	}
	
	public void pauseState(int code) {
		if(code == KeyEvent.VK_P) {
			gp.setGameState(gp.getPlayState());
		}
	}
	
	public void dialogueState(int code) {
		if(code == MouseEvent.BUTTON1 || code == KeyEvent.VK_SPACE) {
			gp.ui.getDialogueBox().advance();
		}
	}
	
	public void introState(int code) {
		if(code == KeyEvent.VK_ESCAPE || code == MouseEvent.BUTTON1) {
			gp.getIntro().skip();
		} else if(code == KeyEvent.VK_SPACE) {
			gp.getIntro().setFast(true);
		}
	}
	
	public void characterState(int code) {
		if(code == KeyEvent.VK_C)
			gp.setGameState(gp.getPlayState());
		if(code == KeyEvent.VK_Z) {
			gp.ui.setSlotRow(gp.ui.getSlotRow() - 1);
			if(gp.ui.getSlotRow() < 0)
				gp.ui.setSlotRow(4);
		}
		
		if(code == KeyEvent.VK_Q) {
			gp.ui.setSlotCol(gp.ui.getSlotCol() - 1);
			if(gp.ui.getSlotCol() < 0)
				gp.ui.setSlotCol(3);
		}
		
		if(code == KeyEvent.VK_S) {
			gp.ui.setSlotRow(gp.ui.getSlotRow() + 1);
			if(gp.ui.getSlotRow() > 4)
				gp.ui.setSlotRow(0);
		}
		
		if(code == KeyEvent.VK_D) {
			gp.ui.setSlotCol(gp.ui.getSlotCol() + 1);
			if(gp.ui.getSlotCol() > 3)
				gp.ui.setSlotCol(0);
		}
		
		if (code == KeyEvent.VK_LEFT) {
		    if (gp.ui.getInventoryIndex() <= 0) {  // On empêche de descendre en dessous de 0
		        gp.ui.setInventoryIndex(gp.getPlayer().getMaxInventoryPage() - 1);
		    } else {
		        gp.ui.setInventoryIndex(gp.ui.getInventoryIndex() - 1);
		    }
		}

		if (code == KeyEvent.VK_RIGHT) {
		    if (gp.ui.getInventoryIndex() >= gp.getPlayer().getMaxInventoryPage() - 1) {  
		        gp.ui.setInventoryIndex(0);
		    } else {
		        gp.ui.setInventoryIndex(gp.ui.getInventoryIndex() + 1);
		    }
		}
		if(code == MouseEvent.BUTTON1) {
			gp.getPlayer().selectItem();
		}
	}

	// ===================== Souris =====================
	
	@Override
	public void mousePressed(MouseEvent e) {
		gp.requestFocusInWindow(); // Le clavier continue de marcher après un clic
		if (e.getButton() != MouseEvent.BUTTON1) return;
		synchronized (gp.getGameLock()) {
			handleKeyPressed(MouseEvent.BUTTON1);
		}
	}
	
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		
		int code = e.getKeyCode();
		
		if(code == KeyEvent.VK_Z) {
			up = false;
		}
		
		if(code == KeyEvent.VK_S) {
			down = false;
		}
		
		if(code == KeyEvent.VK_Q) {
			left = false;
		}
		
		if(code == KeyEvent.VK_D) {
			right = false;
		}
		
		if(code == KeyEvent.VK_F) {
			shotKey = false;
		}
		
		if(code == KeyEvent.VK_SPACE) {
			guardKey = false;
			gp.getIntro().setFast(false);
		}
	}

	public boolean isUp() {
		return up;
	}

	public boolean isDown() {
		return down;
	}

	public boolean isLeft() {
		return left;
	}

	public boolean isRight() {
		return right;
	}

	public boolean isDebug() {
		return gp.getDebug().isEnabled();
	}
	
	public boolean isGuard() {
		return guardKey;
	}
	
	public boolean isEnter() {
		return click;
	}
	
	public boolean isShot() {
		return shotKey;
	}
	
	public void setEnter(boolean enter) {
		this.click = enter;
	}

	// Relâche toutes les touches de jeu (changement d'état, game over...)
	public void releaseAll() {
		up = down = left = right = click = shotKey = guardKey = false;
	}
}
