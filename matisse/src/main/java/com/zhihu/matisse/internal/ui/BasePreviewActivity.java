/*
 * Copyright 2017 Zhihu Inc.
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
package com.zhihu.matisse.internal.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.internal.model.AlbumCollection;
import com.zhihu.matisse.internal.model.SelectedItemCollection;
import com.zhihu.matisse.internal.ui.adapter.PreviewPagerAdapter;
import com.zhihu.matisse.internal.ui.widget.CheckRadioView;
import com.zhihu.matisse.internal.ui.widget.CheckView;
import com.zhihu.matisse.internal.ui.widget.IncapableDialog;
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils;
import com.zhihu.matisse.internal.utils.Platform;
import com.zhihu.matisse.listener.OnFragmentInteractionListener;

public abstract class BasePreviewActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, OnFragmentInteractionListener {

    public static final String EXTRA_DEFAULT_BUNDLE = "extra_default_bundle";
    public static final String EXTRA_RESULT_BUNDLE = "extra_result_bundle";
    public static final String EXTRA_RESULT_APPLY = "extra_result_apply";
    public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    public static final String CHECK_STATE = "checkState";

    protected final SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    protected SelectionSpec mSpec;
    protected ViewPager mPager;

    protected PreviewPagerAdapter mAdapter;

   protected CheckView mCheckView;
   // protected CheckBox viewCheck;
    protected ImageView imageCheck;
    protected TextView mButtonBack;
    protected TextView mButtonApply;
    protected TextView mSize;

    protected int mPreviousPos = -1;

    private LinearLayout mOriginalLayout;
    private CheckRadioView mOriginal;
    protected boolean mOriginalEnable;

    private FrameLayout mBottomToolbar;
    private LinearLayout mTopToolbar;
    private boolean mIsToolbarHide = false;
    private TextView tvCheck;
    protected TextView mSelectedAlbum;
    private final AlbumCollection mAlbumCollection = new AlbumCollection();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(SelectionSpec.getInstance().themeId);
        super.onCreate(savedInstanceState);
        if (!SelectionSpec.getInstance().hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        setContentView(R.layout.activity_media_preview);
        if (Platform.hasKitKat()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        mSpec = SelectionSpec.getInstance();
        if (mSpec.needOrientationRestriction()) {
            setRequestedOrientation(mSpec.orientation);
        }

        if (savedInstanceState == null) {
            mSelectedCollection.onCreate(getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE));
            mOriginalEnable = getIntent().getBooleanExtra(EXTRA_RESULT_ORIGINAL_ENABLE, false);
        } else {
            mSelectedCollection.onCreate(savedInstanceState);
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Drawable navigationIcon = toolbar.getNavigationIcon();
        TypedArray ta = getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        mButtonBack = (TextView) findViewById(R.id.button_back);
        mButtonApply = (TextView) findViewById(R.id.button_apply);
        mSize = (TextView) findViewById(R.id.size);
        mButtonBack.setOnClickListener(this);
        mButtonApply.setOnClickListener(this);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.addOnPageChangeListener(this);
        mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null);
        mPager.setAdapter(mAdapter);
        mCheckView = (CheckView) findViewById(R.id.check_view);
     //   viewCheck = (CheckBox) findViewById(R.id.viewCheck);
        imageCheck = (ImageView) findViewById(R.id.imageCheck);
        mCheckView.setCountable(mSpec.countable);
        Log.d("mSpec.countable",mSpec.countable+"");
        //viewCheck.setChecked(mSpec.countable);
        mBottomToolbar = findViewById(R.id.bottom_toolbar);
        mTopToolbar = findViewById(R.id.top_toolbar);
        tvCheck = (TextView)findViewById(R.id.tv_check);
        mSelectedAlbum = (TextView)findViewById(R.id.selected_album);
/*
        viewCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkItemStatus();
            }
        });
*/
        mTopToolbar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkItemStatus();
            }
        });
        mCheckView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Item item = mAdapter.getMediaItem(mPager.getCurrentItem());
                if (mSelectedCollection.isSelected(item)) {
                    mSelectedCollection.remove(item);
                    if (mSpec.countable) {
                        mCheckView.setCheckedNum(CheckView.UNCHECKED);
                    } else {
                        mCheckView.setChecked(false);
                    }
                } else {
                    if (assertAddSelection(item)) {
                        mSelectedCollection.add(item);
                        if (mSpec.countable) {
                            mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
                        } else {
                            mCheckView.setChecked(true);
                        }
                    }
                }
                updateApplyButton();

                if (mSpec.onSelectedListener != null) {
                    mSpec.onSelectedListener.onSelected(
                            mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
                }
            }
        });


        mOriginalLayout = findViewById(R.id.originalLayout);
        mOriginal = findViewById(R.id.original);
        mOriginalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int count = countOverMaxSize();
                if (count > 0) {
                    IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                            getString(R.string.error_over_original_count, count, mSpec.originalMaxSize));
                    incapableDialog.show(getSupportFragmentManager(),
                            IncapableDialog.class.getName());
                    return;
                }

                mOriginalEnable = !mOriginalEnable;
                mOriginal.setChecked(mOriginalEnable);
                if (!mOriginalEnable) {
                    mOriginal.setColor(Color.WHITE);
                }


                if (mSpec.onCheckedListener != null) {
                    mSpec.onCheckedListener.onCheck(mOriginalEnable);
                }
            }
        });

        updateApplyButton();
    }

    private void checkItemStatus() {
        Item item = mAdapter.getMediaItem(mPager.getCurrentItem());
        if (mSelectedCollection.isSelected(item)) {
            mSelectedCollection.remove(item);
            if (mSpec.countable) {
                imageCheck.setSelected(false);
                //mCheckView.setCheckedNum(CheckView.UNCHECKED);
               // tvCheck.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_unselect),null,null,null);
            } else {
                imageCheck.setSelected(false);
            }
        } else {
            if (assertAddSelection(item)) {
                mSelectedCollection.add(item);
                if (mSpec.countable) {
                  //  mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
                   // tvCheck.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_select),null,null,null);
                    imageCheck.setSelected(true);
                } else {
                    imageCheck.setSelected(true);
                }
            }
        }
        updateApplyButton();

        if (mSpec.onSelectedListener != null) {
            mSpec.onSelectedListener.onSelected(
                    mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mSelectedCollection.onSaveInstanceState(outState);
        outState.putBoolean("checkState", mOriginalEnable);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        sendBackResult(false);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            onBackPressed();
        } else if (v.getId() == R.id.button_apply) {
            sendBackResult(true);
            finish();
        }
    }

    @Override
    public void onClick() {
        if (!mSpec.autoHideToobar) {
            return;
        }

        if (mIsToolbarHide) {
            mTopToolbar.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(-(mTopToolbar.getMeasuredHeight()*2))
                    .start();
            mBottomToolbar.animate()
                    .translationYBy(-mBottomToolbar.getMeasuredHeight())
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        } else {
            mTopToolbar.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(mTopToolbar.getMeasuredHeight()*2)
                    .start();
            mBottomToolbar.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .translationYBy(mBottomToolbar.getMeasuredHeight())
                    .start();
        }

        mIsToolbarHide = !mIsToolbarHide;

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mPager.getAdapter();
        if (mPreviousPos != -1 && mPreviousPos != position) {
            ((PreviewItemFragment) adapter.instantiateItem(mPager, mPreviousPos)).resetView();

            Item item = adapter.getMediaItem(position);
            if (mSpec.countable) {
                int checkedNum = mSelectedCollection.checkedNumOf(item);
             mCheckView.setCheckedNum(checkedNum);
                if (checkedNum > 0) {
                    //mCheckView.setEnabled(true);
                    imageCheck.setEnabled(true);
                } else {
                   // viewCheck.setEnabled(true);

                    imageCheck.setEnabled(!mSelectedCollection.maxSelectableReached());
                    mCheckView.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            } else {
                boolean checked = mSelectedCollection.isSelected(item);
                mCheckView.setChecked(checked);
                imageCheck.setEnabled(checked);
                if (checked) {
                    imageCheck.setEnabled(true);
                    mCheckView.setEnabled(true);
                } else {
                    mCheckView.setEnabled(!mSelectedCollection.maxSelectableReached());
                    imageCheck.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            }
            updateSize(item);
        }
        mPreviousPos = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateApplyButton() {
        int selectedCount = mSelectedCollection.count();
        if (selectedCount == 0) {
            mButtonApply.setText(R.string.button_apply_default);
            mButtonApply.setEnabled(false);
        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
            mButtonApply.setText(R.string.button_apply_default);
            mButtonApply.setEnabled(true);
        } else {
            mButtonApply.setEnabled(true);
            mButtonApply.setText(getString(R.string.button_apply, selectedCount));
        }

        if (mSpec.originalable) {
            mOriginalLayout.setVisibility(View.VISIBLE);
            updateOriginalState();
        } else {
            mOriginalLayout.setVisibility(View.GONE);
        }
    }


    private void updateOriginalState() {
        mOriginal.setChecked(mOriginalEnable);
        if (!mOriginalEnable) {
            mOriginal.setColor(Color.WHITE);
        }

        if (countOverMaxSize() > 0) {

            if (mOriginalEnable) {
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.error_over_original_size, mSpec.originalMaxSize));
                incapableDialog.show(getSupportFragmentManager(),
                        IncapableDialog.class.getName());

                mOriginal.setChecked(false);
                mOriginal.setColor(Color.WHITE);
                mOriginalEnable = false;
            }
        }
    }


    private int countOverMaxSize() {
        int count = 0;
        int selectedCount = mSelectedCollection.count();
        for (int i = 0; i < selectedCount; i++) {
            Item item = mSelectedCollection.asList().get(i);
            if (item.isImage()) {
                float size = PhotoMetadataUtils.getSizeInMB(item.size);
                if (size > mSpec.originalMaxSize) {
                    count++;
                }
            }
        }
        return count;
    }

    protected void updateSize(Item item) {
        if (item.isGif()) {
            mSize.setVisibility(View.VISIBLE);
            mSize.setText(PhotoMetadataUtils.getSizeInMB(item.size) + "M");
        } else {
            mSize.setVisibility(View.GONE);
        }

        if (item.isVideo()) {
            mOriginalLayout.setVisibility(View.GONE);
        } else if (mSpec.originalable) {
            mOriginalLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void sendBackResult(boolean apply) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(EXTRA_RESULT_APPLY, apply);
        intent.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
        setResult(Activity.RESULT_OK, intent);
    }

    private boolean assertAddSelection(Item item) {
        IncapableCause cause = mSelectedCollection.isAcceptable(item);
        IncapableCause.handleCause(this, cause);
        return cause == null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
