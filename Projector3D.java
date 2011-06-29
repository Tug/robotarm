import java.awt.Point;
public class Projector3D
{
    private Point3D         c;      // cam position
    private Point3D.Double  theta;  // cam rotation
    private Point3D         e;      // viewer position
    
    public Projector3D(Point3D c, Point3D.Double theta, Point3D e)
    {
        this.c = c;
        this.theta = theta;
        this.e = e;
    }
    
    public Point projection(Point3D a) 
    {
        Point3D d = new Point3D();
        d.x = (int)(Math.cos(theta.y)*(Math.sin(theta.z)*(a.y-c.y)+Math.cos(theta.z)*(a.x-c.x))
        +Math.cos(theta.x)*(a.x-c.x)+Math.cos(theta.x)*(Math.cos(theta.z)*(a.y-c.y)-Math.sin(theta.z)*(a.x-c.x)));
        d.y = (int)(Math.sin(theta.y)*(Math.cos(theta.y)*(a.z-c.z)+Math.sin(theta.y)*(a.y-c.y)
        +Math.cos(theta.z)*(a.x-c.x))+Math.cos(theta.x)*(a.y-c.y)-Math.sin(theta.z)*(a.x-c.x));
        d.z = (int)(Math.cos(theta.x)*(Math.cos(theta.y)*(a.z-c.z)+Math.sin(theta.z)*(a.y-c.y))
        +Math.cos(theta.z)*(a.x-c.x)-Math.sin(theta.x)*(Math.cos(theta.z)*(a.y-c.y)-Math.sin(theta.z)*(a.x-c.x)));
        int bx = (d.x - e.x)*(e.z/d.z);
        int by = (d.y - e.y)*(e.z/d.z);
        return new Point(bx, by);
    }
    
    

}
