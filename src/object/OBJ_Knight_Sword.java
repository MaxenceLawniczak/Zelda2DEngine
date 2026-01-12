package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Knight_Sword extends Entity{

	public OBJ_Knight_Sword(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Epée de chevalier";
		down1 = setup("/objects/KnightSword", gp.getTileSize(),gp.getTileSize());
		attackValue = 2;
		description = "[" + name + "]\n"
								 + "Une épée élégante forgée \n"
								 + "par des maîtres forgerons. \n"
								 + "Solide et bien équilibrée, \n"
								 + "elle est parfaite pour un \n"
								 + "chevalier en quête de \n"
								 + "gloire et d'aventures.";

	}
}
