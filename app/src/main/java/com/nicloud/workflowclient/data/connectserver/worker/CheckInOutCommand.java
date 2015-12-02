package com.nicloud.workflowclient.data.connectserver.worker;

import android.content.Context;
import android.location.Location;

import com.nicloud.workflowclient.data.connectserver.restful.PostRequestAsyncTask;

import org.json.JSONObject;

/**
 * Created by daz on 11/17/15.
 */
public class CheckInOutCommand implements IWorkerActionCommand, PostRequestAsyncTask.OnFinishPostingDataListener {

    public interface OnDialogCheckInOutStatusListener {
        void onCheckInOutFinished();
        void onCheckInOutFailed();
    }

    public interface OnMainCheckInOutStatusListener {
        void onCheckInOutFinished();
        void onCheckInOutFailed();
    }

    private Context mContext;
    private Location mCurrentLocation;

    private PostRequestAsyncTask mPostRequestAsyncTask;
    private OnDialogCheckInOutStatusListener mOnDialogCheckInOutStatusListener;
    private OnMainCheckInOutStatusListener mOnMainCheckInOutStatusListener;


    public CheckInOutCommand(Context context, Location currentLocation,
                             OnDialogCheckInOutStatusListener onDialogCheckInOutStatusListener,
                             OnMainCheckInOutStatusListener onMainCheckInOutStatusListener) {
        mContext = context;
        mCurrentLocation = currentLocation;
        mOnDialogCheckInOutStatusListener = onDialogCheckInOutStatusListener;
        mOnMainCheckInOutStatusListener = onMainCheckInOutStatusListener;
    }


    @Override
    public void execute() {
        CheckInOutStrategy checkInOutStrategy = new CheckInOutStrategy(mCurrentLocation);
        mPostRequestAsyncTask = new PostRequestAsyncTask(mContext, checkInOutStrategy, this);
        mPostRequestAsyncTask.execute();
    }

    @Override
    public void onFinishPostingData() {
        JSONObject result = mPostRequestAsyncTask.getResult();
        if (result != null) {
            mOnDialogCheckInOutStatusListener.onCheckInOutFinished();
            mOnMainCheckInOutStatusListener.onCheckInOutFinished();
        } else {
            mOnDialogCheckInOutStatusListener.onCheckInOutFailed();
            mOnMainCheckInOutStatusListener.onCheckInOutFailed();
        }
    }
    @Override
    public void onFailPostingData(boolean isFailCausedByInternet) {
        mOnDialogCheckInOutStatusListener.onCheckInOutFailed();
        mOnMainCheckInOutStatusListener.onCheckInOutFailed();
    }
}