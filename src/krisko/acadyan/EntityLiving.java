package krisko.acadyan;

import java.awt.Graphics;

public class EntityLiving extends Entity
{
	public EntityLiving(World world, Vector2D pos)
	{
		super(world, pos);
		
		maxHP = 5;
		hp = maxHP;
	}
	
	public EntityLiving(World world, Vector2D pos, Vector2D motion)
	{
		super(world, pos, motion);
		
		maxHP = 5;
		hp = maxHP;
	}
	
	@Override
	public void update()
	{
		boolean left = false;
		boolean right = false;
		boolean jump = false;
		boolean sprint = false;
		boolean sneak = false;
		
		updateMotion(left, right, jump, sprint, sneak);
		updateCollision();
		
		handleLife();
	}
	
	public void updateMotion(boolean left, boolean right, boolean jump, boolean sprint, boolean sneak)
	{
		// move left/right
		int direction = left ? right ? 0 : -1 : right ? 1 : 0;
		double maxMotionX = sprint ? 0.12D : sneak ? 0.02D : 0.07D;
		
		if(direction == 0)
			dir.x *= 0.8D;
		else
			dir.x += maxMotionX * direction / 15.0D;
		
		if(direction != 0 && Math.abs(dir.x) > maxMotionX)
			dir.x = maxMotionX * direction;
		
		// reset motionX
		if(direction == 0 && Math.abs(dir.x) < 0.0001D)
			dir.x = 0.0D;
		
		// set motionY (and jump)
//		if(jump && !didDoubleJump)
		if(jump && !inAir)
		{
			dir.y = -world.gravity*35.0D;
			
//			if(!inAir)
				inAir = true;
//			else
//				didDoubleJump = true;
		}
		else
		{
			dir.y += world.gravity;
			if(dir.y > 20.0D)
				dir.y = 20.0D;
		}
	}
	
	public void updateCollision()
	{
		double size = EntityPlayer.WorldSize;
		
		// Entity collision (only set dir new)
		boolean didCollideEntityY = false;
		if(Options.playerCollide)
		for(int i = 0; i < world.entities.size(); i++)
		{
			Entity entity = world.entities.get(i);
			if(entity == this || (!(entity instanceof EntityLiving) && !(entity instanceof EntityPlayer)))
				continue;
			
			// Check for collision
			if(!MathHelper.rectBetweenRect(pos.x + dir.x, pos.y + dir.y, size, size, entity.pos.x, entity.pos.y, size, size))
				continue;
			
			boolean didCollide = false;
			
			// collision x
			if(dir.x != 0.0D)
			{
				if(MathHelper.rectBetweenRect(pos.x+dir.x, pos.y, size, size, entity.pos.x, entity.pos.y, size, size))
				{
					if(dir.x >= 0.0D)
						dir.x = entity.pos.x - (pos.x + size);
					else
						dir.x = (entity.pos.x + size) - pos.x;
					
					didCollide = true;
				}
			}
			
			// collision y
			if(dir.y != 0.0D)
			{
				if(MathHelper.rectBetweenRect(pos.x, pos.y+dir.y, size, size, entity.pos.x, entity.pos.y, size, size))
				{
					if(dir.y >= 0.0D)
					{
						dir.y = entity.pos.y - (pos.y + size);
						
						didCollideEntityY = true;
						inAir = false;
					}
					else
						dir.y = (entity.pos.y + size) - pos.y;
					
					didCollide = true;
				}
			}
			
			if(!didCollide)
			{
				// collide with a corner
				// only reset motionY
				dir.y = 0.0D;
			}
			
			if(dir.y != 0.0D)
				inAir = true;
		}
		
		// Collision
		if(world.checkCollision(pos.x + dir.x, pos.y + dir.y, size, size))
		{
			boolean didCollide = false;
			
			// collision x
			if(dir.x != 0.0D)
			{
				if(world.checkCollision(pos.x + dir.x, pos.y, size, size))
				{
					if(dir.x >= 0.0D)
					{
						double nextPos = pos.x + size + dir.x;
						
						pos.x = (int)(nextPos) - size;
						if(nextPos < 0)
							pos.x--;
					}
					else
					{
						double nextPos = pos.x + dir.x;
						
						pos.x  = (int)(nextPos) + 1;
						if(nextPos < 0)
							pos.x--;
					}
					
					dir.x = 0.0D;
					didCollide = true;
				}
			}
			
			// collision y
			if(dir.y != 0.0D)
			{
				if(world.checkCollision(pos.x, pos.y + dir.y, size, size))
				{
					if(dir.y >= 0.0D)
					{
						pos.y = (int)(pos.y + size + dir.y) - size;
						
						inAir = false;
					}
					else
					{
						pos.y  = (int)(pos.y + dir.y) + 1;
					}
					
					dir.y = 0.0D;
					didCollide = true;
				}
			}
			
			if(!didCollide)
			{
				// collide with a corner
				// only reset motionY
				dir.y = 0.0D;
			}
			
			if(dir.y != 0.0D && !didCollideEntityY)
				inAir = true;
		}
		else
		{
			if(!didCollideEntityY)
				inAir = true;
		}
		
		pos.add(dir);
		
		// Collision with death-tiles
		double d = 0.1;
		if(world.checkCollision(pos.x + d/2, pos.y + d/2, size - d, size - d, Block.spike))
		{
			world.addBlood(pos.plus(new Vector2D(size/2, size/2)), dir.mul(1.0D), 3);
			takeDamage();
		}
		else if(world.checkCollision(pos.x, pos.y, size, size, Block.acid))
		{
			takeDamage();
		}
	}
	
/** handles the regeneration / poison */
	public void handleLife()
	{
		if(isPoisened())
		{
			if(Game.tick() > lastPoisened + poisonTime)
			{
				lastPoisened += poisonTime;
				hpPoison--;
				takeDamage();
			}
		}
		
		if(isRegenerating())
		{
			if(Game.tick() > lastRegen + regenTime)
			{
				lastRegen += regenTime;
				hpRegen--;
				heal();
			}
		}
	}
	
	public boolean heal()
	{
		if(hp >= maxHP || hp <= 0)
			return false;
		
		hp++;
		return true;
	}
	
	@Override
	public void takeDamage()
	{
		hp--;
		
		if(hp <= 0)
			dead = true;
	}
	
	public void poison(double timeInSec, int maxHpToLose)
	{
		poisonTime = (int)(timeInSec * Game.TicksPerSec);
		hpPoison = maxHpToLose;
		lastPoisened = Game.tick();
	}
	
	public boolean isPoisened() { return hpPoison > 0; }
	
	public void stopPoison() { hpPoison = 0; }
	
	public void regenerate(double timeInSec, int maxHpRegen)
	{
		regenTime = (int)(timeInSec * Game.TicksPerSec);
		hpRegen = maxHpRegen;
		lastRegen = Game.tick();
	}
	
	public boolean isRegenerating() { return hpRegen > 0; }
	
	public void stopRegen() { hpRegen = 0; }
	
//
	@Override
	public void draw(Graphics g, int xOffset, int yOffset)
	{
	}
	
	public boolean inAir;
	
	/** The remaining lives */
	public int hp;
	public int maxHP;
	
	// status
	private int hpPoison;
	private int poisonTime;
	private int lastPoisened;
	private int hpRegen;
	private int regenTime;
	private int lastRegen;
}