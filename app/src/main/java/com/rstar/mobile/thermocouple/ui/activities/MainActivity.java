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
import android.view.Menu;
import android.view.MenuItem;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;
import com.rstar.mobile.thermocouple.functions.ThermoCouple;
import com.rstar.mobile.thermocouple.ui.LegalDialogFragment;
import com.rstar.mobile.thermocouple.ui.MenuLargeFragment;
import com.rstar.mobile.thermocouple.ui.MenuSmallFragment;
import com.rstar.mobile.thermocouple.ui.ThermocoupleKeypadFragment;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final String DefaultTypeCode = ThermoCouple.all;

    private static final String mTabName[] = {
            MenuSmallFragment.title[0],
            MenuSmallFragment.title[1],
            MenuSmallFragment.title[2]
    };
    private static final int size = mTabName.length;

    private static final int FragmentId_keypad = R.id.activityMain_keypad;
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Savelog.d(TAG, debug, "onCreate()");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo_thermocouple);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);

        FragmentManager fm = getSupportFragmentManager();

        if (!isTablet) {
            setContentView(R.layout.activity_main);

            ViewPager viewPager = (ViewPager) findViewById(R.id.activityMain_viewPager);
            PageAdapter adapter = new PageAdapter(fm);
            viewPager.setAdapter(adapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.activityMain_tabLayout);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            for (int index=0; index<tabLayout.getTabCount(); index++) {
                TabLayout.Tab tab = tabLayout.getTabAt(index);
                tab.setText(mTabName[index]);
            }
        }
        else {
            setContentView(R.layout.activity_main_large);

            Fragment largeFragment = fm.findFragmentById(R.id.activityMain_large);
            if (largeFragment==null) {
                largeFragment = MenuLargeFragment.newInstance(DefaultTypeCode);
                fm.beginTransaction().add(R.id.activityMain_large, largeFragment).commit();
            }
        }

        mFragment = fm.findFragmentById(FragmentId_keypad);
        if (mFragment==null) {
            mFragment = ThermocoupleKeypadFragment.newInstance();
            fm.beginTransaction().add(FragmentId_keypad, mFragment).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.menu_eula: {
                FragmentManager fm = getSupportFragmentManager();
                LegalDialogFragment dialogFragment;
                dialogFragment = LegalDialogFragment.newInstance(R.raw.eula);
                dialogFragment.show(fm, LegalDialogFragment.dialogTag);
                return true;
            }
            case R.id.menu_privacy: {
                FragmentManager fm = getSupportFragmentManager();
                LegalDialogFragment dialogFragment;
                dialogFragment = LegalDialogFragment.newInstance(R.raw.privacypolicy);
                dialogFragment.show(fm, LegalDialogFragment.dialogTag);
                return true;
            }
            case R.id.menu_acknowledgment: {
                FragmentManager fm = getSupportFragmentManager();
                LegalDialogFragment dialogFragment;
                dialogFragment = LegalDialogFragment.newInstance(R.raw.acknowledgment);
                dialogFragment.show(fm, LegalDialogFragment.dialogTag);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        mFragment = null;
        super.onDestroy();
    }



    private static class PageAdapter extends FragmentPagerAdapter {
        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MenuSmallFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return size;
        }
    }

}

