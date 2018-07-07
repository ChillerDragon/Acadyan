package krisko.acadyan;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Random;

public class World
{
	public World()
	{
		mapIndexNull = 0;
	}
	
	public void reset()
	{
		chunks.clear();
		entities.clear();
		pStart = null;
		pGoal = null;
		mapIndexNull = 0;
		System.gc(); // just 4 fun
	}
	
/**
 * Real chunk amount = "1 + chunkAmount * 2
 * @param chunkAmount
 */
	public void generateStart(int chunkAmount)
	{
		chunks.clear();
		generateNew(-chunkAmount, chunkAmount);
	}
	
	public void generateNew(int from, int to)
	{
		if(from > to)
		{
			int tmp = from;
			from = to;
			to = tmp;
		}
		
		for(int i = from; i <= to; i++)
		{
			int chunkIndex = 0;
			boolean addChunk = false;
			
			if(chunks.size() > 0)
			{
				int indexLeft = chunks.get(0).getMapIndex();
				int indexRight = chunks.get(chunks.size()-1).getMapIndex();
				
				if(i < indexLeft)
				{
					chunkIndex = 0;
					addChunk = true;
				}
				else if(i > indexRight)
				{
					chunkIndex = chunks.size();
					addChunk = true;
				}
			}
			else
			{
				addChunk = true;
			}
			
			if(!addChunk)
				continue;
			
			Chunk chunk = new Chunk(i);
			chunks.add(chunkIndex, chunk);
			
			if(i == 0)
				mapIndexNull = chunkIndex;
			else if(chunkIndex <= mapIndexNull)
				mapIndexNull++;
			
			// generate blocks
			Chunk leftChunk = chunkIndex-1 >= 0 ? chunks.get(chunkIndex-1) : null;
			Chunk rightChunk = chunkIndex+1 <= chunks.size()-1 ? chunks.get(chunkIndex+1) : null;
			
			chunk.generateBlocks(leftChunk, rightChunk);
			
			// i should make more comments...
		}
	}
	
	public void addChunks(int amount)
	{
		int lastMapIndex = -1;
		
		if(chunks.size() > 0)
			lastMapIndex = chunks.get(chunks.size()-1).getMapIndex();
		
		for(int i = 1; i <= amount; i++)
			chunks.add(new Chunk(lastMapIndex+i));
	}
	
	public void createNewChunks(int from, int to)
	{
		if(from > to)
		{
			int tmp = from;
			from = to;
			to = tmp;
		}
		
		for(int i = from; i <= to; i++)
		{
			int chunkIndex = 0;
			boolean addChunk = false;
			
			if(chunks.size() > 0)
			{
				int indexLeft = chunks.get(0).getMapIndex();
				int indexRight = chunks.get(chunks.size()-1).getMapIndex();
				
				if(i < indexLeft)
				{
					chunkIndex = 0;
					addChunk = true;
				}
				else if(i > indexRight)
				{
					chunkIndex = chunks.size();
					addChunk = true;
				}
			}
			else
			{
				addChunk = true;
			}
			
			if(!addChunk)
				continue;
			
			Chunk chunk = new Chunk(i);
			chunks.add(chunkIndex, chunk);
			
			if(i == 0)
				mapIndexNull = chunkIndex;
			else if(chunkIndex <= mapIndexNull)
				mapIndexNull++;
		}
	}
	
//
	public void update(InputHandler handler, int xOffset, int yOffset)
	{
		// update blocks
		int left = xOffset / Chunk.WIDTH - 1 - 32;
		int right = (xOffset + Game.ScreenWidth) / Chunk.WIDTH + 32;
		
		for(int i = left; i <= right; i++)
		{
			for(int j = 0; j < Chunk.HEIGHT; j++)
			{
				BlockData data = getBlockData(0, i, j);
				if(data != null)
					data.update(this, i, j);
				
				data = getBlockData(1, i, j);
				if(data != null)
					data.update(this, i, j);
			}
		}
		
		// update entities
		for(int i = 0; i < entities.size(); i++)
		{
			Entity entity = entities.get(i);
			if(!entity.dead)
			{
				entity.update();
				
				if(entity.pos.y >= Chunk.HEIGHT*2)
				{
					entity.kill();
					continue;
				}
				
				int l = (int)entity.pos.x;
				if(entity.pos.x < 0.0D)
					l--;
				int r = (int)(entity.pos.x + EntityPlayer.WorldSize);
				if(entity.pos.x + EntityPlayer.WorldSize < 0.0D || entity.pos.x + EntityPlayer.WorldSize == r)
					r--;
				int t = (int)entity.pos.y;
				if(entity.pos.y < 0.0D)
					t--;
				int b = (int)(entity.pos.y + EntityPlayer.WorldSize);
				if(entity.pos.y + EntityPlayer.WorldSize < 0.0D || entity.pos.y + EntityPlayer.WorldSize == b)
					b--;
				
				// update block data
				for(int y = t; y <= b; y++)
				{
					for(int x = l; x <= r; x++)
					{
						BlockData data = getBlockData(0, x, y);
						if(data != null)
							data.onEnter(this, entity, x, y);
						
						data = getBlockData(1, x, y);
						if(data != null)
							data.onEnter(this, entity, x, y);
					}
				}
			}
		}
		
		// update (GameWorld)
		update(handler);
		
		// remove dead entities
		for(int i = 0; i < entities.size(); i++)
		{
			if(entities.get(i).dead)
			{
				onEntityRemoved(entities.get(i));
				entities.remove(i--);
			}
		}
		
		tick++;
	}
	
