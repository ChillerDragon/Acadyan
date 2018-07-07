package krisko.acadyan.gui;

import java.awt.Color;
import java.awt.Graphics;

import krisko.acadyan.Texture;

public class GuiLevelButton extends GuiButton
{
	public GuiLevelButton(int id, int x, int y, int w, int h, int levelNumber, int stars, int goldblocks, int money, int maxMoney)
	{
		super(id, x, y, w, h);
		
		this.levelNumber = levelNumber;
		this.stars = stars;
		this.goldblocks = goldblocks;
		this.money = money;
		this.maxMoney = maxMoney;
	}
	
	@Override
	protected void drawForeground(Graphics g)
	{
		// draw text
		int textSize = (int)(getHeight()*0.35);
		g.setColor(isEnabled() ? Color.white : Color.lightGray);
		GuiLabel.draw(g, "Level " +levelNumber, getX() + 15, getY() + 15, GuiLabel.ALIGNMENT_LEFT, textSize);
		
		// draw stars
		int starSize = (int)(getHeight()*0.28);
		for(int i = 0; i < 3; i++)
			(stars > i ? Texture.star : Texture.starBG).draw(g, getX() + 15 + i*starSize + i*2, getY() + getHeight() - 15 - starSize, starSize, starSize);
		
		// draw goldblock
		int goldSize = (int)(getHeight() * 0.2);
		for(int i = 0; i < 3; i++)
			(goldblocks >= 3-i ? Texture.gold : Texture.goldGray).draw(g, getX() + getWidth() - 15 - goldSize - i*goldSize - i*5, getY() + getHeight() - 15 - goldSize, goldSize, goldSize);
		
		// draw money
		textSize = (int)(getHeight() * 0.15);
		g.setColor(new Color(248, 241, 28));
		GuiLabel.draw(g, String.format("%d/%d", money, maxMoney), getX() + getWidth() - 12, getY() + 25, GuiLabel.ALIGNMENT_RIGHT, textSize);
	}
	
	private final int levelNumber;
	private final int stars;
	private final int goldblocks;
	private final int money;
	private final int maxMoney;
}