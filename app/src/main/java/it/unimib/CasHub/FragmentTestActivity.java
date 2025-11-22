package it.unimib.CasHub;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class FragmentTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_test);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TransactionFragment())
                    .commit();
        }
    }
}
