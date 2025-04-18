package com.mmhachem.exchange;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Base64;
import java.util.prefs.Preferences;

public class Authentication {
    private static Authentication instance;
    private static final String TOKEN_KEY = "TOKEN";

    private static String token;
    private static int userId;
    private Preferences pref;

    private Authentication() {
        pref = Preferences.userRoot().node(this.getClass().getName());
        token = pref.get(TOKEN_KEY, null);

        if (token != null) {
            try {
                String[] parts = token.split("\\.");
                String payload = new String(Base64.getDecoder().decode(parts[1]));
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(payload).getAsJsonObject();
                userId = json.get("sub").getAsInt();  // ✅ auto-load userId
            } catch (Exception e) {
                System.err.println("Failed to decode token: " + e.getMessage());
                userId = 0;
            }
        }
    }

    static public Authentication getInstance() {
        if (instance == null) {
            instance = new Authentication();
        }
        return instance;
    }

    public static String getToken() {
        return token;
    }

    public void saveToken(String token) {
        this.token = token;
        pref.put(TOKEN_KEY, token);
    }

    public void deleteToken() {
        this.token = null;
        pref.remove(TOKEN_KEY);
    }
    public void saveUserId(int id) {
        Authentication.userId = id;
    }

    public int getUserId() {
        return userId;
    }
    public void parseUserIdFromToken() {
        if (token == null) return;
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(payload).getAsJsonObject();

            userId = json.get("sub").getAsInt();
        } catch (Exception e) {
            System.err.println("Failed to parse user ID from token: " + e.getMessage());
        }
    }

}
