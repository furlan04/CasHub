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

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextEmail = view.findViewById(R.id.textInputEmail);
        editTextPassword = view.findViewById(R.id.textInputPassword);

        Button loginButton = view.findViewById(R.id.login_button);
        Button signupButton = view.findViewById(R.id.goToRegistration);

        loginButton.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (isEmailOk(email) && isPasswordOk(password)) {

                // 1. Reset dello stato nel ViewModel (fondamentale per non leggere vecchi errori)
                userViewModel.resetUserMutableLiveData();

                // 2. Recupero il LiveData (che ora nel ViewModel deve forzare una nuova chiamata)
                // e rimuovo eventuali observer precedenti per sicurezza
                userViewModel.getUserMutableLiveDataLogin(email, password)
                        .removeObservers(getViewLifecycleOwner());

                // 3. Aggiungo l'observer
                userViewModel.getUserMutableLiveDataLogin(email, password)
                        .observe(getViewLifecycleOwner(), result -> {
                            if (result != null) {
                                if (result.isSuccess()) {
                                    // Login successo
                                    userViewModel.setAuthenticationError(false);
                                    Navigation.findNavController(view).navigate(
                                            R.id.action_loginFragment_to_mainActivity);
                                } else {
                                    // Login fallito
                                    userViewModel.setAuthenticationError(true);

                                    // Recupera il messaggio d'errore specifico dal switch getErrorMessage
                                    String errorMsg = getErrorMessage(((Result.Error) result).getMessage());

                                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                            errorMsg, Snackbar.LENGTH_SHORT).show();

                                    // Una volta mostrato l'errore, "puliamo" il risultato
                                    // così al prossimo click l'observer non ri-legge subito questo errore
                                    userViewModel.resetUserMutableLiveData();
                                }
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
        // Aggiungi INVALID_CREDENTIALS_ERROR per evitare che finisca in "Unexpected error"
        switch(message) {
            case WEAK_PASSWORD_ERROR:
                return requireActivity().getString(R.string.error_password_login);
            case INVALID_USER_ERROR:
                return requireActivity().getString(R.string.error_invalid_user);
            case Constants.INVALID_CREDENTIALS_ERROR: // <--- Fondamentale aggiungerlo
                return requireActivity().getString(R.string.error_credentials_login); // "Credenziali non valide"
            case USER_COLLISION_ERROR:
                return requireActivity().getString(R.string.error_collision_user);
            default:
                // Log per debug: così vedi in console cosa sta arrivando davvero se fallisce
                android.util.Log.e("LOGIN_DEBUG", "Errore non gestito: " + message);
                return requireActivity().getString(R.string.error_unexpected) + message;
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