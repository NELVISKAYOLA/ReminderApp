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
import android.view.Menu;
import android.view.MenuItem;
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

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

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
        ListView lvHistory = findViewById(R.id.lvHistory);
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
        TextView tvLogout = findViewById(R.id.tvLogout);
        if (tvLogout != null) {
            tvLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear(); // Clear all session data
                editor.putBoolean("is_logged_in", false);
                editor.apply();
                
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }

        // History Setup (Simulation)
        ArrayList<String> history = new ArrayList<>();
        history.add("✅ Completed: Gym session (Yesterday)");
        history.add("✅ Completed: Buy groceries (2 days ago)");
        history.add("❌ Missed: Drink water (3 days ago)");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_card, R.id.tvItemContent, history);
        if (lvHistory != null) {
            lvHistory.setAdapter(adapter);
        }

        if (btnEditPicture != null) {
            btnEditPicture.setOnClickListener(v -> checkPermissionsAndShowDialog());
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (NavigationHelper.handleOptionsMenu(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}