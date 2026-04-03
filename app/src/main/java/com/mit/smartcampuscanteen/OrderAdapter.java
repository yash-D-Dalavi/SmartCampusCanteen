package com.mit.smartcampuscanteen;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Yeh adapter Counter ke RecyclerView ko data deta hai
// Interface Pattern use kiya hai — isse Counter Activity ko pata chalega
// ki kaunse order ka button daba
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> orderList;
    private List<String> orderIds;
    private Context context;

    // Interface — Counter Activity implement karegi yeh
    public interface OnOrderActionListener {
        void onMarkReady(String orderId, int position);
    }

    private OnOrderActionListener listener;

    // Constructor
    public OrderAdapter(Context context, List<Order> orderList,
                        List<String> orderIds, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.orderIds = orderIds;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Order order = orderList.get(position);
        String orderId = orderIds.get(position);

        // Token number dikhao — e.g. "#105"
        holder.tvToken.setText("#" + order.tokenNumber);
        holder.tvStudentName.setText(order.studentName);
        holder.tvOrderDetails.setText(
                order.itemName + " x" + order.quantity + " • ₹" + order.price);

        // Status badge color — status ke hisaab se
        updateStatusBadge(holder.tvStatusBadge, order.status);

        // "Mark Ready" button — sirf tab dikhao jab Ready nahi hua
        if ("Ready".equals(order.status)) {
            holder.btnMarkReady.setText("✓ Done");
            holder.btnMarkReady.setEnabled(false);
            holder.btnMarkReady.setAlpha(0.5f); // Faded dikhao
        } else {
            holder.btnMarkReady.setText("Mark Ready");
            holder.btnMarkReady.setEnabled(true);
            holder.btnMarkReady.setAlpha(1.0f);

            // Button click — Counter Activity ko batao
            holder.btnMarkReady.setOnClickListener(v -> {
                listener.onMarkReady(orderId, holder.getAdapterPosition());
            });
        }
    }

    // Status text aur color update karta hai
    private void updateStatusBadge(TextView tv, String status) {
        if (status == null) return;
        switch (status) {
            case "Received":
                tv.setText("● Received");
                tv.setTextColor(context.getResources()
                        .getColor(R.color.primary_orange));
                break;
            case "Preparing":
                tv.setText("● Preparing...");
                tv.setTextColor(context.getResources()
                        .getColor(android.R.color.holo_blue_dark));
                break;
            case "Ready":
                tv.setText("✓ Ready!");
                tv.setTextColor(context.getResources()
                        .getColor(R.color.available_green));
                break;
        }
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvToken, tvStudentName, tvOrderDetails, tvStatusBadge;
        Button btnMarkReady;

        ViewHolder(View v) {
            super(v);
            tvToken       = v.findViewById(R.id.tv_token);
            tvStudentName = v.findViewById(R.id.tv_student_name);
            tvOrderDetails = v.findViewById(R.id.tv_order_details);
            tvStatusBadge  = v.findViewById(R.id.tv_order_status_badge);
            btnMarkReady   = v.findViewById(R.id.btn_mark_ready);
        }
    }
}