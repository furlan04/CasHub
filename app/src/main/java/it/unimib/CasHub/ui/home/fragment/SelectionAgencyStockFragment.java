package it.unimib.CasHub.ui.home.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.AgencyRecyclerAdapter;
import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.repository.AgencyAPIRepository;
import it.unimib.CasHub.repository.AgencyMockRepository;
import it.unimib.CasHub.repository.IAgencyRepository;
import it.unimib.CasHub.utils.NetworkUtil;
import it.unimib.CasHub.utils.AgencyResponseCallBack;

public class SelectionAgencyStockFragment extends Fragment implements AgencyResponseCallBack {

    public static final String TAG = SelectionAgencyStockFragment.class.getName();

    private IAgencyRepository agencyRepository;
    private List<Agency> agencyList = new ArrayList<>();
    private AgencyRecyclerAdapter adapter;

    private RecyclerView recyclerView;
    private TextInputEditText searchEditText;
    private LoadingIndicator loadingIndicator;
    private View noInternetMessage;
    private TextView noInternetText;

    public SelectionAgencyStockFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selection_agency_stock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        noInternetMessage = view.findViewById(R.id.noInternetMessage);
        noInternetText = noInternetMessage.findViewById(R.id.noInternetMessage); // TextView interna
        searchEditText = view.findViewById(R.id.searchEditText);

        // Inizializza repository
        if (requireActivity().getResources().getBoolean(R.bool.debug)) {
            agencyRepository = new AgencyMockRepository(requireActivity().getApplication(), this);
        } else {
            agencyRepository = new AgencyAPIRepository(requireActivity().getApplication(), this);
        }

        // Adapter RecyclerView
        adapter = new AgencyRecyclerAdapter(agencyList, agency -> {
            Bundle bundle = new Bundle();
            bundle.putString("companyName", agency.getName());
            if (getView() != null) {
                androidx.navigation.Navigation.findNavController(getView())
                        .navigate(R.id.action_selectionAgencyStockFragment_to_stockDetailsFragment, bundle);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Setup searchbar con debounce
        final android.os.Handler handler = new android.os.Handler();
        final int DEBOUNCE_DELAY = 400;
        searchEditText.addTextChangedListener(new TextWatcher() {
            private Runnable workRunnable;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (workRunnable != null) handler.removeCallbacks(workRunnable);
            }
            @Override public void afterTextChanged(Editable s) {
                workRunnable = () -> performSearch(s.toString());
                handler.postDelayed(workRunnable, DEBOUNCE_DELAY);
            }
        });

        // Stato iniziale
        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            noInternetMessage.setVisibility(View.VISIBLE);
            noInternetText.setText(getString(R.string.no_internet_connection));
            loadingIndicator.setVisibility(View.GONE);
        } else {
            noInternetMessage.setVisibility(View.GONE);
            agencyRepository.getAllAgencies("");
        }
    }

    @Override
    public void onSuccess(List<Agency> agenciesList, long lastUpdate) {
        this.agencyList.clear();
        this.agencyList.addAll(agenciesList);

        if (getActivity() == null) return;

        requireActivity().runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);

            if (agenciesList.isEmpty() && !searchEditText.getText().toString().isEmpty()) {
                noInternetMessage.setVisibility(View.VISIBLE);
                noInternetText.setText(getString(R.string.no_agency_found));
            } else {
                noInternetMessage.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onFailure(String errorMessage) {
        if (getView() == null) return;

        requireActivity().runOnUiThread(() -> {
            loadingIndicator.setVisibility(View.GONE);
            if (!searchEditText.getText().toString().trim().isEmpty()) {
                noInternetMessage.setVisibility(View.VISIBLE);
                noInternetText.setText(errorMessage);
            }
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            agencyList.clear();
            adapter.notifyDataSetChanged();
            noInternetMessage.setVisibility(View.GONE); // Nascondi messaggio se campo vuoto
            return;
        }

        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            noInternetMessage.setVisibility(View.VISIBLE);
            noInternetText.setText(getString(R.string.no_internet_connection));
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);
        agencyRepository.getAllAgencies(query);
    }
}
