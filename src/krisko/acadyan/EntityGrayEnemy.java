package krisko.acadyan;

import java.awt.Graphics;

public class EntityGrayEnemy extends EntityLiving
{
	public EntityGrayEnemy(World world, Vector2D pos)
	{
		super(world, pos);
		maxHP = 5;
		hp = maxHP;
		prevPos = new Vector2D(pos.x, pos.y);
		
		lastShot = Game.tick();
	}
	
	@Override
	public void update()
	{
		boolean canJump = true;
		
		if(player != null)
		{
			if(player.dead || player.pos.minus(pos).getDistance() > 15.0)
				player = null;
		}
		else
		{
			// search for player
			for(int i = 0; i < world.entities.size(); i++)
			{
				Entity entity = world.entities.get(i);
				if(!(entity instanceof EntityPlayer))
					continue;
				
				if(entity.pos.minus(pos).getDistance() <= 15.0) // 15 = distance from enemy to player
				{
					player = (EntityPlayer)entity;
					canJump = false;
				}
			}
		}
		
		boolean left = false;
		boolean right = false;
		boolean jump = false;
		boolean sprint = false;
		boolean sneak = false;
		
		if(player != null)
		{
			// move to player
			if(player.pos.x < pos.x && pos.x - player.pos.x > 0.5)
			{
				left = true;
				jump = canJump && prevPos.x == pos.x && prevPos.y == pos.y;
			}
			else if(player.pos.x > pos.x && player.pos.x - pos.x > 0.5)
			{
				right = true;
				jump = canJump && prevPos.x == pos.x && prevPos.y == pos.y;
			}
			
			// check if he should sprint
			if(Math.abs(player.pos.x - pos.x) > 5)
			{
				sprint = true;
			}
			
			// shoot the player
			if(Game.tick() >= lastShot + Game.TicksPerSec*1.5)
			{
				lastShot = Game.tick();
				
				EntityBullet bullet = new EntityBullet(world, pos.plus(0.5D, 0.5D), player.pos.minus(pos).normalize().mul(0.1D), this);
				world.addEntity(bullet);
			}
		}

		prevPos.set(pos.x, pos.y);
		updateMotion(left, right, jump, sprint, sneak);
		updateCollision();
		
		handleLife();
	}
	
	@Override
	public void draw(Graphics g, int xOffset, int yOffset)
	{
		g.setColor(Game.ForegroundColor);
		g.drawRect((int)(pos.x * Game.BlockSize) - xOffset, (int)(pos.y * Game.BlockSize) - yOffset, EntityPlayer.DrawSize, EntityPlayer.DrawSize);
	}
	
	private Vector2D prevPos;
	private EntityPlayer player; // != null, if the player was found and is next to the enemy
	private int lastShot;
}