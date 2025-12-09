package it.unimib.CasHub.ui.home.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.unimib.CasHub.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomepageStocksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomepageStocksFragment extends Fragment {

    public HomepageStocksFragment() {
        // Required empty public constructor
    }

    public static HomepageStocksFragment newInstance() {
        HomepageStocksFragment fragment = new HomepageStocksFragment();
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
        // collega la classe al tuo layout XML
        View view = inflater.inflate(R.layout.fragment_homepage_stocks, container, false);

        // Qui in futuro potrai fare findViewById(...) per bottoni, TextView, ecc.

        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button button = view.findViewById(R.id.btnAggiungiTitolo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_homepageStocksFragment_to_selectionAgencyStockFragment);
            }
        });

    }

}
