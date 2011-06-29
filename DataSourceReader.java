/**
 * Classe d'exemple trouve sur le site de Sun
 * Puis modifier selon nos convenances
 * Cree un Processor
 * Recupere le flux DataSource et selectionne le Format
 * Cree un Handler pour lire la DataSource
 */
/*
 * @(#)DataSourceReader.java    1.2 01/03/13
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.awt.*;
import javax.swing.*;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.datasink.*;
import javax.media.control.MonitorControl;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.control.FormatControl;
import javax.media.control.TrackControl;

/**
 * Sample program to read data from an output DataSource of a Processor.
 */
public class DataSourceReader extends Frame implements ControllerListener, DataSinkListener 
{

    private Processor p;
    private DataSourceHandler handler;
    private Object waitSync = new Object();
    private boolean stateTransitionOK = true;
    private boolean monitorOn = false;
    private boolean printInfoOn = false;
    private static final float framerate = 10;
    private Format format;

    public DataSourceReader(Format format)
    {
        super("Webcam");
        this.format = format;
    }
    
    /**
     * Given a DataSource, create a processor and hook up the output
     * DataSource from the processor to a customed DataSink.
     */
    public boolean open(DataSource ds) {

    if(printInfoOn) System.err.println("create processor for: " + ds.getContentType());
    
    if (monitorOn) {
        // If monitoring is on, we'd like to enable synchronization
        // by enabling the use of the RawSyncBufferMux.  The default
        // is RawBufferMux which does not perform sychronization.
        enableSyncMux();
    }
    
    //Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, new Boolean(true));
    
    try {
        p = Manager.createProcessor(ds);
    } catch (Exception e) {
        System.err.println("Failed to create a processor from the given DataSource: " + e);
        return false;
    }
    
    p.addControllerListener(this);

    // Put the Processor into configured state.
    p.configure();
    if (!waitForState(p.Configured)) {
        System.err.println("Failed to configure the processor.");
        return false;
    }
    
    setFormat(format);

    // Get the raw output from the processor.
    p.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW));

    p.realize();
    if (!waitForState(p.Realized)) {
        System.err.println("Failed to realize the processor.");
        return false;
    }

    // Get the output DataSource from the processor and
    // hook it up to the DataSourceHandler.
    DataSource ods = p.getDataOutput();
    handler = new DataSourceHandler();

    try {
        handler.setSource(ods);
    } catch (IncompatibleSourceException e) {
        System.err.println("Cannot handle the output DataSource from the processor: " + ods);
        return false;
    }

    handler.addDataSinkListener(this);
    handler.start();

    // Prefetch the processor.
    p.prefetch();
    if (!waitForState(p.Prefetched)) {
        System.err.println("Failed to prefetch the processor.");
        return false;
    }

    if (monitorOn) {

        // If monitor is on, try to obtain any monitor controls 
        // from the processor and display them.

        setLayout(new BorderLayout());

        Control controls[] = p.getControls();
        Panel monitorComp = null;
        Panel monitorPanel = null;
        
        for (int i = 0; i < controls.length; i++) {
        if (controls[i] instanceof com.sun.media.controls.MonitorAdapter) {
            MonitorControl mc = (MonitorControl)controls[i];
            monitorComp = (Panel) mc.getControlComponent();
            Component[] tabc = monitorComp.getComponents();
            for(int j=0; j<tabc.length; j++) if( tabc[j] instanceof Checkbox) tabc[j].setVisible(false);
            if (monitorPanel == null) {
            monitorPanel = new Panel();
            monitorPanel.setLayout(new GridLayout(0, 1));
            }
            if (monitorComp != null)
            monitorPanel.add(monitorComp);
            //mc.setPreviewFrameRate(framerate);
            mc.setEnabled(true);
        }
        }
        
        if (monitorPanel != null) add("Center", monitorPanel);
        /*
        // Display the processor's control component.
        Component cc;
        if ((cc = p.getControlPanelComponent()) != null) {
            getContentPane().add("South", cc);
        }
        */
        addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e)
            {
                stop();
                setVisible( false );
                System.exit(0);
            }
        });
        
        // Start the processor.
        p.start();
        setVisible(true);
    }
    
    addWindowListener(new WindowAdapter(){ public void windowClosing(WindowEvent e){System.exit(0);} });

    return true;
    }

    public void addNotify() {
    super.addNotify();
    pack();
    }
    
    /**
     * Procedure rajoute permettant de selectionner le format desire
     */
    void setFormat(Format format) 
    {
        TrackControl[] tc = p.getTrackControls();
        FormatControl fc = (FormatControl)tc[0];
        Format[] formats = fc.getSupportedFormats();
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].matches(format)) {
                format = formats[i].intersects(format);
                fc.setFormat(format);
                break;
            }
        }
    }


    /**
     * Change the plugin list to disable the default RawBufferMux 
     * thus allowing the RawSyncBufferMux to be used.
     * This is a handy trick.  You wouldn't know this, would you? :)
     */
    void enableSyncMux() {
    Vector muxes = PlugInManager.getPlugInList(null, null, 
                    PlugInManager.MULTIPLEXER);
    for (int i = 0; i < muxes.size(); i++) {
        String cname = (String)muxes.elementAt(i);
        if (cname.equals("com.sun.media.multiplexer.RawBufferMux")) {
        muxes.removeElementAt(i);
        break;
        }
    }
    PlugInManager.setPlugInList(muxes, PlugInManager.MULTIPLEXER);
    }


    /**
     * Block until the processor has transitioned to the given state.
     * Return false if the transition failed.
     */
    boolean waitForState(int state) {
    synchronized (waitSync) {
        try {
        while (p.getState() < state && stateTransitionOK)
            waitSync.wait();
        } catch (Exception e) {}
    }
    return stateTransitionOK;
    }


    /**
     * Controller Listener.
     */
    public void controllerUpdate(ControllerEvent evt) {

    if (evt instanceof ConfigureCompleteEvent ||
        evt instanceof RealizeCompleteEvent ||
        evt instanceof PrefetchCompleteEvent) {
        synchronized (waitSync) {
        stateTransitionOK = true;
        waitSync.notifyAll();
        }
    } else if (evt instanceof ResourceUnavailableEvent) {
        synchronized (waitSync) {
        stateTransitionOK = false;
        waitSync.notifyAll();
        }
    } else if (evt instanceof EndOfMediaEvent) {
        p.close();
    } else if (evt instanceof SizeChangeEvent) {
    }
    }


    /**
     * DataSink Listener
     */
    public void dataSinkUpdate(DataSinkEvent evt) {

    if (evt instanceof EndOfStreamEvent) {
        System.err.println("All done!");
        evt.getSourceDataSink().close();
        System.exit(0);
    }
    }
    
    public void setMonitor(boolean enable) {
        monitorOn = enable;
    }

    /**
     * Stops the transmission if already started
     */
    public void stop() {
        synchronized (this) {
            if (p != null) {
                p.stop();
                p.close();
                p = null;
            }
        }
    }
    
    public DataSourceHandler getDataSourceHandler()
    {
        return handler;
    }
    
    
    
    
    
    
    
    
    
    


    /***************************************************
     * Inner class
     *
     ***************************************************/

