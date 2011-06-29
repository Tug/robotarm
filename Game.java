import java.util.ArrayList;
import java.awt.Point;
import java.awt.Color;

/**
 * La classe Game se charge de relier l'analyse d'image et 
 * l'envoi des donnees sur le 68000
 */
public class Game
{
    private final static HColor colorX = HColor.BLUE;
    private final static HColor colorO = HColor.RED;
    private boolean X = false;
    private IA ia;
    private final static int depth = 5;
    
    public Game()
    {
        ia = new IA(X,depth);
    }
    
    public void play(ImageAnalyser ima)
    {
        ArrayList<Bouchon> bouchons = ima.getBouchons();
        ArrayList<Bouchon> corksOutGrid = new ArrayList<Bouchon>();
        for(int i=0; i<bouchons.size(); i++) {
            if(!bouchons.get(i).isPlaced()) {
                corksOutGrid.add(bouchons.get(i));
            }
        }
        Bouchon[][] corksInGrid = ima.getGrid().getCorksInGrid();
        Pion[][] pions = new Pion[3][3];
        int nbdeX = 0;
        int nbdeO = 0;
        for(int i=0; i<corksInGrid.length; i++) {
            for(int j=0; j<corksInGrid[0].length; j++) {
                Bouchon b = corksInGrid[i][j];
                Pion p = Pion.NO_ONE;
                if(b != null) {
                    if(b.getColor() == colorX) {
                        p = Pion.X;
                        nbdeX++;
                    } else if(b.getColor() == colorO) {
                        p = Pion.O;
                        nbdeO++;
                    } else System.out.println("erreur couleur");   
                }
                pions[i][j] = p;
            }
        }
        int nb = -1;
        if((!X && nbdeO<nbdeX)||(X && nbdeX<nbdeO)) {
            nb = ia.test1(pions);
        }
        if(nb>=0) {
            PolarPoint[] centers = ima.getGrid().getBoxesCentermm();
            PolarPoint putACorkHere = centers[nb];
            int dist = putACorkHere.r;
            double theta = putACorkHere.theta;
            // maintenant a toi de les envoyer sur le 68000
        }
    }

}
