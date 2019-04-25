package bomcompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.concurrent.Task;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class BOMComparison extends Task<String> {

    private BOM sanminaBOM = null;
    private BOM customerBOM = null;
    private ArrayList<String> sanminaUniqueIdList = null;
    private ArrayList<String> customerUniqueIdList = null;
    private final ArrayList<PairComparison> pairComparisonList;

    private String templateURL;
    private String reportURL;

    private File template, report;

    private int progressCount;

    public BOMComparison(String templateURL, String reportURL) {
        this.templateURL = templateURL;
        this.reportURL = reportURL;

        template = new File(templateURL);
        report = new File(reportURL + "/BomCompareReport.xlsx");
        pairComparisonList = new ArrayList<>();
    }

    @Override
    protected String call() throws Exception {
        this.progressCount = 0;
        copy();

        transfer();

        compare();

        return this.report.getAbsolutePath().toString();
    }

    private void copy() {
        try {
            Files.copy(template.toPath(), report.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void transfer() {
        try {
            sanminaBOM = transferComponentFromTemplateToBOM("SanminaBOM");
            sanminaUniqueIdList = sanminaBOM.getUniqueIdList();

            customerBOM = transferComponentFromTemplateToBOM("CustomerBOM");
            customerUniqueIdList = customerBOM.getUniqueIdList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compare() {
        // compare sanmina uniId to customer uniId
        compareSanminaToCustomer();

        // compare customer uniId to sanmina uniId
        compareCustomerToSanmina();
        int rowNum = 2;

        for (int i = 0; i < pairComparisonList.size(); i++) {
            PairComparison pc = pairComparisonList.get(i);

            //writeToBomReport("ADDED", pc, rowNum);
            if (pc.getStatus() == ChangeStatus.ADDED) {
                writeToBomReport(pc, rowNum);
            } else if (pc.getStatus() == ChangeStatus.REMOVED) {
                writeToBomReport(pc, rowNum);
            } else if (pc.getStatus() == ChangeStatus.NONE) {
                writeToBomReport(pc, rowNum);
            }
            rowNum++;

            double percent = ((double) i / (double) (pairComparisonList.size() - 1)) * 100.00;
            this.updateMessage("Comparing: " + String.format("%.2f", percent) + "%");
            this.updateProgress(percent, 100);
        }
    }

    private BOM transferComponentFromTemplateToBOM(String sheetName) throws Exception {
        BOM bom = new BOM();
        try {
            FileInputStream fs = new FileInputStream(template);
            Workbook wb = new XSSFWorkbook(fs);
            Sheet sheet = wb.getSheet(sheetName);
            Iterator<Row> iterator = sheet.iterator();

            while (iterator.hasNext()) {
                Row currentRow = iterator.next();

                if (currentRow.getRowNum() > 0) {    // skip header row
                    Cell lvlCell = currentRow.getCell(Header.LEVEL);
                    Cell numCell = currentRow.getCell(Header.NUMBER);
                    Cell desCell = currentRow.getCell(Header.DESCRIPTION);
                    Cell revCell = currentRow.getCell(Header.REV);
                    Cell qtyCell = currentRow.getCell(Header.QUANTITY);
                    Cell refCell = currentRow.getCell(Header.REF_DES);

                    int level = 0;
                    if (lvlCell.getCellType() == CellType.STRING) {
                        level = Integer.parseInt(lvlCell.getStringCellValue().replaceAll("\\h+", ""));
                    } else {
                        level = (int) lvlCell.getNumericCellValue();
                    }

                    String number = (numCell.getStringCellValue()).replaceAll("\\h+", "");
                    String description = (desCell.getStringCellValue()).replaceAll("\\h+", "");
                    String rev = "";

                    if (revCell != null) {
                        if (revCell.getCellType() == CellType.NUMERIC) {
                            rev = Integer.toString((int) (revCell.getNumericCellValue())).replaceAll("\\h+", "");
                        } else if (revCell.getCellType() == CellType.STRING) {
                            rev = (revCell.getStringCellValue()).replaceAll("\\h+", "");
                        }
                    }

                    int quantity = 0;
                    if (qtyCell != null) {
                        if (qtyCell.getCellType() == CellType.STRING) {
                            quantity = Integer.parseInt(qtyCell.getStringCellValue());
                        } else {
                            quantity = (int) qtyCell.getNumericCellValue();
                        }
                    }

                    String ref_des = (refCell != null) ? (refCell.getStringCellValue()).replaceAll("\\h+", "") : "";

                    // Create a new component
                    Component component = new Component(level, number, description, rev, quantity, ref_des);

                    if (currentRow.getRowNum() == 1) {
                        bom.addComponent(component);
                    } else {
                        if (bom.hasTopLevel()) {
                            bom.addComponent(component);
                        } else {
                            throw new Exception("Missing Top Level");
                        }
                    }
                }
            }
            fs.close();

            // bom.printBOM();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bom;
    }

    private void compareSanminaToCustomer() {
        PairComparison pc;
        Component componentA, componentB;

        for (int i = 0; i < sanminaUniqueIdList.size(); i++) {
            String sUniId = sanminaUniqueIdList.get(i); // sanmina unique ID
            if (customerUniqueIdList.contains(sUniId)) {
                componentA = sanminaBOM.getComponent(sUniId);
                componentB = customerBOM.getComponent(sUniId);
                pc = new PairComparison(sUniId, componentA, componentB, ChangeStatus.NONE);
                customerUniqueIdList.remove(sUniId);     // to avoid re-compare when compare customer to sanmina BOM
            } else {
                componentA = sanminaBOM.getComponent(sUniId);
                pc = new PairComparison(sUniId, componentA, null, ChangeStatus.REMOVED);
            }
            pairComparisonList.add(pc);
        }
    }

    private void compareCustomerToSanmina() {
        PairComparison pc;
        Component componentB;

        for (int i = 0; i < customerUniqueIdList.size(); i++) {
            String cUniId = customerUniqueIdList.get(i);
            if (!sanminaUniqueIdList.contains(cUniId)) {
                componentB = customerBOM.getComponent(cUniId);
                pc = new PairComparison(cUniId, null, componentB, ChangeStatus.ADDED);
                pairComparisonList.add(pc);
            }
        }
    }

    private void writeToBomReport(PairComparison pc, int rowNum) {
        try {
            FileInputStream fs = new FileInputStream(report);
            Workbook wb = new XSSFWorkbook(fs);
            Sheet sheet = wb.getSheet("BOM_Report");
            
            // Set style
            CellStyle addStyle = wb.createCellStyle();
            addStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            addStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            CellStyle removeStyle = wb.createCellStyle();
            removeStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            removeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            CellStyle revChangeStyle = wb.createCellStyle();
            revChangeStyle.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
            revChangeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            CellStyle qtyChangeStyle = wb.createCellStyle();
            qtyChangeStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            qtyChangeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            CellStyle refChangellStyle = wb.createCellStyle();
            refChangellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            refChangellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            if (pc.getStatus() == ChangeStatus.ADDED) {
                Row row = sheet.getRow(rowNum);

                if (row == null) {
                    row = sheet.createRow(rowNum);
                }

                // create Change Alert cell
                Cell changeAlertCell = row.createCell(ColumnIndex.CHANGE_ALERT);
                changeAlertCell.setCellValue("Add");
                changeAlertCell.setCellStyle(addStyle);

                // create Level cell
                Cell levelCell = row.createCell(ColumnIndex.LEVEL);
                levelCell.setCellValue((Integer) pc.getComponentB().getLevel());

                // create Part cell
                Cell partCell = row.createCell(ColumnIndex.PART);
                partCell.setCellValue((String) pc.getComponentB().getNumber());
                partCell.setCellStyle(addStyle);

                // create Rev cell
                Cell revCell = row.createCell(ColumnIndex.REV);
                revCell.setCellValue((String) pc.getComponentB().getRev());
                revCell.setCellStyle(addStyle);

                // create Customer Parent BOM cell (part and rev)
                Cell custParentBOMPartCell = row.createCell(ColumnIndex.CUST_PARENT_PART);
                custParentBOMPartCell.setCellValue((String) pc.getComponentB().getParent().getNumber());

                Cell custParentBOMRevCell = row.createCell(ColumnIndex.CUST_PARENT_REV);
                custParentBOMRevCell.setCellValue((String) pc.getComponentB().getParent().getRev());

                // create Qty Change cell (WAS and IS)
                Cell qtyIsCell = row.createCell(ColumnIndex.QTY_CHANGE_IS);
                qtyIsCell.setCellValue((Integer) pc.getComponentB().getQuantity());
                qtyIsCell.setCellStyle(addStyle);

                // create Ref Des Change cell
                Cell refDesChangeCustCell = row.createCell(ColumnIndex.REF_DES_CHANGE_CUST);
                refDesChangeCustCell.setCellValue((String) (Arrays.toString(pc.getComponentB().getRef_des()).replace("[", "").replace("]", "")));
                refDesChangeCustCell.setCellStyle(addStyle);
                
                // create Ref Des (Cust cell)
                Cell refDesCustCell = row.createCell(ColumnIndex.REF_DES_CUST);
                refDesCustCell.setCellValue((String) (Arrays.toString(pc.getComponentB().getRef_des()).replace("[", "").replace("]", "")));
            } else if (pc.getStatus() == ChangeStatus.REMOVED) {
                Row row = sheet.getRow(rowNum);

                if (row == null) {
                    row = sheet.createRow(rowNum);
                }

                // create Change Alert cell
                Cell changeAlertCell = row.createCell(ColumnIndex.CHANGE_ALERT);
                changeAlertCell.setCellValue("Remove");
                changeAlertCell.setCellStyle(removeStyle);

                // create Level cell
                Cell levelCell = row.createCell(ColumnIndex.LEVEL);
                levelCell.setCellValue((Integer) pc.getComponentA().getLevel());

                // create Part cell
                Cell partCell = row.createCell(ColumnIndex.PART);
                partCell.setCellValue((String) pc.getComponentA().getNumber());
                partCell.setCellStyle(removeStyle);

                // create Rev cell
                Cell revCell = row.createCell(ColumnIndex.REV);
                revCell.setCellValue((String) pc.getComponentA().getRev());
                revCell.setCellStyle(removeStyle);

                // create Customer Parent BOM cell (part and rev)
                Cell sanmParentBOMPartCell = row.createCell(ColumnIndex.SANM_PARENT_PART);
                sanmParentBOMPartCell.setCellValue((String) pc.getComponentA().getParent().getNumber());

                Cell sanmParentBOMRevCell = row.createCell(ColumnIndex.SANM_PARENT_REV);
                sanmParentBOMRevCell.setCellValue((String) pc.getComponentA().getParent().getRev());

                // create Qty Change cell (WAS and IS)
                Cell qtyWasCell = row.createCell(ColumnIndex.QTY_CHANGE_WAS);
                qtyWasCell.setCellValue((Integer) pc.getComponentA().getQuantity());
                qtyWasCell.setCellStyle(removeStyle);

                Cell qtyIsCell = row.createCell(ColumnIndex.QTY_CHANGE_IS);
                qtyIsCell.setCellValue(0);
                qtyIsCell.setCellStyle(removeStyle);

                // create Ref Des Change cell
                Cell refDesChangeSanmCell = row.createCell(ColumnIndex.REF_DES_CHANGE_SANM);
                refDesChangeSanmCell.setCellValue((String) (Arrays.toString(pc.getComponentA().getRef_des()).replace("[", "").replace("]", "")));
                refDesChangeSanmCell.setCellStyle(removeStyle);
                
                // create Ref Des (Cust cell)
                Cell refDesSanmCell = row.createCell(ColumnIndex.REF_DES_SANM);
                refDesSanmCell.setCellValue((String) (Arrays.toString(pc.getComponentA().getRef_des()).replace("[", "").replace("]", "")));
            } else if (pc.getStatus() == ChangeStatus.NONE) {
                if (!pc.isRevDiffer() && !pc.isQtyDiffer() && !pc.isRefDesDiffer()) {
                    Row row = sheet.getRow(rowNum);

                    if (row == null) {
                        row = sheet.createRow(rowNum);
                    }

                    // create Level cell
                    Cell levelCell = row.createCell(ColumnIndex.LEVEL);
                    levelCell.setCellValue((Integer) pc.getComponentA().getLevel());

                    // create Part cell
                    Cell partCell = row.createCell(ColumnIndex.PART);
                    partCell.setCellValue((String) pc.getComponentA().getNumber());

                    // create Rev cell
                    Cell revCell = row.createCell(ColumnIndex.REV);
                    revCell.setCellValue((String) pc.getComponentA().getRev());

                    // create Sanmina Parent BOM cell (part and rev)
                    Cell sanmParentBOMPartCell = row.createCell(ColumnIndex.SANM_PARENT_PART);
                    sanmParentBOMPartCell.setCellValue((String) pc.getComponentA().getParent().getNumber());

                    Cell sanmParentBOMRevCell = row.createCell(ColumnIndex.SANM_PARENT_REV);
                    sanmParentBOMRevCell.setCellValue((String) pc.getComponentA().getParent().getRev());

                    // create Customer Parent BOM cell (part and rev)
                    Cell custParentBOMPartCell = row.createCell(ColumnIndex.CUST_PARENT_PART);
                    custParentBOMPartCell.setCellValue((String) pc.getComponentB().getParent().getNumber());

                    Cell custParentBOMRevCell = row.createCell(ColumnIndex.CUST_PARENT_REV);
                    custParentBOMRevCell.setCellValue((String) pc.getComponentB().getParent().getRev());

                    // create Rev Change cell (Was and IS)
                    Cell revChangeWasCell = row.createCell(ColumnIndex.REV_CHANGE_WAS);
                    revChangeWasCell.setCellValue((String) pc.getComponentA().getRev());

                    Cell revChangeIsCell = row.createCell(ColumnIndex.REV_CHANGE_IS);
                    revChangeIsCell.setCellValue((String) pc.getComponentB().getRev());

                    // create Qty Change cell (WAS and IS)
                    Cell qtyWasCell = row.createCell(ColumnIndex.QTY_CHANGE_WAS);
                    qtyWasCell.setCellValue((Integer) pc.getComponentA().getQuantity());

                    Cell qtyIsCell = row.createCell(ColumnIndex.QTY_CHANGE_IS);
                    qtyIsCell.setCellValue((Integer) pc.getComponentB().getQuantity());

                    // create Ref Des (Cust cell)
                    Cell refDesSanmCell = row.createCell(ColumnIndex.REF_DES_SANM);
                    refDesSanmCell.setCellValue((String) (Arrays.toString(pc.getComponentA().getRef_des()).replace("[", "").replace("]", "")));

                    Cell refDesCustCell = row.createCell(ColumnIndex.REF_DES_CUST);
                    refDesCustCell.setCellValue((String) (Arrays.toString(pc.getComponentB().getRef_des()).replace("[", "").replace("]", "")));
                } else {
                    Row row = sheet.getRow(rowNum);

                    if (row == null) {
                        row = sheet.createRow(rowNum);
                    }

                    // create Change Alert cell
                    Cell changeAlertCell = row.createCell(ColumnIndex.CHANGE_ALERT);

                    if (pc.isRevDiffer()) {
                        changeAlertCell.setCellValue("Revison");
                        changeAlertCell.setCellStyle(revChangeStyle);
                    } else if (pc.isQtyDiffer()) {
                        changeAlertCell.setCellValue("Quantity");
                        changeAlertCell.setCellStyle(qtyChangeStyle);
                    } else if (pc.isRefDesDiffer()) {
                        changeAlertCell.setCellValue("Location");
                        changeAlertCell.setCellStyle(refChangellStyle);
                    }

                    // create Level cell
                    Cell levelCell = row.createCell(ColumnIndex.LEVEL);
                    levelCell.setCellValue((Integer) pc.getComponentA().getLevel());

                    // create Part cell
                    Cell partCell = row.createCell(ColumnIndex.PART);
                    partCell.setCellValue((String) pc.getComponentA().getNumber());

                    // create Rev cell
                    Cell revCell = row.createCell(ColumnIndex.REV);
                    revCell.setCellValue((String) pc.getComponentA().getRev());

                    // create Sanmina Parent BOM cell (part and rev)
                    Cell sanmParentBOMPartCell = row.createCell(ColumnIndex.SANM_PARENT_PART);
                    sanmParentBOMPartCell.setCellValue((String) pc.getComponentA().getParent().getNumber());

                    Cell sanmParentBOMRevCell = row.createCell(ColumnIndex.SANM_PARENT_REV);
                    sanmParentBOMRevCell.setCellValue((String) pc.getComponentA().getParent().getRev());

                    // create Customer Parent BOM cell (part and rev)
                    Cell custParentBOMPartCell = row.createCell(ColumnIndex.CUST_PARENT_PART);
                    custParentBOMPartCell.setCellValue((String) pc.getComponentB().getParent().getNumber());

                    Cell custParentBOMRevCell = row.createCell(ColumnIndex.CUST_PARENT_REV);
                    custParentBOMRevCell.setCellValue((String) pc.getComponentB().getParent().getRev());

                    // create Rev Change cell (Was and IS)
                    Cell revChangeWasCell = row.createCell(ColumnIndex.REV_CHANGE_WAS);
                    revChangeWasCell.setCellValue((String) pc.getComponentA().getRev());

                    Cell revChangeIsCell = row.createCell(ColumnIndex.REV_CHANGE_IS);
                    revChangeIsCell.setCellValue((String) pc.getComponentB().getRev());

                    // create Qty Change cell (WAS and IS)
                    Cell qtyWasCell = row.createCell(ColumnIndex.QTY_CHANGE_WAS);
                    qtyWasCell.setCellValue((Integer) pc.getComponentA().getQuantity());

                    Cell qtyIsCell = row.createCell(ColumnIndex.QTY_CHANGE_IS);
                    qtyIsCell.setCellValue((Integer) pc.getComponentB().getQuantity());

                    // create Ref Des Change cell
                    Cell refDesChangeSanmCell = row.createCell(ColumnIndex.REF_DES_CHANGE_SANM);
                    refDesChangeSanmCell.setCellValue(pc.getRefDesDifferListA());

                    Cell refDesChangeCustCell = row.createCell(ColumnIndex.REF_DES_CHANGE_CUST);
                    refDesChangeCustCell.setCellValue(pc.getRefDesDifferListB());

                    // create Ref Des (Cust cell)
                    Cell refDesSanmCell = row.createCell(ColumnIndex.REF_DES_SANM);
                    refDesSanmCell.setCellValue((String) (Arrays.toString(pc.getComponentA().getRef_des()).replace("[", "").replace("]", "")));

                    Cell refDesCustCell = row.createCell(ColumnIndex.REF_DES_CUST);
                    refDesCustCell.setCellValue((String) (Arrays.toString(pc.getComponentB().getRef_des()).replace("[", "").replace("]", "")));

                    if (pc.isRevDiffer()) {
                        changeAlertCell.setCellStyle(revChangeStyle);
                        partCell.setCellStyle(revChangeStyle);
                        revCell.setCellStyle(revChangeStyle);
                        revChangeWasCell.setCellStyle(revChangeStyle);
                        revChangeIsCell.setCellStyle(revChangeStyle);
                    } else if (pc.isQtyDiffer()) {
                        changeAlertCell.setCellStyle(qtyChangeStyle);
                        partCell.setCellStyle(qtyChangeStyle);
                        revCell.setCellStyle(qtyChangeStyle);
                        qtyWasCell.setCellStyle(qtyChangeStyle);
                        qtyIsCell.setCellStyle(qtyChangeStyle);
                        refDesChangeSanmCell.setCellStyle(qtyChangeStyle);
                        refDesChangeCustCell.setCellStyle(qtyChangeStyle);
                    } else if (pc.isRefDesDiffer()) {
                        changeAlertCell.setCellStyle(refChangellStyle);
                        partCell.setCellStyle(refChangellStyle);
                        revCell.setCellStyle(refChangellStyle);
                        refDesChangeSanmCell.setCellStyle(refChangellStyle);
                        refDesChangeCustCell.setCellStyle(refChangellStyle);
                    }
                }
            }

            fs.close();

            FileOutputStream fo = new FileOutputStream(report);

            wb.write(fo);

            fo.close();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
