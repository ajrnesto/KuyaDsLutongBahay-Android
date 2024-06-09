package com.kuya_d.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kuya_d.R;
import com.kuya_d.Utils.Utils;

public class BundlesFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    ConstraintLayout clLogo, clBundles, clHeadCount;
    MaterialCardView cvBtn3Dishes, cvBtn4Dishes, cvBtn5Dishes;
    TextInputEditText etHeadcount;
    MaterialButton btnBack, btnMenuList, btnNext;
    TextView tvNumberOfDishes, tvBundleRate;

    View view;
    int bundleSize = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bundles, container, false);

        initializeFirebase();
        initializeViews();
        handleButtonClicks();

        return view;
    }

    private void initializeViews() {
        clLogo = view.findViewById(R.id.clLogo);
        clBundles = view.findViewById(R.id.clBundles);
        clHeadCount = view.findViewById(R.id.clHeadCount);
        cvBtn3Dishes = view.findViewById(R.id.cvBtn3Dishes);
        cvBtn4Dishes = view.findViewById(R.id.cvBtn4Dishes);
        cvBtn5Dishes = view.findViewById(R.id.cvBtn5Dishes);
        etHeadcount = view.findViewById(R.id.etHeadcount);
        btnBack = view.findViewById(R.id.btnBack);
        btnNext = view.findViewById(R.id.btnNext);
        btnMenuList = view.findViewById(R.id.btnMenuList);
        tvNumberOfDishes = view.findViewById(R.id.tvNumberOfDishes);
        tvBundleRate = view.findViewById(R.id.tvBundleRate);

        clLogo.setVisibility(View.GONE);
        clBundles.setVisibility(View.GONE);
        clHeadCount.setVisibility(View.VISIBLE);
    }

    private void handleButtonClicks() {
        cvBtn3Dishes.setOnClickListener(view -> {
            clLogo.setVisibility(View.GONE);
            clBundles.setVisibility(View.GONE);
            clHeadCount.setVisibility(View.VISIBLE);
            bundleSize = 3;
            tvNumberOfDishes.setText("3 Dishes");
            tvBundleRate.setText("₱250.00/head");
        });

        cvBtn4Dishes.setOnClickListener(view -> {
            clLogo.setVisibility(View.GONE);
            clBundles.setVisibility(View.GONE);
            clHeadCount.setVisibility(View.VISIBLE);
            bundleSize = 4;
            tvNumberOfDishes.setText("4 Dishes");
            tvBundleRate.setText("₱350.00/head");
        });

        cvBtn5Dishes.setOnClickListener(view -> {
            clLogo.setVisibility(View.GONE);
            clBundles.setVisibility(View.GONE);
            clHeadCount.setVisibility(View.VISIBLE);
            bundleSize = 5;
            tvNumberOfDishes.setText("5 Dishes");
            tvBundleRate.setText("₱450.00/head");
        });

        btnBack.setOnClickListener(view -> {
//            clLogo.setVisibility(View.VISIBLE);
//            clBundles.setVisibility(View.VISIBLE);
//            clHeadCount.setVisibility(View.GONE);
            requireActivity().onBackPressed();
        });

        etHeadcount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strHeadCount = etHeadcount.getText().toString();
                if (!strHeadCount.isEmpty()) {
                    int headCount = Integer.parseInt(strHeadCount);

                    if (headCount > 2000) {
                        etHeadcount.setText("2000");
                        etHeadcount.setSelection(etHeadcount.getText().toString().length());
                    }
                }
            }
        });

        btnNext.setOnClickListener(view -> {
            if (etHeadcount.getText() == null || etHeadcount.getText().toString().isEmpty()) {
                Utils.basicDialog(requireContext(), "Please specify the number of guests for the event", "Okay");
                return;
            }
            int headCount = Integer.parseInt(etHeadcount.getText().toString());
            if (headCount < 50) {
                Utils.simpleDialog(requireContext(), "Minimum Headcount Required", "Sorry, but we only cater events with a headcount of at least 50 persons.", "Okay");
                return;
            }
            Utils.Cache.setInt(requireContext(), "selected_bundle_size", bundleSize);
            Utils.Cache.setInt(requireContext(), "headcount", headCount);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment shopFragment = new ShopFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, shopFragment, "SHOP_FRAGMENT");
            fragmentTransaction.addToBackStack("SHOP_FRAGMENT");
            fragmentTransaction.commit();
        });

        btnMenuList.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment menuListFragment = new MenuListFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, menuListFragment, "MENU_LIST_FRAGMENT");
            fragmentTransaction.addToBackStack("MENU_LIST_FRAGMENT");
            fragmentTransaction.commit();
        });
    }
}