/**
 * This DataSourceHandler class reads from a DataSource and display
 * information of each frame of data received.
 */
class DataSourceHandler implements DataSink, BufferTransferHandler 
{
    DataSource source;
    PullBufferStream pullStrms[] = null;
    PushBufferStream pushStrms[] = null;

    // Data sink listeners.
    private Vector listeners = new Vector(1);

    // Stored all the streams that are not yet finished (i.e. EOM
    // has not been received.
    SourceStream unfinishedStrms[] = null;

    // Loop threads to pull data from a PullBufferDataSource.
    // There is one thread per each PullSourceStream.
    Loop loops[] = null;

    volatile Buffer readBuffer;

    /**
     * Sets the media source this <code>MediaHandler</code>
     * should use to obtain content.
     */
    public void setSource(DataSource source) throws IncompatibleSourceException 
    {
        // Different types of DataSources need to handled differently.
        if (source instanceof PushBufferDataSource) 
        {

            pushStrms = ((PushBufferDataSource)source).getStreams();
            unfinishedStrms = new SourceStream[pushStrms.length];

            // Set the transfer handler to receive pushed data from
            // the push DataSource.
            for (int i = 0; i < pushStrms.length; i++) 
            {
                pushStrms[i].setTransferHandler(this);
                unfinishedStrms[i] = pushStrms[i];
            }

        } else if (source instanceof PullBufferDataSource) {

            pullStrms = ((PullBufferDataSource)source).getStreams();
            unfinishedStrms = new SourceStream[pullStrms.length];

            // For pull data sources, we'll start a thread per
            // stream to pull data from the source.
            loops = new Loop[pullStrms.length];
            for (int i = 0; i < pullStrms.length; i++) 
            {
                loops[i] = new Loop(this, pullStrms[i]);
                unfinishedStrms[i] = pullStrms[i];
            }

        } else {
            // This handler only handles push or pull buffer datasource.
            throw new IncompatibleSourceException();
        }

        this.source = source;
        readBuffer = new Buffer();
    }


    /**
     * For completeness, DataSink's require this method.
     * But we don't need it.
     */
    public void setOutputLocator(MediaLocator ml) { }


    public MediaLocator getOutputLocator() 
    {
        return null;
    }


    public String getContentType() 
    {
        return source.getContentType();
    }


    /**
     * Our DataSink does not need to be opened.
     */
    public void open() { }


