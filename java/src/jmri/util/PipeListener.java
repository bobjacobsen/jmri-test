// PipeListener.java
package jmri.util;

import java.io.IOException;
import java.io.PipedReader;

/**
 * Small service class to read characters from a pipe and post them to a
 * JTextArea for display
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version $Revision$
 */
class PipeListener extends Thread {

    private PipedReader pr;
    private javax.swing.JTextArea ta;

    public PipeListener(PipedReader pr, javax.swing.JTextArea ta) {
        this.pr = pr;
        this.ta = ta;
    }

    public void run() {
        try {
            char[] c = new char[1];
            while (true) {
                try {
                    c[0] = (char) pr.read();
                    ta.append(new String(c));   // odd way to do this, but only
                    // way I could think of with only one
                    // new object created
                } catch (IOException ex) {
                    if (ex.getMessage().equals("Write end dead") || ex.getMessage().equals("Pipe broken")) {
                        // happens when the writer thread, e.g. a script, terminates
                        synchronized (this) {
                            try {
                                wait(500);
                            } catch (InterruptedException exi) {
                                Thread.currentThread().interrupt(); // retain if needed later
                            }
                        }
                    } else {
                        throw ex;
                    }
                }
            }
        } catch (IOException ex) {
            ta.append("PipeListener Exiting on IOException:" + ex);
        }
    }
}
