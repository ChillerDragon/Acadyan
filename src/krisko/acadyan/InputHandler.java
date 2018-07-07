package krisko.acadyan;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

public class InputHandler implements KeyListener, MouseMotionListener, MouseListener, MouseWheelListener
{
	public InputHandler()
	{
		eventList = new ArrayList<KEvent>();
		
		dMouseX = Game.ScreenWidth/2;
		dMouseY = Game.ScreenHeight/2;
		
//		try
//		{
//			robot = new Robot();
//		} catch(AWTException ex)
//		{
//			ex.printStackTrace();
//		}
//		
//		moveMouseToMiddle();
	}
	
	public void stopUpdate(int keyCode)
	{
		if(keys[keyCode])
		{
			stoppedUpdate[keyCode] = true;
			keys[keyCode] = false;
		}
	}
	
	public void stopKeyChar(int code)
	{
		if(code >= 0 && code < keyChars.length)
		{
			stoppedKeyChars[code] = true;
			keyChars[code] = false;
		}
	}
	
/**
 * Reset after updating the game
 */
	public void reset()
	{
		mouseWheelAmount = 0;
		leftClicked = false;
		rightClicked = false;
	}
	
// Key
	@Override
	public void keyPressed(KeyEvent e)
	{
		KEvent event = new KEvent(KEvent.Type.KEY_PRESSED);
		event.keyCode = e.getKeyCode() != 0 ? e.getKeyCode() : e.getKeyChar();
		addEvent(event);
		
		int code = e.getKeyCode();
		
		if(code > 0 && code < keys.length)
		{
			if(!stoppedUpdate[code])
				keys[code] = true;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		KEvent event = new KEvent(KEvent.Type.KEY_RELEASED);
		event.keyCode = e.getKeyCode() != 0 ? e.getKeyCode() : e.getKeyChar();
		addEvent(event);
		
		int code = e.getKeyCode();
		
		if(code > 0 && code < keys.length)
		{
			keys[code] = false;
			stoppedUpdate[code] = false;
		}
		
		char c = e.getKeyChar();
		if(c >= 0 && c < keyChars.length)
		{
			keyChars[c] = false;
			stoppedKeyChars[c] = false;
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e)
	{
		KEvent event = new KEvent(KEvent.Type.KEY_TYPED);
		event.key = e.getKeyChar();
		addEvent(event);
		
		char c = e.getKeyChar();
		
		if(c >= 0 && c < keyChars.length)
		{
			if(!stoppedKeyChars[c])
				keyChars[c] = true;
		}
	}
	
// Mouse Motion
	@Override
	public void mouseDragged(MouseEvent e)
	{
		updateMousePos(e.getX(), e.getY());
		
		KEvent event = new KEvent(KEvent.Type.MOUSE_DRAGGED);
		event.mouseX = mouseX;
		event.mouseY = mouseY;
		addEvent(event);
	}
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		updateMousePos(e.getX(), e.getY());
		
		KEvent event = new KEvent(KEvent.Type.MOUSE_MOVED);
		event.mouseX = mouseX;
		event.mouseY = mouseY;
		addEvent(event);
	}
	
// Mouse
	@Override
	public void mouseClicked(MouseEvent e)
	{
		updateMousePos(e.getX(), e.getY());
		
		KEvent event = new KEvent(KEvent.Type.MOUSE_CLICKED);
		event.mouseX = mouseX;
		event.mouseY = mouseY;
		addEvent(event);
	}
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
		updateMousePos(e.getX(), e.getY());
		
		KEvent event = new KEvent(KEvent.Type.MOUSE_ENTERED);
		event.mouseX = mouseX;
		event.mouseY = mouseY;
		addEvent(event);
		
		mouseInsideFrame = true;
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		KEvent event = new KEvent(KEvent.Type.MOUSE_EXITED);
		addEvent(event);
		
		mouseInsideFrame = false;
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		updateMousePos(e.getX(), e.getY());
		
		KEvent event = new KEvent(KEvent.Type.MOUSE_PRESSED);
		event.mouseX = mouseX;
		event.mouseY = mouseY;
		event.leftClicked = e.getButton() == MouseEvent.BUTTON1;
		event.rightClicked = e.getButton() == MouseEvent.BUTTON3;
		addEvent(event);
		
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			leftClicked = true;
			leftDown = true;
		} else if(e.getButton() == MouseEvent.BUTTON3)
		{
			rightClicked = true;
			rightDown = true;
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		updateMousePos(e.getX(), e.getY());
		
		KEvent event = new KEvent(KEvent.Type.MOUSE_RELEASED);
		event.mouseX = mouseX;
		event.mouseY = mouseY;
		addEvent(event);
		
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			leftClicked = false;
			leftDown = false;
		} else if(e.getButton() == MouseEvent.BUTTON3)
		{
			leftClicked = false;
			rightDown = false;
		}
	}
	
