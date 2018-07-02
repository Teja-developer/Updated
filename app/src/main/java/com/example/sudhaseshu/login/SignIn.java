package com.example.sudhaseshu.login;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class SignIn extends AppCompatActivity {

    Button signin;
   public FirebaseAuth mAuth;
    private EditText email,password;
    TextView passw,signup , usrnm , emailid;
    private String s1,s2;
    FirebaseAuth.AuthStateListener mAuthListener;
    public ImageView hide;
    ConstraintLayout constraintLayout;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        constraintLayout = findViewById(R.id.loading);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();

        ImageView logo = findViewById(R.id.logo_login2);

        ObjectAnimator animator = ObjectAnimator.ofFloat(logo , "translationY"  ,-300f);
        animator.setDuration(2500);
        animator.start();

        ArrayList<View> viewToFadeIn = new ArrayList<>();

        viewToFadeIn.add(findViewById(R.id.signIn_layout));

        for(View v : viewToFadeIn)
        {
            v.setAlpha(0);
        }


        for(View v: viewToFadeIn)
        {
            v.animate().alpha(1.0f).setDuration(3500).start();
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                    Intent intent = new Intent(SignIn.this,Nav.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                }
            }
        };

        email = findViewById(R.id.email_login2);
        password = findViewById(R.id.pass_login2);

        signin = findViewById(R.id.signIn_login2);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    attemptLogin();
                    constraintLayout.setVisibility(View.VISIBLE);
            }
        });

        passw = findViewById(R.id.forgot_pass);
        passw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this,ForgotPsw.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

            }
        });

        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        hide = findViewById(R.id.hideps);
        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if( (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD )== true)
               // {
                  //  password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                //}
                //else {
                  //  password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                //}
                password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });

        signup = findViewById(R.id.signup_login2);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignUp.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        });

    }

    private void attemptLogin(){
        Log.i("Login","Reached!");
        s1 = email.getText().toString();
        s2 = password.getText().toString();
        Log.i("Login",""+s1+" "+s2);
        loginUserWithEmail();
    }

    private void loginUserWithEmail() {
        mAuth.signInWithEmailAndPassword(s1,s2)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i("Login","signInWithEmail() onComplete: "+ task.isSuccessful());
                        if(!task.isSuccessful()){
                            Log.i("Login","Problem signing in: "+ task.getException());
                            showErrorDialog();
                        }else{
                            Toast.makeText(getApplicationContext(),"Successfully Logged In!",Toast.LENGTH_SHORT).show();
                            Log.i("Login","Successfully Logged In!");
                            Intent intent = new Intent(getApplicationContext(),Nav.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            finish();
                        }
                    }

                }
        );

    }



    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage("Problem signing in")
                .setPositiveButton(android.R.string.ok,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
