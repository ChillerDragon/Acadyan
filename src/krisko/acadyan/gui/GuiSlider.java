package krisko.acadyan.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import krisko.acadyan.Game;
import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.MathHelper;

public class GuiSlider extends GuiComponent
{
	public GuiSlider(int id, int x, int y, int w, int h, int value, int minimum, int maximum)
	{
		super(id, x, y, w, h);
		this.minimum = minimum;
		this.maximum = maximum;
		setValue(value);
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		super.update(handler, events);
		
		float fVal = (value-minimum) / (float)(maximum-minimum);
		boolean inside = MathHelper.pointInRect(handler.mouseX, handler.mouseY, getX() + (int)(getWidth()*fVal) - 7, getY(), 15, getHeight());
		
		if(!handler.leftDown)
		{
			mouseState = inside ? MOUSE_OVER : NONE;
			return;
		}
		
		if(handler.leftClicked && inside)
		{
			mouseState = MOUSE_DRAGGED;
			
			// set shiftX for precise sliding
			shiftX = handler.keys[KeyEvent.VK_SHIFT] ? handler.mouseX : -1;
		}
		
		if(mouseState == MOUSE_DRAGGED)
		{
			// save old value
			int oldValue = value;
			
			float f = (handler.mouseX - getX()) / (float)getWidth();
			
			if(handler.keys[KeyEvent.VK_SHIFT]) // slide precisely
			{
				// set shiftX
				if(shiftX == -1)
					shiftX = handler.mouseX;
				
				float fShift = (handler.mouseX - shiftX) / (float)getWidth();
				f = MathHelper.clamp(f - fShift*0.875f, 0.f, 1.f);
				value = minimum + (int)(f * (maximum-minimum+1));
				if(value > maximum)
					value = maximum;
			}
			else // slider normally
			{
				// reset shiftX
				shiftX = -1;
				
				f = MathHelper.clamp(f, 0.f, 1.f);
				value = minimum + (int)(f * (maximum-minimum+1));
				if(value > maximum)
					value = maximum;
			}
			
			// notify gui if value was changed
			if(oldValue != getValue())
			{
				if(listener != null)
					listener.onActionPerformed(this);
				else
					parentGui.onSliderChanged(this);
			}
		}
	}
	
	public void setValue(int value)
	{
		this.value = MathHelper.clamp(value, minimum, maximum);
	}
	
	public int getValue()
	{
		return value;
	}
	
	@Override
	public void draw(Graphics g)
	{
		float fVal = (value-minimum) / (float)(maximum-minimum);
		
		g.setColor(Game.ForegroundColor);
		g.fillRect(getX(), getY() + getHeight()/2 - 2, getWidth(), MathHelper.clamp((int)(getHeight()*0.125), 1, 5));
		
		g.setColor(!isEnabled() ? Color.gray : (mouseState == MOUSE_OVER) ? new Color(0x88, 0x88, 0x88) : Color.darkGray);
		g.fillRect(getX() + (int)(getWidth()*fVal) - 2, getY(), 5, getHeight());
	}
	
	// values
	private int value;
	private int minimum;
	private int maximum;
	
	// for precise sliding
	// ...when the shift-button is pressed
	int shiftX;
	
	private int mouseState;
	private final int NONE = 0;
	private final int MOUSE_OVER = 1;
	private final int MOUSE_DRAGGED = 2;
}