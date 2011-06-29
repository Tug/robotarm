import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.image.Raster;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.lang.ArrayIndexOutOfBoundsException;
import java.awt.image.MemoryImageSource;
import java.awt.Toolkit;
import java.awt.image.DataBufferInt;
import java.util.Random;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.awt.image.ColorModel;
import java.awt.Point;
import javax.naming.CannotProceedException;
import javax.naming.TimeLimitExceededException;

/**
 * La classe ImageAnalyser se charge d'analyser une image
 * Temps d'analyse : 1000 ms environ pour un pc moyen
 */
public class ImageAnalyser
{
    
    private Image maskImage;    // permet d'afficher dans la fenetre le traitement en cours sur le masque
    private BufferedImage buffImage;    // pour convertir l'Image en parametre du constructeur en BufferedImage
    private Graphics2D g2d;         // creer un contexte graphique pour dessiner l'image
    //private Shape circle;
    private int width;          // largeur de l'image
    private int height;         // hauteur de l'image
    private int[] data;         // tableau représentant l'image acquise floute de la webcam
    private int[] databak;      // tableau representant l'image original de la webcam
    private byte[] mask;        // tableau representant l'image binarise de la webcam
    private byte[] maskbak;     // copie de mask
    private boolean socleDetected = false;          // pour savoir si la detection du socle du bras a fonctionne
    private boolean grilleDetected = false;         // pour savoir si la grille a ete detecte
    private boolean moreThan5corksDetected = false; // pour savoir si plus de 5 bouchons ont ete detectes
    private Grille grille = null;   // objet grille representant la grille de morpion
    private Point bras;             // point representant la position du bras dans l'image
    private OrientedAngle socle;    // angle du socle par rapport a l'image
    private int cote_grille_pix = 0;    // taille d'un cote de la grile en pixel sur l'image
    private Point bras_coin = new Point(10,15);//new Point(95,71); // point representant la distance entre coin du socle et le bras en mm
    private static int borderSizeCropped = 2;   // largeur de la bordure d'image ignore
    private final static int minPixelObj = 15;  // taille minimum d'un objet en pixel
    private final static int margeDErreur = 4;  // constante de correction d'erreur
    private final static int cote_grille_en_mm = 150;   // taille reel d'un cote de la grille en mm
    private MemoryImageSource masksrc;      // permet d'actualiser rapidement et facilement la bufferedImage a partir de son tableau de pixel
    public static boolean expo = false;     // si true : filtre gamma expo si false : filtre gamma log 
    public static boolean blur5 = true, blur3 = false, blur7 = false;   // flou 5*5, 3*3, 7*7
    public static boolean clean = true;     // nettoyer l'image
    public static double houghThresh = 0.4;
    public static int[] bakparams = { -30, 42, -350 };
    private ArrayList<Bouchon> bouchons;    // liste de bouchons
    private ImageProcessor ip;
    
    /**
     * Constructeur :
     * On transforme notre Image en BufferedImage puis on recupere un tableau de pixel
     * celui-ci est floute pour ensuite etre binarise. Avec ce masque binaire, on cree un
     * MemoryImageSource permettant d'actualiser maskImage avec les octets du tableau mask
     * et donc d'afficher les traitements en cours sur l'image
     */
    public ImageAnalyser(Image image)
    {
        width = image.getWidth(null);
        height = image.getHeight(null);
        buffImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        g2d = (Graphics2D)buffImage.getGraphics();
        g2d.drawImage(image,0,0,null);
        //if(blur3) buffImage = Filter.blur(buffImage,3); // si le flou du tableau ne suffit pas
        //if(blur5) buffImage = Filter.blur(buffImage,5); // on peut choisir d'ajouter un de ces blur
        //if(blur7) buffImage = Filter.blur(buffImage,7); // plus lent mais simple
        // cree un pointeur direct vers les donnees de la bufferedImage
        data = ((DataBufferInt) buffImage.getRaster().getDataBuffer()).getData();
        databak = data.clone();   // fait une copie des donnes de l'image
        if(blur5) Filter.quickBlur5(data,width); // flou l'image 
        createMask();  // cree le masque binaire de l'image
        // on cree une image a partir de ce tableau de pixel
        masksrc = new MemoryImageSource(width,height,getGrayColorModel(),mask,0,width);
        masksrc.setAnimated(true);
        masksrc.setFullBufferUpdates(true);
        maskImage = Toolkit.getDefaultToolkit().createImage(masksrc);
    }
    
