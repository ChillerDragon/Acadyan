package krisko.acadyan;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import krisko.acadyan.gui.Gui;
import krisko.acadyan.gui.GuiButton;
import krisko.acadyan.gui.GuiLabel;
import krisko.acadyan.gui.GuiSlider;
import krisko.acadyan.gui.GuiTextbox;

public class Editor extends Gui
{
	public Editor(Game game)
	{
		this(game, null);
	}
	
	public Editor(Game game, String name)
	{
		super(game);
		scale = 1.0D;
		
		world = new World();
		
		mapName = name;
		if(mapName == null || !world.loadExtern(mapName))
			world.addChunks(1);
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		super.update(handler, events);
		
		// try saving
		if(handler.keys[KeyEvent.VK_CONTROL] && handler.keys[KeyEvent.VK_S])
		{
			handler.stopUpdate(KeyEvent.VK_S);
			handler.stopUpdate(KeyEvent.VK_CONTROL);
			
			if(mapName == null || handler.keys[KeyEvent.VK_SHIFT])
			{
				handler.stopUpdate(KeyEvent.VK_SHIFT);
				String name = JOptionPane.showInputDialog(null, Strings.SET_MAP_NAME.get() +":", Strings.SAVE_MAP_AS.get(), JOptionPane.QUESTION_MESSAGE);
				
				if(name != null && name.length() > 0)
				{
					if(world.save(name, true))
						mapName = name;
				}
			}
			else
			{
				world.save(mapName);
			}
		}
		
		// try opening
		if(handler.keys[KeyEvent.VK_CONTROL] && handler.keys[KeyEvent.VK_O])
		{
			handler.keys[KeyEvent.VK_O] = false;
			handler.keys[KeyEvent.VK_CONTROL] = false;
			
			String name = JOptionPane.showInputDialog(null, Strings.SET_MAP_NAME.get() +":", Strings.OPEN_MAP.get(), JOptionPane.QUESTION_MESSAGE);
			
			if(name != null && name.length() > 0)
			{
				mapName = name;
				world.loadExtern(mapName);
				
				setCurrentBlockData(null, 0, 0);
				setState(STATE_PLACE);
			}
		}
		
		// try changing the state and layer
		if(!(getFocused() instanceof GuiTextbox))
		{
			// change state
			if(handler.keys[KeyEvent.VK_1])
			{
				handler.stopUpdate(KeyEvent.VK_1);
				setState(STATE_PLACE);
			}
			else if(handler.keys[KeyEvent.VK_2])
			{
				handler.stopUpdate(KeyEvent.VK_2);
				setState(STATE_DATA);
			}
			
			// change layer
			if(handler.keys[KeyEvent.VK_SPACE])
			{
				handler.stopUpdate(KeyEvent.VK_SPACE);
				currentLayer ^= 1;
			}
		}
		
		int blockSize = (int)(Game.BlockSize * scale);
		
		// change scale
		if(handler.mouseWheelAmount != 0 && handler.keys[KeyEvent.VK_CONTROL])
			scale = MathHelper.clamp(scale - handler.mouseWheelAmount/8.0D, 1.0D / 4.0D, 5.0D);
		
		// set mouse-world positions
		int mouseWorldX = xOffset - blockDataWidth + handler.mouseX;
		int mouseWorldY = yOffset + handler.mouseY;
		mouseBlockX = mouseWorldX / blockSize;
		mouseBlockY = mouseWorldY / blockSize;
		if(mouseWorldX < 0)
			mouseBlockX--;
		if(mouseWorldY < 0)
			mouseBlockY--;
		
		// change offset
		if(!(getFocused() instanceof GuiTextbox))
		{
			int offsetSpeed = handler.keys[KeyEvent.VK_SHIFT] ? 16 : 8;
			if(handler.keys[KeyEvent.VK_A])
				xOffset -= offsetSpeed;
			if(handler.keys[KeyEvent.VK_D])
				xOffset += offsetSpeed;
			if(handler.keys[KeyEvent.VK_W])
				yOffset -= offsetSpeed;
			if(handler.keys[KeyEvent.VK_S])
				yOffset += offsetSpeed;
		}
		
		if(xOffset < -Game.BlockSize*12)
			xOffset = -Game.BlockSize*12;
		if(yOffset < -Game.BlockSize*6)
			yOffset = -Game.BlockSize*6;
		else if(yOffset + Game.ScreenHeight > Chunk.HEIGHT*blockSize + Game.BlockSize*6)
			yOffset = Chunk.HEIGHT*blockSize + Game.BlockSize*6 - Game.ScreenHeight;
		
		if(state == STATE_PLACE)
		{
			// set current block
			if(handler.mouseWheelAmount != 0 && !handler.keys[KeyEvent.VK_CONTROL])
			{
				currentBlock = MathHelper.clamp(currentBlock + handler.mouseWheelAmount, 0, blocks.length-1);
				currentMetadata = 0;
			}
			
			// set current metadata
			if(handler.keys[KeyEvent.VK_R])
			{
				handler.stopUpdate(KeyEvent.VK_R);
				
				if(blocks[currentBlock].hasMetadata())
				{
					currentMetadata++;
					if(currentMetadata >= blocks[currentBlock].getMetadataNum())
						currentMetadata = 0;
				}
			}
			
			// place blocks
			if(canPlace)
			{
				if(MathHelper.pointInRect(handler.mouseX, handler.mouseY, worldDrawX, worldDrawY, worldDrawWidth, worldDrawHeight))
				{
					if(handler.leftDown)
						world.setBlock(mouseBlockX, mouseBlockY, blocks[currentBlock], currentMetadata);
					else if(handler.rightDown)
					{
						world.setBlock(0, mouseBlockX, mouseBlockY, Block.air);
						world.setBlock(1, mouseBlockX, mouseBlockY, Block.air);
					}
				}
			}
			else
			{
				if(!handler.leftDown && !handler.rightDown)
					canPlace = true;
			}
		}
		else if(state == STATE_DATA)
		{
			// remove current block data
			if(handler.rightClicked)
				setCurrentBlockData(null, 0, 0);
			
			if(MathHelper.pointInRect(handler.mouseX, handler.mouseY, worldDrawX, worldDrawY, worldDrawWidth, worldDrawHeight)) // mouse inside world
			{
				// select blockdata
				if(currentBlockData instanceof BlockDataTeleporter || currentBlockData instanceof BlockDataPressurePlate)
				{
					if(handler.leftClicked)
					{
						DataPoint d = (DataPoint)currentBlockData.get(0);
						d.x = mouseBlockX;
						d.y = mouseBlockY;
					}
				}
				else
				{
					// set new currentBlockData and add components
					if(handler.leftClicked)
					{
						setCurrentBlockData(world.getBlockData(currentLayer, mouseBlockX, mouseBlockY), mouseBlockX, mouseBlockY);
					}
				}
			}
//			else // mouse inside block-data field
//			{
//				
//			}
		}
		else if(state == STATE_COPY_DATA)
		{
			if(handler.rightClicked)
			{
				setState(STATE_DATA);
				setCurrentBlockData(world.getBlockData(currentBlockLayer, currentBlockX, currentBlockY), currentBlockX, currentBlockY);
			}
			else if(handler.leftDown && (mouseBlockX != currentBlockX || mouseBlockY != currentBlockY))
			{
				// check if it's the same block
				if(world.getBlock(currentBlockLayer, mouseBlockX, mouseBlockY) == world.getBlock(currentBlockLayer, currentBlockX, currentBlockY))
				{
					// check if the data was already copied to this block
					boolean copied = false;
					for(int i = 0; i < listCopiedData.size(); i++)
					{
						Point p = listCopiedData.get(i);
						if(p.x == mouseBlockX && p.y == mouseBlockY)
						{
							copied = true;
							break;
						}
					}
					
					if(!copied) // ...if not, copy it
					{
						// copy data
						BlockData dataMouse = world.getBlockData(currentBlockLayer, mouseBlockX, mouseBlockY);
						BlockData dataCurrent = world.getBlockData(currentBlockLayer, currentBlockX, currentBlockY);
						
						for(int i = 0; i < dataCurrent.size(); i++)
							dataMouse.setData(i, dataCurrent.getData(i));
						
						listCopiedData.add(new Point(mouseBlockX, mouseBlockY));
					}
				}
			}
		}
		
		// new chunk
		if(handler.keys[KeyEvent.VK_N])
		{
			handler.stopUpdate(KeyEvent.VK_N);
			world.addChunks(1);
		}
		
		// test map
		if(handler.keys[KeyEvent.VK_CONTROL] && handler.keys[KeyEvent.VK_T])
		{
			handler.stopUpdate(KeyEvent.VK_T);
			handler.stopUpdate(KeyEvent.VK_CONTROL);
			
			if(mapName == null)
			{
				String name = JOptionPane.showInputDialog(null, Strings.SET_MAP_NAME.get() +": (" +Strings.SAVE_MAP_BEFORE_TESTING.get() +")", Strings.SAVE_MAP_AS.get(), JOptionPane.QUESTION_MESSAGE);
				if(name != null && name.length() > 0)
				{
					if(world.save(name, true))
						mapName = name;
				}
			}
			
			if(mapName != null)
			{
				world.save(mapName);
				game.openWorld(mapName, false, true);
				game.openGui(null);
			}
		}
	}
	