// Mouse Wheel
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		KEvent event = new KEvent(KEvent.Type.MOUSE_WHEEL);
		event.mouseWheelAmount = e.getWheelRotation();
		addEvent(event);
		
		mouseWheelAmount = e.getWheelRotation();
	}
	
// Events
	private void addEvent(KEvent event)
	{
		eventList.add(event);
	}
	
	public synchronized KEvent[] getPassedEvents()
	{
//		KEvent events[] = (KEvent[])eventList.toArray();
		KEvent events[] = eventList.toArray(new KEvent[0]);
		eventList.clear();
		return events;
	}
	
//	public KEvent[] getPassedEvents()
//	{
//		int nullEvents = 0;
//		KEvent events[] = new KEvent[eventList.size()];
//		for(int i = 0; i < events.length; i++)
//		{
//			events[i] = eventList.remove(0);
//			if(events[i] == null)
//				nullEvents++;
//		}
//		if(nullEvents > 0) // unfortunately, this happens sometimes. i just don't know why
//		{
////			System.out.println("Nullevents: " +nullEvents);
//			KEvent e[] = events;
//			events = new KEvent[e.length-nullEvents];
//			
//			int j = 0;
//			for(int i = 0; i < e.length; i++)
//			{
//				if(e[i] == null)
//					continue;
//				
//				events[j++] = e[i];
//			}
//		}
//		return events;
//		
//		/*  !  WARNING  !
//		 * !! NOT  THIS !!
//		 * 
//		 * KEvent events[] = (KEvent[])eventList.toArray();
//		 * eventList.clear(); // now there's maybe 1 event more in the list, so don't just clear everything
//		 * return events;
//		 */
//	}
	
//	public int getEventSize() { return eventList.size(); }
	
//	public KEvent nextEvent() { return eventList.size() == 0 ? null : eventList.remove(0); }
	
// Other stuff
	private synchronized void updateMousePos(int x, int y)
	{
		if(mouseFlag)
		{
			mouseX = Game.ScreenWidth * x / Acadyan.SCREEN_WIDTH_REAL;
			mouseY = Game.ScreenHeight * y / Acadyan.SCREEN_HEIGHT_REAL;
		}
		else
		{
			int dX = x - lastRealMouseX;
			int dY = y - lastRealMouseY;
			
			if(dX == 0 && dY == 0)
				return;
			
			dMouseX +=  (double)dX * Options.mouseSensitivity;
			dMouseY +=  (double)dY * Options.mouseSensitivity;
			
			dMouseX = MathHelper.clamp(dMouseX, 0, Game.ScreenWidth-1);
			dMouseY = MathHelper.clamp(dMouseY, 0, Game.ScreenHeight-1);
			
			mouseX = (int)dMouseX;
			mouseY = (int)dMouseY;
			
			lastRealMouseX = x;
			lastRealMouseY = y;
			
//			robot.mouseMove(Acadyan.SCREEN_X + Acadyan.SCREEN_WIDTH_REAL/2, Acadyan.SCREEN_Y + Acadyan.SCREEN_HEIGHT_REAL/2);
		}
	}
	
//	public synchronized void moveMouseToMiddle()
//	{
//		lastRealMouseX = Acadyan.SCREEN_WIDTH_REAL/2;
//		lastRealMouseY = Acadyan.SCREEN_HEIGHT_REAL/2;
//		robot.mouseMove(Acadyan.SCREEN_X + lastRealMouseX, Acadyan.SCREEN_Y + lastRealMouseY);
//	}
	
	public int mouseX, mouseY;
	public double dMouseX, dMouseY;
	public boolean leftClicked, rightClicked;
	public boolean leftDown, rightDown;
	public boolean mouseInsideFrame;
	public int mouseWheelAmount;
	
	private int lastRealMouseX, lastRealMouseY;
	
	public boolean[] keys = new boolean[65536];
	private boolean[] stoppedUpdate = new boolean[keys.length];
	public boolean[] keyChars = new boolean[65536];
	private boolean[] stoppedKeyChars = new boolean[keyChars.length];
	
	private ArrayList<KEvent> eventList;

//	private Robot robot;
	private static final boolean mouseFlag = true;
}