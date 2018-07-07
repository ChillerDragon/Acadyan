package krisko.acadyan;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Controls
{
	private Controls(Strings name, String synonym, int keyCode)
	{
		list.add(this);
		this.name = name;
		this.synonym = synonym;
		key = keyCode;
	}
	
	public boolean isPressed(InputHandler handler)
	{
		if(key == -1)
			return handler.leftDown;
		else if(key == -2)
			return handler.rightDown;
		else if(key >= 0 && key < handler.keys.length)
			return handler.keys[key];
		else
			return false;
	}
	
	public boolean isPressed2(InputHandler handler)
	{
		if(key == -1)
		{
			boolean flag = handler.leftDown;
			handler.leftDown = false;
			return flag;
		}
		else if(key == -2)
		{
			boolean flag = handler.rightDown;
			handler.rightDown = false;
			return flag;
		}
		else if(key >= 0 && key < handler.keys.length)
		{
			boolean flag = handler.keys[key];
			handler.stopUpdate(key);
			return flag;
		}
		else
		{
			return false;
		}
	}
	
	public static void load() { }
	
	public final Strings name;
	public final String synonym;
	public int key;
	
	public static final ArrayList<Controls> list = new ArrayList<Controls>();
	
	// ingame move
	public static final Controls left = new Controls(Strings.LEFT, "left", KeyEvent.VK_A);
	public static final Controls right = new Controls(Strings.RIGHT, "right", KeyEvent.VK_D);
	public static final Controls jump = new Controls(Strings.JUMP, "jump", KeyEvent.VK_SPACE);
	public static final Controls sprint = new Controls(Strings.SPRINT, "sprint", KeyEvent.VK_SHIFT);
	public static final Controls sneak = new Controls(Strings.SNEAK, "sneak", KeyEvent.VK_ALT);
	
	public static final Controls punch = new Controls(Strings.PUNCH, "punch", -1);
	
	public static final Controls color = new Controls(Strings.CHANGE_COLOR, "color", KeyEvent.VK_Q);
	public static final Controls hide = new Controls(Strings.HIDE_PLAYER, "hide_player", KeyEvent.VK_H);
	public static final Controls inventory = new Controls(Strings.INVENTORY, "inventory", KeyEvent.VK_E);
}