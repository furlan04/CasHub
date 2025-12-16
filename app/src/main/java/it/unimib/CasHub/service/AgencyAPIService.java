package it.unimib.CasHub.service;

import static it.unimib.CasHub.utils.Constants.AGENCY_LIST_ENDPOINT;
import static it.unimib.CasHub.utils.Constants.AGENCY_START_QUERY;
import static it.unimib.CasHub.utils.Constants.AGENCY_STRING_APIKEY;

import java.util.List;

import it.unimib.CasHub.model.Agency;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface AgencyAPIService {
    @GET(AGENCY_LIST_ENDPOINT)
    Call<List<Agency>> getAgencies(
            @Query(AGENCY_START_QUERY) String query,
            @Query(AGENCY_STRING_APIKEY) String apiKey);
}
