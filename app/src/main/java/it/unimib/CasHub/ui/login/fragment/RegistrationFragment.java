package it.unimib.CasHub.ui.login.fragment;

import static it.unimib.CasHub.utils.Constants.USER_COLLISION_ERROR;
import static it.unimib.CasHub.utils.Constants.WEAK_PASSWORD_ERROR;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.HashMap;
import java.util.Map;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.User;
import it.unimib.CasHub.repository.user.IUserRepository;
import it.unimib.CasHub.ui.login.viewmodel.UserViewModel;
import it.unimib.CasHub.ui.login.viewmodel.UserViewModelFactory;
import it.unimib.CasHub.utils.Constants;
import it.unimib.CasHub.utils.ServiceLocator;

public class RegistrationFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference realtimeDb;
    private UserViewModel userViewModel;
    private TextInputEditText textInputEmail, textInputPassword, textInputName;

    public RegistrationFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IUserRepository userRepository = ServiceLocator.getInstance().getUserRepository(requireActivity().getApplication());

        userViewModel = new ViewModelProvider(requireActivity(), new UserViewModelFactory(userRepository)).get(UserViewModel.class);
        userViewModel.setAuthenticationError(false);
    }

    /*
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
    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        textInputName = view.findViewById(R.id.textInputName);
        textInputEmail = view.findViewById(R.id.textInputEmail);
        textInputPassword = view.findViewById(R.id.textInputPassword);

        view.findViewById(R.id.register_button).setOnClickListener(v -> {
            String name = textInputName.getText().toString().trim();
            String email = textInputEmail.getText().toString().trim();
            String password = textInputPassword.getText().toString().trim();

            if (isEmailOk(email) & isPasswordOk(password)) {
                //binding.progressBar.setVisibility(View.VISIBLE);
                if (!userViewModel.isAuthenticationError()) {
                    userViewModel.getUserMutableLiveData(name, email, password, false).observe(
                            getViewLifecycleOwner(), result -> {
                                if (result.isSuccess()) {
                                    User user = (User) ((Result.Success) result).getData();
                                    //saveLoginData(email, password, user.getIdToken());
                                    userViewModel.setAuthenticationError(false);
                                    Navigation.findNavController(view).navigate(
                                            R.id.action_registrationFragment_to_mainActivity);
                                } else {
                                    userViewModel.setAuthenticationError(true);
                                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                            getErrorMessage(((Result.Error) result).getMessage()),
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    userViewModel.getUser(name, email, password, false);
                }
                //binding.progressBar.setVisibility(View.GONE);
            } else {
                userViewModel.setAuthenticationError(true);
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        R.string.error_email_login, Snackbar.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private String getErrorMessage(String message) {
        switch(message) {
            case WEAK_PASSWORD_ERROR:
                return requireActivity().getString(R.string.error_password_login);
            case USER_COLLISION_ERROR:
                return requireActivity().getString(R.string.error_collision_user);
            default:
                return requireActivity().getString(R.string.error_unexpected);
        }
    }

    // Funzioni di validazione
    /**
     * Checks if the email address has a correct format.
     * @param email The email address to be validated
     * @return true if the email address is valid, false otherwise
     */
    private boolean isEmailOk(String email) {
        // Check if the email is valid through the use of this library:
        // https://commons.apache.org/proper/commons-validator/
        if (!EmailValidator.getInstance().isValid((email))) {
            textInputEmail.setError(getString(R.string.error_email_login));
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    /**
     * Checks if the password is not empty.
     * @param password The password to be checked
     * @return True if the password has at least 6 characters, false otherwise
     */
    private boolean isPasswordOk(String password) {
        // Check if the password length is correct
        if (password.isEmpty() || password.length() < Constants.MINIMUM_LENGTH_PASSWORD) {
            textInputPassword.setError(getString(R.string.error_password_login));
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }
}