package source;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.*;

public class Game extends Canvas implements Runnable{
    static float FPS = 60;
    //булевские переменные статуса игры
    private boolean run = false, waiting = true, gameover = false, running = false;
    //настройки главного окна
    static int WIDTH = 600; //ширина
    static int HEIGHT = 150; //высота
    private static String NAME = "T-Rex Game"; //заголовок окна

    private static String audioPath = "";
    static String path = "";
    //игровые параметры
    private float speed = 6, maxSpeed = 13, currentSpeed = 6, acceleration = 0.001f, distance = 0,
            coefficient = 0.025f; //коэффициент для измерения дистанции
    private long gameOverTimer = 0;
    //дополнительные классы
    private Obstacles obstacles;
    private TRex tRex;
    private HorizonLine horizon;
    private GameOverPanel gameOverPanel;
    private DistanceMeter distanceMeter;



    public static void main(String[] args) {
        Game game = new Game();
        game.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JFrame frame = new JFrame(Game.NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //выход из приложения по нажатию клавиши ESC
        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER); //добавляем холст на наш фрейм
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

        path = "/asset/img/sprites.png";
        audioPath = "/asset/audio/";

        game.start();
    }

    private void start() {
        run = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1000000000;
        float delta = 0.0f;

        init();
        //до тех пор пока поток запущен
        while(run) {

            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while(delta >= (float)1/FPS)
            {
                update(delta); //обновление параметров сцены
                delta-=(float)1/FPS;
            }
            render(); //отрисовка сцены
        }
    }

    private void init() {
        //инициализация всех параметров
        addKeyListener(new KeyInput());
        addMouseListener(new MouseInput());
        //комментарии к каждому соответсвубщему парамтру см. внутри соответсвующего класса
        tRex = new TRex(1338, 2, 44*2, 47*2, 40, 59*2, 27*2, 25);
        horizon = new HorizonLine(2, 104, 1200*2, 13*2, 166, 2, 46 * 2, 14 * 2);
        obstacles = new Obstacles(446, 2, 17, 35, 652, 2, 25, 50, 260, 2, 46, 40);
        gameOverPanel = new GameOverPanel();
        distanceMeter = new DistanceMeter(10, 13, 11, 953, 1, 39);
    }

    private void update(float delta) {
        if(!gameover) {
            if (!waiting && !running) {
                running = tRex.updateBegin(delta);
            }

            if (!waiting) {
                tRex.updateWalk(delta);
                distance += currentSpeed * delta * FPS * coefficient;
                if(distanceMeter.update(delta, distance))
                    playSound("achievement");
            } else if (waiting && !tRex.spacePressed) {   //blinking
                tRex.updateBlink(delta);
            }

            if (tRex.spacePressed) {
                boolean temp = tRex.updateJump(delta, currentSpeed);
                if (waiting)
                    waiting = temp;
            }

            if (running) {
                if (currentSpeed < maxSpeed)
                    currentSpeed += acceleration;
                horizon.update(delta, currentSpeed);
                if (distance > 30) {
                    //create obstacles
                    obstacles.createObstacles(currentSpeed);
                    //update obstacles
                    obstacles.update(delta, currentSpeed);
                }
                for (int i = 0; i < obstacles.obstacles.size(); i++) {
                    gameover = tRex.checkCollision(obstacles.obstacles.get(i));
                    if(gameover) {
                        distanceMeter.drawHighScore();
                        playSound("dying");
                        gameOverTimer = System.currentTimeMillis();
                        break;
                    }
                }
            }
        }
    }

    private void playSound (String sound) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(getClass().getResource(audioPath + sound + ".wav")));
            clip.start();
        } catch (LineUnavailableException e) {
            System.out.println("Неверный путь к файлу " + sound);
            System.exit(1);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Формат файла " + sound + " не поддерживается");
            System.exit(2);
        } catch (IOException e) {
            System.out.println("Ошибка воспроизведения файла " + sound);
            System.exit(3);
        }
    }

    private void restart() {
        if(System.currentTimeMillis() - gameOverTimer > 500) {
            running = true;
            gameover = false;
            tRex.xPos = 25;
            tRex.yPos = HEIGHT - tRex.height;
            tRex.spacePressed = false;
            tRex.downPressed = false;
            tRex.halfJump = false;
            currentSpeed = speed;
            obstacles.obstacles.clear();
            distance = 0;
        }
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(2); //создаем BufferStrategy для нашего холста
            requestFocus();
            return;
        }

        Graphics g = bs.getDrawGraphics(); //получаем Graphics из созданной нами BufferStrategy
        g.setColor(Color.decode("#f7f7f7")); //выбрать цвет  #f7f7f7 - Gray
        g.fillRect(0, 0, getWidth(), getHeight()); //заполнить прямоугольник

        horizon.paint(g, waiting, running);
        if(!waiting) {
            distanceMeter.paint(g);
            if (!running) {
                g.setColor(Color.decode("#f7f7f7"));
                g.fillRect((int) tRex.landWaitX, HEIGHT - horizon.getLandHeight(), WIDTH, horizon.getLandHeight());
            }
            if (running) {
                for (int i = 0; i < obstacles.obstacles.size(); i++) {
                    GameObject temp = obstacles.obstacles.get(i);
                    temp.paint(g);
                }
            }
        } else {
            g.setColor(Color.decode("#f7f7f7"));
            g.fillRect((int) tRex.landWaitX, HEIGHT - horizon.getLandHeight(), WIDTH, horizon.getLandHeight());
        }
        tRex.paint(g, waiting, running, gameover);

        if(gameover) {
            g.drawImage(gameOverPanel.restartButton, gameOverPanel.restartTargetX, gameOverPanel.restartTargetY, gameOverPanel.restartTargetWidth, gameOverPanel.restartTargetHeight, null);
            g.drawImage(gameOverPanel.text, gameOverPanel.textTargetX, gameOverPanel.textTargetY, gameOverPanel.textTargetWidth, gameOverPanel.textTargetHeight, null);
        }

        //for debug
        /*tRex.drawCollisionBoxes(g);
        for (Obstacles.Obstacle ob: obstacles.obstacles ) {
            ob.drawCollisionBoxes(g);
        }*/

        g.dispose();
        bs.show();
    }

    private class KeyInput extends KeyAdapter {
        public void keyPressed(KeyEvent e) { //клавиша нажата
            if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
                if(!tRex.downPressed) {
                    if(!waiting)
                        playSound("jump");
                    tRex.spacePressed = true;
                    tRex.speedDropCoeff = 1;
                }
                if(gameover)
                    restart();
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                tRex.downPressed = true;
                if (tRex.spacePressed && running)
                    tRex.speedDropCoeff = 3;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if(gameover)
                    restart();
            }
        }
        public void keyReleased(KeyEvent e) { //клавиша отпущена
            if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
                if(tRex.checkHeight() && tRex.spacePressed && !waiting) {
                    tRex.halfJump = true;
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                tRex.downPressed = false;
            }
        }
    }

    private class MouseInput extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if(gameover && e.getX() > gameOverPanel.restartTargetX && e.getX() < gameOverPanel.restartTargetX + gameOverPanel.restartTargetWidth &&
                    e.getY() > gameOverPanel.restartTargetY && e.getY() < gameOverPanel.restartTargetY + gameOverPanel.restartTargetHeight)
                restart();
        }
    }
}
