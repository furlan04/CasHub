package it.unimib.CasHub.ui.login.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.HashMap;
import java.util.Map;

import it.unimib.CasHub.R;
import it.unimib.CasHub.utils.Constants;

public class RegistrationFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference realtimeDb;

    public RegistrationFragment(){

    }

    public static RegistrationFragment newInstance(String param1, String param2) {
        RegistrationFragment fragment = new RegistrationFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inizializza le view
        TextInputEditText inputName = view.findViewById(R.id.textInputName);
        TextInputEditText inputEmail = view.findViewById(R.id.textInputEmail);
        TextInputEditText inputPassword = view.findViewById(R.id.textInputPassword);
        Button registerButton = view.findViewById(R.id.register_button);
        Button goToLoginButton = view.findViewById(R.id.goToLogin);
        Button registerGoogleButton = view.findViewById(R.id.register_google_button);

        mAuth = FirebaseAuth.getInstance();
        realtimeDb = FirebaseDatabase.getInstance(Constants.REALTIME_DB_URL).getReference("users");

        // Listener per il bottone Registrati
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString();
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                // Validazione
                if (name.isEmpty()) {
                    inputName.setError("Inserisci un nome");
                    return;
                }

                if (!isEmailOk(email)) {
                    inputEmail.setError(getString(R.string.check_email));
                    return;
                }

                if (!isPasswordOk(password)) {
                    inputPassword.setError(getString(R.string.check_password));
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                // UID utente appena creato
                                String uid = mAuth.getCurrentUser().getUid();

                                // Dati utente da salvare su Firestore
                                Map<String, Object> user = new HashMap<>();
                                user.put("name", name);
                                user.put("email", email);
                                user.put("createdAt", System.currentTimeMillis());

                                // SALVATAGGIO SU REALTIME DATABASE
                                realtimeDb.child(uid).setValue(user)
                                        .addOnSuccessListener(aVoid -> {
                                            // NAVIGAZIONE SICURA
                                            NavHostFragment.findNavController(RegistrationFragment.this)
                                                    .navigate(R.id.action_registrationFragment_to_mainActivity);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(),
                                                    "Errore salvataggio Realtime DB: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        });

                            } else {
                                Toast.makeText(
                                        getContext(),
                                        "Registrazione fallita: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
            }
        });

        // Listener per tornare al login
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_registrationFragment_to_loginFragment);
            }
        });

        // Gestisci il bottone Google se presente
        if (registerGoogleButton != null) {
            registerGoogleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Implementa registrazione con Google
                }
            });
        }
    }

    // Funzioni di validazione
    boolean isEmailOk(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    boolean isPasswordOk(String password) {
        return password.length() > 7;
    }
}