import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.util.ArrayList;
import java.awt.Point;
import javax.naming.TimeLimitExceededException;
import javax.naming.CannotProceedException;

/**
 * La classe Filter contient des methodes static utiles pour le traitement d'un tableau de pixels :
 * filtres, transformees, detection de contour, morphologie mathematiques, ...
 */
public class Filter
{
    public static int[] params = { 200, 1, 0, 1040};
    public static float t = 0.75f;
    
    /**
     * Transformation d'une BufferedImage en utilisant la classe ConvolveOp
     * On applique a l'image une matrice de convolution
     * Flou, contrast, netteté, detection de contour, binarisation, posteriser
     * Ces fonctions ne sont plus utilisés durant le traitement
     */
    
    public static BufferedImage blur(BufferedImage bi, int n)
    {
        int n2 = n*n;
        float coef = 1.0f/n2;
        float[] kernel = new float[n2];
        for(int i=0; i<n2; i++) kernel[i] = coef;
        BufferedImageOp blurOp = new ConvolveOp(new Kernel(n, n, kernel));
        return blurOp.filter(bi, null);
    }
    
    public static BufferedImage contrast(BufferedImage bi)
    {
        float coef = 0.165f;
        float[] kernel = {coef, coef, coef, coef, coef, coef, coef, coef, coef };
        BufferedImageOp blurOp = new ConvolveOp(new Kernel(3, 3, kernel));
        return blurOp.filter(bi, null);
    }
    
    public static BufferedImage sharpen(BufferedImage bi) 
    {
        float elements[] = { -1.0f, -1.0f, -1.0f, -1.0f, 9.0f, -1.0f, -1.0f, -1.0f,-1.0f };
        ConvolveOp convolve = new ConvolveOp(new Kernel(3, 3, elements));
        return convolve.filter(bi, null);
    }

    
    public static BufferedImage edgeDetect(BufferedImage bi) 
    {
        float elements[] = { 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,-1.0f };
        ConvolveOp convolve = new ConvolveOp(new Kernel(3, 3, elements));
        return convolve.filter(bi, null);
    }
    
    public static BufferedImage posterize(BufferedImage bi) 
    {
        short[] post = new short[256];
        for (int i = 0; i < 256; i++)
        post[i] = (short)(i - (i % 32));
        BufferedImageOp posterizeOp = new LookupOp(new ShortLookupTable(0, post), null);
        return posterizeOp.filter(bi, null);
    }
    
    // valeur possible pour threshold = [0, 255] exemple : 64, 128,192
    public static BufferedImage threshold(BufferedImage bi, int threshold) 
    {
        int minimum  = 0;
        int maximum = 255;
        short[] thresholdArray = new short[256];
        for (int i = 0; i < 256; i++) 
        {
            if (i < threshold) thresholdArray[i] = (short)minimum;
            else thresholdArray[i] = (short)maximum;
        }
        BufferedImageOp thresholdOp = new LookupOp(new ShortLookupTable(0, thresholdArray), null);
        return thresholdOp.filter(bi, null);
    }
    
    /**
     * Algorithme cree par : Bradley, D., Roth, G.
     * Trouve sur : http://iit-iti.nrc-cnrc.gc.ca/publications/nrc-48816_f.html
     * Temps d'execution de l'ordre de plusieurs secondes => trop long
     * Cette algorithme n'est pas utilise durant le traitement
     */
    public static byte[] adaptativeThreshold(byte[] src, int width, int height)
    {
        int s = width/16;
        long sum;
        byte[] dst = new byte[src.length];
        long[] intImg = new long[width*height];
        for(int i=0; i<width; i++) {
            sum = 0;
            for(int j=0; j<height; j++) {
                int index = i+j*width;
                sum += src[index];
                if(i==0) intImg[index] = sum;
                else intImg[index] = intImg[index-1] + sum;
            }
        }
        for(int i=0; i<width; i++) {
            for(int j=0; j<height; j++) {
                int index = i+j*width;
                int x1 = i - s/2;
                int x2 = i + s/2;
                int y1 = j - s/2;
                int y2 = j + s/2;
                if (x1 < 0) x1 = 0;
                if (x2 >= width) x2 = width-1;
                if (y1 < 0) y1 = 0;
                if (y2 >= height) y2 = height-1;
                int count = (x2-x1)*(y2-y1);
                // I(x,y)=s(x2,y2)-s(x1,y2)-s(x2,y1)+s(x1,x1)
                sum = intImg[x2+y2*width] - intImg[x2+y1*width]
                          - intImg[x1+y2*width] + intImg[x1+y1*width];
                if(src[index]*count < sum*t) dst[index] = (byte)255;
                else dst[index] = 0;
            }
        }
        return dst;
    }
    
