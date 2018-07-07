package krisko.acadyan.gui;

import java.awt.Color;
import java.awt.Graphics;

import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.MathHelper;
import krisko.acadyan.SoundManager;
import krisko.acadyan.Strings;
import krisko.acadyan.Texture;

public class GuiButton extends GuiComponent
{
	public GuiButton(int id, int x, int y, int w, int h)
	{
		super(id, x, y, w, h);
		
		setText("");
		setBorder(ALL);
	}
	
	public GuiButton(int id, int x, int y, int w, int h, Object... str)
	{
		this(id, x, y, w, h);
		setText(str);
	}
	
	public GuiButton(int id, int x, int y, int w, int h, Texture texture)
	{
		this(id, x, y, w, h);
		setTexture(texture);
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		if(!isEnabled())
			return;
		
		super.update(handler, events);
		
		if(MathHelper.pointInRect(handler.mouseX, handler.mouseY, getX(), getY(), getWidth(), getHeight()))
		{
			if(!handler.leftDown || handler.leftClicked || (mouseState & MOUSE_PRESSED) > 0)
			{
				if(handler.leftDown)
				{
					mouseState = MOUSE_OVER_PRESSED;
				}
				else
				{
					if(mouseState == MOUSE_OVER_PRESSED)
					{
						SoundManager.click.play(0.5f);
						
						if(listener != null)
							listener.onActionPerformed(this);
						else
							parentGui.onButtonClicked(this);
					}
					
					mouseState = MOUSE_OVER;
				}
			}
		}
		else
		{
			mouseState = handler.leftDown && (mouseState & MOUSE_PRESSED) > 0 ? MOUSE_PRESSED : MOUSE_NONE;
		}
	}
	
	public GuiButton setBorder(int border)
	{
		this.border = border;
		return this;
	}
	
	public void setText(Object... str)
	{
		strings = str;
	}
	
//	public Strings getString() { return string; }
	
	public String getText()
	{
		String s = "";
		for(int i = 0; i < strings.length; i++)
		{
			if(strings[i] instanceof String)
				s += strings[i].toString();
			else if(strings[i] instanceof Strings)
				s += ((Strings)strings[i]).get();
			else if(strings[i] instanceof Character)
				s += (char)strings[i];
			else if(strings[i] instanceof Integer)
				s += Integer.toString((int)strings[i]);
		}
		
		return s;
	}
	
	public void setTexture(Texture texture)
	{
		this.texture = texture;
	}
	
	@Override
	public void draw(Graphics g)
	{
		if(!isVisible())
			return;
		
		super.draw(g);
		
		if(texture != null)
		{
			texture.draw(g, getX(), getY(), getWidth(), getHeight());
			
			if(!isEnabled())
			{
				g.setColor(new Color(0x55, 0x55, 0x55, 0x60));
				g.fillRect(getX(), getY(), getWidth(), getHeight());
			}
			else if((mouseState & MOUSE_OVER) > 0)
			{
				g.setColor(new Color(0x88, 0x88, 0x88, 0x60));
				g.fillRect(getX(), getY(), getWidth(), getHeight());
			}
		}
		else
		{
			// TODO: mouse-over, mouse-pressed ect. state
			
			int arc = MathHelper.min(getWidth(), getHeight());
			arc -= arc/2.5;
			if(border == NONE)
				arc = 0;
			
			// render background
			g.setColor(!isEnabled() ? Color.gray : (mouseState & MOUSE_OVER) > 0 ? new Color(0x88, 0x88, 0x88) : Color.darkGray);
			g.fillRoundRect(getX(), getY(), getWidth(), getHeight(), arc, arc);
			
			// render text
			drawForeground(g);
		}
	}
	
	protected void drawForeground(Graphics g)
	{
		if(strings != null && strings.length > 0)
		{
			String s = getText();
			
			int textSize = (int)(getHeight()*0.4);
			g.setColor(isEnabled() ? Color.white : Color.lightGray);
			GuiLabel.draw(g, s, getX() + getWidth()/2, getY() + getHeight()/2 - textSize/2, GuiLabel.ALIGNMENT_MIDDLE, textSize);
		}
	}
	
	private Object[] strings;
	private Texture texture;

	private int mouseState;
	private static final int MOUSE_NONE = 0;
	private static final int MOUSE_OVER = 1;
	private static final int MOUSE_PRESSED = 2;
	private static final int MOUSE_OVER_PRESSED = MOUSE_OVER | MOUSE_PRESSED;

	private int border;
	public static final int NONE = 0;
	public static final int ALL = 1;
//	public static final int TOPLEFT = 1;
//	public static final int TOPRIGHT = 2;
//	public static final int BOTTOMLEFT = 4;
//	public static final int BOTTOMRIGHT = 8;
//	public static final int LEFT = TOPLEFT | BOTTOMLEFT;
//	public static final int RIGHT = TOPRIGHT | BOTTOMRIGHT;
//	public static final int TOP = TOPLEFT | TOPRIGHT;
//	public static final int BOTTOM = BOTTOMLEFT | BOTTOMRIGHT;
//	public static final int ALL = LEFT | RIGHT;
}