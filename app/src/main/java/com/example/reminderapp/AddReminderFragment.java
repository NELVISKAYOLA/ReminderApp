package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.reminderapp.database.ReminderEntity;
import com.example.reminderapp.databinding.FragmentAddReminderBinding;

import java.util.Calendar;
import java.util.Locale;

public class AddReminderFragment extends Fragment {

    private FragmentAddReminderBinding binding;
    private ReminderViewModel viewModel;
    private final Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddReminderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReminderViewModel.class);

        setupTypeSpinner();
        setupDateTimePickers();

        binding.btnSave.setOnClickListener(v -> saveReminder());
    }

    private void setupTypeSpinner() {
        String[] types = {"Normal", "Call Someone"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spType.setAdapter(adapter);

        binding.spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.callFields.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDateTimePickers() {
        binding.etDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, y, m, d) -> {
                calendar.set(Calendar.YEAR, y);
                calendar.set(Calendar.MONTH, m);
                calendar.set(Calendar.DAY_OF_MONTH, d);
                binding.etDate.setText(String.format(Locale.getDefault(), "%d-%d-%d", y, m + 1, d));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        binding.etTime.setOnClickListener(v -> {
            new TimePickerDialog(requireContext(), (view, h, min) -> {
                calendar.set(Calendar.HOUR_OF_DAY, h);
                calendar.set(Calendar.MINUTE, min);
                calendar.set(Calendar.SECOND, 0);
                binding.etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, min));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });
    }

    private void saveReminder() {
        String title = binding.etTitle.getText().toString();
        String type = binding.spType.getSelectedItem().toString();
        String contactName = binding.etContactName.getText().toString();
        String phoneNumber = binding.etPhoneNumber.getText().toString();
        String date = binding.etDate.getText().toString();
        String time = binding.etTime.getText().toString();
        String notes = binding.etNotes.getText().toString();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill title, date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type.equals("Call Someone") && (contactName.isEmpty() || phoneNumber.isEmpty())) {
            Toast.makeText(requireContext(), "Please fill contact details", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("active_user_id", -1);

        ReminderEntity reminder = new ReminderEntity(
            userId, title, notes, date, time, "", "None", "Personal", false, type, contactName, phoneNumber, calendar.getTimeInMillis(), false
        );

        viewModel.insert(reminder, id -> {
            reminder.setId((int) id);
            scheduleAlarm(reminder);
            Toast.makeText(requireContext(), "Reminder saved", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });
    }

    private void scheduleAlarm(ReminderEntity reminder) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), CallReminderReceiver.class);
        intent.putExtra("id", reminder.getId());
        intent.putExtra("name", reminder.getContactName());
        intent.putExtra("phone", reminder.getPhoneNumber());
        intent.putExtra("notes", reminder.getNotes());
        intent.putExtra("type", reminder.getType());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 
            reminder.getId(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.getDateTime(), pendingIntent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
