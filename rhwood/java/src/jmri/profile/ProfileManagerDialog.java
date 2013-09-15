/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.profile;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class ProfileManagerDialog extends JDialog {

    private Timer timer;

    /**
     * Creates new form ProfileManagerDialog
     */
    public ProfileManagerDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        ProfileManager.defaultManager().addPropertyChangeListener(ProfileManager.ACTIVE_PROFILE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                profiles.setSelectedValue(ProfileManager.defaultManager().getActiveProfile(), true);
                profiles.repaint();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listLabel = new JLabel();
        jScrollPane1 = new JScrollPane();
        profiles = new JList();
        btnSelect = new JButton();
        btnCreate = new JButton();
        btnUseExisting = new JButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        ResourceBundle bundle = ResourceBundle.getBundle("jmri/profile/Bundle"); // NOI18N
        setTitle(bundle.getString("ProfileManagerDialog.title")); // NOI18N
        setMinimumSize(new Dimension(310, 110));
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        listLabel.setText(bundle.getString("ProfileManagerDialog.listLabel.text")); // NOI18N

        profiles.setModel(new ProfileListModel());
        profiles.setSelectedValue(ProfileManager.defaultManager().getActiveProfile(), true);
        profiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profiles.setToolTipText(bundle.getString("ProfileManagerDialog.profiles.toolTipText")); // NOI18N
        profiles.setNextFocusableComponent(btnSelect);
        profiles.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                profilesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(profiles);
        profiles.getAccessibleContext().setAccessibleName(bundle.getString("ProfileManagerDialog.profiles.AccessibleContext.accessibleName")); // NOI18N

        btnSelect.setText(bundle.getString("ProfileManagerDialog.btnSelect.text")); // NOI18N
        btnSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });

        btnCreate.setText(bundle.getString("ProfileManagerDialog.btnCreate.text")); // NOI18N
        btnCreate.setToolTipText(bundle.getString("ProfilePreferencesPanel.btnCreateNewProfile.toolTipText")); // NOI18N
        btnCreate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });

        btnUseExisting.setText(bundle.getString("ProfileManagerDialog.btnUseExisting.text")); // NOI18N
        btnUseExisting.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnUseExistingActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(listLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnUseExisting)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCreate)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSelect))
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                    .addComponent(listLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelect)
                    .addComponent(btnCreate)
                    .addComponent(btnUseExisting))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        timer.stop();
        if (profiles.getSelectedValue() != null) {
            ProfileManager.defaultManager().setActiveProfile((Profile) profiles.getSelectedValue());
            dispose();
        }
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnCreateActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        timer.stop();
        AddProfileDialog apd = new AddProfileDialog(this, true);
        apd.setLocationRelativeTo(this);
        apd.setVisible(true);
    }//GEN-LAST:event_btnCreateActionPerformed

    private void btnUseExistingActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnUseExistingActionPerformed
        timer.stop();
        JFileChooser chooser = new JFileChooser(FileUtil.getPreferencesPath());
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new ProfileFileFilter());
        chooser.setFileView(new ProfileFileView());
        // TODO: Use NetBeans OpenDialog if its availble
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Profile p = new Profile(chooser.getSelectedFile());
                ProfileManager.defaultManager().addProfile(p);
                profiles.setSelectedValue(p, true);
                if (p.isDisabled()) {
                    // TODO: Display dialog asking if profile should be enabled
                }
            } catch (IOException ex) {
                log.warn("{} is not a profile directory", chooser.getSelectedFile());
                // TODO: Display error dialog - selected file is not a profile directory
            }
        }
    }//GEN-LAST:event_btnUseExistingActionPerformed

    private void formWindowOpened(WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        timer = new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                ProfileManager.defaultManager().setActiveProfile((Profile) profiles.getSelectedValue());
                log.info("Automatically starting with profile " + ProfileManager.defaultManager().getActiveProfile().getId() + " after timeout.");
                dispose();
            }
        });
        timer.setRepeats(false);
        if (profiles.getModel().getSize() > 0) {
            timer.start();
        }
    }//GEN-LAST:event_formWindowOpened

    private void profilesValueChanged(ListSelectionEvent evt) {//GEN-FIRST:event_profilesValueChanged
        timer.stop();
    }//GEN-LAST:event_profilesValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnCreate;
    private JButton btnSelect;
    private JButton btnUseExisting;
    private JScrollPane jScrollPane1;
    private JLabel listLabel;
    private JList profiles;
    // End of variables declaration//GEN-END:variables
    private static final Logger log = LoggerFactory.getLogger(ProfileManagerDialog.class);
}