	private void setState(int state)
	{
		if(this.state == state)
			return;
		
		// 1. -
		xOffset -= blockDataWidth;
		
		this.state = state;
		blockDataWidth = state == STATE_DATA ? 250 : 0;
		worldDrawX = blockDataWidth;
		
		// 2. +
		xOffset += blockDataWidth;
		
		if((state == STATE_PLACE || state == STATE_DATA) && currentBlockData != null)
			setCurrentBlockData(null, 0, 0);
	}
	
	private void setCurrentBlockData(BlockData data, int x, int y)
	{
//		if(currentBlockData == data)
//			return;
		
		removeAll();
		currentBlockX = x;
		currentBlockY = y;
		currentBlockLayer = currentLayer;
		currentBlockData = data;
		listCopiedData.clear();
		
		if(currentBlockData != null)
		{
			int num = 0; // how many visible block-datas exist
			for(int i = 0; i < currentBlockData.size(); i++)
			{
				if(currentBlockData.isVisible(i))
					num++;
			}
			
			// add labels and slider/textboxes/...
			lData = new GuiLabel[num];
			num = 0;
			for(int i = 0; i < currentBlockData.size(); i++)
			{
				if(!currentBlockData.isVisible(i))
					continue;
				
				// add data-specific component
				if(currentBlockData.get(i) instanceof DataValue)
				{
					DataValue d = (DataValue)currentBlockData.get(i);
					
					GuiTextbox textbox = new GuiTextbox(num, 10, 80 + i*75, blockDataWidth - 20, 20, Integer.toString(d.value));
					textbox.setMaxChars(8);
					textbox.setAllowed(GuiTextbox.NUMBERS);
					add(textbox);
					
					lData[num] = new GuiLabel(10, 50 + i*75, 22, GuiLabel.ALIGNMENT_LEFT, currentBlockData.getName(i));
				}
				else if(currentBlockData.get(i) instanceof DataValueMinMax)
				{
					DataValueMinMax d = (DataValueMinMax)currentBlockData.get(i);
					
					add(new GuiSlider(num, 10, 80 + i*75, blockDataWidth - 20, 20, d.value, d.minimum, d.maximum));
					
					lData[num] = new GuiLabel(10, 50 + i*75, 22, GuiLabel.ALIGNMENT_LEFT, currentBlockData.getName(i), ": " +d.value);
				}
//				else if(currentBlockData.get(i) instanceof DataPoint)
//				{
////						DataPoint d = (DataPoint)currentBlockData.get(i);
//					
//					add(new GuiButton(num, 10, 80 + i*75, blockDataWidth - 20, 20, "_____"));
//					
//					lData[num] = new GuiLabel(10, 50 + i*75, 22, GuiLabel.ALIGNMENT_LEFT, currentBlockData.getName(i));
//				}
				
				// add label
				add(lData[num]);
				num++;
			}
			
			// add copy-data button
			add(new GuiButton(0, 10, blockDataHeight - 150, blockDataWidth - 20, 40, Strings.COPY));
		}
	}
	
