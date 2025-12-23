package it.unimib.CasHub.ui.login.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.validator.routines.EmailValidator;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.unimib.CasHub.R;
import it.unimib.CasHub.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference realtimeDb;
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        // URL esplicito del tuo Realtime Database
        realtimeDb = FirebaseDatabase.getInstance(Constants.REALTIME_DB_URL).getReference("users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        TextInputEditText inputEmail = view.findViewById(R.id.textInputEmail);
        TextInputEditText inputPassword = view.findViewById(R.id.textInputPassword);
        Button loginButton = view.findViewById(R.id.login_button);
        Button goToRegistrationButton = view.findViewById(R.id.goToRegistration);

        // Listener per login
        loginButton.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (!isEmailOk(email)) {
                inputEmail.setError(getString(R.string.check_email));
                return;
            }
            if (!isPasswordOk(password)) {
                inputPassword.setError(getString(R.string.check_password));
                return;
            }

            // Firebase Authentication login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Login riuscito, recupera UID
                            String uid = mAuth.getCurrentUser().getUid();

                            // Recupera dati extra da Realtime Database
                            realtimeDb.child(uid).get().addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    String name = snapshot.child("name").getValue(String.class);
                                    // Puoi usare altri campi se li hai
                                    Toast.makeText(getContext(),
                                            "Benvenuto " + name,
                                            Toast.LENGTH_SHORT).show();

                                    // Navigazione verso MainActivity
                                    Navigation.findNavController(view)
                                            .navigate(R.id.action_loginFragment_to_mainActivity);

                                } else {
                                    Toast.makeText(getContext(),
                                            "Dati utente non trovati nel database",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(),
                                        "Errore nel recupero dei dati: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });

                        } else {
                            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                    .setTitle("Error")
                                    .setMessage("Failed login: check your login credentials")
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    });
        });



        goToRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registrationFragment);
            }
        });
    }

    boolean isEmailOk(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    boolean isPasswordOk(String password) {
        return password.length() > 7;
    }
}