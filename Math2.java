import java.awt.Point;

/**
 * La classe Math2 contient des methodes static utiles pour faire de la geometrie  :
 * intersection de droites, projection, changement de variable, ...
 */
public final class Math2
{
    private final static double coef = 512/(2*Math.PI);
    private final static double pi2 = 2*Math.PI;
    
    public static Point intersec(ParametricFunction f1, ParametricFunction f2, Point center)
    {
        Point p = new Point();
        // Ax = b
        double[][] A = {{ Math.cos(f1.getTheta()), Math.sin(f1.getTheta()) },
                        { Math.cos(f2.getTheta()), Math.sin(f2.getTheta()) }};
        double[] b = { f1.getR(), f2.getR() };
        // inverse de la matrice A
        double det = A[0][0] * A[1][1] - A[0][1] * A[1][0];
        // si le determinant est nul, la matrice n'est pas inversible,
        // les droites sont paralleles (ou presque), on retourne null
        if(equals(det,0,0.1)) return null;
        // sinon elle est inversible, 
        // on remplace A par son inverse
        A[0][0] = A[1][1]/det;
        A[0][1] /= -det;
        A[1][0] /= -det;
        A[1][1] = A[0][0]/det;
        // multiplication de la matrice A-1 par la matrice b : x = A-1 b 
        p.x = (int)(A[0][0]*b[0] + A[0][1]*b[1]) + center.x;
        p.y = (int)(A[1][0]*b[0] + A[1][1]*b[1]) + center.y;
        return p;
    }
    
    public static Point intersecInPlan(ParametricFunction f1, ParametricFunction f2, Point center)
    {
        Point p = intersec(f1,f2,center);
        if(p == null) return null;
        if(p.x >=0 && p.x < 2*center.x && p.y >=0 && p.y < 2*center.y) return p;
        return null;
    }
    
    public Point pointCroisement(Point D11, Point D12, Point D21, Point D22)
    {
        RealLine L1 = new RealLine(D11,D12);
        RealLine L2 = new RealLine(D21,D22);
        return L1.intersect(L2);
    }
    
    public static Point intersec(Point D11, Point D12, Point D21, Point D22)
    {
        RealLine L1 = new RealLine(D11,D12);
        RealLine L2 = new RealLine(D21,D22);
        return L1.intersect(L2);
    }
    
    public static boolean equals(double a, double b, double epsi)
    {
        return (Math.abs(a-b) <= epsi);
    }
    
    public static boolean equalsAngleDroite(double a, double b, double epsi)
    {
        return equals(a,b,epsi) || equalsPlusPI(a,b,epsi);
    }
    
    public static boolean equalsPlusPI(double a, double b, double epsi)
    {
        return Math.abs(Math.PI-Math.abs(a-b)) <= epsi;
    }

    public static double normalizeAngle2PI(double a)
    {
        while(a < 0) a+=2*Math.PI;
        while(a > 2*Math.PI) a-= 2*Math.PI;
        return a;
    }
    
    public static double normalizeAnglePI(double a)
    {
        a = normalizeAngle2PI(a);
        if(a > Math.PI) return a-2*Math.PI;
        return a;
    }
    
    public static double normalizeAnglePI2(double a)
    {
        a = normalizeAngle2PI(a);
        if(a > Math.PI) return a-Math.PI;
        return a;
    }
    
    public static PolarPoint cartesianToPolar(Point p, Point origine, int width, double mm_en_pix)
    {
        int x = Math.abs(p.x - origine.x);
        int y = Math.abs(p.y - origine.y);
        int d = (int)Math.sqrt(x*x + y*y);
        d *= mm_en_pix;
        double theta = Math.atan2(y,x);
        return new PolarPoint(theta,d);
    }
    
    
    public static Point perspectiveCorrection(Point p, Grille deformed_grille, int width, int height)
    {
        Point fuite_haut = deformed_grille.getFuiteHaut();
        Point fuite_cote = deformed_grille.getFuiteCote();
        int xA,xB,yA,yD, XAmod,XBmod,YAmod,YDmod;
        int[] mod = deformed_grille.getModifiedPoints();
        XAmod = mod[0];
        XBmod = mod[1];
        YAmod = mod[2];
        YDmod = mod[3];
        xA = mod[4];
        xB = mod[5];
        yA = mod[6];
        yD = mod[7];
        double[] k = deformed_grille.getKModifier();
        double kX = k[0];
        double kY = k[1];
        Point intersect = Math2.intersec(p, fuite_haut, new Point(0,0), new Point(width,0));
        int Xinter = intersect.x;
        double alfX = (Xinter - xA) / ( kX * ( xB - Xinter) );
        intersect = Math2.intersec(p, fuite_cote, new Point(0,0), new Point(0,height));
        int Yinter = intersect.y;
        double alfY = (Yinter - yA) / ( kY * ( yD - Yinter) );
        int Xmod = (int)((XAmod + alfX * XBmod)/(1+alfX));
        int Ymod = (int)((YAmod + alfY * YDmod)/(1+alfY));
        return new Point(Xmod,Ymod);
    }
    
    public static boolean isPointInZone(Point p, Zone z)
    {
        return true;
    }
    
    public static int distanceBetween(Point a, Point b)
    {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(b.x - b.y);
        int d = (int)Math.sqrt(dx*dx + dy*dy);
        return d;
    }

}
