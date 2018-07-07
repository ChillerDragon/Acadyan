package krisko.acadyan;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.nio.charset.Charset;
import java.util.ArrayList;

import krisko.acadyan.gui.Gui;
import krisko.acadyan.gui.GuiAccount;
import krisko.acadyan.gui.GuiInventory;
import krisko.acadyan.gui.GuiLabel;
import krisko.acadyan.gui.GuiLevelSelect;
import krisko.acadyan.gui.GuiLevelSelectMP;
import krisko.acadyan.gui.GuiLevelSolved;
import krisko.acadyan.gui.GuiMenu;
import krisko.socketconnection.UDPClient;
import krisko.socketconnection.UDPClientListener;
import krisko.socketconnection.UDPServer;
import krisko.socketconnection.UDPServerListener;

public class Game implements UDPServerListener, UDPClientListener
{
	public Game()
	{
		openGui(new GuiAccount(this));
	}
	
	public void reset()
	{
		if(client != null)
		{
			client.disconnect();
			client = null;
		}
		if(server != null)
		{
			server.close();
			server = null;
		}
		
		clientMessages.clear();
		serverMessages.clear();
		
		localID = 0;
		
		gameState = STATE_SINGLEPLAYER;
		
		mapWorld = 0;
		mapLevel = 0;
		mapName = "";
		
		world = null;
		camera = null;
		
		openWorld = false;
		testMode = false;
		
		System.gc();
	}
	
	/**
	 * Tries to open a server and sets the state to STATE_MULTIPLAYER_SERVER
	 * @param mapName
	 * @return
	 */
//	public boolean openServer(String mapName)
	public boolean openServer(int world, int level)
	{
		if(server != null)
		{
			openMap(world, level);
			return false;
		}
		
		// set game state
		gameState = STATE_MULTIPLAYER_SERVER;
		
		// open server
		server = new UDPServer(this);
		if(server.open(Port, MaxClients))
		{
			// open map
			openMap(world, level);
			return true;
		}
		
		server = null;
		reset();
		return true;
	}
	
	/**
	 * Joins a server and sets the state to STATE_MULTIPLAYER_CLIENT
	 * @param serverIP
	 */
	/**
	 * Tries to join a server and sets the state to STATE_MULTIPLAYER_CLIENT
	 * @param serverIP
	 * @return
	 */
	public boolean joinServer(String serverIP)
	{
		if(client != null)
			return false;
		
		// set game state
		gameState = STATE_MULTIPLAYER_CLIENT;
		
		// connect to server
		client = new UDPClient(this);
		if(client.connect(serverIP, Port))
		{
			// open map
//			openMap(1, 1);
			
			// send stuff
			client.send("name:" +getAccountName());
			return true;
		}
		
		client = null;
		reset();
		return false;
	}
	
	/** Opens an intern world */
	public boolean openMap(int world, int level)
	{
		mapWorld = world;
		mapLevel = level;
		mapName = String.format("%d_%d", mapWorld, mapLevel);
		openWorld = false;
		testMode = false;
		
		this.world = new GameWorld(this);
		
		if(!this.world.loadIntern(mapName))
		{
			reset();
			return false;
		}
		
		this.world.createPlayerEntity(localID);
		this.world.getLocalPlayer().name = getAccountName();
		
		camera = new Camera(this.world);
		
		return true;
	}
	
	public void onLevelComplete(int time)
	{
		Gui gui = new GuiMenu(this);
		
		if(testMode)
		{
			gui = new Editor(this, mapName);
		}
		else if(!openWorld)
		{
			try
			{
				if(account.level[mapWorld-1] == mapLevel)
				{
					if(mapLevel == LevelAmount[mapWorld-1])
					{
						if(mapWorld-1 < LevelAmount.length-1)
						{
							if(account.level[mapWorld] == 0)
								account.level[mapWorld] = 1;
						}
					}
					else
					{
						account.level[mapWorld-1]++;
					}
					
					saveAccount();
				}
				
				if(gameState == STATE_MULTIPLAYER_SERVER)
					gui = new GuiLevelSelectMP(this, mapWorld);
				else
					gui = new GuiLevelSelect(this, mapWorld);
			} catch(Exception ex)
			{ }
		}
		
		openGui(new GuiLevelSolved(this, gui));
	}
	
	/** Opens an extern world */
	public boolean openWorld(String name, boolean isOpenWorld, boolean isTest)
	{
		mapWorld = 0;
		mapLevel = 0;
		mapName = name;
		openWorld = isOpenWorld;
		testMode = isTest;
		
		world = new GameWorld(this);
		
		if(!world.loadExtern(mapName))
		{
			if(isOpenWorld)
				world.generateStart(2);
			else
				return false;
		}
		world.createPlayerEntity(localID);
		world.getLocalPlayer().name = getAccountName();
		
		camera = new Camera(world);
		return true;
	}
	
	private boolean canUseTeleport = false;
	
