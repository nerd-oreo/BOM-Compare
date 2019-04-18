
package bomcompare;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class BOM {
    private final LinkedHashMap<String, Component> bom; // <uniqueId, Component>
    private final Stack<Component> parentStack = new Stack<>();
    private ArrayList<String> uniqueIdList = new ArrayList<>();
    
    private Component currentParent, prevComponent;
    
    public BOM () {
        bom = new LinkedHashMap<>();
    }

    public ArrayList<String> getUniqueIdList() {
        return uniqueIdList;
    }

    public void setUniqueIdList(ArrayList<String> uniqueIdList) {
        this.uniqueIdList = uniqueIdList;
    }
    
    public void addComponent(Component comp) {
        String uniId;
        
        if(comp.isTopLevel()) { // level = 0
            uniId = comp.getLevel() + ":" + comp.getNumber();
            currentParent = comp;
        } else {
            if (prevComponent.getLevel() != 0 &&                      // lower level
                    prevComponent.getLevel() < comp.getLevel()) {
                parentStack.push(currentParent);
                currentParent = prevComponent;
            } else if (prevComponent.getLevel() > comp.getLevel()) {  // upper level               
                int popCounter = Math.abs(comp.getLevel() - prevComponent.getLevel());
                
                for(int i = 0; i < popCounter; i++) {
                    currentParent= parentStack.pop();
                }
            }

            // generate uniId based on parent and child part number
            uniId = currentParent.getLevel() + ":" + currentParent.getNumber() 
                    + ":" + comp.getLevel() + ":" + comp.getNumber();        
        }
        
        comp.setParent(currentParent);
        addChild(comp);         // add comp to currentParent
        bom.put(uniId, comp);   // add component to BOM
        uniqueIdList.add(uniId);
        prevComponent = comp;
    }
    
    public Component getComponent(String uniId) {
        return (Component) bom.get(uniId);
    }
    
    public void printBOM() {
        for(Map.Entry<String, Component> entry : bom.entrySet()) {
            String uniqueId = entry.getKey();
            Component comp = entry.getValue();
            
            
            System.out.println("");
            System.out.println("Unique ID:\t" + entry.getKey());
            System.out.println("Parent:\t\t" + comp.getParent().getNumber());
            System.out.println("Child List:\t" + comp.getParent().getChildListKeyAsString());
            System.out.println("Level:\t\t" + comp.getLevel());
            System.out.println("Number:\t\t" + comp.getNumber());
            System.out.println("Description:\t" + comp.getDescription());
            System.out.println("Rev:\t\t" + comp.getRev());
            System.out.println("Quantity:\t" + comp.getQuantity());
            System.out.println("Ref Des:\t" + comp.getRef_desAsString());
            System.out.println("");
        }
    }
    
    private void addChild(Component comp) {
        currentParent.addChild(comp.getNumber(), comp);
    }
}
