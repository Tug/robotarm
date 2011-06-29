import java.awt.Point;

public class OrientedAngle
{
    private Point vertex;
    private double angle1 = 0.0;
    private double angle2 = 0.0;
    private ParametricFunction pf1, pf2;
    private boolean orthogonal = false;
    private int r;
    
    public OrientedAngle(Point p, ParametricFunction pf1, ParametricFunction pf2)
    {
        this.vertex = p;
        this.angle1 = pf1.getTheta();
        this.angle2 = pf2.getTheta();
        double a = Math.abs(angle1 - angle2);
        orthogonal = Math2.equals(Math2.normalizeAnglePI(a),Math.PI/2,0.4);
        this.pf1 = pf1;
        this.pf2 = pf2;
    }
    
    public OrientedAngle(Point p, double angle1, double angle2, int r)
    {
        this.vertex = p;
        this.angle1 = angle1;
        this.angle2 = angle2;
        double a = Math.abs(angle1 - angle2);
        orthogonal = Math2.equals(Math2.normalizeAnglePI(a),Math.PI/2,0.4);
        this.r = r;
    }
    
    public boolean isAlmostOrthogonal()
    {
        return orthogonal;   
    }
    
    public Point getPointInside(Point diff, double scale)
    {
        double x = vertex.x + diff.x*Math.sin(angle1)+diff.y*Math.sin(angle2);
        double y = vertex.y + diff.x*Math.cos(angle1)+diff.y*Math.cos(angle2);
        x /= scale;
        y /= scale;
        return new Point((int)x,(int)y);
    }
    
    public void drawSocle(byte[] tab, int width)
    {
        Filter.drawLineCenter(tab,width,pf1);
        Filter.drawLineCenter(tab,width,pf2);
    }
    
    public Point getVertex() { return vertex; }
    
    public void multBy(int nb)
    {
        vertex = new Point(vertex.x*nb, vertex.y*nb);
        r *= 4;
    }
    
    public void correctPerspective(Grille grille, int width, int height)
    {
        vertex = Math2.perspectiveCorrection(vertex, grille, width, height);
        Point pa1 = new Point((int)(vertex.x+r*Math.cos(angle1)),
                              (int)(vertex.y+r*Math.sin(angle1)));
        pa1 = Math2.perspectiveCorrection(pa1, grille, width, height);
        Point pa2 = new Point((int)(vertex.x+r*Math.cos(angle2)),
                              (int)(vertex.y+r*Math.sin(angle2)));
        pa2 = Math2.perspectiveCorrection(pa2, grille, width, height);
        angle1 = Math.atan2(vertex.y-pa1.y, vertex.x-pa1.x);
        angle2 = Math.atan2(vertex.y-pa2.y, vertex.x-pa2.x);
    }
    
    public Point getBras(Point bras_coin, double mm_en_pix)
    {
        bras_coin = new Point((int)(bras_coin.x*mm_en_pix),(int)(bras_coin.y*mm_en_pix));
        double x = vertex.x+bras_coin.x*Math.cos(angle1)+bras_coin.x*Math.cos(angle2);
        double y = vertex.y+bras_coin.y*Math.sin(angle1)+bras_coin.y*Math.sin(angle2);
        return new Point((int)(x*mm_en_pix),(int)(y*mm_en_pix));
    }

}