	public void update(InputHandler handler, KEvent events[])
	{
		gameTick++;
		
		if(handler.keys[KeyEvent.VK_CONTROL] && handler.keys[KeyEvent.VK_C] && handler.keys[KeyEvent.VK_P])
		{
			handler.stopUpdate(KeyEvent.VK_C);
			handler.stopUpdate(KeyEvent.VK_P);
			
			Options.playerCollide = !Options.playerCollide;
			
			if(gameState == STATE_MULTIPLAYER_CLIENT)
				client.send("option_collide");
			else if(gameState == STATE_MULTIPLAYER_SERVER)
				server.send("option_collide:" +(Options.playerCollide ? "1" : "0"));
		}
		
		if(handler.keys[KeyEvent.VK_CONTROL] && handler.keys[KeyEvent.VK_SHIFT] && handler.keys[KeyEvent.VK_E])
		{
			handler.stopUpdate(KeyEvent.VK_E);
			canUseTeleport = !canUseTeleport;
		}
		
		if(canUseTeleport && handler.keys[KeyEvent.VK_R])
		{
			handler.stopUpdate(KeyEvent.VK_R);
			
			if(world != null && world.getLocalPlayer() != null && world.getLocalPlayer().entity != null)
			{
				EntityPlayer entity = world.getLocalPlayer().entity;
				double dX = (handler.mouseX - ScreenWidth/2) / (double)BlockSize;
				double dY = (handler.mouseY - ScreenHeight/2) / (double)BlockSize;
				entity.pos.add(new Vector2D(dX, dY));
			}
		}
		
		if(gameState != STATE_SINGLEPLAYER)
		{
			updateMultiplayer(handler, events);
			return;
		}
		
		if(currentGui != null)
		{
			currentGui.update(handler, events);
		}
		else
		{
			// change color
			if(Controls.color.isPressed2(handler))
			{
				boolean flag = false; // if the color could be changed
				
				for(int i = world.getLocalPlayer().color+1; i < AvailableColors.length; i++)
				{
					if((account.availableColors & (1 << (i-1))) > 0)
					{
						world.getLocalPlayer().color = i;
						flag = true;
						break;
					}
				}
				
				if(!flag)
					world.getLocalPlayer().color = 0;
			}
			
			// use items
			if(getPlayerInventory() != null)
			{
				for(int i = 0; i < 10; i++)
				{
					if(handler.keys[KeyEvent.VK_0 + i])
					{
						handler.stopUpdate(KeyEvent.VK_0 + i);
						
						int j = i - 1;
						if(j < 0)
							j = 9;
						
						getPlayerInventory().useHotbarItem(j, world.getLocalPlayer().entity);
					}
				}
			}
			
			// update world
			world.update(handler, camera.x, camera.y);
			
			// set camera position
			camera.update();
			
			if(!world.getLocalPlayer().entity.dead)
			{
				// try to generate new chunk
				if(!testMode && openWorld) // not in test-mode or non-open-world
				{
					int left = camera.x / Chunk.WIDTH - 1;
					int right = (camera.x + ScreenWidth) / Chunk.WIDTH;
					world.generateNew(world.getMapIndexFromBlockX(left - 2), world.getMapIndexFromBlockX(right + 2));
				}
			
				// try to open inventory
				if(Controls.inventory.isPressed2(handler))
				{
					openGui(new GuiInventory(this, getPlayerInventory()));
				}
			}
			
			// try to open menu && save when opening menu
			if(handler.keys[KeyEvent.VK_ESCAPE])
			{
				handler.stopUpdate(KeyEvent.VK_ESCAPE);
				
				if(testMode)
				{
					openGui(new Editor(this, mapName));
				}
				else
				{
					if(openWorld)
						openGui(new GuiMenu(this));
					else
						openGui(new GuiLevelSelect(this, mapWorld));
					reset();
					saveAccount();
				}
			}
		}
	}
	
	private void handleMessages()
	{
		if(gameState == STATE_MULTIPLAYER_CLIENT)
		{
			String[] messages = getNextClientMessages();
			if(messages != null)
			{
				// loop though server messages
				for(int i = 0; i < messages.length; i++)
				{
					String msg = messages[i];
					String s = msg.toString();
					
					if(s.equals("-1"))
					{
						// server closed
						openGui(new GuiMenu(this));
						reset();
						saveAccount();
						return;
					}
					
					// split string into "command" and "value"
					int index = s.indexOf(":");
					if(index == -1)
						doClientCommand(s, "");
					else
						doClientCommand(s.substring(0, index), s.substring(index+1));
				}
			}
		}
		else
		{
			ServerMessage[] messages = getNextServerMessages();
			if(messages != null)
			{
				// loop though client messages
				for(int i = 0; i < messages.length; i++)
				{
					ServerMessage msg = messages[i];
					
					// Because if you're the server, your id is 0, so the other clients begin at 1...
					int playerID = msg.clientID + 1;
					
					String s = msg.str;
					
					if(s.equals("-1"))
					{
						// client disconnected
						kickPlayer(playerID);
					}
					else if(s.equals("0"))
					{
						// client joined
						// send map info
						server.sendTo("map:" +mapName, msg.clientID);
						
						// send client id to client
						server.sendTo("id:" +playerID, msg.clientID);
						
						// send client all players
						for(int j = 0; j < MaxPlayer; j++)
						{
							if(world.players[j] == null)
								continue;
							
							server.sendTo("player_ce:" +world.players[j].id, msg.clientID);
							server.sendTo("name:" +world.players[j].id +"|" +world.players[j].name, msg.clientID);
							
							if(world.players[j].color != 0)
								server.sendTo("color:" +world.players[j].id +"|" +world.players[j].color, msg.clientID);
							
							if(world.players[j].entity != null)
							{
								server.sendTo("x:" +world.players[j].id +"|" +world.players[j].entity.pos.x, msg.clientID);
								server.sendTo("y:" +world.players[j].id +"|" +world.players[j].entity.pos.y, msg.clientID);
							}
						}
						
						server.sendTo("option_collide:" +(Options.playerCollide ? "1" : "0"), msg.clientID);
						
						// create player
						world.createPlayerEntity(playerID);
						
						// send all clients that someone joined
						server.send("player_ce:" +playerID);
					}
					else
					{
						// split string into "command" and "value"
						int index = s.indexOf(":");
						if(index == -1)
							doServerCommand(playerID, s, "");
						else
							doServerCommand(playerID, s.substring(0, index), s.substring(index+1));
					}
				}
			}
		}
	}
	
