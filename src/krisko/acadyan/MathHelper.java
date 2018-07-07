package krisko.acadyan;

public class MathHelper
{
	public static int clamp(int value, int min, int max)
	{
		return value < min ? min : value > max ? max : value;
	}
	
	public static long clamp(long value, long min, long max)
	{
		return value < min ? min : value > max ? max : value;
	}
	
	public static float clamp(float value, float min, float max)
	{
		return value < min ? min : value > max ? max : value;
	}
	
	public static double clamp(double value, double min, double max)
	{
		return value < min ? min : value > max ? max : value;
	}
	
	public static int min(int value1, int value2)
	{
		return value1 < value2 ? value1 : value2;
	}
	
	public static int max(int value1, int value2)
	{
		return value1 > value2 ? value1 : value2;
	}
	
	public static double getDistance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
	}
	
/**
 *             -90<br>
 *              |<br>
 * (-)180-+-0<br>
 *             |<br>
 *            90
 * @param x1
 * @param y1
 * @param x2
 * @param y2
 * @return Math.atan2(y2 - y1, x2 - x1)
 */
	public static double getAngle(double x1, double y1, double x2, double y2)
	{
		return Math.atan2(y2 - y1, x2 - x1);
	}
	
	public static boolean pointInRect(int pX, int pY, int rectX, int rectY, int rectWidth, int rectHeight)
	{
		return pX >= rectX && pY >= rectY && pX < rectX + rectWidth && pY < rectY + rectHeight;
	}
	
	public static boolean pointInRect(double pX, double pY, double rectX, double rectY, double rectWidth, double rectHeight)
	{
		return pX >= rectX && pY >= rectY && pX < rectX + rectWidth && pY < rectY + rectHeight;
	}
	
//	public static boolean rectInsideRect(double rectX1, double rectY1, double rectW1, double rectH1, double rectX2, double rectY2, double rectW2, double rectH2)
//	{
//		return rectX1 <= rectX2+rectW2 && rectX1+rectW1 >= rectX2 && rectY1 <= rectY2+rectH2 && rectY1+rectH1 >= rectY2;
//	}
	
	public static boolean rectBetweenRect(double rectX1, double rectY1, double rectW1, double rectH1, double rectX2, double rectY2, double rectW2, double rectH2)
	{
		return rectX1 < rectX2+rectW2 && rectX1+rectW1 > rectX2 && rectY1 < rectY2+rectH2 && rectY1+rectH1 > rectY2;
	}
}