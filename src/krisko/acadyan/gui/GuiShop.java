package krisko.acadyan.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import krisko.acadyan.Game;
import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.Strings;
import krisko.acadyan.Texture;

public class GuiShop extends Gui
{
	public GuiShop(Game game)
	{
		super(game);
		init();
		setPage(0);
	}
	
	public void init()
	{
		if(game.account == null)
			return;
		
		// Categories
		Strings[] categories = {
				Strings.SKINS,
				Strings.STATS,
				Strings.UPGRADES,
				Strings.ENCHANTMENTS
		};
		
		int buttonW = Game.ScreenWidth / categories.length;
		int buttonH = 100;
		for(int i = 0; i < categories.length; i++)
		{
			GuiButton button;
			
			if(i < categories.length-1)
				button = new GuiButton(i, i * buttonW, Game.ScreenHeight - buttonH, buttonW, buttonH, categories[i]);
			else
				button = new GuiButton(i, i * buttonW, Game.ScreenHeight - buttonH, Game.ScreenWidth - buttonW*(categories.length-1), buttonH, categories[i]);
			
			button.setBorder(GuiButton.NONE);
			button.setEnabled(i > 0);
			
			add(button);
		}
		
		initSkins();
		initSkills();
		initUpgrades();
		initEnchantments();
		
		// button back
//		buttonW = 300;
//		buttonH = 200;
//		x = (int)(Game.ScreenWidth/2.0D - buttonW/2.0D);
//		y = Game.ScreenHeight - buttonH - 150;
		add(new GuiButton(-1, 0, 0, 150, 50, Strings.BACK).setBorder(GuiButton.NONE));
		
		setButtonsVisible(false);
		setButtonsVisible(true, -1, 0, 1, 2, 3);
	}

	private final int colorCosts[] = {
			10000,
			50000,
			100000
	};
	private void initSkins()
	{
		int x = fieldX + blockSize/2 - buttonBuyW/2;
		int y = fieldY + blockSize+ 50;
		
		// buttons buy
		for(int i = 0; i < Game.AvailableColors.length-1; i++)
		{
			boolean flag = (game.account.availableColors & (1 << i)) > 0;
			int id = Skins + i;
			
			GuiButton button;
			
			if(flag)
				button = new GuiButton(id, x + i*blockSize + i*blockGap, y, buttonBuyW, buttonBuyH, Strings.BOUGHT);
			else
				button = new GuiButton(id, x + i*blockSize + i*blockGap, y, buttonBuyW, buttonBuyH, Strings.BUY, " (" +Game.doCharSequence(Integer.toString(colorCosts[i]), '.', 3) +")");
			
			button.setEnabled(!flag);
			
			add(button, 0);
		}
	}
	
	private GuiLabel lStatsHP, lStatsDmg, lStatsDef;
	private final int statsHPCosts[] = {
			1000, 2000, 3000, 4000, 5000, 50000, 50000, 50000, 50000, 50000, 50000, 50000, 50000, 50000
	};
	private final int statsDmgCosts[] = {
			50, 50, 100, 100, 150, 175, 200, 250, 300, 400, 500
	};
	private final int statsDefCosts[] = {
			50, 50, 100, 100, 150, 175, 200, 250, 300, 400, 500
	};
	private void initSkills()
	{
		int x = 100;
		int y = 100;
		
		add((lStatsHP = new GuiLabel(x, y, 45, GuiLabel.ALIGNMENT_LEFT, "HP lvl " +game.account.hp)), 1);
		GuiButton button = new GuiButton(StatsHP, x + 230, y, 140, 45);
		updateButton(button, game.account.hp, statsHPCosts);
		add(button, 1);
		
		y += 75;
		add(lStatsDmg = new GuiLabel(x, y, 45, GuiLabel.ALIGNMENT_LEFT, "Dmg lvl " +game.account.dmg), 1);
		button = new GuiButton(StatsDmg, x + 230, y, 140, 45);
		updateButton(button, game.account.dmg, statsDmgCosts);
		add(button, 1);
		
		y += 75;
		add(lStatsDef = new GuiLabel(x, y, 45, GuiLabel.ALIGNMENT_LEFT, "Def lvl " +game.account.def), 1);
		button = new GuiButton(StatsDef, x + 230, y, 140, 45);
		updateButton(button, game.account.def, statsDefCosts);
		add(button, 1);
		
		// addBuyButton(new GuiButton..., int[] prices);
		// updateButton(button.id, priceState);
	}
	
	private void initUpgrades()
	{
		int w = 300;
		int h = 100;
		int x = Game.ScreenWidth/2 - w/2;
		int y = 100;
		
		add(new GuiButton(WeaponsID, x, y, w, h, Strings.WEAPONS), 2);
		add(new GuiButton(ItemsID, x, y + h + 20, w, h, Strings.ITEMS), 2);
		
		w = 240;
		h = 80;
		x += 30;
		y += 20;
		add(new GuiLabel(Game.ScreenWidth/2, 20, 40, GuiLabel.ALIGNMENT_MIDDLE, Strings.WEAPONS), 2, 0);
		add(new GuiButton(GunID, x, y, w, h, Strings.GUN), 2, 0);
		add(new GuiButton(RocketID, x, y + h + 20, w, h, Strings.ROCKET), 2, 0);
	}
	
