package com.example.firebasephoneactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private EditText phone, otp;
    private Button btngenOtp, btnverify;
    private ProgressBar bar;
    private FirebaseAuth mAuth;
    private String verificationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();
        mAuth = FirebaseAuth.getInstance();

        btngenOtp.setOnClickListener(v -> {
            if (TextUtils.isEmpty(phone.getText().toString())) {
                Toast.makeText(MainActivity.this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
            } else {
                String number = phone.getText().toString();
                bar.setVisibility(View.VISIBLE);
                sendVerificationCode(number);
            }
        });

        btnverify.setOnClickListener(v -> {
            String code = otp.getText().toString();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(MainActivity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
            } else {
                verifyCode(code);
            }
        });
    }

    private void initializeUI() {
        phone = findViewById(R.id.phone);
        otp = findViewById(R.id.otp);
        btngenOtp = findViewById(R.id.btngenerateOtp);
        btnverify = findViewById(R.id.btnverifyOtp);
        bar = findViewById(R.id.bar);
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+91" + phoneNumber) // Include country code
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Auto-retrieval or auto-verification scenario
                    final String code = credential.getSmsCode();
                    if (code != null) {
                        otp.setText(code); // Auto-fill the OTP if possible
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(MainActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    bar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    verificationID = s; // Save verification ID and resending token for later
                    Toast.makeText(MainActivity.this, "OTP sent.", Toast.LENGTH_SHORT).show();
                    btnverify.setEnabled(true);
                    bar.setVisibility(View.INVISIBLE);
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, code);
        signInByCredentials(credential);
    }

    private void signInByCredentials(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Verification Failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Automatically redirect to HomeActivity if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }
}
