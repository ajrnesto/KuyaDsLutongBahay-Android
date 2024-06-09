package com.kuya_d.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kuya_d.R;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class EditEventDateDialog extends AppCompatDialogFragment {

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
    TextInputEditText etEventDate;
    MaterialButton btnSave;

    // date picker items
    MaterialDatePicker.Builder<Long> EventDate;
    MaterialDatePicker<Long> dpEventDate;
    long dpEventDateSelection = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_event_date, null);

        initializeFirebase();
        initiate(view);
        loadItem();
        initializeDatePicker();
        handleUserInteraction();

        builder.setView(view);
        return builder.create();
    }

    private void initiate(View view) {
        etEventDate = view.findViewById(R.id.etEventDate);
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
        dpEventDateSelection = eventDate;
        etEventDate.setText(sdfDate.format(dpEventDateSelection));
    }

    private void handleUserInteraction() {
        etEventDate.setOnClickListener(view -> {
            etEventDate.setEnabled(false);
            dpEventDate.show(requireActivity().getSupportFragmentManager(), "EVENT_DATE_PICKER");
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate venue
                String dateStr = etEventDate.getText().toString();

                if (dateStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                MaterialAlertDialogBuilder dialogConfirmation = new MaterialAlertDialogBuilder(requireContext());
                dialogConfirmation.setTitle("Warning: Editing Booking Details");
                dialogConfirmation.setMessage("Changing the event date will revert your booking status to pending. Please proceed only if necessary.");
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
                .update("eventDate", dpEventDateSelection,
                        "status", "Pending")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dismiss();
                    }
                });
    }

    private void initializeDatePicker() {
        EventDate = MaterialDatePicker.Builder.datePicker();
        EventDate.setTitleText("Select Event Date")
                .setSelection(dpEventDateSelection);
        dpEventDate = EventDate.build();
        dpEventDate.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
            dpEventDateSelection = dpEventDate.getSelection();
            etEventDate.setText(sdf.format(dpEventDateSelection).toUpperCase(Locale.ROOT));
            etEventDate.setEnabled(true);
        });
        dpEventDate.addOnNegativeButtonClickListener(view -> {
            etEventDate.setEnabled(true);
        });
        dpEventDate.addOnCancelListener(dialogInterface -> {
            etEventDate.setEnabled(true);
        });
    }
}
