package krisko.acadyan;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Acadyan extends Canvas implements Runnable
{
	public Acadyan()
	{
		Dimension size = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
		
		setPreferredSize(size);
		setMinimumSize(size);
		
		// Load
		Options.load();
		Texture.load(); // yes, that's "neccesary"
		Item.load();
		SoundManager.load();
		Controls.load();
		
		setBackground(Game.BackgroundColor);
		setForeground(Game.ForegroundColor);
		
		game = new Game();
		
		setFocusTraversalKeysEnabled(false);
		inputHandler = new InputHandler();
		addKeyListener(inputHandler);
		addMouseListener(inputHandler);
		addMouseMotionListener(inputHandler);
		addMouseWheelListener(inputHandler);
		
		// set blank cursor (draw cursor in paint method)
		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank"));
	}
	
	public static void setClipboardText(String str)
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(str), null);
	}
	
	public static String getClipboardText()
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try
		{
			return (String)clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor);
		} catch(Exception ex)
		{
			return null;
		}
	}
	
	public void run()
	{
		long startTimeSec = System.currentTimeMillis();
		long startTime = startTimeSec;
		long timeNow;
		long timePassed;
		final long timeHasToPass = 1000 / Game.TicksPerSec; // zeit, die pro tick vorbei sein sollte
//		int ticks = 0;
		
		SoundManager.bgm1.loop(-1);
		
		while(running)
		{
			timeNow = System.currentTimeMillis();
//			ticks++;
			
			// set frame x/y
			Point p = getLocationOnScreen();
			if(SCREEN_X != p.x || SCREEN_Y != p.y)
			{
				SCREEN_X = p.x;
				SCREEN_Y = p.y;
			}
			
			// set read screen size
			if(SCREEN_WIDTH_REAL != getWidth() || SCREEN_HEIGHT_REAL != getHeight())
			{
				SCREEN_WIDTH_REAL = getWidth();
				SCREEN_HEIGHT_REAL = getHeight();
			}
			
			 //get passed events
			KEvent events[] = inputHandler.getPassedEvents();
//			inputHandler.moveMouseToMiddle();
//			for(int i = 0; i < events.length; i++)
//			{
//				if(events[i].type == KEvent.Type.MOUSE_ENTERED || events[i].type == KEvent.Type.MOUSE_MOVED || events[i].type == KEvent.Type.MOUSE_DRAGGED)
//				{
//					mouseX = events[i].mouseX;
//					mouseY = events[i].mouseY;
//				}
//			}
			
			game.update(inputHandler, events);
			inputHandler.reset();
			repaint();
			
			timePassed = timeNow - startTime;
			if(timePassed < timeHasToPass)
			{
				try
				{
					Thread.sleep(timeHasToPass - timePassed);
				} catch(InterruptedException ex)
				{ }
			}
			
			startTime += timeHasToPass;
			
			if(startTimeSec <= startTime - 1000)
			{
//				fps = ticks;
//				ticks = 0;
				startTimeSec += 1000;
			}
		}
	}
	
	public void paint(Graphics g)
	{
		if(game != null)
			game.draw(g);
		
		if(game == null || game.isGuiOpen())
		{
			Texture.cursor.draw(g, inputHandler.mouseX, inputHandler.mouseY);
		}
		else
		{
			Texture t = Texture.get(Texture.cursor1.id + Options.cursor);
			int width = (int)(t.getImage().getWidth() * Options.cursorScale);
			int height = (int)(t.getImage().getHeight() * Options.cursorScale);
			
			t.draw(g, inputHandler.mouseX - width/2, inputHandler.mouseY - height/2, width, height);
		}
	}
	
	public void update(Graphics g)
	{
		if(image == null)
		{
			image = createImage(Game.ScreenWidth, Game.ScreenHeight);
			graphics = image.getGraphics();
		}
		
		graphics.setColor(getBackground());
		graphics.fillRect(0, 0, Game.ScreenWidth, Game.ScreenHeight);
		graphics.setColor(getForeground());
		paint(graphics);
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
//		g.drawImage(resizeImg(image, getWidth(), getHeight()), 0, 0, getWidth(), getHeight(), null);
	}
	
//	private BufferedImage resizeImg(Image img, int width, int height)
//	{
//		int sourceWidth = img.getWidth(null);
//		int sourceHeight = img.getHeight(null);
//		BufferedImage source = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_RGB);
//		source.getGraphics().drawImage(img, 0, 0, null);
//		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		
//		int pixelsSource[] = ((DataBufferInt)source.getRaster().getDataBuffer()).getData();
//		int pixelsDest[] = ((DataBufferInt)dest.getRaster().getDataBuffer()).getData();
//		
//		for(int y = 0; y < height; y++)
//		{
//			int sourceY = sourceHeight * y / height;
//			
//			for(int x = 0; x < width; x++)
//			{
//				int sourceX = sourceWidth * x / width;
//				
//				int middle = 0;
//				int num = 0;
//				for(int j = -1; j <= 1; j++)
//				{
//					if(sourceY+j < 0 || y+j >= )
//						continue;
//					
//					for(int i = -1; i <= 1; i++)
//					{
//						;
//					}
//				}
//				
//				pixelsDest[x + y*width] = pixelsSource[sourceX + sourceY*sourceWidth];
//			}
//		}
//		
//		return dest;
//	}
	
	public void start()
	{
		if(running)
			return;
		
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void onWindowClosing()
	{
		if(game != null)
			game.onClose();
	}
	
	public static void main(String argv[])
	{
		final Acadyan main = new Acadyan();
		JFrame frame = new JFrame("Acadyan");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		/** Fullscreen */
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		if(gd.isFullScreenSupported())
//		{
//			frame.setUndecorated(true);
//			gd.setFullScreenWindow(frame);
//		}
//		else
//		{
//			// Fullscreen not supported
//		}
//		frame.setUndecorated(true);
//		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		/** Fullscreen */
		
		frame.add(main);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				main.onWindowClosing();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		
		frame.setVisible(true);
		
		main.start();
		main.requestFocus();
	}
	
	private Game game;
	
	private boolean running;
	private InputHandler inputHandler;
//	private int mouseX, mouseY;
	
	private static final int SCREEN_WIDTH = Game.ScreenWidth;
	private static final int SCREEN_HEIGHT = Game.ScreenHeight;
	
	public static int SCREEN_WIDTH_REAL = SCREEN_WIDTH;
	public static int SCREEN_HEIGHT_REAL = SCREEN_HEIGHT;
	public static int SCREEN_X, SCREEN_Y;
	
	private Thread thread;
	private Image image;
	private Graphics graphics;
	private static final long serialVersionUID = 1L;
	
	public static final String LineSeperator = System.getProperty("line.separator");
	public static final String FileSeperator = System.getProperty("file.separator");
	public static final String FolderPath = (System.getProperty("os.name").contains("Mac")
												? (System.getProperty("user.home") +"/Library/Application Support")
												: System.getenv("APPDATA"))
											+FileSeperator +"Acadyan";
	// System.getProperty("user.dir")
}