package krisko.acadyan;

abstract class BlockData
{
	public BlockData(Data... defaultData)
	{
		data = defaultData;
	}
	
	public void update(World world, int x, int y)
	{
	}
	
	/**
	 * This happens when an entity (the player) enteres the block
	 * TODO: Not really "on enter" now, but "on entity inside"
	 */
	public void onEnter(World world, Entity entity, int x, int y)
	{
	}
	
	/**
	 * This happens when an entity is directly above the block
	 * @param world
	 * @param entity - the entity that's directly above the block
	 * @param x - x-coordinate of the block
	 * @param y - y-coordinate of the block
	 */
	public void onEntityAbove(World world, Entity entity, int x, int y)
	{
	}
	
	public int size() { return data.length; }
	
	public Data get(int index) { return data[index]; }
	
	public boolean isVisible(int index) { return data[index].visible; }
	
	public void setData(int index, String data)
	{
		try
		{
			this.data[index].setData(data);
		} catch(Exception ex)
		{ }
	}
	
	public String getData(int index) { return data[index].getData(); }
	
//	public int setValue(int index, int value) { return data[index].value = value; }
	
//	public int getValue(int index) { return data[index].value; }
	
	public String getName(int index) { return data[index].name != null ? data[index].name.get() : data[index].nameStr; }
	
	private Data data[];
}

class BlockDataCannon extends BlockData
{
	public BlockDataCannon()
	{
		super(	new DataValueMinMax(Strings.SPEED, Game.TicksPerSec, 1, Game.TicksPerSec*2),
				new DataValueMinMax(Strings.OFFSET, 0, 0, Game.TicksPerSec));
		/*
		 * 0: shootSpead | how fast the cannon shoots (in ticks)
		 * 1: offset (in ticks)
		 */
	}
	
	@Override
	public void update(World world, int x, int y)
	{
		DataValueMinMax data0 = (DataValueMinMax)get(0);
		DataValueMinMax data1 = (DataValueMinMax)get(1);
		
		if(data0.value == 0 || (world.tick() + data1.value) % data0.value == 0)
			world.addEntity(new EntityBullet(world, new Vector2D(x+0.5D, y+0.5D), world.getMetadata(Block.cannon.getLayer(), x, y)));
	}
}

class BlockDataTeleporter extends BlockData
{
	public BlockDataTeleporter()
	{
		super(new DataPoint("x / y", 0, 0, false));
	}
	
	@Override
	public void onEnter(World world, Entity entity, int x, int y)
	{
		DataPoint data = (DataPoint)get(0);
		
		double teleX = data.x;
		double teleY = data.y;
		
		if(entity instanceof EntityBullet)
		{
			teleX += 0.5D;
			teleY += 0.5D;
		}
		else if(entity instanceof EntityPlayer)
		{
			teleX += 0.5D - EntityPlayer.WorldSize/2;
			teleY += 0.5D - EntityPlayer.WorldSize/2;
		}
		
		entity.setPosition(teleX, teleY);
	}
}

class BlockDataTrampoline extends BlockData
{
	public BlockDataTrampoline()
	{
		super(	new DataValueMinMax(Strings.STRENGTH, 25, 1, 100),
				new DataValue("", 0, false)); // last jump time (for moving the trampoline arrows)
	}
	
	@Override
	public void onEnter(World world, Entity entity, int x, int y)
	{
		if(entity instanceof EntityLiving || entity instanceof EntityPlayer)
		{
			int metadata = world.getMetadata(Block.trampoline.getLayer(), x, y); // 0=up, 1=right, 2=down, 3=left
			
			if(entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)entity;
				
				// give doublejump to player
				if(metadata == 0)
					player.didDoubleJump = false;
				
				// = horizontal trampoline
				if(metadata % 2 == 1)
					player.hitByTrampoline = true;
			}
			
			DataValueMinMax data = (DataValueMinMax)get(0);
			if(metadata == 1)
				entity.dir.x = data.value/100.0D;
			else if(metadata == 2)
				entity.dir.y = data.value/100.0D;
			else if(metadata == 3)
				entity.dir.x = -data.value/100.0D;
			else
				entity.dir.y = -data.value/100.0D;
			
			DataValue dataTick = (DataValue)get(1);
			dataTick.value = Game.tick();
		}
	}
}

class BlockDataHidden extends BlockData
{
	public BlockDataHidden()
	{
		super(new DataValue("Index", 0));
	}
}

