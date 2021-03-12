package iut.appmob.blah.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import iut.appmob.blah.R;
import iut.appmob.blah.data.User;

public class MainActivity extends AppCompatActivity {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if there is already an user logged in
        // we skip the MainActivity and
        // start HomepageActivity
        if (mAuth.getCurrentUser() != null) {
            this.onStart();
        } else {
            setContentView(R.layout.activity_main);
            Button btnLogin = findViewById(R.id.btnLogin);
            btnLogin.setOnClickListener(this.loginListener());
            Button btnSignup = findViewById(R.id.btnSignup);
            btnSignup.setOnClickListener(this.signupListener());
            ImageButton btnShowPswd = findViewById(R.id.btnShowPswd);
            btnShowPswd.setOnClickListener(this.showPswdListener());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // checks if an user is already signed in and updates the UI accordingly.
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, HomepageActivity.class);
            intent.putExtra("currentUser", new User(mAuth.getCurrentUser().getEmail()));
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // listener for the login button
    private View.OnClickListener loginListener() {
        return v -> {
            EditText editTextEmail = findViewById(R.id.inputEmail);
            EditText editTextPassword = findViewById(R.id.inputPassword);
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (email.length() > 0 && password.length() > 0) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(this, HomepageActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(MainActivity.this, "You did not fill every fields", Toast.LENGTH_LONG).show();
            }
        };
    }

    // listener for the sign up button
    private View.OnClickListener signupListener() {
        return v -> {
            EditText editTextEmail = findViewById(R.id.inputEmail);
            EditText editTextPassword = findViewById(R.id.inputPassword);
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            // at least 8 characters, one number, one uppercase letter,
            // one lowercase letter and no whitespace
            String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

            // if the password meets the requirements
            // we create the user in FirebaseAuth
            // and we add this user in FirebaseDatabase
            if (password.matches(passwordPattern)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                User currentUser = new User(email);
                                database.getReference("users")
                                        .child(mAuth.getCurrentUser().getUid())
                                        .setValue(currentUser)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Intent intent = new Intent(this, HomepageActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(MainActivity.this, task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(MainActivity.this, "The password does not meet the requirement", Toast.LENGTH_LONG).show();
            }
        };
    }

    // listener for the show password button
    private View.OnClickListener showPswdListener() {
        return v -> {
            EditText editTextPassword = findViewById(R.id.inputPassword);
            ImageButton btnShowPswd = findViewById(R.id.btnShowPswd);

            // if password is shown
            if (editTextPassword.getTransformationMethod() == null) {
                editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
                btnShowPswd.setImageResource(R.drawable.eye);
            } else {
                editTextPassword.setTransformationMethod(null);
                btnShowPswd.setImageResource(R.drawable.eye_slash);
            }

            // sets the cursor at the end of the text
            editTextPassword.setSelection(editTextPassword.getText().length());
        };
    }
}
