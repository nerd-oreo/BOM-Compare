
package bomcompare;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class BOM {
    private LinkedHashMap<String, Component> bom; // <uniqueId, Component>
    private Component currentParent;
    private Component prevComponent;
    private Stack<Component> parentStack = new Stack<>();
    
    public BOM () {
        bom = new LinkedHashMap<>();
    }
    
    public void addComponent(Component comp) {
        String uniId = null;
        
        if(comp.isTopLevel()) { // level = 0
            uniId = comp.getLevel() + ":" + comp.getNumber();
            currentParent = comp;
        } else {
            if (prevComponent.getLevel() != 0 &&                      // lower level
                    prevComponent.getLevel() < comp.getLevel()) {
                parentStack.push(currentParent);
                currentParent = prevComponent;
            } else if (prevComponent.getLevel() > comp.getLevel()) {  // upper level
                currentParent= parentStack.pop();
            }
            
            
            
            
            // generate uniId based on parent and child part number
            uniId = currentParent.getLevel() + ":" + currentParent.getNumber() 
                    + ":" + comp.getLevel() + ":" + comp.getNumber();
            
            
        
        }
        
        bom.put(uniId, comp);   // add component to BOM
        prevComponent = comp;
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