class BlockDataBreaking extends BlockData
{
	public BlockDataBreaking()
	{
		super(	new DataValueMinMax(Strings.TIME, Game.TicksPerSec/2, 0, Game.TicksPerSec*2), // time until the block breaks
				new DataValueMinMax(Strings.RESPAWN, Game.TicksPerSec*3, 0, Game.TicksPerSec*10),
				new DataValue("", -1, false), // time, when the player gets on top of it
				new DataBoolean("", true, false)); // if the block is visible or not (destroyed)
	}
	
	@Override
	public void update(World world, int x, int y)
	{
		DataValue dataTick = (DataValue)get(2);
		if(dataTick.value >= 0 && Game.tick() >= dataTick.value)
		{
			DataBoolean dataVisible = (DataBoolean)get(3);
			
			int respawn = ((DataValueMinMax)get(1)).value;
			
			// set visibility of block (if it's destroyed right now or if its 'repaired' again)
			boolean visibleBefore = dataVisible.value;
			dataVisible.value = respawn != 0 && Game.tick() >= dataTick.value + respawn;
			
			if(visibleBefore && !dataVisible.value)
			{
				SoundManager.breaking.play();
			}
			
			if(dataVisible.value)
			{
				for(int i = 0; i < world.entities.size(); i++)
				{
					Entity entity = world.entities.get(i);
					if(!(entity instanceof EntityLiving) && !(entity instanceof EntityPlayer))
						continue;
					
					if(MathHelper.rectBetweenRect(entity.pos.x, entity.pos.y, EntityPlayer.WorldSize, EntityPlayer.WorldSize, x, y, 1, 1))
					{
						dataVisible.value = false;
						return;
					}
				}
				
				// reset timer
				dataTick.value = -1;
			}
		}
	}
	
	@Override
	public void onEntityAbove(World world, Entity entity, int x, int y)
	{
		if(entity instanceof EntityLiving || entity instanceof EntityPlayer)
		{
			DataValue dataTick = (DataValue)get(2);
			
			if(dataTick.value == -1)
			{
				DataValueMinMax data = (DataValueMinMax)get(0);
				dataTick.value = Game.tick() + data.value;
			}
		}
	}
}

class BlockDataPressurePlate extends BlockData
{
	public BlockDataPressurePlate()
	{
		super(new DataPoint("x / y", 0, 0, false));
	}
	
	@Override
	public void update(World world, int x, int y)
	{
		boolean inside = false;
		
		for(int i = 0; i < world.entities.size(); i++)
		{
			Entity entity = world.entities.get(i);
			
			if(!(entity instanceof EntityLiving) && !(entity instanceof EntityPlayer))
				continue;
			
			if(MathHelper.rectBetweenRect(entity.pos.x, entity.pos.y, EntityPlayer.WorldSize, EntityPlayer.WorldSize, x, y+0.75D, 1, 0.25D))
			{
				inside = true;
				break;
			}
		}
		
		if(inside && !entityInside)
		{
			// switch block
			DataPoint point = (DataPoint)get(0);
			BlockData blockData = world.getBlockData(Block.pressurePlate.getLayer(), point.x, point.y);
			
			if(blockData != null && blockData instanceof BlockDataSwitchable)
				((BlockDataSwitchable)blockData).doSwitch(world, point.x, point.y);
			
			SoundManager.pressurePlateOn.play();
		}
		else if(!inside && entityInside)
		{
			SoundManager.pressurePlateOff.play();
		}
		
		entityInside = inside;
	}
	
	public boolean entityInside;
}

class BlockDataSwitchable extends BlockData
{
	public BlockDataSwitchable(boolean value)
	{
		super(new DataBoolean("", value, false));
		startValue = value;
	}
	
	@Override
	public void update(World world, int x, int y)
	{
		DataBoolean data = (DataBoolean)get(0);
		if(data.value == startValue)
		{
			time = 0;
		}
		else if(time > 0)
		{
			if(Game.tick() >= time + Game.TicksPerSec*10)
			{
				time = 0;
				data.value = startValue;
			}
		}
	}
	
	public void doSwitch(World world, int x, int y)
	{
		doSwitch(world, x, y, true);
	}
	
