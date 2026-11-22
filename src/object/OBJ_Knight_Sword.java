package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Knight_Sword extends Entity{

	public OBJ_Knight_Sword(GamePanel gp) {
		super(gp);
		
		type = typeSword;
		name = "Epée de chevalier";
		down1 = setup("/objects/weapons/KnightSword", gp.getTileSize(),gp.getTileSize());
		attackValue = 3;
		attackArea.width = gp.getTileSize() * 30 / 32;
		attackArea.height = gp.getTileSize() * 30 / 32;
		description = "[" + name + "]\n"
								 + "Une épée élégante forgée \n"
								 + "par des maîtres forgerons. \n"
								 + "Solide et bien équilibrée, \n"
								 + "elle est parfaite pour un \n"
								 + "chevalier en quête de \n"
								 + "gloire et d'aventures.";


		// Durabilité et spécificité
		maxDurability = 45;
		durability = maxDurability;
		specialName = "Fiable";
		specialDescription = "Forgée avec soin : elle s'use deux fois moins vite que les autres.";
	}

	@Override
	public float wearChance() {
		return 0.5f;
	}
}
