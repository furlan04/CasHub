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

            // 1. Usa && per la validazione
            if (isEmailOk(email) && isPasswordOk(password)) {

                // 2. RIMUOVI il controllo "if (!userViewModel.isAuthenticationError())"
                // Dobbiamo sempre osservare il risultato della chiamata corrente.

                userViewModel.getUserMutableLiveData(name, email, password, false).observe(
                        getViewLifecycleOwner(), result -> {
                            if (result != null) {
                                if (result.isSuccess()) {
                                    // Reset dell'errore e navigazione
                                    userViewModel.setAuthenticationError(false);
                                    Navigation.findNavController(view).navigate(
                                            R.id.action_registrationFragment_to_mainActivity);
                                } else {
                                    // Gestione errore
                                    userViewModel.setAuthenticationError(true);
                                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                            getErrorMessage(((Result.Error) result).getMessage()),
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                // Errore di validazione locale (input vuoti o malformati)
                userViewModel.setAuthenticationError(true);
                // Nota: isEmailOk e isPasswordOk settano già l'error sugli EditText
            }
        });


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button goToLoginButton = view.findViewById(R.id.goToLogin);
        goToLoginButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_registrationFragment_to_loginFragment));

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