	private void updateMultiplayer(InputHandler handler, KEvent events[])
	{
		// handle client / server messages
		handleMessages();
		
		if(currentGui != null)
		{
			// update world while ingame gui is open
			if(currentGui.isIngameGui() && world != null)
			{
				// update world
				world.update(new InputHandler(), camera.x, camera.y);
				
				// set camera position
				camera.update();
				
				if(!world.getLocalPlayer().entity.dead)
				{
					// try to generate new chunk
					if(!testMode && openWorld) // not in test-mode or non-open-world
					{
						int left = camera.x / Chunk.WIDTH - 1;
						int right = (camera.x + ScreenWidth) / Chunk.WIDTH;
						world.generateNew(world.getMapIndexFromBlockX(left - 2), world.getMapIndexFromBlockX(right + 2));
					}
				}
			}
			
			// update gui
			currentGui.update(handler, events);
		}
		else
		{
			if(world != null)
			{
				// change color
				if(Controls.color.isPressed2(handler))
				{
					int colorBefore = world.getLocalPlayer().color;
					boolean flag = false; // if the color could be changed
					
					// try changing to next color
					for(int i = world.getLocalPlayer().color+1; i < AvailableColors.length; i++)
					{
						if((account.availableColors & (1 << (i-1))) > 0)
						{
							world.getLocalPlayer().color = i;
							flag = true;
							break;
						}
					}
					
					// change to standard (black)
					if(!flag)
						world.getLocalPlayer().color = 0;
					
					// if the color was changed, send it
					if(colorBefore != world.getLocalPlayer().color)
					{
						if(gameState == STATE_MULTIPLAYER_CLIENT)
							client.send("color:" +world.getLocalPlayer().color);
						if(gameState == STATE_MULTIPLAYER_SERVER)
							server.send(String.format("color:%d|%d", localID, world.getLocalPlayer().color));
					}
				}
				
				// use items
				if(getPlayerInventory() != null)
				{
					for(int i = 0; i < 10; i++)
					{
						if(handler.keys[KeyEvent.VK_0 + i])
						{
							handler.stopUpdate(KeyEvent.VK_0 + i);
							
							int j = i - 1;
							if(j < 0)
								j = 9;
							
							getPlayerInventory().useHotbarItem(j, world.getLocalPlayer().entity);
						}
					}
				}
				
				// hide players
				if(Controls.hide.isPressed2(handler))
				{
					Options.hidePlayers++;
					if(Options.hidePlayers >= 3)
						Options.hidePlayers = 0;
				}
				
				// update world
				world.update(handler, camera.x, camera.y);
				
				// set camera position
				camera.update();
				
				if(!world.getLocalPlayer().entity.dead)
				{
					// try to generate new chunk
					if(!testMode && openWorld) // not in test-mode or non-open-world
					{
						int left = camera.x / Chunk.WIDTH - 1;
						int right = (camera.x + ScreenWidth) / Chunk.WIDTH;
						world.generateNew(world.getMapIndexFromBlockX(left - 2), world.getMapIndexFromBlockX(right + 2));
					}
				
					// try to open inventory
					if(Controls.inventory.isPressed2(handler))
					{
						openGui(new GuiInventory(this, getPlayerInventory()));
					}
				}
			}
			
			// try to open player list
			renderPlayerList = handler.keys[KeyEvent.VK_TAB];
			
			// try to open menu && save when opening menu
			if(handler.keys[KeyEvent.VK_ESCAPE])
			{
				handler.stopUpdate(KeyEvent.VK_ESCAPE);
				
				// send the disconnect message
				if(gameState == STATE_MULTIPLAYER_CLIENT)
					client.sendDirectly("-1");
				else if(gameState == STATE_MULTIPLAYER_SERVER)
					server.sendDirectly("-1");

				reset();
				openGui(new GuiMenu(this));
				saveAccount();
				return;
			}
		}
		
		// send messages
		if(world != null)
		{
			if(gameState == STATE_MULTIPLAYER_CLIENT)
			{
				// client
				if(world.getLocalPlayer() != null)
				{
					Player player = world.getLocalPlayer();
					
					// position / motion
//					if(player.entity != null)
//					{
//						client.send("x:" +player.entity.pos.x);
//						client.send("y:" +player.entity.pos.y);
//						client.send("dx:" +player.entity.dir.x);
//						client.send("dy:" +player.entity.dir.y);
//					}
					
					// mouse
					if(player.mouseX != player.lastMouseX)
						client.send("mx:" +player.mouseX);
					if(player.mouseY != player.lastMouseY)
						client.send("my:" +player.mouseY);
					if(player.leftDown != player.lastLeftDown)
						client.send("ml:" +(player.leftDown ? "1" : "0"));
					
					// keys
					if(player.left != player.lastLeft)
						client.send("left:" +(player.left ? "1" : "0"));
					if(player.right != player.lastRight)
						client.send("right:" +(player.right ? "1" : "0"));
					if(player.jump != player.lastJump)
						client.send("jump:" +(player.jump ? "1" : "0"));
					if(player.sprint != player.lastSprint)
						client.send("sprint:" +(player.sprint ? "1" : "0"));
					if(player.sneak != player.lastSneak)
						client.send("sneak:" +(player.sneak ? "1" : "0"));
				}
			}
			else
			{
				// server
				if(world.getLocalPlayer() != null)
				{
					Player player = world.getLocalPlayer();
					
					// position / motion
					if(tick() % TicksPerSec == 0 && player.entity != null)
					{
						server.send("x:" +localID +"|" +player.entity.pos.x);
						server.send("y:" +localID +"|" +player.entity.pos.y);
						server.send("dx:" +localID +"|" +player.entity.dir.x);
						server.send("dy:" +localID +"|" +player.entity.dir.y);
					}
					
					// mouse
					if(player.mouseX != player.lastMouseX)
						server.send("mx:" +localID +"|" +player.mouseX);
					if(player.mouseY != player.lastMouseY)
						server.send("my:" +localID +"|" +player.mouseY);
					if(player.leftDown != player.lastLeftDown)
						server.send("ml:" +localID +"|" +(player.leftDown ? "1" : "0"));
					
					// keys
					if(player.left != player.lastLeft)
						server.send("left:" +localID +"|" +(player.left ? "1" : "0"));
					if(player.right != player.lastRight)
						server.send("right:" +localID +"|" +(player.right ? "1" : "0"));
					if(player.jump != player.lastJump)
						server.send("jump:" +localID +"|" +(player.jump ? "1" : "0"));
					if(player.sprint != player.lastSprint)
						server.send("sprint:" +localID +"|" +(player.sprint ? "1" : "0"));
					if(player.sneak != player.lastSneak)
						server.send("sneak:" +localID +"|" +(player.sneak ? "1" : "0"));
				}
				
				if(tick() % TicksPerSec == 0)
				{
					long time = System.currentTimeMillis(); // this is for checking the ping of a player
					
					for(int i = 0; i < MaxPlayer; i++)
					{
						Player player = world.players[i];
						
						if(player == null || player.id == localID || player.entity == null || player.entity.dead)
							continue;
						
	//						server.sendExcept("x:" +world.players[i].id +"|" +world.players[i].entity.pos.x, i-1);
	//						server.sendExcept("y:" +world.players[i].id +"|" +world.players[i].entity.pos.y, i-1);
	//						server.sendExcept("dx:" +world.players[i].id +"|" +world.players[i].entity.dir.x, i-1);
	//						server.sendExcept("dy:" +world.players[i].id +"|" +world.players[i].entity.dir.y, i-1);
						
						// ask for ping
						if(player.timePingSent == -1)
						{
							player.timePingSent = time;
							server.sendTo(new byte[0], i-1);
						}
						else
						{
							player.ping = (int)MathHelper.clamp(time - player.timePingSent, 0, 999);
							
							if(time - player.timePingSent > TIME_NOT_ACTING)
							{
								kickPlayer(player.id);
								continue;
							}
						}
						
						// send player ping
						server.send("ping:" +player.id +"|" +player.ping);
						
						server.send("x:" +player.id +"|" +player.entity.pos.x);
						server.send("y:" +player.id +"|" +player.entity.pos.y);
						server.send("dx:" +player.id +"|" +player.entity.dir.x);
						server.send("dy:" +player.id +"|" +player.entity.dir.y);
					}
				}
			}
		}
	}
	
