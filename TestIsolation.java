import java.awt.image.MemoryImageSource;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * TestIsolation cree une fenetre de test pour l'isolation d'une couleur
 * Transformation RGB -> HSL ...
 */
public final class TestIsolation extends JFrame
{
    protected int[] T;
    private Image image = null;
    private MemoryImageSource mic = null;
    public static float hue = 118;
    public static float sat = 0.24f;
    public static float light = 0.485f;
    public static float tolHue = 2f;
    public static float tolSat = 0.02f;
    public static float tolLight = 0.01f;
    private MouseInputAdapter mia;
    protected ArrayList<Float> listH = new ArrayList<Float>();
    protected ArrayList<Float> listS = new ArrayList<Float>();
    protected ArrayList<Float> listL = new ArrayList<Float>();
    
    public TestIsolation()
    {
        super("Test isolation couleur");
        
        T = new int[640*480];
        mic = new MemoryImageSource(640,480,T,0,640);
        mic.setAnimated(true);
        mic.setFullBufferUpdates(true);
        image = Toolkit.getDefaultToolkit().createImage(mic);
        mic.newPixels();
        JPanel jp = new JPanel(){
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) g.drawImage(image,0,0,this) ;
            }
        };
        mia = new MouseInputAdapter(){
            @Override public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                int x = e.getX();
                int y = e.getY();
                float[] tabt = getMoyHSL(x+640*y);
                listH.add(tabt[0]);
                listS.add(tabt[1]);
                listL.add(tabt[2]);
                refreshMoy();
            }
        };
        addMouseListener( mia );
        addMouseMotionListener( mia );
        jp.setPreferredSize(new Dimension(640,480));
        jp.setMinimumSize(new Dimension(640,480));
        setResizable(true);
        getContentPane().add(jp);
        pack();
        setVisible(true);
    }
    
    public void setT(int[] tab){
        for(int i=0; i<tab.length; i++) {
            T[i] = 0xff000000 | tab[i];
        }
        isolerTout();
        mic.newPixels();
        repaint();
    }
    
    private void isolerTout()
    {
        for(int i=0; i<listH.size(); i++) {
            isolerCouleur(listH.get(i),listS.get(i),listL.get(i));
        }
    }
    
    private void isolerCouleur(float hue, float saturation, float lightness) 
    {
        for(int i=0; i<T.length; i++) {
            int rgb = T[i];
            float red = ((rgb & 0xff0000) >> 16) / 255f;
            float green = ((rgb & 0xff00) >> 8) / 255f;
            float blue = (rgb & 0xff) / 255f;
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
            if( Math.abs(h - hue) < tolHue &&
                Math.abs(s - saturation ) < tolSat &&
                Math.abs(l - lightness ) < tolLight )
                T[i] = 0xff000000;
        }
    }
    
    public static float[] getHSL(int rgb) {
        float red = ((rgb & 0xff0000) >> 16) / 255f;
        float green = ((rgb & 0xff00) >> 8) / 255f;
        float blue = (rgb & 0xff) / 255f;
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
    
    public float[] getMoyHSL(int rgb) {
        int moy = 0;
        for(int i=-640; i<=640; i+=640)
            for(int j=-1; j<=1; j++)
                moy += T[rgb+i+j];
        rgb = moy/9;
        float red = ((rgb & 0xff0000) >> 16) / 255f;
        float green = ((rgb & 0xff00) >> 8) / 255f;
        float blue = (rgb & 0xff) / 255f;
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
    
    protected void refreshMoy() {
        float newMoyH = getMoyArrayList(listH);
        float newMoyS = getMoyArrayList(listS);
        float newMoyL = getMoyArrayList(listL);
        System.out.println( "H : "+newMoyH + "  S : "+newMoyS + "  L : "+newMoyL);
    }
    
    private float getMoyArrayList(ArrayList<Float> al)
    {
        float moy = 0;
        for(int i=0; i<al.size(); i++) {
            moy += al.get(i);
        }
        return moy/al.size();
    }

    

}
