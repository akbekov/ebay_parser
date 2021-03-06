/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import database.Database;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import main.Item;
import main.Utils;
import net.proteanit.sql.DbUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author takbekov
 */
public class HistoryFrame extends javax.swing.JFrame {

    private final Database database = new Database();
    private final WaitFrame waitFrame = new WaitFrame();
    private final Utils utils = new Utils();
    private Connection connection;

    public HistoryFrame() {
        initComponents();
        this.setLocationRelativeTo(null);
        connection = database.getConnection();
        populateHistory("");
    }

    public HistoryFrame(Connection connection) {
        initComponents();
        this.setLocationRelativeTo(null);
        this.connection = connection;
        populateHistory("");
    }

    public HistoryFrame(Connection connection, String id) {
        initComponents();
        this.setLocationRelativeTo(null);
        this.connection = connection;
        populateItemHistory(id);
    }

    private void populateHistory(String type) {
        try {
            Statement state = connection.createStatement();
            ResultSet rs;
            if (type.equals("all")) {
                rs = database.select("history h, item i",
                        "i.asin, i.url, strftime('%d-%m-%Y %H:%M:%S', h.date_check) date_check, h.price, h.prev_price, h.amount, h.prev_amount, h.descr",
                        "h.id_item = i.id", "h.date_check desc", state);

            } else if (type.equals("last")) {
                rs = database.select("history h, item i",
                        "i.asin, i.url, strftime('%d-%m-%Y %H:%M:%S', h.date_check) date_check, h.price, h.prev_price, h.amount, h.prev_amount, h.descr",
                        "h.id_item = i.id and h.id = (select max(id) from history where id_item = i.id)", "h.date_check desc", state);
            } else {
                rs = database.select("history h, item i",
                        "i.asin, i.url, strftime('%d-%m-%Y %H:%M:%S', h.date_check) date_check, h.price, h.prev_price, h.amount, h.prev_amount, h.descr",
                        "h.id_item = i.id", "h.date_check desc limit 150", state);
            }
            table.setModel(DbUtils.resultSetToTableModel(rs));
            state.close();
            table.getColumnModel().getColumn(0).setMinWidth(35);
            table.getColumnModel().getColumn(1).setMinWidth(35);
            table.getColumnModel().getColumn(2).setMinWidth(130);
            table.getColumnModel().getColumn(3).setMinWidth(40);
            table.getColumnModel().getColumn(4).setMinWidth(40);
            table.getColumnModel().getColumn(5).setMinWidth(40);
            table.getColumnModel().getColumn(6).setMinWidth(40);
            table.getColumnModel().getColumn(7).setMinWidth(130);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateItemHistory(String id) {
        try {
            Statement state = connection.createStatement();
            ResultSet rs = database.select("history h, item i",
                    "i.asin, i.url,strftime('%d-%m-%Y %H:%M:%S', h.date_check) date_check, h.price, h.prev_price, h.amount, h.prev_amount, h.descr",
                    "h.id_item = i.id and i.id = " + id, "h.date_check desc limit 150", state);
            table.setModel(DbUtils.resultSetToTableModel(rs));
            state.close();
            table.getColumnModel().getColumn(0).setMinWidth(35);
            table.getColumnModel().getColumn(1).setMinWidth(35);
            table.getColumnModel().getColumn(2).setMinWidth(110);
            table.getColumnModel().getColumn(3).setMinWidth(40);
            table.getColumnModel().getColumn(4).setMinWidth(40);
            table.getColumnModel().getColumn(5).setMinWidth(40);
            table.getColumnModel().getColumn(6).setMinWidth(40);
            table.getColumnModel().getColumn(7).setMinWidth(150);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkItem(Statement state, Item item) {
        try {
            Document doc = Jsoup.connect(item.getUrl()).get();
            Elements span = doc.select("#qtySubTxt");
            String descr;
            if (!(span.text().length() > 1)) {
                descr = "Not found";
            } else {
                descr = span.text();
            }
            System.out.println("id: " + item.getId());
            System.out.println("url: " + item.getUrl());
            System.out.println("descr: " + descr + "\ncount: " + parseStock(descr.toUpperCase()));
            Element price = doc.select("#prcIsum").first();
            System.out.println("price: " + price.attr("content"));
            Element brand = doc.select("h2[itemprop=brand] span[itemprop=name]").first();
            System.out.println("brand: " + brand.text() + "\n");
            database.insert("history", "id_item,price,prev_price,descr,amount,prev_amount,date_check",
                    item.getId() + "," + price.attr("content") + "," + priceDiff(Double.parseDouble(price.attr("content")), item, state) + ","
                    + "'" + descr + "'," + parseStock(descr.toUpperCase()) + "," + amountDiff(parseStock(descr.toUpperCase()), item, state) + ",DATETIME(current_timestamp, 'localtime')",
                    state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double priceDiff(double price, Item item, Statement state) {
        try {
            ResultSet rs = database.select("history", "price", "id_item = " + item.getId(), "id desc limit 1", state);
            double prevPrice = 0;
            if (rs.next()) {
                prevPrice = rs.getDouble("price");
            }
            rs.close();
            if (prevPrice == 0) {
                return 0;
            }
            return price - prevPrice;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int amountDiff(int amount, Item item, Statement state) {
        try {
            ResultSet rs = database.select("history", "amount", "id_item = " + item.getId(), "id desc limit 1", state);
            int prevAmount = 0;
            if (rs.next()) {
                prevAmount = rs.getInt("amount");
            }
            rs.close();
            if (prevAmount == 0) {
                return 0;
            }
            return amount - prevAmount;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int parseStock(String text) {
        text = text.replaceAll("[^\\d.]", "");
        if (text.length() == 0) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Проверки количества и цен");
        setResizable(false);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(table);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 945, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton1.setText("Новая проверка");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        txtSearch.setToolTipText("Искомый текст");
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        jButton2.setText("Показать все");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Последняя проверка");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Экспорт в Excel");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))
                    .addComponent(txtSearch))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        String text = txtSearch.getText();
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(table.getModel());
        if (text.length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter(text));
        }
        table.setRowSorter(rowSorter);
    }//GEN-LAST:event_txtSearchKeyReleased

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        waitFrame.setVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Statement state = connection.createStatement();
                    List<Item> itemList = new ArrayList<>();
                    ResultSet rs = database.select("item", "*", "status = 1", "id desc", state);
                    while (rs.next()) {
                        Item item = new Item(rs.getInt("id"), rs.getString("url"), rs.getString("asin"), rs.getString("title"), 1, rs.getString("brand"));
                        System.out.println("item: " + item.getId());
                        itemList.add(item);
                    }
                    rs.close();
                    for (Item item : itemList) {
                        checkItem(state, item);
                    }
                    state.close();
                    populateHistory("last");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                waitFrame.dispose();
                new ResultFrame(connection).setVisible(true);
            }
        }).start();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        populateHistory("all");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        populateHistory("last");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        utils.toExcel(table, "History", new int[]{3, 4, 5, 6});
    }//GEN-LAST:event_jButton4ActionPerformed

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
            java.util.logging.Logger.getLogger(HistoryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HistoryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HistoryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HistoryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HistoryFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
