package com.example.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView rvSearchResults;
    private AppDatabase db;
    private int activeUserId;
    private List<ReminderEntity> allReminders = new ArrayList<>();
    private String lastSearchQuery = "";
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Search");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        db = AppDatabase.getInstance(this);
        activeUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("active_user_id", -1);

        EditText etSearchQuery = findViewById(R.id.etSearchQuery);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        Button btnAddFromSearch = findViewById(R.id.btnAddFromSearch);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter();
        rvSearchResults.setAdapter(adapter);

        // Auto-focus search bar and show keyboard
        etSearchQuery.requestFocus();
        etSearchQuery.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearchQuery, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        loadAllReminders();
        performSearch(""); 

        etSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lastSearchQuery = s.toString();
                performSearch(lastSearchQuery);
                
                if (lastSearchQuery.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                    btnAddFromSearch.setVisibility(View.VISIBLE);
                } else {
                    btnAddFromSearch.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAddFromSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddeventActivity.class);
            intent.putExtra("prefill_date", lastSearchQuery);
            startActivity(intent);
        });
    }

    private void loadAllReminders() {
        if (activeUserId != -1) {
            allReminders = db.reminderDao().getPublicRemindersForUser(activeUserId);
        }
    }

    private void performSearch(String query) {
        String lowerQuery = query.toLowerCase();
        List<ReminderEntity> filtered = allReminders.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(lowerQuery) || r.getDate().contains(lowerQuery))
                .collect(Collectors.toList());

        adapter.setReminders(filtered);
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
        private List<ReminderEntity> reminders = new ArrayList<>();

        public void setReminders(List<ReminderEntity> reminders) {
            this.reminders = reminders;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ReminderEntity reminder = reminders.get(position);
            holder.tvContent.setText(reminder.getTitle());
            holder.tvSubtext.setText(reminder.getDate() + " • " + reminder.getTime());
            holder.tvSubtext.setVisibility(View.VISIBLE);
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, ReminderDetailActivity.class);
                intent.putExtra("reminder_id", reminder.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return reminders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent, tvSubtext;
            ViewHolder(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvItemContent);
                tvSubtext = itemView.findViewById(R.id.tvItemSubtext);
            }
        }
    }
}
