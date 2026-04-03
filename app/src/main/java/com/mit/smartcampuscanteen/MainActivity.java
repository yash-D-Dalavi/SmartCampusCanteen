package com.mit.smartcampuscanteen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ===== OFFLINE SUPPORT — YEH EK LINE ADD HUI HAI =====
        // Internet band ho toh bhi app kaam karega
        // Data phone mein save hoga, internet aate hi Firebase sync karega
        // IMPORTANT: setContentView ke BAAD, kisi bhi Firebase call se PEHLE likhna zaroori hai
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // ===== TITLE BAR HATAO =====
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // ===== TEEN CARDS KE REFERENCES =====
        CardView cardStudent = findViewById(R.id.card_student);
        CardView cardKitchen = findViewById(R.id.card_kitchen);
        CardView cardCounter = findViewById(R.id.card_counter);

        // ===== CLICK LISTENERS =====
        cardStudent.setOnClickListener(view -> {
            animateClick(view);
            Intent intent = new Intent(MainActivity.this, StudentActivity.class);
            startActivity(intent);
        });

        cardKitchen.setOnClickListener(view -> {
            animateClick(view);
            Intent intent = new Intent(MainActivity.this, KitchenActivity.class);
            startActivity(intent);
        });

        cardCounter.setOnClickListener(view -> {
            animateClick(view);
            Intent intent = new Intent(MainActivity.this, CounterActivity.class);
            startActivity(intent);
        });
    }

    // ===== PRESS ANIMATION =====
    private void animateClick(android.view.View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start();
                })
                .start();
    }
}