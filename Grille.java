import java.awt.Point;
import java.util.ArrayList;
import javax.naming.CannotProceedException;

/**
 * Cree un objet Grille pour stocker toutes les donnees la concernant
 * et effectuer des traitements dessus
 */
public class Grille
{
    private ArrayList<ParametricFunction> lineList;
    private double grilleAngle;
    private Point[] tidiedPoints;
    private Point[] boxesCenter;
    private PolarPoint[] boxesCentermm;
    private Point fuiteHaut;
    private Point fuiteCote;
    private int[] modifiedPoints = {170, 470, 90, 390, 0, 0, 0, 0, 0, 0};
    private double[] kModifier = new double[2];
    private int width;
    private int height;
    private Bouchon[][] corksInGrid;
    
    /**
     * Constructeur de Grille :
     * en parametre les lignes la caracterisant et le point central de l'image
     */
    @SuppressWarnings("unchecked")
    public Grille(ArrayList<ParametricFunction> lineList, Point center) throws CannotProceedException
    {
        if(lineList.isEmpty() || lineList.size() != 8) throw new CannotProceedException();
        this.lineList = lineList;
        tidiedPoints = new Point[16];
        // on separe les lignes dans 2 tableaux differents.
        // celle qui sont a peu pres paralleles ensemble
        ParametricFunction[] tab1 = new ParametricFunction[4];
        ParametricFunction[] tab2 = new ParametricFunction[4];
        double theta = lineList.get(0).getTheta();
        int j1 = 0, j2 = 0;
        for(int i=0; i<lineList.size(); i++) {
            if(Math2.equalsAngleDroite(theta,lineList.get(i).getTheta(),Math.PI/4)) {
                tab1[j1] = lineList.get(i);
                j1++;
            } else {
                tab2[j2] = lineList.get(i);
                j2++;
            }
        }
        if(j1 != 4 || j2 != 4) throw new CannotProceedException();
        sortByR(tab1);
        sortByR(tab2);
        if( theta >= 0 && theta <= Math.PI/2 ) {
            ParametricFunction[] temp = tab2;
            tab2 = tab1;
            tab1 = temp;
        }
        grilleAngle = tab1[0].getTheta();
        for(int i=0; i<tab1.length; i++) {
            for(int j=0; j<tab2.length; j++) {
                tidiedPoints[4*i+j] = Math2.intersecInPlan(tab1[i],tab2[j],center);
            }
        }
        defineBoxesCenter();
        setPerspectiveParameters();
    }
    
