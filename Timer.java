/**
 * Classe permettant de mesurer le temps d'execution d'une fonction
 */
public class Timer
{
    private long debut = 0;
    private long fin = 0;
    private boolean stopped = false;

    public void start() {
        debut = System.nanoTime();
        stopped = false;
    }
    
    public void stop() {
        fin = System.nanoTime();
        stopped = true;
    }
    
    public void stop(String s) {
        stop();
        System.out.print(s + " ");
        printTime();
    }
    
    public long getTime() {
        long time = 0;
        if(stopped) time = fin - debut;
        else time = System.nanoTime()-debut;
        if(time > 0) return time;
        else return 0;
    }
    
    public long getTimeInMs() {
        return getTime()/1000000;
    }
    
    public boolean moreThan(long t) {
        return ( getTime() >= t );
    }
    
    public void printTime() {
        System.out.println(getTime()+"ns");
    }
    
}