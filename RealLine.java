import java.awt.*;
/**
 * Ligne en coordonnees cartesiennes
 * utile pour la correction de la perspective
 * @author : Alexis Royer
 */
public class RealLine
{
    
    private int y0;
    private double a;

    /**
     * une droite est sous la forme y = ax + y0
     */
    public RealLine(int X1, int Y1, int X2, int Y2)
    {
        a = (((double)Y2)-((double)Y1))/(((double)X2)-((double)X1));
        y0 = (int)(Y1 - (a*X1));
    }
    
    public RealLine(Point P1, Point P2)
    {
        this((int)P1.getX(),(int)P1.getY(),(int)P2.getX(),(int)P2.getY());
    }
    
    public RealLine(int y0, double a)
    {
        this.a = a;
        this.y0 = y0;
    }
    
    public Point intersect( RealLine droite2 )
    {
        double test = (y0 - droite2.getY0());
        double test2 = (droite2.getA() - a);
        int x = (int)( test/ test2);
        int y = (int)(a*x + y0);
        return new Point(x,y);
    }
    
    public double getA()
    {
        return a;
    }
    
    public int getY0()
    {
        return y0;
    }
}
