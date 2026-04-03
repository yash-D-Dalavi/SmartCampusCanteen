package com.mit.smartcampuscanteen;

// Firebase ke "orders" node ka ek order — yeh class us data ko pakadti hai
public class Order {

    // In sabka naam Firebase JSON keys se EXACTLY match karna chahiye
    public String orderId;        // Firebase ka auto-generated key
    public String studentName;
    public String itemName;
    public int quantity;
    public int price;
    public int tokenNumber;       // Digital token #101, #102...
    public String status;         // "Received" -> "Preparing" -> "Ready"
    public long timestamp;        // Order kab place hua (milliseconds)

    // Firebase ko EMPTY constructor CHAHIYE — isko mat hatana!
    public Order() {}

    // Naya order banane ke liye constructor
    public Order(String studentName, String itemName,
                 int quantity, int price, int tokenNumber) {
        this.studentName = studentName;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.tokenNumber = tokenNumber;
        this.status = "Received";   // Hamesha "Received" se shuru hoga
        this.timestamp = System.currentTimeMillis();
    }
}
