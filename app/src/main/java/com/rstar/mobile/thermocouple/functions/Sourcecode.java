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

package com.rstar.mobile.thermocouple.functions;

import com.rstar.mobile.thermocouple.AppSettings;

public class Sourcecode {
    private static final String TAG = Sourcecode.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final String languageC = "C";
    private static final String languageJ = "Java";
    private static final String languageP = "Python";
    public static final String[] type = { languageC, languageJ, languageP };
    public static final int size = type.length;

    private static final int BadIndex = -1;
    private static final String DataFile_extension = ".html";

    private static final String[] source_filename = {
            "src_c",
            "src_java",
            "src_python",
    };
    public static final int defaultIndex = 0;




    public static String getFilename(int index) {
        if (index>=0 && index<size)
            return source_filename[index] + DataFile_extension;
        return "";  // not a valid filename
    }


    private static int getIndex(String code) {
        if (code==null || code.equals("")) return BadIndex;
        for (int index=0; index<size; index++) {
            if (code.equals(type[index])) return index;
        }
        return BadIndex;
    }

}
