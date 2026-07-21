package com.example.reminderapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;
import com.example.reminderapp.database.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePicture;
    private static final String PREFS_NAME = "user_prefs";
    private User currentUser;
    private AppDatabase db;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                showImageSourceDialog();
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap photo = (Bitmap) extras.get("data");
                        ivProfilePicture.setImageBitmap(photo);
                        saveBitmapToInternalStorage(photo);
                        Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ivProfilePicture.setImageBitmap(bitmap);
                        saveBitmapToInternalStorage(bitmap);
                        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);

        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvPhone = findViewById(R.id.tvProfilePhone);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        FloatingActionButton btnEditPicture = findViewById(R.id.btnEditPicture);

        // Load active user info
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt("active_user_id", -1);
        
        if (userId != -1) {
            currentUser = db.userDao().getUserById(userId);
        }

        if (currentUser != null) {
            if (tvName != null) tvName.setText(currentUser.getName());
            if (tvEmail != null) tvEmail.setText(currentUser.getEmail());
            if (tvPhone != null) tvPhone.setText(currentUser.getPhone());
            
            String savedImagePath = currentUser.getProfileImagePath();
            if (savedImagePath != null) {
                File imgFile = new File(savedImagePath);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    if (myBitmap != null) {
                        ivProfilePicture.setImageBitmap(myBitmap);
                    }
                }
            }
        } else {
            Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
        }

        // Logout
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear(); // Clear all session data
                editor.putBoolean("is_logged_in", false);
                editor.apply();
                
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }

        if (btnEditPicture != null) {
            btnEditPicture.setOnClickListener(v -> checkPermissionsAndShowDialog());
        }

        loadFinishedSessions(userId, rvHistory);
    }

    private void loadFinishedSessions(int userId, RecyclerView rvHistory) {
        if (userId == -1 || rvHistory == null) return;

        new Thread(() -> {
            List<ReminderEntity> finishedReminders = db.reminderDao().getCompletedRemindersForUser(userId);
            List<String> historyStrings = new ArrayList<>();
            for (ReminderEntity r : finishedReminders) {
                historyStrings.add("✅ Finished: " + r.getTitle() + " (" + r.getDate() + ")");
            }
            
            if (historyStrings.isEmpty()) {
                historyStrings.add("No finished sessions yet.");
            }

            runOnUiThread(() -> {
                rvHistory.setLayoutManager(new LinearLayoutManager(this));
                HistoryAdapter historyAdapter = new HistoryAdapter(historyStrings);
                rvHistory.setAdapter(historyAdapter);
            });
        }).start();
    }

    private void checkPermissionsAndShowDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showImageSourceDialog();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA
            });
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        try {
                            cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                        } catch (Exception e) {
                            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void saveBitmapToInternalStorage(Bitmap bitmap) {
        if (currentUser == null) return;
        try {
            File directory = getDir("profile_images", Context.MODE_PRIVATE);
            File mypath = new File(directory, "profile_" + currentUser.getId() + ".jpg");

            FileOutputStream fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            currentUser.setProfileImagePath(mypath.getAbsolutePath());
            db.userDao().update(currentUser);
            
            // Update session too for consistency if needed, but DB is source of truth now
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<String> historyItems;

        public HistoryAdapter(List<String> historyItems) {
            this.historyItems = historyItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvContent.setText(historyItems.get(position));
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent;
            ViewHolder(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvItemContent);
            }
        }
    }
}