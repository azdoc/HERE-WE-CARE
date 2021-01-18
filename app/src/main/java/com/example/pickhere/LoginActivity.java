package com.example.pickhere;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity  {

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 2;
    TextView text,signupText;
    //Button btn_logout;
    SignInButton btn_login;
    ProgressBar progressBar;

    private EditText mEmail, mPassword;
    private Button mLogin;
    private TextView mForgotpassword;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLogin= findViewById(R.id.signinBtn);
        btn_login=(SignInButton)findViewById(R.id.login);
        btn_login.setSize(SignInButton.SIZE_STANDARD);
        //btn_logout=findViewById(R.id.logout);
        mForgotpassword=findViewById(R.id.mforgotpassword);
        mEmail=findViewById(R.id.input_email);
        mPassword=findViewById(R.id.input_password);
        signupText=findViewById(R.id.signupText);
        progressBar = findViewById(R.id.progress_circular);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth= FirebaseAuth.getInstance();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();



        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btn_login.setOnClickListener(v->signIn());
        //  btn_logout.setOnClickListener(v->Logout());

        if(mAuth.getCurrentUser()!=null)
        {
            FirebaseUser user=mAuth.getCurrentUser();
            updateUI(user);
        }
        btn_login.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.login:
                    signIn();
                    break;
                // ...
            }
        });

        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);;
                startActivity(intent);
                finish();
                return;
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                if (email.length() == 0){
                    progressBar.setVisibility(View.INVISIBLE);
                    mEmail.setError("Enter an email address");
                    return;
                }
                if (password.length() < 6){
                    progressBar.setVisibility(View.INVISIBLE);
                    mPassword.setError("Password must be at least 6 characters");
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "Sign up Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        mForgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotpasswordActivity.class));
            }
        });
    }

    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        Log.d("TAG","firebaseAuthWithGoogle"+account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,task ->{
            if(task.isSuccessful()){
                progressBar.setVisibility(View.INVISIBLE);
                Log.d("TAG","signin success");
                FirebaseUser user=mAuth.getCurrentUser();

                if(user!=null) {
                    String user_id = mAuth.getCurrentUser().getUid();
                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
                    Map userInfo = new HashMap();
                    userInfo.put("name",account.getDisplayName());
                    userInfo.put("email",account.getEmail());
                    current_user_db.updateChildren(userInfo);
                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
            else
            {
                progressBar.setVisibility(View.INVISIBLE);
                Log.w("TAG","signin failure",task.getException());

                Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if(user !=null)
        {
            String name=user.getDisplayName();
            String email= user.getEmail();
            //String photo=String.valueOf(user.getPhotoUrl());

  /*  text.append("Info : \n");
    text.append(name + "\n");
    text.append(email);*/

            // Picasso.get().load(photo).into(image);
            btn_login.setVisibility(View.VISIBLE);
            // btn_logout.setVisibility(View.VISIBLE);
        }
        else
        {
            // text.setText(getString(R.string.firebase_login));
            btn_login.setVisibility(View.VISIBLE);
            // btn_logout.setVisibility(View.VISIBLE);
        }
    }

    void Logout()
    {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> updateUI(null));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser()!=null && firebaseAuthListener!=null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            mAuth.addAuthStateListener(firebaseAuthListener);
            progressBar.setVisibility(View.INVISIBLE);
            updateUI(currentUser);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuthListener != null){
            mAuth.removeAuthStateListener(firebaseAuthListener);
            progressBar.setVisibility(View.INVISIBLE);

        }
    }
}