	public void doSwitch(World world, int x, int y, boolean switchNearby)
	{
		time = Game.tick();
		DataBoolean data = (DataBoolean)get(0);
		data.value = !data.value;
		
		if(!switchNearby)
			return;
		
		Block block = world.getBlock(Block.pressurePlate.getLayer(), x, y);
		int metadata = world.getMetadata(block.getLayer(), x, y);
		
		if(metadata == 0)
		{
			// go up
			for(int j = y-1; j >= 0; j--)
			{
				if(world.getBlock(block.getLayer(), x, j) != block || world.getMetadata(block.getLayer(), x, j) != metadata)
					break;
				
				((BlockDataSwitchable)world.getBlockData(block.getLayer(), x, j)).doSwitch(world, x, j, false);
			}
			
			// go down
			for(int j = y+1; j < Chunk.HEIGHT; j++)
			{
				if(world.getBlock(block.getLayer(), x, j) != block || world.getMetadata(block.getLayer(), x, j) != metadata)
					break;
				
				((BlockDataSwitchable)world.getBlockData(block.getLayer(), x, j)).doSwitch(world, x, j, false);
			}
		}
		else if(metadata == 1)
		{
			// go left
			int i = x-1;
			while(true)
			{
				if(world.getBlock(block.getLayer(), i, y) != block || world.getMetadata(block.getLayer(), i, y) != metadata)
					break;
				
				((BlockDataSwitchable)world.getBlockData(block.getLayer(), i, y)).doSwitch(world, i, y, false);
				i--;
			}
			
			// go right
			i = x+1;
			while(true)
			{
				if(world.getBlock(block.getLayer(), i, y) != block || world.getMetadata(block.getLayer(), i, y) != metadata)
					break;
				
				((BlockDataSwitchable)world.getBlockData(block.getLayer(), i, y)).doSwitch(world, i, y, false);
				i++;
			}
		}
	}
	
	private int time;
	private boolean startValue;
}

//
abstract class Data
{
	protected Data(Strings name, boolean visible)
	{
		this.name = name;
		this.nameStr = null;
		this.visible = visible;
	}
	
	protected Data(Strings name)
	{
		this(name, true);
	}
	
	protected Data(String name, boolean visible)
	{
		this.name = null;
		this.nameStr = name;
		this.visible = visible;
	}
	
	protected Data(String name)
	{
		this(name, true);
	}
	
	public void setData(String data)
	{
	}
	
	public String getData()
	{
		return "";
	}
	
	protected final String nameStr;
	protected final Strings name;
	
	protected final boolean visible;
}

class DataBoolean extends Data
{
	protected DataBoolean(String name, boolean value, boolean visible)
	{
		super(name, visible);
		this.value = value;
	}
	
	protected DataBoolean(String name, boolean value)
	{
		super(name);
		this.value = value;
	}
	
	@Override
	public void setData(String data)
	{
		value = Boolean.parseBoolean(data);
	}
	
	@Override
	public String getData()
	{
		return Boolean.toString(value);
	}
	
	public boolean value;
}

class DataValue extends Data
{
	protected DataValue(String name, int value, boolean visible)
	{
		super(name, visible);
		this.value = value;
	}
	
	protected DataValue(String name, int value)
	{
		super(name);
		this.value = value;
	}
	
	@Override
	public void setData(String data)
	{
		value = Integer.parseInt(data);
	}
	
	@Override
	public String getData()
	{
		return Integer.toString(value);
	}
	
	public int value;
}

class DataValueMinMax extends Data
{
	protected DataValueMinMax(Strings name, int value, int minimum, int maximum, boolean visible)
	{
		super(name, visible);
		this.value = value;
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	protected DataValueMinMax(Strings name, int value, int minimum, int maximum)
	{
		super(name);
		this.value = value;
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	@Override
	public void setData(String data)
	{
		value = Integer.parseInt(data);
	}
	
	@Override
	public String getData()
	{
		return Integer.toString(value);
	}
	
	public int value;
	public final int minimum;
	public final int maximum;
}

class DataPoint extends Data
{
	protected DataPoint(String name, int x, int y, boolean visible)
	{
		super(name, visible);
		this.x = x;
		this.y = y;
	}
	
	protected DataPoint(String name, int x, int y)
	{
		super(name);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void setData(String data)
	{
		int index = data.indexOf('x');
		x = Integer.parseInt(data.substring(0, index));
		y = Integer.parseInt(data.substring(index+1));
	}
	
	@Override
	public String getData()
	{
		return x +"x" +y;
	}
	
	public int x;
	public int y;
}