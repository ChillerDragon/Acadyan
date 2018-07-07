package krisko.acadyan.gui;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

import krisko.acadyan.Controls;
import krisko.acadyan.Game;
import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.Options;
import krisko.acadyan.SoundManager;
import krisko.acadyan.Strings;
import krisko.acadyan.Texture;

public class GuiOptions extends Gui
{
	public GuiOptions(Game game)
	{
		super(game);
		
		init();
		setPage(0);
	}
	
	private void init()
	{
		/** ~ LEFT SIDE ~ */
		// options label
		add(new GuiLabel(fieldWidth/2, 20, 50, GuiLabel.ALIGNMENT_MIDDLE, Strings.OPTIONS));
		
		// back button
		add(new GuiButton(-1, 0, fieldHeight - 100, fieldWidth, fieldWidth/3, Strings.BACK).setBorder(GuiButton.NONE));
		
		// general button
		int x = 0;
		int y = 200;
		int buttonW = fieldWidth;
		int buttonH = fieldWidth/4;
		add(new GuiButton(0, x, y, buttonW, buttonH, Strings.GENERAL).setBorder(GuiButton.NONE).setEnabled(false));
		
		// audio button
		y += buttonH;
		add(new GuiButton(1, x, y, buttonW, buttonH, Strings.AUDIO).setBorder(GuiButton.NONE));
		
		// controls button
		y += buttonH;
		add(new GuiButton(2, x, y, buttonW, buttonH, Strings.CONTROLS).setBorder(GuiButton.NONE));
		
		/** ~ RIGHT SIDE ~ */
		initGeneral();
		initAudio();
		initControls();
	}
	
	private void initGeneral()
	{
		int x = fieldWidth + (Game.ScreenWidth - fieldWidth)/2;
		int y = 150;
		int buttonW = 300;
		int buttonH = 60;
		
		// general label
		add(new GuiLabel(x, 20, 50, GuiLabel.ALIGNMENT_MIDDLE, Strings.GENERAL), 0);
		
		// language label
		add(new GuiLabel(x, y, 30, GuiLabel.ALIGNMENT_MIDDLE, Strings.LANGUAGE, ": ", Strings.CURRENT_LANGUAGE), 0);
		
		// language button
		add(new GuiButton(10, x - buttonW/2, y + 50, buttonW, buttonH, Strings.CHANGE_LANGUAGE), 0);
		
		// blood
		y += 180;
		add(new GuiLabel(x, y + 10, 30, GuiLabel.ALIGNMENT_RIGHT, Strings.BLOOD), 0);
		add(new GuiCheckbox(0, x + 10, y, 50, Options.blood), 0);
		
		// cursor scale
		x += 100;
		y += 120;
		int sliderW = 300;
		int sliderH = 60;
		add(new GuiLabel(x - sliderW/2 - 10, y + 10, 30, GuiLabel.ALIGNMENT_RIGHT, Strings.CURSOR_SCALE, ":"), 0);
		add(new GuiSlider(3, x - sliderW/2, y, sliderW, sliderH, (int)(Options.cursorScale*10), 5, 50), 0);
		add(lCursorScale = new GuiLabel(x + sliderW/2 + 10, y + 15, 25, GuiLabel.ALIGNMENT_LEFT, String.format("%.1f", Options.cursorScale)), 0);
		
		// cursor
		x = fieldWidth + 100;
		y += 140;
		buttonW = 64;
		buttonH = buttonW;
		int gap = 20;
		GuiComponentListener listener = new GuiComponentListener() {
			@Override
			public void onActionPerformed(GuiComponent comp)
			{
				Options.cursor = comp.id;
			}
		};
		for(int i = 0; i < 4; i++)
		{
			add(new GuiButton(i, x + i*buttonW + i*gap, y, buttonW, buttonH, Texture.get(Texture.cursor1.id + i)).setListener(listener), 0);
		}
	}
	
	private void initAudio()
	{
		int x = fieldWidth + (Game.ScreenWidth - fieldWidth)/2;
		int y = 220;
		int sliderW = 300;
		int sliderH = 60;
		
		// audio label
		add(new GuiLabel(x, 20, 50, GuiLabel.ALIGNMENT_MIDDLE, Strings.AUDIO), 1);
		
		// volume
		add(new GuiLabel(x, y, 55, GuiLabel.ALIGNMENT_MIDDLE, Strings.VOLUME), 1);
		
		// bgm volume
		y += 90;
		add(new GuiLabel(x - 100, y+5, 45, GuiLabel.ALIGNMENT_RIGHT, Strings.MUSIC), 1);
		add(new GuiSlider(0, x - 80, y, sliderW, sliderH, (int)(Options.volumeBgm*100), 0, 100), 1);
		
		// effects volume
		y += 80;
		add(new GuiLabel(x - 100, y+5, 45, GuiLabel.ALIGNMENT_RIGHT, Strings.EFFECTS), 1);
		add(new GuiSlider(1, x - 80, y, sliderW, sliderH, (int)(Options.volumeEffects*100), 0, 100), 1);
	}
	
