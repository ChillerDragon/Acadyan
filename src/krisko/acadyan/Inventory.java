package krisko.acadyan;

import java.awt.Color;
import java.awt.Graphics;

import krisko.acadyan.gui.GuiLabel;

public class Inventory
{
	public Inventory(int width, int height)
	{
		this.width = width;
		this.height = height;
		
		items = new Item[width*height + width*2];
		amounts = new int[items.length];
	}
	
// add
	public void setItem(Item item, int index)
	{
		setItem(item, 1, index);
	}
	
	public void setItem(Item item, int amount, int index)
	{
		items[index] = item;
		amounts[index] = amount;
	}
	
	public void setAmount(int amount, int index)
	{
		amounts[index] = amount;
		
		if(amount <= 0)
			items[index] = null;
	}
	
	public boolean addItem(Item item, int amount, int index)
	{
		if(items[index] == null)
		{
			setItem(item, amount, index);
			return true;
		}
		else if(items[index] == item)
		{
			amounts[index] += amount;
			return true;
		}
		
		return false;
	}
	
	/** returns true when there was a place to add the item */
	public boolean addItem(Item item)
	{
		return addItem(item, 1);
	}
	
	/** returns true when there was a place to add the item */
	public boolean addItem(Item item, int amount)
	{
		// try to stack on available item
		for(int i = 0; i < items.length; i++)
		{
			if(items[i] == item)
			{
				amounts[i] += amount;
				return true;
			}
		}
		
		// if there is no such existing item, try adding it anywhere
		for(int i = 0; i < items.length; i++)
		{
			if(items[i] == null)
			{
				setItem(item, amount, i);
				return true;
			}
		}
		
		return false;
	}
	
	/** returns true when there was a place to add the item */
	public boolean addItemToHotbar(Item item)
	{
		return addItemToHotbar(item, 1);
	}
	
	/** returns true when there was a place to add the item */
	public boolean addItemToHotbar(Item item, int amount)
	{
		// try to stack on available item
		for(int i = getHotbarStart(); i <= getHotbarEnd(); i++)
		{
			if(items[i] == item)
			{
				amounts[i] += amount;
				return true;
			}
		}
		
		// try to create the item in the hotbar
		for(int i = getHotbarStart(); i <= getHotbarEnd(); i++)
		{
			if(items[i] == null)
			{
				setItem(item, amount, i);
				return true;
			}
		}
		
		// if hotbar is full, try to add the item to your normal inventory
		return addItem(item, amount);
	}
	
	public void removeItem(int index)
	{
		setItem(null, 0, index);
	}
	
	public void removeNonSaveItems()
	{
		for(int i = getWidth(); i < items.length; i++)
			removeItem(i);
	}
	
	public void clear()
	{
		for(int i = 0; i < items.length; i++)
			removeItem(i);
	}
	
//
	public boolean move(int index1, int index2)
	{
		if(index1 == index2 || items[index2] != null)
			return false;
		
		setItem(items[index1], amounts[index1], index2);
		removeItem(index1);
		return true;
	}
	
	public boolean moveToSave(int index)
	{
		for(int i = 0; i < getWidth(); i++)
			if(move(index, i))
				return true;
		
		return false;
	}
	
	public void swap(int index1, int index2)
	{
		if(index1 == index2)
			return;
		
		// stack item if it's from the same type
		if(items[index1] == items[index2])
		{
			amounts[index2] += amounts[index1];
			removeItem(index1);
			return;
		}
		
		// else, swap these two
		Item item = items[index1];
		int amount = amounts[index1];
		setItem(items[index2], amounts[index2], index1);
		setItem(item, amount, index2);
	}
	
	public void useItem(int index, EntityPlayer player)
	{
		if(items[index] == null || !items[index].use(player))
			return;
		
		amounts[index]--;
		
		if(amounts[index] <= 0)
			items[index] = null;
	}
	
	public void useHotbarItem(int index, EntityPlayer player)
	{
		useItem(getHotbarStart() + index, player);
	}
	
// get stuff
	public int getWidth() { return width; }
	
	public int getHeight() { return height; }
	
	public int getHotbarStart() { return width + width*height; }
	
	public int getHotbarEnd() { return items.length - 1; }
	
//	public Item[] getItems() { return items; }
	
	public Item getItem(int index)
	{
		if(index < 0 || index >= items.length)
			return null;
		
		return items[index];
	}
	
	public int getAmount(int index)
	{
		if(index < 0 || index >= items.length)
			return 0;
		
		return amounts[index];
	}
	
// render
	public void drawInventorySlotsMidX(Graphics g, int x, int y, int slotSize, int gap, int start, int end)
	{
		int cols = MathHelper.min(getWidth(), end - start + 1);
		drawInventorySlots(g, x - (cols*slotSize + cols*gap + gap)/2, y, slotSize, gap, start, end);
	}
	
	public void drawInventorySlots(Graphics g, int x, int y, int slotSize, int gap, int start, int end)
	{
		if(start > end)
			return;
		
		int cols = getWidth();
		int rows = 1 + (end - start) / (cols+1);
		int width = slotSize*cols + gap*cols + gap;
		int height = slotSize*rows + gap*rows + gap;
		
		// draw background
		g.setColor(background);
		g.fillRect(x, y, width, height);
		
		// draw slots
		x += gap;
		y += gap;
		
		for(int j = 0; j < rows; j++)
		{
			for(int i = 0; i < cols; i++)
			{
				drawSlot(g, x + i*slotSize + i*gap, y + j*slotSize + j*gap, slotSize, slotSize, getItem(start + i + j*cols), getAmount(start + i + j*cols));
			}
		}
	}
	
	private void drawSlot(Graphics g, int x, int y, int w, int h, Item item, int amount)
	{
		g.setColor(foreground);
		g.fillRect(x, y, w, h);
		
		if(item != null)
		{
			item.draw(g, x, y, w, h);
			
			g.setColor(Color.white);
			GuiLabel.draw(g, Integer.toString(amount), x + w - 3, y + h - 25, GuiLabel.ALIGNMENT_RIGHT, 25);
		}
	}
	
	private int width;
	private int height;
	
	private Item items[];
	private int amounts[];
	
	private final Color background = new Color(0xD0, 0xD0, 0xD0, 0xFF);
	private final Color foreground = new Color(0xA0, 0xA0, 0xA0, 0xD0);
}