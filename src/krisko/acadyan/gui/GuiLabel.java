package krisko.acadyan.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import krisko.acadyan.Strings;
import krisko.acadyan.Texture;

public class GuiLabel extends GuiComponent
{
	public GuiLabel(int x, int y, int textSize, int alignment, Object... str)
	{
		super(x, y, -1, -1);
		setText(str);
		setTextSize(textSize);
		setAlignment(alignment);
	}
	
	public GuiLabel(int x, int y, int w, int h, Texture texture)
	{
		super(x, y, w, h);
		setTexture(texture);
	}
	
	public GuiLabel(int x, int y, Texture texture)
	{
		this(x, y, -1, -1, texture);
	}
	
// text
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
	
	public void setTextSize(int size) { textSize = size; }
	
	public int getTextSize() { return textSize; }
	
// alignment
	public void setAlignment(int alignmentType) { alignment = alignmentType; }
	
	public int getAlignment() { return alignment; }
	
// image
	public void setTexture(Texture t) { texture = t; }
	
	public Texture getTexture() { return texture; }
	
	@Override
	public void draw(Graphics g)
	{
		if(!isVisible())
			return;
		
		super.draw(g);
		
		if(texture != null)
		{
			if(getWidth() < 0 || getHeight() < 0)
				texture.draw(g, getX(), getY());
			else
				texture.draw(g, getX(), getY(), getWidth(), getHeight());
		}
		
		if(strings != null && strings.length > 0)
		{
			g.setColor(Color.black);
			draw(g, getText(), getX(), getY(), alignment, textSize);
		}
	}
	
	public static void draw(Graphics g, String text, int x, int y, int alignment, int textSize)
	{
		if(text == null)
			return;
		
		Font font = g.getFont();
		g.setFont(new Font(null, 0, textSize));
		
		y += g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent();
		
		if(alignment == ALIGNMENT_MIDDLE)
			x -= g.getFontMetrics().stringWidth(text)/2;
		else if(alignment == ALIGNMENT_RIGHT)
			x -= g.getFontMetrics().stringWidth(text);
		g.drawString(text, x, y);
		
		g.setFont(font);
	}
	
	private Object[] strings;
	private int textSize;
	private int alignment;
	private Texture texture;
	
	public static final int ALIGNMENT_LEFT = 0;
	public static final int ALIGNMENT_MIDDLE = 1;
	public static final int ALIGNMENT_RIGHT = 2;
}