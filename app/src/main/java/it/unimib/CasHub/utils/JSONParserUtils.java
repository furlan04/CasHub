package it.unimib.CasHub.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.model.ForexAPIResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JSONParserUtils {

    public Context context;

    public JSONParserUtils(Context context) {
        this.context = context;
    }

    public ForexAPIResponse parseJSONFileWithGSonForexRates(String filename) throws IOException {
        InputStream inputStream = context.getAssets().open(filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        return new Gson().fromJson(bufferedReader, ForexAPIResponse.class);
    }

    public List<Agency> parseJSONFileWithGSonAgencyList(String filename) throws IOException {
        InputStream inputStream = context.getAssets().open(filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        Type listType = new TypeToken<List<Agency>>() {}.getType();
        return new Gson().fromJson(bufferedReader, listType);
    }

    public Map<String, String> parseJSONFileWithGSonCurrencies(String filename) throws IOException {
        InputStream inputStream = context.getAssets().open(filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        Type type = new TypeToken<Map<String, String>>() {}.getType();
        return new Gson().fromJson(bufferedReader, type);
    }
}
