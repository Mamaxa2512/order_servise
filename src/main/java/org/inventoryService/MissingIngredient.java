package org.inventoryService;

public class MissingIngredient {
    private final String name;
    private final int requiredCount;
    private final int availableCount;

    public MissingIngredient(String name, int requiredCount, int availableCount){
        this.name = name;
        this.requiredCount = requiredCount;
        this.availableCount = availableCount;
    }

    public String getName() {
        return name;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public int getMissingCount(){
        return (requiredCount-availableCount);
    }
}
