package com.kuya_d;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kuya_d.Dialogs.ProfileDialog;
import com.kuya_d.Fragments.BookingsFragment;
import com.kuya_d.Fragments.BundlesFragment;
import com.kuya_d.Fragments.CartFragment;
import com.kuya_d.Fragments.ChatFragment;
import com.kuya_d.Fragments.CheckoutFragment;
import com.kuya_d.Fragments.EventSelectionFragment;
import com.kuya_d.Fragments.ProfileFragment;
import com.kuya_d.Fragments.ShopFragment;
import com.kuya_d.Fragments.ShopItemFragment;
import com.kuya_d.Utils.Utils;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    BottomNavigationView bottom_navbar;
    MaterialButton btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFirebase();
        initializeViews();
        backstackListener();
        handleUserInteraction();
        countCartItems();
    }

    private void handleUserInteraction() {
        btnProfile.setOnClickListener(view -> {
            if (USER != null) {
                ProfileDialog profileDialog = new ProfileDialog();
                profileDialog.show(getSupportFragmentManager(), "PROFILE_DIALOG");
            }
            else {
                startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
            }
        });

        bottom_navbar.setOnItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
            if (item.getItemId() == R.id.miBookings) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "You need to log in to access this feature.");
                    return false;
                }
                if (bottom_navbar.getSelectedItemId() != R.id.miBookings) {
                    Fragment bookingsFragment = new BookingsFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, bookingsFragment, "BOOKINGS_FRAGMENT");
                    fragmentTransaction.addToBackStack("PROFILE_FRAGMENT");
                    fragmentTransaction.commit();
                }
            }
            else if (item.getItemId() == R.id.miHome) {
                Utils.hideKeyboard(this);
                if (bottom_navbar.getSelectedItemId() != R.id.miHome) {
                    Fragment eventSelectionFragment = new EventSelectionFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, eventSelectionFragment, "EVENT_SELECTION_FRAGMENT");
                    fragmentTransaction.addToBackStack("EVENT_SELECTION_FRAGMENT");
                    fragmentTransaction.commit();
                }
            }
            else if (item.getItemId() == R.id.miProfile) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
                    finish();
                    return false;
                }
                if (bottom_navbar.getSelectedItemId() != R.id.miProfile) {
                    Fragment profileFragment = new ProfileFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, profileFragment, "PROFILE_FRAGMENT");
                    fragmentTransaction.addToBackStack("PROFILE_FRAGMENT");
                    fragmentTransaction.commit();
                }
            }
            return true;
        });
    }

    private void countCartItems() {
        Utils.renderCartBadge(bottom_navbar);
    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) { // if navigation is at first backstack entry
            finish();
        } else {
//            BundlesFragment bundlesFragment = (BundlesFragment) getSupportFragmentManager().findFragmentByTag("BUNDLES_FRAGMENT");
//
//            if (bundlesFragment != null && bundlesFragment.isVisible()) {
//                View clBundles = bundlesFragment.getView().findViewById(R.id.clBundles);
//                View clHeadCount = bundlesFragment.getView().findViewById(R.id.clHeadCount);
//
//                if (clHeadCount.getVisibility() == View.VISIBLE) {
//                    clHeadCount.setVisibility(View.GONE);
//                    clBundles.setVisibility(View.VISIBLE);
//                }
//                else {
//                    super.onBackPressed();
//                }
//            }
//            else {
//                super.onBackPressed();
//            }
            super.onBackPressed();
        }
    }

    private void initializeViews() {
        bottom_navbar = findViewById(R.id.bottom_navbar);
        btnProfile = findViewById(R.id.btnProfile);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment eventSelectionFragment = new EventSelectionFragment();
        fragmentTransaction.replace(R.id.fragmentHolder, eventSelectionFragment, "EVENT_SELECTION_FRAGMENT");
        fragmentTransaction.addToBackStack("EVENT_SELECTION_FRAGMENT");
        fragmentTransaction.commit();
        bottom_navbar.getMenu().getItem(1 ).setChecked(true);
    }

    private void backstackListener() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            BookingsFragment bookingsFragment = (BookingsFragment) getSupportFragmentManager().findFragmentByTag("BOOKINGS_FRAGMENT");
            EventSelectionFragment eventSelectionFragment = (EventSelectionFragment) getSupportFragmentManager().findFragmentByTag("EVENT_SELECTION_FRAGMENT");
            BundlesFragment bundlesFragment = (BundlesFragment) getSupportFragmentManager().findFragmentByTag("BUNDLES_FRAGMENT");
            ShopFragment shopFragment = (ShopFragment) getSupportFragmentManager().findFragmentByTag("SHOP_FRAGMENT");
            ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("PROFILE_FRAGMENT");
            ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag("CHAT_FRAGMENT");

            if (bookingsFragment != null && bookingsFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(0).setChecked(true);
            }
            else if (chatFragment != null && bookingsFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(0).setChecked(true);
                softKeyboardListener();
            }
            else if ((eventSelectionFragment != null && eventSelectionFragment.isVisible()) ||
                    (bundlesFragment != null && bundlesFragment.isVisible()) ||
                    (shopFragment != null && shopFragment.isVisible()) ) {
                bottom_navbar.getMenu().getItem(1).setChecked(true);
                softKeyboardListener();
            }
            else if (profileFragment != null && profileFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(2).setChecked(true);
            }
        });
    }

    private void softKeyboardListener() {
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(windowInsets, view);
            if (insetsCompat.isVisible(WindowInsetsCompat.Type.ime())) {
                bottom_navbar.setVisibility(View.GONE);
            }
            else {
                bottom_navbar.setVisibility(View.VISIBLE);
            }
            return windowInsets;
        });
    }
}