package com.kuya_d.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kuya_d.R;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class EditHeadcountDialog extends AppCompatDialogFragment {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    String id, status;
    int bundleSize, headcount;
    double total;
    TextInputEditText etHeadcount;
    MaterialButton btnSave;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_headcount, null);

        initializeFirebase();
        initiate(view);
        loadItem();
        handleUserInteraction();

        builder.setView(view);
        return builder.create();
    }

    private void initiate(View view) {
        etHeadcount = view.findViewById(R.id.etHeadcount);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void loadItem() {
        assert getArguments() != null;
        id = getArguments().getString("id");
        status = getArguments().getString("status");
        headcount = getArguments().getInt("headcount");
        bundleSize = getArguments().getInt("bundleSize");
        total = getArguments().getDouble("double");

        etHeadcount.setText("" + headcount);
    }

    private void handleUserInteraction() {
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

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate venue
                String headcountStr = etHeadcount.getText().toString();
                headcount = Integer.parseInt(headcountStr);

                if (headcountStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                MaterialAlertDialogBuilder dialogConfirmation = new MaterialAlertDialogBuilder(requireContext());
                dialogConfirmation.setTitle("Warning: Editing Booking Details");
                dialogConfirmation.setMessage("Changing the headcount will revert your booking status to pending. Please proceed only if necessary.");
                dialogConfirmation.setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        uploadData();
                    }
                });
                dialogConfirmation.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });

                if (Objects.equals(status, "Confirmed")) {
                    dialogConfirmation.show();
                }
                else if (Objects.equals(status, "Pending")) {
                    uploadData();
                }
            }
        });
    }

    private void uploadData() {
        // update booking field values
        btnSave.setEnabled(false);

        DB.collection("bookings").document(id)
                .update("headcount", headcount,
                        "status", "Pending")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dismiss();
                    }
                });
    }
}