	public void kickPlayer(int playerID)
	{
		// remove player from world
		world.removePlayer(playerID);
		
		// send all clients that someone left
		server.send("player_r:" +playerID);
	}
	
	private synchronized void addClientMessage(String str)
	{
		clientMessages.add(str);
	}
	
	private synchronized void addServerMessage(int clientID, String str)
	{
		serverMessages.add(new ServerMessage(clientID, str));
	}
	
	private synchronized String[] getNextClientMessages()
	{
		if(clientMessages.size() == 0)
			return null;
		
		String[] messages = new String[clientMessages.size()];
		for(int i = 0; i < messages.length; i++)
			messages[i] = clientMessages.remove(0);
		
		return messages;
	}
	
	private synchronized ServerMessage[] getNextServerMessages()
	{
		if(serverMessages.size() == 0)
			return null;
		
		ServerMessage[] messages = new ServerMessage[serverMessages.size()];
		for(int i = 0; i < messages.length; i++)
			messages[i] = serverMessages.remove(0);
		
		return messages;
	}
	
// Multiplayer (Client)
	@Override
	public void onMessageReceived(byte[] data, int received)
	{
		if(received == 0) // respond to server ping question
		{
			client.send(new byte[0]);
			return;
		}
		
		byte[] newData = new byte[received];
		for(int i = 0; i < received; i++)
			newData[i] = data[i];
		
		addClientMessage(byteToString(newData));
	}
	
