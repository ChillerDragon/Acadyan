package krisko.acadyan.gui;

import java.awt.Color;
import java.awt.Graphics;

import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.MathHelper;

public class GuiCheckbox extends GuiComponent
{
	public GuiCheckbox(int id, int x, int y, int size)
	{
		this(id, x, y, size, false);
	}
	
	public GuiCheckbox(int id, int x, int y, int size, boolean checked)
	{
		super(id, x, y, size, size);
		setChecked(checked);
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		for(int i = 0; i < events.length; i++)
		{
			KEvent e = events[i];
			if(e.type != KEvent.Type.MOUSE_PRESSED)
				continue;
			
			if(MathHelper.pointInRect(e.mouseX, e.mouseY, getX(), getY(), getWidth(), getHeight()))
			{
				checked = !checked;
				
				if(listener != null)
					listener.onActionPerformed(this);
				else
					parentGui.onCheckboxClicked(this);
			}
		}
	}
	
	public void setChecked(boolean checked) { this.checked = checked; }
	
	public boolean isChecked() { return checked; }
	
	@Override
	public void draw(Graphics g)
	{
		g.setColor(Color.black);
		
		// draw empty square
		GuiLabel.draw(g, Character.toString((char)(9744)), getX(), getY(), GuiLabel.ALIGNMENT_LEFT, getWidth());

		// draw checkmark
		if(checked)
			GuiLabel.draw(g, Character.toString((char)(10003)), getX() + (int)(getWidth()*0.175f), getY() + (int)(getHeight()*0.07f), GuiLabel.ALIGNMENT_LEFT, (int)(getWidth()*0.875f));
	}
	
	private boolean checked;
}