	private void initEnchantments()
	{
		// just for not-crashing when opening this page
		add(new GuiLabel(0, 0, 20, 0, ""), 3);
	}
	
	private void updateButton(GuiButton button, int progress, int costs[])
	{
		if(progress >= costs.length)
		{
			button.setText(Strings.BOUGHT);
			button.setEnabled(false);
		}
		else
		{
			button.setText(Strings.BUY, " (" +Game.doCharSequence(costs[progress], '.', 3) +")");
			button.setEnabled(true);
		}
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		if(game.account == null)
		{
			game.openGui(null);
			return;
		}
		
		super.update(handler, events);
		
		if(handler.keys[KeyEvent.VK_ESCAPE])
		{
			handler.stopUpdate(KeyEvent.VK_ESCAPE);
			game.openGui(parentGui);
		}
	}
	
	@Override
	public void onButtonClicked(GuiButton button)
	{
		if(button.id == -1) // back
		{
			game.openGui(parentGui);
		}
		else if(button.id >= 0 && button.id < 4) // buttons at the bottom (categories)
		{
			setPage(button.id);
			setButtonsEnabled(true, 0, 1, 2, 3);
			setButtonsEnabled(false, button.id);
			
		}
		else if(button.id >= Skins && button.id < Skins + Game.AvailableColors.length - 1) // buy color
		{
			int i = button.id - Skins;
			if(game.getCoins() >= colorCosts[i])
			{
				game.account.availableColors |= 1 << i;
				game.addCoins(-colorCosts[i]);
				
				button.setText(Strings.BOUGHT);
				button.setEnabled(false);
				
				// save account after purchase
				game.saveAccount();
			}
		}
		else if(button.id >= Stats && button.id < Upgrades) // skills
		{
			switch(button.id)
			{
				case StatsHP: {
					if(game.account.hp < statsHPCosts.length && game.getCoins() >= statsHPCosts[game.account.hp])
					{
						game.addCoins(-statsHPCosts[game.account.hp]);
						game.account.hp++;
						
						lStatsHP.setText("HP lvl " +game.account.hp);
						updateButton(button, game.account.hp, statsHPCosts);
					}
					break;
				}
				
				case StatsDmg: {
					if(game.account.dmg < statsDmgCosts.length && game.getCoins() >= statsDmgCosts[game.account.dmg])
					{
						game.addCoins(-statsDmgCosts[game.account.dmg]);
						game.account.dmg++;
						
						lStatsDmg.setText("Dmg lvl " +game.account.dmg);
						updateButton(button, game.account.dmg, statsDmgCosts);
					}
					break;
				}
				
				case StatsDef: {
					if(game.account.def < statsDefCosts.length && game.getCoins() >= statsDefCosts[game.account.def])
					{
						game.addCoins(-statsDefCosts[game.account.def]);
						game.account.def++;
						
						lStatsDef.setText("Def lvl " +game.account.def);
						updateButton(button, game.account.def, statsDefCosts);
					}
					break;
				}
			}
		}
		else if(button.id >= Upgrades && button.id < Enchantments) // upgrades
		{
			switch(button.id - Upgrades)
			{
				case 0: {
					setPage(2, 0);
					break;
				}
			}
		}
		else if(button.id >= Enchantments) // entchantments
		{
		}
	}
	
	@Override
	public void onGuiClosed()
	{
		game.saveAccount();
	}
	
	@Override
	public void draw(Graphics g)
	{
		super.draw(g);
		
		// draw colors
		if(getPage() == 0)
		{
			for(int i = 0; i < Game.AvailableColors.length-1; i++)
			{
				g.setColor(Game.AvailableColors[i+1]);
				g.fillRect(fieldX + i*blockSize+ i*blockGap, fieldY, blockSize, blockSize);
			}
		}
		
		// coins
		int x = Game.ScreenWidth - 90;
		int y = 25;
		Texture.coin64.draw(g, x, y);
		g.setColor(Color.black);
		GuiLabel.draw(g, Game.doCharSequence(game.getCoins(), '.', 3), x - 6, y + 5, GuiLabel.ALIGNMENT_RIGHT, 64);
	}
	
	private final int blockSize = 64;
	private final int blockGap = 150;
	private final int buttonBuyW = 180;
	private final int buttonBuyH = 50;
	private final int fieldX = 200;
	private final int fieldY = 200;
	
	// ID's
	private final int Skins = 4;
	
	private final int Stats = Skins + 50;
	private final int StatsDef = Stats;
	private final int StatsDmg = Stats + 1;
	private final int StatsHP = Stats + 2;
	
	private final int Upgrades = Stats + 50;
	private final int WeaponsID = Upgrades;
	private final int ItemsID = Upgrades + 1;
	private final int GunID = Upgrades + 2;
	private final int RocketID = Upgrades + 3;
//	private final int GunDmgID = Upgrades + 4;
//	private final int GunHandleID = Upgrades + 5;
//	private final int RocketDmgID = Upgrades + 6;
//	private final int RocketHandleID = Upgrades + 7;
	
	private final int Enchantments = Upgrades + 50;
	
//	private final int costsLive[] = {
//			1000,
//			2000,
//			3000,
//			4000,
//			50000,
//			50000,
//			50000,
//			50000,
//			50000,
//	};
}