package com.example.reminderapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.reminderapp.database.Reminder
import com.example.reminderapp.databinding.FragmentAddReminderBinding
import java.util.*

class AddReminderFragment : Fragment() {

    private var _binding: FragmentAddReminderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReminderViewModel by viewModels()
    private val calendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTypeSpinner()
        setupDateTimePickers()

        binding.btnSave.setOnClickListener {
            saveReminder()
        }
    }

    private fun setupTypeSpinner() {
        val types = arrayOf("Normal", "Call Someone")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spType.adapter = adapter

        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.callFields.visibility = if (position == 1) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDateTimePickers() {
        binding.etDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                binding.etDate.setText("$y-${m + 1}-$d")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                calendar.set(Calendar.SECOND, 0)
                binding.etTime.setText(String.format("%02d:%02d", h, min))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun saveReminder() {
        val title = binding.etTitle.text.toString()
        val type = binding.spType.selectedItem.toString()
        val contactName = binding.etContactName.text.toString()
        val phoneNumber = binding.etPhoneNumber.text.toString()
        val date = binding.etDate.text.toString()
        val time = binding.etTime.text.toString()
        val notes = binding.etNotes.text.toString()

        if (title.isBlank() || date.isBlank() || time.isBlank()) {
            Toast.makeText(requireContext(), "Please fill title, date and time", Toast.LENGTH_SHORT).show()
            return
        }

        if (type == "Call Someone" && (contactName.isBlank() || phoneNumber.isBlank())) {
            Toast.makeText(requireContext(), "Please fill contact details", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("active_user_id", -1)

        val reminder = Reminder(
            userId = userId,
            title = title,
            type = type,
            contactName = contactName,
            phoneNumber = phoneNumber,
            date = date,
            time = time,
            dateTime = calendar.timeInMillis,
            notes = notes
        )

        viewModel.insert(reminder) { id ->
            scheduleAlarm(reminder.copy(id = id.toInt()))
            Toast.makeText(requireContext(), "Reminder saved", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun scheduleAlarm(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), CallReminderReceiver::class.java).apply {
            putExtra("id", reminder.id)
            putExtra("name", reminder.contactName)
            putExtra("phone", reminder.phoneNumber)
            putExtra("notes", reminder.notes)
            putExtra("type", reminder.type)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 
            reminder.id, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.dateTime, pendingIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}