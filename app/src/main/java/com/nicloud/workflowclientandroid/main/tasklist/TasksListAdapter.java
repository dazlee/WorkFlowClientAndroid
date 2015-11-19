package com.nicloud.workflowclientandroid.main.tasklist;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nicloud.workflowclientandroid.R;
import com.nicloud.workflowclientandroid.data.data.Task;
import com.nicloud.workflowclientandroid.data.data.WorkingData;
import com.nicloud.workflowclientandroid.dialog.DisplayDialogFragment;
import com.nicloud.workflowclientandroid.dialog.DisplayDialogFragment.DialogType;
import com.nicloud.workflowclientandroid.tasklog.log.TaskLogActivity;
import com.nicloud.workflowclientandroid.utility.Utilities;

import java.util.List;

/**
 * Created by logicmelody on 2015/11/11.
 */
public class TasksListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private FragmentManager mFragmentManager;
    private List<TasksListItem> mDataSet;

    private DisplayDialogFragment mDisplayDialogFragment;

    public static class ItemViewType {
        public static final int TITLE = 0;
        public static final int WIP_TASK = 1;
        public static final int SCHEDULED_TASK = 2;
    }

    private class TitleViewHolder extends RecyclerView.ViewHolder {

        public TextView title;

        public TitleViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.task_list_title);
        }
    }

    private class WipTaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public View wipTaskCardView;
        public View noWipTaskCardView;
        public TextView chooseTaskText;
        public TextView wipTaskName;
        public TextView wipCaseName;
        public TextView wipWorkingTime;
        public View pauseButton;
        public View completeButton;

        public WipTaskViewHolder(View view) {
            super(view);
            findViews(view);
            setupViews();
        }

        private void findViews(View view) {
            wipTaskCardView = view.findViewById(R.id.wip_task_card_view);
            noWipTaskCardView = view.findViewById(R.id.no_wip_task_card_view);
            chooseTaskText = (TextView) view.findViewById(R.id.no_wip_task_card_choose_task_text);
            wipTaskName = (TextView) view.findViewById(R.id.wip_task_card_task_name);
            wipCaseName = (TextView) view.findViewById(R.id.wip_task_card_case_name);
            wipWorkingTime = (TextView) view.findViewById(R.id.wip_task_card_working_time);
            pauseButton = view.findViewById(R.id.wip_task_card_pause_button);
            completeButton = view.findViewById(R.id.wip_task_card_complete_button);
        }

        private void setupViews() {
            wipTaskCardView.setOnClickListener(this);
            pauseButton.setOnClickListener(this);
            completeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.wip_task_card_pause_button:
                    break;

                case R.id.wip_task_card_complete_button:
                    Utilities.showDialog(mFragmentManager, DialogType.COMPLETE_TASK, mDataSet.get(getAdapterPosition()).task.id);
                    break;

                case R.id.wip_task_card_view:
                    goToTaskLogActivity(mDataSet.get(getAdapterPosition()).task.id);
                    break;
            }
        }
    }

    private class ScheduledTaskViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public TextView taskIndex;
        public TextView taskName;
        public TextView caseName;

        public ScheduledTaskViewHolder(View view) {
            super(view);
            findViews(view);
            setupViews();
        }

        private void findViews(View view) {
            this.view = view;
            taskIndex = (TextView) view.findViewById(R.id.scheduled_task_index);
            taskName = (TextView) view.findViewById(R.id.scheduled_task_task_name);
            caseName = (TextView) view.findViewById(R.id.scheduled_task_case_name);
        }

        private void setupViews() {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utilities.showDialog(mFragmentManager, DialogType.CHOOSE_TASK, mDataSet.get(getAdapterPosition()).task.id);
                }
            });
        }
    }


    public TasksListAdapter(Context context, FragmentManager fm, List<TasksListItem> dataSet) {
        mContext = context;
        mFragmentManager = fm;
        mDataSet = dataSet;
    }

    private void goToTaskLogActivity(String taskId) {
        Intent intent = new Intent(mContext, TaskLogActivity.class);
        intent.putExtra(TaskLogActivity.EXTRA_TASK_ID, taskId);

        mContext.startActivity(intent);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ItemViewType.TITLE:
                return new TitleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.task_list_title, parent, false));

            case ItemViewType.WIP_TASK:
                return new WipTaskViewHolder(LayoutInflater.from(mContext).inflate(R.layout.wip_task_card, parent, false));

            case ItemViewType.SCHEDULED_TASK:
                return new ScheduledTaskViewHolder(LayoutInflater.from(mContext).inflate(R.layout.scheduled_task_card,
                                                   parent, false));

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null) return;

        Task task = mDataSet.get(position).task;

        switch (mDataSet.get(position).itemViewType) {
            case ItemViewType.TITLE:
                bindTitleViewHolder((TitleViewHolder) holder, task);
                break;

            case ItemViewType.WIP_TASK:
                bindWipTaskViewHolder((WipTaskViewHolder) holder, task);
                break;

            case ItemViewType.SCHEDULED_TASK:
                bindScheduledTaskViewHolder((ScheduledTaskViewHolder) holder, task, position);
                break;
        }
    }

    private void bindTitleViewHolder(TitleViewHolder holder, Task task) {
        holder.title.setText(task.name);
    }

    private void bindWipTaskViewHolder(WipTaskViewHolder holder, Task task) {
        if (task == null) {
            holder.wipTaskCardView.setVisibility(View.GONE);
            holder.noWipTaskCardView.setVisibility(View.VISIBLE);

            holder.chooseTaskText.setVisibility(View.VISIBLE);

        } else {
            holder.wipTaskCardView.setVisibility(View.VISIBLE);
            holder.noWipTaskCardView.setVisibility(View.GONE);

            holder.wipTaskName.setText(task.name);
            holder.wipCaseName.setText(task.caseName);
            holder.wipWorkingTime.setText(Utilities.millisecondsToTimeString(task.getWorkingTime()));
        }
    }

    private void bindScheduledTaskViewHolder(ScheduledTaskViewHolder holder, Task task, int position) {
        holder.taskIndex.setText(String.valueOf(position - 2));
        holder.taskName.setText(task.name);
        holder.caseName.setText(task.caseName);
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType = mDataSet.get(position).itemViewType;

        if (ItemViewType.TITLE == itemViewType || ItemViewType.TITLE == itemViewType) {
            return ItemViewType.TITLE;

        } else if (ItemViewType.WIP_TASK == itemViewType) {
            return ItemViewType.WIP_TASK;

        } else {
            return ItemViewType.SCHEDULED_TASK;
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void onCompleteTaskOk(String taskId) {
        // Complete task here
        Toast.makeText(mContext,
                "Complete task " + WorkingData.getInstance(mContext).getTask(taskId).name, Toast.LENGTH_SHORT).show();
        Utilities.dismissDialog(mFragmentManager);
    }

    public void onCompleteTaskCancel() {
        Utilities.dismissDialog(mFragmentManager);
    }

    public void onChooseTaskStartWork() {
        Utilities.dismissDialog(mFragmentManager);
    }

    public void onChooseTaskLog(String taskId) {
        goToTaskLogActivity(taskId);
        Utilities.dismissDialog(mFragmentManager);
    }
}
