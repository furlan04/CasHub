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
import com.google.android.material.textfield.TextInputEditText;
import org.apache.commons.validator.routines.EmailValidator;
import it.unimib.CasHub.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistrationFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    public static RegistrationFragment newInstance(String param1, String param2) {
        RegistrationFragment fragment = new RegistrationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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

                // TODO: Implementa la logica di registrazione
                // Se la registrazione ha successo, naviga alla MainActivity
                // Navigation.findNavController(v).navigate(R.id.action_registrationFragment_to_mainActivity);
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