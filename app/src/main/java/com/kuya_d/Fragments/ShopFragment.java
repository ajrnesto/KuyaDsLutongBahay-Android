package com.kuya_d.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kuya_d.AuthenticationActivity;
import com.kuya_d.Adapters.ShopItemAdapter;
import com.kuya_d.Dialogs.CheckoutDialog;
import com.kuya_d.Dialogs.ProfileDialog;
import com.kuya_d.Dialogs.ShopItemDialog;
import com.kuya_d.Objects.ShopItem;
import com.kuya_d.R;
import com.kuya_d.Utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ShopFragment extends Fragment implements ShopItemAdapter.OnShopItemListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryShop;
    ListenerRegistration velShop;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    ConstraintLayout clMenuItems;
    TextInputLayout tilSearch;
    TextInputEditText etSearch;
    TextView tvTotal;
    RecyclerView rvShop;
    MaterialButton btnCheckout;
    AutoCompleteTextView menuCategories;
    ArrayList<String> itemsCategories;
    ArrayList<String> itemsCategoriesId;
    ArrayAdapter<String> adapterCategories;

    ArrayList<ShopItem> arrShopItem;
    ShopItemAdapter shopItemAdapter;
    ShopItemAdapter.OnShopItemListener onShopItemListener = this;
    String categorySelectedId = "-1";

    View view;
    ArrayList<String> arrSelectedItemsId;
    ArrayList<ShopItem> arrSelectedItems;
    int selectedBundleSize = 0;
    int selectedDishesCount = 0;
    double total = 0;
    int headcount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_shop, container, false);

        initializeFirebase();
        initializeViews();
        initializeSpinners();
        handleUserInteraction();
        loadRecyclerView(null, categorySelectedId);

        return view;
    }

    private void initializeViews() {
        clMenuItems = view.findViewById(R.id.clMenuItems);
        tilSearch = view.findViewById(R.id.tilSearch);
        etSearch = view.findViewById(R.id.etSearch);
        rvShop = view.findViewById(R.id.rvShop);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        menuCategories = view.findViewById(R.id.menuCategories);
        tvTotal = view.findViewById(R.id.tvTotal);

        arrSelectedItemsId = new ArrayList<>();
        arrSelectedItems = new ArrayList<>();
        selectedBundleSize = Utils.Cache.getInt(requireContext(), "selected_bundle_size");
        selectedDishesCount = arrSelectedItemsId.size();
        btnCheckout.setText("Checkout (0 Items)");
        Utils.Cache.setBoolean(requireContext(), "shop_item_dialog_is_visible", false);
        headcount = Utils.Cache.getInt(requireContext(), "headcount");
    }

    private void initializeSpinners() {
        // civil status
        itemsCategories = new ArrayList<>();
        itemsCategoriesId = new ArrayList<>();

        itemsCategories.add("All");
        itemsCategoriesId.add("All");

        itemsCategories.add("Uncategorized");
        itemsCategoriesId.add("-1");
        DB.collection("categories").whereNotEqualTo("categoryName", "Uncategorized").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (QueryDocumentSnapshot snapshot : snapshots) {
                    String id = snapshot.getId();
                    String categoryName = snapshot.get("categoryName").toString();

                    itemsCategoriesId.add(id);
                    itemsCategories.add(categoryName);
                }

                adapterCategories = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsCategories);
                menuCategories.setAdapter(adapterCategories);

                menuCategories.setOnItemClickListener((adapterView, view, position, id) -> {
                    if (position == 0) {
                        categorySelectedId = "-1";
                        loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
                    }
                    else {
                        categorySelectedId = itemsCategoriesId.get(position);
                        loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
                    }
                });
            }
        });
    }

    private void loadRecyclerView(String searchKey, String categoryId) {
        arrShopItem = new ArrayList<>();
        rvShop = view.findViewById(R.id.rvShop);
        rvShop.setHasFixedSize(true);
        /*GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);*/
        rvShop.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (categoryId == "-1") {
            if (searchKey == null || searchKey.isEmpty()) {
                qryShop = DB.collection("products")
                        .orderBy("productName", Query.Direction.ASCENDING);
            }
            else {
                qryShop = DB.collection("products")
                        .orderBy("productNameAllCaps")
                        .startAt(searchKey)
                        .endAt(searchKey+'\uf8ff');
            }
        }
        else {
            if (searchKey == null || searchKey.isEmpty()) {
                qryShop = DB.collection("products")
                        .whereEqualTo("categoryId", categoryId)
                        .orderBy("productName", Query.Direction.ASCENDING);;
            }
            else {
                qryShop = DB.collection("products")
                        .orderBy("productNameAllCaps")
                        .startAt(searchKey)
                        .endAt(searchKey+'\uf8ff')
                        .whereEqualTo("categoryId", categoryId);
            }
        }

        qryShop.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("DEBUG", "Listen failed.", error);
                    return;
                }

                arrShopItem.clear();

                for (QueryDocumentSnapshot doc : value) {
                    ShopItem shopItem = doc.toObject(ShopItem.class);
                    arrShopItem.add(shopItem);
                    shopItemAdapter.notifyItemInserted(arrShopItem.indexOf(shopItem));
                }
            }
        });

        shopItemAdapter = new ShopItemAdapter(requireContext(), arrShopItem, arrSelectedItemsId, onShopItemListener);
        rvShop.setAdapter(shopItemAdapter);
    }

    private void handleUserInteraction() {
        BottomNavigationView bottom_navbar = requireActivity().findViewById(R.id.bottom_navbar);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        CartFragment cartFragment = (CartFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("CART_FRAGMENT");

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder dialogSiatonLocationConfirmation = new MaterialAlertDialogBuilder(requireContext());
                dialogSiatonLocationConfirmation.setMessage("Please be informed that our services are available in Siaton Municipality only.");
                dialogSiatonLocationConfirmation.setPositiveButton("I Understand", (dialogInterface, i) -> {
                    Fragment checkoutFragment = new CheckoutFragment();
                    Bundle args = new Bundle();
                    args.putStringArrayList("selected_items_id", arrSelectedItemsId);
                    args.putSerializable("selected_items", arrSelectedItems);
                    args.putDouble("total", total);

                    checkoutFragment.setArguments(args);
                    fragmentTransaction.replace(R.id.fragmentHolder, checkoutFragment, "CHECKOUT_FRAGMENT");
                    fragmentTransaction.addToBackStack("CHECKOUT_FRAGMENT");
                    fragmentTransaction.commit();
                });
                dialogSiatonLocationConfirmation.setNeutralButton("Back", (dialogInterface, i) -> { });
                dialogSiatonLocationConfirmation.show();
            }
        });

        tilSearch.setEndIconOnClickListener(view -> {
            Utils.hideKeyboard(requireActivity());
            loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(requireActivity());
                loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
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
                    loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onShopItemClick(int position) {
        // if (!arrSelectedItemsId.contains(arrShopItem.get(position).getId()) && selectedDishesCount < selectedBundleSize) {
        if (!arrSelectedItemsId.contains(arrShopItem.get(position).getId())) {
            arrSelectedItemsId.add(arrShopItem.get(position).getId());
            arrSelectedItems.add(arrShopItem.get(position));
            total += arrShopItem.get(position).getPrice();
        }
        else {
            arrSelectedItemsId.remove(arrShopItem.get(position).getId());
            arrSelectedItems.remove(arrShopItem.get(position));
            total -= arrShopItem.get(position).getPrice();
        }
        selectedDishesCount = arrSelectedItemsId.size();

        DecimalFormat df = new DecimalFormat("0.00");
        tvTotal.setText("Total: ₱"+df.format(total * headcount));
        btnCheckout.setText("Checkout ("+selectedDishesCount+" Items)");
        btnCheckout.setEnabled(selectedDishesCount > 0);

        shopItemAdapter.notifyItemChanged(position);
        /*if (!Utils.Cache.getBoolean(requireContext(), "shop_item_dialog_is_visible")) {
            ShopItemDialog shopItemDialog = new ShopItemDialog();

            ShopItem shopItem = arrShopItem.get(position);
            Bundle args = new Bundle();
            args.putString("id", shopItem.getId());
            args.putDouble("price", shopItem.getPrice());
            args.putString("productDetails", shopItem.getProductDetails());
            args.putString("productName", shopItem.getProductName());
            args.putInt("stock", shopItem.getStock());
            if (shopItem.getThumbnail() != null) {
                args.putLong("thumbnail", shopItem.getThumbnail());
            }
            *//*shopItemDialog.setArguments(args);
            shopItemDialog.show(requireActivity().getSupportFragmentManager(), "SHOP_ITEM_DIALOG");

            Utils.Cache.setBoolean(requireContext(), "shop_item_dialog_is_visible", true);*//*

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment shopItemFragment = new ShopItemFragment();
            shopItemFragment.setArguments(args);
            fragmentTransaction.replace(R.id.fragmentHolder, shopItemFragment, "SHOP_ITEM_FRAGMENT");
            fragmentTransaction.addToBackStack("SHOP_ITEM_FRAGMENT");
            fragmentTransaction.commit();
        }*/
    }
}