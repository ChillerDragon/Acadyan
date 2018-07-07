package krisko.acadyan.gui;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import krisko.acadyan.Animation;
import krisko.acadyan.Game;
import krisko.acadyan.InputHandler;
import krisko.acadyan.KEvent;
import krisko.acadyan.Strings;

public class GuiLevelSolved extends Gui
{
	public GuiLevelSolved(Game game, Gui openGui)
	{
		super(game);
		this.openGui = openGui;
		startTime = Game.tick();
		init();
	}
	
	private void init()
	{
		add(new GuiLabel(Game.ScreenWidth/2, Game.ScreenHeight/2 - 80, 60, GuiLabel.ALIGNMENT_MIDDLE, Strings.TEXT_LVL_COMPLETE, "!"));
		
		lContinue = new GuiLabel(Game.ScreenWidth/2, Game.ScreenHeight/2 + 20, 25, GuiLabel.ALIGNMENT_MIDDLE, Strings.TEXT_PRESS_SPACE_TO_CONTINUE);
		lContinue.setVisible(false);
		lContinue.setTextSize(0);
		add(lContinue);
	}
	
	@Override
	public void update(InputHandler handler, KEvent events[])
	{
		super.update(handler, events);
		
		// set size of continue label
//		int size = 25;
//		int add = Game.tick() / 10 % 16;
//		if(add >= 8)
//			add = 15 - add;
//		size += add;
//		
//		lContinue.setTextSize(size);
		
		// add confetti
		if(confettiList.size() < amount)
		{
			double x = Game.ScreenWidth/2.0D;
			double y = Game.ScreenHeight/2.0D - 100;
			
			double force = 2.75D + random.nextDouble()/2;
			double r = (Math.PI * 4 * confettiList.size() / (double)amount) - Math.PI;
			
			double mX = Math.cos(r) * force;
			double mY = Math.sin(r) * force;
			
			if((confettiList.size() / (amount/4)) % 2 == 1)
				mY = -mY;
			
			confettiList.add(new Confetti(x, y, mX, mY, confettiAnim[random.nextInt(confettiAnim.length)]));
		}
		
		// update confetti
		for(int i = 0; i < confettiList.size(); i++)
			confettiList.get(i).update();
		
		// try to go to menu
		if(Game.tick() - startTime < Game.TicksPerSec)
		{
			handler.stopUpdate(KeyEvent.VK_SPACE);
		}
		else
		{
			if(!lContinue.isVisible())
				lContinue.setVisible(true);
			if(lContinue.getTextSize() < 25 &&  Game.tick() % 2 == 0)
				lContinue.setTextSize(lContinue.getTextSize() + 1);
			
			if(handler.keys[KeyEvent.VK_SPACE])
			{
				handler.stopUpdate(KeyEvent.VK_SPACE);
				game.reset();
//				game.openGui(new GuiMenu(game));
				game.openGui(openGui);
			}
		}
	}
	
	@Override
	public void draw(Graphics g)
	{
		super.draw(g);
		
		for(int i = 0; i < confettiList.size(); i++)
			confettiList.get(i).draw(g);
	}
	
	int amount = 400;
	private Random random = new Random();
	private Animation confettiAnim[] = {
			Animation.confettiBlue,
			Animation.confettiGreen,
			Animation.confettiMagenta,
			Animation.confettiOrange,
			Animation.confettiRed,
	};
	private ArrayList<Confetti> confettiList = new ArrayList<Confetti>();
	
	private GuiLabel lContinue;
	private int startTime;
	
	// Confetti class
	private class Confetti
	{
		public Confetti(double x, double y, double mX, double mY, Animation anim)
		{
			posX = x;
			posY = y;
			motionX = mX;
			motionY = mY;
			animation = anim;
			Random random = new Random();
			timeStart = random.nextInt(120);
		}
		
		public void update()
		{
			motionX *= 0.995D;
			
			motionY += gravity;
			if(motionY < 0.0D)
				motionY += airFriction;
			
			if(motionY >= 0.8D)
				motionY = 0.8D;
			
			posX += motionX;
			posY += motionY;
		}
		
		public void draw(Graphics g)
		{
			int size = 8;
			int x = (int)posX;
			int y = (int)posY;
			
			animation.draw(g, x, y, size, size, 0.25D, timeStart);
		}
		
		private double posX, posY;
		private double motionX, motionY;
		
		private double gravity = 0.004D;
		private double airFriction = 0.015D;
		
		private Animation animation;
		private int timeStart;
	}
	
	private Gui openGui;
}