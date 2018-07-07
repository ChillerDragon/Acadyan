package krisko.acadyan;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import krisko.acadyan.gui.GuiLabel;

public class Block
{
	private Block(int id, int type, String displayName)
	{
		this.id = id;
		this.type = type;
		this.displayName = displayName;
		
		if(id < 0 || id >= blocks.length)
		{
			// debug
			System.out.println("Block ID: " +id +" -> too low or too high");
		}
		else if(blocks[id] != null)
		{
			// debug
			System.out.println("Block Collision (ID: " +id +")");
		}
		else
		{
			blocks[id] = this;
		}
	}
	
	public String getName() { return displayName; }
	
	public boolean isSolid() { return (type & SOLID) > 0; }
	
	public boolean isConsumable() { return (type & CONSUMABLE) > 0; }
	
	public boolean isDamageDealing() { return (type & DAMAGE) > 0; }
	
	public int getMetadataNum()
	{
		if(this == hidden)
			return 5;
		else if(this == spike || this == cannon || this == trampoline)
			return 4;
		else if(this == door || this == laser)
			return 2;
		else
			return 0;
	}
	
	public boolean hasMetadata() { return getMetadataNum() > 0; }
	
	public int getLayer()
	{
		if(this == hidden)
			return 1;
		else
			return 0;
	}
	
	public BlockData createBlockData()
	{
		if(this == cannon)
			return new BlockDataCannon();
		else if(this == teleporter)
			return new BlockDataTeleporter();
		else if(this == trampoline)
			return new BlockDataTrampoline();
		else if(this == hidden)
			return new BlockDataHidden();
		else if(this == breaking)
			return new BlockDataBreaking();
		else if(this == pressurePlate)
			return new BlockDataPressurePlate();
		else if(this == door)
			return new BlockDataSwitchable(false);
		else if(this == laser)
			return new BlockDataSwitchable(true);
		
		return null;
	}
	
	public void onBlockConsume(Game game, int x, int y)
	{
		if(this == coin)
		{
			game.addCoin();
			
			int r = (int)Math.random()*3;
			if(r == 0)
				SoundManager.coin1.play(0.5f);
			else if(r == 1)
				SoundManager.coin2.play(0.5f);
			else
				SoundManager.coin3.play(0.5f);
		}
		else if(this == potion)
			game.getPlayerInventory().addItemToHotbar(Item.potion);
		else if(this == poison)
			game.getPlayerInventory().addItemToHotbar(Item.poison);
		else if(this == regeneration)
			game.getPlayerInventory().addItemToHotbar(Item.regeneration);
	}
	
// render
//	public void draw(Graphics g, int x, int y, int w, int h)
//	{
//		draw(g, x, y, w, h, 0);
//	}
	
	public void draw(Graphics g, int x, int y, int w, int h, int infoData)
	{
		draw(g, x, y, w, h, 0, infoData);
	}
	
//	public void draw(Graphics g, int x, int y, int w, int h, int metadata)
//	{
//		draw(g, x, y, w, h, metadata, false);
//	}
	
