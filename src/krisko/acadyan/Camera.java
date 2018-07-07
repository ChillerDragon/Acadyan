package krisko.acadyan;

public class Camera
{
	public Camera(GameWorld gameWorld)
	{
		world = gameWorld;
		update(); // yes !
	}
	
	public void update()
	{
		if(world == null || world.getLocalPlayer() == null || world.getLocalPlayer().entity == null || world.getLocalPlayer().entity.dead)
			return;
		
		x = (int)(world.getLocalPlayer().entity.pos.x*Game.BlockSize + EntityPlayer.DrawSize/2.0D - Game.ScreenWidth/2.0D);
		y = (int)(world.getLocalPlayer().entity.pos.y*Game.BlockSize + EntityPlayer.DrawSize/2.0D - Game.ScreenHeight/2.0D);
		
		// Hard-mode
//		double angle = Game.tick()/60.0D;
//		double cos = Math.cos(angle);
//		double sin = Math.sin(angle);
//		double radius = 150;
//		
//		x += cos*radius;
//		y += sin*radius;
	}
	
	public int x;
	public int y;
	
	private GameWorld world;
}