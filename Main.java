import java.util.Vector;
import java.io.IOException;
import javax.media.*;
import javax.media.rtp.*;
import javax.media.format.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.swing.JFrame;
import java.awt.Dimension;

/**
 *
 * Write a description of class Main here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Main
{
    private static DataSourceReader dsr;
    private final static String videoEncoding = VideoFormat.RGB;
    private final static int videoWidth = 640;
    private static long timestep = 5000; //ms
    
    
   /**
     * Main program
     */
    public static void main(String [] args) 
    {
        // recuperation des peripheriques de capture video
        Vector videoDevices = CaptureDeviceManager.getDeviceList(new VideoFormat(videoEncoding));
        // selection du premier
        CaptureDeviceInfo cdiVideo = (CaptureDeviceInfo)videoDevices.elementAt(0);
        // recuperation des formats geres par ce peripherique
        Format[] fmts = cdiVideo.getFormats();
        // selection du plus grand format
        VideoFormat vf = Fonctions.getVideoFormat(fmts,videoEncoding,videoWidth);   
        // creation d'un pointeur vers ce peripherique
        MediaLocator ml = cdiVideo.getLocator();
        // Creation d'un DataSource pour ce MediaLocator
        DataSource ds = null;
        try {
            ds = Manager.createDataSource(ml);
        } catch (Exception e) {
            System.err.println("Creation de la DataSource impossible : " + ml);
            System.err.println("Le programme n'a pas reussi a recuperer le flux de la source video");
            System.exit(0);
        }
        // Creation d'un lecteur de flux pour le format choisi
        dsr = new DataSourceReader(vf);
        // activation de l'affichage de la source dans une Frame
        dsr.setMonitor(true);
        // demarrage de la lecture
        if (!dsr.open(ds)) System.exit(0);
        // determintion de la puissance du pc pour l'initialisation de l'ImageProcessor
        timestep += Fonctions.findBenchMark();
        // Creation d'une instance de jeu
        Game game = new Game();
        // creation d'un objet traitant le flux
        ImageProcessor ip = new ImageProcessor(dsr,timestep,game);
        // demarrage du traitement
        ip.start();
        Param parm = new Param(true);
    }
    
}
