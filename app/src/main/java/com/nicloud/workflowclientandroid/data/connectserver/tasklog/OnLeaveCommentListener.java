package com.nicloud.workflowclientandroid.data.connectserver.tasklog;

/**
 * Created by logicmelody on 2015/11/23.
 */
public interface OnLeaveCommentListener {
    void onFinishLeaveComment();
    void onFailLeaveComment(boolean isFailCausedByInternet);
}