    /**
     * Correction gamma logarithmique
     * Cette correction a ete incluse dans makeMask afin de rendre le traitement plus rapide
     * valeur possible pour logscale, exemple : 80,
     * valeur possible pour offsetval, exemple : -160
     */
    public static void apply_log(int [] src_1d,  int width, int height, double logscale, float offsetval) 
    {
        int d_w = width;
        int d_h = height;
        int src_rgb;
        int result = 0; 
        for(int i = 0; i < src_1d.length; i++) 
        {
            //Select required bits from image
            src_rgb = src_1d[i] & 0x000000ff;
            //Perform operation
            result = (int) (logscale * Math.log(src_rgb + 1.0));
            result = (int) (result + offsetval);
            if (result > 255) result = 255;
            if (result < 0) result = 0;
            src_1d[i] = 0xff000000 | result << 16 | result << 8 | result;
        }
    }
  
    /**
     * Cree un masque ie binarise une image couleur 
     * dont chaque pixel est code sur un int : AARRGGBB (alpha red green blue)
     * Le tableau retourne represente une image de meme taille binarise dont chaque pixel est code
     * sur un byte (0 a 255) et peu prendre la valeur 0 (noir) ou 255 (blanc)
     */
    public static void makeMask(int[] src, byte[] dst, boolean expo)
    {
        int thresh = params[0];
        int scale = params[1];
        int offsetval = params[2];
        float gammaexpo = params[3]/1000f;
        
        //System.out.println(" t : " + thresh + "  l : " + logscale + "  o : " + offsetval);
        for(int i=0; i<src.length; i++)
        {
            int color = src[i];
            int r = (color >> 16) & 0xFF;
            int v = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            //niveau de gris =  0.11*rouge + 0.59*vert + 0.3*bleu
            color = (r*76 + v*151 + b*28)/256;
            //grayTab[i] = (byte) color;
            // correction gamma en log
            if(expo) color = (int) (scale * Math.pow(gammaexpo,color));
            else color = (int) (scale * Math.log(color + 1));
            color = (int) (color + offsetval);
            if(color>=thresh) dst[i] = (byte)255;
            else dst[i] = 0;
        }
    }
    
    /**
     * Cree un masque de la meme facon que makeMask(int[],byte[],boolean)
     * sauf que les parametres de correction gamma (scalc et offstc) et le niveau de threshold
     * est passe en parametre
     */
    public static void makeMask(int[] src, byte[] dst, int thrc, int scalc, int offstc)
    {  
        int thresh = params[0] + thrc;
        int scale = params[1] + scalc;
        int offset = params[2] + offstc;
        //System.out.println(" t : " + thresh + "  l : " + logscale + "  o : " + offsetval);
        for(int i=0; i<src.length; i++)
        {
            int color = src[i];
            int r = (color >> 16) & 0xFF;
            int v = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            //niveau de gris =  0.11*rouge + 0.59*vert + 0.3*bleu
            color = (r*76 + v*151 + b*28)/256;
            //grayTab[i] = (byte) color;
            // correction gamma en log
            color = (int) (scale * Math.log(color + 1));
            color = (int) (color + offset);
            if(color >= thresh) dst[i] = (byte)255;
            else dst[i] = 0;
        }
    }
  
    /**
     * Effectue un seuillage (thresholding) d'une image couleur 
     * represente par son tableau de pixel. Le tableau passe en parametre
     * est remplace par celuid de l'image seuillé
     * valeur possible pour tresh = [0,255], exemple : 180
     * Utilise dans makeGray()
     */
    public static void threshold(int[] src, int width, int height, int thresh) 
    {
        int i_w = width;
        int i_h = height;
        for (int i=0; i<src.length; i++) 
        {
            int blue = src[i] & 0x000000ff; 
            src[i] = (blue>=thresh)?0xffffffff:0xff000000;
        }
    }

    /**
     * Algorithme de flou simple :
     * Pour chaque pixel, on effectue la moyenne des 8 pixels l'entourant + lui
     * Plus utilise
     */
    public static void blur(int[] T, int width, int n)
    {
        int[] tab = T.clone();
        final int n1 = n/2;
        final int n2 = n*n;
        for(int i=n1*width+n1; i<T.length-n1*width-n1; i++) {
            int somR = 0, somG = 0, somB = 0;
            for(int j=-n1*width; j<=n1*width; j+=width) {
                for(int k=-n1; k<=n1; k++) {
                    somR += (tab[i+k+j]  >> 16);
                    somG += (tab[i+k+j] >> 8) & 0xff;
                    somB += tab[i+k+j] & 0xff;
                }
            }
            T[i] = (somR/n2) << 16 | (somG/n2) << 8 | (somB/n2);
        }
    }
    
