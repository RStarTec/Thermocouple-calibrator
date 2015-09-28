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

package com.rstar.mobile.thermocouple;

import android.os.Build;

public class AppSettings {
    private static final String TAG = AppSettings.class.getSimpleName()+"_class";

    public static final boolean defaultDebug = false;  // Generate debug messages if set
    public static final boolean developerMode = false;  // Turn on all developer's control functions if set
    public static final boolean testerEnabled = false; // Save the log file in external folder if set, allow tester to report bugs to developer

    public static final String defaultFolder = "/TCcalibrator";

    // NIST database site with data in JSON format
    public static final String databaseLink = "http://catalog.data.gov/dataset/nist-its-90-thermocouple-database-srd-60";


    public static boolean isNewVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
