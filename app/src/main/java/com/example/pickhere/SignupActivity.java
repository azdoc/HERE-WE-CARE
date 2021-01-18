package com.example.pickhere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {


    private Button mRegistration;
    private ProgressBar progressBar;
    private TextInputEditText nameField, emailField, passwordField;
    private TextInputLayout nameFieldLayout, emailFieldLayout, mobileFieldLayout, passwordFieldLayout;
    private TextView signinText;
    private String name,email,password;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signinText=findViewById(R.id.signintext);
        nameField =findViewById(R.id.input_name);
        nameFieldLayout =findViewById(R.id.nameLayout);
        emailField =findViewById(R.id.input_email);
        emailFieldLayout =findViewById(R.id.emailLayout);
        mobileFieldLayout =findViewById(R.id.mobileLayout);
        passwordField =findViewById(R.id.input_password);
        passwordFieldLayout =findViewById(R.id.passwordLayout);

        mRegistration =(Button) findViewById(R.id.signUpBtn);
        progressBar = findViewById(R.id.progress_circular);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(SignupActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        signinText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


        mRegistration.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validate())
                {
                    progressBar.setVisibility(View.VISIBLE);
                final String email = emailField.getText().toString();
                final String password = passwordField.getText().toString();
                final String name =nameField.getText().toString();

                if (email.length() == 0) {
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SignupActivity.this, "Sign up Error", Toast.LENGTH_SHORT).show();
                        } else {
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
                            Map userInfo = new HashMap();
                            userInfo.put("email", email);
                            userInfo.put("name",name );
                            current_user_db.updateChildren(userInfo);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
        progressBar.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuthListener != null){
            mAuth.removeAuthStateListener(firebaseAuthListener);
            progressBar.setVisibility(View.INVISIBLE);

        }
    }

    //method to validate user input
    private boolean validate() {
        boolean flag = true; //set flag true
        name = nameField.getText().toString();
        email = emailField.getText().toString();
        password = passwordField.getText().toString();

        if(name == null || name.isEmpty()) {
            flag = false;//set the flaf value false
            nameFieldLayout.setError("Please enter your name");
            nameField.requestFocus();
        }
        else if(email == null || email.isEmpty()) {
            flag = false;
            emailFieldLayout.setError("Please enter an Email");
            emailField.requestFocus();
            nameFieldLayout.setError(null);
        }

        else if(password == null || password.isEmpty()) {
            flag = false;
            passwordFieldLayout.setError("Please choose a Password");
            passwordField.requestFocus();
            mobileFieldLayout.setError(null);
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if (password.length() < 6){

            passwordField.setError("Password must be at least 6 characters");
            progressBar.setVisibility(View.INVISIBLE);
        }
        return flag;
    }
}
