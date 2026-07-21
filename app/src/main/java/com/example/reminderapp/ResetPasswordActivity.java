package com.example.reminderapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.User;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etResetEmail, etResetPhone, etNewPassword, etConfirmNewPassword;
    private View newPasswordLayout;
    private Button btnResetAction;
    private AppDatabase db;
    private User targetUser;
    private boolean isVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        db = AppDatabase.getInstance(this);

        etResetEmail = findViewById(R.id.etResetEmail);
        etResetPhone = findViewById(R.id.etResetPhone);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        btnResetAction = findViewById(R.id.btnResetAction);

        btnResetAction.setOnClickListener(v -> {
            if (!isVerified) {
                verifyUser();
            } else {
                updatePassword();
            }
        });
    }

    private void verifyUser() {
        String email = etResetEmail.getText().toString().trim();
        String phone = etResetPhone.getText().toString().trim();

        if (email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        targetUser = db.userDao().getUserByEmail(email);

        if (targetUser != null && targetUser.getPhone().equals(phone)) {
            isVerified = true;
            newPasswordLayout.setVisibility(View.VISIBLE);
            btnResetAction.setText(R.string.btn_reset_password);
            findViewById(R.id.resetEmailBox).setEnabled(false);
            findViewById(R.id.resetPhoneBox).setEnabled(false);
            Toast.makeText(this, R.string.msg_identity_verified, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid information provided!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmNewPassword.getText().toString().trim();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please enter new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        targetUser.setPassword(newPass);
        db.userDao().update(targetUser);

        Toast.makeText(this, R.string.msg_reset_success, Toast.LENGTH_LONG).show();
        finish();
    }
}
