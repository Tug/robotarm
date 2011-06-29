import java.awt.Point;
import java.text.DecimalFormat;

/**
 * La classe ParametricFunction permet d'instancier des droites en representation parametrique :
 * x*cos(theta)+y*sin(theta) = r
 */
public class ParametricFunction
{
    private double theta;
    private int r;
    private int intensity;
    
    public ParametricFunction(double theta, int r, int intensity)
    {
        this.intensity = intensity;
        this.r = r;
        this.theta = theta;
    }

    public double getTheta() { return theta; }
    public int getR() { return r; }
    public int getIntensity() { return intensity; }
    public void multR(int factor) { this.r = factor*r; }
    public void setTheta(double theta) { this.theta = theta; }
    
    public void printFunction()
    {
        System.out.println("x*cos("+theta+") + y*sin("+theta+") = "+ r);
    }
    
    public boolean touched(int x, int y)
    {
        return Math2.equals(x*Math.cos(theta)+y*Math.sin(theta), r, 1);
    }
    
    public int getY(int x)
    {
        if(isVertical()) return Integer.MAX_VALUE;
        return (int)((r-x*Math.cos(theta))/Math.sin(theta));
    }
    
    public int getX(int y)
    {
        if(isHorizontal()) return Integer.MAX_VALUE;
        return (int)((r-y*Math.sin(theta))/Math.cos(theta));
    }
    
    public boolean isVertical()
    {
        return Math2.equals(theta,0,10e-4);
    }
    
    public boolean isAlmostVertical()
    {
        return Math2.equals(theta,0,0.5) || Math2.equals(theta,Math.PI,0.5);
    }
    
    public boolean isHorizontal()
    {
        return Math2.equals(theta,Math.PI/2,10e-4);
    }
    
    @Override
    public String toString()
    {
        double t = 180 * theta / Math.PI;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2); //arrondi à 2 chiffres apres la virgule
        return "x*cos("+df.format(t)+")+y*sin("+df.format(t)+") = " + r;
    }
    
}
