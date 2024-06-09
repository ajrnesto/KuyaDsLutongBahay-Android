package com.kuya_d.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kuya_d.Objects.ShopItem;
import com.kuya_d.R;
import com.kuya_d.Utils.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CheckoutFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    TextInputEditText etFirstName, etLastName, etMobile, etEventDate, etEventTime, etPurok;
    AutoCompleteTextView menuBarangay;
    TextView tvBundleSize, tvSelectedEvent, tvHeadCount, tvTotal;
    MaterialButton btnSubmitBooking;

    // date picker items
    MaterialDatePicker.Builder<Long> EventDate;
    MaterialDatePicker<Long> dpEventDate;
    long dpEventDateSelection = 0;
    String selectedEvent;
    int bundleSize;
    int headcount;
    double total;
    ArrayList<String> arrSelectedItemsId;
    ArrayList<ShopItem> arrSelectedItems;
    // time pickers
    MaterialTimePicker tpEventTime;
    long tpEventTimeSelection = 0;

    // Spinner items
    String[] itemsBarangay;
    ArrayAdapter<String> adapterBarangay;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_checkout, container, false);

        initializeFirebase();
        initializeViews();
        initializeSpinners();
        loadUserInfo();
        initializeDatePicker();
        initializeTimePicker();
        loadBookingDetails();
        handleUserInteraction();

        return view;
    }

    private void initializeTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        tpEventTime = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .setTitleText("Event time")
                //.setTheme(R.style.MotoRenta_TimePicker)
                .build();
        tpEventTime.addOnPositiveButtonClickListener(view -> {
            calendar.set(Calendar.HOUR_OF_DAY, tpEventTime.getHour());
            calendar.set(Calendar.MINUTE, tpEventTime.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            tpEventTimeSelection = calendar.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
            etEventTime.setText(sdf.format(tpEventTimeSelection));
            etEventTime.setEnabled(true);
        });
        tpEventTime.addOnNegativeButtonClickListener(view -> {
            etEventTime.setEnabled(true);
        });
        tpEventTime.addOnCancelListener(dialogInterface -> {
            etEventTime.setEnabled(true);
        });
    }

    private void loadUserInfo() {
        DB.collection("users").document(AUTH.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot user = task.getResult();
                            etFirstName.setText(user.getString("firstName"));
                            etLastName.setText(user.getString("lastName"));
                            etMobile.setText(user.getString("mobile"));
                        }
                    }
                });
    }

    private void initializeViews() {
        btnSubmitBooking = view.findViewById(R.id.btnSubmitBooking);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etMobile = view.findViewById(R.id.etMobile);
        etEventDate = view.findViewById(R.id.etEventDate);
        etEventTime = view.findViewById(R.id.etEventTime);
        etPurok = view.findViewById(R.id.etPurok);
        menuBarangay = view.findViewById(R.id.menuBarangay);
        tvBundleSize = view.findViewById(R.id.tvBundleSize);
        tvSelectedEvent = view.findViewById(R.id.tvSelectedEvent);
        tvHeadCount = view.findViewById(R.id.tvHeadCount);
        tvTotal = view.findViewById(R.id.tvTotal);

        selectedEvent = Utils.Cache.getString(requireContext(), "event");
        bundleSize = Utils.Cache.getInt(requireContext(), "selected_bundle_size");
        headcount = Utils.Cache.getInt(requireContext(), "headcount");
        arrSelectedItems = (ArrayList<ShopItem>) getArguments().getSerializable("selected_items");
        arrSelectedItemsId = getArguments().getStringArrayList("selected_items_id");
        total = getArguments().getDouble("total");
    }private void initializeSpinners() {
        itemsBarangay = new String[]{"ALBIGA", "APOLOY", "BONAWON", "BONBONON", "CABANGAHAN", "CANAWAY", "CASALA-AN", "CATICUGAN", "DATAG", "GILIGA-ON", "INALAD", "MALABUHAN", "MALOH", "MANTIQUIL", "MANTUYOP", "NAPACAO", "POBLACION I", "POBLACION II", "POBLACION III", "POBLACION IV", "SALAG", "SAN JOSE", "SANDULOT", "SI-IT", "SUMALIRING", "TAYAK"};
        adapterBarangay = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsBarangay);
        menuBarangay.setAdapter(adapterBarangay);
    }

    private void loadBookingDetails() {
        tvSelectedEvent.setText("Event: " + Utils.capitalizeEachWord(selectedEvent));
        tvBundleSize.setText("Bundle Size: " + bundleSize + " Dishes");
        tvHeadCount.setText("Headcount: " + headcount + " Persons");
        DecimalFormat df = new DecimalFormat("0.00");
        tvTotal.setText("Total: ₱"+df.format(total * headcount));

    }

    private void handleUserInteraction() {
        etEventTime.setOnClickListener(view -> {
            etEventTime.setEnabled(false);
            tpEventTime.show(getParentFragmentManager(), "EVENT_TIME_PICKER");
        });

        btnSubmitBooking.setOnClickListener(view -> {
            btnSubmitBooking.setEnabled(false);

            String firstName = etFirstName.getText().toString();
            String lastName = etLastName.getText().toString();
            String mobile = etMobile.getText().toString();
            String eventDate = etEventDate.getText().toString();
            String eventTime = etEventTime.getText().toString();
            String purok = etPurok.getText().toString();
            String barangay = menuBarangay.getText().toString();

            if (firstName.isEmpty() ||
                    lastName.isEmpty() ||
                    mobile.isEmpty() ||
                    eventDate.isEmpty() ||
                    eventTime.isEmpty() ||
                    purok.isEmpty() ||
                    barangay.isEmpty())
            {
                Utils.simpleDialog(requireContext(), "Incomplete Form", "Please completely fill up the form to process your catering booking.", "Okay");
                btnSubmitBooking.setEnabled(true);
                return;
            }

            DocumentReference refNewOrder = DB.collection("bookings").document();

            Map<String, Object> booking = new HashMap<>();
            booking.put("id", refNewOrder.getId());
            booking.put("customerUid", AUTH.getCurrentUser().getUid());
            booking.put("firstName", firstName);
            booking.put("lastName", lastName);
            booking.put("mobile", mobile);
            Calendar cEventDate = Calendar.getInstance();
            cEventDate.setTimeInMillis(dpEventDate.getSelection());
            cEventDate.set(Calendar.HOUR_OF_DAY, 0);
            cEventDate.set(Calendar.MINUTE, 0);
            cEventDate.set(Calendar.SECOND, 0);
            cEventDate.set(Calendar.MILLISECOND, 0);
            booking.put("eventDate", cEventDate.getTimeInMillis());
            booking.put("eventTime", tpEventTimeSelection);
            booking.put("purok", purok);
            booking.put("barangay", barangay);
            booking.put("total", total);
            booking.put("eventType", selectedEvent);
            booking.put("bundleSize", bundleSize);
            booking.put("headcount", headcount);
            booking.put("status", "Pending");
            booking.put("dishes", arrSelectedItems);
            booking.put("timestamp", System.currentTimeMillis());

            refNewOrder.set(booking).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment bookingsFragment = new BookingsFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, bookingsFragment, "BOOKINGS_FRAGMENT");
                    fragmentTransaction.addToBackStack("BOOKINGS_FRAGMENT");
                    fragmentTransaction.commit();

                    Toast.makeText(requireContext(), "You have successfully booked for catering!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        etEventDate.setOnClickListener(view -> {
            etEventDate.setEnabled(false);
            dpEventDate.show(requireActivity().getSupportFragmentManager(), "EVENT_DATE_PICKER");
        });
    }

    private void initializeDatePicker() {
        Calendar calNow = Calendar.getInstance();
        calNow.set(Calendar.HOUR_OF_DAY, 0);
        calNow.set(Calendar.MINUTE, 0);
        calNow.set(Calendar.SECOND, 0);
        calNow.set(Calendar.MILLISECOND, 0);

        EventDate = MaterialDatePicker.Builder.datePicker();
        EventDate.setTitleText("Select Event Date")
                .setSelection(System.currentTimeMillis())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.from(calNow.getTimeInMillis())).build());
        dpEventDate = EventDate.build();
        dpEventDate.addOnPositiveButtonClickListener(selection -> {
            // check selected date for multiple bookings
            Calendar cSelectedDate = Calendar.getInstance();
            cSelectedDate.setTimeInMillis(dpEventDate.getSelection());
            cSelectedDate.set(Calendar.HOUR_OF_DAY, 0);
            cSelectedDate.set(Calendar.MINUTE, 0);
            cSelectedDate.set(Calendar.SECOND, 0);
            cSelectedDate.set(Calendar.MILLISECOND, 0);
            DB.collection("bookings").whereEqualTo("eventDate", cSelectedDate.getTimeInMillis())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                etEventDate.setEnabled(true);

                                QuerySnapshot snaps = task.getResult();
                                if (snaps.size() == 2) {
                                    Utils.simpleDialog(requireContext(), "Selected Date is Fully Booked", "Thank you for choosing Kuya D's. We regret to inform you that the selected date is already fully booked. We can only accommodate a maximum of 2 bookings per day to ensure the highest quality service for our clients.", "Go back");
                                    return;
                                }
                                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
                                dpEventDateSelection = dpEventDate.getSelection();
                                etEventDate.setText(sdf.format(dpEventDateSelection).toUpperCase(Locale.ROOT));
                            }
                        }
                    });
        });
        dpEventDate.addOnNegativeButtonClickListener(view -> {
            etEventDate.setEnabled(true);
        });
        dpEventDate.addOnCancelListener(dialogInterface -> {
            etEventDate.setEnabled(true);
        });
    }
}