    public ImageAnalyser(Image image, ImageProcessor ip)
    {
        this(image);
        this.ip = ip;
    }
    
    public void show()
    {
        masksrc.newPixels();
        ip.refreshImage();   
    }
    
    /**
     * Permet de lancer l'analyse
     */
    public void analyse()
    {
        try {
            show();
            removeBorderObjectsAndFindSocle();
            show();
            findBouchons();
            show();
            grilleDetection();
            show();
            perspectiveCorrection();
            show();
            transformAllInPolar();
            
            moreThan5corksDetected = true;
            socleDetected = true;
            grilleDetected = true;
            
        } catch(NotEnoughCorksException nece) {
            moreThan5corksDetected = false;
            System.out.println("Pas assez de bouchons pour jouer");
        } catch(TimeLimitExceededException tlee) {
            socleDetected = false;
            grilleDetected = false;
        } catch(CannotProceedException cpe) {
            grilleDetected = false;
        } catch(Exception e){}
        
    }   
    
    /**
     * retourne true si toutes les operations de traitement ont fonctionne
     */
    public boolean isOk()
    {
        return (socleDetected && grilleDetected && moreThan5corksDetected);
    }
    
    /**
     * Renvoi le status des operations
     */
    public boolean[] getStatus()
    {
        return new boolean[] {socleDetected,  grilleDetected, moreThan5corksDetected};
    }
    
    /**
     * Trouver les bouchons sur l'image
     */
    public void findBouchons() throws NotEnoughCorksException
    {
        bouchons = new ArrayList<Bouchon>();
        for(int i=0; i < mask.length; i++)
        {
            if(mask[i] != RGBColor.xWHITE){
                Bouchon b = scanForACork(i);
                if(b != null) {
                    bouchons.add(b);
                    //clearZone(b.getTopLeft(),b.getDownRight());
                    System.out.println("x="+b.getX()+" y="+b.getY()+" w="+b.getWidth()+" h="+b.getHeight());
                    
                }
            }
        }
        if(bouchons.size() < 3) throw new NotEnoughCorksException();
    }
    
