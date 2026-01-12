package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Boko_Bow extends Entity{

	public OBJ_Boko_Bow(GamePanel gp) {
		super(gp);

		type = typeBokoBow;
		name = "Arc Boko";
		down1 = setup("/objects/BokoBow", gp.getTileSize(),gp.getTileSize());
		//defenseValue = 2;
		description = "[" + name + "]\n"
								 + "Un arc de fortune façonné \n"
								 + "dans du bois brut, utilisé \n"
								 + "par les combattants les \n"
								 + "moins expérimentés. Simple \n"
								 + "mais efficace, il permet \n"
								 + "d'attaquer à distance avec \n"
								 + "agilité.";
	
	}
}
