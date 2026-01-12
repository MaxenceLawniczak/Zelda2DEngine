package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Key extends Entity{
	
	public OBJ_Key(GamePanel gp) {
		super(gp);
		
		name = "Clé rouillée";
		down1 = setup("/objects/Key", gp.getTileSize()/2,gp.getTileSize()/2);
		description = "[" + name + "]\n"
								 + "Une petite clé en métal \n"
								 + "usée par le temps. Elle \n"
								 + "peut ouvrir toutes sortes \n"
								 + "de verrous... ou alors \n"
								 + "rien du tout.";
	}
}
