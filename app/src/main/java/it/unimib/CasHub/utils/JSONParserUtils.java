package it.unimib.CasHub.utils;

import android.content.Context;

import com.google.gson.Gson;
import it.unimib.CasHub.model.ForexAPIResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JSONParserUtils {

    public Context context;

    public JSONParserUtils(Context context) {
        this.context = context;
    }

    public ForexAPIResponse parseJSONFileWithGSon(String filename) throws IOException {
        InputStream inputStream = context.getAssets().open(filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        return new Gson().fromJson(bufferedReader, ForexAPIResponse.class);
    }
}