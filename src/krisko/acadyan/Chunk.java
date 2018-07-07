package krisko.acadyan;

import java.util.Random;

public class Chunk
{
	public Chunk(int mapIndex)
	{
		index = mapIndex;
	}
	
	public void generateBlocks(Chunk leftChunk, Chunk rightChunk)
	{
		// set air blocks
		for(int i = 0; i < WIDTH; i++)
		{
			for(int j = 0; j < HEIGHT; j++)
			{
				set(0, i, j, Block.air);
				set(1, i, j, Block.air);
			}
		}
		
		Random random = new Random();
		int from = 0;
		int to = WIDTH;
		
		int lastTopY = 5; // this is where the last solid block was
		
		final int minCoinGap = 2;
		final int maxCoinGap = 8;
		int lastCoin = -minCoinGap - 1;
		
		final int minPotGap = 50;
		final int maxPotGap = 100;
		int lastPot = -minPotGap - 1;
		
		if(leftChunk != null)
		{
			// connect with left chunk
			// set lastTopY
			for(int j = 0; j < HEIGHT; j++)
			{
				if(leftChunk.get(0, WIDTH-1, j).isSolid())
				{
					lastTopY = j;
					break;
				}
			}
			
			// set lastCoin
			loop1:
			for(int i = WIDTH-1; i >= 0; i--)
			{
				for(int j = 0; j < HEIGHT; j++)
				{
					if(leftChunk.get(0, i, j) == Block.coin)
					{
						lastCoin = WIDTH - i;
						break loop1;
					}
					else if(leftChunk.get(0, i, j).isSolid())
						break;
				}
			}
			
			// set lastPot
			loop1:
			for(int i = WIDTH-1; i >= 0; i--)
			{
				for(int j = 0; j < HEIGHT; j++)
				{
					Block block = leftChunk.get(0, i, j);
					if(block == Block.potion || block == Block.poison || block == Block.regeneration)
					{
						lastPot = WIDTH - i;
						break loop1;
					}
					else if(leftChunk.get(0, i, j).isSolid())
						break;
				}
			}
		}
		else if(rightChunk != null)
		{
			// connect with right chunk
			// set lastTopY
			for(int j = 0; j < HEIGHT; j++)
			{
				if(rightChunk.get(0, 0, j).isSolid())
				{
					lastTopY = j;
					break;
				}
			}
			
			// set lastCoin
			loop1:
			for(int i = 0; i < WIDTH; i++)
			{
				for(int j = 0; j < HEIGHT; j++)
				{
					if(rightChunk.get(0, i, j) == Block.coin)
					{
						lastCoin = WIDTH + i;
						break loop1;
					}
					else if(rightChunk.get(0, i, j).isSolid())
						break;
				}
			}
			
			// set lastPot
			loop1:
			for(int i = WIDTH-1; i >= 0; i--)
			{
				for(int j = 0; j < HEIGHT; j++)
				{
					Block block = rightChunk.get(0, i, j);
					if(block == Block.potion || block == Block.poison || block == Block.regeneration)
					{
						lastPot = WIDTH + i;
						break loop1;
					}
					else if(rightChunk.get(0, i, j).isSolid())
						break;
				}
			}
			
			from = WIDTH-1;
			to = -1;
		}
		
		// generate solid blocks
		for(int i = from; i != to; i += to >= from ? 1 : -1)
		{
			int r = random.nextInt(100);
			int newTopY = lastTopY + (r < 76 ? 0 : r < 88 ? 1 : -1);
			
			if(newTopY < 1) // blocks[0][*] will ONLY be coins
				newTopY = 1;
			else if(newTopY > 15)
				newTopY = 15;
			
			lastTopY = newTopY;
			
			for(int j = newTopY; j < HEIGHT; j++)
			{
				// set blocks that HAVE to be there
				if(j >= HEIGHT - 2)
				{
					set(i, j, Block.solid);
					continue;
				}
				
				set(i, j, Block.solid);
			}
		}
		
		// place acid
//		for(int i = from; i != to; i += to >= from ? 1 : -1)
//		{
//			for(int j = HEIGHT-1; j >= 0; j--)
//			{
//				if(blocks[j][i] == Block.air)
//				{
//					blocks[j][i] = Block.acid;
//					break;
//				}
//			}
//		}
		
		// place coins
		for(int i = from; i != to; i += to >= from ? 1 : -1)
		{
			if(Math.abs(i - lastCoin) <= minCoinGap)
				continue;
			
			if(Math.abs(i - lastCoin) <= maxCoinGap && random.nextInt(5) > 0)
				continue;
			
			lastCoin = i;
			
			for(int j = HEIGHT-1; j >= 0; j--)
			{
				if(get(0, i, j) == Block.air)
				{
					set(i, j, Block.coin);
					break;
				}
			}
		}
		
		// place pots
		for(int i = from; i != to; i += to >= from ? 1 : -1)
		{
			if(Math.abs(i - lastPot) <= minPotGap)
				continue;
			
			if(Math.abs(i - lastPot) <= maxPotGap && random.nextInt(20) > 0)
				continue;
			
			lastPot = i;
			
			for(int j = HEIGHT-1; j >= 0; j--)
			{
				if(get(0, i, j) == Block.air)
				{
					int r = (new Random()).nextInt(3);
					set(i, j, r == 0 ? Block.potion : r == 1 ? Block.poison : Block.regeneration);
					break;
				}
			}
		}
	}
	
	public void set(int x, int y, Block block)
	{
		set(block.getLayer(), x, y, block);
	}
	
	public void set(int layer, int x, int y, Block block)
	{
		if(layer < 0 || layer > 1 || x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT)
			return;
		
		blocks[layer][y][x] = block;
	}
	
	public Block get(int layer, int x, int y)
	{
		if(layer < 0 || layer > 1 || x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT || blocks[layer][y][x] == null)
			return Block.air;
		
		return blocks[layer][y][x];
	}
	
	public void setMetadata(int layer, int x, int y, int data)
	{
		if(layer < 0 || layer > 1 || x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT)
			return;
		
		metadata[layer][y][x] = data;
	}
	
	public int getMetadata(int layer, int x, int y)
	{
		if(layer < 0 || layer > 1 || x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT)
			return 0;
		
		return metadata[layer][y][x];
	}
	
	public void setBlockData(int layer, int x, int y, BlockData data)
	{
		if(layer < 0 || layer > 1 || x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT)
			return;
		
		blockData[layer][y][x] = data;
	}
	
	public BlockData getBlockData(int layer, int x, int y)
	{
		if(layer < 0 || layer > 1 || x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT)
			return null;
		
		return blockData[layer][y][x];
	}
	
	public int getMapIndex() { return index; }
	
	private int index;
	
	public static final int WIDTH = 32;
	public static final int HEIGHT = 64;
	private Block[][][] blocks = new Block[2][HEIGHT][WIDTH]; // 2 = layers
	private int[][][] metadata = new int[2][HEIGHT][WIDTH];
	private BlockData[][][] blockData = new BlockData[2][HEIGHT][WIDTH];
}