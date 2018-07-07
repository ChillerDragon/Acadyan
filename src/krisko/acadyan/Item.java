package krisko.acadyan;

import java.awt.Graphics;

public class Item
{
	private final int id;
	private final Texture texture;
	private final IItem itemInterface;
	
	public static final int MAX_ITEMS = 16;
	private static final Item[] items = new Item[MAX_ITEMS];
	
	private Item(int itemID, Texture tex, IItem iInterface)
	{
		id = itemID;
		texture = tex;
		itemInterface = iInterface;
		
		if(id >= 0 && id < MAX_ITEMS)
		{
			if(items[id] != null)
				System.out.println("Item Collision! ID: " +id);
			
			items[id] = this;
		}
	}
	
	public static Item get(int id)
	{
		if(id < 0 || id >= items.length)
			return null;
		
		return items[id];
	}
	
	public int getID() { return id; }
	
//
	public boolean use(EntityPlayer player)
	{
		return player == null ? false : itemInterface.use(player);
	}
	
// render
	public void draw(Graphics g, int x, int y, int w, int h)
	{
		texture.draw(g, x, y, w, h);
	}
	
// items
// potions
	public static final Item potion = new Item(0, Texture.potion, new IItem() {
		@Override
		public boolean use(EntityPlayer player) {
			return player.heal();
		}
	});
	
	public static final Item poison = new Item(1, Texture.poison, new IItem() {
		@Override
		public boolean use(EntityPlayer player) {
			if(player.isPoisened())
				return false;
			
			player.poison(3.0D, 5);
			return true;
		}
	});
	
	public static final Item regeneration = new Item(2, Texture.regeneration, new IItem() {
		@Override
		public boolean use(EntityPlayer player) {
			if(player.isRegenerating() || player.hp >= player.maxHP)
				return false;
			
			player.regenerate(2.0D, player.maxHP - player.hp);
			return true;
		}
	});
	
	public static void load()
	{ }
}