    /**
     * Algorithme de flou rapide :
     * Meme chose que pour le flou simple sauf que deux des 3 colonnes necessaires
     * a la moyenne d'un pixel sont utilises pour faire la moyenne du pixel suivant
     * On passe de 120ms (pour le flou simple) a 55ms
     * Il est meme plus rapide que le flou d'une bufferedImage effectue grace a ConvolveOp (77ms)
     */
    public static void quickBlur5(int[] T, int width)
    {
        int[] tab = T.clone();
        // initialisation
        int xR1 = 0, xR2 = 0, xR3 = 0, xR4 = 0, xR5 = 0;     
        int xG1 = 0, xG2 = 0, xG3 = 0, xG4 = 0, xG5 = 0;  
        int xB1 = 0, xB2 = 0, xB3 = 0, xB4 = 0, xB5 = 0;
        for(int i=0; i<5; i++) {
            int offset = i*width;
            xR1 += (tab[offset] >> 16);
            xR2 += (tab[offset+1] >> 16);
            xR3 += (tab[offset+2] >> 16);
            xR4 += (tab[offset+3] >> 16);
            xR5 += (tab[offset+4] >> 16);
            xG1 += (tab[offset] >> 8) & 0xff;
            xG2 += (tab[offset+1] >> 8) & 0xff;
            xG3 += (tab[offset+2] >> 8) & 0xff;
            xG4 += (tab[offset+3] >> 8) & 0xff;
            xG5 += (tab[offset+4] >> 8) & 0xff;
            xB1 += (tab[offset] & 0xff);
            xB2 += (tab[offset+1] & 0xff);
            xB3 += (tab[offset+2] & 0xff);
            xB4 += (tab[offset+3] & 0xff);
            xB5 += (tab[offset+4] & 0xff);
        }
        
        for(int i=2*width+2; i < tab.length-2*width-3; i++) {
            int moyR = (xR1 + xR2 + xR3 + xR4 + xR5)/25;
            int moyG = (xG1 + xG2 + xG3 + xG4 + xG5)/25;
            int moyB = (xB1 + xB2 + xB3 + xB4 + xB5)/25;
            T[i] = moyR << 16 | moyG << 8 | moyB;
            xR1 = xR2; xR2 = xR3; xR3 = xR4; xR4 = xR5; xR5 = 0;
            xG1 = xG2; xG2 = xG3; xG3 = xG4; xG4 = xG5; xG5 = 0;
            xB1 = xB2; xB2 = xB3; xB3 = xB4; xB4 = xB5; xB5 = 0;
            for(int j=-2; j<=2; j++) xR5 += tab[i+3+j*width] >> 16;
            for(int j=-2; j<=2; j++) xG5 += (tab[i+3+j*width] >> 8) & 0xff;
            for(int j=-2; j<=2; j++) xB5 += (tab[i+3+j*width] & 0xff);
        }
    }
    
    /**
     * Supprime les pixels isolés dans un image binarise
     * Sorte de flou pour image binaire
     */
    public static void cleanPixels(byte[] T, int width)
    {
        for(int i=width+1; i<T.length-width-1; i++) {
            int blackPix = 0;
            //if( T[i-width-1] == RGBColor.xBLACK ) blackPix++;
            if( T[ i-width ] == 0 ) blackPix++;
            //if( T[i-width+1] == RGBColor.xBLACK ) blackPix++;
            if( T[   i-1   ] == 0 ) blackPix++;
            if( T[   i+1   ] == 0 ) blackPix++;
            //if( T[i+width-1] == RGBColor.xBLACK ) blackPix++;
            if( T[ i+width ] == 0 ) blackPix++;
            //if( T[i+width+1] == RGBColor.xBLACK ) blackPix++;
            if( T[i] == 255 ) // si pixel blanc entoure de 3 pixels noir
            if( blackPix >= 2 ) T[i] = 0; // alors  mettre le pixel en noir
            else if( blackPix <= 2 ) T[i] = (byte)255; // si pixel noir entoure de 3 pixels blanc mettre en blanc
        }
    }
    
    /**
     * Cree une image en niveau de gris a partir d'une image couleur
     * On effectue une moyenne ponderee des composantes couleurs de chaque pixel
     * Plus utilise
     */
    public static void makeGray(int[] src, byte[] dst)
    {
        for(int i=0; i<src.length; i++) {
            int color = src[i];
            int r = (color >> 16) & 0xFF;
            int v = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            //niveau de gris =  0.11*rouge + 0.59*vert + 0.3*bleu
            dst[i] = (byte)((r*76 + v*151 + b*28)/256);
        }
    }
    
