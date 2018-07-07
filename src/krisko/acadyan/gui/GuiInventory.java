package krisko.acadyan.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import krisko.acadyan.Controls;
import krisko.acadyan.Game;
import krisko.acadyan.InputHandler;
import krisko.acadyan.Inventory;
import krisko.acadyan.Item;
import krisko.acadyan.KEvent;
import krisko.acadyan.MathHelper;

public class GuiInventory extends Gui
{
	public GuiInventory(Game game, Inventory inv)
	{
		super(game);
		holdItem = null;
		holdAmount = 0;
		
		slotIndex = -1;
		
		inventory = inv;
		
		slotSize = 64;
		slotGap = 10;

		inventoryWidth = slotSize*inventory.getWidth() + slotGap*inventory.getWidth() + slotGap;
		inventoryHeight = slotSize*inventory.getHeight() + slotGap*inventory.getHeight() + slotGap;
		
		middleX = Game.ScreenWidth/2 - inventoryWidth/2;
		middleY = Game.ScreenHeight/2 - inventoryHeight/2;
		upperX = middleX;
		upperY = middleY - slotSize - slotGap*2 - 15;
		lowerX = middleX;
		lowerY = middleY + inventoryHeight + 15;
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		super.update(handler, events);
		
		mouseX = handler.mouseX;
		mouseY = handler.mouseY;
		
		slotIndex = -1;
		
		// get current mouse-over slot
		if(MathHelper.pointInRect(mouseX, mouseY, middleX, middleY, inventoryWidth, inventoryHeight))
		{
			int x = middleX + slotGap;
			int y;
			
			loop1:
			for(int i = 0; i < inventory.getWidth(); i++)
			{
				y = middleY + slotGap;
				
				for(int j = 0; j < inventory.getHeight(); j++)
				{
					if(MathHelper.pointInRect(mouseX, mouseY, x, y, slotSize, slotSize))
					{
						slotIndex = inventory.getWidth() + i + j*inventory.getWidth();
						slotX = x;
						slotY = y;
						break loop1;
					}
					
					y += slotSize + slotGap;
				}
				
				x += slotSize + slotGap;
			}
		}
		else if(MathHelper.pointInRect(mouseX, mouseY, upperX, upperY+slotGap, inventoryWidth, slotSize))
		{
			int x = upperX + slotGap;
			int y = upperY + slotGap;
			
			for(int i = 0; i < inventory.getWidth(); i++)
			{
				if(MathHelper.pointInRect(mouseX, mouseY, x, y, slotSize, slotSize))
				{
					slotIndex = i;
					slotX = x;
					slotY = y;
					break;
				}
				
				x += slotSize + slotGap;
			}
		}
		else if(MathHelper.pointInRect(mouseX, mouseY, lowerX, lowerY+slotGap, inventoryWidth, slotSize))
		{
			int x = lowerX + slotGap;
			int y = lowerY + slotGap;
			
			for(int i = 0; i < inventory.getWidth(); i++)
			{
				if(MathHelper.pointInRect(mouseX, mouseY, x, y, slotSize, slotSize))
				{
					slotIndex = inventory.getHotbarStart() + i;
					slotX = x;
					slotY = y;
					break;
				}
				
				x += slotSize + slotGap;
			}
		}
		
		// handle moving item
		if(slotIndex >= 0)
		{
			if(handler.leftClicked)
			{
				if(holdItem == null)
				{
					// take item
					if(inventory.getItem(slotIndex) != null)
					{
						// try moving item to save
						if(handler.keys[KeyEvent.VK_SHIFT])
						{
							inventory.moveToSave(slotIndex);
						}
						else
						{
							// get clicked item
							holdItem = inventory.getItem(slotIndex);
							holdAmount = inventory.getAmount(slotIndex);
							inventory.removeItem(slotIndex);
						}
					}
				}
				else
				{
					// place item
					if(inventory.addItem(holdItem, holdAmount, slotIndex))
					{
						holdItem = null;
						holdAmount = 0;
					}
					else
					{
						// swap holding item with inventory item
						Item item = inventory.getItem(slotIndex);
						int amount = inventory.getAmount(slotIndex);
						
						inventory.setItem(holdItem, holdAmount, slotIndex);
						holdItem = item;
						holdAmount = amount;
					}
				}
			}
			else if(handler.rightClicked)
			{
				if(holdItem == null)
				{
					// take item (half the amount)
					if(inventory.getItem(slotIndex) != null)
					{
						// get clicked item
						holdItem = inventory.getItem(slotIndex);
						holdAmount = inventory.getAmount(slotIndex) - inventory.getAmount(slotIndex)/2; // get the greater half
						
						// remove half of the items
						inventory.setAmount(inventory.getAmount(slotIndex) - holdAmount, slotIndex);
					}
				}
				else
				{
					// place 1 item
					if(inventory.addItem(holdItem, 1, slotIndex))
					{
						holdAmount--;
						if(holdAmount <= 0)
							holdItem = null;
					}
				}
			}
		}
		
		// close inventory
		if(handler.keys[KeyEvent.VK_ESCAPE])
		{
			handler.stopUpdate(KeyEvent.VK_ESCAPE);
			game.openGui(null);
		}
		else if(Controls.inventory.isPressed2(handler))
		{
			game.openGui(null);
		}
	}
	
	@Override
	public boolean isIngameGui() { return true; }
	
	@Override
	public void onGuiClosed()
	{
		// TODO: drop item
		if(holdItem != null)
			inventory.addItem(holdItem, holdAmount);
	}
	
	@Override
	public void draw(Graphics g)
	{
		super.draw(g);
		
		// draw middle
		inventory.drawInventorySlots(g, middleX, middleY, slotSize, slotGap, inventory.getWidth(), inventory.getHotbarStart() - 1);
		
		// draw upper (save items)
		inventory.drawInventorySlots(g, upperX, upperY, slotSize, slotGap, 0, inventory.getWidth() - 1);
		
		// draw lower (hotbar)
		inventory.drawInventorySlots(g, lowerX, lowerY, slotSize, slotGap, inventory.getHotbarStart(), inventory.getHotbarEnd());
		
		if(slotIndex >= 0)
		{
			g.setColor(new Color(0xFF, 0xFF, 0xFF, 0xB0));
			g.fillRect(slotX, slotY, slotSize, slotSize);
		}
		if(holdItem != null)
		{
			// render item
			holdItem.draw(g, mouseX, mouseY, slotSize, slotSize);
			
			// render amount
			g.setColor(Color.white);
			GuiLabel.draw(g, Integer.toString(holdAmount), mouseX + slotSize - 3, mouseY + slotSize - 25, GuiLabel.ALIGNMENT_RIGHT, 25);
		}
	}
	
	private int mouseX, mouseY;
	
	private int slotIndex; // current mouse-over slot-index
	private int slotX;
	private int slotY;
	
	private Item holdItem;
	private int holdAmount;
	
	private final Inventory inventory;
	
	private final int slotSize;
	private final int slotGap;
	private final int inventoryWidth;
	private final int inventoryHeight;
	private final int upperX, upperY;
	private final int middleX, middleY;
	private final int lowerX, lowerY;
}