    /**
     * A partir du pixel p, premier pixel trouve appartenant probablement a un bouchon,
     * on commence par faire un scan en ricochet (scan rapide) si l'objet n'est pas assez gros on retourne null
     * Sinon on selectionne tous les pixels qui se touchent pour isoler l'objet grace a une dilatation, 
     * on traite ces pixels selectionnes pour determiner le centre, la largeur et la hauteur du bouchon
     * enfin on soustrait chaque pixel du bouchon au masque et on retourne l'objet bouchon trouve
     */
    private Bouchon scanForACork(int p)
    {
        Bouchon temp = bounceScan(p);
        if(temp == null) return null;
        // on commence par estimer les dimensions du bouchons
        // grace au scan en ricochet
        //Fonctions.afficheTab(mask,width,p-p%width-width,p+3*width);
        int majo_width = 0, majo_height = 0, mino_width = 0, mino_height = 0;
        int start = 0, relativp = 0, deltaHorSP = 0;
        if(mask[temp.getOneDimCenter()] == 0) {   
            mino_width = temp.getWidth()/2-margeDErreur;
            mino_height = temp.getHeight()/2-margeDErreur;
            majo_width = temp.getWidth() + 8*margeDErreur;
            majo_height = temp.getHeight() + 2*margeDErreur;
            deltaHorSP = (majo_width/2)-2*margeDErreur;
            start = p-2*width-deltaHorSP;
            relativp = temp.getOneDimCenter()-start;
            relativp = (relativp/width)*majo_width + relativp%width;
        } else {
            majo_width = 140;
            majo_height = 140;
            deltaHorSP = (majo_width/2)-2*margeDErreur;
            start = p-2*width-deltaHorSP;
            relativp = 2*majo_width+deltaHorSP;
        }
        
        if(deltaHorSP > start%width) start -= start%width;
        if(start%width+majo_width > width) majo_width = width - p%width;
        if(start/width+majo_height > height) majo_height = height - p/width;
        
        byte[] pMask = new byte[majo_width*majo_height];
        for(int i=0; i<majo_height; i++) {
            for(int j=0; j<majo_width; j++) {
                pMask[j+i*majo_width] = mask[start+i*width+j];
            }
        }
        //Fonctions.afficheTab(pMask,majo_width);
        byte[] oMask = new byte[pMask.length];
        for(int i=0; i<oMask.length; i++) oMask[i] = (byte)255;
        if(mino_width != 0 
           && mino_width+relativp%majo_width < majo_width 
           && mino_height+relativp/majo_width < majo_height ) {
            for(int i=-mino_height/2; i<mino_height/2; i++) {
                for(int j=-mino_width/2; j<mino_width/2; j++) {
                    oMask[relativp+i*majo_width+j] = 0;
                }
            }
        } else oMask[relativp] = 0;
        //Fonctions.afficheTab(oMask,majo_width);
        int nbBlackPix = Filter.AND(oMask,pMask);
        int bak = 0;
        while( bak != nbBlackPix )
        {
            bak = nbBlackPix;
            Filter.dilatation(oMask,majo_width);
            nbBlackPix = Filter.AND(oMask,pMask);
        }
        Filter.soustraction(mask,oMask,start,width,majo_width);
        Filter.dilatation(oMask,majo_width);
        Filter.soustraction(maskbak,oMask,start,width,majo_width);
        return createCork(oMask,majo_width,start);
    }
    
    /**
     * Cree un bouchon a partir de l'image binaire isole de celui-ci
     */
    private Bouchon createCork(byte[] tab, int w, int start)
    {
        //coord haut
        int h = tab.length/w;
        int up=0, down=0, left=0, right=0;
        int i=0;
        int lim1 = 0;
        int lim2 = 0;
        while((lim1==0 || lim2==0) && i<tab.length) {
            if(tab[i] == 0) lim1 = i;
            if(lim1 != 0 && tab[i] != 0) lim2 = i-1;
            i++;
        }
        up = lim1 + (lim2-lim1)/2;
        
        //coord bas
        i = tab.length-1;
        lim1 = 0;
        lim2 = 0;
        while((lim1==0 || lim2==0) && i>0) {
            if(tab[i] == 0) lim1 = i;
            if(lim1 != 0 && tab[i] != 0) lim2 = i+1;    
            i--;
        }
        down = lim2 + (lim1-lim2)/2;
        
        //coord gauche
        i = 0;
        lim1 = 0;
        lim2 = 0;
        int offset = 0;
        while((lim1 == 0 || lim2 == 0) && offset<w) {
            int j = i*w+offset;
            if(tab[j] == 0) lim1 = j;
            if(lim1 != 0 && tab[j] != 0) lim2 = j-w;
            if(i >= h-1) { offset++; i=0; }
            else i++;
        }
        left = lim1 + (lim2-lim1)/2;
        
        //coord droite
        i = tab.length-1;
        lim1 = 0;
        lim2 = 0;
        offset = 0;
        while((lim1==0 || lim2==0) && offset<w) {
            int j = i-offset;
            if(tab[j] == 0) lim1 = j;
            if(lim1 != 0 && tab[j] != 0) lim2 = j+w;
            if(i/w <= 0) { offset++; i=tab.length-1; }
            else i-=w;
        }
        right = lim2 + (lim1-lim2)/2;

        int h_b = Math.abs((down-up)/w);
        int w_b = Math.abs(right%w-left%w);
        int rCenterX = (down%w+up%w)/2;
        int rCenterY = (right+left)/(2*w);
        int c = start + rCenterX + width*rCenterY;
        int col = defineColor(c,w_b/2,h_b/2);
        return new Bouchon(c,w_b,h_b,width,col);
    }
    
