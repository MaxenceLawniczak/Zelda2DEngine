package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Tree_Branch extends Entity{

	public OBJ_Tree_Branch(GamePanel gp) {
		super(gp);
		
		type = typeBranch;
		name = "Branche d'Arbre";
		down1 = setup("/objects/TreeBranch", gp.getTileSize(),gp.getTileSize());
		attackValue = 1;
		description = "[" + name + "]\n"
								 + "Un morceau de bois tombé \n"
								 + "d’un arbre. Légère et \n"
								 + "fragile, elle peut tout de \n"
								 + "même servir d’arme \n"
								 + "improvisée pour ceux qui \n"
								 + "n’ont rien d’autre sous \n"
								 + "la main.";
	}
}
