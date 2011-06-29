import java.awt.Point;

/**
 * La classe bouchon encapsule les donnees realtive a un bouchon :
 * position et taille
 */
public class Bouchon
{
    //private int xCenter;
    //private int yCenter;
    private Point center;
    private int oneDimCenter;
    private int width;
    private int height;
    //private int topLeft;
    //private int downRight;
    private int tableWidth;
    private boolean polarized = false;
    private PolarPoint point = null;
    private int diametre = 0;
    private boolean placed = false;
    private HColor color;
    
    public Bouchon(int oneDimCenter, int width, int height, int tableWidth, int color)
    {
        this.oneDimCenter = oneDimCenter;
        this.width = width;
        this.height = height;
        this.tableWidth = tableWidth;
        setColor(color);
        //this.topLeft = oneDimCenter - width/2 - tableWidth*height/2;
        //this.downRight = oneDimCenter + width/2 + tableWidth*height/2;
        int xCenter = oneDimCenter%tableWidth;
        int yCenter = oneDimCenter/tableWidth;
        center = new Point(xCenter,yCenter);
    }
    
    public int getWidth(){ return width; }
    public int getHeight(){ return height; }
    public Point getCenter(){ return center; }
    public int getX(){ return center.x; }
    public int getY(){ return center.y; }
    public int getOneDimCenter() { return oneDimCenter; }
    public HColor getColor() { return color; }
    public PolarPoint getPolarPoint() { return point; }
    public void setWidth(int width){ this.width = width; }
    public void setHeight(int height){ this.height = height; }
    public void setX(int x){ this.center.x = x; }
    public void setY(int y){ this.center.y = y; }
    public void setOneDimCenter(int center){ 
        this.oneDimCenter = center; 
        setCenter(new Point(center%width,center/width)); 
    }
    public void setCenter(Point center){ 
        this.center = center;
        setOneDimCenter( center.x+center.y*width );
    }
    
    public void correctPerspective(Grille grille, int height)
    {
        center = Math2.perspectiveCorrection(center, grille, tableWidth, height);
    }
    
    public void setPolar(Point origine, double mm_en_pix)
    {
        polarized = true;
        point = Math2.cartesianToPolar(center,origine,tableWidth,mm_en_pix);
        diametre = (int)((width+height)*mm_en_pix/2);
    }
    
    @Override public String toString() {
        return "bouchon: "+point+"  diametre: "+diametre+"mm";
    }
    
    public boolean isPlaced() { return placed; }
    
    private void setColor(int h)
    {
        if(h <= 40 || h >= 300) color = HColor.RED;
        else if(h > 40 && h <= 80) color = HColor.YELLOW;
        else if(h > 80 && h < 155) color = HColor.GREEN;
        else if(h >= 155 && h < 300) color = HColor.BLUE;
    }
    
}