	public void draw(Graphics g, int x, int y, int w, int h, int metadata, int infoData)
	{
		/*
		 * metadata:
		 * 0 -> normal
		 * 1 -> rotation 90°
		 * 2 -> rotation 180°
		 * 3 -> rotation 270°
		 * 
		 * infoData:
		 * < 0 = editor
		 * > 0 = in world
		 * block-specific (trampoline = move pixels, hidden = 0/1=transparent)
		 */
		if(this == air)
			return;
		
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform at = g2.getTransform();
		double r = metadata * Math.PI / 2.0D;
		double mX = x+w/2.0D;
		double mY = y+h/2.0D;
		if(hasMetadata() && metadata > 0 && metadata < 4)
			g2.rotate(r, mX, mY);
		
		if(this == solid)
		{
			g.setColor(Game.ForegroundColor);
			g.fillRect(x, y, w, h);
		}
		else if(this == trampoline)
		{
			Texture.trampoline.draw(g, x, y, w, h);
		}
		else if(this == breaking)
		{
			if(infoData != 1)
				Texture.breaking.draw(g, x, y, w, h);
		}
		else if(this == teleporter)
		{
			r = -((Game.tick() * 2) % 360) * Math.PI / 180.0D;
			g2.rotate(r, mX, mY);
			
			Texture.teleporter.draw(g, x, y, w, h);
		}
		else if(this == hidden)
		{
			if(infoData == 2)
				g.setColor(new Color(Game.ForegroundColor.getRed(), Game.ForegroundColor.getGreen(), Game.ForegroundColor.getBlue(), 0xC0));
			else
				g.setColor(Game.ForegroundColor);
			
			if(metadata == 4) // draw full-width block
				g.fillRect(x, y, w, h);
			else
				g.fillRect(x, y+1, w, h-1);
			
			if(infoData < 0)
			{
				g.setColor(Color.black);
				GuiLabel.draw(g, "H", x + w/2, y + h/4, GuiLabel.ALIGNMENT_MIDDLE, (int)(h*0.6875));
			}
		}
		else if(this == spike)
		{
			int[] xPoints = { x, x+w/2, x+w };
			int[] yPoints = { y+h, y, y+h };
			g.setColor(Game.ForegroundColor);
			g.fillPolygon(xPoints, yPoints, 3);
		}
		else if(this == fallingSpike)
		{
			g.setColor(Color.black);
			GuiLabel.draw(g, "F", x + w/2, y, GuiLabel.ALIGNMENT_MIDDLE, h);
		}
		else if(this == acid)
		{
			Animation.acid.draw(g, x, y, w, h, 0.05D);
		}
		else if(this == cannon)
		{
			g.setColor(Game.ForegroundColor);
			g.fillRect(x, y + 4, 8, h - 8);
		}
		else if(this == pressurePlate)
		{
			int height = infoData == 2 ? 4 : 6;
			
			g.setColor(Game.ForegroundColor);
			g.fillRect(x+2, y+h-height, w-4, height);
		}
		else if(this == door)
		{
			if(infoData == 2)
			{
				g.setColor(new Color((int)(Game.ForegroundColor.getRed() * 0.875), (int)(Game.ForegroundColor.getGreen() * 0.875), (int)(Game.ForegroundColor.getBlue() * 0.875)));
				g.fillRect(x, y, w, h);
			}
			else
			{
				g.setColor(Game.ForegroundColor);
				g.fillRect(x+8, y, w-16, h);
			}
		}
		else if(this == laser)
		{
			infoData = Math.abs(infoData) - 1;
			
			// render laser
			if(infoData < 4)
			{
				g.setColor(Color.red);
				g.fillRect(x+4, y, w-8, h);
			}
			
			// render post
			g.setColor(Game.ForegroundColor);
			if(infoData%4 == 0 || infoData%4 == 1)
				g.fillRect(x+2, y, w-4, h/4);
			if(infoData%4 == 0 || infoData%4 == 3)
				g.fillRect(x+2, y+h-h/4, w-4, h/4);
		}
		else if(this == coin)
		{
			Texture.coin32.draw(g, x, y, w, h);
		}
		else if(this == gold)
		{
			Texture.gold.draw(g, x, y, w, h);
		}
		else if(this == potion)
		{
			Texture.potion.draw(g2, x, y, w, h);
		}
		else if(this == poison)
		{
			Texture.poison.draw(g2, x, y, w, h);
		}
		else if(this == regeneration)
		{
			Texture.regeneration.draw(g2, x, y, w, h);
		}
		else if(this == start)
		{
			if(infoData < 0)
			{
				g.setColor(Color.black);
				GuiLabel.draw(g, "S", x + w/2, y, GuiLabel.ALIGNMENT_MIDDLE, h);
			}
		}
		else if(this == goal)
		{
			Animation.goal.draw(g, x, y, w, h, 0.2D);
		}
		
		g2.setTransform(at);
	}
	
	public final int id;
	private final int type;
	private final String displayName;
	
	private static final int AIR = 0;
	private static final int SOLID = 1;
	private static final int CONSUMABLE = 2;
	private static final int DAMAGE = 4;
	
	public static final Block blocks[] = new Block[64];
	
/* All blocks */
	public static final Block air = new Block(0, AIR, "Air");
	public static final Block solid = new Block(1, SOLID, "Solid");
	
	public static final Block start = new Block(2, AIR, "Start");
	public static final Block goal = new Block(3, AIR, "Goal");
	
	public static final Block trampoline = new Block(4, AIR, "Trampoline");
	public static final Block pressurePlate = new Block(5, AIR, "Pressure Plate");
	public static final Block breaking = new Block(6, SOLID, "Breaking Block");
	public static final Block teleporter = new Block(7, AIR, "Teleporter");
	public static final Block hidden = new Block(8, AIR, "Hidden Block");
	
	public static final Block spike = new Block(9, DAMAGE, "Spike");
	public static final Block fallingSpike = new Block(10, DAMAGE, "Falling Spike");
	public static final Block acid = new Block(11, DAMAGE, "Acid");
	
	public static final Block cannon = new Block(12, AIR, "Cannon");
	
	public static final Block coin = new Block(13, CONSUMABLE, "Coin");
	public static final Block gold = new Block(14, CONSUMABLE, "Goldblock");
	
	public static final Block potion = new Block(15, CONSUMABLE, "Potion");
	public static final Block poison = new Block(16, CONSUMABLE, "Poison");
	public static final Block regeneration = new Block(17, CONSUMABLE, "Regeneration");
	
	public static final Block door = new Block(18, SOLID, "Door");
	public static final Block laser = new Block(19, DAMAGE, "Laser");
}