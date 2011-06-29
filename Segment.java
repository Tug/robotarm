import java.awt.Point;

public class Segment
{
    private Point a;
    private Point b;
    private ParametricFunction pf;
    
    public Segment(Point a, Point b, ParametricFunction pf)
    {
        this.a = a;
        this.b = b;
        this.pf = pf;
    }
    
    public Segment(int a, int b, ParametricFunction pf, int width)
    {
        this.a = new Point(a%width,a/width);
        this.b = new Point(b%width,b/width);
        this.pf = pf;
    }
    
    public double getDistance() {
        return Math.sqrt((b.x-a.x)*(b.x-a.x)+(b.y-a.y)*(b.y-a.y));
    }
}
