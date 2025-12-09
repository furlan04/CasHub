package it.unimib.CasHub.repository;

import android.app.Application;

import java.io.IOException;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.utils.Constants;
import it.unimib.CasHub.utils.JSONParserUtils;
import it.unimib.CasHub.utils.NewsResponseCallBack;

public class AgencyMockRepository implements IAgencyRepository{
    private final Application application;
    private final NewsResponseCallBack responseCallback;
    public AgencyMockRepository(Application application, NewsResponseCallBack responseCallback) {
        this.application = application;
        this.responseCallback = responseCallback;
    }

    @Override
    public void getAllAgencies(){
        List<Agency> agencyApiResponse;
        JSONParserUtils jsonParserUtils = new JSONParserUtils(application.getApplicationContext());

        try {
            // 1. Leggi i dati simulati dal file JSON
            agencyApiResponse = jsonParserUtils.parseJSONFileWithGSonAgencyList(Constants.TEST_AGENCY_APP);

            if (agencyApiResponse != null) {
                //  2. Invia i dati direttamente al ViewModel/UI tramite la callback.
                responseCallback.onSuccess(agencyApiResponse, System.currentTimeMillis());
            } else {
                responseCallback.onFailure(String.valueOf(R.string.error_retrieving_agencies));
            }
        } catch (IOException e) {
            responseCallback.onFailure(String.valueOf(R.string.error_retrieving_agencies));
            throw new RuntimeException(e);
        }
    }
}
