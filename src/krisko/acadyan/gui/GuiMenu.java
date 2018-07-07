package krisko.acadyan.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import krisko.acadyan.Acadyan;
import krisko.acadyan.Editor;
import krisko.acadyan.Game;
import krisko.acadyan.Strings;

public class GuiMenu extends Gui
{
	public GuiMenu(Game game)
	{
		super(game);
		init();
	}
	
	public void init()
	{
		// label Acadyan
		lTitle = new GuiLabel(Game.ScreenWidth/2, 25, 100, GuiLabel.ALIGNMENT_MIDDLE, "Acadyan");
		add(lTitle);
		
		// label account
		add(new GuiLabel(20, Game.ScreenHeight - 60, 40, GuiLabel.ALIGNMENT_LEFT, Strings.LOGGEDIN_AS, ": " +game.getAccountName()));
		
		int buttonW = 250;
		int buttonH = 70;
		
		int x = (int)(Game.ScreenWidth/2.0D - buttonW/2.0D);
		int y = 170;
		int gap = 20;
		
		Strings[] str = {
			Strings.PLAY,
			Strings.LEVEL_SELECT,
			Strings.SHOP,
			Strings.MULTIPLAYER,
			Strings.OPTIONS,
			Strings.EXIT
		};
		
		// buttons
		for(int i = 0; i < str.length; i++)
		{
			add(new GuiButton(i, x, y, buttonW, buttonH, str[i]));
			y += buttonH + gap;
		}
		
		// developer stuff
		add(new GuiLabel(Game.ScreenWidth - buttonW/2 - 40, Game.ScreenHeight - buttonH - 80, 30, GuiLabel.ALIGNMENT_MIDDLE, "Developer Stuff"));
		add(new GuiButton(100, Game.ScreenWidth - buttonW - 40, Game.ScreenHeight - buttonH - 40, buttonW, buttonH, Strings.EDITOR));
//		add(new GuiButton(100, Game.ScreenWidth - buttonW - 40, Game.ScreenHeight - buttonH - 40, buttonW, buttonH, Strings.LEVEL_SELECT));
		
		// Acadyan folder
		add(new GuiButton(200, Game.ScreenWidth - buttonW + buttonW/3 - 40, 300, buttonW - buttonW/3, buttonH - buttonH/3, "Acaydan folder"));
	}
	
	@Override
	public void onButtonClicked(GuiButton button)
	{
		switch(button.id)
		{
			case 0: // play
				game.openWorld("World1", true, false);
				game.openGui(null);
				break;
			
			case 1: // level select
				game.openGui(new GuiLevelSelect(game).setParentGui(this));
				break;
			
			case 2: // shop
				game.openGui(new GuiShop(game).setParentGui(this));
				break;
			
			case 3:
				game.openGui(new GuiMultiplayer(game).setParentGui(this));
				break;
			
			case 4: // options
				game.openGui(new GuiOptions(game).setParentGui(this));
				break;
			
			case 5: // close
				game.close();
				break;
			
			case 100: // editor
				game.openGui(new Editor(game).setParentGui(this));
//				String result = JOptionPane.showInputDialog("Levelname:");
//				if(result == null)
//					break;
//				
//				if(game.openWorld(result, false, false))
//					game.openGui(null);
				break;
			
			case 200: // acadyan folder
				if(Desktop.isDesktopSupported())
				{
					try
					{
						Desktop.getDesktop().open(new File(Acadyan.FolderPath));
					} catch(IOException ex)
					{
						// TODO: Tell user that this doesnt work
					}
				}
				else
				{
					// here too...
				}
				break;
		}
	}
	
	private GuiLabel lTitle;
}