	@Override
	public void onDisconnect()
	{
		addClientMessage("-1");
	}
	
// Multiplayer (Server)
	@Override
	public void onClientJoined(int clientID, byte[] data, int received)
	{
		addServerMessage(clientID, "0");
		
		if(received > 0)
		{
			byte[] newData = new byte[received];
			for(int i = 0; i < received; i++)
				newData[i] = data[i];
			
			addServerMessage(clientID, byteToString(newData));
		}
	}
	
	@Override
	public void onClientDisconnect(int clientID)
	{
		addServerMessage(clientID, "-1");
	}
	
	@Override
	public void onMessageReceived(int clientID, byte[] data, int received)
	{
		if(received == 0) // got response from ping question
		{
			Player player = world.players[clientID+1];
			
			if(player != null)
			{
				long time = System.currentTimeMillis();
				player.ping = (int)MathHelper.clamp(time - player.timePingSent, 0, 999);
				player.timePingSent = -1;
			}
			
			return;
		}
		
		byte[] newData = new byte[received];
		for(int i = 0; i < received; i++)
			newData[i] = data[i];
		
		addServerMessage(clientID, byteToString(newData));
	}
	
	/** Server receives a command from client
	 * commands are:
	 * x, y (set position)
	 */
	private void doServerCommand(int playerID, String cmd, String value)
	{
		// create player if it doesnt already exist
		if(world.players[playerID] == null)
			world.createPlayer(playerID);
		
		try
		{
			boolean send = false;
			
			switch(cmd)
			{
				case "option_collide": {
					Options.playerCollide = !Options.playerCollide;
					server.sendExcept("option_collide:" +(Options.playerCollide ? "1" : "0"), playerID-1);
					break;
				}
				
				case "ping": {
					long timeNow = System.currentTimeMillis();
					long timeSent = Long.parseLong(value);
					
					world.players[playerID].ping = (int)MathHelper.clamp(timeNow - timeSent, 0, 999);
					break;
				}
				
				case "name": {
					world.players[playerID].name = value;
					server.sendExcept(String.format("name:%d|%s", playerID, value), playerID-1);
					break;
				}
				
				case "color": {
					world.players[playerID].color = Integer.parseInt(value);
					server.sendExcept(String.format("color:%d|%d", playerID, world.players[playerID].color), playerID-1);
					break;
				}
				
				case "x": {
					if(world.players[playerID].entity == null || world.players[playerID].entity.dead)
						world.createPlayerEntity(playerID);
					world.players[playerID].entity.pos.x = Double.parseDouble(value);
					break;
				}
				
				case "y": {
					if(world.players[playerID].entity == null || world.players[playerID].entity.dead)
						world.createPlayerEntity(playerID);
					world.players[playerID].entity.pos.y = Double.parseDouble(value);
					break;
				}
				
				case "dx": {
					if(world.players[playerID].entity == null || world.players[playerID].entity.dead)
						world.createPlayerEntity(playerID);
					world.players[playerID].entity.dir.x = Double.parseDouble(value);
					break;
				}
				
				case "dy": {
					if(world.players[playerID].entity == null || world.players[playerID].entity.dead)
						world.createPlayerEntity(playerID);
					world.players[playerID].entity.dir.y = Double.parseDouble(value);
					break;
				}
				
				case "mx": {
					world.players[playerID].mouseX = Integer.parseInt(value);
					send = true;
					break;
				}
				
				case "my": {
					world.players[playerID].mouseY = Integer.parseInt(value);
					send = true;
					break;
				}
				
				case "ml": {
					world.players[playerID].leftDown = value.equals("1");
					send = true;
					break;
				}
				
				case "left": {
					world.players[playerID].left = value.equals("1");
					send = true;
					break;
				}
				
				case "right": {
					world.players[playerID].right = value.equals("1");
					send = true;
					break;
				}
				
				case "jump": {
					world.players[playerID].jump = value.equals("1");
					send = true;
					break;
				}
				
				case "sprint": {
					world.players[playerID].sprint = value.equals("1");
					send = true;
					break;
				}
				
				case "sneak": {
					world.players[playerID].sneak = value.equals("1");
					send = true;
					break;
				}
			}
			
			if(send) // sends the same object to all other clients
				server.sendExcept(cmd +":" +playerID +"|" +value, playerID-1);
		} catch(NumberFormatException ex)
		{
		}
	}
	
