package krisko.acadyan;

public class Vector2D
{
	public Vector2D()
	{
		this(0.0D, 0.0D);
	}
	
	public Vector2D(Vector2D vec)
	{
		this(vec.x, vec.y);
	}
	
	public Vector2D(double angle)
	{
		this(Math.cos(angle), Math.sin(angle));
	}
	
	public Vector2D(double x, double y)
	{
		set(x, y);
	}
	
//
	public void set(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public double getDistance()
	{
		return Math.sqrt(y*y + x*x);
	}
	
	public double getAngle()
	{
		return Math.atan2(y, x);
	}
	
	public Vector2D normalize()
	{
		double angle = getAngle();
		set(Math.cos(angle), Math.sin(angle));
		return this;
	}
	
//
	public void add(Vector2D vec)
	{
		x += vec.x;
		y += vec.y;
	}
	
	public void subtract(Vector2D vec)
	{
		x -= vec.x;
		y -= vec.y;
	}
	
	public void multiply(double d)
	{
		x *= d;
		y *= d;
	}
	
	public void divide(double d)
	{
		if(d == 0.0D)
		{
			x = Double.POSITIVE_INFINITY;
			y = Double.POSITIVE_INFINITY;
		}
		else
		{
			x /= d;
			y /= d;
		}
	}
	
//
	public Vector2D plus(double x, double y) { return new Vector2D(this.x + x, this.y + y); }
	public Vector2D plus(Vector2D vec) { return plus(vec.x, vec.y); }
	
	public Vector2D minus(double x, double y) { return new Vector2D(this.x - x, this.y - y); }
	public Vector2D minus(Vector2D vec) { return minus(vec.x, vec.y); }
	
	public Vector2D mul(double d) { return new Vector2D(x * d, y * d); }
	
	public Vector2D div(double d)
	{
		if(d == 0.0D)
			return new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		else
			return new Vector2D(x / d, y / d);
	}
	
// static
	public static Vector2D add(Vector2D vec1, Vector2D vec2)
	{
		return new Vector2D(vec1.x + vec2.x, vec1.y + vec2.y);
	}
	
	public static Vector2D subtract(Vector2D vec1, Vector2D vec2)
	{
		return new Vector2D(vec1.x - vec2.x, vec1.y - vec2.y);
	}
	
	public static Vector2D multiply(Vector2D vec1, double d)
	{
		return new Vector2D(vec1.x * d, vec1.y * d);
	}
	
	public static Vector2D divide(Vector2D vec1, double d)
	{
		if(d == 0.0D)
			return new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		else
			return new Vector2D(vec1.x / d, vec1.y / d);
	}
	
	public double x, y;
}