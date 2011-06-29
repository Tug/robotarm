/**
 * Point en coordonnees polaire
 */
public class PolarPoint
{
	public double theta;
    public int r;
    
    public PolarPoint(double theta, int r)
    {
        this.theta = theta;
        this.r = r;
    }
    
    @Override public String toString() {
        return "r: "+r+"mm  theta: "+theta+"rad";
    }
    
}
