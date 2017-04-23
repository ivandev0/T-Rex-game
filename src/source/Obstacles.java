package source;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Obstacles {

    LinkedList<Obstacle> obstacles;
    private float minGapCactus = 120,
                  minGapPter = 150,
                  gapCoefficient = 1.5f,
                  maxDuplication = 2,
                  minSpeedCactus = 0,
                  minSpeedPter = 8.5f;
    private int gap = 0;
    private int xSmall, ySmall, widthSmall, heightSmall, xLarge,  yLarge,  widthLarge,  heightLarge, xPtero, yPtero, widthPtero, heightPtero;

    Obstacles(int xSmall, int ySmall, int widthSmall, int heightSmall,
              int xLarge, int yLarge, int widthLarge, int heightLarge,
              int xPtero, int yPtero, int widthPtero, int heightPtero) {
        obstacles = new LinkedList<>();
        this.xSmall = xSmall;
        this.ySmall = ySmall;
        this.widthSmall = widthSmall;
        this.heightSmall = heightSmall;
        this.xLarge = xLarge;
        this.yLarge = yLarge;
        this.widthLarge = widthLarge;
        this.heightLarge = heightLarge;
        this.xPtero = xPtero;
        this.yPtero = yPtero;
        this.widthPtero = widthPtero;
        this.heightPtero = heightPtero;
    }

    void update(float delta, float currentSpeed) {
        if(obstacles.size() > 0){
            for (Obstacle ob : obstacles) {
                ob.update(delta, currentSpeed);
            }
            if(obstacles.getFirst().xPos < - Game.WIDTH * 4) {
                obstacles.removeFirst();
            }
        }
    }

    void createObstacles(float currentSpeed) {
        if(obstacles.size() > 0) {
            Obstacle last = obstacles.getLast();
            float minSpeed;
            if(last.xPos + last.width + gap < Game.WIDTH) {
                int duplicationCount = 0;
                int type;
                do {
                    type = 1 + (int) (Math.random() * 3);
                    for (Obstacle i: obstacles) {
                        duplicationCount = (i.type == type) ? duplicationCount + 1 : 0;
                    }
                    minSpeed = (type == 3) ? minSpeedPter : minSpeedCactus;
                } while (duplicationCount > maxDuplication  || currentSpeed < minSpeed);
                createObstacle(currentSpeed, type);
                getGap(currentSpeed, type, last.width);
            }
        } else {
            int type = 1 + (int) (Math.random() * 3);
            createObstacle(currentSpeed, type);
            if(obstacles.size() != 0) {
                getGap(currentSpeed, type, obstacles.getLast().width);
            }
        }
    }

    private void createObstacle (float currentSpeed, int type) {
        switch (type) {
            case 1: {
                int size = 1 + (int) (Math.random()*3);
                if (size > 1 && 4 > currentSpeed)
                    size = 1;
                obstacles.addLast(new Obstacle((int)((widthSmall*2 * size)*(0.5f * (size-1)) + xSmall), ySmall, widthSmall*2*size, heightSmall*2, Game.WIDTH + widthSmall*2*size, Game.HEIGHT - heightSmall, type, size, 0));
                break;
            }
            case 2: {
                int size = 1 + (int) (Math.random()*3);
                if (size > 1 && 7 > currentSpeed)
                    size = 1;
                obstacles.addLast(new Obstacle((int)((widthLarge*2 * size)*(0.5f * (size-1)) + xLarge), yLarge, widthLarge*2*size, heightLarge*2, Game.WIDTH + widthLarge*2*size, Game.HEIGHT - heightLarge, type, size, 0));
                break;
            }
            case 3: {
                if (currentSpeed > minSpeedPter) {
                    int height = 2 + (int) (Math.random() * 3);
                    float speedOffset = Math.random() > 0.5f ? 0.8f : -0.8f; //только для птеродактелей
                    obstacles.addLast(new Obstacle(xPtero, yPtero, widthPtero * 2, heightPtero * 2, Game.WIDTH + widthPtero*2, 25 * height + 10, type, 1, speedOffset));
                    break;
                }
            }
        }
    }

    private void getGap(float currentSpeed, int type, int width) {
        int minGap;
        if (type == 1 || type == 2)
            minGap = Math.round(width * currentSpeed + this.minGapCactus * 0.6f);
        else
            minGap = Math.round(width * currentSpeed + this.minGapPter * 0.6f);
        int maxGap = Math.round(minGap * gapCoefficient);
        gap = minGap + (int)(Math.random()* (maxGap - minGap + 1));
    }

    //public for debugging
    private class Obstacle extends GameObject {

        int type, size;
        //только для птеродактелей
        private float speedOffset;
        private float j = 0;
        private boolean upWing = true;
        private BufferedImage imageUp, imageDown;

        private Obstacle(int x, int y, int width, int height, float xPos, float yPos, int type, int size, float speedOffset) {
            super(x, y, width, height, xPos, yPos);
            this.type = type;
            this.size = size;
            this.speedOffset = speedOffset;
            if(type == 3) {
                GameObject temp = new GameObject(x + width, y, width, height, xPos, yPos);
                imageUp = this.image;
                imageDown = temp.image;
            }
            switch (type) {
                case 1: {
                    addBoxes(0, 7, 5, 27);
                    addBoxes(4, 0, /*6*/width/2 - 5 - 7, 34);
                    addBoxes(/*10*/width/2 - 7, 4, 7, 14);
                    break;
                }
                case 2: {
                    addBoxes(0, 12, 7, 38);
                    addBoxes(8, 0, /*7*/width/2 - 7 - 10, 49);
                    addBoxes(/*13*/width/2 - 10, 10, 10, 38);
                    break;
                }
                case 3: {
                    addBoxes(15, 15, 16, 5);
                    addBoxes(18, 21, 24, 6);
                    addBoxes(2, 14, 4, 3);
                    addBoxes(6, 10, 4, 7);
                    addBoxes(10, 8, 6, 9);
                    break;
                }
            }
        }

        void update(float delta, float currentSpeed) {
            switch (type) {
                case 1:
                case 2: {
                    xPos -= Math.floor(delta * currentSpeed * Game.FPS);
                    break;
                }
                case 3: {
                    xPos -= Math.floor(delta * Game.FPS * (currentSpeed + speedOffset));
                    j += delta;
                    if (j >= delta * 10) { //попробовать 6. попробовать 1000/6
                        if (upWing)
                            this.image = imageUp;
                        else
                            this.image = imageDown;
                        upWing = !upWing;
                        j = 0;
                    }
                    break;
                }
            }
        }


        //for debug
        /*public void drawCollisionBoxes(Graphics g) {
            this.setOriginalBox();
            //int boxesSize = downPressed ? 1 : boxes.size();
            g.setColor(Color.GREEN);
            //g.drawRect((int)originalBox.xCollider, (int)originalBox.yCollider, originalBox.widthCollider, originalBox.heightCollider);
            for (int i = 0; i < boxes.size(); i++) {
                BoxCollider boxTRex = originalBox.createAdjustedCollisionBox(boxes.get(i));
                g.drawRect((int)boxTRex.xCollider, (int)boxTRex.yCollider, boxTRex.widthCollider, boxTRex.heightCollider);
            }
        }*/
    }
}


