// PerformActionPanel.java

package apps;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.*;

/**
 * Provide a GUI for configuring PerformActionModel objects.
 * <P>
 * A PerformModel object invokes a Swing Action when
 * the program is started.
 * <P>
 *
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.2 $
 * @see apps.PerformActionModel
 */
public class PerformActionPanel extends JPanel {

    JPanel self;  // used for synchronization
    protected ResourceBundle rb;

    public PerformActionPanel() {
        self = this;

        rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

        // GUi is a series of horizontal entries
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add existing items

        JButton addButton = new JButton(rb.getString("ButtonAdd"));
        JPanel panel = new JPanel();  // button is a horizontal item too; expands to fill BoxLayout
        panel.setLayout(new FlowLayout());
        panel.add(addButton);
        add(panel);
        addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addItem();
                }
            }
        );

        // are there any existing objects from reading existing config?
        int n = PerformActionModel.rememberedObjects().size();
        for (int i = 0; i< n; i++) {
            PerformActionModel m = (PerformActionModel) PerformActionModel.rememberedObjects().get(i);
            add(new Item(m));
        }
    }

    protected void addItem() {
        synchronized(self) {
            add(new Item());
            validate();
            if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
        }
    }

    public class Item extends JPanel implements ActionListener {
        JButton removeButton = new JButton(rb.getString("ButtonRemove"));
        Item() {
            setLayout(new FlowLayout());
            add(removeButton);
            removeButton.addActionListener(this);
            // create the list of possibilities
            selections = new JComboBox(PerformActionModel.nameList());
            add(selections);
        }
        Item(PerformActionModel m) {
            this();
            model = m;
            selections.setSelectedItem(m.getName());
        }

        PerformActionModel model = new PerformActionModel();
        JComboBox selections;

        public PerformActionModel updatedModel() {
            model.setName((String)selections.getSelectedItem());
            return model;
        }

        public void actionPerformed(ActionEvent e) {
            synchronized (self) {
                // remove this item from display
                Container parent = this.getParent();  // have to do this before remove
                Component topParent = this.getTopLevelAncestor();
                parent.remove(this);
                parent.validate();
                if (topParent!=null) ((JFrame)topParent).pack();
                parent.repaint();
                // unlink to encourage garbage collection
                removeButton.removeActionListener(this);
                model = null;
            }
        }
    }
}