	@Override
	public void onButtonClicked(GuiButton button)
	{
		if(button.id == 0) // copy data
		{
			removeAll();
			setState(STATE_COPY_DATA);
		}
	}
	
	@Override
	public void onSliderChanged(GuiSlider slider)
	{
		int value = (int)slider.getValue();
		lData[slider.id].setText(currentBlockData.getName(slider.id), ": " +value);
		currentBlockData.setData(slider.id, Integer.toString(value));
	}
	
	@Override
	public void onTextboxChanged(GuiTextbox textbox)
	{
		int value = 0;
		if(textbox.getText().length() > 0)
			value = Integer.parseInt(textbox.getText());
		
		currentBlockData.setData(textbox.id, Integer.toString(value));
	}
	
	@Override
	public void removeAll()
	{
		super.removeAll();
		lData = null;
	}
	
	@Override
	public void draw(Graphics g)
	{
		// draw world
		world.draw(g, worldDrawX, worldDrawY, worldDrawWidth, worldDrawHeight, xOffset, yOffset, scale, true);
		
		int blockSize = (int)(Game.BlockSize * scale);
		
		// draw world outlines
		int top = -yOffset;
		int bottom = top + Chunk.HEIGHT*blockSize;
		int left = -xOffset + blockDataWidth;
		int right = left + Chunk.WIDTH*blockSize*world.size();
		
		g.setColor(Color.red);
		if(top >= 0 && top < Game.ScreenHeight)
			g.drawLine(MathHelper.clamp(left, worldDrawX, worldDrawX + worldDrawWidth - 1), top, MathHelper.clamp(right, worldDrawX, worldDrawX + worldDrawWidth - 1), top); // top
		if(bottom >= 0 && bottom < Game.ScreenHeight)
			g.drawLine(MathHelper.clamp(left, worldDrawX, worldDrawX + worldDrawWidth - 1), bottom, MathHelper.clamp(right, worldDrawX, worldDrawX + worldDrawWidth - 1), bottom); // bottom
		if(left >= blockDataWidth && left < Game.ScreenWidth)
			g.drawLine(left, MathHelper.clamp(top, worldDrawY, worldDrawY + worldDrawHeight - 1), left, MathHelper.clamp(bottom, worldDrawY, worldDrawY + worldDrawHeight - 1)); // left
		if(right >= blockDataWidth && right < Game.ScreenWidth)
			g.drawLine(right, MathHelper.clamp(top, worldDrawY, worldDrawY + worldDrawHeight - 1), right, MathHelper.clamp(bottom, worldDrawY, worldDrawY + worldDrawHeight - 1)); // right
		
		// draw "press n for more space"
		g.setColor(Color.black);
		GuiLabel.draw(g, Strings.N_MORE_SPACE.get(), right + 30, top + (bottom-top)/2, GuiLabel.ALIGNMENT_LEFT, 30);
		
		// draw mouse rect
		int x = mouseBlockX*blockSize - xOffset + blockDataWidth;
		int y = mouseBlockY*blockSize - yOffset;
		
		if(state == STATE_PLACE)
		{
			g.setColor(Game.BackgroundColor);
			g.fillRect(x, y, blockSize, blockSize);
		}
		g.setColor(Color.blue);
		g.drawRect(x-1, y-1, blockSize+1, blockSize+1);
		
		if(state == STATE_PLACE)
		{
			// draw mouse block
			blocks[currentBlock].draw(g, x, y, blockSize, blockSize, currentMetadata, -1);
			
			// draw available blocks
			int gap = 8;
			int size = 36;
			x = Game.ScreenWidth - size - 20;
			y = 20;
			
			g.setColor(Game.ForegroundColor);
			for(int i = 0; i < blocks.length; i++)
				blocks[i].draw(g, x, y + i*size + i*gap, size, size, -1);
			
			g.setColor(Color.blue);
			g.drawRect(x - gap/2, y + currentBlock*size + currentBlock*gap - gap/2, size + gap - 1, size + gap - 1);
			
			// draw current block name
			g.setColor(Color.black);
			GuiLabel.draw(g, blocks[currentBlock].getName(), Game.ScreenWidth/2, 10, GuiLabel.ALIGNMENT_MIDDLE, 20);
		}
		else if(state == STATE_DATA)
		{
			// draw teleporter line (and pressure plate line)
			if(currentBlockData instanceof BlockDataTeleporter || currentBlockData instanceof BlockDataPressurePlate)
			{
				DataPoint d = (DataPoint)currentBlockData.get(0);
				int fromX = currentBlockX*blockSize - xOffset + blockSize/2 + blockDataWidth;
				int fromY = currentBlockY*blockSize - yOffset + blockSize/2;
				int toX = d.x*blockSize - xOffset + blockSize/2 + blockDataWidth;
				int toY = d.y*blockSize - yOffset + blockSize/2;
				
				g.setColor(Color.red);
				g.drawLine(fromX, fromY, toX, toY);
			}
			
			// draw block data window
			g.setColor(new Color(0x6D, 0xA2, 0xD0));
			g.fillRect(0, 0, blockDataWidth-1, blockDataHeight-1);
			g.setColor(Color.black);
			g.drawRect(0, 0, blockDataWidth-1, blockDataHeight-1);
			
			// draw info
			String infos[] = {
					world.getBlock(currentLayer, mouseBlockX, mouseBlockY).getName(),
					String.format("X: %d", mouseBlockX),
					String.format("Y: %d", mouseBlockY),
			};
			
			g.setColor(Color.black);
			for(int i = 0; i < infos.length; i++)
				GuiLabel.draw(g, infos[i], 5, blockDataHeight - infos.length*25 + i*25, GuiLabel.ALIGNMENT_LEFT, 20);
		}
		else if(state == STATE_COPY_DATA)
		{
			Block currentBlock = world.getBlock(currentBlockLayer, currentBlockX, currentBlockY);
			
			// render blocks green/red/grey
			left = xOffset / Chunk.WIDTH - 1;
			right = (xOffset + Game.ScreenWidth) / Chunk.WIDTH;
			
			for(int i = left; i <= right; i++)
			{
				for(int j = 0; j < Chunk.HEIGHT; j++)
				{
					if(i == currentBlockX && j == currentBlockY) // block = current block from blockData
						g.setColor(new Color(0x0, 0xFF, 0x0, 0x60));
					else if(world.getBlock(currentBlockLayer, i, j) == currentBlock)
					{
						boolean copied = false;
						
						// check if it should be drawn green or red
						for(int k = 0; k < listCopiedData.size(); k++)
						{
							Point p = listCopiedData.get(k);
							if(p.x == i && p.y == j)
							{
								copied = true;
								break;
							}
						}
						
						if(copied)
							g.setColor(new Color(0x0, 0xFF, 0x0, 0x60));
						else
							g.setColor(new Color(0xFF, 0x0, 0x0, 0x60));
					}
					else
						continue;
					
					g.fillRect(i*blockSize - xOffset, j*blockSize - yOffset, blockSize, blockSize);
				}
			}
		}
		
		// draw layer
		g.setColor(Color.black);
		GuiLabel.draw(g, "Layer: " +(currentLayer == 0 ? "Background" : "Foreground"), Game.ScreenWidth/2, Game.ScreenHeight - 30, GuiLabel.ALIGNMENT_MIDDLE, 20);
		
		super.draw(g);
	}
	