	public void update(InputHandler handler)
	{
	}
	
//
	public final void addEntity(Entity entity)
	{
		onEntityAdded(entity);
		entities.add(entity);
	}
	
	public void onEntityAdded(Entity entity)
	{
	}
	
	public final void removeEntity(Entity entity)
	{
		onEntityRemoved(entity);
		entities.remove(entity);
	}
	
	public void onEntityRemoved(Entity entity)
	{
	}
	
//
	public int getLeftX()
	{
		if(chunks.size() == 0)
			return 0;
		
		return chunks.get(0).getMapIndex() * Chunk.WIDTH;
	}
	
	public int getRightX()
	{
		if(chunks.size() == 0)
			return 0;
		
		return chunks.get(chunks.size()-1).getMapIndex() * Chunk.WIDTH + Chunk.WIDTH - 1;
	}
	
	public int getLeftMapIndex()
	{
		if(chunks.size() == 0)
			return 0;
		
		return chunks.get(0).getMapIndex();
	}
	
	public int getRightMapIndex()
	{
		if(chunks.size() == 0)
			return 0;
		
		return chunks.get(chunks.size()-1).getMapIndex();
	}
	
	public int getMapIndexFromBlockX(int x)
	{
		int mapIndex = x / Chunk.WIDTH;
		
		if(x < 0)
			mapIndex = (x+1) / Chunk.WIDTH - 1;
		
		return mapIndex;
	}
	
	public Chunk getChunk(int mapIndex)
	{
		int chunkIndex = mapIndexNull + mapIndex;
		
		if(chunkIndex < 0 || chunkIndex >= chunks.size())
			return null;
		
		return chunks.get(chunkIndex);
	}
	
	public void setBlock(int x, int y, Block block)
	{
		setBlock(x, y, block, 0);
	}
	
	public void setBlock(int layer, int x, int y, Block block)
	{
		setBlock(layer, x, y, block, 0);
	}
	
	public void setBlock(int x, int y, Block block, int metadata)
	{
		setBlock(block.getLayer(), x, y, block, metadata);
	}
	
	public void setBlock(int layer, int x, int y, Block block, int metadata)
	{
		int blockX = x % Chunk.WIDTH;
		int blockY = y;
		if(x < 0)
			blockX = Chunk.WIDTH - 1 + ((x+1) % Chunk.WIDTH);
		
		Chunk chunk = getChunk(getMapIndexFromBlockX(x));
		if(chunk == null)
			return;
		
		// remove start / goal
		if(chunk.get(0, blockX, blockY) == Block.start)
			pStart = null;
		else if(chunk.get(0, blockX, blockY) == Block.goal)
			pGoal = null;
		
		// set block
		chunk.set(layer, blockX, blockY, block);
		chunk.setMetadata(layer, blockX, blockY, metadata);
		chunk.setBlockData(layer, blockX, blockY, block.createBlockData());
		
		// add start / goal
		if(block == Block.start)
		{
			if(pStart != null)
				setBlock(pStart.x, pStart.y, Block.air);
			pStart = new Point(x, y);
		}
		else if(block == Block.goal)
		{
			if(pGoal != null)
				setBlock(pGoal.x, pGoal.y, Block.air);
			pGoal = new Point(x, y);
		}
	}
	
	public Block getBlock(int layer, double x, double y)
	{
		return getBlock(layer, (int)(x >= 0.0D ? x : x-1), (int)(y >= 0.0D ? y : y-1));
	}
	
