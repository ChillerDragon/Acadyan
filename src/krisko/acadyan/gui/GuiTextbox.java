package krisko.acadyan.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import krisko.acadyan.Acadyan;
import krisko.acadyan.Game;
import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.MathHelper;

public class GuiTextbox extends GuiComponent
{
	public GuiTextbox(int id, int x, int y, int w, int h)
	{
		this(id, x, y, w, h, "");
	}
	
	public GuiTextbox(int id, int x, int y, int w, int h, String text)
	{
		super(x, y, w, h);
		if(text == null)
			text = "";
		
		this.id = id;
		
//		label = new GuiLabel(x + w/2, y, h, GuiLabel.ALIGNMENT_MIDDLE, getText());
		label = new GuiLabel(x+2, y, h, GuiLabel.ALIGNMENT_LEFT, getText());
		add(label);
		
		setMaxChars(32);
		setText(text);
		cursorPos = text.length();
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		super.update(handler, events);
		
		if(parentGui.getFocused() != this)
			return;
		
		String textBefore = text;
		boolean changed = false;
		
		for(int i = 0; i < events.length; i++)
		{
			KEvent e = events[i];
			if(e.type == KEvent.Type.KEY_PRESSED)
			{
				if(e.keyCode == KeyEvent.VK_LEFT)
				{
					if(cursorPos > 0)
						cursorPos--;
				}
				else if(e.keyCode == KeyEvent.VK_RIGHT)
				{
					if(cursorPos < text.length())
						cursorPos++;
				}
				else if(e.keyCode == KeyEvent.VK_END)
				{
					cursorPos = text.length();
				}
				continue;
			}
			else if(e.type == KEvent.Type.KEY_TYPED)
			{
				if(e.key == 3) // copy
				{
					Acadyan.setClipboardText(getText());
				}
				else if(e.key == 22) // paste
				{
					addText(Acadyan.getClipboardText());
				}
				else if(e.key == 24) // cut
				{
					Acadyan.setClipboardText(getText());
					setText(null);
				}
				else if(e.key == KeyEvent.VK_BACK_SPACE)
				{
					if(text.length() > 0)
						removeText(1);
					
					changed = true;
				}
				else if(e.key == KeyEvent.VK_ESCAPE)
				{
					// clear text
					setText("");
					
					changed = true;
				}
				else if(allowedChars == LETTERS ?		((e.key >= 'A' && e.key <= 'Z') || (e.key >= 'a' && e.key <= 'z') || e.key == '_') :
						allowedChars == NUMBERS ?		(e.key >= '0' && e.key <= '9') :
						allowedChars == ACCOUNT_NAME ?	((e.key >= 'A' && e.key <= 'Z') || (e.key >= 'a' && e.key <= 'z') || (e.key >= '0' && e.key <= '9') || e.key == '_') :
														((e.key >= 'A' && e.key <= 'Z') || (e.key >= 'a' && e.key <= 'z') || (e.key >= '0' && e.key <= '9') || e.key == '_' || e.key == '.' ||
														 e.key == '-' || e.key == ','))
				{
					addText(Character.toString(e.key));
					
					changed = true;
				}
			}
		}
		
		if(changed && !text.equals(textBefore))
			parentGui.onTextboxChanged(this);
	}
	
	public void setText(String s)
	{
		if(s == null)
			text = "";
		else
			text = s.length() > maxChars ? s.substring(0, maxChars) : s;
			
		label.setText(text);
		cursorPos = MathHelper.clamp(cursorPos, 0, text.length());
	}
	
	public void addText(String s)
	{
		if(s == null)
			return;
		
		if(text.length() == 0)
		{
			setText(s);
			cursorPos = text.length();
			return;
		}
		
		String newText = text.substring(0, cursorPos) +s +text.substring(cursorPos);
		
		if(newText.length() <= maxChars)
		{
			setText(newText);
			cursorPos += s.length();
		}
		else
		{
			int cut = newText.length() - maxChars;
			int add = s.length() - cut;
			setText(text.substring(0, cursorPos) +s.substring(0, add) +text.substring(cursorPos));
			cursorPos += add;
		}
	}
	
	public void removeText(int amount)
	{
		if(cursorPos == 0)
			return;
		
		int newPos = MathHelper.clamp(cursorPos - amount, 0, cursorPos);
		setText(text.substring(0, newPos) +text.substring(cursorPos));
		cursorPos = newPos;
	}
	
	public void setBackgroundColor(Color color)
	{
		colorBackground = color == null ? BackgroundColor : color;
	}
	
	public void setBorderColor(Color color)
	{
		colorBorder = color == null ? BorderColor : color;
	}
	
	public void setAllowed(int type)
	{
		allowedChars = type;
	}
	
	@Override
	public void draw(Graphics g)
	{
		// draw background
		g.setColor(colorBackground);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(colorBorder);
		g.drawRect(getX(), getY(), getWidth()-1, getHeight()-1);
		
		// draw text
		super.draw(g);
		
		// draw cursor line
		if(parentGui.getFocused() == this && Game.tick() % Game.TicksPerSec*2 < Game.TicksPerSec)
		{
			Font font = g.getFont();
			g.setFont(new Font(null, 0, getHeight()));
			
			int x = getX() + g.getFontMetrics().stringWidth(text.substring(0, cursorPos));
			g.fillRect(x - 1, getY(), 3, getHeight());
			
			g.setFont(font);
		}
	}
	
	public String getText() { return text; }
	
	public void setMaxChars(int i) { maxChars = i; }
	
	public final int id;
	
	private int cursorPos;
	
	private String text;
	private int maxChars;
	private GuiLabel label;
	
	// Standard colors
	private static final Color BackgroundColor = new Color(0xEE, 0xEE, 0xEE);
	private static final Color BorderColor = Color.black;
	
	// Colors
	private Color colorBackground = BackgroundColor;
	private Color colorBorder = BorderColor;
	
	// Allowed chars state
	private int allowedChars;
	public static final int EVERYTHING = 0;
	public static final int LETTERS = 1;
	public static final int NUMBERS = 2;
	public static final int ACCOUNT_NAME = 3;
}