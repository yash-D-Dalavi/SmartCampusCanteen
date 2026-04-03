package com.mit.smartcampuscanteen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class StudentActivity extends AppCompatActivity {

    private DatabaseReference menuRef;
    private DatabaseReference ordersRef;
    private DatabaseReference counterRef;

    private RecyclerView rvMenu;
    private MenuAdapter menuAdapter;
    private EditText etStudentName;

    private List<MenuItem> menuItems   = new ArrayList<>();
    private List<String>   menuItemIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        // Firebase connect
        menuRef    = FirebaseDatabase.getInstance().getReference("menu_items");
        ordersRef  = FirebaseDatabase.getInstance().getReference("orders");
        counterRef = FirebaseDatabase.getInstance().getReference("counters/token_counter");

        // Views
        etStudentName = findViewById(R.id.et_student_name);
        rvMenu        = findViewById(R.id.rv_menu_items);

        // ===== YEH 4 LINES ADD HUI HAIN — AUTO FILL =====
        // LoginActivity ne SharedPreferences mein naam save kiya tha
        // Woh naam yahan automatically fill ho jaayega — student ko dobara likhna nahi padega
        String savedName = getSharedPreferences("canteen_prefs", MODE_PRIVATE)
                .getString("student_name", "");
        if (!savedName.isEmpty()) {
            etStudentName.setText(savedName);
        }
        // ===== AUTO FILL END =====

        // 2-column grid — Zomato jaisa
        rvMenu.setLayoutManager(new GridLayoutManager(this, 2));
        menuAdapter = new MenuAdapter();
        rvMenu.setAdapter(menuAdapter);

        // My Orders button
        Button btnMyOrders = findViewById(R.id.btn_my_orders);
        btnMyOrders.setOnClickListener(v ->
                startActivity(new Intent(this, CounterActivity.class)));

        // Firebase se menu load karo
        loadMenuFromFirebase();
    }

    // ===== FIREBASE SE MENU LOAD =====
    private void loadMenuFromFirebase() {
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

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(StudentActivity.this,
                        "Menu load failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== TOKEN GENERATE + ORDER PLACE =====
    private void placeOrder(MenuItem item) {

        String studentName = etStudentName.getText().toString().trim();
        if (studentName.isEmpty()) {
            etStudentName.setError("Please enter your name!");
            etStudentName.requestFocus();
            return;
        }

        counterRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Integer currentToken = currentData.child("lastToken").getValue(Integer.class);
                if (currentToken == null) currentToken = 100;

                int newToken = currentToken + 1;
                currentData.child("lastToken").setValue(newToken);
                currentData.child("date").setValue(
                        java.text.DateFormat.getDateInstance().format(new java.util.Date()));

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed,
                                   DataSnapshot snapshot) {
                if (committed && error == null) {
                    int tokenNumber = snapshot.child("lastToken").getValue(Integer.class);

                    Order newOrder = new Order(
                            studentName,
                            item.name,
                            1,
                            item.price,
                            tokenNumber
                    );

                    String orderId = ordersRef.push().getKey();
                    newOrder.orderId = orderId;
                    ordersRef.child(orderId).setValue(newOrder)
                            .addOnSuccessListener(unused -> {
                                Intent intent = new Intent(
                                        StudentActivity.this,
                                        OrderTrackingActivity.class
                                );
                                intent.putExtra("ORDER_ID", orderId);
                                intent.putExtra("TOKEN_NUMBER", tokenNumber);
                                intent.putExtra("ITEM_NAME", item.name);
                                intent.putExtra("PRICE", item.price);
                                startActivity(intent);
                            });
                }
            }
        });
    }

    // ===== RECYCLERVIEW ADAPTER =====
    class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MenuItem item = menuItems.get(position);

            holder.tvName.setText(item.name);
            holder.tvPrice.setText("₹" + item.price);
            holder.tvCategory.setText(item.category);
            holder.tvEmoji.setText(getEmoji(item.name));

            if (item.isAvailable) {
                holder.soldOutOverlay.setVisibility(View.GONE);
                holder.btnAdd.setVisibility(View.VISIBLE);
                holder.btnAdd.setOnClickListener(v -> placeOrder(item));
            } else {
                holder.soldOutOverlay.setVisibility(View.VISIBLE);
                holder.btnAdd.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return menuItems.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName, tvPrice, tvCategory, soldOutOverlay;
            Button btnAdd;

            ViewHolder(View v) {
                super(v);
                tvEmoji        = v.findViewById(R.id.tv_food_emoji);
                tvName         = v.findViewById(R.id.tv_item_name);
                tvPrice        = v.findViewById(R.id.tv_price);
                tvCategory     = v.findViewById(R.id.tv_category);
                soldOutOverlay = v.findViewById(R.id.tv_sold_out_overlay);
                btnAdd         = v.findViewById(R.id.btn_add_order);
            }
        }
    }

    // ===== EMOJI FUNCTION — same as tumhara current code =====
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