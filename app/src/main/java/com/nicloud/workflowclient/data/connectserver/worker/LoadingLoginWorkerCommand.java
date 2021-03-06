package com.nicloud.workflowclient.data.connectserver.worker;

import android.content.Context;

import com.nicloud.workflowclient.data.data.data.WorkingData;
import com.nicloud.workflowclient.data.connectserver.LoadingDataUtils;
import com.nicloud.workflowclient.data.connectserver.restful.GetRequestAsyncTask;

import org.json.JSONObject;

/**
 * Created by daz on 10/20/15.
 */
public class LoadingLoginWorkerCommand implements IWorkerActionCommand, GetRequestAsyncTask.OnFinishGettingDataListener {

    public interface OnLoadingLoginWorker {
        void onLoadingLoginWorkerSuccessful();
        void onLoadingLoginWorkerFailed(boolean isFailCausedByInternet);
    }

    private Context mContext;

    private GetRequestAsyncTask mGetRequestAsyncTask;
    private OnLoadingLoginWorker mOnLoadingLoginWorker;


    public LoadingLoginWorkerCommand(Context context, OnLoadingLoginWorker onLoadingLoginWorker) {
        mContext = context;
        mOnLoadingLoginWorker = onLoadingLoginWorker;
    }


    @Override
    public void execute() {
        LoadingLoginWorkerStrategy strategy = new LoadingLoginWorkerStrategy();
        mGetRequestAsyncTask = new GetRequestAsyncTask(mContext, strategy, this);
        mGetRequestAsyncTask.execute();
    }


    @Override
    public void onFinishGettingData() {
        JSONObject result = mGetRequestAsyncTask.getResult();

        if (result != null) {
            WorkingData.getInstance(mContext).setLoginWorker(LoadingDataUtils.retrieveWorkerFromJson(mContext, result));
            mOnLoadingLoginWorker.onLoadingLoginWorkerSuccessful();

        } else {
            mOnLoadingLoginWorker.onLoadingLoginWorkerFailed(false);
        }
    }

    @Override
    public void onFailGettingData(boolean isFailCausedByInternet) {
        mOnLoadingLoginWorker.onLoadingLoginWorkerFailed(isFailCausedByInternet);
    }
}
