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

import org.apache.commons.validator.routines.EmailValidator;

import com.google.android.material.textfield.TextInputEditText;

import it.unimib.CasHub.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Button button = view.findViewById(R.id.login_button);
        TextInputEditText inputEmail = view.findViewById(R.id.textInputEmail);
        TextInputEditText inputPassword = view.findViewById(R.id.textInputPassword);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                if(isEmailOk(email)){
                    if(isPasswordOk(password)){
                        Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_mainActivity);
                    }
                    else {
                        inputPassword.setError(getString(R.string.check_password));
                    }
                } else {
                    inputEmail.setError(getString(R.string.check_email));
                }


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