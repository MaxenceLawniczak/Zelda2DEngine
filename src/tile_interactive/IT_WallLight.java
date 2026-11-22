package tile_interactive;

import java.awt.image.BufferedImage;

import main.GamePanel;

public class IT_WallLight extends InteractiveTile {

    private String direction;
    protected int animationTimer = 0;
    protected int animationSpeed = 12; // tu peux ajuster la vitesse
    protected int frameIndex = 0;
    protected int animationDirection = 1;
    protected BufferedImage[] animationFrames;


    public IT_WallLight(GamePanel gp, int col, int row, String direction) {
        super(gp, col, row);
        this.direction = direction;

        this.worldX = gp.getTileSize() * col;
        this.worldY = gp.getTileSize() * row;

        loadAnimationImages();
        down1 = animationFrames[0]; // L'image affichée par défaut (obligatoire pour l'affichage)
        destructible = false;
    }

    private void loadAnimationImages() {
        int tileSize = gp.getTileSize();

        switch (direction.toLowerCase()) {
            case "up":
                animationFrames = new BufferedImage[] {
                    setup("/tiles_interactive/lights/WallLightUp0", tileSize, tileSize),
                    setup("/tiles_interactive/lights/WallLightUp1", tileSize, tileSize),
                    setup("/tiles_interactive/lights/WallLightUp2", tileSize, tileSize),
                    setup("/tiles_interactive/lights/WallLightUp3", tileSize, tileSize),
                };
                break;

            case "down":
                animationFrames = new BufferedImage[] {
                        setup("/tiles_interactive/lights/WallLightDown0", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightDown1", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightDown2", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightDown3", tileSize, tileSize),
                };
                break;

            case "left":
                animationFrames = new BufferedImage[] {
                        setup("/tiles_interactive/lights/WallLightLeft0", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightLeft1", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightLeft2", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightLeft3", tileSize, tileSize),
                };
                break;

            case "right":
                animationFrames = new BufferedImage[] {
                        setup("/tiles_interactive/lights/WallLightRight0", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightRight1", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightRight2", tileSize, tileSize),
                        setup("/tiles_interactive/lights/WallLightRight3", tileSize, tileSize),
                };
                break;

            default:
                animationFrames = new BufferedImage[] {
                    setup("/tiles_interactive/lights/WallLightUp0", tileSize, tileSize)
                };
                break;
        }
    }

    @Override
    public void update() {
        animationTimer++;
        if (animationTimer >= animationSpeed) {
            animationTimer = 0;

            // Avance ou recule dans l'animation selon la direction
            frameIndex += animationDirection;

            // Inverser la direction si on atteint une extrémité
            if (frameIndex == animationFrames.length - 1 || frameIndex == 0) {
                animationDirection *= -1;
            }

            down1 = animationFrames[frameIndex];
        }
    }

}
