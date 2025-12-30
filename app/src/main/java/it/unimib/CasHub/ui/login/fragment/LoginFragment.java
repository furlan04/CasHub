package it.unimib.CasHub.ui.login.fragment;

import static it.unimib.CasHub.utils.Constants.INVALID_USER_ERROR;
import static it.unimib.CasHub.utils.Constants.USER_COLLISION_ERROR;
import static it.unimib.CasHub.utils.Constants.WEAK_PASSWORD_ERROR;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.validator.routines.EmailValidator;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.User;
import it.unimib.CasHub.repository.user.IUserRepository;
import it.unimib.CasHub.ui.login.viewmodel.UserViewModel;
import it.unimib.CasHub.ui.login.viewmodel.UserViewModelFactory;
import it.unimib.CasHub.utils.Constants;
import it.unimib.CasHub.utils.ServiceLocator;

public class LoginFragment extends Fragment {
    private TextInputEditText editTextEmail, editTextPassword;
    private UserViewModel userViewModel;
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        IUserRepository userRepository = ServiceLocator.getInstance()
                .getUserRepository(requireActivity().getApplication());

        userViewModel = new ViewModelProvider(requireActivity(),
                new UserViewModelFactory(userRepository)).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    /*
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

     */

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextEmail = view.findViewById(R.id.textInputEmail);
        editTextPassword = view.findViewById(R.id.textInputPassword);

        Button loginButton = view.findViewById(R.id.login_button);
        Button signupButton = view.findViewById(R.id.goToRegistration);

        // 🔐 LOGIN EMAIL / PASSWORD REALE
        loginButton.setOnClickListener(v -> {

            String name = null;
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (isEmailOk(email) && isPasswordOk(password)) {

                userViewModel.getUserMutableLiveData(name, email, password, true)
                        .observe(getViewLifecycleOwner(), result -> {

                            if (result.isSuccess()) {
                                User user = (User) ((Result.Success) result).getData();
                                //saveLoginData(email, password, user.getIdToken());
                                userViewModel.setAuthenticationError(false);
                                Navigation.findNavController(view).navigate(
                                        R.id.action_loginFragment_to_mainActivity);

                            } else {
                                userViewModel.setAuthenticationError(true);
                                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                        getErrorMessage(((Result.Error) result).getMessage()),
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });

            } else {
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        R.string.error_email_login, Snackbar.LENGTH_SHORT).show();
            }
        });

        signupButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_loginFragment_to_registrationFragment));
    }

    private String getErrorMessage(String message) {
        switch(message) {
            case WEAK_PASSWORD_ERROR:
                return requireActivity().getString(R.string.error_password_login);
                case INVALID_USER_ERROR:
                    return requireActivity().getString(R.string.error_invalid_user);
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
            editTextEmail.setError(getString(R.string.error_email_login));
            return false;
        } else {
            editTextEmail.setError(null);
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
            editTextPassword.setError(getString(R.string.error_password_login));
            return false;
        } else {
            editTextPassword.setError(null);
            return true;
        }
    }
}