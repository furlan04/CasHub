package it.unimib.CasHub.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.User;
import it.unimib.CasHub.repository.transaction.TransactionRepository;
import it.unimib.CasHub.repository.user.IUserRepository;
import it.unimib.CasHub.ui.login.NavLoginHomeActivity;
import it.unimib.CasHub.ui.login.viewmodel.UserViewModel;
import it.unimib.CasHub.ui.login.viewmodel.UserViewModelFactory;
import it.unimib.CasHub.utils.ServiceLocator;

public class HomeActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //Gestione toolbar

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);

        IUserRepository userRepository = ServiceLocator.getInstance().getUserRepository(this.getApplication());
        TransactionRepository transactionRepository = ServiceLocator.getInstance().getTransactionRepository(this.getApplication(), false);
        UserViewModel userViewModel = new ViewModelProvider(this, new UserViewModelFactory(userRepository, transactionRepository)).get(UserViewModel.class);
        userViewModel.setAuthenticationError(false);

        User loggedUser = userViewModel.getLoggedUser();

        if (loggedUser != null && loggedUser.getName() != null) {
            toolbarTitle.setText("Ciao, " + loggedUser.getName() + "!");
        } else {
            toolbarTitle.setText("Benvenuto su CasHub!");
        }

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // 1. Esegui il logout (azione immediata)
            userViewModel.logout();

            // 2. Naviga immediatamente alla LoginActivity
            Toast.makeText(HomeActivity.this, "Logout effettuato", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HomeActivity.this, NavLoginHomeActivity.class);
            // Pulisci lo stack così l'utente non può tornare indietro col tasto back
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });



        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragmentContainerView);

        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homepageTransactionFragment, R.id.homepageStocksFragment
        ).build();

        NavigationUI.setupWithNavController(bottomNav, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}