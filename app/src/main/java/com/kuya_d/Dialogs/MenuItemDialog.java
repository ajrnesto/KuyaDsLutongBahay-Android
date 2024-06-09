package com.kuya_d.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kuya_d.AuthenticationActivity;
import com.kuya_d.R;
import com.kuya_d.Utils.Utils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MenuItemDialog extends AppCompatDialogFragment {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    String id;
    String productDetails;
    String productName;
    String category;
    Long thumbnail;

    RoundedImageView ivProduct;
    TextView tvName, tvCategory, tvDetails;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_menu_item, null);

        initializeFirebase();
        initiate(view);
        loadItem();

        builder.setView(view);
        return builder.create();
    }

    private void initiate(View view) {
        ivProduct = view.findViewById(R.id.ivProduct);
        tvName = view.findViewById(R.id.tvName);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvDetails = view.findViewById(R.id.tvDetails);
    }

    private void loadItem() {
        assert getArguments() != null;
        id = getArguments().getString("id");
        productName = getArguments().getString("productName");
        productDetails = getArguments().getString("productDetails");
        category = getArguments().getString("category");
        thumbnail = getArguments().getLong("thumbnail");

        tvName.setText(productName);
        tvDetails.setText(productDetails);
        DB.collection("categories").document(category)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            tvCategory.setText(task.getResult().getString("categoryName"));
                        }
                    }
                });
        DecimalFormat df = new DecimalFormat("0.00");
        storageRef.child("products/" + thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(500, 0).centerInside().into(ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(ivProduct));
    }
}
