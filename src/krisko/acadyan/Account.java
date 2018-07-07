package krisko.acadyan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Account
{
	public Account(String accountName)
	{
		name = accountName;
		availableColors = 0;
		inventory = new Inventory(Game.InventoryWidth, Game.InventoryHeight);
		level = new int[Game.LevelAmount.length];
		level[0] = 1; // unlock the first level
	}
	
	public void reset()
	{
		playerCoins = 0;
		availableColors = 0;
		inventory.clear();
		
		level[0] = 1;
		for(int i = 1; i < level.length; i++)
			level[i] = 0;
	}
	
	public void save()
	{
		final String fileName = "+" +name +".acc";
		final String pathFolder = Acadyan.FolderPath +Acadyan.FileSeperator +"Accounts";
		final String pathFile = pathFolder +Acadyan.FileSeperator +fileName;
		
		File file = new File(pathFolder);
		if(!file.exists())
		{
			if(!file.mkdirs())
			{
				// something went wrong
				// TODO:
				return;
			}
		}
		
		file = new File(pathFile);
		if(!file.exists())
		{
			try
			{
				if(!file.createNewFile())
				{
					// something went wrong
					return;
				}
			} catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
		try
		{
			/* Account Structure:
			 * coins
			 * colors (0/1) [not the first {black} one]
			 * hp
			 * dmg
			 * def
			 */
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			// coins
			bw.write("coin:" +Long.toString(playerCoins) +Acadyan.LineSeperator);
			
			// colors
			bw.write("color:" +Integer.toString(availableColors) +Acadyan.LineSeperator);
			
			// hp, dmg, def
			bw.write("hp:" +Integer.toString(hp) +Acadyan.LineSeperator);
			bw.write("dmg:" +Integer.toString(dmg) +Acadyan.LineSeperator);
			bw.write("def:" +Integer.toString(def) +Acadyan.LineSeperator);
			
			// level  "level:<world>|<unlocked_levels>"
			for(int i = 0; i < level.length; i++)
				bw.write("level:" +Integer.toString(i) +"|" +Integer.toString(level[i]) +Acadyan.LineSeperator);
			
			bw.close();
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public boolean load()
	{
		final String fileName = "+" +name +".acc";
		final String pathFile = Acadyan.FolderPath +Acadyan.FileSeperator +"Accounts" +Acadyan.FileSeperator +fileName;
		
		File file = new File(pathFile);
		if(!file.exists())
			return false;
		
		try
		{
			/* Account Structure:
			 * coins
			 * colors (0/1) [not the first {black} one]
			 * hp
			 * dmg
			 * def
			 */
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String s;
			while((s = br.readLine()) != null)
			{
				int index = s.indexOf(':');
				if(index == -1)
					continue;
				
				String syn = s.substring(0, index);
				s = s.substring(index+1);
				
				try
				{
					if(syn.equals("coin"))
						playerCoins = Long.parseLong(s);
					else if(syn.equals("color"))
						availableColors = Integer.parseInt(s);
					else if(syn.equals("hp"))
						hp = MathHelper.max(0, Integer.parseInt(s));
					else if(syn.equals("dmg"))
						dmg = MathHelper.max(0, Integer.parseInt(s));
					else if(syn.equals("def"))
						def = MathHelper.max(0, Integer.parseInt(s));
					else if(syn.equals("level"))
					{
						int i = s.indexOf('|');
						int world = Integer.parseInt(s.substring(0, i));
						int amount = Integer.parseInt(s.substring(i+1));
						level[world] = amount;
					}
				} catch(Exception ex)
				{ }
			}
			
			br.close();
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
		// unlock the first level
		if(level[0] < 1)
			level[0] = 1;
		
		return true;
	}
	
	public String getName() { return name; }
	
	private final String name;
	
	public long playerCoins;
	public int availableColors; // available EXTRA colors (black one not included)
	
	public int hp;
	public int dmg;
	public int def;
	
	public int level[];
	
	public Inventory inventory;
}