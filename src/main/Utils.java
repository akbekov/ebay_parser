/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author takbekov
 */
public class Utils {

    public void toExcel(JTable table, String file, int[] array) {
        try {
            TableModel model = table.getModel();
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
            File newFile = new File(file + "_" + sdf.format(new Date()) + ".xls");
            FileWriter excel = new FileWriter(newFile);
            for (int i = 0; i < model.getColumnCount(); i++) {
                excel.write(model.getColumnName(i) + "\t");
            }
            excel.write("\n");
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    if (contains(j, array)) {
                        excel.write(model.getValueAt(i, j).toString().replaceAll("\\.", ",") + "\t");
                    } else {
                        excel.write(model.getValueAt(i, j).toString() + "\t");
                    }
                }
                excel.write("\n");
            }
            excel.close();
            JOptionPane.showMessageDialog(null, "Сохранено в файл: " + newFile.getName(), "Экспорт в Excel", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Возникла ошибка при экспорте в Excel!\nКод ошибки: " + e.getLocalizedMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean contains(int key, int[] array) {
        return Arrays.stream(array).anyMatch(i -> i == key);
    }

    public String getBrand(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Element brand = doc.select("h2[itemprop=brand] span[itemprop=name]").first();
        return brand.text();
    }

}
