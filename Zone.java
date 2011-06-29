import java.awt.Point;
 
/**
 * Write a description of class Zone here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Zone extends Point
{
    private int width;
    private int height;
    private Point center;

    /**
     * Constructor for objects of class Zone
     */
    public Zone(int x, int y, int width,int height)
    {
        super(x,y);
        this.width = width;
        this.height = height;
        center = new Point(x+width/2,y+height/2);
    }
    public Zone(Point p, int width,int height)
    {
        super(p);
        this.width = width;
        this.height = height;
        center = new Point(p.x+width/2,p.y+height/2);
    }


    public Point getCenter(){ return center; }
    public int getWidth(){ return width; }
    public int getHeight(){ return height; }
    
}
