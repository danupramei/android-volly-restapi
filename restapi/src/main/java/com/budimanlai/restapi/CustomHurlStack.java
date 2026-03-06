package com.budimanlai.restapi;

import com.android.volley.toolbox.HurlStack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CustomHurlStack extends HurlStack {
    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = super.createConnection(url);

        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        return connection;
    }
}