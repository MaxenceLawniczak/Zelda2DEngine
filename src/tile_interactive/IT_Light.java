package tile_interactive;

import java.awt.image.BufferedImage;

import main.GamePanel;

public class IT_Light extends InteractiveTile {

    protected int animationTimer = 0;
    protected int animationSpeed = 12;
    protected int frameIndex = 0;
    protected int animationDirection = 1; // +1 vers l'avant, -1 vers l'arrière
    protected BufferedImage[] animationFrames;

    public IT_Light(GamePanel gp, int col, int row) {
        super(gp, col, row);

        this.worldX = gp.getTileSize() * col;
        this.worldY = gp.getTileSize() * row;

        loadAnimationImages();
        down1 = animationFrames[0]; // image par défaut
        destructible = false;
    }

    private void loadAnimationImages() {
        int tileSize = gp.getTileSize();

        animationFrames = new BufferedImage[] {
            setup("/tiles_interactive/lights/Light0", tileSize, tileSize),
            setup("/tiles_interactive/lights/Light1", tileSize, tileSize),
            setup("/tiles_interactive/lights/Light2", tileSize, tileSize),
            setup("/tiles_interactive/lights/Light3", tileSize, tileSize),
        };
    }

    @Override
    public void update() {
        animationTimer++;
        if (animationTimer >= animationSpeed) {
            animationTimer = 0;

            frameIndex += animationDirection;

            if (frameIndex == animationFrames.length - 1 || frameIndex == 0) {
                animationDirection *= -1; // Inverse le sens
            }

            down1 = animationFrames[frameIndex];
        }
    }
}
