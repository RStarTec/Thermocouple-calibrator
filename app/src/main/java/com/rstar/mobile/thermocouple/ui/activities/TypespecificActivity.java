/*
 * Copyright (c) 2015 Annie Hui @ RStar Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rstar.mobile.thermocouple.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.ui.BlankFragment;
import com.rstar.mobile.thermocouple.ui.InfoFragment;
import com.rstar.mobile.thermocouple.ui.MenuLargeFragment;

public class TypespecificActivity extends AppCompatActivity {
    private static final String TAG = TypespecificActivity.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String EXTRA_typeCode = TypespecificActivity.class.getSimpleName()+".typeCode";

    private static final int Tag_info = 0;
    private static final int Tag_more = 1;

    private static final String mTabName[] = {
            InfoFragment.title,
            BlankFragment.title
    };


    private static final int size = 1;   // Currently only 1 tab. Add more in the future.
    //private static final int size = mTabName.length;

    private String mType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        // Inflate different layout based on number of tabs
        if (size>1)
            setContentView(R.layout.activity_typespecific);
        else
            setContentView(R.layout.activity_typespecific_single);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo_thermocouple);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mType = getIntent().getStringExtra(EXTRA_typeCode);
        setTitle("Type " + mType);


        FragmentManager fm = getSupportFragmentManager();

        // Depending on the number of tabs to be hosted, if more than 1, then use pager.
        // If only 1, then just use a single fragment.
        if (size>1) {
            ViewPager viewPager = (ViewPager) findViewById(R.id.activityTypespecific_viewPager);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.activityTypespecific_tabLayout);

            PageAdapter adapter = new PageAdapter(fm, mType);
            viewPager.setAdapter(adapter);

            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            for (int index=0; index<tabLayout.getTabCount(); index++) {
                TabLayout.Tab tab = tabLayout.getTabAt(index);
                tab.setText(mTabName[index]);
            }
        }
        else {
            // The only one tab is the InfoFragment.
            Fragment fragment = fm.findFragmentById(R.id.activityTypespecific_id);
            if (fragment==null) {
                fragment = InfoFragment.newInstance(mType);
                fm.beginTransaction().add(R.id.activityTypespecific_id, fragment).commit();
            }
        }

    }



    private static class PageAdapter extends FragmentPagerAdapter {
        private String mType;
        public PageAdapter(FragmentManager fm, String type) {
            super(fm);
            mType = type;
        }

        @Override
        public Fragment getItem(int position) {
            if (position==Tag_info)
                return InfoFragment.newInstance(mType);
            else  // default is a blank
                return BlankFragment.newInstance(mType);
        }

        @Override
        public int getCount() {
            return size;
        }
    }
}

