package krisko.acadyan;

import java.awt.Graphics;

public class EntityBullet extends Entity
{
	public EntityBullet(World world, Vector2D pos, int direction)
	{
		super(world, pos);
		
		dir.x = direction == 0 ? 1 : direction == 2 ? -1 : 0;
		dir.y = direction == 1 ? 1 : direction == 3 ? -1 : 0;
		
		dir.multiply(0.08D);
		
		startTick = Game.tick();
	}
	
	public EntityBullet(World world, Vector2D pos, Vector2D dir, Entity owner)
	{
		super(world, pos, dir);
		this.owner = owner;
		
		startTick = Game.tick();
	}
	
	@Override
	public void update()
	{
		if(Game.tick() >= startTick + Game.TicksPerSec*10)
		{
			kill();
			return;
		}
		
		pos.add(dir);
		
		for(int i = 0; i < world.entities.size(); i++)
		{
			Entity entity = world.entities.get(i);
			if(entity == owner || !(entity instanceof EntityPlayer))
				continue;
			
			double playerSize = EntityPlayer.WorldSize;
//			if(MathHelper.rectInRect(posX, posY, 1.0D, 1.0D, entity.posX, entity.posY, playerSize, playerSize))
//			if(MathHelper.getDistance(pos.x + 0.5D, pos.y + 0.5D, entity.pos.x + playerSize/2, entity.pos.y + playerSize/2) < 0.5D + playerSize/2)
			if(pos.minus(entity.pos.plus(playerSize/2, playerSize/2)).getDistance() < 0.5D + playerSize/2)
			{
				world.addBlood(entity.pos.plus(new Vector2D(playerSize/2, playerSize/2)), dir.plus(entity.dir.mul(0.4D)), 5);
				entity.takeDamage();
				kill();
				return;
			}
		}
		
		double size = owner == null ? 0.95D : 0.6D;
		if(world.checkCollision(pos.x - size/2, pos.y - size/2, size, size))
			kill();
	}
	
	@Override
	public Texture getTexture() { return Texture.bullet; }
	
	@Override
	public void draw(Graphics g, int xOffset, int yOffset)
	{
		if(owner == null)
		{
			getTexture().draw(g, (int)((pos.x-0.5D) * Game.BlockSize) - xOffset, (int)((pos.y-0.5D) * Game.BlockSize) - yOffset, Game.BlockSize, Game.BlockSize);
		}
		else
		{
			int pixelSize = (int)(Game.BlockSize * 0.6D);
			
			g.setColor(Game.ForegroundColor);
			g.fillRect((int)(pos.x * Game.BlockSize) - xOffset - pixelSize/2, (int)(pos.y * Game.BlockSize) - yOffset - pixelSize/2, pixelSize, pixelSize);
		}
	}
	
	private Entity owner;
	private int startTick;
}