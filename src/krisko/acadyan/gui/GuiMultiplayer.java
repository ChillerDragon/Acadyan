package krisko.acadyan.gui;

import java.awt.Color;
import java.io.File;

import krisko.acadyan.Acadyan;
import krisko.acadyan.Game;
import krisko.acadyan.Strings;

public class GuiMultiplayer extends Gui
{
	public GuiMultiplayer(Game game)
	{
		super(game);
		init();
		
		setPage(0);
		
		// set the server-ip textbox focused
		setFocused(textboxIP);
	}
	
	private void init()
	{
		// add join/host buttons
		int buttonW = Game.ScreenWidth/2;
		int buttonH = 70;
		add(new GuiButton(0, 0, 0, buttonW, buttonH, Strings.JOIN_SERVER).setBorder(GuiButton.NONE).setEnabled(false));
		add(new GuiButton(1, buttonW, 0, Game.ScreenWidth-buttonW, buttonH, Strings.HOST_SERVER).setBorder(GuiButton.NONE));
		
		// add back button
		buttonW = 250; // Game.ScreenWidth
		buttonH = 70;
		add(new GuiButton(-1, Game.ScreenWidth/2 - buttonW/2, Game.ScreenHeight - buttonH - 15, buttonW, buttonH, Strings.BACK));
		
		// add port label
		add(new GuiLabel(Game.ScreenWidth - 10, Game.ScreenHeight - 30, 20, GuiLabel.ALIGNMENT_RIGHT, "Port: " +Game.Port));
		
		initJoin();
		initHost();
	}
	
	private void initJoin()
	{
		int y = 300;
		
		// add label
		add(new GuiLabel(Game.ScreenWidth/2, y, 40, GuiLabel.ALIGNMENT_MIDDLE, "Server-IP"), 0);
		
		y += 50;
		
		// add server-ip textbox
		int textboxW = 600;
		int textboxH = 50;
		textboxIP = new GuiTextbox(0, Game.ScreenWidth/2 - textboxW/2, y, textboxW, textboxH, game.lastIP);
		textboxIP.setMaxChars(32);
		add(textboxIP, 0);
		
		y += textboxH + 40;
		
		// add join button
		int buttonW = 250;
		int buttonH = 70;
		add(new GuiButton(2, Game.ScreenWidth/2 - buttonW/2, y, buttonW, buttonH, Strings.JOIN), 0);
	}
	
	private void initHost()
	{
		int y = 300;
		
		// add label
//		add(new GuiLabel(Game.ScreenWidth/2, y, 40, GuiLabel.ALIGNMENT_MIDDLE, "Map name"), 1);
		
		y += 50;
		
		// add mapname textbox
//		int textboxW = 500;
		int textboxH = 50;
//		textboxMapname = new GuiTextbox(1, Game.ScreenWidth/2 - textboxW/2, y, textboxW, textboxH);
//		textboxMapname.setMaxChars(32);
//		add(textboxMapname, 1);
		
		y += textboxH + 40;
		
		// add host button
		int buttonW = 250;
		int buttonH = 70;
		add(new GuiButton(3, Game.ScreenWidth/2 - buttonW/2, y, buttonW, buttonH, Strings.HOST), 1);
	}
	
	@Override
	public void onButtonClicked(GuiButton button)
	{
		switch(button.id)
		{
			case -1: { // back
				game.openGui(parentGui);
				break;
			}
			
			case 0: { // join server (page)
				setPage(0);
				setButtonsEnabled(true, 1);
				button.setEnabled(false);
				setFocused(textboxIP);
				break;
			}
			
			case 1: { // host server (page)
				setPage(1);
				setButtonsEnabled(true, 0);
				button.setEnabled(false);
//				setFocused(textboxMapname);
				break;
			}
			
			case 2: { // join
				game.lastIP = textboxIP.getText();
				
				if(game.joinServer(textboxIP.getText()))
					game.openGui(null);
				break;
			}
			
			case 3: { // host
				game.openGui(new GuiLevelSelectMP(game));
//				if(game.openServer(textboxMapname.getText()))
//					game.openGui(null);
				break;
			}
		}
	}
	
	@Override
	public void onTextboxChanged(GuiTextbox textbox)
	{
		switch(textbox.id)
		{
			case 1: { // mapname
				if(textbox.getText().length() > 0)
				{
					// get map file
					File file = new File(Acadyan.FolderPath +Acadyan.FileSeperator +"Maps" +Acadyan.FileSeperator +textbox.getText() +".mp");
					
					// color background)
					textbox.setBackgroundColor(file.exists() ? Color.green : Color.red);
				}
				else
				{
					textbox.setBackgroundColor(null);
				}
				break;
			}
		}
	}
	
	private GuiTextbox textboxIP;
//	private GuiTextbox textboxMapname;
}