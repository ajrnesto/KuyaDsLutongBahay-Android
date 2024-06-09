package com.kuya_d.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kuya_d.Adapters.MenuItemAdapter;
import com.kuya_d.Objects.ShopItem;
import com.kuya_d.R;
import com.kuya_d.Utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class MenuListFragment extends Fragment implements MenuItemAdapter.OnMenuItemClicked {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryShop;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    RecyclerView rvShop;
    MaterialButton btnBack;
    TextInputLayout tilSearch;
    TextInputEditText etSearch;

    ArrayList<ShopItem> arrShopItem;
    MenuItemAdapter menuItemAdapter;
    MenuItemAdapter.OnMenuItemClicked onMenuItemClicked = this;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_menu_list, container, false);

        initializeFirebase();
        initializeViews();
        handleButtonClicks();
        loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase());

        return view;
    }

    private void initializeViews() {
        rvShop = view.findViewById(R.id.rvShop);
        btnBack = view.findViewById(R.id.btnBack);
        tilSearch = view.findViewById(R.id.tilSearch);
        etSearch = view.findViewById(R.id.etSearch);
    }

    private void loadRecyclerView(String searchKey) {
        arrShopItem = new ArrayList<>();
        rvShop = view.findViewById(R.id.rvShop);
        rvShop.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvShop.setLayoutManager(linearLayoutManager);

        /*GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvShop.setLayoutManager(gridLayoutManager);*/
        
        qryShop = DB.collection("products")
                .orderBy("productNameAllCaps")
                .startAt(searchKey)
                .endAt(searchKey+'\uf8ff');

        qryShop.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d("DEBUG", "Listen failed.", error);
                return;
            }

            arrShopItem.clear();

            for (QueryDocumentSnapshot doc : value) {
                arrShopItem.add(doc.toObject(ShopItem.class));
                menuItemAdapter.notifyDataSetChanged();
            }
        });

        menuItemAdapter = new MenuItemAdapter(requireContext(), arrShopItem, onMenuItemClicked);
        rvShop.setAdapter(menuItemAdapter);
        menuItemAdapter.notifyDataSetChanged();
    }

    private void handleButtonClicks() {
        btnBack.setOnClickListener(view -> requireActivity().onBackPressed());

        tilSearch.setEndIconOnClickListener(view -> {
            Utils.hideKeyboard(requireActivity());
            loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase());
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(requireActivity());
                loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase());
                return true;
            }
            return false;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onMenuItemClicked(int position) {
    }
}