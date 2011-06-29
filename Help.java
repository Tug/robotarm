/**
 * Contient des explication sur le programme et les codes rejetes
 */

/**
 * Le tableau de pixels :
 * 
 * w = width;
 * t = tab.length;
 * __________________________________________________________
 * |  0  |  1  |  2  |  3  |  ...   | w-4 | w-3 | w-2 | w-1 |
 * ----------------------------------------------------------
 * |  w  | w+1 | w+2 | w+3 |  ...   |2w-4 |2w-3 |2w-2 |2w-1 |
 * ----------------------------------------------------------
 * | 2w  |2w+1 |2w+2 |2w+3 |  ...   |3w-4 |3w-3 |3w-2 |3w-1 |
 * ----------------------------------------------------------
 * | 3w  |3w+1 |   ..........................   |4w-2 |4w-1 |
 * ----------------------------------------------------------
 *   .      .                                     .     .
 *   .      .                                     .     .
 *   .      .                                     .     .
 * ----------------------------------------------------------
 * |t-3w|t-3w+1|   .........................  |t-2w-2|t-2w-1|
 * ----------------------------------------------------------
 * |t-2w|t-2w+1|t-2w+2|t-2w+3|  ... |t-w-4|t-w-3|t-w-2|t-w-1|
 * ----------------------------------------------------------
 * |t-w |t-w+1 |t-w+2 |t-w+3 |  ... | t-4 | t-3 | t-2 | t-1 |
 * ----------------------------------------------------------
 * 
 * !!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!
 * !! NE JAMAIS COMPARER UN byte A 255  !!
 * !! AU PIRE -1 OU (byte)255           !!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 */
public final class Help
{
    public final static boolean help = false;

}

/*
 * CODE JETE
 */

        /*
        Point A = new Point(0,10000);
        for(int i=0; i<points.size(); i++) {
            Point p = points.get(i);
            points.remove(i);
            if(p.y < A.y) A = p;
        }
        Point B = new Point(0,0);
        for(int i=0; i<points.size(); i++) {
            Point p = points.get(i);
            points.remove(i);
            if(p.x > B.x) B = p;
        }
        Point C = new Point(0,0);
        for(int i=0; i<points.size(); i++) {
            Point p = points.get(i);
            points.remove(i);
            if(p.y > C.y) C = p;
        }
        Point D = new Point(10000,0);
        for(int i=0; i<points.size(); i++) {
            Point p = points.get(i);
            points.remove(i);
            if(p.x < D.x) D = p;
        }
        tidiedPoints[0] = A;
        tidiedPoints[3] = B;
        tidiedPoints[15] = C;
        tidiedPoints[12] = D;
        this.centerGrille = new Point((B.x+D.x)/2, (A.y+C.y)/2);
        
        Point un = null, deux = null;
        for(int i=0; i<points.size(); i++) {
            Point p = points.get(i);
            points.remove(i);
            if(p.x >= A.x && p.y <= B.y) {
                if(un != null) deux = p;
                else un = p;
            }
        }
        tidiedPoints[1] = un;
        tidiedPoints[2] = deux;
        un = null; deux = null;
        for(int i=0; i<points.size(); i++) {
            Point p = points.get(i);
            points.remove(i);
            if(p.x <= A.x && p.y <= D.y) {
                if(un != null) deux = p;
                else un = p;
            }
        }
        tidiedPoints[4] = un;
        tidiedPoints[8] = deux;
        */
