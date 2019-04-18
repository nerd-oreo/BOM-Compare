package bomcompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class BOMComparison {
    private BOM sanminaBOM, customerBOM;
    private ArrayList<String> sanminaUniqueIdList, customerUniqueIdList;
    
    public BOMComparison(String template) {
        System.out.println("+++ SANMINA BOM +++");
        sanminaBOM = transferComponentFromTemplateToBOM(template, "SanminaBOM");
        sanminaUniqueIdList = sanminaBOM.getUniqueIdList();
        
        System.out.println("+++ CUSTOMER BOM +++");
        customerBOM = transferComponentFromTemplateToBOM(template, "CustomerBOM");
        customerUniqueIdList = customerBOM.getUniqueIdList();
    }
    
    
    private BOM transferComponentFromTemplateToBOM(String template, String sheetName) {
        BOM bom = new BOM();
        try {
                FileInputStream fs = new FileInputStream(new File(template));
                Workbook wb = new XSSFWorkbook(fs);
                Sheet sheet = wb.getSheet(sheetName);
                Iterator<Row> iterator = sheet.iterator();

                while(iterator.hasNext()) {
                    Row currentRow = iterator.next();

                    if(currentRow.getRowNum() > 0) {    // skip header row
                        Cell lvlCell = currentRow.getCell(Header.LEVEL);
                        Cell numCell = currentRow.getCell(Header.NUMBER);
                        Cell desCell = currentRow.getCell(Header.DESCRIPTION);
                        Cell revCell = currentRow.getCell(Header.REV);
                        Cell qtyCell = currentRow.getCell(Header.QUANTITY);
                        Cell refCell = currentRow.getCell(Header.REF_DES);

                        int level = (int) lvlCell.getNumericCellValue();
                        String number = numCell.getStringCellValue();
                        String description = desCell.getStringCellValue();
                        String rev = revCell.getStringCellValue();
                        int quantity = (qtyCell != null) ? (int) qtyCell.getNumericCellValue() : 0;
                        String ref_des = (refCell != null) ? refCell.getStringCellValue() : "";

                        // Create a new component
                        Component component = new Component(level, number, description, rev, quantity, ref_des);
                        bom.addComponent(component);
                        //System.out.println(component.toString());
                    }
                } 

                bom.printBOM();


        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bom;
    }
    
    private void compare() {
    
    }
    
}
