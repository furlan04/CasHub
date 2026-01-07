package it.unimib.CasHub.ui.login.fragment;

import static android.content.ContentValues.TAG;
import static it.unimib.CasHub.utils.Constants.INVALID_CREDENTIALS_ERROR;
import static it.unimib.CasHub.utils.Constants.INVALID_USER_ERROR;
import static it.unimib.CasHub.utils.Constants.USER_COLLISION_ERROR;
import static it.unimib.CasHub.utils.Constants.WEAK_PASSWORD_ERROR;

import android.app.Activity;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.validator.routines.EmailValidator;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.User;
import it.unimib.CasHub.repository.transaction.ITransactionRepository;
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


    // 🔹 One Tap Google
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        IUserRepository userRepository = ServiceLocator.getInstance()
                .getUserRepository(requireActivity().getApplication());

        userViewModel = new ViewModelProvider(requireActivity(),
                new UserViewModelFactory(userRepository)).get(UserViewModel.class);

        // --- Configura One Tap Google
        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true).build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(true)
                .build();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                            String idToken = credential.getGoogleIdToken();
                            if (idToken != null) {
                                userViewModel.getGoogleUserMutableLiveData(idToken)
                                        .observe(getViewLifecycleOwner(), authenticationResult -> {
                                            if (authenticationResult.isSuccess()) {
                                                User user = (User) ((Result.Success) authenticationResult).getData();
                                                Log.i(TAG, "Logged as: " + user.getEmail());
                                                Navigation.findNavController(getView())
                                                        .navigate(R.id.action_loginFragment_to_mainActivity);
                                            } else {
                                                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                                        getErrorMessage(((Result.Error) authenticationResult).getMessage()),
                                                        Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } catch (ApiException e) {
                            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                    getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
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
        Button googleLoginButton = view.findViewById(R.id.google_login_button);

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
            }
        });

        signupButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_loginFragment_to_registrationFragment));

        // --- Login Google
        googleLoginButton.setOnClickListener(v ->
                oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(requireActivity(), result -> {
                            IntentSenderRequest intentSenderRequest =
                                    new IntentSenderRequest.Builder(result.getPendingIntent()).build();
                            activityResultLauncher.launch(intentSenderRequest);
                        })
                        .addOnFailureListener(requireActivity(), e ->
                                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                        getString(R.string.error_unexpected),
                                        Snackbar.LENGTH_SHORT).show())
        );
    }

    private String getErrorMessage(String message) {
        switch(message) {
            case WEAK_PASSWORD_ERROR:
                return requireActivity().getString(R.string.error_password_login);

            case INVALID_USER_ERROR:
            case INVALID_CREDENTIALS_ERROR: // <--- AGGIUNGI QUESTO CASO
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
            editTextPassword.setError(getString(R.string.error_password_login), null);
            return false;
        } else {
            editTextPassword.setError(null);
            return true;
        }
    }
}