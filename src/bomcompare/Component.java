
package bomcompare;

public class Component {
    private int level;
    private String number;
    private String description;
    private int quantity;
    private String ref_des;

    public Component(int level, String number, String description, int quantity, String ref_des) {
        this.level = level;
        this.number = number;
        this.description = description;
        this.quantity = quantity;
        this.ref_des = ref_des;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getRef_des() {
        return ref_des;
    }

    public void setRef_des(String ref_des) {
        this.ref_des = ref_des;
    }
    
    public boolean isTopLevel() {
        return (this.level == 0);
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
