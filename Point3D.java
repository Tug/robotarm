
public class Point3D
{
    public int x;
    public int y;
    public int z;
    
    public Point3D(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Point3D()
    {
        this(0,0,0);
    }

    public static class Double
    {
        public double x;
        public double y;
        public double z;
        
        public Double(double x, double y, double z) 
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public Double()
        {
            this(0,0,0);
        }
    }
}
