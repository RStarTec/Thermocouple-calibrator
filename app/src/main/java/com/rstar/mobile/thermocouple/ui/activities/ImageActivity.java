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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;

public class ImageActivity extends AppCompatActivity {
    private static final String TAG = ImageActivity.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final String EXTRA_imageId = ImageActivity.class.getSimpleName()+".imageId";

    private int mImageId;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo_thermocouple);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        setContentView(R.layout.activity_image);
        mImageId = getIntent().getIntExtra(EXTRA_imageId, 0);

        if (mImageId!=0) {
            ImageView imageView = (ImageView) findViewById(R.id.activityImage_image);
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                imageView.setImageDrawable(getDrawable(mImageId));
            else
                imageView.setImageDrawable(getResources().getDrawable(mImageId));
        }
    }


}

