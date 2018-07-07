package krisko.acadyan;

import java.awt.Graphics;

public class GameWorld extends World
{
	public GameWorld(Game game)
	{
		this.game = game;
		
		addEntity(new EntityGrayEnemy(this, new Vector2D(0, 0)));
	}
	
	@Override
	public void update(InputHandler handler)
	{
		// update players
		for(int i = 0; i < Game.MaxPlayer; i++)
		{
			if(players[i] == null)
				continue;
			
			if(i == game.localID) // local player
			{
				players[i].lastMouseX = players[i].mouseX;
				players[i].lastMouseY = players[i].mouseY;
				players[i].lastLeftDown = players[i].leftDown;
				
				players[i].lastLeft = players[i].left;
				players[i].lastRight = players[i].right;
				players[i].lastJump = players[i].jump;
				players[i].lastSprint = players[i].sprint;
				players[i].lastSneak = players[i].sneak;
				
				players[i].mouseX = handler.mouseX;
				players[i].mouseY = handler.mouseY;
				players[i].leftDown = Controls.punch.isPressed(handler);
				
				players[i].left = Controls.left.isPressed(handler);
				players[i].right = Controls.right.isPressed(handler);
				players[i].jump = Controls.jump.isPressed(handler);
				players[i].sprint = Controls.sprint.isPressed(handler);
				players[i].sneak = Controls.sneak.isPressed(handler);
			}
			
			players[i].update(this);
		}
		
		super.update(handler);
	}
	
// Player stuff
	public Player getLocalPlayer()
	{
		return players[game.localID];
	}
	
/**
 * Only creates a 'Player, not an 'EntityPlayer'
 * @param playerID
 */
	public void createPlayer(int playerID)
	{
		if(players[playerID] == null)
			players[playerID] = new Player(playerID);
	}
	
/**
 * Creates a 'Player' and 'EntityPlayer'
 * @param playerID
 */
	public void createPlayerEntity(int playerID)
	{
		if(players[playerID] == null)
			createPlayer(playerID);
		else
			removePlayerEntity(playerID);
		
		players[playerID].entity = new EntityPlayer(this, players[playerID]);
		addEntity(players[playerID].entity);
	}
	
	public void removePlayer(int playerID)
	{
		if(players[playerID] == null)
			return;
		
		removePlayerEntity(playerID);
		players[playerID] = null;
	}
	
	public void removePlayerEntity(int playerID)
	{
		if(players[playerID] == null || players[playerID].entity == null)
			return;
		
		removeEntity(players[playerID].entity);
		players[playerID].entity = null;
	}
	
	public void swapPlayer(int id1, int id2)
	{
		Player player = players[id1];
		players[id1] = players[id2];
		players[id2] = player;
		
		if(players[id1] != null)
			players[id1].id = id1;
		if(players[id2] != null)
			players[id2].id = id2;
	}
	
//
	@Override
	public void onEntityAdded(Entity entity)
	{
//		if(game.gameState == Game.STATE_MULTIPLAYER_SERVER)
//		{
//			if(entity instanceof EntityPlayer)
//			{
//				EntityPlayer player = (EntityPlayer)entity;
//				game.server.send("player_a:" +player.id);
//			}
//		}
	}
	
	@Override
	public void onEntityRemoved(Entity entity)
	{
		if(entity instanceof EntityPlayer || !(entity instanceof EntityLiving))
			return;
		
		game.addCoins(10);
	}
	
// draw
	@Override
	public int playerInsideHidden(EntityPlayer player)
	{
		int l = (int)player.pos.x;
		if(player.pos.x < 0.0D)
			l--;
		int r = (int)(player.pos.x + EntityPlayer.WorldSize);
		if(player.pos.x + EntityPlayer.WorldSize < 0.0D || getLocalPlayer().entity.pos.x + EntityPlayer.WorldSize == r)
			r--;
		int t = (int)player.pos.y;
		if(player.pos.y < 0.0D)
			t--;
		int b = (int)(player.pos.y + EntityPlayer.WorldSize);
		if(player.pos.y + EntityPlayer.WorldSize < 0.0D || getLocalPlayer().entity.pos.y + EntityPlayer.WorldSize == b)
			b--;
		
		// update block data
		for(int y = t; y <= b; y++)
		{
			for(int x = l; x <= r; x++)
			{
				BlockData data = getBlockData(Block.hidden.getLayer(), x, y);
				if(data != null && data instanceof BlockDataHidden)
				{
					DataValue dataValue = (DataValue)data.get(0);
					return dataValue.value;
				}
			}
		}
		
		return -1;
	}
	
	@Override
	public void drawPlayer(Graphics g, int xOffset, int yOffset)
	{
		for(int i = 0; i < Game.MaxPlayer; i++)
		{
			if(players[i] != null && players[i].entity != null && !players[i].entity.dead)
			{
				players[i].entity.draw(g, xOffset, yOffset);
			}
		}
	}
	
	public final Game game;
	public Player players[] = new Player[Game.MaxPlayer];
}