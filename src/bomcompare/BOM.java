
package bomcompare;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BOM {
    private LinkedHashMap<String, Component> bom; // <uniqueId, Component>
    private Component currentParent;
    
    public BOM () {
        bom = new LinkedHashMap<>();
    }
    
    public void addComponent(Component comp) {
        String uniId = null;
        
        if(comp.isTopLevel()) {
            uniId = comp.getLevel() + ":" + comp.getNumber();
            currentParent = comp;
        } else {
            // generate uniId based on parent and child part number
            uniId = currentParent.getLevel() + ":" + currentParent.getNumber() + ":" + comp.getLevel() + ":" + comp.getNumber();
        }
        
        bom.put(uniId, comp);   // add component to BOM
    }
    
    public Component getComponent(String uniId) {
        return (Component) bom.get(uniId);
    }
    
    public void printBOM() {
        for(Map.Entry<String, Component> entry : bom.entrySet()) {
            System.out.println("Key = " + entry.getKey());
            System.out.println("Value = [ " + entry.getValue().toString() + " ]");
            System.out.println("***************");
        }
    }
}
