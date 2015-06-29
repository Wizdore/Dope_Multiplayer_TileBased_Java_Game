package com.PinixDynasty.dope;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import com.PinixDynasty.dope.graphics.Screen;
import com.PinixDynasty.dope.input.Keyboard;

public class Game extends Canvas implements Runnable
{
    private static final long serialVersionUID = 1L;

    public static int width = 300, height = width / 16 * 9, scale = 4;
    public static String title = "Dope";

    private Thread thread;
    private boolean running = false;
    JFrame frame;
    private Keyboard key;

    private BufferedImage image = new BufferedImage(width, height,
	    BufferedImage.TYPE_INT_RGB);
    private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
	    .getData();

    Screen screen;

    public Game()
    {
	Dimension size = new Dimension(width * scale, height * scale);
	setPreferredSize(size);
	setMinimumSize(size);
	setMaximumSize(size);

	screen = new Screen(width, height);
	frame = new JFrame();
	key = new Keyboard();
	addKeyListener(key);
    }

    public synchronized void start()
    {
	running = true;
	thread = new Thread(this, "Display");
	thread.start();
    }

    public synchronized void stop()
    {
	running = false;
	try
	{
	    thread.join();
	} catch (InterruptedException e)
	{
	    e.printStackTrace();
	}
    }

    public void run()
    {
	long lastTime = System.nanoTime();
	long timer = System.currentTimeMillis();
	final double ns = 1000000000.0 / 60.0;
	double delta = 0;
	int frames = 0;
	int updates = 0;
	requestFocus();
	while (running)
	{
	    long now = System.nanoTime();
	    delta += (now - lastTime) / ns;
	    lastTime = now;
	    while (delta >= 1)
	    {
		update();
		updates++;
		delta--;
	    }
	    render();
	    frames++;
	    if (System.currentTimeMillis() - timer > 1000)
	    {
		timer += 1000;
		frame.setTitle(title + "   |   " + updates + " Hz, " + frames
			+ " Fps. ");
		updates = 0;
		frames = 0;
	    }
	}
	stop();
    }

    public int x = 0, y = 0, speed = 1; // Movement Variables

    public void update()
    {
	key.update();
	if (key.up)
	    y += speed;
	if (key.down)
	    y -= speed;
	if (key.left)
	    x += speed;
	if (key.right)
	    x -= speed;
    }

    public void render()
    {
	BufferStrategy bs = getBufferStrategy();
	if (bs == null)
	{
	    createBufferStrategy(3);
	    return;
	}

	screen.clear();
	screen.render(x, y);

	for (int i = 0; i < pixels.length; i++)
	{
	    pixels[i] = screen.pixels[i];
	}

	Graphics g = bs.getDrawGraphics();

	g.drawImage(image, 0, 0, getWidth(), getHeight(), null);

	g.dispose();
	bs.show();
    }

    public static void main(String[] args)
    {
	Game game = new Game();
	game.frame.setResizable(false);
	game.frame.setTitle(game.title);
	game.frame.add(game);
	game.frame.pack();
	game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	game.frame.setLocationRelativeTo(null);
	game.frame.setVisible(true);
	game.start();
    }
}
