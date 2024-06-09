package com.kuya_d.Fragments;

import android.os.Bundle;
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

public class EventSelectionFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    MaterialCardView cvWedding, cvAnniversary, cvFiesta, cvBirthday;
    MaterialButton btnMenuList, btnNext;
    TextInputEditText etCustomEvent;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_event_selection, container, false);

        initializeFirebase();
        initializeViews();
        handleButtonClicks();

        return view;
    }

    private void initializeViews() {
        cvWedding = view.findViewById(R.id.cvWedding);
        cvAnniversary = view.findViewById(R.id.cvAnniversary);
        cvFiesta = view.findViewById(R.id.cvFiesta);
        cvBirthday = view.findViewById(R.id.cvBirthday);
        btnMenuList = view.findViewById(R.id.btnMenuList);
        btnNext = view.findViewById(R.id.btnNext);
        etCustomEvent = view.findViewById(R.id.etCustomEvent);
    }

    private void handleButtonClicks() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etCustomEvent.getText().toString().isEmpty()) {
                    Utils.basicDialog(requireContext(), "Please specify an event", "Okay");
                    return;
                }
                Utils.Cache.setString(requireContext(), "event", etCustomEvent.getText().toString());
                navigateToBundleSelectionFragment();
            }
        });

        cvWedding.setOnClickListener(view -> {
            Utils.Cache.setString(requireContext(), "event", "wedding");
            navigateToBundleSelectionFragment();
        });

        cvFiesta.setOnClickListener(view -> {
            Utils.Cache.setString(requireContext(), "event", "fiesta");
            navigateToBundleSelectionFragment();
        });

        cvBirthday.setOnClickListener(view -> {
            Utils.Cache.setString(requireContext(), "event", "birthday");
            navigateToBundleSelectionFragment();
        });

        cvAnniversary.setOnClickListener(view -> {
            Utils.Cache.setString(requireContext(), "event", "anniversary");
            navigateToBundleSelectionFragment();
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

    private void navigateToBundleSelectionFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment bundlesFragment = new BundlesFragment();
        fragmentTransaction.replace(R.id.fragmentHolder, bundlesFragment, "BUNDLES_FRAGMENT");
        fragmentTransaction.addToBackStack("BUNDLES_FRAGMENT");
        fragmentTransaction.commit();
    }
}