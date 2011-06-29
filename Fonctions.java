import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.media.format.VideoFormat;
import javax.media.Format;
import java.util.Random;

public class Fonctions
{
    private final static int deltaColor = 20;

    private static void printTable(int[] tab, int max)
    {
           for(int i = 0; i < max; i++)
           {
               //decommenter si ARGB
               int alpha = (tab[i] >>24 ) & 0xFF;
               int rouge = (tab[i] >>16 ) & 0xFF;
               int vert = (tab[i] >> 8 ) & 0xFF;
               int bleu = tab[i] & 0xFF;
               System.out.println(" "+i+"alpha : "+alpha);
               System.out.println(" "+i+"rouge : "+rouge);
               System.out.println(" "+i+"vert : "+vert);
               System.out.println(" "+i+"bleu : "+bleu);
           }
    }
    
    public static float askforfloat(float defa)
    {
          float f = defa;
          BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
          try {
              f = Float.valueOf(br.readLine());
          } catch ( Exception error ) {
                f = defa;
          }
          return f;
    }
    
    public static int askforint()
    {
          int i = 9;
          BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
          try {
              i = Integer.valueOf(br.readLine());
          } catch ( Exception error ) {
                i = 9;
          }
          return i;
    }
    
    public static boolean isAproxEqual(int color1, int color2)
    {
        int rouge1 = (color1 >>16 ) & 0xFF;
        int vert1 = (color1 >> 8 ) & 0xFF;
        int bleu1 = color1 & 0xFF;
        int rouge2 = (color2 >>16 ) & 0xFF;
        int vert2 = (color2 >> 8 ) & 0xFF;
        int bleu2 = color2 & 0xFF;
        
        if( ( Math.abs(rouge1-rouge2) <= deltaColor ) &&
            ( Math.abs(vert1-vert2) <= deltaColor ) &&
            ( Math.abs(bleu1-bleu2) <= deltaColor ) ) {
            return true;
        } else {
            return false;
        }
    }
    
    public static VideoFormat getLargestVideoFormat(Format[] tf, String type)
    {
        VideoFormat vfMax = null;
        for(int i=0; i<tf.length; i++) {
            if(tf[i] instanceof VideoFormat && type.equals(tf[i].getEncoding())) {
                VideoFormat vf = (VideoFormat)tf[i];
                if( vfMax == null || ( vfMax.getSize().width < vf.getSize().width ) ) 
                    vfMax = vf;
            }
        }
        return vfMax;
    }
    
    public static VideoFormat getVideoFormat(Format[] tf, String type, int width)
    {
        for(int i=0; i<tf.length; i++) {
            if(tf[i] instanceof VideoFormat && type.equals(tf[i].getEncoding())) {
                VideoFormat vf = (VideoFormat)tf[i];
                if( vf.getSize().width == width ) return vf;
            }
        }
        return null;
    }
    
    /**
     * Tri par insertion :
     * Invariant : I(k) == (T[0...k-1] croissant) et (T[0...k-1] <= T[k...n-1])
     * Initialisation : k = 0
     * Arret : k = n-1
     * Implication : I(k) et (k!=n) et (tk' = min T[i] , k <= i <= n-1) et (T[k] = tk') et (T[k'] = tk) -> I(k+1)
     * 
     */
    public static void insertionSort(int[] T)
    {
        int n = T.length;
        int k = 0;          // I(k)
        while(k != n-1)
        {
            int k1 = min(T,k,n);  // tk' = min(k...n-1)
            int x = T[k];
            T[k] = T[k1];
            T[k1] = x;      // I(k+1)
            k++;            // I(k)
       }
    }
    
    /**
     * Calcul de l'indice du minimum du tableau
     * Invariant : I(k1,j) == (T[k1] = min(T[k...j-1])
     * Initialisation : (k1 = k) et (j = k+1)
     * Arret : j = n
     * Implication : - I(k1,j) et (j!=n) et ( T[j] >= T[k1] ) -> I(k1, j+1)
     *               - I(k1,j) et (j!=n) et ( T[j] < T[k1] ) -> I(j, j+1)
     * 
     */
    public static int min(int[] T, int k, int n)
    {
        int k1 = k;
        int j = k+1;
        while(j != n)
        {
            if(T[j] >= T[k1]) { // I(k1,j+1)
                j++;
            } else {                //I(j,j+1)
                k1 = j;
                j++;
            }
        }
        return k1;
    }
    
    /**
     * Retourne un tableau d'entier de taille n rempli aleatoirement
     */
    public static int[] randomize(int n) 
    {
        int[] returnArray = null;
        if (n > 0) 
        {
            returnArray = new int[n];
            for (int index = 0; index < n; ++index) 
            {
                returnArray[index] = index;
            }
            Random random = new Random(System.currentTimeMillis());
            for (int index = 0; index < n; ++index) 
            {
                int j = (int) (random.nextDouble() * (n - index) + index);
                int tmp = returnArray[index];
                returnArray[index] = returnArray[j];
                returnArray[j] = tmp;
            }
        }
        return returnArray;
    }
    
    public static long findBenchMark()
    {
        Timer tim = new Timer();
        tim.start();
        int[] tab = randomize(8000);
        insertionSort(tab);
        tim.stop();
        return tim.getTimeInMs();
    }
    
    /**
     * Affiche le contenu d'un tableau d'entiers
     */
    public static void afficheTab(int[] t)
    {
        for (int i=0; i<t.length; i++) 
        {
            System.out.print(t[i]+" ");
            if(i%20 == 0 && i != 0) System.out.println("");
        } 
    }
    
    public static void afficheTab(byte[] t)
    {
        for (int i=0; i<t.length; i++) 
        {
            System.out.print(t[i]+" ");
            if(i%640 == 0 && i != 0) System.out.println("");
        } 
    }
    
    public static void afficheTab(byte[] t, int width)
    {
        for (int i=0; i<t.length; i++) 
        {
            System.out.print(t[i]+" ");
            if(i%width == 0 && i != 0) System.out.println("");
        } 
    }
    
    public static void afficheTab(byte[] t, int width, int start, int stop)
    {
        for (int i=start; i<stop; i++) 
        {
            System.out.print(t[i]+" ");
            if(i%width == 0 && i != 0) System.out.println("");
        } 
    }
    
    public static void afficheTab(byte[][] t)
    {
        for (int i=0; i<t.length; i++) 
        {
            for(int j=0; j<t[0].length; j++)
            {
                System.out.print(t[i][j]+" ");
            }
            System.out.println("");
        } 
    }
    
}
