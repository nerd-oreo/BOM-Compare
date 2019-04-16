package bomcompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class BOMComparison {
    private BOM sanminaBOM = new BOM();
    private BOM customerBOM;
    
    
    public BOMComparison(String template) {
    try {
            FileInputStream fs = new FileInputStream(new File(template));
            Workbook wb = new XSSFWorkbook(fs);
            Sheet sheet = wb.getSheet("SanminaBOM");
            Iterator<Row> iterator = sheet.iterator();
            
            while(iterator.hasNext()) {
                Row currentRow = iterator.next();
                int rowNum = currentRow.getRowNum();
                if(currentRow.getRowNum() > 0) {    // skip header row
                    Cell lvlCell = currentRow.getCell(Header.LEVEL);
                    Cell numCell = currentRow.getCell(Header.NUMBER);
                    Cell desCell = currentRow.getCell(Header.DESCRIPTION);
                    Cell qtyCell = currentRow.getCell(Header.QUANTITY);
                    Cell refCell = currentRow.getCell(Header.REF_DES);
                    
                    int level = (int) lvlCell.getNumericCellValue();
                    String number = numCell.getStringCellValue();
                    String description = desCell.getStringCellValue();
                    int quantity = (qtyCell != null) ? (int) qtyCell.getNumericCellValue() : 0;
                    String ref_des = (refCell != null) ? refCell.getStringCellValue() : "";
                    
                    // Create a new component
                    Component component = new Component(level, number, description, quantity, ref_des);
                    sanminaBOM.addComponent(component);
                    System.out.println(component.toString());
                }
            } 
            
            sanminaBOM.printBOM();
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BOMComparisonTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BOMComparisonTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
