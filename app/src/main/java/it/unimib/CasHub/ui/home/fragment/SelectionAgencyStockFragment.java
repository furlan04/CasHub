package it.unimib.CasHub.ui.home.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.AgencyRecyclerAdapter;
import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.repository.AgencyAPIRepository;
import it.unimib.CasHub.repository.AgencyMockRepository;
import it.unimib.CasHub.repository.IAgencyRepository;
import it.unimib.CasHub.utils.NetworkUtil;
import it.unimib.CasHub.utils.NewsResponseCallBack;
import it.unimib.CasHub.utils.SharedPreferencesUtils;

public class SelectionAgencyStockFragment extends Fragment implements NewsResponseCallBack {

    public static final String TAG = SelectionAgencyStockFragment.class.getName();

    private IAgencyRepository agencyRepository;
    private List<Agency> agencyList = new ArrayList<>();
    private AgencyRecyclerAdapter adapter;

    // Dichiarazione delle viste
    private RecyclerView recyclerView;
    private LoadingIndicator loadingIndicator;
    private View noInternetMessage; // Meglio tenerne un riferimento se lo usi più volte

    public SelectionAgencyStockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Qui non si inizializza più il repository per evitare crash
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selection_agency_stock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Trova tutte le viste una sola volta, all'inizio
        recyclerView = view.findViewById(R.id.recyclerView);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        noInternetMessage = view.findViewById(R.id.noInternetMessage);

        // 2. Inizializza il repository QUI, quando la vista è pronta
        if (requireActivity().getResources().getBoolean(R.bool.debug)) {
            agencyRepository = new AgencyMockRepository(requireActivity().getApplication(), this);
        } else {
            agencyRepository = new AgencyAPIRepository(requireActivity().getApplication(), this);
        }

        // 3. Prepara la RecyclerView e l'Adapter
        adapter = new AgencyRecyclerAdapter(agencyList);

        // Imposta il listener per gestire il click sulle card
        adapter.setOnAgencyClickListener(agency -> {
            // Naviga al fragment dei dettagli
            Bundle bundle = new Bundle();
            bundle.putSerializable("agency", agency);
            Navigation.findNavController(view).navigate(
                    R.id.action_selectionAgencyStockFragment_to_stockDetailsFragment,
                    bundle
            );
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext())); // Usa requireContext()
        recyclerView.setAdapter(adapter);

        // 4. Mostra lo stato di caricamento iniziale
        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // 5. Controlla la rete e avvia il caricamento dei dati
        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            noInternetMessage.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE); // Nascondi il caricamento se non c'è rete
        } else {
            noInternetMessage.setVisibility(View.GONE);
            agencyRepository.getAllAgencies(); // Avvia la chiamata API
        }
    }

    @Override
    public void onSuccess(List<Agency> articlesList, long lastUpdate) {
        this.agencyList.clear();
        this.agencyList.addAll(articlesList); // Usa addAll, è più efficiente

        Log.i(TAG, "Arrivati: " + this.agencyList.size());

        if (getActivity() == null) return; // Controllo di sicurezza aggiuntivo

        requireActivity().runOnUiThread(() -> { // Usa una lambda, è più pulito
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);
        });
    }

    @Override
    public void onFailure(String errorMessage) {
        if (getView() == null) return; // Controllo di sicurezza

        requireActivity().runOnUiThread(() -> {
            loadingIndicator.setVisibility(View.GONE); // Nascondi il caricamento anche in caso di errore
            Snackbar.make(getView(), errorMessage, Snackbar.LENGTH_SHORT).show();
        });
    }
}