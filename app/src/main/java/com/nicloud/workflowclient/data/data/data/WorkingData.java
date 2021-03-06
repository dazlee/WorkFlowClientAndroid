package com.nicloud.workflowclient.data.data.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.nicloud.workflowclient.data.data.observer.DataObserver;
import com.nicloud.workflowclient.data.data.observer.DataSubject;
import com.nicloud.workflowclient.main.receiver.MinuteReceiver;
import com.nicloud.workflowclient.utility.Utilities;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ben on 2015/7/18.
 */
public final class WorkingData implements DataSubject {

    public static final String SHARED_PREFERENCE_KEY = "workflow";
    public static final String COMPANY_ACCOUNT = "company_account";
    public static final String USER_ID = "userId";
    public static final String AUTH_TOKEN = "authToken";

    private static final String TAG = "WorkingData";

    private static final class DataType {
        public static final int EQUIPMENT = 0;
        public static final int FACTORY = 1;
        public static final int MANAGER = 2;
        public static final int TASK = 3;
        public static final int CASE = 4;
        public static final int VENDOR = 5;
        public static final int WARNING = 6;
        public static final int WORKER = 7;
    }

    private volatile static WorkingData sWorkingData = null;
    private static int sDataIdCount = -1;

    private Context mContext;
    private BroadcastReceiver mMinuteReceiver;
    private List<DataObserver> mDataObservers = new ArrayList<>();

    private static String sUserId;
    private static String sAuthToken;

    private Worker mLoginWorker;

    private Task mWipTask;
    private List<Task> mScheduledTasks = new ArrayList<>();


    public static WorkingData getInstance(Context context) {
        if (sWorkingData == null) {
            synchronized (WorkingData.class) {
                if (sWorkingData == null) {
                    sWorkingData = new WorkingData(context);
                }
            }
        }

        return sWorkingData;
    }

    private WorkingData(Context context) {
        mContext = context;
        mMinuteReceiver = new MinuteReceiver();
    }

    public void registerMinuteReceiver(Context context) {
        IntentFilter timeTickFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        context.registerReceiver(mMinuteReceiver, timeTickFilter);
        //Log.d(TAG, "Register MinuteReceiver");
    }

    public void unregisterMinuteReceiver(Context context) {
        context.unregisterReceiver(mMinuteReceiver);
        //Log.d(TAG, "Unregister MinuteReceiver");
    }

    public static void resetAccount () {
        sUserId = "";
        sAuthToken = "";
    }
    public static void setUserId(String userId) {
        sUserId = userId;
    }
    public static void setAuthToken(String authToken) {
        sAuthToken = authToken;
    }
    public static String getUserId () {
        return sUserId;
    }
    public static String getAuthToken() {
        return sAuthToken;
    }


    public void setLoginWorker(Worker worker) {
        mLoginWorker = worker;
    }
    public Worker getLoginWorker() {
        return mLoginWorker;
    }


    public void setWipTask(Task task) {
        mWipTask = task;
    }
    public Task getWipTask() {
        return mWipTask;
    }


    public void addScheduledTask(Task task) {
        mScheduledTasks.add(task);
    }
    public void addAllScheduledTasks(List<Task> scheduledTasks) {
        mScheduledTasks.addAll(scheduledTasks);
    }
    public void clearScheduledTasks() {
        mScheduledTasks.clear();
    }
    public void removeScheduledTask(int position) {
        mScheduledTasks.remove(position);
    }
    public List<Task> getScheduledTasks() {
        return mScheduledTasks;
    }


    public Task getTask(String taskId) {
        if (mWipTask != null && Utilities.isSameId(mWipTask.id, taskId)) {
            return mWipTask;
        }

        for (Task scheduledTask : mScheduledTasks) {
            if (Utilities.isSameId(scheduledTask.id, taskId)) {
                return scheduledTask;
            }
        }

        return null;
    }


    public void updateData() {
        notifyDataObservers();
    }

    @Override
    public void registerDataObserver(DataObserver o) {
        mDataObservers.add(o);
    }

    @Override
    public void removeDataObserver(DataObserver o) {
        int index = mDataObservers.indexOf(o);
        if (index >= 0) {
            mDataObservers.remove(index);
        }
    }

    @Override
    public void notifyDataObservers() {
        for (DataObserver dataObserver : mDataObservers) {
            dataObserver.updateData();
        }
    }
}
