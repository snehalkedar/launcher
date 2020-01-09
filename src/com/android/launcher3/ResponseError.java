package com.android.launcher3;

import org.json.JSONObject;
import org.json.JSONException;

class ResponseError {
    private int version;
    private int code;
    private String message;

    ResponseError(String json) {
        try {
            JSONObject payload = new JSONObject(json);
            String tmp = payload.getString("version");
            try {
                version = Integer.parseInt(tmp);
            } catch (NumberFormatException e) {}
            JSONObject error = payload.getJSONObject("error");
            tmp = error.getString("code");
            try {
                code = Integer.parseInt(tmp);
            } catch (NumberFormatException e) {}
            message = error.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int getVersion() {
        return version;
    }

    int getCode() {
        return code;
    }

    String getMessage() {
        return message;
    }
}
