package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener{

	private GamePanel gp;
	private boolean up, down, left, right, enter;
	
	//Debug
	private boolean debug;
	private boolean shotKey;
	
	public KeyHandler(GamePanel gp) {
		this.gp = gp;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		int code = e.getKeyCode();
		
		//Title
		if(gp.getGameState() == gp.getTitleState()) {
			if(code == KeyEvent.VK_ESCAPE)
				System.exit(0);
			titleState(code);
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
		if(code == KeyEvent.VK_UP) {
			gp.ui.setCommandNum(gp.ui.getCommandNum()-1);
			if(gp.ui.getCommandNum()<0) {
				gp.ui.setCommandNum(1);
			}
		}
		if(code == KeyEvent.VK_DOWN) {
			gp.ui.setCommandNum(gp.ui.getCommandNum()+1);
			if(gp.ui.getCommandNum()>1) {
				gp.ui.setCommandNum(0);
			}
		}
		if(code == KeyEvent.VK_ENTER) {
			if(gp.ui.getCommandNum() == 0) {
				gp.setGameState(gp.getPlayState());
				gp.retry();
			}
			if(gp.ui.getCommandNum() == 1) {
				gp.setGameState(gp.getTitleState());
				gp.restart();
			}
		}
	}

	public void titleState(int code) {
		if(code == KeyEvent.VK_Z) {
			gp.ui.setCommandNum(gp.ui.getCommandNum() - 1);
			if(gp.ui.getCommandNum() < 0) 
				gp.ui.setCommandNum(2);
		}
		
		if(code == KeyEvent.VK_S) {
			gp.ui.setCommandNum(gp.ui.getCommandNum() + 1);
			if(gp.ui.getCommandNum() > 2) 
				gp.ui.setCommandNum(0);
		}
		
		if(code == KeyEvent.VK_ENTER) {
			if(gp.ui.getCommandNum() == 0)
				gp.setGameState(gp.getPlayState());
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
		
		if(code == KeyEvent.VK_ENTER) {
			enter = true;
		}
		
		if(code == KeyEvent.VK_F) {
			shotKey = true;
		}
		
		//Debug
		if(code == KeyEvent.VK_T) {
			debug = !debug;
		}
		
		if(code == KeyEvent.VK_R) 
			switch(gp.getCurrentMap()) {
			case 0: gp.getTileM().loadMap("/maps/map01.txt", 0); break;
			case 1: gp.getTileM().loadMap("/maps/map02.txt", 1); break;
			
			}
			
		if(code == KeyEvent.VK_ESCAPE)
			System.exit(0);
	}
	
	public void pauseState(int code) {
		if(code == KeyEvent.VK_P) {
			gp.setGameState(gp.getPlayState());
		}
	}
	
	public void dialogueState(int code) {
		if(code == KeyEvent.VK_ENTER) {
			gp.setGameState(gp.getPlayState());
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
		if(code == KeyEvent.VK_ENTER) {
			gp.getPlayer().selectItem();
		}
	}

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
		return debug;
	}
	
	public boolean isEnter() {
		return enter;
	}
	
	public boolean isShot() {
		return shotKey;
	}
	
	public void setEnter(boolean enter) {
		this.enter = enter;
	}
}
