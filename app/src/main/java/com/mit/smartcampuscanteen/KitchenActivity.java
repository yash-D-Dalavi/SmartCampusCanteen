package com.mit.smartcampuscanteen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class KitchenActivity extends AppCompatActivity {

    private static final String TAG = "KitchenActivity";

    private DatabaseReference menuRef;
    private DatabaseReference ordersRef;

    private RecyclerView rvMenu;
    private KitchenMenuAdapter menuAdapter;
    private List<MenuItem> menuItems   = new ArrayList<>();
    private List<String>   menuItemIds = new ArrayList<>();

    private RecyclerView rvOrders;
    private KitchenOrderAdapter orderAdapter;
    private List<Order>  incomingOrders   = new ArrayList<>();
    private List<String> incomingOrderIds = new ArrayList<>();

    private TextView tabMenu, tabOrders;
    private boolean isMenuTabActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen);

        menuRef   = FirebaseDatabase.getInstance().getReference("menu_items");
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        tabMenu   = findViewById(R.id.tab_menu);
        tabOrders = findViewById(R.id.tab_orders);
        rvMenu    = findViewById(R.id.rv_kitchen_menu);
        rvOrders  = findViewById(R.id.rv_incoming_orders);

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new KitchenMenuAdapter();
        rvMenu.setAdapter(menuAdapter);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new KitchenOrderAdapter();
        rvOrders.setAdapter(orderAdapter);

        tabMenu.setOnClickListener(v   -> switchTab(true));
        tabOrders.setOnClickListener(v -> switchTab(false));

        listenForMenu();
        listenForOrders();
    }

    private void switchTab(boolean showMenu) {
        isMenuTabActive = showMenu;
        if (showMenu) {
            tabMenu.setTextColor(getResources().getColor(R.color.primary_orange));
            tabMenu.setBackgroundResource(R.drawable.tab_active_indicator);
            tabOrders.setTextColor(getResources().getColor(R.color.text_secondary));
            tabOrders.setBackgroundColor(0x00000000);
            rvMenu.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tabOrders.setTextColor(getResources().getColor(R.color.primary_orange));
            tabOrders.setBackgroundResource(R.drawable.tab_active_indicator);
            tabMenu.setTextColor(getResources().getColor(R.color.text_secondary));
            tabMenu.setBackgroundColor(0x00000000);
            rvMenu.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    private void listenForMenu() {
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                menuItems.clear();
                menuItemIds.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    MenuItem item = snap.getValue(MenuItem.class);
                    if (item != null) {
                        menuItems.add(item);
                        menuItemIds.add(snap.getKey());
                    }
                }
                menuAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError e) {
                Log.e(TAG, "Menu error: " + e.getMessage());
            }
        });
    }

    private void listenForOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                incomingOrders.clear();
                incomingOrderIds.clear();

                if (!snapshot.exists()) {
                    orderAdapter.notifyDataSetChanged();
                    tabOrders.setText("Incoming Orders (0)");
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
                        if ("Ready".equals(order.status)) continue;

                        if (order.status == null)      order.status      = "Received";
                        if (order.studentName == null) order.studentName = "Student";
                        if (order.itemName == null)    order.itemName    = "Item";

                        incomingOrders.add(order);
                        incomingOrderIds.add(snap.getKey());

                    } catch (Exception e) {
                        Log.e(TAG, "Order parse skip: " + e.getMessage());
                    }
                }

                orderAdapter.notifyDataSetChanged();
                tabOrders.setText("Incoming Orders (" + incomingOrders.size() + ")");
            }

            @Override
            public void onCancelled(DatabaseError e) {
                Log.e(TAG, "Orders error: " + e.getMessage());
            }
        });
    }

    private void updateOrderStatus(String orderId, String newStatus, int position) {
        ordersRef.child(orderId).child("status").setValue(newStatus)
                .addOnSuccessListener(unused -> {
                    // CHANGED: Hindi → English
                    String msg = "Preparing".equals(newStatus)
                            ? "👨‍🍳 Started Preparing!"
                            : "✅ Order marked as Ready!";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Status update failed: " + e.getMessage()));
    }

    class KitchenMenuAdapter extends RecyclerView.Adapter<KitchenMenuAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_kitchen_card, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            if (pos >= menuItems.size()) return;
            MenuItem item = menuItems.get(pos);
            if (item == null) return;

            h.tvName.setText(item.name);
            h.tvPrice.setText("₹" + item.price);
            h.tvEmoji.setText(getEmoji(item.name));

            h.sw.setOnCheckedChangeListener(null);
            h.sw.setChecked(item.isAvailable);

            if (item.isAvailable) {
                h.tvStatus.setText("● AVAILABLE");
                h.tvStatus.setTextColor(getResources().getColor(R.color.available_green));
            } else {
                h.tvStatus.setText("● SOLD OUT");
                h.tvStatus.setTextColor(getResources().getColor(R.color.sold_out_red));
            }

            h.sw.setOnCheckedChangeListener((btn, checked) -> {
                int currentPos = h.getAdapterPosition();
                if (currentPos == RecyclerView.NO_ID) return;
                menuRef.child(menuItemIds.get(currentPos))
                        .child("isAvailable").setValue(checked);
                // CHANGED: Hindi → English
                Toast.makeText(KitchenActivity.this,
                        item.name + (checked ? " ✅ Available" : " ❌ Sold Out"),
                        Toast.LENGTH_SHORT).show();
            });
        }

        @Override public int getItemCount() {
            return menuItems != null ? menuItems.size() : 0;
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName, tvPrice, tvStatus;
            Switch sw;
            VH(View v) {
                super(v);
                tvEmoji  = v.findViewById(R.id.tv_item_emoji);
                tvName   = v.findViewById(R.id.tv_item_name);
                tvPrice  = v.findViewById(R.id.tv_item_price);
                tvStatus = v.findViewById(R.id.tv_item_status);
                sw       = v.findViewById(R.id.switch_availability);
            }
        }
    }

    class KitchenOrderAdapter extends RecyclerView.Adapter<KitchenOrderAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_kitchen_order, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            if (pos >= incomingOrders.size()) return;
            Order order = incomingOrders.get(pos);
            String orderId = incomingOrderIds.get(pos);
            if (order == null || orderId == null) return;

            h.tvToken.setText("#" + order.tokenNumber);
            h.tvItem.setText(order.itemName + " x" + order.quantity);
            // CHANGED: Hindi → English
            h.tvStudent.setText("Student: " + order.studentName);

            if ("Received".equals(order.status)) {
                h.tvNewBadge.setVisibility(View.VISIBLE);
                h.btnPreparing.setEnabled(true);
                h.btnPreparing.setAlpha(1f);
                h.btnDone.setEnabled(false);
                h.btnDone.setAlpha(0.4f);
                h.btnPreparing.setOnClickListener(v ->
                        updateOrderStatus(orderId, "Preparing", h.getAdapterPosition()));

            } else if ("Preparing".equals(order.status)) {
                h.tvNewBadge.setVisibility(View.GONE);
                h.btnPreparing.setEnabled(false);
                h.btnPreparing.setAlpha(0.4f);
                h.btnDone.setEnabled(true);
                h.btnDone.setAlpha(1f);
                h.btnDone.setOnClickListener(v ->
                        updateOrderStatus(orderId, "Ready", h.getAdapterPosition()));
            }
        }

        @Override public int getItemCount() {
            return incomingOrders != null ? incomingOrders.size() : 0;
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvToken, tvItem, tvStudent, tvNewBadge;
            Button btnPreparing, btnDone;
            VH(View v) {
                super(v);
                tvToken      = v.findViewById(R.id.tv_kitchen_token);
                tvItem       = v.findViewById(R.id.tv_kitchen_item);
                tvStudent    = v.findViewById(R.id.tv_kitchen_student);
                tvNewBadge   = v.findViewById(R.id.tv_new_badge);
                btnPreparing = v.findViewById(R.id.btn_start_preparing);
                btnDone      = v.findViewById(R.id.btn_mark_done);
            }
        }
    }

    private String getEmoji(String name) {
        if (name == null) return "🍽️";
        switch (name.toLowerCase()) {
            case "samosa":         return "🥟";
            case "vada pav":       return "🍔";
            case "paneer roll":    return "🌯";
            case "maggi":          return "🍜";
            case "sandwich":       return "🥪";
            case "bhel puri":      return "🥗";
            case "kachori":        return "🫓";
            case "poha":           return "🍚";
            case "idli sambar":    return "🫓";
            case "upma":           return "🍲";
            case "misal pav":      return "🥘";
            case "dosa":           return "🫔";
            case "masala dosa":    return "🫔";
            case "bread omelette": return "🍳";
            case "aloo paratha":   return "🫓";
            case "rava idli":      return "🍮";
            case "pav bhaji":      return "🍛";
            case "dal rice":       return "🍱";
            case "chole bhature":  return "🥙";
            case "rajma rice":     return "🫘";
            case "paneer bhurji":  return "🧆";
            case "thali full":     return "🍽️";
            case "tea":            return "☕";
            case "coffee":         return "☕";
            case "cold coffee":    return "🧋";
            case "lassi":          return "🥛";
            case "lemon soda":     return "🍋";
            case "mango shake":    return "🥭";
            case "gulab jamun":    return "🍯";
            case "kheer":          return "🍮";
            default:               return "🍽️";
        }
    }
}