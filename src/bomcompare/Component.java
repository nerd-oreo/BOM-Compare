
package bomcompare;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

public class Component {
    private int level;
    private String number;
    private String description;
    private String rev;
    private int quantity;
    private String [] ref_des;
    
    private Component parent;
    private LinkedHashMap<String, Component> childList = new LinkedHashMap<>(); // <part_number, Component>

    public Component(int level, String number, String description, String rev, int quantity, String ref_des) {
        this.level = level;
        this.number = number;
        this.description = description;
        this.rev = rev;
        this.quantity = quantity;
        this.ref_des  = ref_des.split(",");
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }
    
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String [] getRef_des() {
        return ref_des;
    }
    
    
    public String getRef_desAsString() {
        return Arrays.toString(this.ref_des);
    }

    public void setRef_des(String ref_des) {
        this.ref_des  = ref_des.split(",");
    }
    
    public boolean isTopLevel() {
        return (this.level == 0);
    }

    public Component getParent() {
        return parent;
    }

    public void setParent(Component parent) {
        this.parent = parent;
    }

    public LinkedHashMap<String, Component> getChildList() {
        return childList;
    }

    public void addChild(String number, Component comp) {
        this.childList.put(number, comp);
    }
    
    public String getChildListKeyAsString() {
        Set<String> key = childList.keySet();
        String [] childNumbers = key.toArray(new String[key.size()]);
        return Arrays.toString(childNumbers);
    }
    
    public Component getComponentFromChildList(String number) {
        return (Component) this.childList.get(number);
    }
    
    @Override
    public String toString() {
        return this.level + " | " 
                + this.number + " | " 
                + this.description + " | "
                + this.quantity + " | "
                + this.ref_des;
    }
}
