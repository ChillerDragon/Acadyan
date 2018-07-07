package krisko.acadyan;

import java.awt.Color;
import java.awt.Graphics;

import krisko.acadyan.gui.GuiLabel;

public class EntityPlayer extends Entity
{
	public EntityPlayer(GameWorld gameWorld, Player player)
	{
		super(gameWorld);
		this.player = player;
		dirMouse = new Vector2D();
		world = gameWorld;
		
		maxHP = world.game.getPlayerHP();
		hp = maxHP;
		
		respawn();
	}
	
	public void respawn()
	{
		if(world.pStart != null)
		{
			pos.x = world.pStart.x + 0.5 - WorldSize/2;
			pos.y = world.pStart.y + 1.0 - WorldSize;
		}
		else
		{
			pos.x = Chunk.WIDTH/2.0D - WorldSize/2.0D;
			pos.y = -10.0D;
		}
		
		dir.set(0.0D, 0.0D);
		
		stopPoison();
		stopRegen();
		
		fistTime = -1;
		
		hitByTrampoline = false;
		
		levelTime = 0;
//		dead = false;
	}
	
	@Override
	public void update() { }
	
	public void update(int mouseX, int mouseY, boolean leftDown, boolean left, boolean right, boolean jump, boolean sprint, boolean sneak)
	{
		if(dead)
			return;
		
		if(!sprint)
			stopSprint = false;
		else if(stopSprint)
			sprint = false;
		
		if(fistTime == -1)
		{
			// start punching
			if(leftDown && !lastLeftDown)
			{
				double angle = MathHelper.getAngle(Game.ScreenWidth/2, Game.ScreenHeight/2, mouseX, mouseY);
				dirMouse.x = Math.cos(angle);
				dirMouse.y = Math.sin(angle);
				
				fistTime = 0;
				fistHit = false;
				fistPos = new Vector2D(pos.x + WorldSize/2, pos.y + WorldSize/2);
				SoundManager.punch.play();
			}
		}
		else if(fistTime >= 0)
		{
			// while fist is outside
			if(++fistTime >= fistTimeOutside)
			{
				fistTime = -1;
				fistPos = null;
			}
			else
			{
				double d = fistTime / (double)fistTimeOutside;
				d *= 2;
				if(d > 1.0D)
					d = 2.0D - d;
				
				fistPos.set(pos.x + WorldSize/2, pos.y + WorldSize/2);
				fistPos.add(dirMouse.mul(fistDistance * d));
			}
		}
		
		// try hitting an entity with the fist
		if(!fistHit && fistPos != null)
		{
			for(int i = 0; i < world.entities.size(); i++)
			{
				Entity entity = world.entities.get(i);
				if(entity == this || !(entity instanceof EntityLiving))
					continue;
				
				if(MathHelper.pointInRect(fistPos.x, fistPos.y, entity.pos.x, entity.pos.y, WorldSize, WorldSize))
				{
					fistHit = true;
					entity.takeDamage();
					entity.dir.add(dirMouse.div(8.0D));
					world.addBlood(fistPos, dirMouse.mul(0.125D), 3);
					SoundManager.hit.play();
					break;
				}
			}
		}
		
		// set level-time to 0
		if(!levelTimeStarted && !world.game.openWorld && (left || right || jump))
			levelTimeStarted = true;
		
		// move left/right
		int direction = left ? right ? 0 : -1 : right ? 1 : 0;
		double maxMotionX = sprint ? 0.12D : sneak ? 0.02D : 0.07D;
		
		if(hitByTrampoline)
		{
			if(direction == 0)
				dir.x *= 0.995D;
			else
				hitByTrampoline = false;
		}
		
		if(!hitByTrampoline)
		{
			if(direction == 0)
				dir.x *= 0.8D;
			else
				dir.x += maxMotionX * direction / 15.0D;
		}
		
		if(direction != 0 && Math.abs(dir.x) > maxMotionX)
			dir.x = maxMotionX * direction;
		
		// reset motionX
		if(direction == 0 && Math.abs(dir.x) < 0.0001D)
			dir.x = 0.0D;
		
		// set motionY (and jump)
		if(jump && !lastJump && !didDoubleJump)
		{
			dir.y = -world.gravity*35.0D;
			
			if(!inAir)
				inAir = true;
			else
				didDoubleJump = true;
			
			SoundManager.jump.play();
		}
		else
		{
			dir.y += world.gravity;
			if(dir.y > 20.0D)
				dir.y = 20.0D;
		}
		
		// Entity collision (only set dir new)
		boolean didCollideEntityY = false;
		if(Options.playerCollide)
		for(int i = 0; i < world.entities.size(); i++)
		{
			Entity entity = world.entities.get(i);
			if(entity == this || (!(entity instanceof EntityLiving) && !(entity instanceof EntityPlayer)))
				continue;
			
			// Check for collision
			if(!MathHelper.rectBetweenRect(pos.x + dir.x, pos.y + dir.y, WorldSize, WorldSize, entity.pos.x, entity.pos.y, WorldSize, WorldSize))
				continue;
			
			boolean didCollide = false;
			
			// collision x
			if(dir.x != 0.0D)
			{
				if(MathHelper.rectBetweenRect(pos.x+dir.x, pos.y, WorldSize, WorldSize, entity.pos.x, entity.pos.y, WorldSize, WorldSize))
				{
					// push other entity
					entity.dir.x += dir.x * 0.125D;
					
					if(dir.x >= 0.0D)
						dir.x = entity.pos.x - (pos.x + WorldSize);
					else
						dir.x = (entity.pos.x + WorldSize) - pos.x;
					
					didCollide = true;
					
					// remove sprint
//					if(sprint)
//						stopSprint = true;
				}
			}
			
			// collision y
			if(dir.y != 0.0D)
			{
				if(MathHelper.rectBetweenRect(pos.x, pos.y+dir.y, WorldSize, WorldSize, entity.pos.x, entity.pos.y, WorldSize, WorldSize))
				{
					if(dir.y >= 0.0D)
					{
						dir.y = entity.pos.y - (pos.y + WorldSize);
						
						didCollideEntityY = true;
						inAir = false;
						didDoubleJump = false;
					}
					else
						dir.y = (entity.pos.y + WorldSize) - pos.y;
					
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
		if(world.checkCollision(pos.x + dir.x, pos.y + dir.y, WorldSize, WorldSize))
		{
			hitByTrampoline = false;
			
			boolean didCollide = false;
			
			// collision x
			if(dir.x != 0.0D)
			{
				if(world.checkCollision(pos.x + dir.x, pos.y, WorldSize, WorldSize))
				{
					if(dir.x >= 0.0D)
					{
						double nextPos = pos.x + WorldSize + dir.x;
						
						pos.x = (int)(nextPos) - WorldSize;
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
					
					// remove sprint
					if(sprint)
						stopSprint = true;
				}
			}
			
			// collision y
			if(dir.y != 0.0D)
			{
				if(world.checkCollision(pos.x, pos.y + dir.y, WorldSize, WorldSize))
				{
					if(dir.y >= 0.0D)
					{
						pos.y = (int)(pos.y + WorldSize + dir.y) - WorldSize;
						
						inAir = false;
						didDoubleJump = false;
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
		
		// get consumable blocks
		if(!world.game.testMode)
		{
			int leftBlock = (int)(pos.x >= 0.0D ? pos.x : pos.x-1.0D);
			int rightBlock = (int)(pos.x+WorldSize >= 0.0D ? pos.x+WorldSize : pos.x+WorldSize-1.0D);
			int topBlock = (int)(pos.y >= 0.0D ? pos.y : pos.y-1.0D);
			int bottomBlock = (int)(pos.y+WorldSize >= 0.0D ? pos.y+WorldSize : pos.y+WorldSize-1.0D);
			
			// correct left
			if(pos.x < 0.0D && leftBlock == pos.x-1.0D)
				leftBlock++;
			
			// correct right
			if(pos.x+WorldSize >= 0.0D && rightBlock == pos.x+WorldSize)
				rightBlock--;
			
			// correct top
			if(pos.y < 0.0D && topBlock == pos.y-1.0D)
				topBlock++;
			
			// correct bottom
			if(pos.y+WorldSize >= 0.0D && bottomBlock == pos.y+WorldSize)
				bottomBlock--;
			
			// check the collision
			for(int i = leftBlock; i <= rightBlock; i++)
			{
				for(int j = topBlock; j <= bottomBlock; j++)
				{
					if(world.getBlock(0, i, j).isConsumable())
					{
						world.getBlock(0, i, j).onBlockConsume(world.game, i, j);
						world.setBlock(0, i, j, Block.air);
					}
				}
			}
		}
		
		// notify blocks that are directly below the player
		int iX = 0;
		int iY = (int)(pos.y+WorldSize >= 0.0D ? pos.y+WorldSize : pos.y+WorldSize-1);
		for(double x = pos.x; x < pos.x+WorldSize; x++)
		{
			iX = (int)(x >= 0.0D ? x : x-1);
			
			if(world.getBlock(Block.breaking.getLayer(), iX, iY) == Block.breaking)
			{
				BlockData data = world.getBlockData(Block.breaking.getLayer(), iX, iY);
				data.onEntityAbove(world, this, iX, iY);
			}
		}
		double x = pos.x+WorldSize - 1.0D/Game.BlockSize;
		int iX2 = (int)(x >= 0.0D ? x : x-1);
		if(iX != iX2 && world.getBlock(Block.breaking.getLayer(), iX2, iY) == Block.breaking)
		{
			BlockData data = world.getBlockData(Block.breaking.getLayer(), iX2, iY);
			data.onEntityAbove(world, this, iX2, iY);
		}
		
		
		// Collision with death-tiles
		double d = 0.1;
		if(world.checkCollision(pos.x + d/2, pos.y + d/2, WorldSize - d, WorldSize - d, Block.spike))
		{
			world.addBlood(pos.plus(new Vector2D(WorldSize/2, WorldSize/2)), dir.mul(1.0D), 3);
			takeDamage();
		}
		else if(world.checkCollision(pos.x, pos.y, WorldSize, WorldSize, Block.acid))
		{
			takeDamage();
		}
		else if(world.checkCollision(pos.x, pos.y, WorldSize, WorldSize, Block.laser))
		{
			takeDamage();
		}
		
		handleLife();
		
		lastJump = jump;
		lastLeftDown = leftDown;
		
		// Collision with goal
		if(world.checkCollision(pos.x, pos.y, WorldSize, WorldSize, Block.goal))
		{
			world.game.onLevelComplete(levelTime);
			return;
		}
		
		if(levelTimeStarted)
			levelTime++;
	}
	
	public void updateMultiplayer(int mouseX, int mouseY, boolean leftDown, boolean left, boolean right, boolean jump, boolean sprint, boolean sneak)
	{
		if(Options.hidePlayers == 2 && player != world.getLocalPlayer())
			return;
		
		if(dead)
			return;
		
		if(!sprint)
			stopSprint = false;
		else if(stopSprint)
			sprint = false;
		
		if(fistTime == -1)
		{
			// start punching
			if(leftDown && !lastLeftDown)
			{
				double angle = MathHelper.getAngle(Game.ScreenWidth/2, Game.ScreenHeight/2, mouseX, mouseY);
				dirMouse.x = Math.cos(angle);
				dirMouse.y = Math.sin(angle);
				
				fistTime = 0;
				fistHit = false;
				fistPos = new Vector2D(pos.x + WorldSize/2, pos.y + WorldSize/2);
				SoundManager.punch.play();
			}
		}
		else if(fistTime >= 0)
		{
			// while fist is outside
			if(++fistTime >= fistTimeOutside)
			{
				fistTime = -1;
				fistPos = null;
			}
			else
			{
				double d = fistTime / (double)fistTimeOutside;
				d *= 2;
				if(d > 1.0D)
					d = 2.0D - d;
				
				fistPos.set(pos.x + WorldSize/2, pos.y + WorldSize/2);
				fistPos.add(dirMouse.mul(fistDistance * d));
			}
		}
		
		// try hitting an entity with the fist
		if(!fistHit && fistPos != null)
		{
			for(int i = 0; i < world.entities.size(); i++)
			{
				Entity entity = world.entities.get(i);
				if(entity == this || !(entity instanceof EntityLiving))
					continue;
				
				if(MathHelper.pointInRect(fistPos.x, fistPos.y, entity.pos.x, entity.pos.y, WorldSize, WorldSize))
				{
					fistHit = true;
					entity.takeDamage();
					entity.dir.add(dirMouse.div(8.0D));
					world.addBlood(fistPos, dirMouse.mul(0.125D), 3);
					SoundManager.hit.play();
					break;
				}
			}
		}
		
		// set level-time to 0
		if(!levelTimeStarted && !world.game.openWorld && (left || right || jump))
			levelTimeStarted = true;
		
		// move left/right
		int direction = left ? right ? 0 : -1 : right ? 1 : 0;
		double maxMotionX = sprint ? 0.12D : sneak ? 0.02D : 0.07D;
		
		if(hitByTrampoline)
		{
			if(direction == 0)
				dir.x *= 0.995D;
			else
				hitByTrampoline = false;
		}
		
		if(!hitByTrampoline)
		{
			if(direction == 0)
				dir.x *= 0.8D;
			else
				dir.x += maxMotionX * direction / 15.0D;
		}
		
		if(direction != 0 && Math.abs(dir.x) > maxMotionX)
			dir.x = maxMotionX * direction;
		
		// reset motionX
		if(direction == 0 && Math.abs(dir.x) < 0.0001D)
			dir.x = 0.0D;
		
		// set motionY (and jump)
		if(jump && !lastJump && !didDoubleJump)
		{
			dir.y = -world.gravity*35.0D;
			
			if(!inAir)
				inAir = true;
			else
				didDoubleJump = true;
			
			SoundManager.jump.play();
		}
		else
		{
			dir.y += world.gravity;
			if(dir.y > 20.0D)
				dir.y = 20.0D;
		}
		
		// Entity collision (only set dir new)
		boolean didCollideEntityY = false;
		if(Options.playerCollide)
		for(int i = 0; i < world.entities.size(); i++)
		{
			Entity entity = world.entities.get(i);
			if(entity == this || (!(entity instanceof EntityLiving) && !(entity instanceof EntityPlayer)))
				continue;
			
			// Check for collision
			if(!MathHelper.rectBetweenRect(pos.x + dir.x, pos.y + dir.y, WorldSize, WorldSize, entity.pos.x, entity.pos.y, WorldSize, WorldSize))
				continue;
			
			boolean didCollide = false;
			
			// collision x
			if(dir.x != 0.0D)
			{
				if(MathHelper.rectBetweenRect(pos.x+dir.x, pos.y, WorldSize, WorldSize, entity.pos.x, entity.pos.y, WorldSize, WorldSize))
				{
					// push other entity
					entity.dir.x += dir.x * 0.125D;
					
					if(dir.x >= 0.0D)
						dir.x = entity.pos.x - (pos.x + WorldSize);
					else
						dir.x = (entity.pos.x + WorldSize) - pos.x;
					
					didCollide = true;
					
					// remove sprint
//						if(sprint)
//							stopSprint = true;
				}
			}
			
			// collision y
			if(dir.y != 0.0D)
			{
				if(MathHelper.rectBetweenRect(pos.x, pos.y+dir.y, WorldSize, WorldSize, entity.pos.x, entity.pos.y, WorldSize, WorldSize))
				{
					if(dir.y >= 0.0D)
					{
						dir.y = entity.pos.y - (pos.y + WorldSize);
						
						didCollideEntityY = true;
						inAir = false;
						didDoubleJump = false;
					}
					else
						dir.y = (entity.pos.y + WorldSize) - pos.y;
					
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
		if(world.checkCollision(pos.x + dir.x, pos.y + dir.y, WorldSize, WorldSize))
		{
			hitByTrampoline = false;
			
			boolean didCollide = false;
			
			// collision x
			if(dir.x != 0.0D)
			{
				if(world.checkCollision(pos.x + dir.x, pos.y, WorldSize, WorldSize))
				{
					if(dir.x >= 0.0D)
					{
						double nextPos = pos.x + WorldSize + dir.x;
						
						pos.x = (int)(nextPos) - WorldSize;
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
					
					// remove sprint
					if(sprint)
						stopSprint = true;
				}
			}
			
			// collision y
			if(dir.y != 0.0D)
			{
				if(world.checkCollision(pos.x, pos.y + dir.y, WorldSize, WorldSize))
				{
					if(dir.y >= 0.0D)
					{
						pos.y = (int)(pos.y + WorldSize + dir.y) - WorldSize;
						
						inAir = false;
						didDoubleJump = false;
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
		
		handleLife();
		
		lastJump = jump;
		lastLeftDown = leftDown;
		
		if(levelTimeStarted)
			levelTime++;
	}
	
/** handles the regeneration / poison */
	private void handleLife()
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
		if(!world.game.openWorld)
		{
//			respawn();
			kill();
			return;
		}
		
		hp--;
		
		if(hp <= 0)
		{
			kill();
			return;
		}
	}
	
	@Override
	public void kill()
	{
		for(int i = 0; i < 8; i++)
			world.addBlood(pos.plus(new Vector2D(WorldSize/2, WorldSize*0.8)), new Vector2D(0, -0.04 - i*0.004), 10);
		
		super.kill();
		
		world.game.onPlayerDied();
		
		SoundManager.die.play();
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
		// don't render if hidePlayers is active (2)
		if(Options.hidePlayers == 2 && player != world.getLocalPlayer())
			return;
		
		int drawX = (int)(pos.x * Game.BlockSize) - xOffset;
		int drawY = (int)(pos.y * Game.BlockSize) - yOffset;
		
		// draw body
		g.setColor(Game.AvailableColors[player.color]);
		g.fillRect(drawX, drawY, DrawSize, DrawSize);
		
		// draw fist
		if(fistPos != null)
		{
			drawX = (int)(fistPos.x * Game.BlockSize) - xOffset;
			drawY = (int)(fistPos.y * Game.BlockSize) - yOffset;
			
			int drawSize = 13;
			g.fillRect(drawX - drawSize/2, drawY - drawSize/2, drawSize, drawSize);
		}
		
		// render name
		if(Options.hidePlayers == 0 && player != world.getLocalPlayer() && (world.playerInsideHidden(this) == -1 || world.playerInsideHidden(this)==world.playerInsideHidden(world.getLocalPlayer().entity)))
		{
			drawX = (int)(pos.x * Game.BlockSize) + DrawSize/2 - xOffset;
			drawY = (int)(pos.y * Game.BlockSize) - 25 - yOffset;
			
			g.setColor(Color.black);
			GuiLabel.draw(g, player.name, drawX, drawY, GuiLabel.ALIGNMENT_MIDDLE, 20);
		}
	}
	
	public final Player player;
	
	public boolean inAir;
	public boolean didDoubleJump;
	
	public boolean hitByTrampoline;
	
	/** The remaining hp */
	public int hp;
	public final int maxHP;
	
	// controls
	private boolean lastJump;
	private boolean lastLeftDown;
	
	private boolean stopSprint;
	
	// status
	private int hpPoison;
	private int poisonTime;
	private int lastPoisened;
	private int hpRegen;
	private int regenTime;
	private int lastRegen;
	
	// fist
	private int fistTime;
	private Vector2D fistPos;
	private boolean fistHit; // if the fist already hit an enemy or not
	private final int fistTimeOutside = Game.TicksPerSec/6;
	private final double fistDistance = 1.075D;
	
	// level stuff
	private boolean levelTimeStarted;
	public int levelTime;
	
	public GameWorld world;
	
	private Vector2D dirMouse;
	
	public static final int DrawSize = 28;
	public static final double WorldSize = DrawSize / (double)Game.BlockSize;
}