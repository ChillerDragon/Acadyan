package krisko.acadyan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Options
{
// Language
	public static void setLanguage(int lang) { language = lang; }
	
	public static int getLanguage() { return language; }
	
	public static boolean english() { return language == ENGLISH; }
	
	public static boolean german() { return language == GERMAN; }
	
	public static final int ENGLISH = 0;
	public static final int GERMAN = 1;
	private static int language = ENGLISH;
	
// Mouse Settings
	public static float mouseSensitivity = 1.f;
	
// Account
	public static String lastLoggedIn;
	
// Volume
	public static float volumeBgm = 0.25f;
	public static float volumeEffects = 0.5f;
	
// Stuff
	public static boolean blood = true;
	public static int cursor = 0;
	public static float cursorScale = 1.f;
	
// Multiplayer Stuff
	public static int hidePlayers = 0;
	
	
	
	
	
	public static boolean playerCollide = true;
	
	
	
//
	public static void save()
	{
		final String pathFile = Acadyan.FolderPath +Acadyan.FileSeperator +"Acadyan.cfg";
		
		File file = new File(Acadyan.FolderPath);
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
			 * lastLoggedIn
			 * Language
			 * bgm volume
			 * effects volume
			 * blood
			 * ~ controls ~
			 */
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			// lastLoggedIn
			bw.write("login:" +lastLoggedIn +Acadyan.LineSeperator);
			
			// language
			bw.write("lang:" +Integer.toString(language) +Acadyan.LineSeperator);
			
			// mouse sensitivity
			bw.write("mousespeed:" +Float.toString(mouseSensitivity) +Acadyan.LineSeperator);
			
			// volume bgm, effects
			bw.write("bgm:" +Float.toString(volumeBgm) +Acadyan.LineSeperator);
			bw.write("sfx:" +Float.toString(volumeEffects) +Acadyan.LineSeperator);
			
			// blood
			bw.write("blood:" +(blood ? "1" : "0") +Acadyan.LineSeperator);
			
			// cursor
			bw.write("cursor:" +Integer.toString(cursor) +Acadyan.LineSeperator);
			bw.write("cursor_scale:" +Float.toString(cursorScale) +Acadyan.LineSeperator);
			
			// controls
			for(int i = 0; i < Controls.list.size(); i++)
			{
				Controls c = Controls.list.get(i);
				bw.write(c.synonym +":" +Integer.toString(c.key) +Acadyan.LineSeperator);
			}
			
			bw.close();
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void load()
	{
		final String pathFile = Acadyan.FolderPath +Acadyan.FileSeperator +"Acadyan.cfg";
		
		File file = new File(pathFile);
		if(!file.exists())
			return;
		
		try
		{
			/* Structure:
			 * LastLoggedIn
			 * Language
			 * bgm volume
			 * effects volume
			 * blood
			 * ~ controls ~
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
					if(syn.equals("login"))
						lastLoggedIn = s;
					else if(syn.equals("lang"))
						language = Integer.parseInt(s);
					else if(syn.equals("mousespeed"))
						mouseSensitivity = Float.parseFloat(s);
					else if(syn.equals("bgm"))
						volumeBgm = Float.parseFloat(s);
					else if(syn.equals("sfx"))
						volumeEffects = Float.parseFloat(s);
					else if(syn.equals("blood"))
						blood = s.equals("1");
					else if(syn.equals("cursor"))
						cursor = Integer.parseInt(s);
					else if(syn.equals("cursor_scale"))
						cursorScale = Float.parseFloat(s);
					else
					{
						for(int i = 0; i < Controls.list.size(); i++)
						{
							Controls c = Controls.list.get(i);
							if(syn.equals(c.synonym))
								c.key = Integer.parseInt(s);
						}
					}
				} catch(Exception ex)
				{ }
			}
			
			br.close();
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}