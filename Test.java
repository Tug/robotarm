import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;
import java.awt.Point;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.Toolkit;
import java.awt.Dimension;
/**
 * Une classe pour tester differents algorithme :
 * fonctionnement et rapidite
 */
public final class Test
{
    private JFrame jf;
    private JPanel jp;
    private Image image = null;
    private MemoryImageSource masksrc;
    
    public Test()
    {
        initComponents();
        int width = 640, height = 480;
        ImageAnalyser ia = new ImageAnalyser(loadImage( "", "test2.jpg" ));
        byte[] mask = ia.getMaskData();
        ArrayList<ParametricFunction> list = null;
        try {
            list = Filter.HoughTransform(mask,width,0.1);
        } catch(Exception e){}
        for(int i=0; i<list.size(); i++) {
            Filter.drawLineCenter(mask,width,list.get(i));
        }
        masksrc = new MemoryImageSource(width,height,getGrayColorModel(),mask,0,width);
        masksrc.setAnimated(true);
        masksrc.setFullBufferUpdates(true);
        image = Toolkit.getDefaultToolkit().createImage(masksrc);
        refreshImage();
        /*
        byte[] by = randomizeByte(10000000);
        byte[] by2 = by.clone(); 
        Timer tim = new Timer();
        tim.start();
        testByte255(by);
        tim.stop();
        System.out.println("byte 255 : "+tim.getTime());
        tim.start();
        testBytemoins1(by2);
        tim.stop();
        System.out.println("byte -1 : "+tim.getTime());
        */
        /*
        boolean[] b = randomizeBoolean(1000000);
        Timer tim = new Timer();
        tim.start();
        for(int i=0; i<1000; i++)
        testCmp(b);
        tim.stop();
        System.out.println("boolean[1000000] : "+tim.getTime());
        byte[] by = randomizeByte(1000000);
        tim.start();
        for(int i=0; i<1000000; i++)
            if(by[i] == 0) ;
        tim.stop();
        System.out.println("byte[1000000] ( 0 ou 1 ) : "+tim.getTime());
        short[] s = randomizeShort(1000000);
        tim.start();
        for(int i=0; i<1000; i++)
        testCmp(s);
        tim.stop();
        System.out.println("short[1000000] ( 0 ou 255 ) : "+tim.getTime());
        */
    }
    
    public static ColorModel getGrayColorModel()
    {
        byte[] g = new byte[256];
        for (int i = 0; i < 256; i++) g[i] = (byte)i;
        return new IndexColorModel(8, 256, g, g, g);
    }
    
    private void initComponents()
    {
        //ti = new TestIsolation();
        jf = new JFrame("Image Traite");
        jp = new JPanel() { 
        // redefini la methode d'affichage dans cette JPanel
        // pour lui faire afficher l'image en traitement
            @Override public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                if (image != null)
                {
                    g.drawImage(image, 0, 0, jp);
                }
            }
        };
        // definition des dimensions de la JPanel par defaut
        jp.setPreferredSize(new Dimension(640,480));
        jp.setMinimumSize(new Dimension(640,480));
        // et autorisation de redimmensionner
        jf.setResizable(true);
        // fond de la fenetre noir
        jp.setBackground(Color.BLACK);
        // ajout du JPanel (image traite)
        jf.getContentPane().add(jp);
        // quitte le programme si on ferme la fenetre
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // application des dimensions de la fenetre
        jf.pack();
        // centrer la fenetre
        jf.setLocationRelativeTo(null);
        // afficher la fenetre
        jf.setVisible(true);
    }
    
    public void refreshImage()
    {
        masksrc.newPixels();
        jp.repaint();
    }
    
    /**
     * Charge un fichier image depuis le chemin et le nom passe en param
     * et retourne une BufferedImage
     */
    public BufferedImage loadImage( String chemin, String file )
    {
        try {
            return ImageIO.read(getClass().getResource( chemin + file ));
        }
        catch(Exception e){ System.out.println("erreur lors de l'acces a l'image "+ chemin + file ); return null; }
    }
    
    final static boolean[] randomizeBoolean(int nb)
    {
        boolean[] tab = new boolean[nb];
        for(int i=0; i<tab.length; i++)
        {
            double d = Math.random();
            if(d<0.5) tab[i] = false;
            else tab[i] = true;
        }
        return tab;
    }
    
    final static byte[] randomizeByte(int nb)
    {
        byte[] tab = new byte[nb];
        for(int i=0; i<tab.length; i++)
        {
            double d = Math.random();
            if(d<0.5) tab[i] = 0;
            else tab[i] = 1;
        }
        return tab;
    }
    
    final static short[] randomizeShort(int nb)
    {
        short[] tab = new short[nb];
        for(int i=0; i<tab.length; i++)
        {
            double d = Math.random();
            if(d<0.5) tab[i] = 0;
            else tab[i] = 255;
        }
        return tab;
    }
    
    
    final static void testCmp(boolean[] b)
    {
        for(int i=0; i<b.length; i++)
        {
            if(b[i] == true) ;
        }
    }
    
    final static void testCmp(byte[] b)
    {
        for(int i=0; i<b.length; i++)
        {
            if(b[i] == 1) ;
        }
    }
    
    final static void testCmp(short[] s)
    {
        for(int i=0; i<s.length; i++)
        {
            if(s[i] == 255) ;
        }
    }
    
    final static void testByte255(byte[] b)
    {
        for(int i=0; i<b.length; i++) b[i] = (byte)255;
    }
    
    final static void testBytemoins1(byte[] b)
    {
        for(int i=0; i<b.length; i++) b[i] = -1;
    }

}