    private int defineColor(int center, int w2, int h2)
    {
        int r=0,g=0,b=0;
        int nb = 0;
        for(int i=-w2/2; i<w2/2; i++) {
            for(int j=-h2/2; j<h2/2; j++) {
                int coul = data[i+j*width+center];
                r += (coul >> 16) & 0xFF;
                g += (coul >> 8) & 0xFF;
                b += coul & 0xFF;
                nb++;
            }
        }
        r /= nb; r &= 0xFF;
        g /= nb; g &= 0xFF;
        b /= nb; b &= 0xFF;
        float H = Filter.RGB_to_HSL(r,g,b)[0];
        return Math.round(H);
    }
    
    /**
     * Scan en ricochet, on parcours l'interieur de l'objet en rebondissant sur ces faces
     * Apres 4 rebonds on trouve une estimation des dimensions du bouchon
     */
    private Bouchon bounceScan(int p)
    {
        Bouchon b = null;
        int top = 0, right = 0, down = 0, left = 0, end = 0;
        boolean stop = false;
        boolean changeDir = true;
        int col = 0;
        int pbak = 0;
        int dp = 0;
        
        while(!stop)
        {
            try {
                if(mask[p] == 0) {
                    if(changeDir) {
                        switch (col) {
                            case 0: dp = width+1; top = p; break;
                            case 1: dp = width-1; right = p; break;
                            case 2: dp = -width-1; down = p; break;
                            case 3: dp = -width+1; left = p; break;
                            default: stop = true; end = p; break;
                        }
                        col++;
                        changeDir = false;
                    }
                    pbak = p;
                    p = p + dp;
                } else {
                    p = pbak;
                    changeDir = true;
                }
            } catch(ArrayIndexOutOfBoundsException aioobe) {
                aioobe.printStackTrace();
                System.err.println("Erreur, detection d'objet dans les limites de l'image");
                return null;
            }
        }
        down += width*margeDErreur;
        right += margeDErreur;
        left -= margeDErreur;
        int h = (down - top)/width;
        int w = right%width - left%width;
        int center = (top+down)/2;
        if( h <= minPixelObj || w <= minPixelObj ) {
            return null;
        } else {
            return new Bouchon(center, w, h, width,0);
        }
    }

    /**
     * Dessine les centres des bouchons sur l'image
     */
    public void drawCenters()
    {
        for(int i = 0; i < bouchons.size(); i++)
        {
            data[bouchons.get(i).getOneDimCenter()] = RGBColor.xBLACK;
        }
    }
    
    /**
     * Actualise le masque bianire a partir de son tableau de pixel et renvoi ce masque
     */
    public Image getAnalysedImage()
    {
        masksrc.newPixels();
        return maskImage;
    }

    /**
     * Binarise l'image couleur
     */
    private void createMask()
    {
        mask = new byte[data.length];
        Filter.makeMask(data,mask,expo);
        if(clean) Filter.cleanPixels(mask,width);
        Filter.clearBorders(mask,width);
        maskbak = new byte[data.length];
        Filter.makeMask(databak, maskbak, bakparams[0], bakparams[1], bakparams[2]);
        Filter.clearBorders(maskbak,width);
    }
    
    /**
     * Premier essai de detection de la grille grace a des dessins successifs de cercles
     * en prenant pour centre le premier coin de la grille trouvee et en agrandissant le cercle
     * jusqu'a ce qu'il ne coupe plus la grille. Le rayon du cercle est alors la diagonale du carre
     * Plus utilise
     */
    public void detectTableauDeJeu()
    {
        int nbPixTouched = 1;
        int rayon = 0;
        int top = 0;
        int down = mask.length-1;
        while(top < mask.length){
            if(mask[top] != RGBColor.xWHITE) break;
            top++;
        }
        while(down > 0){
            if(mask[down] != RGBColor.xWHITE) break;
            down--;
        }
        while(nbPixTouched != 0)
        {
            //nbPixTouched = drawCircle(mask,rayon++,top,Math.PI);
        }
        int dy = (down-top)/width;
        int dx = Math.abs(down%width-top%width);
        int rayon2 = (int) Math.sqrt( dx*dx + dy*dy );
        if(rayon > 1) {
            System.out.println(rayon + "  ,  " + rayon2);
        }
    }
    