	public Block getBlock(int layer, int x, int y)
	{
		int blockX = x % Chunk.WIDTH;
		int blockY = y;
		if(x < 0)
			blockX = Chunk.WIDTH - 1 + ((x+1) % Chunk.WIDTH);
		
		int mapIndex = x / Chunk.WIDTH;
		
		if(x < 0)
			mapIndex = (x+1) / Chunk.WIDTH - 1;
		
		Chunk chunk = getChunk(mapIndex);
		if(chunk == null)
			return Block.air;
		
		return chunk.get(layer, blockX, blockY);
	}
	
	public void setMetadata(int layer, int x, int y, int metadata)
	{
		int blockX = x % Chunk.WIDTH;
		int blockY = y;
		if(x < 0)
			blockX = Chunk.WIDTH - 1 + ((x+1) % Chunk.WIDTH);
		
		Chunk chunk = getChunk(getMapIndexFromBlockX(x));
		if(chunk == null)
			return;
		
		// set block
		chunk.setMetadata(layer, blockX, blockY, metadata);
	}
	
	public int getMetadata(int layer, int x, int y)
	{
		int blockX = x % Chunk.WIDTH;
		int blockY = y;
		if(x < 0)
			blockX = Chunk.WIDTH - 1 + ((x+1) % Chunk.WIDTH);
		
		int mapIndex = x / Chunk.WIDTH;
		
		if(x < 0)
			mapIndex = (x+1) / Chunk.WIDTH - 1;
		
		Chunk chunk = getChunk(mapIndex);
		if(chunk == null)
			return 0;
		
		return chunk.getMetadata(layer, blockX, blockY);
	}
	
	public void setBlockData(int layer, int x, int y, BlockData blockData)
	{
		int blockX = x % Chunk.WIDTH;
		int blockY = y;
		if(x < 0)
			blockX = Chunk.WIDTH - 1 + ((x+1) % Chunk.WIDTH);
		
		Chunk chunk = getChunk(getMapIndexFromBlockX(x));
		if(chunk == null)
			return;
		
		// set block
		chunk.setBlockData(layer, blockX, blockY, blockData);
	}
	
	public BlockData getBlockData(int layer, double x, double y)
	{
		return getBlockData(layer, (int)(x >= 0.0D ? x : x-1), (int)(y >= 0.0D ? y : y-1));
	}
	
	public BlockData getBlockData(int layer, int x, int y)
	{
		int blockX = x % Chunk.WIDTH;
		int blockY = y;
		if(x < 0)
			blockX = Chunk.WIDTH - 1 + ((x+1) % Chunk.WIDTH);
		
		int mapIndex = x / Chunk.WIDTH;
		
		if(x < 0)
			mapIndex = (x+1) / Chunk.WIDTH - 1;
		
		Chunk chunk = getChunk(mapIndex);
		if(chunk == null)
			return null;
		
		return chunk.getBlockData(layer, blockX, blockY);
	}
	
	public boolean checkCollision(double x, double y, double width, double height)
	{
		return checkCollision(x, y, width, height, Block.solid);
	}
	
