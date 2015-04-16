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

package com.grarak.kerneladiutor.fragments;

import android.content.res.Configuration;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.grarak.kerneladiutor.MainActivity;
import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

interface IRecyclerView {
    public void preInitCardView();
    public void initCardView(Bundle savedInstanceState);
    public void postInitCardView(Bundle savedInstanceState);
}

/**
 * Created by willi on 22.12.14.
 */
public abstract class RecyclerViewFragment extends BaseFragment implements IRecyclerView {
    protected View view;
    protected LayoutInflater inflater;
    protected ViewGroup container;

    private ProgressBar progressBar;
    private final List<DAdapter.DView> views = new ArrayList<>();
    protected RecyclerView recyclerView;
    protected View applyOnBootLayout;
    protected SwitchCompat applyOnBootView;
    private DAdapter.Adapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private Handler hand;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        this.inflater = inflater;
        this.container = container;

        Log.i(Constants.TAG, "Opening " + getClassName());

        recyclerView = getRecyclerView();
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
        setRecyclerView(recyclerView);
        recyclerView.setPadding(5, 5, 5, 5);

        progressBar = new ProgressBar(getActivity());
        setProgressBar(progressBar);

        if (!showApplyOnBoot()) showApplyOnBoot(false);

        new CardViewTask().execute(savedInstanceState);

        return view;
    }

    protected View getParentView(int layout) {
        return view != null ? view : (view = inflater.inflate(layout, container, false));
    }

    public RecyclerView getRecyclerView() {
        if (showApplyOnBoot()) {
            applyOnBootView = (SwitchCompat) getParentView(R.layout.recyclerview_vertical).findViewById(R.id.apply_on_boot_view);
            applyOnBootView.setChecked(Utils.getBoolean(getClassName() + "onboot", false, getActivity()));
            applyOnBootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activateApplyOnBoot(applyOnBootView.isChecked());
                }
            });

            applyOnBootLayout = getParentView(R.layout.recyclerview_vertical).findViewById(R.id.apply_on_boot_layout);
            applyOnBootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyOnBootView.setChecked(!applyOnBootView.isChecked());
                    activateApplyOnBoot(applyOnBootView.isChecked());
                }
            });
        }

        return (RecyclerView) getParentView(R.layout.recyclerview_vertical).findViewById(R.id.recycler_view);
    }

    private void activateApplyOnBoot(boolean active) {
        Utils.saveBoolean(getClassName() + "onboot", active, getActivity());
        Utils.toast(getString(active ? R.string.apply_on_boot_enabled : R.string.apply_on_boot_disabled,
                getActionBar().getTitle()), getActivity());
    }

    public String getClassName() {
        return getClass().getSimpleName();
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        layoutManager = new StaggeredGridLayoutManager(getSpan(), StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void setProgressBar(ProgressBar progressBar) {
        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000,
                getResources().getColor(android.R.color.white)));
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(progressBar, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.END));
    }

    public void addView(DAdapter.DView view) {
        if (views.indexOf(view) < 0) {
            views.add(view);
            adapter.notifyDataSetChanged();
        }
    }

    public void removeView(DAdapter.DView view) {
        int position = views.indexOf(view);
        if (position > -1) {
            views.remove(position);
            adapter.notifyDataSetChanged();
        }
    }

    public void removeAllViews() {
        views.clear();
        adapter.notifyDataSetChanged();
    }

    public void addAllViews(List<DAdapter.DView> views) {
        this.views.addAll(views);
        adapter.notifyDataSetChanged();
    }

    public int getCount() {
        return views.size();
    }

    public void animateRecyclerView() {
        try {
            recyclerView.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.recyclerview));
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        layoutManager.setSpanCount(getSpan());
    }

    public ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public boolean showApplyOnBoot() {
        return true;
    }

    public void showApplyOnBoot(boolean visible) {
        try {
            getParentView(R.layout.recyclerview_vertical).findViewById(R.id.apply_on_boot_layout).setVisibility(
                    visible ? View.VISIBLE : View.GONE);
        } catch (NullPointerException ignored) {
        }
    }

    public int getSpan() {
        int orientation = Utils.getScreenOrientation(getActivity());
        if (Utils.isTablet(getActivity()))
            return orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
        return orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 2;
    }

    public Handler getHandler() {
        return hand;
    }

    public boolean onRefresh() {
        return false;
    }

    private final Runnable run = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                if (onRefresh()) {
                    if (hand != null) hand.postDelayed(run, 1000);
                } else if (hand != null) hand.removeCallbacks(run);
            } else if (hand != null) hand.postDelayed(run, 1000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hand != null) hand.removeCallbacks(run);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private class CardViewTask extends AsyncTask<Bundle, Bundle, Bundle> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hand = new Handler();
                }
            });
            views.clear();
            adapter = new DAdapter.Adapter(views);
            try {
                if (isAdded()) preInitCardView();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Bundle doInBackground(Bundle... params) {
            Bundle savedInstanceState = params[0];
            try {
                if (isAdded()) initCardView(savedInstanceState);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            return savedInstanceState;
        }

        @Override
        protected void onPostExecute(Bundle savedInstanceState) {
            super.onPostExecute(savedInstanceState);

            recyclerView.setAdapter(adapter);
            animateRecyclerView();
            if (hand != null) hand.post(run);

            try {
                ((ViewGroup) progressBar.getParent()).removeView(progressBar);
            } catch (NullPointerException ignored) {
            }
            try {
                if (isAdded()) postInitCardView(savedInstanceState);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

}