    /**
     * Cree un ColorModel en niveau de gris pour creer notre MemoryImageSource
     */
    public static ColorModel getGrayColorModel()
    {
        byte[] g = new byte[256];
        for (int i = 0; i < 256; i++) g[i] = (byte)i;
        return new IndexColorModel(8, 256, g, g, g);
    }

    
    /**
     * retourne le tableau representant l'image webcam floute
     */
    public int[] getData() { return data; }
    public byte[] getMaskData() { return mask; }
    
    /**
     * fonction permerttant de trouver les objets collés aux bords
     * On cree un marqueur dont on ne rempli que les bords, on fait un AND avec notre masque
     * et on fait grandir le marqueur par dilatation puis on refait un AND a chaque fois
     * jusqu'a ce que le nombre de bit rempli du marqueur ne grandisse plus. On
     * a alors tous les objets qui etaient colé aux zones rempli du marqueur au depart :
     * ici les bords.
     * puis selection de celle avec 1 angle droit, sauvegarde des coordonnees et  
     * de l'angle puis suppression de toutes ces formes
     */
    public void removeBorderObjectsAndFindSocle() throws TimeLimitExceededException, CannotProceedException
    {
        final int div = 4;
        int smallWidth = (width-4)/div;
        int smallHeight = (height-4)/div;
        byte[] smallMask = new byte[smallWidth*smallHeight];
        for(int i=0; i<smallHeight; i++) {
            for(int j=0; j<smallWidth; j++) {
                smallMask[i*smallWidth+j] = (byte)mask[((i*div+2)*width+(j*div+2))];
            }
        }
        byte[] marqueur = new byte[smallMask.length];
        Timer timer = new Timer();
        timer.start();
        for(int i=0; i<marqueur.length; i++) marqueur[i] = (byte)255;
        Filter.addSquare(marqueur, smallWidth, 0);
        int nbBlackPix = Filter.AND(marqueur,smallMask);
        int bak = 0;
        long t1sec = 1000000000;
        while( bak != nbBlackPix ) {// && !timer.moreThan(t1sec) ) {
            bak = nbBlackPix;
            Filter.dilatation(marqueur,smallWidth);
            nbBlackPix = Filter.AND(marqueur,smallMask);
        }
        //Filter.soustraction(smallMask,marqueur);
        Filter.dilatation(marqueur,smallWidth);
        /*
        ArrayList<ParametricFunction> list = Filter.HoughTransform(marqueur,smallWidth,0.4);
        ArrayList<OrientedAngle> angleList = new ArrayList<OrientedAngle>();
        for(int i=0; i<list.size(); i++) list.get(i).multR(2);
        for(int i=0; i<list.size(); i++) {
            for(int j=i+1; j<list.size(); j++) {
                Point p2 = Math2.intersecInPlan(list.get(i),list.get(j),new Point(width/2,height/2));
                if(p2 != null) {
                    OrientedAngle ora = new OrientedAngle(p2,list.get(i),list.get(j));
                    if(ora.isAlmostOrthogonal()) angleList.add(ora);
                }
            }
        }
        Point centerImage = new Point(width/2,height/2);
        int min = 16*width; // infini
        for(int i=0; i<angleList.size(); i++) {
            int temp = Math2.distanceBetween(centerImage,angleList.get(i).getVertex());
            if(temp < min) {
                min = temp;
                socle = angleList.get(i);
                socle.drawSocle(mask,width);
            }
        }
        if( socle != null ) {
            Point cornerPoint = socle.getVertex();
            int c = cornerPoint.x + cornerPoint.y*width;
            Filter.drawCross(mask,c,width);
            socleDetected = true;
        }
        Filter.clearBorders(mask,width);
        */
        int corner = Filter.escargot(marqueur,smallWidth);
        Point corn = new Point(corner%smallWidth,corner/smallWidth);
        int corn2 = div*(corner%smallWidth)+2+width*(div*corner/smallWidth+2);
        Filter.drawCross(mask,corn2,width);
        show();
        socle = Filter.specialCircle(corn,marqueur,smallWidth);
        if(socle == null) throw new CannotProceedException();
        socle.multBy(div);
        Filter.fermeture(marqueur,smallWidth,10/div);
        Filter.dilatation(marqueur,smallWidth,30/div);
        for(int i=2; i<height-2; i++) {
            for(int j=2; j<width-2; j++) {
                if(marqueur[((i-2)/div)*smallWidth+(j-2)/div] == 0) {
                    mask[i*width+j] = (byte)255;
                    maskbak[i*width+j] = (byte)255;
                }
            }
        }
    }
    
