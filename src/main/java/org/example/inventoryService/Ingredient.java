package org.example.inventoryService;

public class Ingredient {
    private final String type;
    private final String name;
    private int count;

    public Ingredient(String type, String name, int count) {
        this.type = type;
        this.name = name;
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }
}
