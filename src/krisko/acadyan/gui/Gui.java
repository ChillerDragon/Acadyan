package krisko.acadyan.gui;

import java.awt.Graphics;
import java.util.ArrayList;

import krisko.acadyan.Game;
import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.MathHelper;

public class Gui
{
	public Gui(Game game)
	{
		this.game = game;
		setPage(-1);
	}
	
//
	public void add(GuiComponent guiComponent)
	{
		guiComponent.setParentGui(this);
		components.add(guiComponent);
	}
	
	private void add(GuiComponent guiComponent, int n, int... page)
	{
		// create pages
		for(int i = pages.size(); i <= page[n]; i++)
			pages.add(new Gui(game));
		
		if(n < page.length-1)
		{
			pages.get(page[n]).add(guiComponent, n+1, page);
		}
		else
		{
			guiComponent.setParentGui(this);
			pages.get(page[n]).components.add(guiComponent);
		}
	}
	
	public void add(GuiComponent guiComponent, int... page)
	{
		add(guiComponent, 0, page);
	}
	
	public void removeAll()
	{
		components.clear();
	}
	
	public void setPage(int... page)
	{
		currentPage = page;
	}
	
	public int getPage() { return currentPage[0]; }
	
	public Gui getPage(int page) { return pages.get(page); }
	
	public Gui getLastPage()
	{
		Gui lastPage = this;
		for(int i = 0; i < currentPage.length; i++)
		{
			if(currentPage[i] == -1)
				break;
			
			lastPage = lastPage.getPage(currentPage[i]);
		}
		
		return lastPage;
	}
	
// update
	public void update(InputHandler handler, KEvent events[])
	{
		// get last page
		Gui lastPage = getLastPage();
		
		// update last page
		if(lastPage != this)
			lastPage.update(handler, events);
		
		// set focused component
		if(handler.leftClicked)
		{
			focusedComponent = null;
			
			if(lastPage != this)
				focusedComponent = lastPage.focusedComponent;
			
			// don't search if the last page already has a focused component
			if(focusedComponent == null)
			{
				for(int i = 0; i < components.size(); i++)
				{
					GuiComponent comp = components.get(i);
					if(comp.isFocusable() && MathHelper.pointInRect(handler.mouseX, handler.mouseY, comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight()))
					{
						focusedComponent = comp;
						break;
					}
				}
			}
		}
		
		// update components
		for(int i = 0; i < components.size(); i++)
			components.get(i).update(handler, events);
	}
	
//
	public void onButtonClicked(GuiButton button)
	{
		if(parentGui != null)
			parentGui.onButtonClicked(button);
	}
	
	public void onSliderChanged(GuiSlider slider)
	{
		if(parentGui != null)
			parentGui.onSliderChanged(slider);
	}
	
	public void onCheckboxClicked(GuiCheckbox checkbox)
	{
		if(parentGui != null)
			parentGui.onCheckboxClicked(checkbox);
	}
	
	public void onTextboxChanged(GuiTextbox textbox)
	{
		if(parentGui != null)
			parentGui.onTextboxChanged(textbox);
	}
	
/**
 * if no button id's are given, ALL buttons will be visible
 * @param visible
 * @param buttonIDs
 */
	public void setButtonsVisible(boolean visible, int... buttonIDs)
	{
		for(int i = 0; i < components.size(); i++)
		{
			if(components.get(i) instanceof GuiButton)
			{
				GuiButton button = (GuiButton)components.get(i);
				
				if(buttonIDs.length == 0)
				{
					button.setVisible(visible);
				}
				else
				{
					for(int j = 0; j < buttonIDs.length; j++)
					{
						if(button.id == buttonIDs[j])
						{
							button.setVisible(visible);
							break;
						}
					}
				}
			}
		}
	}
	
	public void setButtonsEnabled(boolean enabled, int... buttonIDs)
	{
		for(int i = 0; i < components.size(); i++)
		{
			if(components.get(i) instanceof GuiButton)
			{
				GuiButton button = (GuiButton)components.get(i);
				
				if(buttonIDs.length == 0)
				{
					button.setEnabled(enabled);
				}
				else
				{
					for(int j = 0; j < buttonIDs.length; j++)
					{
						if(button.id == buttonIDs[j])
						{
							button.setEnabled(enabled);
							break;
						}
					}
				}
			}
		}
	}
	
	public void setFocused(GuiComponent component)
	{
		focusedComponent = component;
	}
	
	public GuiComponent getFocused() { return focusedComponent; }
	
	public Gui setParentGui(Gui gui)
	{
		parentGui = gui;
		return this;
	}
	
//
	public boolean isIngameGui() { return false; }
	
	public void onGuiClosed() { }
	
// render
	public void draw(Graphics g)
	{
		// get last page
		Gui lastPage = getLastPage();
		
		// draw last page
		if(lastPage != this)
			lastPage.draw(g);
		
		// draw components
		for(int i = 0; i < components.size(); i++)
			components.get(i).draw(g);
	}
	
	protected Gui parentGui;
	private int[] currentPage;
	private ArrayList<Gui> pages = new ArrayList<Gui>();
	private ArrayList<GuiComponent> components = new ArrayList<GuiComponent>();
	private GuiComponent focusedComponent;
	protected Game game;
}