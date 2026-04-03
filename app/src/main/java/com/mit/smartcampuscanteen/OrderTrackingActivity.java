package com.mit.smartcampuscanteen;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.google.firebase.database.*;

public class OrderTrackingActivity extends AppCompatActivity {

    private DatabaseReference orderRef;

    private TextView tvTokenNumber, tvOrderItem, tvOrderPrice;
    private TextView dotReceived, dotPreparing, dotReady;
    private View line1, line2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        String orderId  = getIntent().getStringExtra("ORDER_ID");
        int tokenNumber = getIntent().getIntExtra("TOKEN_NUMBER", 0);
        String itemName = getIntent().getStringExtra("ITEM_NAME");
        int price       = getIntent().getIntExtra("PRICE", 0);

        tvTokenNumber = findViewById(R.id.tv_token_number);
        tvOrderItem   = findViewById(R.id.tv_order_item);
        tvOrderPrice  = findViewById(R.id.tv_order_price);
        dotReceived   = findViewById(R.id.dot_received);
        dotPreparing  = findViewById(R.id.dot_preparing);
        dotReady      = findViewById(R.id.dot_ready);
        line1         = findViewById(R.id.line_1);
        line2         = findViewById(R.id.line_2);

        tvTokenNumber.setText("#" + tokenNumber);
        tvOrderItem.setText("Item: " + itemName + " x1");
        tvOrderPrice.setText("Total: ₹" + price);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        orderRef = FirebaseDatabase.getInstance()
                .getReference("orders").child(orderId);

        listenForStatusUpdates();
    }

    private void listenForStatusUpdates() {
        orderRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status == null) return;
                updateStatusUI(status);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void updateStatusUI(String status) {
        int inactiveColor   = getResources().getColor(R.color.text_hint);
        int lineActiveColor = getResources().getColor(R.color.available_green);

        setDotActive(dotReceived, true);

        switch (status) {
            case "Received":
                setDotActive(dotPreparing, false);
                setDotActive(dotReady, false);
                line1.setBackgroundColor(inactiveColor);
                line2.setBackgroundColor(inactiveColor);
                break;

            case "Preparing":
                setDotActive(dotPreparing, true);
                dotPreparing.setText("✓");
                setDotActive(dotReady, false);
                line1.setBackgroundColor(lineActiveColor);
                line2.setBackgroundColor(inactiveColor);
                break;

            case "Ready":
                setDotActive(dotPreparing, true);
                dotPreparing.setText("✓");
                setDotActive(dotReady, true);
                dotReady.setText("✓");
                line1.setBackgroundColor(lineActiveColor);
                line2.setBackgroundColor(lineActiveColor);

                // CHANGED: Hindi → English
                Toast.makeText(OrderTrackingActivity.this,
                        "🎉 Your order is READY! Please collect from counter.",
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void setDotActive(TextView dot, boolean active) {
        if (active) {
            dot.setBackgroundResource(R.drawable.status_dot_active);
        } else {
            dot.setBackgroundResource(R.drawable.status_dot_inactive);
        }
    }
}