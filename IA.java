import java.util.*;

public class IA
{
    private Pion joueur;
    private Pion adve;
    private int profondeur;
    private int gag;
    private int per;
    private int ega;
    
    public IA(boolean X,int prof)
    {
        setProfondeur(prof);
        setPion(X);
    }

    public void setProfondeur(int prof)
    {
        profondeur = prof;
    }
    
    public void setPion(boolean X)
    {
        if (X)
        {
            joueur = Pion.X;
            adve = Pion.O;
        }
        else
        {
            joueur = Pion.O;
            adve = Pion.X;
        }
    }
    
    /**
     * Retourne le tour de jeu actuelle (0 si c'est le premier et 8 si c'est le dernier) et 9 si la partie est finie
     */
    public int getTour(Pion[][] tab)
    {
        int tour = 0;
        for(int i = 0; i < 9; i++)
        {
            if (!(tab[i%3][i/3].equals(Pion.NO_ONE)))
            {
                tour++;
            }
        }
        return tour;
    }
    
    /**
     * Retourne la factorielle du nombre n
     */
    public int fact(int n)
    {
        int fact = 1;
        for (int i = 2; i <= n;i++)
        {
            fact = fact*i;
        }
        return fact;
    }
    
    /**
     * Retourne les possibilités de jeu
     */
    public int[] positionPossible(Pion[][] tab, int tour)
    {
        int[] pos = new int[9-tour];
        int tr=0;
        for(int i = 0; i < 9; i++)
        {
            if ((tab[i%3][i/3].equals(Pion.NO_ONE)))
            {
                pos[tr] = i;
                tr++;
            }
        }
        return pos;
    }
    
    /**
     * Teste les n positions restantes a jouer et determine laquelle est la meilleur grace a un min/max
     */
    public int test1(Pion[][] tab)
    {
        int tour = getTour(tab);
        int posi = -1;
        int max = -1*fact(9-tour);
        int curr;
        int[] poss = positionPossible(tab,tour);
        for (int i = 0; i<poss.length;i++)
        {
            if (tour%2 == 0)                     // les X commencent toujours en premier!
            {
                tab[poss[i]%3][poss[i]/3] = Pion.X;
            }
            else
            {
                tab[poss[i]%3][poss[i]/3] = Pion.O;
            }
            curr = test2(1,tab,tour+1);
            System.out.println("Pour la position " + poss[i] + " le max est " + curr );
            if (curr > max)
            {
                max = curr;
                posi = poss[i];
            }
            else if (curr == max)
            {
                if (((int)(Math.random()*9)) == 0)
                {
                    posi = poss[i];
                }
            }
            tab[i%3][i/3] = Pion.NO_ONE;
        }
        
        return posi;
    }
    
    /**
     * Test la coordonnée selectionné jusqu'au lvl de l'IA;
     */
    public int test2(int n, Pion[][] tab, int tour)
    {
        int nb = 0;
        int att = 0;
        boolean trouv = false;
        Pion p = testPlateau(tab);
        int fact = fact(profondeur-n);
        if (p == adve)
        {
            return -1*fact;
        }
        if (p == joueur)
        {
            return 1*fact;
        }
        if ( (tour == 8) || (n == profondeur) )
        {
            return 0;
        }
        int[] poss = positionPossible(tab, tour);
        for (int i = 0; i < poss.length; i++)
        {
            if (tour%2 == 0)                     // les X commencent toujours en premier!
            {
                tab[poss[i]%3][poss[i]/3] = Pion.X;
            }
            else
            {
                tab[poss[i]%3][poss[i]/3] = Pion.O;
            }
            if (trouv)
            {
                att = test2(profondeur,tab,tour+1);
                att = att*Math.abs(att);
            }
            else
            {
                att = test2(n+1,tab,tour+1);
                if (Math.abs(att) == 1*fact(profondeur-(n+1)))
                {
                    trouv = true;
                }
            }
            tab[poss[i]%3][poss[i]/3] = Pion.NO_ONE;
            nb+=att;
        }
        return nb;
    }
    
    /**
     * Test si le plateau a un gagnant ou non
     */
    
    public Pion testPlateau(Pion[][] tab)
    {
        if ((tab[0][0]==tab[0][1]) & (tab[0][0]==tab[0][2]) & (tab[0][0] != Pion.NO_ONE))
        {
            return tab[0][0];
        }
        if ((tab[1][0]==tab[1][1]) & (tab[1][0]==tab[1][2]) & (tab[1][0] != Pion.NO_ONE))
        {
            return tab[1][0];
        }
        if ((tab[2][0]==tab[2][1]) & (tab[2][0]==tab[2][2]) & (tab[2][0] != Pion.NO_ONE))
        {
            return tab[2][0];
        }
        if ((tab[0][0]==tab[1][1]) & (tab[0][0]==tab[2][2]) & (tab[0][0] != Pion.NO_ONE))
        {
            return tab[0][0];
        }
        if ((tab[0][2]==tab[1][1]) & (tab[0][2]==tab[2][0]) & (tab[0][2] != Pion.NO_ONE))
        {
            return tab[0][2];
        }
        if ((tab[0][0]==tab[1][0]) & (tab[0][0]==tab[2][0]) & (tab[0][0] != Pion.NO_ONE))
        {
            return tab[0][0];
        }
        if ((tab[0][1]==tab[1][1]) & (tab[0][1]==tab[2][1]) & (tab[0][1] != Pion.NO_ONE))
        {
            return tab[0][1];
        }
        if ((tab[0][2]==tab[1][2]) & (tab[0][2]==tab[2][2]) & (tab[0][2] != Pion.NO_ONE))
        {
            return tab[0][2];
        }
        return Pion.NO_ONE;
    }
}
