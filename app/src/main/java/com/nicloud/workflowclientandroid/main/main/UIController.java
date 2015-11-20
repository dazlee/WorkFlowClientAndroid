package com.nicloud.workflowclientandroid.main.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nicloud.workflowclientandroid.R;
import com.nicloud.workflowclientandroid.data.connectserver.worker.CheckInOutCommand;
import com.nicloud.workflowclientandroid.data.connectserver.worker.ShiftTaskCommand;
import com.nicloud.workflowclientandroid.data.data.Task;
import com.nicloud.workflowclientandroid.data.data.WorkingData;
import com.nicloud.workflowclientandroid.data.data.observer.DataObserver;
import com.nicloud.workflowclientandroid.data.connectserver.LoadingWorkerTasks;
import com.nicloud.workflowclientandroid.data.connectserver.LoadingWorkerTasks.OnFinishLoadingDataListener;
import com.nicloud.workflowclientandroid.data.connectserver.worker.CompleteTaskForWorkerCommand;
import com.nicloud.workflowclientandroid.dialog.DisplayDialogFragment;
import com.nicloud.workflowclientandroid.login.LoginActivity;
import com.nicloud.workflowclientandroid.main.tasklist.TasksListAdapter;
import com.nicloud.workflowclientandroid.main.tasklist.TasksListAdapter.ItemViewType;
import com.nicloud.workflowclientandroid.main.tasklist.TasksListItem;
import com.nicloud.workflowclientandroid.utility.DividerItemDecoration;
import com.nicloud.workflowclientandroid.utility.Utilities;

import java.util.ArrayList;
import java.util.List;


/**
 * Main component to control the UI
 *
 * @author Danny Lin
 * @since 2015.05.28
 *
 */
public class UIController implements View.OnClickListener, DataObserver, ShiftTaskCommand.OnFinishShiftTaskListener {

    private static final String TAG = "UIController";

    private AppCompatActivity mMainActivity;
    private ActionBar mActionBar;
    private Toolbar mToolbar;

    private TextView mActionBarWorkerName;
    private TextView mActionBarSubtitle;

    private FloatingActionButton mFab;

    private RecyclerView mTasksList;
    private LinearLayoutManager mTasksListManager;
    private TasksListAdapter mTasksListAdapter;
    private List<TasksListItem> mTasksDataSet = new ArrayList<>();