    /**
     * Deuxieme constructeur de Grille. Permet de dupliquer un objet car la methode clone() a echoue
     */
    public Grille( ArrayList<ParametricFunction> lineList, double grilleAngle, Point[] tidiedPoints,
                   Point[] boxesCenter, PolarPoint[] boxesCentermm, Point fuiteHaut, Point fuiteCote,
                   int[] modifiedPoints, double[] kModifier, int width, int height )
    {
        this.lineList = lineList;
        this.grilleAngle = grilleAngle;
        this.tidiedPoints = tidiedPoints;
        this.boxesCenter = boxesCenter;
        this.boxesCentermm = boxesCentermm;
        this.fuiteHaut = fuiteHaut;
        this.fuiteCote = fuiteCote;
        this.modifiedPoints = modifiedPoints;
        this.kModifier = kModifier;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Defini les points des centres de chaque cases
     */
    private void defineBoxesCenter() 
    {
        boxesCenter = new Point[9];
        for(int i=0; i<boxesCenter.length; i++) {
            int t = i+i/3;
            int y = tidiedPoints[t].y+Math.abs(tidiedPoints[t].y-tidiedPoints[5+t].y)/2;
            int x = tidiedPoints[4+t].x+Math.abs(tidiedPoints[4+t].x-tidiedPoints[1+t].x)/2;
            boxesCenter[i] = new Point(x,y);
        }
    }
    
    /**
     * Classe les droites selon leur distance au centre de l'image,
     * Tri par insertion
     */
    private static void sortByR(ParametricFunction[] tab)
    {
        for(int i=0; i<tab.length; i++) {
            int min = i;
            for(int j=i; j<tab.length; j++) {
                if(tab[min].getR() > tab[j].getR()) min = j;
            }
            ParametricFunction temp = tab[min];
            tab[min] = tab[i];
            tab[i] = temp;
        }
    }
    
    /*
     * Accesseurs
     */
    public Point[] getTidiedPoints() { return tidiedPoints; }
    public Point[] getBoxesCenter() { return boxesCenter; }
    public PolarPoint[] getBoxesCentermm() { return boxesCentermm; }
    public ArrayList<ParametricFunction> getLines() { return lineList; }
    public double getAngle(){ return grilleAngle; }
    public Point getFuiteHaut() { return fuiteHaut; }
    public Point getFuiteCote() { return fuiteCote; }
    public int[] getModifiedPoints() { return modifiedPoints; }
    public double[] getKModifier() { return kModifier; }
    public Bouchon[][] getCorksInGrid() { return corksInGrid; }
    
    /**
     * Retourne la taille en pixel d'une cote de la grille
     */
    public int getSideLength()
    {
        int x = Math.abs(tidiedPoints[12].x - tidiedPoints[15].x);
        int y = Math.abs(tidiedPoints[12].y - tidiedPoints[15].y);
        return (int)Math.sqrt(x*x+y*y);
    }
    
    /**
     * Transforme les points des centres des cases en polaires et en unite mm, rad
     */
    public void setBoxesCenterInmm(Point origine, double mm_en_pix, int width)
    {
        boxesCentermm = new PolarPoint[boxesCenter.length];
        for(int i=0; i<boxesCenter.length; i++) {
            boxesCentermm[i] = Math2.cartesianToPolar(origine,boxesCenter[i],width,mm_en_pix);
        }
    }
    
    /**
     * Defini les parametres necessaire a la projection de la grille deforme
     */
    public void setPerspectiveParameters()
    {
        fuiteHaut = Math2.intersec(tidiedPoints[0], tidiedPoints[12],tidiedPoints[3],tidiedPoints[15]);
        fuiteCote = Math2.intersec(tidiedPoints[0], tidiedPoints[3],tidiedPoints[12],tidiedPoints[15]);
        Point intersect, intersect2;
        intersect = Math2.intersec( tidiedPoints[0], fuiteHaut,new Point(0,0), new Point(640,0));
        int xA = intersect.x;
        intersect = Math2.intersec( tidiedPoints[3], fuiteHaut,new Point(0,0), new Point(640,0));
        int xB = intersect.x;
        intersect = Math2.intersec( tidiedPoints[0], fuiteCote,new Point(0, 0), new Point(0,480));
        int yA = intersect.y;
        intersect = Math2.intersec( tidiedPoints[12], fuiteCote,new Point(0, 0), new Point(0,480));
        int yD = intersect.y;
        intersect = Math2.intersec( tidiedPoints[0], tidiedPoints[15], tidiedPoints[3], tidiedPoints[12]);
        intersect2 = Math2.intersec( intersect , fuiteHaut , new Point(0, 0), new Point(640,0));
        int XcentreAB = intersect2.x;
        double kX = (double)(XcentreAB - xA)/(xB - XcentreAB) ;
        
        intersect2 = Math2.intersec( intersect , fuiteCote , new Point(0, 0), new Point(0,480));
        int YcentreAD = intersect2.y;
        
        double kY = (double)(YcentreAD - yA)/(yD - YcentreAD);
        
        modifiedPoints[5] = xA;
        modifiedPoints[5] = xB;
        modifiedPoints[6] = yA;
        modifiedPoints[7] = yD;
        kModifier[0] = kX;
        kModifier[1] = kY;
    }
    
    /**
     * Correction des parametres de la grille
     */
    public void correctGrille(int width, int height)
    {
        Grille grilleclone = new Grille(lineList,grilleAngle,tidiedPoints,boxesCenter,boxesCentermm,
                                        fuiteHaut,fuiteCote,modifiedPoints,kModifier,width,height);
        for(int i=0; i<tidiedPoints.length; i++) {
            tidiedPoints[i] = Math2.perspectiveCorrection(tidiedPoints[i], grilleclone, width, height);
        }
        for(int i=0; i<boxesCenter.length; i++) {
            boxesCenter[i] = Math2.perspectiveCorrection(boxesCenter[i], grilleclone, width, height);
        }
    }
    
    /**
     * initialise le tableau de bouchon avec la liste de bouchons trouves
     */
    public void setCorksInGrid(ArrayList<Bouchon> bouchons) 
    {
        corksInGrid = new Bouchon[3][3];
        for(int i=0; i<corksInGrid.length; i++) {
            for(int j=0; j<corksInGrid[0].length; j++) {
                corksInGrid[i][j] = null;
                int off = i*3+j;
                int t = off+off/3;
                Point A = tidiedPoints[t];
                Point B = tidiedPoints[t+1];
                Point C = tidiedPoints[t+5];
                Point D = tidiedPoints[t+4];
                int w = Math.abs(B.x-D.x);
                int h = Math.abs(C.y-A.y);
                Point topleftcorner = new Point(C.x,A.y);
                Zone z = new Zone(topleftcorner,w,h);
                for(int k=0; k<bouchons.size(); k++) {
                    Point p = bouchons.get(k).getCenter();
                    if(Math2.isPointInZone(p,z)) corksInGrid[i][j] = bouchons.get(k);
                }
            }
        }
        
    }
    
    
}