	/** Client receives a command from server
	 * commands are:
	 * a (add new player)
	 * d (delete player)
	 * x, y (set position)
	 */
	private void doClientCommand(String cmd, String value)
	{
		try
		{
			int playerID = -1;
			
			// try to split value into "client id" and "actual value"
			int index = value.indexOf("|");
			if(index >= 0)
			{
				playerID = Integer.parseInt(value.substring(0, index));
				value = value.substring(index+1);
			}
			
			switch(cmd)
			{
				case "option_collide": {
					Options.playerCollide = value.equals("1");
					break;
				}
				
				case "ping": {
					world.players[playerID].ping = Integer.parseInt(value);
					break;
				}
				
				case "id": {
					playerID = Integer.parseInt(value);
					world.swapPlayer(localID, playerID);
					localID = playerID;
					break;
				}
				
				case "name": {
					world.players[playerID].name = value;
					break;
				}
				
				case "map": {
					index = value.indexOf("_");
					
					int world = Integer.parseInt(value.substring(0, index));
					int level = Integer.parseInt(value.substring(index+1));
					
					openMap(world, level);
					break;
				}
				
				case "color": {
					world.players[playerID].color = Integer.parseInt(value);
					break;
				}
				
				case "player_ce": {
					playerID = Integer.parseInt(value);
					world.createPlayerEntity(playerID);
					break;
				}
				
				case "player_re": {
					playerID = Integer.parseInt(value);
					world.removePlayerEntity(playerID);
					break;
				}
				
				case "player_r": {
					playerID = Integer.parseInt(value);
					world.removePlayer(playerID);
					break;
				}
				
				case "player_d": {
					playerID = Integer.parseInt(value);
					if(world.players[playerID] != null && world.players[playerID].entity != null)
						world.players[playerID].entity.kill();
					break;
				}
				
		// Position / Motion
				case "x": {
					if(world.players[playerID] == null || world.players[playerID].entity == null || world.players[playerID].entity.dead)
						world.createPlayerEntity(playerID);
					world.players[playerID].entity.pos.x = Double.parseDouble(value);
					break;
				}
				
				case "y": {
					if(world.players[playerID] == null || world.players[playerID].entity == null || world.players[playerID].entity.dead)
						world.createPlayerEntity(playerID);
					world.players[playerID].entity.pos.y = Double.parseDouble(value);
					break;
				}
				
				case "dx": {
					if(world.players[playerID] == null || world.players[playerID].entity == null || world.players[playerID].entity.dead)
						world.createPlayerEntity(playerID);
					world.players[playerID].entity.dir.x = Double.parseDouble(value);
					break;
				}
				
				case "dy": {
					if(world.players[playerID] == null || world.players[playerID].entity == null || world.players[playerID].entity.dead)
						world.createPlayerEntity(playerID);
					world.players[playerID].entity.dir.y = Double.parseDouble(value);
					break;
				}
				
		// Mouse / Keys
				case "mx": {
					if(world.players[playerID] == null)
						world.createPlayer(playerID);
					world.players[playerID].mouseX = Integer.parseInt(value);
					break;
				}
				
				case "my": {
					if(world.players[playerID] == null)
						world.createPlayer(playerID);
					world.players[playerID].mouseY = Integer.parseInt(value);
					break;
				}
				
				case "ml": {
					if(world.players[playerID] == null)
						world.createPlayer(playerID);
					world.players[playerID].leftDown = value.equals("1");
					break;
				}
				
				case "left": {
					if(world.players[playerID] == null)
						world.createPlayer(playerID);
					world.players[playerID].left = value.equals("1");
					break;
				}
				
				case "right": {
					if(world.players[playerID] == null)
						world.createPlayer(playerID);
					world.players[playerID].right = value.equals("1");
					break;
				}
				
				case "jump": {
					if(world.players[playerID] == null)
						world.createPlayer(playerID);
					world.players[playerID].jump = value.equals("1");
					break;
				}
				
				case "sprint": {
					if(world.players[playerID] == null)
						world.createPlayer(playerID);
					world.players[playerID].sprint = value.equals("1");
					break;
				}
				
				case "sneak": {
					if(world.players[playerID] == null)
						world.createPlayer(playerID);
					world.players[playerID].sneak = value.equals("1");
					break;
				}
			}
		} catch(Exception ex)
		{
		}
	}
	
	public void openGui(Gui gui)
	{
		if(currentGui != null)
			currentGui.onGuiClosed();
		
		currentGui = gui;
	}
	
	public void close()
	{
		// do stuff before closing
		onClose();
		
		// close
		System.exit(0);
	}
	
	public void onClose()
	{
		// save before closing
		saveAccount();
		Options.save();
		
		// disconnect from server / close server
		if(gameState == STATE_MULTIPLAYER_CLIENT)
		{
			client.sendDirectly("-1");
			client.disconnect();
		}
		else if(gameState == STATE_MULTIPLAYER_SERVER)
		{
			server.sendDirectly("-1");
			server.close();
		}
	}
	
//	public int getXOffset() { return camera.x; }
//	
//	public int getYOffset() { return camera.y; }
	
//
	public void draw(Graphics g)
	{
		if(currentGui != null && !currentGui.isIngameGui())
		{
			if(!(currentGui instanceof Editor))
			{
				Graphics2D g2 = (Graphics2D)g;
				GradientPaint gp = new GradientPaint(1000.f, ScreenHeight + 100, new Color(0xA0, 0xA0, 0xA0), ScreenWidth - 100.f, 50.f, new Color(0xF0, 0xF0, 0xF0));
				g2.setPaint(gp);
				g2.fillRect(0, 0, ScreenWidth, ScreenHeight);
			}
			
			currentGui.draw(g);
		}
		else
		{
			if(account == null || world == null)
				return;
			
			// render blocks
			world.draw(g, camera.x, camera.y);
			
			// render HUD
			drawHUD(g);
			
			// render ingame gui
			if(currentGui != null)
				currentGui.draw(g);
		}
	}
	
