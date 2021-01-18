package com.example.pickhere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotpasswordActivity extends AppCompatActivity {

    private EditText mResetemail;
    private Button mresetpassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        mResetemail = (EditText) findViewById(R.id.recovery_email);
        mresetpassword = (Button) findViewById(R.id.forgetpass);
        mAuth = FirebaseAuth.getInstance();

        mresetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mResetemail.getText().toString().trim();
                String password = mresetpassword.getText().toString().trim();

                if (email.length() == 0){
                    mResetemail.setError("Enter registered email address");
                }
                else{
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ForgotpasswordActivity.this, "Reset password mail sent", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(ForgotpasswordActivity.this, MapActivity.class));
                            }
                            else{
                                Toast.makeText(ForgotpasswordActivity.this, "Not a Registered User", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }
}
