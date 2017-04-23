package source;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class GameObject extends JPanel {
    int width, height;
    float xPos, yPos;
    private int sourceX, sourceY;
    BufferedImage image;
    LinkedList<BoxCollider> boxes;
    BoxCollider originalBox;

    public GameObject(int x, int y, int width, int height, float xPos, float yPos) {
        this.sourceX = x;
        this.sourceY = y;
        this.width = width/2;
        this.height = height/2;
        this.xPos = xPos;
        this.yPos = yPos;
        getImage();
        boxes = new LinkedList<>();
    }
    private void getImage() {
        BufferedImage sourceImage = null;
        try {
            sourceImage = ImageIO.read(getClass().getResource(Game.path));
        } catch (IOException e) {
            System.out.println("Ошибка открытия файла " + Game.path);
            System.exit(3);
        }
        image = sourceImage.getSubimage(sourceX, sourceY, width*2, height*2);
    }
    @Override
    public void paint(Graphics g) {
        g.drawImage(image, (int)xPos, (int)yPos, width, height, null);
    }

    //collisions
    void addBoxes (int x, int y, int width, int height) {
        boxes.push(new BoxCollider(x, y, width, height));
    }

    void setOriginalBox() { //+1 т.к. 1 пиксель белый
        originalBox = new BoxCollider(xPos + 1, yPos + 1, width - 2, height - 2);
    }

    public class BoxCollider {

        float xCollider, yCollider;
        int widthCollider, heightCollider;

        public BoxCollider(float xCollider, float yCollider, int widthCollider, int heightCollider) {
            this.xCollider = xCollider;
            this.yCollider = yCollider;
            this.widthCollider = widthCollider;
            this.heightCollider = heightCollider;
        }

        boolean boxCompare(BoxCollider ob) {
            boolean crashed = false;
            if(xCollider < ob.xCollider + ob.widthCollider &&
                    xCollider + widthCollider > ob.xCollider &&
                    yCollider < ob.yCollider + ob.heightCollider &&
                    yCollider + heightCollider > ob.yCollider)
                crashed = true;
            return crashed;
        }

        BoxCollider createAdjustedCollisionBox(BoxCollider ob2) {
            return new BoxCollider(xCollider + ob2.xCollider, yCollider + ob2.yCollider, ob2.widthCollider, ob2.heightCollider);
        }
    }



}
