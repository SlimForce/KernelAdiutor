/*
 * Copyright (C) 2015 Willi Ye
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grarak.kerneladiutor.fragments.kernel;

import android.os.Bundle;
import android.util.Log;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DividerCardView;
import com.grarak.kerneladiutor.elements.EditTextCardView;
import com.grarak.kerneladiutor.elements.PopupCardItem;
import com.grarak.kerneladiutor.elements.SeekBarCardView;
import com.grarak.kerneladiutor.elements.SwitchCompatCardItem;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.Misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by willi on 02.01.15.
 */
public class MiscFragment extends RecyclerViewFragment implements PopupCardItem.DPopupCard.OnDPopupCardListener,
        SeekBarCardView.DSeekBarCardView.OnDSeekBarCardListener,
        SwitchCompatCardItem.DSwitchCompatCard.OnDSwitchCompatCardListener,
        EditTextCardView.DEditTextCard.OnDEditTextCardListener {

    private SeekBarCardView.DSeekBarCardView mVibrationCard;

    private SwitchCompatCardItem.DSwitchCompatCard mSmb135xWakeLockCard;
    private SwitchCompatCardItem.DSwitchCompatCard mSensorIndWakeLockCard;
    private SwitchCompatCardItem.DSwitchCompatCard mMsmHsicHostWakeLockCard;

    private SwitchCompatCardItem.DSwitchCompatCard mLoggerEnableCard;

    private PopupCardItem.DPopupCard mSelinuxCard;

    private SwitchCompatCardItem.DSwitchCompatCard mFsyncCard;
    private SwitchCompatCardItem.DSwitchCompatCard mDynamicFsyncCard;

    private PopupCardItem.DPopupCard mPowerSuspendModeCard;
    private SwitchCompatCardItem.DSwitchCompatCard mOldPowerSuspendStateCard;
    private SeekBarCardView.DSeekBarCardView mNewPowerSuspendStateCard;

    private PopupCardItem.DPopupCard mTcpCongestionCard;
    private EditTextCardView.DEditTextCard mHostnameCard;

    @Override
    public void preInitCardView() {
    }

    @Override
    public void postInitCardView(Bundle savedInstanceState) {
    }

    @Override
    public void initCardView(Bundle savedInstanceState) {
        if (Misc.hasVibration()) vibrationInit();
        if (Misc.hasSmb135xWakeLock()) smb135xWakeLockInit();
        if (Misc.hasSensorIndWakeLock()) sensorIndWakeLockInit();
        if (Misc.hasMsmHsicHostWakeLock()) msmHsicHostWakeLockInit();
        if (Misc.hasLoggerEnable()) loggerInit();
        if (Misc.hasSelinux()) selinuxInit();
        fsyncInit();
        if (Misc.hasPowerSuspend()) powersuspendInit();
        networkInit();
    }

    private void vibrationInit() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 101; i++)
            list.add(i + "%");

        int max = Misc.getVibrationMax();
        int min = Misc.getVibrationMin();
        float offset = (max - min) / (float) 101;

        mVibrationCard = new SeekBarCardView.DSeekBarCardView(list);
        mVibrationCard.setTitle(getString(R.string.vibration_strength));
        mVibrationCard.setProgress(Math.round((Misc.getCurVibration() - min) / offset));
        mVibrationCard.setOnDSeekBarCardListener(this);

        addView(mVibrationCard);
    }

    private void smb135xWakeLockInit() {
        mSmb135xWakeLockCard = new SwitchCompatCardItem.DSwitchCompatCard();
        mSmb135xWakeLockCard.setTitle(getString(R.string.smb135x_wakelock));
        mSmb135xWakeLockCard.setDescription(getString(R.string.smb135x_wakelock_summary));
        mSmb135xWakeLockCard.setChecked(Misc.isSmb135xWakeLockActive());
        mSmb135xWakeLockCard.setOnDSwitchCompatCardListener(this);

        addView(mSmb135xWakeLockCard);
    }

    private void sensorIndWakeLockInit() {
        mSensorIndWakeLockCard = new SwitchCompatCardItem.DSwitchCompatCard();
        mSensorIndWakeLockCard.setTitle(getString(R.string.sensor_ind_wakelock));
        mSensorIndWakeLockCard.setDescription(getString(R.string.sensor_ind_wakelock_summary));
        mSensorIndWakeLockCard.setChecked(Misc.isSensorIndWakeLockActive());
        mSensorIndWakeLockCard.setOnDSwitchCompatCardListener(this);

        addView(mSensorIndWakeLockCard);
    }

    private void msmHsicHostWakeLockInit() {
        mMsmHsicHostWakeLockCard = new SwitchCompatCardItem.DSwitchCompatCard();
        mMsmHsicHostWakeLockCard.setTitle(getString(R.string.msm_hsic_host_wakelock));
        mMsmHsicHostWakeLockCard.setDescription(getString(R.string.msm_hsic_host_wakelock_summary));
        mMsmHsicHostWakeLockCard.setChecked(Misc.isMsmHsicHostWakeLockActive());
        mMsmHsicHostWakeLockCard.setOnDSwitchCompatCardListener(this);

        addView(mMsmHsicHostWakeLockCard);
    }

    private void loggerInit() {
        mLoggerEnableCard = new SwitchCompatCardItem.DSwitchCompatCard();
        mLoggerEnableCard.setDescription(getString(R.string.android_logger));
        mLoggerEnableCard.setChecked(Misc.isLoggerActive());
        mLoggerEnableCard.setOnDSwitchCompatCardListener(this);

        addView(mLoggerEnableCard);
    }

    private void selinuxInit() {
        String[] items = getResources().getStringArray(R.array.selinux_items);
        mSelinuxCard = new PopupCardItem.DPopupCard(new ArrayList<>(Arrays.asList(items)));
        mSelinuxCard.setTitle(getString(R.string.selinux));
        mSelinuxCard.setDescription(getString(R.string.selinux_summary));
        mSelinuxCard.setItem(Misc.getSelinux());
        mSelinuxCard.setOnDPopupCardListener(this);

        addView(mSelinuxCard);
    }

    private void fsyncInit() {
        if (Misc.hasFsync()) {
            mFsyncCard = new SwitchCompatCardItem.DSwitchCompatCard();
            mFsyncCard.setTitle(getString(R.string.fsync));
            mFsyncCard.setDescription(getString(R.string.fsync_summary));
            mFsyncCard.setChecked(Misc.isFsyncActive());
            mFsyncCard.setOnDSwitchCompatCardListener(this);

            addView(mFsyncCard);
        }

        if (Misc.hasDynamicFsync()) {
            mDynamicFsyncCard = new SwitchCompatCardItem.DSwitchCompatCard();
            mDynamicFsyncCard.setTitle(getString(R.string.dynamic_fsync));
            mDynamicFsyncCard.setDescription(getString(R.string.dynamic_fsync_summary));
            mDynamicFsyncCard.setChecked(Misc.isDynamicFsyncActive());
            mDynamicFsyncCard.setOnDSwitchCompatCardListener(this);

            addView(mDynamicFsyncCard);
        }
    }


    private void powersuspendInit() {
        if (Misc.hasPowerSuspendMode()) {
            mPowerSuspendModeCard = new PopupCardItem.DPopupCard(new ArrayList<>(
                    Arrays.asList(getResources().getStringArray(R.array.powersuspend_items))));
            mPowerSuspendModeCard.setTitle(getString(R.string.power_suspend_mode));
            mPowerSuspendModeCard.setDescription(getString(R.string.power_suspend_mode_summary));
            mPowerSuspendModeCard.setItem(Misc.getPowerSuspendMode());
            mPowerSuspendModeCard.setOnDPopupCardListener(this);

            addView(mPowerSuspendModeCard);
        }

        if (Misc.hasOldPowerSuspendState()) {
            mOldPowerSuspendStateCard = new SwitchCompatCardItem.DSwitchCompatCard();
            mOldPowerSuspendStateCard.setTitle(getString(R.string.power_suspend_state));
            mOldPowerSuspendStateCard.setDescription(getString(R.string.power_suspend_state_summary));
            mOldPowerSuspendStateCard.setChecked(Misc.isOldPowerSuspendStateActive());
            mOldPowerSuspendStateCard.setOnDSwitchCompatCardListener(this);

            addView(mOldPowerSuspendStateCard);
        }

        if (Misc.hasNewPowerSuspendState()) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < 3; i++)
                list.add(String.valueOf(i));

            mNewPowerSuspendStateCard = new SeekBarCardView.DSeekBarCardView(list);
            mNewPowerSuspendStateCard.setTitle(getString(R.string.power_suspend_state));
            mNewPowerSuspendStateCard.setDescription(getString(R.string.power_suspend_state_summary));
            mNewPowerSuspendStateCard.setProgress(Misc.getNewPowerSuspendState());
            mNewPowerSuspendStateCard.setOnDSeekBarCardListener(this);

            addView(mNewPowerSuspendStateCard);
        }
    }

    private void networkInit() {
        DividerCardView.DDividerCard mNetworkDividerCard = new DividerCardView.DDividerCard();
        mNetworkDividerCard.setText(getString(R.string.network));
        addView(mNetworkDividerCard);

        try {
            mTcpCongestionCard = new PopupCardItem.DPopupCard(Misc.getTcpAvailableCongestions());
            mTcpCongestionCard.setTitle(getString(R.string.tcp));
            mTcpCongestionCard.setDescription(getString(R.string.tcp_summary));
            mTcpCongestionCard.setItem(Misc.getCurTcpCongestion());
            mTcpCongestionCard.setOnDPopupCardListener(this);

            addView(mTcpCongestionCard);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Failed to read TCP");
        }

        String hostname = Misc.getHostname();
        mHostnameCard = new EditTextCardView.DEditTextCard();
        mHostnameCard.setTitle(getString(R.string.hostname));
        mHostnameCard.setDescription(hostname);
        mHostnameCard.setValue(hostname);
        mHostnameCard.setOnDEditTextCardListener(this);

        addView(mHostnameCard);
    }

    @Override
    public void onItemSelected(PopupCardItem.DPopupCard dPopupCard, int position) {
        if (dPopupCard == mTcpCongestionCard)
            Misc.setTcpCongestion(Misc.getTcpAvailableCongestions().get(position), getActivity());
        else if (dPopupCard == mSelinuxCard) Misc.setSelinux(position, getActivity());
        else if (dPopupCard == mPowerSuspendModeCard)
            Misc.setPowerSuspendMode(position, getActivity());
    }

    @Override
    public void onChanged(SeekBarCardView.DSeekBarCardView dSeekBarCardView, int position) {
    }

    @Override
    public void onStop(SeekBarCardView.DSeekBarCardView dSeekBarCardView, int position) {
        if (dSeekBarCardView == mVibrationCard) {
            int max = Misc.getVibrationMax();
            int min = Misc.getVibrationMin();
            float offset = (max - min) / (float) 101;
            Misc.setVibration(Math.round(offset * position) + min, getActivity());

            // Vibrate
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        Utils.vibrate(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else if (dSeekBarCardView == mNewPowerSuspendStateCard)
            if (Misc.getPowerSuspendMode() == 1) {
                Misc.setNewPowerSuspend(position, getActivity());
            } else dSeekBarCardView.setProgress(Misc.getNewPowerSuspendState());
    }

    @Override
    public void onChecked(SwitchCompatCardItem.DSwitchCompatCard dSwitchCompatCard, boolean checked) {
        if (dSwitchCompatCard == mSmb135xWakeLockCard)
            Misc.activateSmb135xWakeLock(checked, getActivity());
        else if (dSwitchCompatCard == mSensorIndWakeLockCard)
            Misc.activateSensorIndWakeLock(checked, getActivity());
        else if (dSwitchCompatCard == mMsmHsicHostWakeLockCard)
            Misc.activateMsmHsicHostWakeLock(checked, getActivity());
        else if (dSwitchCompatCard == mLoggerEnableCard)
            Misc.activateLogger(checked, getActivity());
        else if (dSwitchCompatCard == mFsyncCard)
            Misc.activateFsync(checked, getActivity());
        else if (dSwitchCompatCard == mDynamicFsyncCard)
            Misc.activateDynamicFsync(checked, getActivity());
        else if (dSwitchCompatCard == mOldPowerSuspendStateCard)
            if (Misc.getPowerSuspendMode() == 1) {
                Misc.activateOldPowerSuspend(checked, getActivity());
            } else dSwitchCompatCard.setChecked(Misc.isOldPowerSuspendStateActive());
    }

    @Override
    public void onApply(EditTextCardView.DEditTextCard dEditTextCard, String value) {
        dEditTextCard.setDescription(value);
        if (dEditTextCard == mHostnameCard) Misc.setHostname(value, getActivity());
    }
}
