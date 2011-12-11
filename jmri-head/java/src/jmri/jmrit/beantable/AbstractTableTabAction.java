package jmri.jmrit.beantable;

import jmri.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import jmri.util.ConnectionNameFromSystemName;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import jmri.util.com.sun.TableSorter;

abstract public class AbstractTableTabAction extends AbstractTableAction {

    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    protected JPanel dataPanel;
    protected JTabbedPane dataTabs;

    protected boolean init = false;
    
    public AbstractTableTabAction(String s){
        super(s);
    }
    
    protected void createModel(){
        dataPanel = new JPanel();
        dataTabs = new JTabbedPane();
        dataPanel.setLayout(new BorderLayout());
        if (getManager() instanceof jmri.managers.AbstractProxyManager){
            jmri.managers.AbstractProxyManager proxy = (jmri.managers.AbstractProxyManager) getManager();
            List<jmri.Manager> managerList = proxy.getManagerList();
            tabbedTableArray.add(new TabbedTableItem("All", true, getManager(), getNewTableAction("All")));
            for(int x = 0; x<managerList.size(); x++){
                String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                TabbedTableItem itemModel = new TabbedTableItem(manuName, true, managerList.get(x), getNewTableAction(manuName));
                tabbedTableArray.add(itemModel);
            }
        } else {
            String manuName = ConnectionNameFromSystemName.getConnectionName(getManager().getSystemPrefix());
            tabbedTableArray.add(new TabbedTableItem(manuName, true, getManager(), getNewTableAction(manuName)));
        }
        for(int x = 0; x<tabbedTableArray.size(); x++){
            AbstractTableAction table = tabbedTableArray.get(x).getAAClass();
            table.addToPanel(this);
            dataTabs.addTab(tabbedTableArray.get(x).getItemString(),null, tabbedTableArray.get(x).getPanel(),null);
        }
        dataTabs.addChangeListener(new ChangeListener() { 
            public void stateChanged(ChangeEvent evt) { 
                setMenuBar(f);
            }
        });
        dataPanel.add(dataTabs, BorderLayout.CENTER);
        init = true;
    }
    
    abstract protected Manager getManager();
    
    abstract protected AbstractTableAction getNewTableAction(String choice);
    
    @Override
    public JPanel getPanel(){
        if (!init)
            createModel();
        return dataPanel;
    }

    protected ArrayList<TabbedTableItem> tabbedTableArray = new ArrayList<TabbedTableItem>();
    