    /**
     * A ce niveau on a deja enleve les objets colles aux bords et les bouchons
     * Effectue une transformee de hough sur cette image ou il ne reste a priori plus que la grille
     */
    public void grilleDetection() throws CannotProceedException, TimeLimitExceededException
    {
        Filter.remplacer(mask,maskbak);
        ArrayList<ParametricFunction> list = Filter.HoughTransform(maskbak,width,houghThresh);
        for(int i=0; i<list.size(); i++) {
            Filter.drawLineCenter(mask,width,list.get(i));
            System.out.println(list.get(i));
        }
        show();
        grille = new Grille(list,new Point(width/2,height/2));
        grilleDetected = true;
        Point[] points = grille.getTidiedPoints();
        for(int i=0; i<points.length; i++) {
            System.out.println(points[i]);
            Filter.drawCross(mask, points[i].x + points[i].y*width,width);
        }
        points = grille.getBoxesCenter();
        for(int i=0; i<points.length; i++) {
            Filter.drawCross(mask, points[i].x + points[i].y*width,width);
        }
        show();
        cote_grille_pix = grille.getSideLength();
        grille.setCorksInGrid(bouchons);
    }
    
    /**
     * Une fois tous les traitements termines, on utilise la forme de la grille 
     * pour determiner une correction de la perspective
     */
    public void perspectiveCorrection()
    {
        for(int i=0; i < bouchons.size(); i++) {
            bouchons.get(i).correctPerspective(grille, height);
        }
        grille.correctGrille(width,height);
        socle.correctPerspective(grille, width, height);
        Point[] grillecor = grille.getTidiedPoints();
        for(int i=0; i<grillecor.length; i++) Filter.drawCross(mask,grillecor[i].x+grillecor[i].y*width,width);
    }

    /**
     * Transformation de toutes les donnes cartesiennes (unité pixel) en
     * donnees polaires (unite mm et radian)
     */
    public void transformAllInPolar()
    {
        // nombre de pixel pour 1 mm
        double mm_en_pix = (double)cote_grille_pix/cote_grille_en_mm;
        Point bras = socle.getPointInside(bras_coin,mm_en_pix);
        for(int i=0; i < bouchons.size(); i++) {
            bouchons.get(i).setPolar(bras,mm_en_pix);
            System.out.println(bouchons.get(i));
        }
        grille.setBoxesCenterInmm(bras,mm_en_pix,width);
        perspectiveCorrectionBras(mm_en_pix);
    }
    
    public void perspectiveCorrectionBras(double mm_en_pix)
    {
        Point coin = socle.getVertex();
        bras = socle.getBras(bras_coin,mm_en_pix);
        Filter.drawCross(mask,coin.x+coin.y*width,width);
        Filter.drawCross(mask,bras.x+bras.y*width,width);
    }
    
    public ArrayList<Bouchon> getBouchons() { return bouchons; }
    public Grille getGrid() { return grille; }
}
