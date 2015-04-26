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

package com.grarak.kerneladiutor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.grarak.kerneladiutor.elements.ListAdapter;
import com.grarak.kerneladiutor.elements.SlidingTabLayout;
import com.grarak.kerneladiutor.fragments.BaseFragment;
import com.grarak.kerneladiutor.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by willi on 24.04.15.
 */
public class FileBrowserActivity extends AppCompatActivity {

    public static final String FILE_TYPE_ARG = "file_type";

    private static FileBrowserActivity fileBrowserActivity;
    private ViewPager mViewPager;
    private StorageFragment internalStorage;
    private StorageFragment externalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileBrowserActivity = this;
        boolean dark = Utils.getBoolean("darktheme", false, this);
        if (dark) super.setTheme(R.style.AppThemeDark);
        setContentView(R.layout.activity_filebrowser);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar;
        if ((actionBar = getSupportActionBar()) != null) actionBar.setDisplayHomeAsUpEnabled(true);

        String fileType = getIntent().getExtras().getString(FILE_TYPE_ARG);
        String externalStorage = Utils.getExternalStorage();

        LinkedHashMap<String, Fragment> fragments = new LinkedHashMap<>();
        internalStorage = StorageFragment.newInstance(Environment.getExternalStorageDirectory().toString(), fileType);
        fragments.put(getString(R.string.internal_storage), internalStorage);
        if (externalStorage != null) {
            this.externalStorage = StorageFragment.newInstance(externalStorage, fileType);
            fragments.put(getString(R.string.external_storage), this.externalStorage);
        }

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new Adapter(getSupportFragmentManager(), fragments));
        if (dark)
            mViewPager.setBackgroundColor(getResources().getColor(R.color.card_background_dark));

        final SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        for (int i = 0; i < fragments.keySet().toArray().length; i++)
            mSlidingTabLayout.setContentDescription(i, (String) fragments.keySet().toArray()[i]);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(fragments.size() > 1 ?
                R.color.white : R.color.color_primary));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.color_primary_dark));
            mSlidingTabLayout.setVisibility(View.INVISIBLE);
            mSlidingTabLayout.getRootView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    Utils.circleAnimate(mSlidingTabLayout, 0, 0);
                }
            });
        }
    }

    private class Adapter extends FragmentPagerAdapter {

        private final LinkedHashMap<String, Fragment> fragments;

        public Adapter(FragmentManager fm, LinkedHashMap<String, Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return (Fragment) fragments.values().toArray()[position];
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return (String) fragments.keySet().toArray()[position];
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        boolean finish;
        if (mViewPager.getCurrentItem() == 1) finish = externalStorage.onBackPressed();
        else finish = internalStorage.onBackPressed();
        if (finish) super.onBackPressed();
    }

    protected void finished(String path) {
        Bundle args = new Bundle();
        args.putString("path", path);
        Intent intent = new Intent();
        intent.putExtras(args);
        setResult(RESULT_OK, intent);
        finish();
    }

    public static class StorageFragment extends BaseFragment {

        private static final String PATH_ARG = "path";
        private static final String TYPE_ARG = "type";

        public static StorageFragment newInstance(String path, String type) {
            Bundle args = new Bundle();
            args.putString(PATH_ARG, path);
            if (type != null) args.putString(TYPE_ARG, type);
            StorageFragment storageFragment = new StorageFragment();
            storageFragment.setArguments(args);
            return storageFragment;
        }

        private TextView pathText;
        private List<ListAdapter.ListItem> files = new ArrayList<>();
        private ListAdapter.Adapter adapter;
        private String current_path;
        private String type;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            current_path = getArguments().getString(PATH_ARG);
            type = getArguments().getString(TYPE_ARG);

            View view = inflater.inflate(R.layout.fragment_storage, container, false);

            pathText = (TextView) view.findViewById(R.id.path_text);
            ListView listView = (ListView) view.findViewById(R.id.list);
            adapter = new ListAdapter.Adapter(getActivity(), files);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final File file = ((FileItem) files.get(position)).getFile();
                    if (file.isDirectory()) {
                        current_path = file.getAbsolutePath();
                        updateData();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(getString(R.string.select_file, file.getName()))
                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        fileBrowserActivity.finished(file.getAbsolutePath());
                                    }
                                }).show();
                    }
                }
            });

            updateData();
            return view;
        }

        private void updateData() {
            this.files.clear();

            List<File> files = new ArrayList<>();
            List<File> folders = new ArrayList<>();
            File[] storageFiles = new File(current_path).listFiles();
            if (storageFiles == null || storageFiles.length == 0) {
                return;
            }
            for (File file : storageFiles) {
                if (file.isDirectory()) folders.add(file);
                else if (type != null) {
                    if (file.getName().endsWith("." + type)) files.add(file);
                } else files.add(file);
            }

            Collections.sort(files);
            Collections.sort(folders);

            for (File folder : folders) this.files.add(new FileItem(folder));
            for (File file : files) this.files.add(new FileItem(file));

            pathText.setText(current_path);
            adapter.notifyDataSetChanged();
        }

        @Override
        public boolean onBackPressed() {
            if (!current_path.equals(getArguments().getString(PATH_ARG))) {
                current_path = getPreviousPath(current_path);
                updateData();
                return false;
            }
            return true;
        }

        private String getPreviousPath(String path) {
            return new File(path).getParentFile().toString();
        }

        private class FileItem implements ListAdapter.ListItem {

            private final File file;

            public FileItem(File file) {
                this.file = file;
            }

            @Override
            public View getView(LayoutInflater inflater, ViewGroup parent) {
                TextView view = (TextView) inflater.inflate(R.layout.filebrowser_item, parent, false);
                view.setText(file.getName());
                view.setCompoundDrawablesWithIntrinsicBounds(file.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file,
                        0, 0, 0);
                return view;
            }

            @Override
            public BaseFragment getFragment() {
                return null;
            }

            @Override
            public String getTitle() {
                return null;
            }

            public File getFile() {
                return file;
            }

        }
    }

}
