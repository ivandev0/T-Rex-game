package source;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class DistanceMeter extends JPanel {
    public LinkedList<Image> numbers;  //список содержащий набор изображений с цифрами
    private int achievement = 100, //дистанция при которой учитывается дотижение
            flashIteration = 3, //количество миганий
            iteration = 0, //текущее количество миганий
            maxDigits = 5; //макс количество цифр в строке
    private float flashDuration = 250, //время мигания (мс)
            flashTimer = 0; //текщее время пройденное от начала мигания
    //параметры для взятия отдельного изображения
    private int width, height, destWidth, xPosSource, yPosSource, highScoreWidth;
    //параметры хранящие информацию о текущем счёте
    private int score = 0, highScore = 0;
    //флаги для отрисовки
    private boolean highScorePaint = false, paint = true, isAchievement = false;
    //итоговые массивы счета
    private int[] scoreNumbers, highScoreNumbers, achievementNumbers;

    public DistanceMeter(int width, int height, int destWidth, int xPosSource, int yPosSource, int highScoreWidth) {
        this.width = width;
        this.height = height;
        this.destWidth = destWidth;
        this.xPosSource = xPosSource;
        this.yPosSource = yPosSource;
        this.highScoreWidth = highScoreWidth;
        numbers = new LinkedList<>();
        for (int i = 0; i <= 10; i++)
            numbers.add(getImageNumbers(i));

        scoreNumbers = new int[maxDigits];
        highScoreNumbers = new int[maxDigits];
        achievementNumbers = new int[maxDigits];
    }

    private Image getImageNumbers (int value) {
        int sourceX = width * 2 * value;
        int sourceY = 0;
        int sourceWidth = width * 2;
        int sourceHeight = height * 2;

        sourceX += xPosSource;
        sourceY += yPosSource;

        if(value == 10) {
            sourceWidth = highScoreWidth;
            sourceX = width * 2 * value + 953;
        }

        BufferedImage sourceImage = null;
        try {
            sourceImage = ImageIO.read(getClass().getResource(Game.path));
        } catch (IOException e) {
            System.out.println("Ошибка открытия файла " + Game.path);
            System.exit(3);
        }
        return sourceImage.getSubimage(sourceX, sourceY, sourceWidth, sourceHeight);
    }

    public boolean update(float delta, float distance) {
        boolean playSound = false;
        paint = true;
        score = Math.round(distance);
        scoreNumbers = fillArray();
        //если макс счёт меньше текущего
        if(score > highScore)
            highScore = score;
        //условие достижения
        if(score > 1 && (score - 1) % achievement == 0) {
            isAchievement = true;
            score--;
            achievementNumbers = fillArray();
        }
        //воспроизведение звука
        if(flashTimer == 0 && iteration == 0 && isAchievement)
            playSound = true;
        //если было достижение то начинаем процесс мигания
        if(isAchievement) {
            if (iteration <= flashIteration) {
                flashTimer += delta * 1000;
                if (flashTimer < flashDuration) {
                    paint = false;
                }
                else if (flashTimer > flashDuration * 2) {
                    flashTimer = 0;
                    iteration++;
                }
            } else {
                isAchievement = false;
                score++;
                flashTimer = 0;
                iteration = 0;
            }
        }
        return playSound;
    }

    public void drawHighScore () {
        if(highScore == 0 || score < highScore)
            return;
        highScorePaint = true;
        highScoreNumbers = fillArray();

        isAchievement = false;
        paint = true;
        score++;
        flashTimer = 0;
        iteration = 0;
    }

    private int[] fillArray() {
        int[] numbers = new int[maxDigits];
        Arrays.fill(numbers, 0);
        int temp = score;
        int i = 4;
        while (temp != 0) {
            numbers[i] = temp % 10;
            temp /= 10;
            i--;
        }
        return numbers;
    }

    public void paint(Graphics g) {
        int targetX;
        int targetY;
        int targetWidth;
        int targetHeight;

        int[] numbersTemp = !isAchievement ? scoreNumbers : achievementNumbers;
        //обычная отрисовка
        if(paint) {
            for (int i = 0; i < maxDigits; i++) {
                targetX = i * destWidth + Game.WIDTH - destWidth * maxDigits;
                targetY = 10;
                targetWidth = width;
                targetHeight = height;
                g.drawImage(numbers.get(numbersTemp[i]), targetX, targetY, targetWidth, targetHeight, null);
            }
        }
        //отрисовка макс счёта
        if (highScorePaint) {
            targetX = Game.WIDTH - destWidth * (maxDigits * 2 + 4);
            targetY = 10;
            targetWidth = highScoreWidth / 2 - 1;
            targetHeight = height;
            g.drawImage(numbers.get(10), targetX, targetY, targetWidth, targetHeight, null);
            for (int i = 0; i < maxDigits; i++) {
                targetX = i * destWidth + Game.WIDTH - destWidth * (maxDigits * 2 + 1);
                targetY = 10;
                targetWidth = width;
                targetHeight = height;
                g.drawImage(numbers.get(highScoreNumbers[i]), targetX, targetY, targetWidth, targetHeight, null);
            }
        }
    }
}
