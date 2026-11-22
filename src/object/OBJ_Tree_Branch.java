package object;

import entity.Entity;
import entity.Player;
import tile_interactive.InteractiveTile;
import main.GamePanel;

public class OBJ_Tree_Branch extends Entity{

	public OBJ_Tree_Branch(GamePanel gp) {
		super(gp);
		
		type = typeBranch;
		name = "Branche d'Arbre";
		down1 = setup("/objects/weapons/TreeBranch", gp.getTileSize(),gp.getTileSize());
		attackValue = 1;
		attackArea.width = gp.getTileSize() * 27 / 32;
		attackArea.height = gp.getTileSize() * 27 / 32;
		description = "[" + name + "]\n"
								 + "Un morceau de bois tombé \n"
								 + "d’un arbre. Légère et \n"
								 + "fragile, elle peut tout de \n"
								 + "même servir d’arme \n"
								 + "improvisée pour ceux qui \n"
								 + "n’ont rien d’autre sous \n"
								 + "la main.";

		// Durabilité et spécificité
		maxDurability = 12;
		durability = maxDurability;
		specialName = "Récolte";
		specialDescription = "Couper de l'herbe ou un buisson donne deux chances de trouver un objet.";
	}

	@Override
	public void onTileCut(Player player, InteractiveTile tile) {
		tile.checkDrop(); // Deuxième chance de butin
	}
}