    /**
     * Parcours les pixels depuis le centre de l'image, en spiral jusqu'a
     * tombé sur le premier pixel noir
     */
    public static int escargot(byte[] tab, int width)
    {
        int correc = 2;
        int height = tab.length/width;
        int center = (height/2)*width+width/2;
        int r = 0;
        int l = 0;
        int t = 0;
        int b = 0;
        int p = 0;
        while(r-l < width-1 && (b-t)/width < height-1)
        {
            for(int i=l; i<=r; i++) {
                p = center+i+b;
                if(tab[p] == 0) return p+correc;
            }
            l--;
            for(int i=b; i>=t; i-=width) {
                p = center+i+r;
                if(tab[p] == 0) return p-correc*width;
            }
            b+=width;
            for(int i=r; i>=l; i--) {
                p = center+i+t;
                if(tab[p] == 0) return p-correc;
            }
            r++;
            for(int i=t; i<=b; i+=width) {
                p = center+i+l;
                if(tab[p] == 0) return p+correc*width;
            }
            t-=width;
        }
        return 0;
    }
    
    /**
     * Comme escargot mais en plus simple avec un cercle
     */
    public static int escargot2(byte[] tab, int width) throws CannotProceedException
    {
        double angleTotal = 2*Math.PI;
        int rayon = 1;
        int height = tab.length/width;
        int p = (height/2)*width+width/2;
        while(rayon < width/2) {
            int nombreTotalDePixels = (int)Math.round(angleTotal*rayon);
            boolean isBlackCrossed = false;
            for(double angle = 0.0; angle < angleTotal; angle +=(angleTotal/nombreTotalDePixels)) 
            {
                int x1 = (int) Math.round(rayon*Math.cos(angle));
                int y1 = (int) Math.round(rayon*Math.sin(angle));
                if(!overflowCircle(p,x1,width)) {
                    try {
                        int pix = -y1*width+x1+p;
                        if(tab[pix] == 0) return pix;
                    } catch(ArrayIndexOutOfBoundsException aioobe){}
                }
            }
            rayon++;
        }
        throw new CannotProceedException();
    }
    
    /**
     * fait un AND sur T1, et renvoi le nombre de pixels  noir (0)
     */
    public static int AND(byte[] T1, byte[] T2)
    {
        if(T1.length != T2.length) return 0;
        int nb = 0;
        for(int i=0; i<T1.length; i++)
        {
            if(T2[i] != 0) { //T2 blanc
                T1[i] = (byte)255;  // =>T1 blanc
            } else if(T1[i] == 0) nb++;
        }
        return nb;
        
    }
    

