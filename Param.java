import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * Param cree une fenetre de reglages des filtres de l'image
 * Et permet de sauvegarder ces parametres
 */
public class Param extends JFrame implements ActionListener, ChangeListener, Serializable
{
    private static final String nomFichier = "params";
    private final static String[] sNom = { "Thresh","scale","offset","gammaexpo" };
   // private JRadioButtonMenuItem choicemenu;
    private JSlider[] s;
    private JSpinner[] p;
    private final static int nb = 4;
    private final static int[] sdeb = { 100, 1, -5000, 800};
    private final static int[] sfin = { 255, 800, 5000, 1400};
    private int[] bakparams = { -10, 42, -350};
    private double houghthresh = 0.3;
    private int[] expodef = null;
    private int[] logdef = null;
    private boolean expo = false;
    private boolean blur5 = true, blur3 = false, blur7 = false;
    private boolean clean = true;
    
    public Param(boolean visible)
    {
        load();
        s = new JSlider[nb];
        p = new JSpinner[nb];
        for(int i=0; i<nb; i++) s[i] = new JSlider(JSlider.HORIZONTAL, sdeb[i], sfin[i], logdef[i]);
        for(int i=0; i<nb; i++) p[i] = new JSpinner(new SpinnerNumberModel(logdef[i], sdeb[i], sfin[i], 1));
        
        s[3].setVisible(false);
        p[3].setVisible(false);
        setPreferredSize(new Dimension(350,nb*40+80));
        setMinimumSize(new Dimension(350,nb*50+80));
        setLayout(new GridLayout(0, 3));
        addRadioChoice("Filtre gamma","expo","log",expo);
        Filter.params = new int[nb];
        for(int i=0; i<nb; i++) {
            s[i].setName(""+i);
            p[i].setName(""+i);
            s[i].addChangeListener(this);
            p[i].addChangeListener(this);
            //s[i].setMajorTickSpacing((sfin[i]-sdeb[i])/10);
            //s[i].setMinorTickSpacing((sfin[i]-sdeb[i])/10);
            //s[i].setPaintTicks(true);
            //s[i].setPaintLabels(true);
            p[i].setValue( logdef[i] );
            JLabel jl = new JLabel();
            jl.setText(sNom[i]);
            getContentPane().add(jl);
            getContentPane().add(s[i]);
            getContentPane().add(p[i]);
        }
        
        
        addRadioChoice("flou3",blur3);
        addRadioChoice("flou5",blur5);
        addRadioChoice("flou7",blur7);
        addRadioChoice("clean",clean);
        
        
        JButton reset = new JButton("Reset");
        JButton sauver = new JButton("Sauver");
        reset.setActionCommand("reset");
        sauver.setActionCommand("save");
        reset.addActionListener(this);
        sauver.addActionListener(this);
        
        getContentPane().add(reset);
        getContentPane().add(new JLabel());
        getContentPane().add(sauver);
        
        for(int i=0; i<4;i++){
           JSpinner jsp = null;
           if(i==0) { 
               jsp = new JSpinner(new SpinnerNumberModel(-30, -120, 20, 1));
               jsp.setName("bakthresh"); 
               jsp.setValue(bakparams[0]); 
           }
           if(i==1) { 
               jsp = new JSpinner(new SpinnerNumberModel(42, 0, 100, 1));
               jsp.setName("bakscalc"); 
               jsp.setValue(bakparams[1]);
           }
           if(i==2) { 
               jsp = new JSpinner(new SpinnerNumberModel(-350, -1200, 0, 1));
               jsp.setName("backoffset"); 
               jsp.setValue(bakparams[2]); 
            }
            if(i==3) { 
               jsp = new JSpinner(new SpinnerNumberModel(0.4, 0, 1, 0.01));
               jsp.setName("houghthresh");
               jsp.setValue(houghthresh); 
            }
           jsp.addChangeListener( new ChangeListener(){
                public void stateChanged(ChangeEvent e){
                     JSpinner jspi = (JSpinner)e.getSource();
                     if(jspi.getName().equals("bakthresh")) ImageAnalyser.bakparams[0] = (Integer)(jspi.getValue());
                     else if(jspi.getName().equals("bakscalc")) ImageAnalyser.bakparams[1] = (Integer)(jspi.getValue());
                     else if(jspi.getName().equals("backoffset")) ImageAnalyser.bakparams[2] = (Integer)(jspi.getValue());
                     else if(jspi.getName().equals("houghthresh")) ImageAnalyser.houghThresh = (Double)(jspi.getValue());
                }});
           getContentPane().add(jsp);
        }
        
        pack();
        setVisible(visible);
        refreshAll();
    }
    
    
    @Override public void stateChanged(ChangeEvent e){
        Object o = e.getSource();
        int nb = 0;
        int value = 0;
        if( o instanceof JSlider) {
            JSlider sl = (JSlider)o;
            nb = Integer.parseInt(sl.getName());
            value = sl.getValue();
            if(expo) expodef[nb] = value;
            else logdef[nb] = value;
        } else if( o instanceof JSpinner) {
            JSpinner sp = (JSpinner)o;
            nb = Integer.parseInt(sp.getName());
            value = (Integer)sp.getValue();
        } else return ;
        
        if(expo) expodef[nb] = value;
        else logdef[nb] = value;
        refreshAll();
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("expo")){
            expo = true;
            showGammaExpo();
        } else if(e.getActionCommand().equals("log")) {
            expo = false;
            hideGammaExpo();
        } else if(e.getActionCommand().equals("save")) {
            save();
        } else if(e.getActionCommand().equals("reset")) {
            setDefaultValues();
        } else if(e.getActionCommand().equals("flou3")) {
            blur3 = true;
        } else if(e.getActionCommand().equals("pasflou3")) {
            blur3 = false;
        }  else if(e.getActionCommand().equals("flou5")) {
            blur5 = true;
        } else if(e.getActionCommand().equals("pasflou5")) {
            blur5 = false;
        } else if(e.getActionCommand().equals("flou7")) {
            blur7 = true;
        } else if(e.getActionCommand().equals("pasflou7")) {
            blur7 = false;
        } else if(e.getActionCommand().equals("clean")) {
            clean = true;
        } else if(e.getActionCommand().equals("pasclean")) {
            clean = false;
        }
        refreshAll();
    }
    
    public void load()
    {
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(nomFichier)));
            expodef = (int[]) ois.readObject();
            logdef = (int[]) ois.readObject();
            expo = (Boolean) ois.readObject();
            blur5 = (Boolean) ois.readObject();
            blur3 = (Boolean) ois.readObject();
            blur7 = (Boolean) ois.readObject();
            clean = (Boolean) ois.readObject();
            bakparams = (int[]) ois.readObject();
            ImageAnalyser.bakparams = bakparams;
            houghthresh = (Double) ois.readObject();
            ImageAnalyser.houghThresh = houghthresh;
        } catch (Exception e) {
        } finally {
            if(expodef == null && logdef == null) setDefaultValues();
            else if(expodef == null) setDefaultExpo();
            else if(logdef == null) setDefaultLog();
            else return;
            save();
        }
    }
    
    public void save()
    {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(nomFichier)));
            oos.writeObject(expodef);
            oos.writeObject(logdef);
            oos.writeObject(expo);
            oos.writeObject(blur5);
            oos.writeObject(blur3);
            oos.writeObject(blur7);
            oos.writeObject(clean);
            oos.writeObject(ImageAnalyser.bakparams);
            oos.writeObject(ImageAnalyser.houghThresh);
            oos.close();
        } catch (FileNotFoundException e) {
             e.printStackTrace();
        } catch (IOException e) {
             e.printStackTrace();
        }
    }
    
    private void setDefaultValues()
    {
        setDefaultExpo();
        setDefaultLog();
    }
    
    private void setDefaultExpo()
    {
        expodef = new int[] { 200, 300, -1300, 1040};
    }
    
    private void setDefaultLog()
    {
        logdef = new int[] { 200, 1, 0, 1040};
    }
    
    private void refresh(JSpinner sp,int i)
    {
        if(expo) sp.setValue(expodef[i]);
        else sp.setValue(logdef[i]);
        //sp.repaint();
    }
    
    private void refresh(JSlider sl,int i)
    {
        if(expo) sl.setValue(expodef[i]);
        else sl.setValue(logdef[i]);
        //sl.repaint();
    }
    
    private void refreshAll()
    {
        for(int i=0; i<nb; i++) {
            refresh(s[i],i);
            refresh(p[i],i);
        }
        refreshParams();
    }
    
    private void refreshParams()
    {
        if(expo) {
            Filter.params = expodef.clone();
        } else {
            Filter.params = logdef.clone();
        }
        ImageAnalyser.expo = expo;
        ImageAnalyser.blur3 = blur3;
        ImageAnalyser.blur5 = blur5;
        ImageAnalyser.blur7 = blur7;
        ImageAnalyser.clean = clean;
    }
    
    private void hideGammaExpo()
    {
        s[nb-1].setVisible(false);
        p[nb-1].setVisible(false);
    }
    
    private void showGammaExpo()
    {
        s[nb-1].setVisible(true);
        p[nb-1].setVisible(true);
    }
    
    private void addRadioChoice(String s1,String s2,String s3,boolean yesno, boolean first)
    {
        if(s==null) return;
        JRadioButton yesButton = new JRadioButton(s2);
        JRadioButton noButton = new JRadioButton(s3);
        if(first) yesButton.setSelected(true);
        else noButton.setSelected(true);
        
        if(yesno) {
            yesButton.setActionCommand(s1);
            noButton.setActionCommand("pas"+s1);
        } else {
            yesButton.setActionCommand(s2);
            noButton.setActionCommand(s3);
        }
        yesButton.addActionListener(this);
        noButton.addActionListener(this);
        
        ButtonGroup group = new ButtonGroup();
        group.add(yesButton);
        group.add(noButton);
        
        JLabel jlab = new JLabel();
        jlab.setText(s1);
        getContentPane().add(jlab);
        getContentPane().add(yesButton);
        getContentPane().add(noButton);
    }
    
    private void addRadioChoice(String s, boolean first)
    {
        addRadioChoice(s,"active","desactive",true,first);
    }
    
    private void addRadioChoice(String s1,String s2,String s3, boolean first)
    {
        addRadioChoice(s1,s2,s3,false,first);
    }
             
}
 


