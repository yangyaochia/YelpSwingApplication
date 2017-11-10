/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yelp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
//import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import oracle.jdbc.internal.OraclePreparedStatement;

/**
 *
 * @author yangy
 */
public class hw3 extends javax.swing.JFrame {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private HashSet<String> mainCategoriesSet = new HashSet();
    private HashSet<String> subCategoriesSet = new HashSet();
    private HashSet<String> attributesSet = new HashSet();
    private String location = null;
    private String day = null;
    private String from = null;
    private String to = null;
    private String id = null;
    private StringBuilder mainCategoriesString = new StringBuilder();
    private StringBuilder subCategoriesString = new StringBuilder();
    private StringBuilder attributesString = new StringBuilder();
    
    private final String DB_USER = "system";
    private final String DB_PASS = "coen280hw3";
    private final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private Connection con;
    
    private List<String> dayOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    private class DayComparator implements Comparator<String> {
        public int compare(String d1, String d2) {
            return dayOrder.indexOf(d1) - dayOrder.indexOf(d2);
        }
    }
    
    private Connection getConnect() throws SQLException, ClassNotFoundException{
        System.out.println("Checking JDBC...");
        Class.forName(JDBC_DRIVER);
        System.out.println("Connecting to database...");
        return DriverManager.getConnection(ORACLE_URL, DB_USER, DB_PASS);
    }
    private void closeConnection(Connection con) {
        try {
            con.close();
            System.out.println("Disconnection Successful.");
        } catch (SQLException e) {
            System.err.println("Cannot close connection");
        } 
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public hw3() {
        initComponents();
        try {
            init();
        } catch (SQLException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    private void init() throws SQLException, ClassNotFoundException {
        System.out.println("+++init+++");
        con = getConnect();
        System.out.println("Connection successful");
        StringBuilder sql = new StringBuilder();
        PreparedStatement preparedStatement;
        ResultSet rs;
            
        //init mainCategory
        sql.append("SELECT DISTINCT mainCategory").append("\n")
           .append("FROM MainCategory").append("\n")
           .append("ORDER BY mainCategory");
        preparedStatement = con.prepareStatement(sql.toString());
        rs = preparedStatement.executeQuery();
        while (rs.next()) {
            String mainCategoryName = rs.getString(rs.findColumn("mainCategory"));
            JCheckBox mc = new JCheckBox(mainCategoryName);
            mc.addMouseListener(new MouseListener(){                                   
                @Override
                public void mouseClicked(MouseEvent e) {
  
                    JCheckBox mc = (JCheckBox) e.getSource();
                    String mainCategory = mc.getText();
                    if (mc.isSelected()) {
                        mainCategoriesSet.add(mainCategory);
                    }
                    else {
                        mainCategoriesSet.remove(mainCategory);
                    }
                    // get mainCategories from hashSet to arrayList
                    mainCategoriesString.setLength(0);
                    Iterator<String> it = mainCategoriesSet.iterator();
                    while (it.hasNext()) {
                        mainCategoriesString.append("'").append(it.next()).append("',");
                    }
                    // Remove the last "," if there is at least one element
                    if (mainCategoriesString.length() > 0) {
                        mainCategoriesString.deleteCharAt(mainCategoriesString.length() - 1);
                    }
                    System.out.println("DEBUG=========== select mainCategories: " + mainCategoriesString.toString());
                    try {
                        attributeListPanel.removeAll();
                        attributesSet.clear();
                        getSubCategories();
                        getLocations();
                        getDays();
                        getFromTime();
                        getToTime();
                    } catch (SQLException | ClassNotFoundException ex) {
                        Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                @Override
                public void mousePressed(MouseEvent e) {
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                }
                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            mainCategoryListPanel.add(mc);
        }                   
        rs.close();
        preparedStatement.close();
    }
    
    private void getSubCategories() throws SQLException, ClassNotFoundException {
        //try (Connection connection = populate.getConnect()) {
            subCategoryListPanel.removeAll();
            subCategoriesSet.clear();

            System.out.println("Get subCategories...");
            
            StringBuilder sql = new StringBuilder();
            StringBuilder main_sql = getMainCategoriesSQL();
            PreparedStatement preparedStatement;
            ResultSet rs;
            //System.out.println(main_sql);
            List<String> subCategoriesList = new ArrayList();    
            if (!mainCategoriesSet.isEmpty()) {
                sql.setLength(0);
                sql.append("SELECT DISTINCT sc.subCategory").append("\n")
                   .append("FROM SubCategory sc").append("\n")
                   .append("WHERE sc.business_id IN (").append(main_sql).append(")\n");
                   //.append("ORDER BY sc.subCategory");
                //System.out.println(sql.toString());
                //System.out.println(mainCategoryComboBox.getSelectedIndex());
                preparedStatement = con.prepareStatement(sql.toString());
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    String subCategory = rs.getString(rs.findColumn("subCategory"));
                    subCategoriesList.add(subCategory);
                }
                rs.close();
                preparedStatement.close();
            }
            Collections.sort(subCategoriesList);
            for (String scName: subCategoriesList) {
                JCheckBox sc = new JCheckBox(scName);
                
                sc.addMouseListener(new MouseListener(){                                   
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JCheckBox sc = (JCheckBox) e.getSource();
                        String subCategory = sc.getText();
                        if (sc.isSelected()) {
                            subCategoriesSet.add(subCategory);
                        }
                        else {
                            subCategoriesSet.remove(subCategory);
                        }
                        subCategoriesString.setLength(0);
                        Iterator<String> it = subCategoriesSet.iterator();
                        while (it.hasNext()) {
                            subCategoriesString.append("'").append(it.next()).append("',");
                        }
                        if (subCategoriesString.length() > 0) {
                            subCategoriesString.deleteCharAt(subCategoriesString.length() - 1);
                        }
                        System.out.println("DEBUG=========== select subCategories: " + subCategoriesString.toString() + "\n");
                        try {
                            getLocations();
                            getDays();
                            getFromTime();
                            getToTime();
                            getAttributes();
                        } catch (SQLException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
                subCategoryListPanel.add(sc);
            }
            subCategoryListPanel.updateUI();
        //}
    }
    private void getAttributes() throws SQLException, ClassNotFoundException {
        //try (Connection connection = populate.getConnect()) {
        attributeListPanel.removeAll();
        attributesSet.clear();
        System.out.println("Get attributes...");
            
            StringBuilder sql = new StringBuilder();
            StringBuilder main_sql = getMainCategoriesSQL();
            StringBuilder sub_sql = getSubCategoriesSQL();
            PreparedStatement preparedStatement;
            ResultSet rs;

            //System.out.println(main_sql);
            List<String> attributesList = new ArrayList();   
            if (!subCategoriesSet.isEmpty()) {
                sql.setLength(0);
                sql.append("SELECT DISTINCT a.attribute").append("\n")
                   .append("FROM Attribute a").append("\n")
                   .append("WHERE a.business_id IN (").append(main_sql).append(") AND \n")
                   .append("a.business_id IN (").append(sub_sql).append(")\n");
                   //.append("ORDER BY a.attribute");
                //System.out.println(sql.toString());
                //System.out.println(mainCategoryComboBox.getSelectedIndex());
                preparedStatement = con.prepareStatement(sql.toString());
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    String attribute = rs.getString(rs.findColumn("attribute"));
                    attributesList.add(attribute);
                }
                rs.close();
                preparedStatement.close();
            }
 
            Collections.sort(attributesList);
            for (String aName: attributesList) {
                JCheckBox a = new JCheckBox(aName);
                
                a.addMouseListener(new MouseListener(){                                   
                    @Override
                    public void mouseClicked(MouseEvent e) {

                        locationComboBox.removeAllItems();
                        location = null;
                        
                        JCheckBox a = (JCheckBox) e.getSource();
                        String attribute = a.getText();
                        if (a.isSelected()) {
                            attributesSet.add(attribute);
                        }
                        else {
                            attributesSet.remove(attribute);
                        }
                        attributesString.setLength(0);
                        Iterator<String> it = attributesSet.iterator();
                        while (it.hasNext()) {
                            attributesString.append("'").append(it.next()).append("',");
                        }
                        if (attributesString.length() > 0) {
                            attributesString.deleteCharAt(attributesString.length() - 1);
                        } 
                        System.out.println("DEBUG=========== attributes: " + attributesString.toString());
                        try {
                            getLocations();
                            getDays();
                            getFromTime();
                            getToTime();
                        } catch (SQLException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
                attributeListPanel.add(a);
            }
            attributeListPanel.updateUI();
        //}
    }
    private void getLocations() throws SQLException, ClassNotFoundException {
        //try (Connection connection = populate.getConnect()) {
        locationComboBox.removeAllItems();
        location = null;
        
        StringBuilder sql = new StringBuilder();
        StringBuilder main_sql = getMainCategoriesSQL();
        StringBuilder sub_sql = getSubCategoriesSQL();
        StringBuilder a_sql = getAttributesSQL();
        StringBuilder d_sql = getDaySQL();
        StringBuilder f_sql = getFromSQL();
        StringBuilder t_sql = getToSQL();        
        PreparedStatement preparedStatement;
        ResultSet rs;
        
        if ( !mainCategoriesSet.isEmpty()) {
        sql.append("SELECT b.state, b.city").append("\n")
            .append("FROM Business b").append("\n")
            .append("WHERE b.business_id IN (").append(main_sql).append(")\n");

            if ( !subCategoriesSet.isEmpty())
                sql.append(" AND b.business_id IN (").append(sub_sql).append(")\n");
            if ( !attributesSet.isEmpty())
                sql.append(" AND b.business_id IN (").append(a_sql).append(")\n");
            if ( day != null )
                sql.append(" AND b.business_id IN (").append(d_sql).append(")\n");
            if ( from != null )
                sql.append(" AND b.business_id IN (").append(f_sql).append(")\n");
            if ( to != null )
                sql.append(" AND b.business_id IN (").append(t_sql).append(")\n");
            sql.append("GROUP BY b.state, b.city").append("\n");
            //System.out.println(sql.toString());
            preparedStatement = con.prepareStatement(sql.toString());
            rs = preparedStatement.executeQuery();
            List<String> locationsList = new ArrayList();
            
            while (rs.next()) {
                String state = rs.getString(rs.findColumn("state"));
                String city = rs.getString(rs.findColumn("city"));
                locationsList.add(state + ", " + city);
            //System.out.println(state + ", " + city);
            }
            Collections.sort(locationsList);
            locationsList.add(0,"Select the location");
            String [] stateArray = new String[locationsList.size()];
            stateArray = locationsList.toArray(stateArray);
            locationComboBox.setModel(new javax.swing.DefaultComboBoxModel(stateArray));
        }
        
    }
    private void getDays() throws SQLException, ClassNotFoundException {
        dayComboBox.removeAllItems();
        day = null;            
        StringBuilder sql = new StringBuilder();
        StringBuilder main_sql = getMainCategoriesSQL();
        StringBuilder sub_sql = getSubCategoriesSQL();
        StringBuilder a_sql = getAttributesSQL();
        StringBuilder l_sql = getLocationsSQL();
        StringBuilder f_sql = getFromSQL();
        StringBuilder t_sql = getToSQL();
        PreparedStatement preparedStatement;
        ResultSet rs;
        if ( !mainCategoriesSet.isEmpty()) {
            sql.append("SELECT DISTINCT o.day").append("\n")
                .append("FROM OpenHour o").append("\n")
                .append("WHERE o.business_id IN (").append(main_sql).append(")\n");
            if ( !subCategoriesSet.isEmpty())
                sql.append(" AND o.business_id IN (").append(sub_sql).append(")\n");
            if ( !attributesSet.isEmpty())
                sql.append(" AND o.business_id IN (").append(a_sql).append(")\n");
            if ( location != null )
                sql.append(" AND o.business_id IN (").append(l_sql).append(")\n");
            if ( from != null )
                sql.append(" AND o.business_id IN (").append(f_sql).append(")\n");
            if ( to != null )
                sql.append(" AND o.business_id IN (").append(t_sql).append(")\n");
            //System.out.println(sql.toString());
            preparedStatement = con.prepareStatement(sql.toString());
            rs = preparedStatement.executeQuery();
            List<String> daysList = new ArrayList();   
            while (rs.next()) {
                daysList.add(rs.getString(rs.findColumn("day")));
                //System.out.println(state + ", " + city);
            }
            Collections.sort(daysList, new DayComparator());
            daysList.add(0,"Select the weekday");
            String [] dayArray = new String[daysList.size()];
            dayArray = daysList.toArray(dayArray);
            dayComboBox.setModel(new javax.swing.DefaultComboBoxModel(dayArray));  
        }
    }
    private void getFromTime() throws SQLException, ClassNotFoundException {
        fromComboBox.removeAllItems();
        from = null;            
        StringBuilder sql = new StringBuilder();
        StringBuilder main_sql = getMainCategoriesSQL();
        StringBuilder sub_sql = getSubCategoriesSQL();
        StringBuilder a_sql = getAttributesSQL();
        StringBuilder l_sql = getLocationsSQL();
        StringBuilder d_sql = getDaySQL();
        StringBuilder t_sql = getToSQL();
        PreparedStatement preparedStatement;
        ResultSet rs;
        if ( !mainCategoriesSet.isEmpty()) {
            sql.append("SELECT DISTINCT o.from_time").append("\n")
                .append("FROM OpenHour o").append("\n")
                .append("WHERE o.business_id IN (").append(main_sql).append(")\n");
            if ( !subCategoriesSet.isEmpty())
                sql.append(" AND o.business_id IN (").append(sub_sql).append(")\n");
            if ( !attributesSet.isEmpty())
                sql.append(" AND o.business_id IN (").append(a_sql).append(")\n");
            if ( location != null )
                sql.append(" AND o.business_id IN (").append(l_sql).append(")\n");
            if ( day != null )
                sql.append(" AND o.business_id IN (").append(d_sql).append(")\n");
            if ( to != null )
                sql.append(" AND o.business_id IN (").append(t_sql).append(")\n");
            //System.out.println(sql.toString());
            preparedStatement = con.prepareStatement(sql.toString());
            rs = preparedStatement.executeQuery();
            List<String> fromsList = new ArrayList();   
            while (rs.next()) {
                String time = rs.getString(rs.findColumn("from_time"));
                int position = time.indexOf(" ");
                fromsList.add(time.substring(position+1,position+6));
            }
            Collections.sort(fromsList);
            fromsList.add(0,"Select the open hour");
            String [] fromArray = new String[fromsList.size()];
            fromArray = fromsList.toArray(fromArray);
            fromComboBox.setModel(new javax.swing.DefaultComboBoxModel(fromArray));  
        }
    }
    private void getToTime() throws SQLException, ClassNotFoundException {
        toComboBox.removeAllItems();
        to = null;            
        StringBuilder sql = new StringBuilder();
        StringBuilder main_sql = getMainCategoriesSQL();
        StringBuilder sub_sql = getSubCategoriesSQL();
        StringBuilder a_sql = getAttributesSQL();
        StringBuilder l_sql = getLocationsSQL();
        StringBuilder d_sql = getDaySQL();
        StringBuilder f_sql = getFromSQL();
        PreparedStatement preparedStatement;
        ResultSet rs;
        if ( !mainCategoriesSet.isEmpty()) {
            sql.append("SELECT DISTINCT o.to_time").append("\n")
                .append("FROM OpenHour o").append("\n")
                .append("WHERE o.business_id IN (").append(main_sql).append(")\n");
            if ( !subCategoriesSet.isEmpty())
                sql.append(" AND o.business_id IN (").append(sub_sql).append(")\n");
            if ( !attributesSet.isEmpty())
                sql.append(" AND o.business_id IN (").append(a_sql).append(")\n");
            if ( location != null )
                sql.append(" AND o.business_id IN (").append(l_sql).append(")\n");
            if ( day != null )
                sql.append(" AND o.business_id IN (").append(d_sql).append(")\n");
            if ( from != null )
                sql.append(" AND o.business_id IN (").append(f_sql).append(")\n");            
            //System.out.println(sql.toString());
            preparedStatement = con.prepareStatement(sql.toString());
            rs = preparedStatement.executeQuery();
            List<String> tosList = new ArrayList();   
            while (rs.next()) {
                String time = rs.getString(rs.findColumn("to_time"));
                int position = time.indexOf(" ");
                tosList.add(time.substring(position+1,position+6));
            }
            Collections.sort(tosList);
            tosList.add(0,"Select the close hour");
            String [] toArray = new String[tosList.size()];
            toArray = tosList.toArray(toArray);
            toComboBox.setModel(new javax.swing.DefaultComboBoxModel(toArray));  
        }
    }
    private void getBusinesses() throws SQLException, ClassNotFoundException {
        //try (Connection connection = populate.getConnect()) {
        /*DefaultTableModel dm = (DefaultTableModel)businessTable.getModel();
        dm.getDataVector().removeAllElements();
        dm.fireTableDataChanged();
        System.out.println("Get businesses...");*/
            
        StringBuilder sql = new StringBuilder();
        StringBuilder main_sql = getMainCategoriesSQL();
        StringBuilder sub_sql = getSubCategoriesSQL();
        StringBuilder a_sql = getAttributesSQL();
        StringBuilder l_sql = getLocationsSQL();
        StringBuilder d_sql = getDaySQL();
        StringBuilder f_sql = getFromSQL();
        StringBuilder t_sql = getToSQL();
        StringBuilder review_sql = getNumReviewSQL();
        DefaultTableModel defaultTableModel;
        String[][] data;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        if ( !mainCategoriesSet.isEmpty()) {
            sql.append("WITH business_num_review(business_id, num_review) AS (\n")
               .append(review_sql)
               .append(")\n");
            sql.append("SELECT b.business_id, b.name, b.city, b.state, b.stars, b.full_address, r.num_review, b.number_checkin").append("\n")
                .append("FROM Business b JOIN business_num_review r ON b.business_id = r.business_id").append("\n")
                .append("WHERE b.business_id IN (").append(main_sql).append(")\n");
            if ( !subCategoriesSet.isEmpty())
                sql.append(" AND b.business_id IN (").append(sub_sql).append(")\n");
            if ( !attributesSet.isEmpty())
                sql.append(" AND b.business_id IN (").append(a_sql).append(")\n");
            if ( location != null )
                sql.append(" AND b.business_id IN (").append(l_sql).append(")\n");
            if ( day != null )
                sql.append(" AND b.business_id IN (").append(d_sql).append(")\n");
            if ( from != null )
                sql.append(" AND b.business_id IN (").append(f_sql).append(")\n");
            if ( to != null )
                sql.append(" AND b.business_id IN (").append(t_sql).append(")\n");
            //sql.append("GROUP BY b.business_id, b.name, b.city, b.state, b.stars, b.full_address, b.number_checkin");
            //System.out.println("---------------------Search query------------------------");
            //System.out.println(sql.toString());
            preparedStatement = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = preparedStatement.executeQuery();
        }
        if (sql.length() > 0) {
            rs.last();
            ResultSetMetaData rsmd = rs.getMetaData();
            int rowCount = rs.getRow();
            int columnCount = rsmd.getColumnCount();
            data = new String[rowCount][columnCount];
            String[] columnNames = new String[columnCount];
            
            // get column names
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = rsmd.getColumnName(i);
            }
            
            rs.beforeFirst();
            for (int i = 0; i < rowCount; i++) {
                if (rs.next()) {
                    for (int j = 1; j <= columnCount; j++) {
                        data[i][j - 1] = rs.getString(j);
                    }
                }
            }
            rs.close();
            preparedStatement.close();
            defaultTableModel = new DefaultTableModel(data, columnNames);
            businessTable.setModel(defaultTableModel);
            businessTable.getColumnModel().getColumn(0).setWidth(0);
            businessTable.getColumnModel().getColumn(0).setMinWidth(0);
            businessTable.getColumnModel().getColumn(0).setMaxWidth(0); 
        }
        businessTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        JTable target = (JTable)e.getSource();
                        int row = target.getSelectedRow();
                        String new_id = businessTable.getModel().getValueAt(row, 0).toString();
                        //System.out.println(id);
                        try {
                            if ( new_id != id ) {
                                id = new_id;
                                showReview(id);
                            } 
                        } catch (SQLException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
        });

            //queryTextArea.append(query.toString());
    }
    private void showReview(String id) throws SQLException, ClassNotFoundException {
        System.out.println("Get review information...");
        JFrame reviewFrame = new JFrame("Review");
        reviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        reviewFrame.setSize(500, 600);
        reviewFrame.setLayout(new FlowLayout());
        reviewFrame.setVisible(true);
        TableModel dataModel = new DefaultTableModel();
        JTable reviewTable = new JTable(dataModel);
        JScrollPane scrollpane = new JScrollPane(reviewTable);
        JPanel panel = new JPanel();

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);        
        JButton closeButtonReview = new javax.swing.JButton();
        closeButtonReview.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        closeButtonReview.setText("Close");

        panel.setLayout( new FlowLayout() );
        panel.add(closeButtonReview);
        DefaultTableModel defaultTableModel;
        String[][] data;
        //try (Connection connection = populate.getConnect();) {
            StringBuilder query = new StringBuilder();
            OraclePreparedStatement preparedStatement;
            ResultSet rs;
            query.append("WITH b_review (user_id, review_date, stars, review_text, votes) AS ( \n")
                 .append("SELECT r.user_id, r.review_date, r.stars, r.review_text, r.votes\n")
                 .append("FROM REVIEW r\n")
                 .append("WHERE r.business_id = '" + id + "'\n")
                 .append(")\n")
                 .append("SELECT br.review_date, br.stars, br.review_text, y.name, br.votes\n")
                 .append("FROM b_review br, YelpUser y\n")
                 .append("WHERE br.user_id = y.user_id ");
            
            System.out.println("DEBUG================= review query: \n" + query.toString());
            preparedStatement = (OraclePreparedStatement) con.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = preparedStatement.executeQuery();

            rs.last();
            ResultSetMetaData rsmd = rs.getMetaData();
            int rowCount = rs.getRow();
            int columnCount = rsmd.getColumnCount();
            data = new String[rowCount][columnCount];
            String[] columnNames = new String[columnCount];

            // get column names
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = rsmd.getColumnName(i);
            }

            rs.beforeFirst();
            for (int i = 0; i < rowCount; i++) {
                if (rs.next()) {
                    for (int j = 1; j <= columnCount; j++) {
                        data[i][j - 1] = rs.getString(j);
                    }
                }
            }
            rs.close();
            preparedStatement.close();
            defaultTableModel = new DefaultTableModel(data, columnNames);
            reviewTable.setModel(defaultTableModel);
            reviewTable.setAutoCreateRowSorter(true);
        

            reviewFrame.add(scrollpane);
            reviewFrame.add(panel);

        closeButtonReview.addActionListener(e -> reviewFrame.dispose());

    }
    private StringBuilder getMainCategoriesSQL() {
        StringBuilder main_sql = new StringBuilder();
        Iterator<String> mc = mainCategoriesSet.iterator();
            main_sql.setLength(0);
            main_sql.append("SELECT DISTINCT mc.business_id").append("\n")
                    .append("FROM MainCategory mc").append("\n")
                    .append("WHERE ");
                    
            while (mc.hasNext()) {

                    
                main_sql.append("mc.mainCategory = \'").append(mc.next()).append("\'");
                if ( mc.hasNext() )
                    if ( mainCategoryComboBox.getSelectedIndex() == 0 )
                        main_sql.append(" OR \n");
                    else if ( mainCategoryComboBox.getSelectedIndex() == 1)
                        main_sql.append(" AND \n");
            }
        return main_sql;
    }
    private StringBuilder getSubCategoriesSQL() {
        StringBuilder sub_sql = new StringBuilder();
        Iterator<String> sc = subCategoriesSet.iterator();
        sub_sql.setLength(0);
        sub_sql.append("SELECT DISTINCT sc.business_id").append("\n")
               .append("FROM SubCategory sc").append("\n")
               .append("WHERE "); 
        while (sc.hasNext()) {
            String s = sc.next();
            int position;
            if ( s.contains("'")) {
                position = s.indexOf("'");
                s = s.substring(0,position) + "'"+ s.substring(position);
                //System.out.println(s);
            }
            sub_sql.append("sc.subCategory = \'").append(s).append("\'");
            if ( sc.hasNext() )
                if ( subCategoryComboBox.getSelectedIndex() == 0 )
                    sub_sql.append(" OR \n");
                else if ( subCategoryComboBox.getSelectedIndex() == 1)
                    sub_sql.append(" AND \n");
        }
        return sub_sql;
    }
    private StringBuilder getAttributesSQL() {
        StringBuilder a_sql = new StringBuilder();
        Iterator<String> a = attributesSet.iterator();
        a_sql.setLength(0);
        a_sql.append("SELECT DISTINCT a.business_id").append("\n")
             .append("FROM Attribute a").append("\n")
             .append("WHERE ");
                    
        while (a.hasNext()) {
            a_sql.append("a.attribute = \'").append(a.next()).append("\'");
            if ( a.hasNext() )
                if ( attributeComboBox.getSelectedIndex() == 0 )
                    a_sql.append(" OR \n");
                else if ( attributeComboBox.getSelectedIndex() == 1)
                    a_sql.append(" AND \n");
        }
        return a_sql;
    }
    private StringBuilder getLocationsSQL() {
        StringBuilder l_sql = new StringBuilder();
        l_sql.setLength(0);
        l_sql.append("SELECT DISTINCT b.business_id").append("\n")
             .append("FROM Business b").append("\n");
        if ( location != null ) {
            l_sql.append("WHERE ");
            int position = location.indexOf(",");
            l_sql.append("b.state = \'").append(location.substring(0,position)).append("\' AND ");
            l_sql.append("b.city = \'").append(location.substring(position+2)).append("\'");   
        }  
        //System.out.println(l_sql);
        return l_sql;
    }
    private StringBuilder getDaySQL() {
        StringBuilder d_sql = new StringBuilder();
        d_sql.setLength(0);
        d_sql.append("SELECT DISTINCT o.business_id").append("\n")
             .append("FROM OpenHour o").append("\n");
        if ( day != null ) {
            d_sql.append("WHERE ");
            d_sql.append("o.day = \'").append(day).append("\'");
        }  
        //System.out.println(d_sql);
        return d_sql;
    }
    private StringBuilder getFromSQL() {
        StringBuilder f_sql = new StringBuilder();
        f_sql.setLength(0);
        f_sql.append("SELECT DISTINCT o.business_id").append("\n")
             .append("FROM OpenHour o").append("\n");
        if ( from != null ) {
            f_sql.append("WHERE ");
            f_sql.append("o.from_time < \'").append(from).append("\'");
            f_sql.append(" OR o.from_time = \'").append(from).append("\'");
        }  
        //System.out.println(f_sql);
        return f_sql;
    }
    private StringBuilder getToSQL() {
        StringBuilder t_sql = new StringBuilder();
        t_sql.setLength(0);
        t_sql.append("SELECT DISTINCT o.business_id").append("\n")
             .append("FROM OpenHour o").append("\n");
        if ( to != null ) {
            t_sql.append("WHERE ");
            t_sql.append("o.to_time > \'").append(to).append("\'");
            t_sql.append(" OR o.to_time = \'").append(to).append("\'");
        }  
        //System.out.println(t_sql);
        return t_sql;
    }
    private StringBuilder getNumReviewSQL() {
        //System.out.println("-----------------------------");
        StringBuilder review_sql = new StringBuilder();
        review_sql.setLength(0);
        review_sql.append("SELECT r.business_id, count(*) AS num_review").append("\n")
                  .append("FROM REVIEW r").append("\n")
                  .append("GROUP BY r.business_id").append("\n");
        //System.out.println(review_sql);
        return review_sql;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        categoryPanel = new javax.swing.JPanel();
        mainCategoryPanel = new javax.swing.JPanel();
        mainCategoryScrollPane = new javax.swing.JScrollPane();
        mainCategoryListPanel = new javax.swing.JPanel();
        mainCategoryLabel = new javax.swing.JLabel();
        mainCategoryComboBox = new javax.swing.JComboBox<>();
        subCategoryPanel = new javax.swing.JPanel();
        subCategoryLabel = new javax.swing.JLabel();
        subCategoryScrollPane = new javax.swing.JScrollPane();
        subCategoryListPanel = new javax.swing.JPanel();
        subCategoryComboBox = new javax.swing.JComboBox<>();
        attributePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        attributeScrollPane = new javax.swing.JScrollPane();
        attributeListPanel = new javax.swing.JPanel();
        attributeComboBox = new javax.swing.JComboBox<>();
        locationComboBox = new javax.swing.JComboBox<>();
        dayComboBox = new javax.swing.JComboBox<>();
        fromComboBox = new javax.swing.JComboBox<>();
        toComboBox = new javax.swing.JComboBox<>();
        searchButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        businessScrollPane = new javax.swing.JScrollPane();
        businessTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        mainPanel.setPreferredSize(new java.awt.Dimension(1200, 800));

        mainCategoryListPanel.setLayout(new java.awt.GridLayout(0, 1));
        mainCategoryScrollPane.setViewportView(mainCategoryListPanel);

        mainCategoryLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        mainCategoryLabel.setText("Main Category");

        mainCategoryComboBox.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        mainCategoryComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OR", "AND" }));
        mainCategoryComboBox.setToolTipText("");
        mainCategoryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainCategoryComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainCategoryPanelLayout = new javax.swing.GroupLayout(mainCategoryPanel);
        mainCategoryPanel.setLayout(mainCategoryPanelLayout);
        mainCategoryPanelLayout.setHorizontalGroup(
            mainCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCategoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainCategoryPanelLayout.createSequentialGroup()
                        .addComponent(mainCategoryScrollPane)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainCategoryPanelLayout.createSequentialGroup()
                        .addGap(0, 28, Short.MAX_VALUE)
                        .addGroup(mainCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainCategoryPanelLayout.createSequentialGroup()
                                .addComponent(mainCategoryLabel)
                                .addGap(32, 32, 32))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainCategoryPanelLayout.createSequentialGroup()
                                .addComponent(mainCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(60, 60, 60))))))
        );
        mainCategoryPanelLayout.setVerticalGroup(
            mainCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCategoryPanelLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(mainCategoryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainCategoryScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(mainCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        subCategoryLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        subCategoryLabel.setText("Sub Category");

        subCategoryListPanel.setLayout(new java.awt.GridLayout(0, 1));
        subCategoryScrollPane.setViewportView(subCategoryListPanel);

        subCategoryComboBox.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        subCategoryComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OR", "AND" }));
        subCategoryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subCategoryComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout subCategoryPanelLayout = new javax.swing.GroupLayout(subCategoryPanel);
        subCategoryPanel.setLayout(subCategoryPanelLayout);
        subCategoryPanelLayout.setHorizontalGroup(
            subCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subCategoryPanelLayout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addGroup(subCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, subCategoryPanelLayout.createSequentialGroup()
                        .addComponent(subCategoryLabel)
                        .addGap(47, 47, 47))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, subCategoryPanelLayout.createSequentialGroup()
                        .addComponent(subCategoryScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, subCategoryPanelLayout.createSequentialGroup()
                        .addComponent(subCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(74, 74, 74))))
        );
        subCategoryPanelLayout.setVerticalGroup(
            subCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subCategoryPanelLayout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(subCategoryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subCategoryScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(subCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
        );

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Attribute");

        attributeListPanel.setLayout(new java.awt.GridLayout(0, 1));
        attributeScrollPane.setViewportView(attributeListPanel);

        attributeComboBox.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        attributeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OR", "AND" }));
        attributeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout attributePanelLayout = new javax.swing.GroupLayout(attributePanel);
        attributePanel.setLayout(attributePanelLayout);
        attributePanelLayout.setHorizontalGroup(
            attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributePanelLayout.createSequentialGroup()
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(jLabel1))
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(attributeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(attributeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        attributePanelLayout.setVerticalGroup(
            attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributePanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(attributeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(attributeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        locationComboBox.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        locationComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select the location" }));
        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        dayComboBox.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        dayComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select the weekday" }));
        dayComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dayComboBoxActionPerformed(evt);
            }
        });

        fromComboBox.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        fromComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select the open hour" }));
        fromComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromComboBoxActionPerformed(evt);
            }
        });

        toComboBox.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        toComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select the close hour" }));
        toComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toComboBoxActionPerformed(evt);
            }
        });

        searchButton.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        searchButton.setText("Search");
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchButtonMouseClicked(evt);
            }
        });
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        closeButton.setFont(new java.awt.Font("Arial", 1, 18));
        closeButton.setText("Close");
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeButtonMouseClicked(evt);
            }
        });
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        businessTable.setAutoCreateRowSorter(true);
        businessTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                //{null, null, null, null},
                //{null, null, null, null},
                //{null, null, null, null},
                //{null, null, null, null}
            },
            new String [] {
                //"Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        businessScrollPane.setViewportView(businessTable);

        javax.swing.GroupLayout categoryPanelLayout = new javax.swing.GroupLayout(categoryPanel);
        categoryPanel.setLayout(categoryPanelLayout);
        categoryPanelLayout.setHorizontalGroup(
            categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(categoryPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(categoryPanelLayout.createSequentialGroup()
                        .addComponent(mainCategoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(subCategoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(categoryPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(attributePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(69, 69, 69)
                .addComponent(businessScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(110, Short.MAX_VALUE))
            .addGroup(categoryPanelLayout.createSequentialGroup()
                .addGap(494, 494, 494)
                .addGroup(categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(toComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(categoryPanelLayout.createSequentialGroup()
                        .addComponent(fromComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(263, 263, 263)
                        .addComponent(searchButton)
                        .addGap(80, 80, 80)
                        .addComponent(closeButton)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        categoryPanelLayout.setVerticalGroup(
            categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(categoryPanelLayout.createSequentialGroup()
                .addGroup(categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(subCategoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attributePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(categoryPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mainCategoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(businessScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(32, 32, 32)
                .addGroup(categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fromComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton)
                    .addComponent(closeButton))
                .addGroup(categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(categoryPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addGroup(categoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(32, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, categoryPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(toComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(categoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(categoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(153, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(89, 89, 89)
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 1457, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(1404, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(54, Short.MAX_VALUE)
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mainCategoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainCategoryComboBoxActionPerformed
        try {
            // TODO add your handling code here:
                getLocations();
                getDays();
                getFromTime();
                getToTime();
                getSubCategories();  

                              
            //}
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_mainCategoryComboBoxActionPerformed

    private void subCategoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subCategoryComboBoxActionPerformed
        try {
            getLocations();
            getDays();
            getFromTime();
            getToTime();
            getAttributes();

        } catch (SQLException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_subCategoryComboBoxActionPerformed

    private void attributeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeComboBoxActionPerformed
        try {
            // TODO add your handling code here:
            //if ( attributesSet.size() > 1) {
                getLocations();
                getDays();
                getFromTime();
                getToTime();
            //}
        } catch (SQLException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_attributeComboBoxActionPerformed

    private void dayComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dayComboBoxActionPerformed
        // TODO add your handling code here:
        if ( dayComboBox.getSelectedItem() != null )
            if ( dayComboBox.getSelectedItem().toString() == "Select the weekday" )
                day = null;
            else
                day = dayComboBox.getSelectedItem().toString();
        else
            day = null;
        //getLocations();
        //getFromTime();
    }//GEN-LAST:event_dayComboBoxActionPerformed

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed

        if ( locationComboBox.getSelectedItem() != null )
            if ( locationComboBox.getSelectedItem().toString() == "Select the location")
                location = null;
            else
                location = locationComboBox.getSelectedItem().toString();
        else
            location = null;
        //getDays();
        //getFromTime();
    }//GEN-LAST:event_locationComboBoxActionPerformed

    private void fromComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromComboBoxActionPerformed
        // TODO add your handling code here:
        if (fromComboBox.getSelectedItem() != null) {
            if ( fromComboBox.getSelectedItem().toString() != "Select the open hour")
                from = fromComboBox.getSelectedItem().toString();
            else
                from = null;
            //fromComboBox.setSelectedItem();
        } else {
            from = null;
            //getDays();
            //getLocations();
        }
        //System.out.println(from);
    }//GEN-LAST:event_fromComboBoxActionPerformed

    private void toComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toComboBoxActionPerformed
        // TODO add your handling code here:
        if (toComboBox.getSelectedItem() != null) {
            if ( toComboBox.getSelectedItem().toString() != "Select the close hour")
                to = toComboBox.getSelectedItem().toString();
            else
                to = null;
            //fromComboBox.setSelectedItem();
        } else {
            to = null;
            //getDays();
            //getLocations();
        }
    }//GEN-LAST:event_toComboBoxActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
        //System.exit(0);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:
        //getBusiness();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void searchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseClicked
        try {
            // TODO add your handling code here:
            getBusinesses();
        } catch (SQLException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_searchButtonMouseClicked

    private void closeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeButtonMouseClicked
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_closeButtonMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new hw3().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> attributeComboBox;
    private javax.swing.JPanel attributeListPanel;
    private javax.swing.JPanel attributePanel;
    private javax.swing.JScrollPane attributeScrollPane;
    private javax.swing.JScrollPane businessScrollPane;
    private javax.swing.JTable businessTable;
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox<String> dayComboBox;
    private javax.swing.JComboBox<String> fromComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox<String> locationComboBox;
    private javax.swing.JComboBox<String> mainCategoryComboBox;
    private javax.swing.JLabel mainCategoryLabel;
    private javax.swing.JPanel mainCategoryListPanel;
    private javax.swing.JPanel mainCategoryPanel;
    private javax.swing.JScrollPane mainCategoryScrollPane;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton searchButton;
    private javax.swing.JComboBox<String> subCategoryComboBox;
    private javax.swing.JLabel subCategoryLabel;
    private javax.swing.JPanel subCategoryListPanel;
    private javax.swing.JPanel subCategoryPanel;
    private javax.swing.JScrollPane subCategoryScrollPane;
    private javax.swing.JComboBox<String> toComboBox;
    // End of variables declaration//GEN-END:variables
}
