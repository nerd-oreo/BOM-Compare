package bomcompare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PairComparison {

    private String uniId;
    private Component componentA, componentB;
    private int status;
    private ArrayList<String> refDesDifferListA, refDesDifferListB;

    private boolean refDesDiffer = false;
    private boolean isRevDiffer = false;
    private boolean isQtyDiffer = false;

    public PairComparison(String uniId, Component componentA, Component componentB, int status) {
        this.uniId = uniId;
        this.componentA = componentA;
        this.componentB = componentB;
        this.status = status;

        if (this.componentA != null && this.componentB != null) {
            refDesDifferListA = new ArrayList<String>();
            refDesDifferListB = new ArrayList<String>();

            // check rev
            if (!this.componentA.getRev().equals((String) this.componentB.getRev())) {
                this.isRevDiffer = true;
            }

            // check qty
            if (this.componentA.getQuantity() != this.componentB.getQuantity()) {
                this.isQtyDiffer = true;
            }

            compareRefDes();	// compare ref des of two component
        }
    }

    public String getUniId() {
        return uniId;
    }

    public void setUniId(String uniId) {
        this.uniId = uniId;
    }

    public Component getComponentA() {
        return componentA;
    }

    public void setComponentA(Component componentA) {
        this.componentA = componentA;
    }

    public Component getComponentB() {
        return componentB;
    }

    public void setComponentB(Component componentB) {
        this.componentB = componentB;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isRefDesDiffer() {
        return refDesDiffer;
    }

    public boolean isRevDiffer() {
        return isRevDiffer;
    }

    public boolean isQtyDiffer() {
        return isQtyDiffer;
    }

    /*
    private void setRefDesDiffer(String list, String refDef) {
    	if(list.equals("A")) {
    		refDesDifferListA.add(refDef);
    	} else if (list.equals("B")) {
    		refDesDifferListB.add(refDef);
    	}
    }
     */
    public String getRefDesDifferListA() {
        return this.refDesDifferListA.toString().replace("[", "").replace("]", "");
    }

    public String getRefDesDifferListB() {
        return this.refDesDifferListB.toString().replace("[", "").replace("]", "");
    }

    private void compareRefDes() {
        List<String> listA = Arrays.asList(componentA.getRef_des());
        List<String> listB = Arrays.asList(componentB.getRef_des());

        for (int i = 0; i < componentA.getRef_des().length; i++) {
            String refDes = componentA.getRef_des()[i];
            if (!listB.contains(refDes)) {
                refDesDifferListA.add(refDes);
            }
            //System.out.println("List A: " + refDesDifferListA.toString());
        }

        for (int i = 0; i < componentB.getRef_des().length; i++) {
            String refDes = componentB.getRef_des()[i];
            if (!listA.contains(refDes)) {
                refDesDifferListB.add(refDes);
            }
            //System.out.println("List B: " + refDesDifferListB.toString());
        }

        if (!refDesDifferListA.isEmpty() || !refDesDifferListB.isEmpty()) {
            refDesDiffer = true;
        }
    }

}