	private void initControls()
	{
		changingKeyButton = -1;
		keyButtons = new GuiButton[Controls.list.size()];
		
		int x = fieldWidth + (Game.ScreenWidth - fieldWidth)/2;
		int y = 90;
		int buttonW = 130;
		int buttonH = 45;
		int gap = 20;
		int textButtonGap = 350;
		
		// controls label
		add(new GuiLabel(x, 20, 50, GuiLabel.ALIGNMENT_MIDDLE, Strings.CONTROLS), 2);
		
		// mouse sensitivity
//		int sliderW = 400;
//		int sliderH = 60;
//		x -= 80;
//		
//		add(new GuiLabel(x - 25, y + 15, 30, GuiLabel.ALIGNMENT_RIGHT, Strings.MOUSE_SENSITIVITY, ":"), 2);
//		add(new GuiSlider(2, x, y, sliderW, sliderH, (int)(Options.mouseSensitivity*100), 25, 400), 2);
//		add(lMouseSens = new GuiLabel(x + sliderW + 25, y + 10, 40, GuiLabel.ALIGNMENT_LEFT, String.format("%.2f", Options.mouseSensitivity)), 2);
//		
//		x -= 120;
//		y += sliderH + 70;
		
		x -= 200;
		y += 20;
		
		for(int i = 0; i < Controls.list.size(); i++)
		{
			Controls c = Controls.list.get(i);
			
			GuiLabel label = new GuiLabel(x, y + i*buttonH + i*gap, buttonH - buttonH/8, GuiLabel.ALIGNMENT_LEFT, c.name, ":");
			GuiButton button = new GuiButton(100+i, x + textButtonGap, y + i*buttonH + i*gap, buttonW, buttonH, c.key == 130 ? "^" : c.key == -1 ? "LM-Button" :
																												c.key == -2 ? "RM-Button" : KeyEvent.getKeyText(c.key));
			
			add(label, 2);
			add(button, 2);
			keyButtons[i] = button;
		}
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		if(changingKeyButton == -1)
		{
			super.update(handler, events);
		}
		else
		{
			// the hotkey to change
			Controls c = Controls.list.get(changingKeyButton);
			
			// loop through events and try setting new hotkey
			for(int i = 0; i < events.length; i++)
			{
				KEvent e = events[i];
				int key = e.keyCode;
				if(e.type == KEvent.Type.MOUSE_PRESSED)
				{
					if(e.leftClicked)
						key = -1;
					else if(e.rightClicked)
						key = -2;
					else
						continue;
				}
				else if(e.type != KEvent.Type.KEY_PRESSED || (e.keyCode >= '0' && e.keyCode <= '9'))
					continue;
				
				if(key != KeyEvent.VK_ESCAPE)
				{
					// loop through controls and check if the pressed key is already in use, if yes, change it to c.key
					for(int j = 0; j < Controls.list.size(); j++)
					{
						if(j == changingKeyButton)
							continue;
						
						Controls c1 = Controls.list.get(j);
						if(c1.key == key)
						{
							c1.key = c.key;
							keyButtons[j].setText(c1.key == 130 ? "^" : c1.key == -1 ? "LM-Button" : c1.key == -2 ? "RM-Button" : KeyEvent.getKeyText(c1.key));
							break;
						}
					}
					
					c.key = key;
				}
				
				// set button name
				keyButtons[changingKeyButton].setText(c.key == 130 ? "^" : c.key == -1 ? "LM-Button" : c.key == -2 ? "RM-Button" : KeyEvent.getKeyText(c.key)); // TODO: '^' = 130 ?
				keyButtons[changingKeyButton].setEnabled(true);
				changingKeyButton = -1;
				break;
			}
		}
	}
	
	@Override
	public void onButtonClicked(GuiButton button)
	{
		if(button.id >= 0 && button.id < 3)
		{
			// change page
			setPage(button.id);
			setButtonsEnabled(true, 0, 1, 2);
			button.setEnabled(false);
			return;
		}
		else if(button.id >= 100)
		{
			// controls
			button.setText("...");
			button.setEnabled(false);
			changingKeyButton = button.id - 100;
		}
		
		switch(button.id)
		{
			case -1: {
				Options.save();
				game.openGui(parentGui);
				break;
			}
			
			// general
			case 10: {
				Options.setLanguage(Options.getLanguage() ^ 1);
				break;
			}
		}
	}
	
	@Override
	public void onSliderChanged(GuiSlider slider)
	{
		switch(slider.id)
		{
			case 0: {
				Options.volumeBgm = slider.getValue() / 100.f;
				SoundManager.bgm1.setVolume(Options.volumeBgm);
				break;
			}
			
			case 1: {
				Options.volumeEffects = slider.getValue() / 100.f;
				break;
			}
			
			case 2: {
				Options.mouseSensitivity = slider.getValue() / 100.f;
				lMouseSens.setText(String.format("%.2f", Options.mouseSensitivity));
				break;
			}
			
			case 3: {
				Options.cursorScale = slider.getValue() / 10.f;
				lCursorScale.setText(String.format("%.1f", Options.cursorScale));
				break;
			}
		}
	}
	
	@Override
	public void onCheckboxClicked(GuiCheckbox checkbox)
	{
		Options.blood = checkbox.isChecked();
	}
	
	@Override
	public void draw(Graphics g)
	{
		super.draw(g);
		
		g.setColor(Game.ForegroundColor);
		for(int i = 0; i < 5; i++)
		{
			g.drawRect(i, i, fieldWidth - i*2, fieldHeight - i*2);
			g.drawRect(i, i, fieldWidth - i*2, 85-i*2);
		}
	}
	
	private GuiLabel lMouseSens;
	private GuiLabel lCursorScale;
	
	// size of the left field with the options buttons
	private int fieldWidth = 300;
	private int fieldHeight = Game.ScreenHeight;
	
	private int changingKeyButton;
	private GuiButton keyButtons[];
}