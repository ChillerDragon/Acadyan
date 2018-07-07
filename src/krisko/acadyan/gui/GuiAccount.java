package krisko.acadyan.gui;

import krisko.acadyan.Account;
import krisko.acadyan.Game;
import krisko.acadyan.Options;
import krisko.acadyan.Strings;

public class GuiAccount extends Gui
{
	public GuiAccount(Game game)
	{
		super(game);
		
		// label
		add(new GuiLabel(Game.ScreenWidth/2, Game.ScreenHeight/2 - 140, 60, GuiLabel.ALIGNMENT_MIDDLE, Strings.ACCOUNT_NAME));
		
		// textbox
		textbox = new GuiTextbox(0, Game.ScreenWidth/2 - 200, Game.ScreenHeight/2 - 60, 400, 50, Options.lastLoggedIn);
		textbox.setMaxChars(16);
		textbox.setAllowed(GuiTextbox.EVERYTHING);
		add(textbox);
		setFocused(textbox);
		
		// button back
		add(new GuiButton(0, Game.ScreenWidth/2 - 125, Game.ScreenHeight - 200, 250, 80, Strings.OKAY));
	}
	
	@Override
	public void onButtonClicked(GuiButton button)
	{
		if(textbox.getText().length() == 0)
			return;
		
		Account account = new Account(textbox.getText());
		
		if(!account.load())
			account.reset();
		
		Options.lastLoggedIn = account.getName();
		Options.save();

		game.account = account;
		game.openGui(new GuiMenu(game));
	}
	
	private GuiTextbox textbox;
}