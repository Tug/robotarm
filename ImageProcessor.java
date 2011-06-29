import javax.media.Buffer;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.media.util.BufferToImage;
import javax.media.format.VideoFormat;
import javax.media.Format;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;

/**
 * ImageProcessor est une tache independante (Thread) chargee
 * de recuperer une image sequentiellement et de gerer les traitements
 * a effectuer sur celle-ci
 */
public class ImageProcessor extends Thread
{
    private Game game;
    private long timestep; // tenps entre chaque capture d'image en ms
    boolean paused = false; // pour faire des pause 
    boolean killed = false; // pour terminer le thread
    private DataSourceReader dsr;
    private Image image = null;
    private JFrame jf;
    private JPanel jp;
    private Buffer buffer;
    private BufferToImage b2i;
    private ImageAnalyser ia;
    private Timer tim = new Timer();
    //private TestIsolation ti = null;
    /**
     * Constructeur
     */
    public ImageProcessor(DataSourceReader dsr, long timestep, Game game)
    {
        this.dsr = dsr;
        this.timestep = timestep;
        this.game = game;
        initComponents();
    }
    
    /**
     * Initialise la fenetre d'affichage du traitement
     */
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
    
    /**
     * Demmarre la tache
     */
    @Override public void run()
    {
        try {
            waitForStream();
        } catch (Exception e) {
            kill();
            System.out.println("Erreur : " + e);
        }
        while (!killed) {
            if(!paused) {
                    // Convertion des donnees du buffer en Image
                    image = loadImage( "test3.jpg" );
                    //image = b2i.createImage(buffer);
                    // Creation d'un objet ImageAnalyser
                    tim.start();
                    ia = new ImageAnalyser(image,this);
                    ia.analyse();
                    //ti.setT(ia.getData());
                    tim.stop();
                    tim.printTime();
                    //printTable(ia.getData(),25);
                    //ia.drawCenters();
                    // recupere l'image analyse
                    image = ia.getAnalysedImage();
                    //ia.detectTableauDeJeu();
                    //ia.setBufferedImage(imgmod.processImg(ia.getBufferedImage()));
                    //image = ia.getImage();
                    refreshImage();
                    if(ia.isOk()) game.play(ia);
            }
            try {
                sleep(timestep);
            } catch (Exception e) {
                kill();
                System.out.println("Erreur : " + e);
            }
        }
    }
    
    /**
     * This is the correct way to pause a thread; unlike suspend.
     */
    public synchronized void pause() 
    {
        paused = true;
    }

    /**
     * This is the correct way to kill a thread; unlike stop.
     */
    public synchronized void kill() 
    {
        killed = true;
        notify();
    }
   
    public synchronized void restart() 
    {
        paused = false;
        notify();
    }
    
    /**
     * Raffraichi la JPanel
     */
    public void refreshImage()
    {
        image = ia.getAnalysedImage();
        jp.repaint();
    }
    
    /**
     * Raffraichi la JPanel en changeant l'image avec celle passe en parametre
     */
    public void refreshImage(Image im)
    {
        image = im;
        jp.repaint();
    }
    
    /**
     * Charge un fichier image depuis le chemin passe en param
     * et retourne une BufferedImage
     */
    public BufferedImage loadImage( String file )
    {
        try {
            return ImageIO.read(getClass().getClassLoader().getResource( file ));
        }
        catch(Exception e){ System.out.println("erreur lors de l'acces a l'image "+ file ); return null; }
    }
    
    /**
     * Recupere une image dans le buffer
     * Si le buffer ne contient pas d'image, attend
     */
    private void waitForStream() throws InterruptedException
    {
        buffer = dsr.getDataSourceHandler().getBuffer();
        while(image == null) {
            b2i = new BufferToImage((VideoFormat)buffer.getFormat());
            image = b2i.createImage(buffer);
            sleep(200);
        }
        sleep(1000);
    }

    
}

