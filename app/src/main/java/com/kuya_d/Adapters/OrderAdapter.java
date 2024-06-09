package com.kuya_d.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.FirestoreGrpc;
import com.kuya_d.Dialogs.EditEventDateDialog;
import com.kuya_d.Dialogs.EditHeadcountDialog;
import com.kuya_d.Dialogs.EditVenueDialog;
import com.kuya_d.Dialogs.MenuItemDialog;
import com.kuya_d.Fragments.ChatFragment;
import com.kuya_d.Fragments.CheckoutFragment;
import com.kuya_d.Objects.Booking;
import com.kuya_d.Objects.Product;
import com.kuya_d.Objects.ShopItem;
import com.kuya_d.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kuya_d.Utils.Utils;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.orderViewHolder> {

    FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<Booking> arrBooking;
    OrderItemAdapter orderItemsAdapter;
    private OnOrderListener mOnOrderListener;

    public OrderAdapter(Context context, ArrayList<Booking> arrBooking, OnOrderListener onOrderListener) {
        this.context = context;
        this.arrBooking = arrBooking;
        this.mOnOrderListener = onOrderListener;
    }

    @NonNull
    @Override
    public orderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_order, parent, false);
        return new orderViewHolder(view, mOnOrderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull orderViewHolder holder, int position) {
        Booking booking = arrBooking.get(position);

        String id = booking.getId();
        String customerUid = booking.getCustomerUid();
        String firstName = booking.getFirstName();
        String lastName = booking.getLastName();
        String mobile = booking.getMobile();
        long eventDate = booking.getEventDate();
        String purok = booking.getPurok();
        String barangay = booking.getBarangay();
        double total = booking.getTotal();
        String eventType = booking.getEventType();
        int bundleSize = booking.getBundleSize();
        int headcount = booking.getHeadcount();
        String status = booking.getStatus();
        ArrayList<ShopItem> dishes = booking.getDishes();
        long timestamp = booking.getTimestamp();

        // timestamp
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("MM/dd/yyyy, hh:mm aa");
        holder.tvTimestamp.setText(sdfTimestamp.format(timestamp));
        // status
        holder.tvStatus.setText(status);
        // event type
        holder.tvEventType.setText("Event: "+Utils.capitalizeEachWord(eventType));
        // bundle size
        holder.tvBundleSize.setText("Bundle Size: "+bundleSize+" Dishes");
        // headcount
        holder.tvHeadcount.setText("Headcount: "+headcount+" Persons");
        // venue
        holder.tvEventVenue.setText("Venue: "+purok+", "+barangay+", Siaton");
        // event date
        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, yyyy");
        holder.tvEventDate.setText("Event Date: "+sdfDate.format(eventDate));
        // total
        holder.tvTotal.setText("Total: ₱"+String.format("%.2f", total * headcount));

        holder.rvOrderItems.setHasFixedSize(true);
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        orderItemsAdapter = new OrderItemAdapter(holder.itemView.getContext(), dishes);
        holder.rvOrderItems.setAdapter(orderItemsAdapter);

        if (Objects.equals(status, "Pending")) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        }
        else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        if (Objects.equals(status, "Pending") || Objects.equals(status, "Confirmed")) {
            holder.btnChangeVenue.setVisibility(View.VISIBLE);
            holder.btnChangeEventDate.setVisibility(View.VISIBLE);
            holder.btnChangeHeadcount.setVisibility(View.VISIBLE);
        }
        else {
            holder.btnChangeVenue.setVisibility(View.GONE);
            holder.btnChangeEventDate.setVisibility(View.GONE);
            holder.btnChangeHeadcount.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return arrBooking.size();
    }

    public class orderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnOrderListener onOrderListener;
        TextView tvTimestamp, tvStatus, tvEventType, tvBundleSize, tvHeadcount, tvEventVenue, tvEventDate, tvTotal;
        MaterialButton btnChat, btnChangeEventDate, btnChangeHeadcount, btnChangeVenue, btnCancel;
        RecyclerView rvOrderItems;

        public orderViewHolder(@NonNull View itemView, OnOrderListener onOrderListener) {
            super(itemView);

            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvEventType = itemView.findViewById(R.id.tvEventType);
            tvBundleSize = itemView.findViewById(R.id.tvBundleSize);
            tvHeadcount = itemView.findViewById(R.id.tvHeadcount);
            tvEventVenue = itemView.findViewById(R.id.tvEventVenue);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnChangeEventDate = itemView.findViewById(R.id.btnChangeEventDate);
            btnChangeHeadcount = itemView.findViewById(R.id.btnChangeHeadcount);
            btnChangeVenue = itemView.findViewById(R.id.btnChangeVenue);
            btnCancel = itemView.findViewById(R.id.btnCancel);

            this.onOrderListener = onOrderListener;
            itemView.setOnClickListener(this);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    MaterialAlertDialogBuilder dialogCancelBooking = new MaterialAlertDialogBuilder(context);
                    dialogCancelBooking.setTitle("Cancel Booking");
                    dialogCancelBooking.setMessage("Do you want to cancel your booking?");
                    dialogCancelBooking.setPositiveButton("Cancel Booking", (dialogInterface, i) -> {
                        DB.collection("bookings").document(arrBooking.get(getAdapterPosition()).getId())
                                .delete()
                                .addOnCompleteListener(task -> notifyItemRemoved(getAdapterPosition()));
                    });
                    dialogCancelBooking.setNeutralButton("Back", (dialogInterface, i) -> { });
                    dialogCancelBooking.show();
                }
            });

            btnChat.setOnClickListener(view -> {
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment chatFragment = new ChatFragment();
                fragmentTransaction.replace(R.id.fragmentHolder, chatFragment, "CHAT_FRAGMENT");
                fragmentTransaction.addToBackStack("CHAT_FRAGMENT");
                fragmentTransaction.commit();
            });

            btnChangeVenue.setOnClickListener(view -> {
                Bundle args = new Bundle();
                args.putString("id", arrBooking.get(getAdapterPosition()).getId());
                args.putString("eventType", arrBooking.get(getAdapterPosition()).getEventType());
                args.putLong("eventDate", arrBooking.get(getAdapterPosition()).getEventDate());
                args.putString("customerName", arrBooking.get(getAdapterPosition()).getFirstName() + " " + arrBooking.get(getAdapterPosition()).getLastName());
                args.putString("mobile", arrBooking.get(getAdapterPosition()).getMobile());
                args.putString("purok", arrBooking.get(getAdapterPosition()).getPurok());
                args.putString("barangay", arrBooking.get(getAdapterPosition()).getBarangay());
                args.putString("status", arrBooking.get(getAdapterPosition()).getStatus());

                EditVenueDialog editVenueDialog = new EditVenueDialog();
                editVenueDialog.setArguments(args);
                editVenueDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "EDIT_VENUE_DIALOG");
            });

            btnChangeEventDate.setOnClickListener(view -> {
                Bundle args = new Bundle();
                args.putString("id", arrBooking.get(getAdapterPosition()).getId());
                args.putString("eventType", arrBooking.get(getAdapterPosition()).getEventType());
                args.putLong("eventDate", arrBooking.get(getAdapterPosition()).getEventDate());
                args.putString("customerName", arrBooking.get(getAdapterPosition()).getFirstName() + " " + arrBooking.get(getAdapterPosition()).getLastName());
                args.putString("mobile", arrBooking.get(getAdapterPosition()).getMobile());
                args.putString("purok", arrBooking.get(getAdapterPosition()).getPurok());
                args.putString("barangay", arrBooking.get(getAdapterPosition()).getBarangay());
                args.putString("status", arrBooking.get(getAdapterPosition()).getStatus());

                EditEventDateDialog editEventDateDialog = new EditEventDateDialog();
                editEventDateDialog.setArguments(args);
                editEventDateDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "EDIT_EVENT_DATE_DIALOG");
            });

            btnChangeHeadcount.setOnClickListener(view -> {
                Bundle args = new Bundle();
                args.putString("id", arrBooking.get(getAdapterPosition()).getId());
                args.putString("eventType", arrBooking.get(getAdapterPosition()).getEventType());
                args.putLong("eventDate", arrBooking.get(getAdapterPosition()).getEventDate());
                args.putString("customerName", arrBooking.get(getAdapterPosition()).getFirstName() + " " + arrBooking.get(getAdapterPosition()).getLastName());
                args.putString("mobile", arrBooking.get(getAdapterPosition()).getMobile());
                args.putString("purok", arrBooking.get(getAdapterPosition()).getPurok());
                args.putString("barangay", arrBooking.get(getAdapterPosition()).getBarangay());
                args.putString("status", arrBooking.get(getAdapterPosition()).getStatus());
                args.putInt("headcount", arrBooking.get(getAdapterPosition()).getHeadcount());
                args.putDouble("total", arrBooking.get(getAdapterPosition()).getTotal());
                args.putInt("bundleSize", arrBooking.get(getAdapterPosition()).getBundleSize());

                EditHeadcountDialog editHeadcountDialog = new EditHeadcountDialog();
                editHeadcountDialog.setArguments(args);
                editHeadcountDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "EDIT_HEADCOUNT_DIALOG");
            });
        }

        @Override
        public void onClick(View view) {
            onOrderListener.onOrderClick(getAdapterPosition());
        }
    }

    public interface OnOrderListener{
        void onOrderClick(int position);
    }
}
