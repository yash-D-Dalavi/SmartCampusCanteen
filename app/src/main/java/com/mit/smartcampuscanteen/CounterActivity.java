package com.mit.smartcampuscanteen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class CounterActivity extends AppCompatActivity
        implements OrderAdapter.OnOrderActionListener {

    private static final String TAG = "CounterActivity";

    private DatabaseReference ordersRef;
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private TextView tvPendingCount;

    private List<Order>  allOrders      = new ArrayList<>();
    private List<String> allOrderIds    = new ArrayList<>();
    private List<Order>  displayOrders  = new ArrayList<>();
    private List<String> displayOrderIds = new ArrayList<>();

    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        tvPendingCount = findViewById(R.id.tv_pending_count);
        rvOrders       = findViewById(R.id.rv_orders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        orderAdapter = new OrderAdapter(this, displayOrders, displayOrderIds, this);
        rvOrders.setAdapter(orderAdapter);

        setupFilterButtons();
        listenForOrders();
    }

    private void listenForOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allOrders.clear();
                allOrderIds.clear();

                if (!snapshot.exists()) {
                    tvPendingCount.setText("Pending: 0");
                    applyFilter(currentFilter);
                    return;
                }

                for (DataSnapshot snap : snapshot.getChildren()) {
                    try {
                        if (snap.getValue() instanceof String) {
                            Log.w(TAG, "String entry skip: " + snap.getValue());
                            continue;
                        }

                        Order order = snap.getValue(Order.class);
                        if (order == null) continue;

                        if (order.status == null)      order.status      = "Received";
                        if (order.studentName == null) order.studentName = "Student";
                        if (order.itemName == null)    order.itemName    = "Item";

                        allOrders.add(order);
                        allOrderIds.add(snap.getKey());

                    } catch (Exception e) {
                        Log.e(TAG, "Order parse skip: " + e.getMessage());
                    }
                }

                long pendingCount = allOrders.stream()
                        .filter(o -> !"Ready".equals(o.status)).count();
                tvPendingCount.setText("Pending: " + pendingCount);

                applyFilter(currentFilter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Orders cancelled: " + error.getMessage());
            }
        });
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        displayOrders.clear();
        displayOrderIds.clear();

        for (int i = 0; i < allOrders.size(); i++) {
            Order order = allOrders.get(i);
            boolean shouldShow;

            switch (filter) {
                case "Pending":
                    shouldShow = !"Ready".equals(order.status);
                    break;
                case "Ready":
                    shouldShow = "Ready".equals(order.status);
                    break;
                default:
                    shouldShow = true;
            }

            if (shouldShow) {
                displayOrders.add(order);
                displayOrderIds.add(allOrderIds.get(i));
            }
        }
        orderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMarkReady(String orderId, int position) {
        if (position < 0 || position >= displayOrders.size()) {
            Log.e(TAG, "Invalid position: " + position);
            return;
        }

        int tokenNum = displayOrders.get(position).tokenNumber;

        ordersRef.child(orderId).child("status").setValue("Ready")
                .addOnSuccessListener(unused ->
                        // CHANGED: Hindi → English
                        Toast.makeText(this,
                                "✅ Token #" + tokenNum + " marked as Ready!",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void setupFilterButtons() {
        Button btnAll     = findViewById(R.id.btn_filter_all);
        Button btnPending = findViewById(R.id.btn_filter_pending);
        Button btnReady   = findViewById(R.id.btn_filter_ready);

        btnAll.setOnClickListener(v     -> applyFilter("All"));
        btnPending.setOnClickListener(v -> applyFilter("Pending"));
        btnReady.setOnClickListener(v   -> applyFilter("Ready"));
    }
}