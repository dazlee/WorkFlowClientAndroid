package com.nicloud.workflowclient.data.connectserver.worker;

import android.util.Log;

import com.nicloud.workflowclient.data.data.data.WorkingData;
import com.nicloud.workflowclient.data.connectserver.LoadingDataUtils;
import com.nicloud.workflowclient.data.connectserver.restful.IGetRequestStrategy;
import com.nicloud.workflowclient.data.utility.RestfulUtils;
import com.nicloud.workflowclient.data.utility.URLUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by daz on 10/20/15.
 */
public class LoadingLoginWorkerStrategy implements IGetRequestStrategy {

    private static final String TAG = LoadingLoginWorkerStrategy.class.toString();

    @Override
    public JSONObject get() {
        try {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("x-user-id", WorkingData.getUserId());
            headers.put("x-auth-token", WorkingData.getAuthToken());

            String urlString = URLUtils.buildURLString(LoadingDataUtils.sBaseUrl, LoadingDataUtils.WorkingDataUrl.EndPoints.LOGIN_WORKER, null);
            String responseJSONString = RestfulUtils.restfulGetRequest(urlString, headers);
            JSONObject responseJSON = new JSONObject(responseJSONString);
            if (responseJSON.getString("status").equals("success")) {
                return responseJSON.getJSONObject("result");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Exception in LoadingLoginWorkerStrategy()");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
