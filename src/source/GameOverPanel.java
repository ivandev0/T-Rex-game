package source;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GameOverPanel {

    public Image restartButton, text;
    private int textSourceX = 0, textSourceY = 13*2, textSourceWidth = 191*2, textSourceHeight = 11*2, restartSourceX = 2, restartSourceY = 2, restartSourceWidth = 36*2, restartSourceHeight = 32*2;
    int textTargetX, textTargetY, textTargetWidth, textTargetHeight, restartTargetX, restartTargetY, restartTargetWidth = 36, restartTargetHeight = 32;

    public GameOverPanel() {
        int centerX = Game.WIDTH/2;

        textTargetX += centerX - textSourceWidth/4;
        textTargetY += (Game.HEIGHT - 25) / 3;
        textSourceX += 954;
        textSourceY += 2;
        textTargetWidth = textSourceWidth/2;
        textTargetHeight = textSourceHeight/2;

        restartTargetX = centerX - restartSourceWidth/4;
        restartTargetY = Game.HEIGHT/2;

        text = getImage(textSourceX, textSourceY, textSourceWidth, textSourceHeight);
        restartButton = getImage(restartSourceX, restartSourceY, restartSourceWidth, restartSourceHeight);
    }

    private Image getImage(int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
        BufferedImage sourceImage = null;
        try {
            sourceImage = ImageIO.read(getClass().getResource(Game.path));
        } catch (IOException e) {
            System.out.println("Ошибка открытия файла " + Game.path);
            System.exit(3);
        }
        return sourceImage.getSubimage(sourceX, sourceY, sourceWidth, sourceHeight);
    }
}
