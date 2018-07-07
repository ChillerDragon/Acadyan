package krisko.acadyan;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Animation
{
	private final int id;
	private final BufferedImage[] images;
	
	public static final int MAX_ANIMATIONS = 16;
	private static final Animation[] animations = new Animation[MAX_ANIMATIONS];
	
	private final int rows, cols;
	
	/*
	 * Animations
	 */
	
	// Stuff
	public static final Animation goal = new Animation(1, "goal/goal", 0, 4, ".png");
	public static final Animation acid = new Animation(2, "acid/acid", 0, 15, ".png");
	
	public static final Animation confettiBlue = new Animation(3, "confetti/confetti_b", 1, 5, ".png");
	public static final Animation confettiGreen = new Animation(4, "confetti/confetti_g", 1, 5, ".png");
	public static final Animation confettiMagenta = new Animation(5, "confetti/confetti_m", 1, 5, ".png");
	public static final Animation confettiOrange = new Animation(6, "confetti/confetti_o", 1, 5, ".png");
	public static final Animation confettiRed = new Animation(7, "confetti/confetti_r", 1, 5, ".png");
	
	// Animation method 1 (use different files for animation)
	private Animation(int id, String texturePath, int start, int end, String fileEnding)
	{
		this.id = id;
		
		rows = 1;
		cols = 1;
		
		images = new BufferedImage[end-start + 1];
		for(int i = 0; i <= end-start; i++)
		{
			images[i] = loadTexture("/" +texturePath +Integer.toString(start+i) +fileEnding);
		}
		
		if(id >= 0 && id < animations.length)
		{
			if(animations[id] != null)
				System.out.println("Animation Collision! ID: " +id);
			
			animations[id] = this;
		}
	}
	
	// Animation method 2 (use the same file for animation)
	private Animation(int id, String texturePath, int cols, int rows)
	{
		this.id = id;
		
		this.rows = rows;
		this.cols = cols;
		
		images = new BufferedImage[1];
		images[0] = loadTexture("/" +texturePath);
		
		if(id >= 0 && id < animations.length)
		{
			if(animations[id] != null)
				System.out.println("Animation Collision! ID: " +id);
			
			animations[id] = this;
		}
	}
	
	public static Animation get(int id)
	{
		if(id < 0 || id >= animations.length || animations[id] == null)
			return null;
		
		return animations[id];
	}
	
	public static BufferedImage[] getImages(int id) { return get(id).getImages(); }
	
	public int getID() { return id; }
	public int getImageAmount() { return images.length; }
	public BufferedImage[] getImages() { return images; }
	public BufferedImage getImage(int index) { return images[index]; }
	
	public void draw(Graphics g, int x, int y, int w, int h, double timeNext)
	{
//		double timeAll = timeNext * (getImageAmount() - 1);
//		int ticksAll = (int)(timeAll * Game.TicksPerSec);
//		int tick = Game.tick() % ticksAll;
//		int index = tick * getImageAmount() / ticksAll;
//		
//		g.drawImage(getImage(index), x, y, w, h, null);
		draw(g, x, y, w, h, timeNext, 0);
	}
	
	public void draw(Graphics g, int x, int y, int w, int h, double timeNext, int startTick)
	{
		if(rows > 1 || cols > 1)
		{
			double timeAll = timeNext * ((rows * cols) - 1);
			int ticksAll = (int)(timeAll * Game.TicksPerSec);
			int tick = (Game.tick() - startTick) % ticksAll;
			if(tick < 0)
				tick = -tick;
			int index = tick * (rows * cols) / ticksAll;

			int imgW = getImage(0).getWidth() / cols;
			int imgH = getImage(0).getHeight() / rows;
			int imgX = (index % cols) * imgW;
			int imgY = (index / cols) * imgH;
			
			g.drawImage(getImage(0), x, y, x+w, y+h, imgX, imgY, imgX+imgW, imgY+imgH, null);
			return;
		}
		
		double timeAll = timeNext * (getImageAmount() - 1);
		int ticksAll = (int)(timeAll * Game.TicksPerSec);
		int tick = (Game.tick() - startTick) % ticksAll;
		if(tick < 0)
			tick = -tick;
		int index = tick * getImageAmount() / ticksAll;
		
		g.drawImage(getImage(index), x, y, w, h, null);
	}
	
//	public void draw(Graphics g, int x, int y, double timeNext)
//	{
//		double timeAll = timeNext * (getImageAmount() - 1);
//		int ticksAll = (int)(timeAll * Game.TicksPerSec);
//		int tick = Game.tick() % ticksAll;
//		int index = tick * getImageAmount() / ticksAll;
//		
//		g.drawImage(getImage(index), x, y, null);
//	}
	
	private BufferedImage loadTexture(String path)
	{
		try
		{
			return ImageIO.read(Animation.class.getResource(path));
		} catch(Exception ex)
		{
//			ex.printStackTrace();
			System.out.println("Image not found: " +path);
			return null;
		}
	}
	
	public static void load()
	{ }
}