	private void drawHUD(Graphics g)
	{
		// render life
		if(openWorld)
		{
			int x = 10;
			int y = 10;
			int size = 32;
			int gap = 5;
			for(int i = 0; i < world.getLocalPlayer().entity.maxHP; i++)
			{
				// inside
				g.setColor(	world.getLocalPlayer().entity.hp <= i ? Color.gray :
							world.getLocalPlayer().entity.isPoisened() ? Color.green :
							world.getLocalPlayer().entity.isRegenerating() ? Color.magenta : Color.red);
				g.fillRect(x, y, size, size);
				
				// outside / border
				g.setColor(Color.black);
				g.drawRect(x, y, size - 1, size - 1);
				
				x += size + gap;
			}
		}
		
		// render coins
		int x = ScreenWidth - 90;
		int y = 25;
		Texture.coin64.draw(g, x, y);
		g.setColor(Color.black);
		GuiLabel.draw(g, doCharSequence(getCoins(), '.', 3), x - 6, y + 5, GuiLabel.ALIGNMENT_RIGHT, 64);
		
		// render inventory
		if(openWorld && currentGui == null)
			getPlayerInventory().drawInventorySlotsMidX(g, ScreenWidth/2, ScreenHeight - 76, 64, 6, getPlayerInventory().getHotbarStart(), getPlayerInventory().getHotbarEnd());
		
		// render level time
		if(!openWorld && world.getLocalPlayer().entity != null)
		{
			int time = world.getLocalPlayer().entity.levelTime;
			String sTime = String.format("%02d:%02d:%03d", time / TicksPerSec / 60, time / TicksPerSec % 60, (int)(time * 1000.0D / TicksPerSec) % 1000);
			
			g.setColor(Color.white);
			GuiLabel.draw(g, sTime, ScreenWidth/2, 25, GuiLabel.ALIGNMENT_MIDDLE, 35);
		}
		
		// render player info
		if(world.getLocalPlayer().entity != null)
		{
			String infos[] = {
					String.format("X: %.2f", world.getLocalPlayer().entity.pos.x),
					String.format("Y: %.2f", world.getLocalPlayer().entity.pos.y),
			};

			g.setColor(Color.black);
			for(int i = 0; i < infos.length; i++)
				GuiLabel.draw(g, infos[i], 5, 500 + i*25, GuiLabel.ALIGNMENT_LEFT, 20);
		}
		
		if(renderPlayerList)
			drawPlayerList(g);
	}
	
	private void drawPlayerList(Graphics g)
	{
		int width = 1000;
		int height = 650;
		int x = Game.ScreenWidth/2 - width/2;
		int y = Game.ScreenHeight/2 - height/2;
		
		// render background
		g.setColor(new Color(0xA0, 0xA0, 0xA0, 0xB0));
		g.fillRect(x, y, width, height);
		
		int sideGap = 20;
		x += sideGap;
		y += sideGap;
		width -= sideGap*2;
		height = 40;
		
		// render player list
		for(int i = 0; i < world.players.length; i++)
		{
			if(world.players[i] == null)
				continue;
			
			// render background
			g.setColor(new Color(0xFF, 0xFF, 0xFF, 0x80));
			g.fillRect(x, y, width, height);
			
			// render name
			g.setColor(Color.black);
			GuiLabel.draw(g, "(" +world.players[i].id +")  -  " +world.players[i].name, x + 10, y + 4, GuiLabel.ALIGNMENT_LEFT, height - 8);
			
			// reder ping
			GuiLabel.draw(g, "Ping: " +world.players[i].ping, x + width - 120, y + 6, GuiLabel.ALIGNMENT_LEFT, height - 14);
			
			y += height + 5;
		}
	}
	
// Account stuff
	// coins
	public void addCoin()
	{
		if(account != null)
			account.playerCoins++;
	}
	
	public void addCoins(long amount)
	{
		if(account != null)
			account.playerCoins += amount;
	}
	
	public long getCoins()
	{
		if(account == null)
			return 0;
		
		return account.playerCoins;
	}
	
	// save
	public void saveAccount()
	{
		if(account != null)
			account.save();
	}
	
	// name
	public String getAccountName()
	{
		if(account == null)
			return "";
		
		return account.getName();
	}
	
	// hp
	public int getPlayerHP()
	{
		if(account == null)
			return 1;
		
		return 1 + account.hp;
	}
	
	// dmg
	public int getPlayerDmg()
	{
		if(account == null)
			return 1;
		
		return 1 + account.dmg;
	}
	
	// inventory
	public Inventory getPlayerInventory()
	{
		if(account == null)
			return null;
		
		return account.inventory;
	}
	
	public void onPlayerDied()
	{
		if(getPlayerInventory() != null)
		{
			getPlayerInventory().removeNonSaveItems();
		}
	}
	
//
	public static int tick()
	{
		return gameTick;
	}
	
