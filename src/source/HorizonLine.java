package source;

import java.awt.*;
import java.util.LinkedList;

public class HorizonLine {

    public Land lands;
    public LinkedList<Cloud> clouds;

    public HorizonLine(int xLand, int yLand, int widthLand, int heightLand, int xCloud, int yCloud, int widthCloud, int heightCloud) {
        lands = new Land(xLand, yLand, widthLand, heightLand, 0, Game.HEIGHT - heightLand/2);
        clouds = new LinkedList<>();
        clouds.push(new Cloud(xCloud, yCloud, widthCloud, heightCloud, Game.WIDTH + widthCloud/2, 0));
    }

    public void update(float delta, float currentSpeed) {
        //update lands
        lands.update(delta, currentSpeed);
        //update clouds
        for (Cloud curCloud : clouds) {
            curCloud.xPos -= curCloud.update(delta, currentSpeed);
        }
        Cloud lastCloud = clouds.getLast();
        float cloudGap = 100 + (int) (Math.random() * 250);
        if (clouds.size() < lastCloud.maxCloudsCount && (Game.WIDTH - lastCloud.xPos) > cloudGap && lastCloud.freqCloud > Math.random() * 10) {
            clouds.addLast(new Cloud(lastCloud.xSource, lastCloud.ySource, lastCloud.width * 2, lastCloud.height * 2, Game.WIDTH + lastCloud.width, 0));
        }
        Cloud firstCloud = clouds.getFirst();
        if (firstCloud.xPos < -firstCloud.width) {
            clouds.removeFirst();
        }
    }

    public int getLandHeight() {
        return  lands.land1.height;
    }

    public void paint(Graphics g, boolean waiting, boolean running) {
        lands.paint(g, waiting, running);

        for (int i = 0; i < clouds.size(); i++) {
            Cloud curCloud = clouds.get(i);
            curCloud.paint(g);
        }
    }

    private class Cloud extends GameObject {

        public int maxCloud = 61, minCloud = 20, maxCloudsCount = 6;
        public float speedCloud = 0.2f, freqCloud = 0.2f;
        private String path;
        private int xSource, ySource;

        public Cloud(int x, int y, int width, int height, float xPos, float yPos) {
            super(x, y, width, height, xPos, yPos);
            this.yPos = minCloud + (int) (Math.random() * maxCloud);

            xSource = x;
            ySource = y;
        }

        public float update(float delta, float currentSpeed) {
            float speedCloud = this.speedCloud / (delta * currentSpeed * 2);

            return speedCloud;
        }

    }

    private class Land extends GameObject {

        public GameObject land1, land2;

        public Land(int x, int y, int width, int height, float xPos, float yPos) {
            super(x, y, width, height, xPos, yPos);
            land1 = this;
            land2 = new GameObject(x, y, width, height, land1.width, yPos);
        }

        public void update(float delta, float currentSpeed) {
            land1.xPos -= Math.floor(currentSpeed * delta * Game.FPS);
            land2.xPos -= Math.floor(currentSpeed * delta * Game.FPS);
            if (land1.xPos <= -land1.width)
                land1.xPos = land2.xPos + land1.width;
            if (land2.xPos <= -land1.width)
                land2.xPos = land1.xPos + land1.width;
        }

        public void paint(Graphics g, boolean waiting, boolean running) {
            if(!waiting) {
                if (!running)
                    land1.paint(g);
                if (running) {
                    land1.paint(g);
                    land2.paint(g);
                }
            } else {
                land1.paint(g);
            }

        }
    }
}




