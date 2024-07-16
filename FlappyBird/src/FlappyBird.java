
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class FlappyBird extends JPanel implements ActionListener , KeyListener{
    int boardWidth=360;
    int boardHeight=640;

    //images
    Image bgimg;
    Image topPipe;
    Image botPipe;
    Image birdImg;

    //Bird
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdheight=24;
    int birdwidth=34;

    class Bird{
        int x = birdX;
        int y = birdY;
        int width=birdwidth;
        int height=birdheight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }

    //pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe{
        int x= pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;

        Image img;
        boolean passed=false;
        Pipe(Image img){
            this.img = img;
        }
    }


    //Game Logic
    Bird bird;

    int velocityX=-4;
    int velocityY=0;
    int gravity = 1;
    int HighScore=0;

    Timer gameLoop;
    Timer placePipes;
    
    ArrayList<Pipe> pipes;

    Random random = new Random();

    boolean gameOver = false;
    boolean paused = false;

    double score =0;


    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        //load Images
        bgimg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipe = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        botPipe = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        //Bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //game Timer
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

        //place pipes Timer
        placePipes = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                placePipe();
            }
        });
        placePipes.start();
    }

    public void placePipe(){
        int randPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int opening = boardHeight/4;

        Pipe TopPipe = new Pipe(topPipe);
        TopPipe.y = randPipeY;
        pipes.add(TopPipe);
        
        Pipe bottomPipe = new Pipe(botPipe);
        bottomPipe.y= TopPipe.y + pipeHeight + opening;
        pipes.add(bottomPipe);

    }


    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        //background
        g.drawImage(bgimg,0,0,boardWidth,boardHeight,null);
        //Bird
        g.drawImage(bird.img,bird.x,bird.y,bird.width,bird.height,null);
        //pipes
        for(int i =0; i<pipes.size();i++){
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img,pipe.x,pipe.y,pipe.width,pipe.height,null);
        }

        //score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 25));
        if(gameOver){
            Sound.playSound("die.wav");
            if(score > HighScore)
                HighScore=(int)score;
            g.drawString("Game Over "+String.valueOf((int)score),5,35);
            g.drawString("High Score: "+String.valueOf((int)HighScore),5,58);
            g.setColor(Color.RED);
            g.setFont(new Font("Arial Black",Font.PLAIN,20));
            g.drawString("PRESS ENTER TO RESTART ",20,320);
        }else{
            g.drawString(String.valueOf("Score : "+(int)score),5,35);
            g.drawString("High Score: "+String.valueOf((int)HighScore),5,58);
        }

    }

    public void move(){
        //bird
        velocityY+= gravity;
        bird.y+= velocityY;
        bird.y = Math.max(bird.y, 0);

        //pipes
        for(int i=0;i<pipes.size();i++){
            Pipe pipe = pipes.get(i);
            pipe.x+= velocityX;
        
            if(!pipe.passed && bird.x> pipe.x+pipe.width){
                pipe.passed=true;
                Sound.playSound("point.wav");
                score+=0.5;
            }

            if(collision(bird, pipe)){
                gameOver=true;
            }
        }

        if (bird.y>boardHeight ) {
            gameOver=true;
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(!paused){
            move();
            repaint();
            if(gameOver){
                placePipes.stop();
                gameLoop.stop();
            }
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE){
            if (!paused) {
                velocityY = -9;
            }
            Sound.playSound("flap.wav");
        }
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            if(gameOver){
                bird.y=birdY;
                velocityY=0;
                pipes.clear();
                score=0;
                gameOver=false;
                gameLoop.start();
                placePipes.start();
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            paused=!paused;
            if(paused){
                gameLoop.stop();
                placePipes.stop();
            }else
            gameLoop.start();
            placePipes.start();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

}

