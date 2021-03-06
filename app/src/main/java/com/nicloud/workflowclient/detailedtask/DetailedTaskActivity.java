package com.nicloud.workflowclient.detailedtask;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.nicloud.workflowclient.R;
import com.nicloud.workflowclient.data.connectserver.activity.ILoadingActivitiesStrategy;
import com.nicloud.workflowclient.data.connectserver.activity.LoadingActivitiesAsyncTask;
import com.nicloud.workflowclient.data.connectserver.activity.LoadingTaskActivitiesStrategy;
import com.nicloud.workflowclient.data.connectserver.tasklog.OnLoadImageListener;
import com.nicloud.workflowclient.data.data.activity.ActivityDataFactory;
import com.nicloud.workflowclient.data.data.activity.BaseData;
import com.nicloud.workflowclient.data.data.data.Task;
import com.nicloud.workflowclient.data.data.data.WorkingData;
import com.nicloud.workflowclient.detailedtask.addlog.AddLogActivity;
import com.nicloud.workflowclient.detailedtask.checklist.CheckListFragment;
import com.nicloud.workflowclient.detailedtask.tasklog.DetailedTaskListAdapter;
import com.nicloud.workflowclient.detailedtask.tasklog.TaskLogFragment;
import com.nicloud.workflowclient.utility.DividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailedTaskActivity extends AppCompatActivity implements TabHost.OnTabChangeListener {

    public static final String EXTRA_TASK_ID = "DetailedTaskActivity_extra_task_id";

    private static final int REQUEST_ADD_LOG = 32;
    private static final int TASK_LOG_LIMIT = 15;

    private static final class TabPosition {
        public static final int CHECK = 0;
        public static final int TEXT = 1;
        public static final int PHOTO = 2;
        public static final int FILE = 3;
    }

    private static final class TabTag {
        public static final String CHECK = "tag_tab_check";
        public static final String TEXT = "tag_tab_text";
        public static final String PHOTO = "tag_tab_photo";
        public static final String FILE = "tag_tab_file";
    }

    private static final class FragmentTag {
        public static final String CHECK_LIST = "tag_fragment_check_list";
        public static final String TASK_LOG = "tag_fragment_task_log";
    }

    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;

    private ActionBar mActionBar;
    private Toolbar mToolbar;

    private TextView mTaskName;
    private TextView mCaseName;

    private TabHost mDetailedTaskTabHost;

    private Task mTask;


    private class DetailedTaskTabContentFactory implements TabHost.TabContentFactory {

        private Context mContext;

        public DetailedTaskTabContentFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            v.setVisibility(View.GONE);
            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_task);
        initialize();
    }

    private void initialize() {
        mFragmentManager = getSupportFragmentManager();
        mTask = WorkingData.getInstance(this).getTask(getIntent().getStringExtra(EXTRA_TASK_ID));
        findViews();
        setupActionBar();
        setupTabs();
        setupInitFragment();
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mTaskName = (TextView) findViewById(R.id.detailed_task_task_name);
        mCaseName = (TextView) findViewById(R.id.detailed_task_case_name);
        mDetailedTaskTabHost = (TabHost) findViewById(R.id.detailed_task_tab_host);
    }

    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mTaskName.setText(mTask.name);
        mCaseName.setText(mTask.caseName);
    }

    private void setupTabs() {
        mDetailedTaskTabHost.setup();
        addTab(TabTag.CHECK);
        addTab(TabTag.TEXT);
        addTab(TabTag.PHOTO);
        addTab(TabTag.FILE);
        mDetailedTaskTabHost.setOnTabChangedListener(this);
    }

    private void addTab(String tag) {
        mDetailedTaskTabHost.addTab(mDetailedTaskTabHost.newTabSpec(tag).setIndicator(getTabView(tag))
                .setContent(new DetailedTaskTabContentFactory(this)));
    }

    private View getTabView(String tag) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.detailed_task_tab, null);
        TextView tabText = (TextView) tabView.findViewById(R.id.detailed_task_tab_text);

        String text = "";
        if (TabTag.CHECK.equals(tag)) {
            text = getString(R.string.detailed_task_tab_check_list);
        } else if(TabTag.TEXT.equals(tag)) {
            text = getString(R.string.detailed_task_tab_text);
        } else if(TabTag.PHOTO.equals(tag)) {
            text = getString(R.string.detailed_task_tab_photo);
        } else if(TabTag.FILE.equals(tag)) {
            text = getString(R.string.detailed_task_tab_file);
        }

        tabText.setText(text);

        return tabView;
    }

    private void setupInitFragment() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        CheckListFragment checkListFragment = (CheckListFragment) mFragmentManager.findFragmentByTag(FragmentTag.CHECK_LIST);
        if (checkListFragment == null) {
            checkListFragment = new CheckListFragment();
            fragmentTransaction.add(R.id.detailed_task_content, checkListFragment, FragmentTag.CHECK_LIST);
        }

        mCurrentFragment = checkListFragment;
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailed_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_add_log:
                goToAddLogActivity();
                return true;

            default:
                return false;
        }
    }

    private void goToAddLogActivity() {
        Intent intent = new Intent(this, AddLogActivity.class);
        intent.putExtra(AddLogActivity.EXTRA_TASK_ID, mTask.id);

        startActivityForResult(intent, REQUEST_ADD_LOG);
    }

    @Override
    public void onTabChanged(String tabId) {
        switch (mDetailedTaskTabHost.getCurrentTab()) {
            case TabPosition.CHECK:
                if (mCurrentFragment instanceof CheckListFragment) return;
                replaceTo(CheckListFragment.class, FragmentTag.CHECK_LIST);

                break;

            case TabPosition.TEXT:
            case TabPosition.PHOTO:
            case TabPosition.FILE:
                if (mCurrentFragment instanceof TaskLogFragment) return;
                replaceTo(TaskLogFragment.class, FragmentTag.TASK_LOG);

                break;
        }
    }

    private void replaceTo(Class<?> fragmentClass, String fragmentTag) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        Fragment fragment = mFragmentManager.findFragmentByTag(fragmentTag);
        if (fragment == null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                fragmentTransaction.replace(R.id.detailed_task_content, fragment, fragmentTag);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        mCurrentFragment = fragment;
        fragmentTransaction.commit();
    }
}