	public boolean checkCollision(double x, double y, double width, double height, Block block)
	{
		// extras
		if(block == Block.acid)
		{
			height -= 0.1D;
		}
		
		// TODO: Set correct collision on acid with metadata > 0
		
		int left = (int)(x >= 0.0D ? x : x-1.0D);
		int right = (int)(x+width >= 0.0D ? x+width : x+width-1.0D);
		int top = (int)(y >= 0.0D ? y : y-1.0D);
		int bottom = (int)(y+height >= 0.0D ? y+height : y+height-1.0D);
		
		// correct left
		if(x < 0.0D && left == x-1.0D)
			left++;
		
		// correct right
		if(x+width >= 0.0D && right == x+width)
			right--;
		
		// correct top
		if(y < 0.0D && top == y-1.0D)
			top++;
		
		// correct bottom
		if(y+height >= 0.0D && bottom == y+height)
			bottom--;
		
		// check the collision
		for(int i = left; i <= right; i++)
		{
			for(int j = top; j <= bottom; j++)
			{
				if((block == Block.solid && (getBlock(0, i, j).isSolid()) || getBlock(1, i, j).isSolid()) || getBlock(block.getLayer(), i, j) == block)
				{
					// some extras
					if(block == Block.spike)
					{
						int d = Game.BlockSize;
						
						int[] xPoints;
						int[] yPoints;
						
						switch(getMetadata(block.getLayer(), i, j))
						{
							case 0: {
								xPoints = new int[] { i*d, i*d+d/2, i*d+d };
								yPoints = new int[] { j*d+d, j*d, j*d+d };
								break;
							}
							
							case 1: {
								xPoints = new int[] { i*d, i*d+d, i*d };
								yPoints = new int[] { j*d, j*d+d/2, j*d+d };
								break;
							}
							
							case 2: {
								xPoints = new int[] { i*d, i*d+d/2, i*d+d };
								yPoints = new int[] { j*d, j*d+d, j*d };
								break;
							}
							
							default: {
								xPoints = new int[] { i*d+d, i*d, i*d+d };
								yPoints = new int[] { j*d, j*d+d/2, j*d+d };
								break;
							}
						}
						
						Polygon p = new Polygon(xPoints, yPoints, 3);
						
						if(p.intersects(x*d, y*d, width*d, height*d))
							return true;
					}
					else if(block == Block.laser)
					{
						DataBoolean data = (DataBoolean)getBlockData(Block.door.getLayer(), i, j).get(0);
						if(data.value)
							return true;
					}
					else if(getBlock(Block.breaking.getLayer(), i, j) == Block.breaking)
					{
						DataBoolean data = (DataBoolean)getBlockData(Block.breaking.getLayer(), i, j).get(3);
						if(data.value)
							return true;
					}
					else if(getBlock(Block.door.getLayer(), i, j) == Block.door)
					{
						DataBoolean data = (DataBoolean)getBlockData(Block.door.getLayer(), i, j).get(0);
						if(!data.value)
							return true;
					}
					else
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public int size() { return chunks.size(); }
	
// Save / Load
	public boolean save(String mapName)
	{
		return WorldLoader.save(this, mapName, false);
	}
	
	public boolean save(String mapName, boolean giveWarningWhenMapExists)
	{
		return WorldLoader.save(this, mapName, giveWarningWhenMapExists);
	}
	
	public boolean loadExtern(String mapName)
	{
		return WorldLoader.loadExtern(this, mapName);
	}
	
	public boolean loadIntern(String mapName)
	{
		return WorldLoader.loadIntern(this, mapName);
	}
	
//
	public int tick() { return tick; }
	
	public void addBlood(Vector2D pos, Vector2D dir, int amount)
	{
		if(!Options.blood)
			return;
		
		Random random = new Random();
		double angle = dir.getAngle();
		double dif = 5.0D * Math.PI / 180.0D;
		
		for(int i = -amount/2; i <= amount/2; i++)
		{
			if(i == 0 && amount % 2 == 0)
				continue;
			
			int size = 4 + random.nextInt(3);
			double angleRandomDif = random.nextDouble()/2 - 0.25D;
			
			Vector2D newDir = new Vector2D(angle + dif*i + (amount % 2 == 1 ? 0.0D : i < 0 ? dif/2.0D : -dif/2.0D) + angleRandomDif);
			newDir.multiply(dir.getDistance());
			newDir.add((new Vector2D(angle)).mul((2-(size-4))*0.008D));
			addEntity(new EntityBlood(this, pos, newDir, size));
		}
	}
	
// Draw
	public int playerInsideHidden(EntityPlayer player)
	{
		return -1;
	}
	
	public Player getLocalPlayer()
	{
		return null;
	}
	
	public void draw(Graphics g, int xOffset, int yOffset)
	{
		draw(g, 0, 0, Game.ScreenWidth, Game.ScreenHeight, xOffset, yOffset, 1.0D, false);
	}
	
	public void draw(Graphics g, int drawX, int drawY, int drawWidth, int drawHeight, int xOffset, int yOffset, double scale, boolean isEditor)
	{
		g.setClip(drawX, drawY, drawWidth, drawHeight);
		
		xOffset -= drawX;
		yOffset -= drawY;
		
		// render blocks
		int left = (int)(xOffset/scale) / Chunk.WIDTH - 1;
		int right = (int)(xOffset/scale + drawWidth/scale) / Chunk.WIDTH;
		int blockSize = (int)(Game.BlockSize*scale);
		
		// draw background layer
		drawBlocks(g, xOffset, yOffset, left, right, blockSize, 0, isEditor);
		
		// render entities
		for(int i = 0; i < entities.size(); i++)
			if(!(entities.get(i) instanceof EntityPlayer))
				entities.get(i).draw(g, xOffset, yOffset);
		
		// render players
		drawPlayer(g, xOffset, yOffset);
		
		// draw foreground layer
		drawBlocks(g, xOffset, yOffset, left, right, blockSize, 1, isEditor);
		
		g.setClip(null);
	}
	
	private void drawBlocks(Graphics g, int xOffset, int yOffset, int left, int right, int blockSize, int layer, boolean isEditor)
	{
		Graphics2D g2 = (Graphics2D)g;
		Composite composite = g2.getComposite();
		
		if(isEditor && layer == 1)
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
		
		Block block;
		for(int i = left; i <= right; i++)
		{
			for(int j = 0; j < Chunk.HEIGHT; j++)
			{
				block = getBlock(layer, i, j);
				int e = isEditor ? -1 : 1; // multiply the standard infoData with this (< 0 = editor, > 0 = normal world)
				
				if(block == Block.goal)
				{
					block.draw(g, i*blockSize - blockSize/2 - xOffset, j*blockSize - blockSize/2 - yOffset, blockSize*2, blockSize*2, getMetadata(layer, i, j), e);
				}
				else if(block == Block.trampoline)
				{
					// move trampoline texture 5 pixels up when it hits the player
					DataValue data = (DataValue)getBlockData(layer, i, j).get(1);
					int startTick = data.value;
					int move = MathHelper.clamp((Game.tick() - startTick), 0, 32);
					if(move > 16)
						move = 32 - move;
					
					int metadata = getMetadata(layer, i, j); // 0=up, 1=right, 2=down, 3=left
					int moveX = metadata == 1 ? move : metadata == 3 ? -move : 0;
					int moveY = metadata == 0 ? -move : metadata == 2 ? move : 0;
					
					block.draw(g, i*blockSize + moveX - xOffset, j*blockSize + moveY - yOffset, blockSize, blockSize, getMetadata(layer, i, j), e);
				}
				else if(block == Block.hidden)
				{
					DataValue data = (DataValue)getBlockData(layer, i, j).get(0);
					int playerInside = (getLocalPlayer() != null && getLocalPlayer().entity != null) ? playerInsideHidden(getLocalPlayer().entity) : -1;
					boolean drawTransparent = playerInside == data.value;
					block.draw(g, i*blockSize - xOffset, j*blockSize - yOffset, blockSize, blockSize, getMetadata(layer, i, j), (drawTransparent ? 2 : 1) * e);
				}
				else if(block == Block.breaking)
				{
					DataBoolean data = (DataBoolean)getBlockData(layer, i, j).get(3);
					block.draw(g, i*blockSize - xOffset, j*blockSize - yOffset, blockSize, blockSize, getMetadata(layer, i, j), (data.value ? 2 : 1) * e);
				}
				else if(block == Block.pressurePlate)
				{
					BlockDataPressurePlate blockData = (BlockDataPressurePlate)getBlockData(layer, i, j);
					block.draw(g, i*blockSize - xOffset, j*blockSize - yOffset, blockSize, blockSize, getMetadata(layer, i, j), (blockData.entityInside ? 2 : 1) * e);
				}
				else if(block == Block.door)
				{
					DataBoolean data = (DataBoolean)getBlockData(layer, i, j).get(0);
					block.draw(g, i*blockSize - xOffset, j*blockSize - yOffset, blockSize, blockSize, getMetadata(layer, i, j), (data.value ? 2 : 1) * e);
				}
				else if(block == Block.laser)
				{
					int metadata = getMetadata(layer, i, j);
					int xNext = metadata;
					int yNext = metadata^1;
					
					int infoData = 1;
					if(getBlock(layer, i+xNext, j-yNext) == block && getMetadata(layer, i+xNext, j-yNext) == metadata)
					{
						if(getBlock(layer, i-xNext, j+yNext) == block && getMetadata(layer, i-xNext, j+yNext) == metadata)
							infoData = 3;
						else
							infoData = 4;
					}
					else if(getBlock(layer, i-xNext, j+yNext) == block && getMetadata(layer, i-xNext, j+yNext) == metadata)
						infoData = 2;

					DataBoolean data = (DataBoolean)getBlockData(layer, i, j).get(0);
					if(!data.value)
						infoData += 4;
					
					block.draw(g, i*blockSize - xOffset, j*blockSize - yOffset, blockSize, blockSize, metadata, infoData * e);
				}
				else
				{
					block.draw(g, i*blockSize - xOffset, j*blockSize - yOffset, blockSize, blockSize, getMetadata(layer, i, j), e);
				}
			}
		}
		
		g2.setComposite(composite);
	}
	
	public void drawPlayer(Graphics g, int xOffset, int yOffset)
	{
	}
	
	private int tick;
	
	public final double gravity = 0.005D;
	
	// map
	private ArrayList<Chunk> chunks = new ArrayList<Chunk>();
	private int mapIndexNull; // chunks.get(mapIndexNull) gibt den chunk mit dem mapIndex = 0 aus (-> anfangschunk)
	
	protected ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public Point pStart;
	public Point pGoal;
}