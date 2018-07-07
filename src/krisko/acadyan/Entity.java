package krisko.acadyan;

import java.awt.Graphics;

public class Entity
{
	public Entity(World world)
	{
		this.world = world;
		pos = new Vector2D();
		dir = new Vector2D();
	}
	
	public Entity(World world, Vector2D pos)
	{
		this.world = world;
		this.pos = new Vector2D(pos.x, pos.y);
		this.dir = new Vector2D();
	}
	
	public Entity(World world, Vector2D pos, Vector2D dir)
	{
		this.world = world;
		this.pos = new Vector2D(pos.x, pos.y);
		this.dir = new Vector2D(dir.x, dir.y);
	}
	
//
	public void update() { }
	
//
	public void setPosition(double x, double y)
	{
		pos.set(x, y);
	}
	
	public void takeDamage()
	{
	}
	
	public void kill()
	{
		dead = true;
	}
	
	public Texture getTexture()
	{
		return Texture.none;
	}
	
//
	public void draw(Graphics g, int xOffset, int yOffset)
	{
		getTexture().draw(g, (int)(pos.x * Game.BlockSize) - xOffset, (int)(pos.y * Game.BlockSize) - yOffset, Game.BlockSize, Game.BlockSize);
	}
	
	protected World world;
	
	public final Vector2D pos;
	public final Vector2D dir;
	
	public boolean dead;
}