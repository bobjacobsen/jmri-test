// Pr1ExportAction.java

package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Action to export the CV values to a PR1DOS data file.
 * <P>
 * Note that this format is somewhat different from the PR1WIN format, and
 * it's not clear they will interoperate.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision: 1.5 $
 */
public class Pr1ExportAction  extends AbstractAction {

    /**
     * Create the action
     * @param actionName String name to be displayed in menus, etc
     * @param pModel  CvTableModel that contains the data to (eventually) be exported
     * @param pParent JFrame that will eventually invoke the action, used to anchor a file dialog
     */
    public Pr1ExportAction(String actionName, CvTableModel pModel, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent ;
    }

    JFileChooser fileChooser ;
    JFrame mParent ;

    /**
     * CvTableModel to load
     */
    CvTableModel mModel;


    public void actionPerformed(ActionEvent e) {

        if ( fileChooser == null ){
            fileChooser = new JFileChooser() ;
        }

        int retVal = fileChooser.showSaveDialog( mParent ) ;

        if(retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled())  log.debug("start to export to PR1 file "+file);

            try {

                PrintStream str = new PrintStream(new FileOutputStream(file));

                str.println("[DecoderData]");
                for (int i=1; i<=256; i++) {
                    int lowCvIndex = i;
                    CvValue cv1 = (CvValue)mModel.allCvVector().elementAt(lowCvIndex);
                    int value1 = (cv1!=null) ? cv1.getValue() : 0;

                    str.println("CV"+i+"="+value1);
                }
                str.flush();
                str.close();
            }
            catch (IOException ex) {
                log.error("Error writing file: "+ex);
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Pr1ExportAction.class.getName());
}