	public boolean isGuiOpen()
	{
		return currentGui != null;
	}
	
// Stuff that doesnt have to do anything with the game itself
	public static String doCharSequence(String s, char c, int i)
	{
		int j = 0;
		for(int k = s.length()-2; k >= 0; k--)
		{
			j++;
			
			if(j % i == 0)
				s = s.substring(0, k+1) +c +s.substring(k+1);
		}
		
		return s;
	}
	
	public static String doCharSequence(int number, char c, int i)
	{
		return doCharSequence(Integer.toString(number), c, i);
	}
	
	public static String doCharSequence(long number, char c, int i)
	{
		return doCharSequence(Long.toString(number), c, i);
	}
	
	public static byte[] stringToByte(String s)
	{
		return s.getBytes(Charset.forName("ISO-8859-1"));
	}
	
	public static String byteToString(byte bytes[])
	{
		return new String(bytes, Charset.forName("ISO-8859-1"));
	}
	
//	public static byte[] stringToByte(String s)
//	{
//		byte bytes[] = new byte[s.length()];
//		
//		for(int i = 0; i < s.length(); i++)
//			bytes[i] = (byte)s.charAt(i);
//		
//		return bytes;
//	}
//	
//	public static String byteToString(byte bytes[])
//	{
//		try
//		{
//			return new String(bytes, Charset.forName("ISO-8859-1"));
////			return new String(bytes, "UTF-8");
//		} catch(Exception ex)
//		{ }
//		
//		String s = "";
//		for(int i = 0; i < bytes.length; i++)
//			s += bytes[i] >= 0 ? (char)bytes[i] : (char)(256 + bytes[i]);
//		
//		return s;
//	}
	
	public static String[] split(String s, char c)
	{
		int size = 0;
		int index = 0;
		
		// get array size
		for(int i = 0; i < s.length(); i++)
		{
			if(s.charAt(i) == c)
			{
				if(index > 0)
					size++;
				index = 0;
			}
			else
			{
				index++;
			}
		}
		
		if(index > 0)
			size++;

		if(size == 0)
			return new String[0];
		
		// create array
		String values[] = new String[size];
		
		index = 0;
		int lastIndex = 0;
		int pos = 0;
		while(index != -1)
		{
			size = 0;
			index = s.indexOf(c, lastIndex);
			
			if(index == -1)
				size = s.length() - lastIndex;
			else
				size = index - lastIndex;
			
			if(size > 0)
			{
				if(index == -1)
					values[pos++] = s.substring(lastIndex);
				else
					values[pos++] = s.substring(lastIndex, index);
			}
			
			lastIndex = index + 1;
		}
		
		return values;
	}
	
	public static String integerToString(int i, int radix)
	{
		if(i == 0)
			return Character.toString((char)0);
		
		String s = "";
		while(i > 0)
		{
			s += (char)(i % radix);
			i /= radix;
		}
		
		String sCorrect = "";
		for(int j = 0; j < s.length(); j++)
			sCorrect += s.charAt(s.length() - j - 1);
		
		return sCorrect;
	}
	
	public static int stringToInteger(String s, int radix)
	{
		int result = 0;
		
		for(int i = 0; i < s.length(); i++)
			result += s.charAt(s.length() - i - 1) * Math.pow(radix, i);
		
		return result;
	}
	
	private GameWorld world;
	private Camera camera;
	
	private Gui currentGui;
	
	private static int gameTick;
	public boolean testMode;
	public boolean openWorld;
	
	private int mapWorld;
	private int mapLevel;
	private String mapName;
	
	public Account account;
	
	public static final int BlockSize = 32;
	
	public static final Color BackgroundColor = new Color(0xEE, 0xEE, 0xEE);
	public static final Color ForegroundColor = new Color(0x77, 0x77, 0x77);
	
	public static final int ScreenWidth = 1280;
	public static final int ScreenHeight = 720;
	public static final int TicksPerSec = 120;
	
	public static final int InventoryWidth = 10;
	public static final int InventoryHeight = 5;
	
	// GameState
	public int gameState;
	public static final int STATE_SINGLEPLAYER = 0;
	public static final int STATE_MULTIPLAYER_CLIENT = 1;
	public static final int STATE_MULTIPLAYER_SERVER = 2;
	
	// Level
	public static final int LevelAmount[] = {
			12,	// World 1
			12,	// World 2
			12,	// World 3
			12,	// World 4
			1,	// World 5
	};
	
	// Color
	public static final Color[] AvailableColors = {
		Color.black,
		Game.ForegroundColor,
		new Color(0x0, 0xDD, 0x0),
		Color.magenta,
	};
	
	// Multiplayer
	public int localID;
	public String lastIP = "localhost";
	private boolean renderPlayerList;
	
	private ArrayList<String> clientMessages = new ArrayList<String>();
	private ArrayList<ServerMessage> serverMessages = new ArrayList<ServerMessage>();
	public UDPClient client;
	public UDPServer server;
	
	public static final int MaxPlayer = 32;
	public static final int Port = 9110;
	public static final int MaxClients = MaxPlayer - 1;
	/** If a player doesnt respond to the ping question for that time (milliseconds), he'll be kicked */
	public static final long TIME_NOT_ACTING = 10000;
	
	class ServerMessage
	{
		public ServerMessage(int clientID, String message)
		{
			this.clientID = clientID;
			str = message;
		}
		
		public final int clientID;
		public final String str;
	}
}