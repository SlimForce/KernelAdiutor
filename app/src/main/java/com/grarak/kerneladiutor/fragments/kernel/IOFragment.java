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

import android.content.Context;
import android.os.Bundle;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.CardViewItem;
import com.grarak.kerneladiutor.elements.DividerCardView;
import com.grarak.kerneladiutor.elements.PopupCardItem;
import com.grarak.kerneladiutor.fragments.PathReaderFragment;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.fragments.ViewPagerFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.IO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 11.04.15.
 */
public class IOFragment extends ViewPagerFragment implements Constants {

    private static IOFragment ioFragment;
    private static SchedulerPart schedulerPart;
    private static IO.StorageType storageType;

    private String mTitle;

    @Override
    public void initCardView(Bundle savedInstanceState) {
        ioFragment = this;

        schedulerPart = new SchedulerPart();
        allowSwipe(false);
        addFragment(new IOPart());
        addFragment(schedulerPart);

        mTitle = getActionBar().getTitle().toString();
    }

    @Override
    public void onSwipe(int page) {
        if (page == 0) {
            getActionBar().setTitle(mTitle);
            allowSwipe(false);
        } else if (page == 1) {
            getActionBar().setTitle(IO.getScheduler(storageType == IO.StorageType.INTERNAL ? IO.StorageType.INTERNAL :
                    IO.StorageType.EXTERNAL));
            allowSwipe(true);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getCurrentPage() == 1) {
            setCurrentItem(0);
            return true;
        }
        return false;
    }

    public static class IOPart extends RecyclerViewFragment implements PopupCardItem.DPopupCard.OnDPopupCardListener,
            CardViewItem.DCardView.OnDCardListener {

        List<String> readheads = new ArrayList<>();

        private PopupCardItem.DPopupCard mInternalSchedulerCard, mExternalSchedulerCard;

        private CardViewItem.DCardView mInternalTunableCard, mExternalTunableCard;

        private PopupCardItem.DPopupCard mInternalReadAheadCard, mExternalReadAheadCard;

        @Override
        public boolean showApplyOnBoot() {
            return false;
        }

        @Override
        public void preInitCardView() {
        }

        @Override
        public void postInitCardView(Bundle savedInstanceState) {
        }

        @Override
        public void initCardView(Bundle savedInstanceState) {
            readheads.clear();
            internalStorageInit();
            if (IO.hasExternalStorage())
                externalStorageInit();
        }

        private void internalStorageInit() {
            DividerCardView.DDividerCard mInternalStorageDivider = new DividerCardView.DDividerCard();
            mInternalStorageDivider.setText(getString(R.string.internal_storage));

            addView(mInternalStorageDivider);

            mInternalSchedulerCard = new PopupCardItem.DPopupCard(IO.getSchedulers(IO.StorageType.INTERNAL));
            mInternalSchedulerCard.setTitle(getString(R.string.scheduler));
            mInternalSchedulerCard.setDescription(getString(R.string.scheduler_summary));
            mInternalSchedulerCard.setItem(IO.getScheduler(IO.StorageType.INTERNAL));
            mInternalSchedulerCard.setOnDPopupCardListener(this);

            addView(mInternalSchedulerCard);

            mInternalTunableCard = new CardViewItem.DCardView();
            mInternalTunableCard.setTitle(getString(R.string.scheduler_tunable));
            mInternalTunableCard.setDescription(getString(R.string.scheduler_tunable_summary));
            mInternalTunableCard.setOnDCardListener(this);

            addView(mInternalTunableCard);

            for (int i = 0; i < 32; i++)
                readheads.add((i * 128 + 128) + getString(R.string.kb));

            mInternalReadAheadCard = new PopupCardItem.DPopupCard(readheads);
            mInternalReadAheadCard.setTitle(getString(R.string.read_ahead));
            mInternalReadAheadCard.setDescription(getString(R.string.read_ahead_summary));
            mInternalReadAheadCard.setItem(IO.getReadahead(IO.StorageType.INTERNAL) + getString(R.string.kb));
            mInternalReadAheadCard.setOnDPopupCardListener(this);

            addView(mInternalReadAheadCard);
        }

        private void externalStorageInit() {
            DividerCardView.DDividerCard mExternalStorageDivider = new DividerCardView.DDividerCard();
            mExternalStorageDivider.setText(getString(R.string.external_storage));

            addView(mExternalStorageDivider);

            mExternalSchedulerCard = new PopupCardItem.DPopupCard(IO.getSchedulers(IO.StorageType.EXTERNAL));
            mExternalSchedulerCard.setDescription(getString(R.string.scheduler));
            mExternalSchedulerCard.setItem(IO.getScheduler(IO.StorageType.EXTERNAL));
            mExternalSchedulerCard.setOnDPopupCardListener(this);

            addView(mExternalSchedulerCard);

            mExternalTunableCard = new CardViewItem.DCardView();
            mExternalTunableCard.setDescription(getString(R.string.scheduler_tunable));
            mExternalTunableCard.setOnDCardListener(this);

            addView(mExternalTunableCard);

            mExternalReadAheadCard = new PopupCardItem.DPopupCard(readheads);
            mExternalReadAheadCard.setDescription(getString(R.string.read_ahead));
            mExternalReadAheadCard.setItem(IO.getReadahead(IO.StorageType.EXTERNAL) + getString(R.string.kb));
            mExternalReadAheadCard.setOnDPopupCardListener(this);

            addView(mExternalReadAheadCard);
        }

        @Override
        public void onItemSelected(PopupCardItem.DPopupCard dPopupCard, int position) {
            if (dPopupCard == mInternalSchedulerCard)
                IO.setScheduler(IO.StorageType.INTERNAL, IO.getSchedulers(IO.StorageType.INTERNAL)
                        .get(position), getActivity());
            else if (dPopupCard == mExternalSchedulerCard)
                IO.setScheduler(IO.StorageType.EXTERNAL, IO.getSchedulers(IO.StorageType.EXTERNAL)
                        .get(position), getActivity());
            else if (dPopupCard == mInternalReadAheadCard)
                IO.setReadahead(IO.StorageType.INTERNAL, Utils.stringToInt(readheads.get(position)
                        .replace(getString(R.string.kb), "")), getActivity());
            else if (dPopupCard == mExternalReadAheadCard)
                IO.setReadahead(IO.StorageType.EXTERNAL, Utils.stringToInt(readheads.get(position)
                        .replace(getString(R.string.kb), "")), getActivity());
        }

        @Override
        public void onClick(CardViewItem.DCardView dCardView) {
            storageType = dCardView == mInternalTunableCard ? IO.StorageType.INTERNAL : IO.StorageType.EXTERNAL;
            schedulerPart.reload();
            ioFragment.setCurrentItem(1);
        }

    }

    public static class SchedulerPart extends PathReaderFragment {

        @Override
        public String getPath() {
            return storageType == IO.StorageType.INTERNAL ? IO_INTERNAL_SCHEDULER_TUNABLE : IO_EXTERNAL_SCHEDULER_TUNABLE;
        }

        @Override
        public PATH_TYPE getType() {
            return PATH_TYPE.IO;
        }

        @Override
        public String getError(Context context) {
            return context.getString(R.string.not_tunable, IO.getScheduler(storageType == IO.StorageType.INTERNAL ?
                    IO.StorageType.INTERNAL : IO.StorageType.EXTERNAL));
        }

    }

}