    private FragmentManager mFragmentManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private OnFinishLoadingDataListener mOnFinishLoadingDataListener = new OnFinishLoadingDataListener() {
        @Override
        public void onFinishLoadingData() {
            if (mFirstLaunch) {
                forceHideRefreshSpinner();
                mFab.show();

                mFirstLaunch = false;
            } else {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            WorkingData.getInstance(mMainActivity).updateData();
        }

        @Override
        public void onFailLoadingData(boolean isFailCausedByInternet) {

        }
    };

    private boolean mFirstLaunch = true;


    public UIController(AppCompatActivity activity) {
        mMainActivity = activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        initialize();
    }

    public void onStart() {
        WorkingData.getInstance(mMainActivity).registerMinuteReceiver(mMainActivity);
        WorkingData.getInstance(mMainActivity).registerDataObserver(this);
    }

    public void onStop() {
        WorkingData.getInstance(mMainActivity).unregisterMinuteReceiver(mMainActivity);
        WorkingData.getInstance(mMainActivity).removeDataObserver(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        mMainActivity.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                //ParsePush.unsubscribeInBackground("user_" + WorkingData.getUserId());

                WorkingData.resetAccount();
                SharedPreferences sharedPreferences = mMainActivity.getSharedPreferences(WorkingData.SHARED_PREFERENCE_KEY, 0);
                sharedPreferences.edit().remove(WorkingData.USER_ID).remove(WorkingData.AUTH_TOKEN).commit();

                mMainActivity.startActivity(new Intent(mMainActivity, LoginActivity.class));
                mMainActivity.finish();

                return true;

            case R.id.action_settings:
                return true;

            default:
                return false;
        }
    }

    public void onCompleteTaskOk(String taskId) {
        final String completedTaskName = WorkingData.getInstance(mMainActivity).getWipTask().name;

        CompleteTaskForWorkerCommand completeTaskForWorkerCommand =
                new CompleteTaskForWorkerCommand(mMainActivity, WorkingData.getUserId(), taskId, new CompleteTaskForWorkerCommand.OnCompleteTaskForWorkerListener() {
                    @Override
                    public void onFinishCompleteTask() {
                        if (WorkingData.getInstance(mMainActivity).getScheduledTasks().size() > 0) {
                            WorkingData.getInstance(mMainActivity)
                                    .setWipTask(WorkingData.getInstance(mMainActivity).getScheduledTasks().get(0));
                            WorkingData.getInstance(mMainActivity).removeScheduledTask(0);

                        } else {
                            WorkingData.getInstance(mMainActivity).setWipTask(null);
                        }

                        Toast.makeText(mMainActivity,
                                String.format(mMainActivity.getString(R.string.complete_task), completedTaskName),
                                Toast.LENGTH_SHORT).show();

                        setScheduledTasksData();
                        mTasksListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailCompleteTask() {

                    }
                });

        completeTaskForWorkerCommand.execute();

        Utilities.dismissDialog(mFragmentManager);
    }

    public void onCompleteTaskCancel() {
        Utilities.dismissDialog(mFragmentManager);
    }

    public void onChooseTaskStartWork(String taskId) {
        ShiftTaskCommand shiftTaskCommand = new ShiftTaskCommand(mMainActivity, taskId, this);
        shiftTaskCommand.execute();

        Utilities.dismissDialog(mFragmentManager);
    }

    public void onChooseTaskLog(String taskId) {
        Utilities.goToTaskLogActivity(mMainActivity, taskId);
        Utilities.dismissDialog(mFragmentManager);
    }

    public void onCheck(Location currentLocation, CheckInOutCommand.OnFinishCheckinStatusListener onFinishCheckinStatusListener) {
        CheckInOutCommand checkInOutCommand = new CheckInOutCommand(mMainActivity, currentLocation, onFinishCheckinStatusListener);
        checkInOutCommand.execute();
    }

    private void initialize() {
        mFragmentManager = mMainActivity.getSupportFragmentManager();
        findViews();
        setupViews();
        setupActionbar();
        setupTasksList();
        setupSwipeRefresh();
        loadDataInFirstLaunch();
    }

    private void setupViews() {
        mActionBarWorkerName.setText(WorkingData.getInstance(mMainActivity).getLoginWorker().name);
        mActionBarSubtitle.setText(WorkingData.getInstance(mMainActivity).getLoginWorker().address);
        mFab.setOnClickListener(this);
    }

    private void findViews() {
        mToolbar = (Toolbar) mMainActivity.findViewById(R.id.tool_bar);
        mActionBarWorkerName = (TextView) mMainActivity.findViewById(R.id.action_bar_worker_name);
        mActionBarSubtitle = (TextView) mMainActivity.findViewById(R.id.action_bar_subtitle);
        mFab = (FloatingActionButton) mMainActivity.findViewById(R.id.fab);
        mTasksList = (RecyclerView) mMainActivity.findViewById(R.id.tasks_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mMainActivity.findViewById(R.id.swipe_refresh_container);
    }

    private void setupActionbar() {
        mMainActivity.setSupportActionBar(mToolbar);
        mActionBar = mMainActivity.getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void setupTasksList() {
        mTasksListManager = new LinearLayoutManager(mMainActivity);
        mTasksListAdapter = new TasksListAdapter(mMainActivity, mFragmentManager, mTasksDataSet);

        mTasksList.setLayoutManager(mTasksListManager);
        mTasksList.addItemDecoration(
                new DividerItemDecoration(mMainActivity.getResources().getDrawable(R.drawable.list_divider), false, true));
        mTasksList.setAdapter(mTasksListAdapter);
    }

    private void setupSwipeRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new LoadingWorkerTasks(mMainActivity, mOnFinishLoadingDataListener).execute();
            }

        });

        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void loadDataInFirstLaunch() {
        forceShowRefreshSpinner();
        new LoadingWorkerTasks(mMainActivity, mOnFinishLoadingDataListener).execute();
    }

    /**
     * Use for the first time we enter the app.
     * If we want to trigger the refresh spinner programmatically, we need to use this method.
     */
    private void forceShowRefreshSpinner() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    /**
     * Use for the first time we enter the app.
     * To remove the refresh spinner triggered by forceShowRefreshSpinner().
     */
    private void forceHideRefreshSpinner() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setScheduledTasksData() {
        mTasksDataSet.clear();

        // WIP task
        mTasksDataSet.add(new TasksListItem(new Task(mMainActivity.getString(R.string.wip_task)), ItemViewType.TITLE));
        mTasksDataSet.add(new TasksListItem(WorkingData.getInstance(mMainActivity).getWipTask(), ItemViewType.WIP_TASK));

        // Scheduled task
        mTasksDataSet.add(new TasksListItem(new Task(mMainActivity.getString(R.string.next_task)), ItemViewType.TITLE));
        for (Task scheduledTask : WorkingData.getInstance(mMainActivity).getScheduledTasks()) {
            mTasksDataSet.add(new TasksListItem(scheduledTask, ItemViewType.SCHEDULED_TASK));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Utilities.showDialog(mFragmentManager, DisplayDialogFragment.DialogType.CHECK_IN_OUT, null);
                break;
        }
    }

    @Override
    public void updateData() {
        setScheduledTasksData();
        mTasksListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShiftTaskFinished() {
        new LoadingWorkerTasks(mMainActivity, mOnFinishLoadingDataListener).execute();
    }

    @Override
    public void onShiftTaskFailed() {

    }
}