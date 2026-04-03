package com.mit.smartcampuscanteen;
// Yeh class Firebase ke ek menu item ko represent karti hai
// Firebase automatically is class mein data fill karta hai
public class MenuItem {

    // In fields ke naam Firebase JSON keys se EXACTLY match karne chahiye!
    public String name;
    public int price;
    public String category;
    public String imageUrl;
    public boolean isAvailable;

    // Firebase ko ek EMPTY constructor chahiye hota hai - mandatory!
    public MenuItem() {}

    // Convenience constructor
    public MenuItem(String name, int price, String category, boolean isAvailable) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.isAvailable = isAvailable;
    }
}
