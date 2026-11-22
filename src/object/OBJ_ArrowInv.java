package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_ArrowInv extends Entity{

	public OBJ_ArrowInv(GamePanel gp, int amount) {
		super(gp);
		
		this.amount = amount;
		this.maxAmount = 500;
		
		type = typeArrow;
		name = "Flèche";
		down1 = setup("/objects/inventory/ArrowInventory", gp.getTileSize(),gp.getTileSize());
		//attackValue = 1;
		description = "[" + name + "]\n"
								 + "Une simple flèche en \n"
								 + "bois. \n";
	}
}
