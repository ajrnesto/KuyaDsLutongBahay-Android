package com.kuya_d.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kuya_d.R;
import com.kuya_d.Utils.Utils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class EditVenueDialog extends AppCompatDialogFragment {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    String id, purok, barangay, status;
    long eventDate;
    TextInputEditText etPurok, etBarangay;
    MaterialButton btnSave;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_venue, null);

        initializeFirebase();
        initiate(view);
        loadItem();
        handleUserInteraction();

        builder.setView(view);
        return builder.create();
    }

    private void initiate(View view) {
        etPurok = view.findViewById(R.id.etPurok);
        etBarangay = view.findViewById(R.id.etBarangay);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void loadItem() {
        assert getArguments() != null;
        id = getArguments().getString("id");
        eventDate = getArguments().getLong("eventDate");
        purok = getArguments().getString("purok");
        barangay = getArguments().getString("barangay");
        status = getArguments().getString("status");

        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, yyyy");
        etPurok.setText(purok);
        etBarangay.setText(barangay);
    }

    private void handleUserInteraction() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate venue
                String newPurok = etPurok.getText().toString();
                String newBarangay = etBarangay.getText().toString();

                if (newPurok.isEmpty() || newBarangay.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                MaterialAlertDialogBuilder dialogConfirmation = new MaterialAlertDialogBuilder(requireContext());
                dialogConfirmation.setTitle("Warning: Editing Booking Details");
                dialogConfirmation.setMessage("Changing the event venue will revert your booking status to pending. Please proceed only if necessary.");
                dialogConfirmation.setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        uploadData(newPurok, newBarangay);
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
                    uploadData(newPurok, newBarangay);
                }
            }
        });
    }

    private void uploadData(String newPurok, String newBarangay) {
        // update booking field values
        btnSave.setEnabled(false);

        DB.collection("bookings").document(id)
                .update("purok", newPurok,
                        "barangay", newBarangay,
                        "status", "Pending")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dismiss();
                    }
                });
    }
}
