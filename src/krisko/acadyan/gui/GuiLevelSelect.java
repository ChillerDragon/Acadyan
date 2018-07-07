package krisko.acadyan.gui;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

import krisko.acadyan.Game;
import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.MathHelper;
import krisko.acadyan.Strings;

public class GuiLevelSelect extends Gui
{
	public GuiLevelSelect(Game game, int world)
	{
		super(game);
		this.world = MathHelper.clamp(world, 1, Game.LevelAmount.length);
		init();
	}
	
	public GuiLevelSelect(Game game)
	{
		this(game, 1);
	}
	
	private void init()
	{
		// button back
		add(new GuiButton(-1, 0, 0, 150, 50, Strings.BACK).setBorder(GuiButton.NONE));
		
		// add world label
		lWorld = new GuiLabel(Game.ScreenWidth/2, 40, 60, GuiLabel.ALIGNMENT_MIDDLE, Strings.WORLD, " ", Integer.toString(world));
		add(lWorld);
//		add(new GuiLabel(game, Game.ScreenWidth/2, 40, 60, GuiLabel.ALIGNMENT_MIDDLE, Strings.LEVEL_SELECT));
		
		int rows = 3;
		int cols = 4;
		int gap = 40;
		
		int buttonW = 200;
		int buttonH = 100;

		int w = buttonW*cols + gap*cols + gap;
		int h = buttonH*rows + gap*rows + gap;
		
		int x = Game.ScreenWidth/2 - w/2;
		int y = Game.ScreenHeight/2 - h/2 + 60;
		
		x += gap;
		y += gap;
		
		for(int j = 0; j < rows; j++)
		{
			for(int i = 0; i < cols; i++)
			{
				int level = 1 + i + j*cols;
				int stars = 2;
				int goldblocks = 1;
				int money = 20;
				int maxMoney = 50;
				add(new GuiLevelButton(i + j*cols, x + i*buttonW + i*gap, y + j*buttonH + j*gap, buttonW, buttonH,
										level, stars, goldblocks, money, maxMoney));
			}
		}
		
		// button previous/next world
		buttonW = 100;
		buttonH = (int)(Game.ScreenHeight*0.4);
		add(new GuiButton(100, 0, Game.ScreenHeight/2 - buttonH/2, buttonW, buttonH, "<").setBorder(GuiButton.NONE));
		add(new GuiButton(101, Game.ScreenWidth - buttonW, Game.ScreenHeight/2 - buttonH/2, buttonW, buttonH, ">").setBorder(GuiButton.NONE));
		
		deactivate();
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		super.update(handler, events);
		
		if(handler.keys[KeyEvent.VK_ESCAPE])
		{
			handler.stopUpdate(KeyEvent.VK_ESCAPE);
			game.openGui(new GuiMenu(game));
		}
	}
	
	@Override
	public void onButtonClicked(GuiButton button)
	{
		if(button.id == -1) // back
		{
			game.openGui(new GuiMenu(game));
			return;
		}
		
		if(button.id == 100 || button.id == 101) // previous/next world
		{
			world = MathHelper.clamp(world + (button.id-100)*2 - 1, 1, Game.LevelAmount.length);
			lWorld.setText(Strings.WORLD, " ", Integer.toString(world));
			deactivate();
			return;
		}
		
//		if(game.openMap(String.format("%d_%d", world, button.id+1)))
		if(game.openMap(world, button.id+1))
			game.openGui(null);
	}
	
	private void deactivate()
	{
		for(int i = 0; i < 12; i++)
		{
			boolean visible = Game.LevelAmount[world-1] > i;
			setButtonsVisible(visible, i);
			
			if(visible)
				setButtonsEnabled(game.account.level[world-1] > i, i);
		}
	}
	
	private GuiLabel lWorld;
	
	/** Which world to display */
	private int world;
}