	private int mouseBlockX, mouseBlockY;
	
	private int currentBlock, currentMetadata;
	private Block[] blocks = {
			Block.solid,
			
			Block.trampoline,
			Block.breaking,
			Block.teleporter,
			Block.hidden,
			
			Block.spike,
			Block.fallingSpike,
			Block.acid,
			
			Block.cannon,
			
			Block.pressurePlate,
			Block.door,
			Block.laser,
			
			Block.coin,
			Block.gold,
			
			Block.start,
			Block.goal,
	};
	
	// State
	private int state;
	private static final int STATE_PLACE = 0;
	private static final int STATE_DATA = 1;
	private static final int STATE_COPY_DATA = 2;
	
	// Block info
	private int currentBlockX;
	private int currentBlockY;
	private int currentBlockLayer;
	private BlockData currentBlockData;
	private GuiLabel lData[];
	private ArrayList<Point> listCopiedData = new ArrayList<Point>();
	
	// Map
	private String mapName;
	
	// World
	private World world;
	private int xOffset, yOffset;
	private double scale;
	private boolean canPlace;
	private int currentLayer;
	
	private int blockDataWidth = 0;
	private final int blockDataHeight = Game.ScreenHeight;
	
	// Draw
	private int worldDrawX = blockDataWidth;
	private final int worldDrawY = 0;
	private final int worldDrawWidth = Game.ScreenWidth - worldDrawX;
	private final int worldDrawHeight = Game.ScreenHeight - worldDrawY;
}