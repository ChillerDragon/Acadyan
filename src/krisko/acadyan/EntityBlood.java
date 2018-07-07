package krisko.acadyan;

import java.awt.Color;
import java.awt.Graphics;

public class EntityBlood extends Entity
{
	public EntityBlood(World world, Vector2D pos, Vector2D motion)
	{
		this(world, pos, motion, 5);
	}
	
	public EntityBlood(World world, Vector2D pos, Vector2D motion, int bloodSize)
	{
		super(world, pos, motion);
		size = bloodSize;
	}
	
	@Override
	public void update()
	{
		if(world.checkCollision(pos.x, pos.y, 0.03125D, 0.03125D))
		{
			dir.x = 0;
			dir.y = 0;
			return;
		}
		
		pos.add(dir);
		
		dir.x *= 0.995D;
		
		dir.y += 0.002D;
//		motion.y *= 0.9999D;
		if(dir.y > 0.25D)
			dir.y = 0.25D;
	}
	
	@Override
	public void draw(Graphics g, int xOffset, int yOffset)
	{
		g.setColor(Color.red);
		g.fillRect((int)(pos.x * Game.BlockSize) - xOffset - size/2, (int)(pos.y * Game.BlockSize) - yOffset - size/2, size, size);
	}
	
	private int size;
}