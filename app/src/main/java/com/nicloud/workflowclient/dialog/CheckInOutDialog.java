package com.nicloud.workflowclient.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.nicloud.workflowclient.data.connectserver.worker.CheckInOutCommand;
import com.nicloud.workflowclient.data.connectserver.worker.LoadingLoginWorkerCommand;
import com.nicloud.workflowclient.data.data.data.Worker;
import com.nicloud.workflowclient.data.data.data.WorkingData;
import com.nicloud.workflowclient.googlelocation.CurrentAddress;
import com.nicloud.workflowclient.googlelocation.GoogleLocationUtils;
import com.nicloud.workflowclient.main.main.MainApplication;
import com.nicloud.workflowclient.R;
import com.nicloud.workflowclient.googlelocation.AddressResultReceiver;
import com.nicloud.workflowclient.googlelocation.FetchAddressIntentService;
import com.nicloud.workflowclient.dialog.DisplayDialogFragment.OnDialogActionListener;
import com.nicloud.workflowclient.utility.Utilities;

/**
 * Dialog for checking in/out
 *
 * Do not use this class directly, if you want to display the dialog, use DisplayDialogFragment.
 *
 * @author Danny Lin
 * @since 2015/11/4.
 */
public class CheckInOutDialog extends Dialog implements View.OnClickListener,
        AddressResultReceiver.OnReceiveListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, CheckInOutCommand.OnDialogCheckInOutStatusListener,
        LoadingLoginWorkerCommand.OnLoadingLoginWorker {

    private Context mContext;

    private OnDialogActionListener mOnDialogActionListener;

    private TextView mCheckDate;
    private TextView mCheckInTime;
    private TextView mCheckOutTime;
    private TextView mCheckInOutButton;
    private TextView mThanksForWorking;
    private TextView mCurrentTime;
    private TextView mLocation;

    private ProgressBar mRecordLocationProgressBar;

    private CurrentAddress mCurrentAddress;

    private Worker mWorker;

    private boolean mFirstReceiveLocation = true;


    public CheckInOutDialog(Context context, OnDialogActionListener listener) {
        super(context);
        mContext = context;
        mOnDialogActionListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_check_in_out);
        initialize();
    }

    private void initialize() {
        mWorker = WorkingData.getInstance(mContext).getLoginWorker();
        findViews();
        setupViews();
        setupCheckInOutTime();
        setupCheckInOutButton();
        setupFetchingAddress();
    }

    private void findViews() {
        mCheckDate = (TextView) findViewById(R.id.check_date);
        mCheckInTime = (TextView) findViewById(R.id.check_in_time);
        mCheckOutTime = (TextView) findViewById(R.id.check_out_time);
        mCheckInOutButton = (TextView) findViewById(R.id.check_button);
        mThanksForWorking = (TextView) findViewById(R.id.check_in_out_thanks_for_working);
        mCurrentTime = (TextView) findViewById(R.id.check_current_time);
        mLocation = (TextView) findViewById(R.id.location_text);
        mRecordLocationProgressBar = (ProgressBar) findViewById(R.id.location_progress_bar);
    }

    private void setupViews() {
        mCheckDate.setText(Utilities.millisecondsToMMDD(mContext, System.currentTimeMillis()));
        mCurrentTime.setText(Utilities.millisecondsToHHMMA(System.currentTimeMillis()));
    }

    private void setupCheckInOutTime() {
        if (mWorker.status != Worker.Status.STOP && mWorker.status != Worker.Status.OFF) {
            mCheckInTime.setTextColor(mContext.getResources().getColor(R.color.dialog_check_time_text_color));
            mCheckInTime.setText(Utilities.millisecondsToHHMMA(mWorker.checkInTime));
        }
    }

    private void setupCheckInOutButton() {
        if (mWorker.status == Worker.Status.OFF || mWorker.status == Worker.Status.STOP) {
            mCheckInOutButton.setText(mContext.getString(R.string.check_in));

        } else {
            mCheckInOutButton.setText(mContext.getString(R.string.check_out));
        }

        mCheckInOutButton.setOnClickListener(this);
    }

    private void setCheckInOutButtonView(Worker.Status status) {
        switch (status) {
            case OFF:
                mCheckInOutButton.setEnabled(false);
                mCheckInOutButton.setTextColor(mContext.getResources().getColor(R.color.dialog_disabled_button_text_color));
                mCheckInOutButton.setText(mContext.getString(R.string.check_in));
                mCheckInOutButton.setBackgroundResource(R.drawable.dialog_disable_button_background);
                break;

            case STOP:
                mCheckInOutButton.setEnabled(true);
                mCheckInOutButton.setTextColor(mContext.getResources().getColor(R.color.dialog_green_button_text_color));
                mCheckInOutButton.setText(mContext.getString(R.string.check_in));
                mCheckInOutButton.setBackgroundResource(R.drawable.dialog_green_button_background);

                break;

            default:
                mCheckInOutButton.setEnabled(true);
                mCheckInOutButton.setTextColor(mContext.getResources().getColor(R.color.dialog_green_button_text_color));
                mCheckInOutButton.setText(mContext.getString(R.string.check_out));
                mCheckInOutButton.setBackgroundResource(R.drawable.dialog_green_button_background);

                break;
        }

    }

    private void setupFetchingAddress() {
        mCurrentAddress = new CurrentAddress(mContext, this, this, this);
        GoogleLocationUtils.showLocationEnabledDialog((Activity) mContext,
                mCurrentAddress.mLocationRequest, mCurrentAddress.mGoogleApiClient);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentAddress.mGoogleApiClient.connect();
        if (mCurrentAddress.mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
        if (mCurrentAddress.mGoogleApiClient.isConnected()) {
            mCurrentAddress.mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnDialogActionListener == null) return;

        switch (v.getId()) {
            case R.id.check_button:
                mOnDialogActionListener.onCheck(mCurrentAddress.mCurrentLocation, mLocation.getText().toString(), this);

                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mCurrentAddress.mGoogleApiClient, mCurrentAddress.mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mCurrentAddress.mGoogleApiClient, this);
    }

    private void startFetchAddressIntentService() {
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mCurrentAddress.mReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.EXTRA_LOCATION_DATA, mCurrentAddress.mCurrentLocation);
        mContext.startService(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mCurrentAddress.mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentAddress.mCurrentLocation = location;

        if (!Geocoder.isPresent()) return;

        startFetchAddressIntentService();
    }

    @Override
    public void onReceiveSuccessful(String message) {
        if (mFirstReceiveLocation) {
            mRecordLocationProgressBar.startAnimation(MainApplication.sFadeOutAnimation);
            mLocation.startAnimation(MainApplication.sFadeInAnimation);

            mRecordLocationProgressBar.setVisibility(View.GONE);
            mLocation.setVisibility(View.VISIBLE);

            setCheckInOutButtonView(mWorker.status);

            mFirstReceiveLocation = false;
        }

        mLocation.setText(message);
    }

    @Override
    public void onReceiveFailed(String message) {

    }

    @Override
    public void onCheckInOutFinished() {
        new LoadingLoginWorkerCommand(mContext, this).execute();
    }

    @Override
    public void onCheckInOutFailed() {

    }

    @Override
    public void onLoadingLoginWorkerSuccessful() {
        if (WorkingData.getInstance(mContext).getLoginWorker().status == Worker.Status.STOP) {
            mCheckOutTime.setTextColor(mContext.getResources().getColor(R.color.dialog_check_time_text_color));
            mCheckOutTime.setText(Utilities.millisecondsToHHMMA(System.currentTimeMillis()));

            mCheckInOutButton.setVisibility(View.GONE);
            mThanksForWorking.startAnimation(MainApplication.sFadeInAnimation);
            mThanksForWorking.setVisibility(View.VISIBLE);

        } else {
            mCheckInTime.setTextColor(mContext.getResources().getColor(R.color.dialog_check_time_text_color));
            mCheckInTime.setText(Utilities.millisecondsToHHMMA(System.currentTimeMillis()));
        }

        setCheckInOutButtonView(WorkingData.getInstance(mContext).getLoginWorker().status);
    }

    @Override
    public void onLoadingLoginWorkerFailed(boolean isFailCausedByInternet) {

    }
}
