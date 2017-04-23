package source;

import java.awt.*;

public class TRex extends GameObject{

    private GameObject TRexLeft, TRexRight,
            TRexSpace, TRexBlink, TRexCrashed,
            TRexDuck1, TRexDuck2;
    private float i = 0;
    private boolean left = true;

    private float gravity = 0.6f, dropVelocity = -5, velocity = -10;
    private int distance = 63;

    private float blinkDelay = (float) Math.ceil(Math.random() * 7000);
    private boolean blink = false;

    boolean halfJump, spacePressed, downPressed;
    private boolean jumping = false;
    float landWaitX = 45;
    int speedDropCoeff = -3;


    public TRex(int xRex, int yRex, int width, int height, int yDuck, int duckWidth, int duckHeight, int duckXPos) {
        super(xRex, yRex, width, height, 0, Game.HEIGHT - height/2);
        TRexSpace = this;
        TRexBlink = new GameObject(xRex + width, yRex, width, height, 0, Game.HEIGHT - height/2);
        TRexCrashed = new GameObject(xRex+width*5, yRex, width, height, 0, Game.HEIGHT - height/2);
        TRexRight = new GameObject(xRex+width*2, yRex, width, height, 0, Game.HEIGHT - height/2);
        TRexLeft = new GameObject(xRex+width*3, yRex, width, height, 0, Game.HEIGHT - height/2);
        TRexDuck1 = new GameObject(xRex + width*6, yDuck, duckWidth, duckHeight, duckXPos, Game.HEIGHT-duckHeight/2);
        TRexDuck2 = new GameObject(xRex + width*6 + duckWidth, yDuck, duckWidth, duckHeight, duckXPos, Game.HEIGHT-duckHeight/2);

        addBoxes(22, 0, 17, 16);
        addBoxes(1, 18, 30, 9);
        addBoxes(10, 35, 14, 8);
        addBoxes(1, 24, 29, 5);
        addBoxes(5, 30, 21, 4);
        addBoxes(9, 34, 15, 4);
        addBoxes(1, 18, 55, 25); //ducking

    }

    boolean updateJump(float delta, float currentSpeed) {
        if(!jumping) {
            velocity = velocity - currentSpeed / 10;
            jumping = true;
        }
        TRexSpace.yPos += Math.round(velocity );
        velocity += gravity * speedDropCoeff;
        if(TRexSpace.yPos <= Game.HEIGHT-TRexSpace.height - distance && velocity < dropVelocity) { //73 = MAX_HEIGHT
            velocity = dropVelocity; //отвечает за высоту прыжка
        }
        if(halfJump && TRexSpace.yPos >= Game.HEIGHT-TRexSpace.height - 50 && TRexSpace.yPos <= Game.HEIGHT-TRexSpace.height - 45 && velocity < dropVelocity) {
            velocity = dropVelocity;
            halfJump = false;
        }
        if(TRexSpace.yPos > Game.HEIGHT-TRexSpace.height) {
            TRexSpace.yPos = Game.HEIGHT-TRexSpace.height;
            velocity = -10; //начальная скорость
            spacePressed = false;
            halfJump = false;
            jumping = false;
            return false;
        }
        return true;
    }

    void updateBlink(float delta) {
        i += delta;
        if (i > blinkDelay / 1000 && !blink) {
            blink = true;
        } else if (i > (blinkDelay / 1000) + delta * 10) {
            i = 0;
            blink = false;
            blinkDelay = (float) Math.ceil(Math.random() * 7000);
        }
    }

    void updateWalk(float delta) {
        i += delta;
        if (i >= delta * 5) {
            left = !left;
            i = 0;
        }
    }

    boolean updateBegin(float delta) {
        if (TRexLeft.xPos <= 50 / 2 || TRexRight.xPos <= 50 / 2) {
            TRexLeft.xPos = TRexRight.xPos = TRexSpace.xPos = TRexCrashed.xPos += delta * Game.FPS;
            landWaitX += (float) (Game.WIDTH - 45) / (float) (50 / 2 / (delta * Game.FPS)); //(delta * FPS) = 1
            return false;
        } else {
            TRexLeft.xPos = TRexRight.xPos = TRexSpace.xPos = TRexCrashed.xPos = 25;
            return true;
        }
    }

    boolean checkHeight () {
        if(TRexSpace.yPos >= Game.HEIGHT-TRexSpace.height - 50) {
            return true;
        } else
            return false;
    }

    boolean checkCollision(GameObject ob) {
        this.setOriginalBox();
        ob.setOriginalBox();
        if(originalBox.boxCompare(ob.originalBox)) {
            int first = downPressed ? 0 : 1;
            int last = downPressed ? 1 : boxes.size();
            for (int i = first; i < last; i++) {
                for (int j = 0; j < ob.boxes.size(); j++) {
                    BoxCollider boxTRex = originalBox.createAdjustedCollisionBox(boxes.get(i));
                    BoxCollider boxObstacle = ob.originalBox.createAdjustedCollisionBox(ob.boxes.get(j));
                    if (boxTRex.boxCompare(boxObstacle)) {
                        int offset = downPressed ? 1 : 0;
                        TRexCrashed.xPos += offset;
                        TRexCrashed.yPos = yPos;

                        velocity = -10; //начальная скорость
                        jumping = false;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void paint(Graphics g, boolean waiting, boolean running, boolean gameover) {
        if(gameover)
            TRexCrashed.paint(g);
        else if(!waiting) {
            if (!running) {
                if (left && !spacePressed)
                    TRexLeft.paint(g);
                else if (!left && !spacePressed)
                    TRexRight.paint(g);
                else if (spacePressed)
                    TRexSpace.paint(g);
            }
            if (running && !spacePressed) {
                if(!downPressed) {
                    if (left)
                        TRexLeft.paint(g);
                    else
                        TRexRight.paint(g);
                } else {
                    if (left)
                        TRexDuck1.paint(g);
                    else
                        TRexDuck2.paint(g);
                }
            }
            if (running && spacePressed) {
                TRexSpace.paint(g);
            }
        } else {
            if(!blink)
                TRexSpace.paint(g);
            else
                TRexBlink.paint(g);
        }
    }

    //debugging
    /*public void drawCollisionBoxes(Graphics g) {
        this.setOriginalBox();
        ///g.drawRect((int)originalBox.xCollider, (int)originalBox.yCollider, originalBox.widthCollider, originalBox.heightCollider);
        int first = downPressed ? 0 : 1;
        int last = downPressed ? 1 : boxes.size();
        g.setColor(Color.GREEN);
        for (int i = first; i < last; i++) {
            BoxCollider boxTRex = originalBox.createAdjustedCollisionBox(boxes.get(i));
            g.drawRect((int)boxTRex.xCollider, (int)boxTRex.yCollider, boxTRex.widthCollider, boxTRex.heightCollider);
        }
    }*/
}
