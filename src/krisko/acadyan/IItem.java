package krisko.acadyan;

public interface IItem
{
	/** returns true when the item could be used */
	boolean use(EntityPlayer player);
}