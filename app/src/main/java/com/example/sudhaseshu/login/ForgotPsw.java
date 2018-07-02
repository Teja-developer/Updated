package com.example.sudhaseshu.login;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPsw extends AppCompatActivity {

    private String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        final EditText email = findViewById(R.id.email_forgot);
        Button forgot = findViewById(R.id.reset_forgot);

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s = email.getText().toString();
                forgot();
            }
        });
    }
    private void forgot() {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        if(!s.isEmpty())
            mAuth.sendPasswordResetEmail(s)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i("Login", "Email sent.");
                                Toast.makeText(getApplicationContext(),"Mail Sent",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }
}
