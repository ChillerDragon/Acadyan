package krisko.acadyan;

public class KEvent
{
	public KEvent(Type eventType)
	{
		type = eventType;
	}
	
	public int mouseX, mouseY;
	public boolean leftClicked, rightClicked;
	public boolean mouseInsideFrame;
	public int mouseWheelAmount;
	
	/** Use this for KEY_TYPED */
	public char key;
	/** Use this for KEY_PRESSED and KEY_RELEASED */
	public int keyCode;
	
	public final Type type;
	
	public static enum Type
	{
		MOUSE_PRESSED,
		MOUSE_RELEASED,
		MOUSE_CLICKED,
		
		MOUSE_MOVED,
		MOUSE_DRAGGED,
		MOUSE_WHEEL,
		
		MOUSE_ENTERED,
		MOUSE_EXITED,
		
		KEY_PRESSED,
		KEY_RELEASED,
		KEY_TYPED
	}
}