    /**
     * Algorithme de morphologie mathematique : dilatation
     * Cela consiste a dilater un objet (le rendre plus gros)
     * Pour cela on utilise un objet structurant carre 3*3
     * Pour chaque pixel, si il est blanc on test les 8 pixels
     * qui l'entourent si un de ceux-ci est noir alors on met 
     * ce pixel a noir (dans un autre tableau). Si il est deja noir,
     * on ne fait rien.
     */
    public static void dilatation(byte[] tab, int width)
    {
        final int size_eStruc = 3;  // element structurant 3*3
        final int d = size_eStruc/2;
        byte[] tab2 = tab.clone();
        int height = tab2.length/width;
        for(int i=0; i<tab.length; i++) {    //9*n acces max
            if(tab2[i] != 0) { // si pixel blanc
                int x = i%width;
                int y = i/width;
                flag :
                for(int g=-d; g<=d; g++) {
                    for(int h=-d; h<=d; h++) {
                        if(x+g>=0 && x+g<width && y+h>=0 && y+h<height) {
                            if(tab2[x+g+(y+h)*width]==0) {    // si ce pixel est entouré d'un pixel noir
                                tab[i]=0;                       // alors on met ce pixel a noir
                                break flag;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Algorithme de morphologie mathematique : erosion
     * Cela consiste a eroder un objet (enlever de l'epaisseur)
     * C'est le contraire de la dilataion :
     * Pour cela on utilise un objet structurant carre 3*3
     * Pour chaque pixel, si il est noir on test les 8 pixels
     * qui l'entourent si un de ceux-ci est blanc alors on met 
     * ce pixel a blan (dans un autre tableau). Si il est deja blanc,
     * on ne fait rien.
     * Plus utilise
     */
    public static void erosion(byte[] tab, int width)
    {
        byte[] tab2 = tab.clone();
        int height = tab2.length/width;
        for(int i=0; i<tab2.length; i++) {
            if(tab2[i] == 0) { // si pixel noir
                int x = i%width;
                int y = i/width;
                flag :
                for(int g=-1; g<2; g++) {
                    for(int h=-1; h<2; h++) {
                        if(x+g>=0 && x+g<width && y+h>=0 && y+h<height) {
                            if(tab2[x+g+(y+h)*width]!=0) {    // si ce pixel est entouré d'un pixel blanc
                                tab[i]=(byte)255;                       // alors on met ce pixel a blanc
                                break flag;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Algorithme de morphologie mathematique : fermeture
     * Cela consiste a dilater un objet (le rendre plus gros)
     * puis l'eroder. L'utilité de cette algorithme est de relier 
     * les objets proches en un seul ou de combler les trous d'un objet
     * Plus utilise
     */
    public static void fermeture(byte[] tab, int width, int nb)
    {
        for(int i=0; i<nb; i++) dilatation(tab,width);
        for(int i=0; i<nb; i++) erosion(tab,width);
    }
    
    /**
     * Algorithme de morphologie mathematique : ouverture
     * Operation inverse de la fermeture : erosion puis dilatation
     * L'utilité de cette algorithme est de separer les faibles liaisons entre
     * les objets ou de supprimer les petits objets.
     * Plus utilise
     */
    public static void ouverture(byte[] tab, int width, int nb)
    {
        for(int i=0; i<nb; i++) erosion(tab,width);
        for(int i=0; i<nb; i++) dilatation(tab,width);
    }
    
    public static void dilatation(byte[] tab, int width, int nb)
    {
        for(int i=0; i<nb; i++) dilatation(tab,width);
    }
    
    /**
     * Permet de soustraire 2 tableaux d'images binaires.
     * noir - noir = blanc
     * noir - blanc = noir
     * blanc - X = blanc
     * Le parametre start permet de donner la position du tableau soustrayant (point en haut a gauche)
     * Tres utile pour gagner en rapidite
     */
    public static void soustraction(byte[] tab1, byte[] tab2, int start, int width1, int width2)
    {
        int height2 = tab2.length/width2;
        for(int i=0; i<height2; i++) {
            for(int j=0; j<width2; j++) {
                if(tab2[i*width2+j] == 0 && tab1[i*width1+j+start] == 0) tab1[i*width1+j+start] = (byte)255;
            }
        }
    }
    
    /**
     * Remplace chaque pixel de tab1 par les pixels de tab2
     * Ceci permet de ne pas modifier la valeur des pointeurs.
     * Utile dans ImageAnalyser ou on passe en parametre de MemoryImageSource
     * le pointeur de tableau de pixel
     */
    public static void remplacer(byte[] tab1, byte[] tab2)
    {
        for(int i=0; i<tab1.length; i++) tab1[i] = tab2[i];   
    }
    
    /**
     * Fonction de rotation d'une image, pas terminee !!!!!!!!!!!!
     * L'idee y est
     * Non utilise
     */
    public static void rotate(byte[] tab, int width, float angle)
    {
        byte[] tab2 = new byte[(int)(tab.length*Math.sqrt(2))];
        int width2 = 2;
        int height2 = 2;
        int height = tab.length/width;
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                int nx = (int)((x - halfWidth) * cos + (y - halfHeight) * sin + halfWidth);
                int ny = (int)(-(x - halfWidth) * sin + (y - halfHeight) * cos + halfHeight);
                if (nx >= 0 && ny >= 0 && nx < width2 && ny < height2)
                    tab2[y * width2 + x] = tab[ny * width + nx];
                else tab2[y * width2 + x] = (byte)255;
            }
        }
    }
  
    /**
     * 
     */
    public static void addSquare(byte[] tab, int width, int offset)
    {
        int height = tab.length/width;
        for(int i=offset*width+offset; i<=(offset+1)*(width-1); i++) tab[i] = 0;
        for(int i=(offset+1)*width+offset; i<=(height-offset-1)*width; i+=width) tab[i] = 0;
        for(int i=(offset+2)*width-(offset+1); i<=tab.length-(offset+1)*(width-1); i+=width) tab[i] = 0;
        for(int i=tab.length-(offset+1)*width+offset; i<=tab.length-offset*width-(offset+1); i++) tab[i] = 0;
    }

    /**
     * Transformee de Hough
     * Algorithme permettant de detecter les lignes droites dans une image binaire
     * Cette algorithme est en temps de calcul non constant depend de l'information de l'image et
     * peut s'averer tres long dans le cas ou l'image n'est pas nette. Cette fonction peut generer une exception
     * dans le cas ou le traitement etait trop long ie l'image n'est pas celle recherche
     */
    public static ArrayList<ParametricFunction> HoughTransform(byte[] T, int width, double thr) throws TimeLimitExceededException
    {
        final int max_time_ms = 800;
        long start = System.currentTimeMillis();
        // detection de contour
        byte[] tab = edgedetection(T, width);
        clearBorders(tab,width);
        final int height = tab.length/width;
        final int center_x = width/2;
        final int center_y = height/2;
        final int pDelta = (int)Math.sqrt((height*height+width*width)/4)+60;
        final int pMax = pDelta/2;
        final int thLength = pDelta/2+200;
        final double tStep = Math.PI/thLength;
        int highest = 0;
        // on cree un tableau de 2 dimensions pour l'espace de Hough
        // en abcsisse p (la distance de l'origine a la droite)
        // en ordonnee theta (l'angle de la droite)
        byte[][] houghSpace = new byte[thLength][pDelta];
        // on initialise a 0
        for(int i=0; i<houghSpace.length; i++)
            for(int j=0; j<houghSpace[0].length; j++)
                houghSpace[i][j] = 0;
        // on parcours notre image
        for(int i=0; i<width; i++) {
            for(int j=0; j<height; j++) {
                if( tab[i+j*width] == 0 ) {   // si le pixel est noir
                    int xi= i-center_x;     // changement de repere
                    int yi= j-center_y;     // par rapport au centre de l'image
                    for(int t=0; t<thLength; t++) {
                        // on parcours l'espace de Hough en largeur
                        // pour y dessiner une sinusoide
                        int rHough = (int)(xi*Math.cos(t*tStep) + yi*Math.sin(t*tStep));
                        rHough += pMax;     // on ajoute delta/2 pour avoir tous les p positifs
                        // et on incrémente la valeur de la case de l'espace de Hough
                        if(rHough>=0 && rHough<pDelta) houghSpace[t][rHough]++;
                    }
                }
            }
            //if(System.currentTimeMillis() - start > max_time_ms) throw new TimeLimitExceededException();
        }
        //Fonctions.afficheTab(houghSpace);
        // on cherche le maximum
        for(int i=0; i<thLength; i++)
            for(int j=0; j<pDelta; j++)
                if(houghSpace[i][j] > highest)
                    highest = houghSpace[i][j];
        // et on defini une limite pour les points de l'espace de Hough a choisir
        thr = highest*thr;
        // on cree notre liste de droites
        ArrayList<ParametricFunction> list = new ArrayList<ParametricFunction>();
        // on parcours a nouveau l'espace pour enregistrer les points superieurs a notre limite
        // et en deduire les droites correspondantes
        // un point dans l'espace de Hough correspond a une droite dans l'espace cartesien
        // un point dans l'espace cartesien correspond a une sinusoide dans l'espace de Hough
        // l'endroit ou il y a de nombreux croisements de sinusoides correspond donc a une droite
        for(int i=1; i<thLength-1; i++) {
            for(int j=0; j<pDelta-2; j++) {
                int puissance = houghSpace[i][j];
                if( puissance >= thr) {  // si superieur a la limite
                    // on cherche un maximum local
                    int max_y = i;
                    int max_x = j;
                    for(int g=-1; g<=1; g++) for(int h=0; h<=2; h++) {
                        if(houghSpace[i+g][j+h] > houghSpace[max_y][max_x]) {
                            max_y = i+g;
                            max_x = j+h;
                            puissance = houghSpace[max_y][max_x];
                        }
                        houghSpace[i+g][j+h] = 0;  // et on efface tous les autres
                    }
                    
                    // On cree notre droite
                    // avec pour origine le centre du tableau
                    // on met en parametre teta et r
                    double theta = max_y*tStep;
                    int r = max_x-pMax;
                    boolean addit = true;
                    int remplacer = -1;
                    for(int k=0; k<list.size(); k++) {/*
                        if( (Math2.equals(r,list.get(k).getR(),14)
                                && Math2.equalsAngleDroite(theta,list.get(k).getTheta(),0.4) )
                            || (Math2.equals(-r,list.get(k).getR(),14)
                                && Math2.equalsPlusPI(theta,list.get(k).getTheta(),0.4)) ) { */
                            if( (Math2.equals(r,list.get(k).getR(),14)
                                && Math2.equalsAngleDroite(theta,list.get(k).getTheta(),0.4) ) ) {
                            addit = false;
                            if( list.get(k).getIntensity() < puissance ) remplacer = k;
                            break;
                        }
                    }
                    if(addit) list.add(new ParametricFunction(theta,r,puissance));
                    else if(remplacer > -1) list.set(remplacer,new ParametricFunction(theta,r,puissance));
                }
            }
        }
        return list;
    }
    
    /**
     * Detection de contours
     * Algorithme le plus simple et le plus rapide de detection de contour :
     * Roberts Cross. On convolue l'image a une matrice 2*2 :
     * +1 0
     * 0 -1
     * Plus simplement dans notre cas (image binaire) : 
     * a b
     * c d
     * Si a = d et b = c alors on est sur le bord d'un objet
     */
    public static byte[] edgedetection(byte[] tab, int width)
    {
        byte[] tab2 = tab.clone();
        int height = tab.length/width;
        for(int i=2; i<height; i++){
            for(int j=2; j<width; j++) {
                int p = j+i*width;
                if( j >= width-4 || i >= height-4) {
                    tab2[p] = (byte)255;
                } else {
                    int a = tab[p];
                    int b = tab[p+1];
                    int c = tab[p+width];
                    int d = tab[p+width+1];
                    if(a == d && b == c) tab2[p] = (byte)255;
                }
            }
        }
        return tab2;
    }
    
    /**
     * Transforme un tableau d'une seule dimension (representant souvent une image)
     * en un tableau a deux dimensions ou chaque ligne est separe dans un sous tableau
     */
    public static byte[][] to2DArray(byte[] tab, int width) 
    { 
        byte[][] tab2 = new byte[tab.length/width][width];
        for(int i=0; i<tab2.length; i++) {
            for(int j=0; j<tab2[0].length; j++) {
                tab2[i][j] = tab[i+j*width];
            }
        }
        return tab2;
    }
    
    /**
     * Transforme un tableau de 2 dimension en un tableau a 1 dimension en mettant a la suite les lignes
     */
    public static byte[] to1DArray(byte[][] tab)
    {
        int width = tab[0].length;
        byte[] tab2 = new byte[tab.length*width];
        for(int i=0; i<tab2.length; i++) {
            tab2[i] = tab[i/width][i%width];
        }
        return tab2;
    }

    /**
     * Supprime (met en blanc) 2 pixels sur les 4 bords de l'image
     */
    public static void clearBorders(byte[] tab, int width)
    {
        int height = tab.length/width;
        clearZone(tab, 0, 0, 2, height, width);  //2 lignes a gauche
        clearZone(tab, 0, 0, width, 2, width);   //2 lignes en haut
        clearZone(tab, width-2, 0, 2, height, width);     // 2 lignes a droite
        clearZone(tab, 0, height-2, width, 2, width);     // 2 lignes en bas
    }
    
    /**
     * Supprime (met en blanc) un rectangle de pixel commencant au point (xTop,yTop),
     * de largeur w et le hauteur h
     */
    private static void clearZone(byte[] tab, int xTop, int yTop, int w, int h, int width)
    {
        for(int i=yTop; i < yTop+h; i++) {
            for(int j=xTop; j < xTop+w; j++) {
                try {
                    tab[i*width+j] = (byte)255;
                } catch(ArrayIndexOutOfBoundsException aioobe){ }
            }
        }
    }
    
    /**
     * Supprime (met en blanc) un rectangle de pixel compris entre
     * le point situe en haut a gauche topLeft et celui en bas a droite downRight
     */
    private static void clearZone(byte[] tab, int topLeft, int downRight, int width)
    {
        
        for(int i=topLeft/width; i < downRight/width; i++) {
            for(int j=topLeft%width; j < downRight%width; j++) {
                try {
                    tab[i*width+j] = (byte)255;
                } catch(ArrayIndexOutOfBoundsException aioobe){ }
            }
        }  
    }
    
    /**
     * Dessine une ligne dans un tableau de pixel,
     * relativement au point en haut a gauche de l'image.
     */
    public static void drawLine(byte[] tab, int width, ParametricFunction pf)
    {
        int height = tab.length/width;
        if(!pf.isAlmostVertical()) {
            for(int i=0; i<width; i++) {
                int y = pf.getY(i);
                if(y>=0 && y<height) tab[y*width+i] = 0;
            }
        } else {
            for(int i=0; i<height; i++) {
                int x = pf.getX(i);
                if(x>=0 && x<width) tab[i*width+x] = 0;
            }
        }
    }
    
    /**
     * Dessine une ligne dans un tableau de pixel,
     * relativement au centre de l'image.
     */
    public static int drawLineCenter(byte[] tab, int width, ParametricFunction pf)
    {
        int height = tab.length/width;
        int center_x = width/2;
        int center_y = (height/2);
        int pixelsDrawn = 0;
        if(!pf.isAlmostVertical()) {
            for(int i=-width/2; i<width/2; i++) {
                int y = pf.getY(i) + center_y ;
                if(y>=0 && y<height) {
                    tab[y*width+i+center_x] = 0;
                    pixelsDrawn++;
                }
            }
        } else {
            for(int i=-height/2; i<height/2; i++) {
                int x = pf.getX(i) + center_x;
                if(x>=0 && x<width) {
                    tab[(i+center_y)*width+x] = 0;
                    pixelsDrawn++;
                }
            }
        }
        return pixelsDrawn;
    }
    
    /**
     * Met tous les pixels d'un tableau a blanc (255)
     */
    public static void clear(byte[] tab)
    {
        for(int i=0; i<tab.length; i++) tab[i] = (byte)255;   
    }
    
    /**
     * Dessine un arc de cercle d'angle angleTotal
     */
    public static void drawCircle(byte[] tab, int rayon, int p, double angleTotal, int width)
    {
        int nombreTotalDePixels = (int)Math.round(angleTotal*rayon);
        boolean isBlackCrossed = false;
        for(double angle = 0.0; angle < angleTotal; angle +=(angleTotal/nombreTotalDePixels)) 
        {
            int x1 = (int) Math.round(rayon*Math.cos(angle));
            int y1 = (int) Math.round(rayon*Math.sin(angle));
            if(!overflowCircle(p,x1,width)) {
                try {
                    int pix = -y1*width+x1+p;
                    tab[pix] = 0;
                } catch(ArrayIndexOutOfBoundsException aioobe){}
            }
        }
    }
    
    /**
     * Dessine un cercle et renvoi les angles des bords de la forme traversee
     */
    public static OrientedAngle specialCircle(Point p, byte[] tab, int width)
    {
        final double angleTotal = 2*Math.PI;
        int ps = p.x + p.y*width;
        int height = tab.length/width;
        int rayon = p.x-4;
        int mr = width/100;
        if(p.x >= width/2) rayon = width-p.x-mr;
        if(p.y <= rayon || p.y >= width-rayon) {
            if(p.y >= height/2) rayon = height-p.y-mr;
            else rayon = p.y-mr;
        }
        int nombreTotalDePixels = (int)Math.round(angleTotal*rayon);
        double angl1 = 0.0;
        double angl2 = 0.0;
        int tempix = tab[rayon+ps];
        for(double angle = 0; angle <= angleTotal ; angle +=(angleTotal/nombreTotalDePixels)) {
            int x1 = (int) Math.round(rayon*Math.cos(angle));
            int y1 = (int) Math.round(rayon*Math.sin(angle));
            if(!overflowCircle(ps,x1,y1,width,height)) {
                int pix = y1*width+x1+ps;
                if( tab[pix] != tempix && angl1 == 0 ) angl1 = angle;
                else if( tab[pix] != tempix ) { angl2 = angle; break; }
                tempix = tab[pix];
            }
        }
        if(angl1 == angl2) return null;
        else return new OrientedAngle(p,angl1,angl2,rayon);
    }
    
    /**
     * Test si un cercle va deborder sur une ligne du dessus
     * lorsqu'on est dans un tableau a 1 dimension
     */
    private static boolean overflowCircle(int p, int x1, int width)
    {
        int diff = p%width + x1;
        return (diff < 0  || diff >= width);
    }
    
    private static boolean overflowCircle(int p, int x1, int y1, int width, int height)
    {
        return (p%width+x1 < 0 || p%width+x1 >= width || p/width+y1 < 0 || p/width+y1 >= height);
    }
    
    /**
     * Dessine une croix noir entoure de blanc de 5*5 au point c
     */
    public static void drawCross(byte[] tab, int c, int width)
    {
        try {
            for(int i=c-2*width; i<=c+2*width; i+=width) {
                for(int j=-2; j<=+2; j++) {
                    tab[i+j] = (byte)255;
                }
            }
            for(int i=c-2; i<=c+2; i++) tab[i] = 0;
            for(int i=c-2*width; i<=c+2*width; i+=width) tab[i] = 0;
        } catch(ArrayIndexOutOfBoundsException aioobe) {}
        
    }
    
    /**
     * Transforme un pixel RGB en HSL 
     * (wikipedia)
     */
    public static float[] RGB_to_HSL(int red,int green,int blue) {
        float max = Math.max(Math.max(red, green), blue);
        float min = Math.min(Math.min(red, green), blue);
        float h = 0;
        if(red == max) {
            h = ((green - blue) / (max - min)) * 60;
           if(green < blue) h += 360;
        } else if (green == max) {
            h = (((blue - red) / (max - min)) * 60) + 120;
        } else if (blue == max) {
            h = (((red - green) / (max - min)) * 60) + 240;
        }
        float l = (max+min)/2;
        float s = 0;
        if(max != min) {
            if(l > 0.5) s = (max - min)/(2-2*l);
            else s = (max - min)/(2*l);
        }
        return new float[]{h,s,l};
    }
        

}


