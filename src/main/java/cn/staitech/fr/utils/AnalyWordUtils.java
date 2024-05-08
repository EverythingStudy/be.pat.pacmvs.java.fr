package cn.staitech.fr.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/3/28 17:06
 * @desc
 */
public class AnalyWordUtils {


    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream(new File("D:\\文档\\2.0机构版\\测试文件/大鼠致癌试验蜡块编号表.docx"));
        XWPFDocument document = new XWPFDocument(fis);
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        String text = paragraphs.get(1).getText().trim();
        System.out.println("--"+text);

        // 读取每个表格
        List<XWPFTable> tables = document.getTables();
        XWPFTable xwpfTable = tables.get(0);
        List<XWPFTableRow> rows = xwpfTable.getRows();
        for (int i=1;i<rows.size();i++){
            System.out.println(rows.get(i).getTableCells().stream()
                    .map(cell -> cell.getText().trim())
                    .reduce((cell1, cell2) -> cell1 + " |-| " + cell2)
                    .orElse(""));
        }
        /*for (XWPFTableRow row : xwpfTable.getRows()) {
            System.out.println(row.getTableCells().stream()
                    .map(cell -> cell.getText().trim())
                    .reduce((cell1, cell2) -> cell1 + " | " + cell2)
                    .orElse(""));
        }*/
        /*for (XWPFTable table : tables) {
            // 读取每一行
            for (XWPFTableRow row : table.getRows()) {
                System.out.println(row.getTableCells().stream()
                        .map(cell -> cell.getText().trim())
                        .reduce((cell1, cell2) -> cell1 + " | " + cell2)
                        .orElse(""));
            }
        }*/

        fis.close();


    }

}
