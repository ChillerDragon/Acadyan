package krisko.acadyan;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Texture
{
	public final int id;
	private final BufferedImage image;
	
	public static final int MAX_TEXTURES = 32;
	private static final Texture[] textures = new Texture[MAX_TEXTURES];
	
	/*
	 * Textures
	 */
	public static final Texture none = new Texture(-1, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
	
	// Stuff
	public static final Texture coin32 = new Texture(0, "coin_32.png");
	public static final Texture coin64 = new Texture(1, "coin_64.png");
	public static final Texture star = new Texture(2, "star.png");
	public static final Texture starBG = new Texture(3, "star_bg.png");
	public static final Texture goldGray = new Texture(4, "goldblock_gray.png");
	public static final Texture bullet = new Texture(5, "bullet.png");
	
	// Blocks
	public static final Texture gold = new Texture(10, "goldblock.png");
	public static final Texture teleporter = new Texture(11, "teleporter.png");
	public static final Texture trampoline = new Texture(12, "trampoline.png");
	public static final Texture breaking = new Texture(13, "breaking.png");
	
	// Items
	public static final Texture potion = new Texture(20, "potion.png");
	public static final Texture poison = new Texture(21, "poison.png");
	public static final Texture regeneration = new Texture(22, "regeneration.png");
	
	// Cursor
	public static final Texture cursor = new Texture(27, "cursor/cursor.png");
	public static final Texture cursor1 = new Texture(28, "cursor/cursor_1.png");
	public static final Texture cursor2 = new Texture(29, "cursor/cursor_2.png");
	public static final Texture cursor3 = new Texture(30, "cursor/cursor_3.png");
	public static final Texture cursor4 = new Texture(31, "cursor/cursor_4.png");
	
	private Texture(int id, String texturePath)
	{
		this.id = id;
		
		image = loadTexture("/" +texturePath);
		
		if(id >= 0 && id < MAX_TEXTURES)
		{
			if(textures[id] != null)
				System.out.println("Texture Collision! ID: " +id);
			
			textures[id] = this;
		}
	}
	
	private Texture(int id, BufferedImage img)
	{
		this.id = id;
		
		image = img;
		
		if(id >= 0 && id < MAX_TEXTURES)
		{
			if(textures[id] != null)
				System.out.println("Texture Collision! ID: " +id);
			
			textures[id] = this;
		}
	}
	
	public static Texture get(int id)
	{
		if(id < 0 || id >= textures.length || textures[id] == null)
			return none;
		
		return textures[id];
	}
	
	public static BufferedImage getImage(int id) { return get(id).getImage(); }
	
	public BufferedImage getImage() { return image; }
	public void draw(Graphics g, int x, int y, int w, int h) { g.drawImage(getImage(), x, y, w, h, null); }
	public void draw(Graphics g, int x, int y) { g.drawImage(getImage(), x, y, null); }
	
	private BufferedImage loadTexture(String path)
	{
		try
		{
			return ImageIO.read(Texture.class.getResource(path));
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