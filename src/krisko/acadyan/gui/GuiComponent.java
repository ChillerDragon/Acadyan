package krisko.acadyan.gui;

public abstract class GuiComponent extends Gui
{
	public GuiComponent(int x, int y, int w, int h)
	{
		this(0, x, y, w, h);
	}
	
	public GuiComponent(int id, int x, int y, int w, int h)
	{
		super(null);
		
		this.id = id;
		setEnabled(true);
		setVisible(true);
		setBounds(x, y, w, h);
	}
	
	public void setBounds(int x, int y, int w, int h)
	{
		setPosition(x, y);
		setSize(w, h);
	}
	
	public void setPosition(int x, int y)
	{
		posX = x;
		posY = y;
	}
	
	public void setSize(int w, int h)
	{
		width = w;
		height = h;
	}
	
	public GuiComponent setListener(GuiComponentListener listener)
	{
		this.listener = listener;
		return this;
	}
	
	public boolean isFocusable() { return true; }
	
	public GuiComponent setEnabled(boolean flag)
	{
		enabled = flag;
		return this;
	}
	
	public boolean isEnabled() { return isVisible() && enabled; }
	
	public GuiComponent setVisible(boolean flag)
	{
		visible = flag;
		return this;
	}
	
	public boolean isVisible() { return visible; }
	
	public int getX() { return posX; }
	
	public int getY() { return posY; }
	
	public int getWidth() { return width; }
	
	public int getHeight() { return height; }
	
	public final int id;
	
	private int posX, posY;
	private int width, height;
	private boolean enabled;
	private boolean visible;
	
	protected GuiComponentListener listener;
}