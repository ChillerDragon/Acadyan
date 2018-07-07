package krisko.acadyan;

public class Player
{
	public Player(int playerID)
	{
		this.id = playerID;
	}
	
	public void update(GameWorld world)
	{
		if(entity != null && !entity.dead)
		{
			// update entity
			if(world.game.gameState == Game.STATE_SINGLEPLAYER || world.game.gameState == Game.STATE_MULTIPLAYER_SERVER)
				entity.update(mouseX, mouseY, leftDown, left, right, jump, sprint, sneak);
			else
				entity.updateMultiplayer(mouseX, mouseY, leftDown, left, right, jump, sprint, sneak);
		}
		else if(timePlayerDied == 0)
		{
			timePlayerDied = Game.tick();
			
			if(world.game.gameState == Game.STATE_MULTIPLAYER_SERVER)
				world.game.server.send("player_d:" +id);
		}
		else
		{
			if(Game.tick() - timePlayerDied >= Game.TicksPerSec*2)
			{
				timePlayerDied = 0;
				world.createPlayerEntity(id);
			}
		}
	}
	
	/** This is the client id (+1) of the player */
	public int id;
	
	public long timePingSent = -1;
	public int ping;
	
	// player data
	public int color;
	public String name;
	
	public int mouseX, mouseY;
	public boolean leftDown;
	public boolean left, right, jump, sprint, sneak;
	
	public int lastMouseX, lastMouseY;
	public boolean lastLeftDown;
	public boolean lastLeft, lastRight, lastJump, lastSprint, lastSneak;
	
	// The character entity
	public EntityPlayer entity;
	
	// entity data
	private int timePlayerDied;
}