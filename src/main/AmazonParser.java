/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import database.Database;
import frames.MainFrame;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author takbekov
 */
public class AmazonParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
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
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
//                new AmazonParser().run();
            }
        });
    }

    private void run() {
        try {
            Utils utils = new Utils();
            JFileChooser fileOpen = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Document", "xlsx");
            fileOpen.setFileFilter(filter);
            File file = null;
            int ret = fileOpen.showDialog(null, "Открыть файл");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = fileOpen.getSelectedFile();
            }
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            List<Item> itemList = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(0);
            int first = 1;
            int last = sheet.getLastRowNum();
            while (first <= last) {
                Item item = new Item();
                item.setAsin(sheet.getRow(first).getCell(1).getStringCellValue());
                item.setUrl(sheet.getRow(first).getCell(2).getStringCellValue());
                item.setBrand(utils.getBrand(item.getUrl()));
                itemList.add(item);
                System.out.println(item.toString());
                first = first + 1;
            }
            Database database = new Database();
            Statement state = database.getConnection().createStatement();
            for (Item item : itemList) {
                database.insert("item", "asin, url, brand", "'" + item.getAsin() + "','" + item.getUrl() + "','" + item.getBrand() + "'", state);
            }
            workbook.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