    protected JTable makeJTable(TableSorter sorter) {
        return new JTable(sorter)  {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                boolean res = super.editCellAt(row, column, e);
                java.awt.Component c = this.getEditorComponent();
                if (c instanceof javax.swing.JTextField) {
                    ( (JTextField) c).selectAll();
                }            
                return res;
            }
        };
    }
    protected void setTitle() {
        //atf.setTitle("multiple sensors");
    }
    
    abstract protected String helpTarget();
    
    protected void addPressed(ActionEvent e) {
        log.warn("This should not have happened");
    }
    
    public void addToFrame(BeanTableFrame f){
        try {
            tabbedTableArray.get(dataTabs.getSelectedIndex()).getAAClass().addToFrame(f);
        } catch (ArrayIndexOutOfBoundsException ex){
            log.error(ex.toString() + " in add to Frame "  + dataTabs.getSelectedIndex() + " " + dataTabs.getSelectedComponent());
        }
    }
    
    public void setMenuBar(BeanTableFrame f){
        try {
            tabbedTableArray.get(dataTabs.getSelectedIndex()).getAAClass().setMenuBar(f);
        } catch (ArrayIndexOutOfBoundsException ex){
            log.error(ex.toString()  + " in add to Menu " + dataTabs.getSelectedIndex() + " " + dataTabs.getSelectedComponent());
        }
    }
    
    public void addToBottomBox(JComponent c, String str){
        for(int x = 0; x<tabbedTableArray.size(); x++){
            if (tabbedTableArray.get(x).getItemString().equals(str))
                tabbedTableArray.get(x).addToBottomBox(c);
        }
    }
    
    public void print(javax.swing.JTable.PrintMode mode, java.text.MessageFormat headerFormat, java.text.MessageFormat footerFormat){
        try {
            tabbedTableArray.get(dataTabs.getSelectedIndex()).getDataTable().print(mode, headerFormat, footerFormat);
        } catch (java.awt.print.PrinterException e1) {
            log.warn("error printing: "+e1,e1);
        } catch ( NullPointerException ex) {
            log.error("Trying to print returned a NPE error");
        }
    }
    
    public void dispose(){
        for(int x=0; x<tabbedTableArray.size(); x++){
            tabbedTableArray.get(x).dispose();
        }
        super.dispose();
    }
   
    protected class TabbedTableItem {
        
        AbstractTableAction tableAction;
        String itemText;
        BeanTableDataModel dataModel;
        JTable dataTable;
        JScrollPane dataScroll;
        Box bottomBox;
        Boolean AddToFrameRan = false;
        Manager manager;

        int bottomBoxIndex;	// index to insert extra stuff
        static final int bottomStrutWidth = 20;
        
        boolean standardModel = true;
        
        final JPanel dataPanel = new JPanel();
        
        public TabbedTableItem(String choice, boolean stdModel, Manager manager, AbstractTableAction tableAction){

            this.tableAction = tableAction;
            itemText = choice;
            standardModel=stdModel;
            this.manager = manager;

            //If a panel model is used, it should really add to the bottom box
            //but it can be done this way if required.
            bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;
            dataPanel.setLayout(new BorderLayout());
            if (stdModel)
                createDataModel();
            else
                addPanelModel();
        }
        
        void createDataModel(){
            if (manager!=null)
                tableAction.setManager(manager);
            dataModel = tableAction.getTableDataModel();
            TableSorter sorter = new TableSorter(dataModel);
            dataTable = makeJTable(sorter);
            sorter.setTableHeader(dataTable.getTableHeader());
            dataScroll	= new JScrollPane(dataTable);
            
            try {
                TableSorter tmodel = ((TableSorter)dataTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(BeanTableDataModel.SYSNAMECOL, TableSorter.ASCENDING);
            } catch (java.lang.ClassCastException e) {}  // happens if not sortable table
            
            dataModel.configureTable(dataTable);
            
            java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
            // width is right, but if table is empty, it's not high
            // enough to reserve much space.
            dataTableSize.height = Math.max(dataTableSize.height, 400);
            dataScroll.getViewport().setPreferredSize(dataTableSize);
            
            // set preferred scrolling options
            dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            
            //dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.add(dataScroll, BorderLayout.CENTER);
            
            dataPanel.add(bottomBox, BorderLayout.SOUTH);
            JButton addButton = new JButton(rbean.getString("ButtonAdd"));
            addToBottomBox(addButton);
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tableAction.addPressed(e);
                }
            });       
        }
        
        void addPanelModel(){
            dataPanel.add(tableAction.getPanel(), BorderLayout.CENTER);
            dataPanel.add(bottomBox, BorderLayout.SOUTH);
        }
        
        public boolean getStandardTableModel(){ return standardModel; }
        
        public String getItemString(){
            return itemText;
        }
        
        public AbstractTableAction getAAClass(){
            return tableAction;
        }
        
        public JPanel getPanel() {
            return dataPanel;
        }
        
        public boolean getAdditionsToFrameDone() { return AddToFrameRan; }
        
        public void setAddToFrameRan() { AddToFrameRan=true; }
        
        public JTable getDataTable(){ 
            return dataTable;
        }
        
        protected void addToBottomBox(JComponent comp) {
            try {
                bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
                ++bottomBoxIndex;
                bottomBox.add(comp, bottomBoxIndex);
                ++bottomBoxIndex;
            } catch (java.lang.IllegalArgumentException ex){
                log.error (ex);
            }
        }
        
        protected void dispose(){
            if (dataModel != null)
                dataModel.dispose();
            dataModel = null;
            dataTable = null;
            dataScroll = null;
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractTableTabAction.class.getName());

}