    public void start() 
    {
        try {
        source.start();
        } catch (IOException e) {
        System.err.println(e);
        }

        // Start the processing loop if we are dealing with a
        // PullBufferDataSource.
        if (loops != null) {
        for (int i = 0; i < loops.length; i++)
            loops[i].restart();
        }
    }


    public void stop() 
    {
        try {
        source.stop();
        } catch (IOException e) {
        System.err.println(e);
        }

        // Start the processing loop if we are dealing with a
        // PullBufferDataSource.
        if (loops != null) {
        for (int i = 0; i < loops.length; i++)
            loops[i].pause();
        }
    }


    public void close() 
    {
        stop();
        if (loops != null) {
        for (int i = 0; i < loops.length; i++)
            loops[i].kill();
        }
    }

    
    public void addDataSinkListener(DataSinkListener dsl) 
    {
        if (dsl != null)
        if (!listeners.contains(dsl))
            listeners.addElement(dsl);
    }


    public void removeDataSinkListener(DataSinkListener dsl) 
    {
        if (dsl != null)
        listeners.removeElement(dsl);
    }


    protected void sendEvent(DataSinkEvent event) 
    {
        if (!listeners.isEmpty()) {
        synchronized (listeners) {
            Enumeration list = listeners.elements();
            while (list.hasMoreElements()) {
            DataSinkListener listener = 
                (DataSinkListener)list.nextElement();
            listener.dataSinkUpdate(event);
            }
        }
        }
    }


    /**
     * This will get called when there's data pushed from the
     * PushBufferDataSource.
     */
    public void transferData(PushBufferStream stream) 
    {

        try {
        stream.read(readBuffer);
        } catch (IOException e) {
        System.err.println(e);
        sendEvent(new DataSinkErrorEvent(this, e.getMessage()));
        return;
        }

        if(printInfoOn) printDataInfo(readBuffer);

        // Check to see if we are done with all the streams.
        if (readBuffer.isEOM() && checkDone(stream)) {
        sendEvent(new EndOfStreamEvent(this));
        }
    }


    /**
     * This is called from the Loop thread to pull data from
     * the PullBufferStream.
     */
    public boolean readPullData(PullBufferStream stream) 
    {
        try {
        stream.read(readBuffer);
        } catch (IOException e) {
        System.err.println(e);
        return true;
        }
        
        if(printInfoOn) printDataInfo(readBuffer);

        if (readBuffer.isEOM()) {
            // Check to see if we are done with all the streams.
        if (checkDone(stream)) {
            if(printInfoOn) System.err.println("All done!");
            close();
        }
        return true;
        }
        return false;
    }


    /**
     * Check to see if all the streams are processed.
     */
    public boolean checkDone(SourceStream strm) 
    {
        boolean done = true;

        for (int i = 0; i < unfinishedStrms.length; i++) {
        if (strm == unfinishedStrms[i])
            unfinishedStrms[i] = null;
        else if (unfinishedStrms[i] != null) {
            // There's at least one stream that's not done.
            done = false;
        }
        }
        return done;
    }


    void printDataInfo(Buffer buffer) 
    {
        if (buffer.getFormat() instanceof VideoFormat) {
            System.err.println("Read video data:");
            System.err.println("  Time stamp: " + buffer.getTimeStamp());
            System.err.println("  Sequence #: " + buffer.getSequenceNumber());
            System.err.println("  Data length: " + buffer.getLength());
        }

        if (buffer.isEOM())
        System.err.println("  Got EOM!");
    }

    public Object [] getControls() 
    {
        return new Object[0];
    }

    public Object getControl(String name) 
    {
        return null;
    }
    
    public synchronized Buffer getBuffer()
    {
        return readBuffer;   
    }
    
    public DataSource getDataSource()
    {
        return source;   
    }
    
    public void killAllThreads()
    {
        for(int i=0; i<loops.length; i++) loops[i].kill();
    }
    
}





/**
 * A thread class to implement a processing loop.
 * This loop reads data from a PullBufferDataSource.
 */
class Loop extends Thread 
{

    DataSourceHandler handler;
    PullBufferStream stream;
    boolean paused = true;
    boolean killed = false;

    public Loop(DataSourceHandler handler, PullBufferStream stream) 
    {
        this.handler = handler;
        this.stream = stream;
        start();
    }

    public synchronized void restart() 
    {
        paused = false;
        notify();
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

    /**
     * This is the processing loop to pull data from a 
     * PullBufferDataSource.
     */
    public void run() 
    {
        while (!killed) {
            try {
                while (paused && !killed) {
                    wait(5);
                }
            } catch (InterruptedException e) {}

            if (!killed) {
                boolean done = handler.readPullData(stream);
                
                if (done) pause();
            }
        }
    }
    
    
}


}

