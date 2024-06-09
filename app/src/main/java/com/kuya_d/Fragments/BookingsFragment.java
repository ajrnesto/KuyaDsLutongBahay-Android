package com.kuya_d.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kuya_d.Adapters.OrderAdapter;
import com.kuya_d.Objects.Booking;
import com.kuya_d.Objects.Product;
import com.kuya_d.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class BookingsFragment extends Fragment implements OrderAdapter.OnOrderListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;

    TabLayout tabOrders;
    RecyclerView rvOrders;
    TextView tvEmpty;

    ArrayList<Booking> arrBookings;
    ArrayList<Product> arrOrderItems;
    OrderAdapter orderAdapter;
    OrderAdapter.OnOrderListener onOrderListener = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bookings, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(tabOrders.getSelectedTabPosition());

        tabOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadRecyclerView(tabOrders.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    private void loadRecyclerView(int tabIndex) {
        arrBookings = new ArrayList<>();
        arrOrderItems = new ArrayList<>();
        rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setHasFixedSize(true);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        String statusFilter = "";
        if (tabIndex == 0) {
            statusFilter = "Pending";
        }
        else if (tabIndex == 1) {
            statusFilter = "Confirmed";
        }
        else if (tabIndex == 2) {
            statusFilter = "Preparing";
        }
        else if (tabIndex == 3) {
            statusFilter = "In Transit";
        }
        else if (tabIndex == 4) {
            statusFilter = "Completed";
        }

        Query qryMyOrders = DB.collection("bookings")
                        .whereEqualTo("customerUid", Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                        .whereEqualTo("status", statusFilter)
                        .orderBy("timestamp", Query.Direction.DESCENDING);

        qryMyOrders.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        arrBookings.clear();
                        // orderAdapter.notifyDataSetChanged();

                        for (QueryDocumentSnapshot doc : value) {
                            Booking booking = doc.toObject(Booking.class);
                            arrBookings.add(booking);
                            // orderAdapter.notifyItemInserted(arrBookings.indexOf(booking));
                            orderAdapter.notifyDataSetChanged();
                        }

                        if (arrBookings.isEmpty()) {
                            rvOrders.setVisibility(View.INVISIBLE);
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                        else {
                            rvOrders.setVisibility(View.VISIBLE);
                            tvEmpty.setVisibility(View.GONE);
                        }
                    }
                });

        orderAdapter = new OrderAdapter(requireContext(), arrBookings, onOrderListener);
        rvOrders.setAdapter(orderAdapter);
    }

    private void initializeViews() {
        tabOrders = view.findViewById(R.id.tabOrders);
        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    @Override
    public void onOrderClick(int position) {
        
    }
}