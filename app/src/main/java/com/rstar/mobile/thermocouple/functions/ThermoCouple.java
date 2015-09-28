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

import android.content.Context;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.IO;
import com.rstar.mobile.thermocouple.R;
import com.rstar.mobile.thermocouple.Savelog;

import java.io.File;

public class ThermoCouple {
    private static final String TAG = ThermoCouple.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final int size = 8;

    public static final String codeB = "B";
    public static final String codeE = "E";
    public static final String codeJ = "J";
    public static final String codeK = "K";
    public static final String codeN = "N";
    public static final String codeR = "R";
    public static final String codeS = "S";
    public static final String codeT = "T";

    private static final int BadIndex = -1;

    private static final int[] ResourceFn_jsonId = {
            R.raw.srd60_type_b_coefficients,
            R.raw.srd60_type_e_coefficients,
            R.raw.srd60_type_j_coefficients,
            R.raw.srd60_type_k_coefficients,
            R.raw.srd60_type_n_coefficients,
            R.raw.srd60_type_r_coefficients,
            R.raw.srd60_type_s_coefficients,
            R.raw.srd60_type_t_coefficients
    };
    private static final int[] ResourceFnInv_jsonId = {
            R.raw.srd60_type_b_coefficients_inverse,
            R.raw.srd60_type_e_coefficients_inverse,
            R.raw.srd60_type_j_coefficients_inverse,
            R.raw.srd60_type_k_coefficients_inverse,
            R.raw.srd60_type_n_coefficients_inverse,
            R.raw.srd60_type_r_coefficients_inverse,
            R.raw.srd60_type_s_coefficients_inverse,
            R.raw.srd60_type_t_coefficients_inverse
    };

    public static final int[] ResourceFn_imgId = {
            R.raw.fn_type_b,
            R.raw.fn_type_e,
            R.raw.fn_type_j,
            R.raw.fn_type_k,
            R.raw.fn_type_n,
            R.raw.fn_type_r,
            R.raw.fn_type_s,
            R.raw.fn_type_t
    };
    public static final int[] ResourceFnInv_imgId = {
            R.raw.fninv_type_b,
            R.raw.fninv_type_e,
            R.raw.fninv_type_j,
            R.raw.fninv_type_k,
            R.raw.fninv_type_n,
            R.raw.fninv_type_r,
            R.raw.fninv_type_s,
            R.raw.fninv_type_t
    };

    // These are the app's own webpages that contain additional information about each thermocouple type
    public static final String[] Info_filename = {
            "info_type_b",
            "info_type_e",
            "info_type_j",
            "info_type_k",
            "info_type_n",
            "info_type_r",
            "info_type_s",
            "info_type_t"
    };
    private static final String InfoFile_extension = ".html";



    public static final String all = "All";
    public static final String[] type = { codeB, codeE, codeJ, codeK, codeN, codeR, codeS, codeT };

    private static final String DataFile_extension = ".json";



    public static final double tolerance = 0.0000001;

    private static final int function_forward = 1;
    private static final int function_inverse = 2;


    public static Fn fn[] = null;
    public static FnInv fnInv[] = null;

    public ThermoCouple(Context context) {
        if (fn==null) {
            fn = new Fn[size];
            for (int index=0; index<size; index++) {
                String data = getData(context, index, function_forward);
                fn[index] = new Fn(data);
            }
        }
        else {
            // already exists. Do nothing.
        }
        if (fnInv==null) {
            fnInv = new FnInv[size];
            for (int index=0; index<size; index++) {
                String data = getData(context, index, function_inverse);
                fnInv[index] = new FnInv(data);
            }
        }
        else {
            // already exists. Do nothing.
        }
    }

    public static String getFactFilename(String code) {
        int index = getIndex(code);
        if (index!=BadIndex) return Info_filename[index] + InfoFile_extension;
        return "";  // not a valid name
    }

    public double getTMinOfFn() {
        double Tmin=fn[0].getTmin();
        for (int index=1; index<size; index++)  if (Tmin > fn[index].getTmin())  Tmin = fn[index].getTmin();
        return Tmin;
    }
    public double getTMaxOfFn() {
        double Tmax=fn[0].getTmax();
        for (int index=1; index<size; index++)  if (Tmax < fn[index].getTmax()) Tmax = fn[index].getTmax();
        return Tmax;
    }
    public double getApproxEMinOfFn() {
        double Emin=fn[0].getApproxEmin();
        for (int index=1; index<size; index++)  if (Emin > fn[index].getApproxEmin())  Emin = fn[index].getApproxEmin();
        return Emin;
    }
    public double getApproxEMaxOfFn() {
        double Emax=fn[0].getApproxEmax();
        for (int index=1; index<size; index++)  if (Emax < fn[index].getApproxEmax()) Emax = fn[index].getApproxEmax();
        return Emax;
    }

    public double getEMinOfFnInv() {
        double Emin=fnInv[0].getEmin();
        for (int index=1; index<size; index++)  if (Emin > fnInv[index].getEmin())  Emin = fnInv[index].getEmin();
        return Emin;
    }
    public double getEMaxOfFnInv() {
        double Emax=fnInv[0].getEmax();
        for (int index=1; index<size; index++)  if (Emax < fnInv[index].getEmax()) Emax = fnInv[index].getEmax();
        return Emax;
    }



    public Fn getFn(String code) {
        int index = getIndex(code);
        if (index<0 || index>=size) return null;
        return fn[index];
    }

    public FnInv getFnInv(String code) {
        int index = getIndex(code);
        if (index<0 || index>=size) return null;
        return fnInv[index];
    }

    public String getUnitT() {
        return fn[0].getUnitT();  // Just pick the first one to return
    }

    public String getUnitEMF() {
        return fn[0].getUnitEMF();   // Just pick the first one to return
    }


    public double computeT(String code, double T_reference, double E_measured) throws Exception {
        // Requirement: The reference junction must be of the same type as the junction at the
        // measuring end of thermal couple, but connected in opposite direction
        //
        // Let E_measured be the EMF generated by the thermocouple with respect to the reference temperature
        // Let E_0 be the EMF generated by the thermocouple with respect to 0 Celcius
        // E_reference is the EMF generated by a reference thermocouple at reference temperature, with respect to 0 celcius
        //
        //     E_reference is computed as E(T_reference) using Fn
        //     E_0 = E_measured + E_reference
        //     T_0 is computed as T(E_0) using FnInv

        int index = getIndex(code);
        double E_reference = fn[index].computeE(T_reference);
        double E_0 = E_measured + E_reference;
        double T_0 = fnInv[index].computeT(E_0);
        return T_0;
    }

    public String describeT(String code, double T_reference, double E_measured) throws Exception {
        String data = "";
        double T = computeT(code, T_reference, E_measured);
        data += "T = Finv( EMF + F(Tr) ) \n";
        data += "  = Finv( " + E_measured + " + F(" + T_reference + ") ) \n";
        data += "  = " + String.format("%.2f", T) + " " + getUnitT();

        data += "\n\n";
        data += "F is the reference function. \n";
        data += "Finv is the inverse function of F. \n";

        return data;
    }



    @Override
    public String toString() {
        String data = "";
        for (int index=0; index<size; index++) {
            data += toString(index);
        }
        return data;
    }

    private String toString(int index) {
        if (index<0 || index>=size) return "";
        String data = "";
        data += "Fn:\n" + fn[index].toString();
        data += "Fn inverse:\n" + fnInv[index].toString();
        return data;
    }

    public String toString(String code) {
        int index = getIndex(code);
        Savelog.d(TAG, debug, "code=" + code + " index=" + index);
        return toString(index);
    }

    private static int getIndex(String code) {
        if (code==null || code.equals("")) return BadIndex;
        for (int index=0; index<size; index++) {
            if (code.equals(type[index])) return index;
        }
        return BadIndex;
    }

    public int getFnImageId(String code) {
        int index = getIndex(code);
        if (index==BadIndex) return 0;   // id=0 is an invalid resource
        return ResourceFn_imgId[index];
    }
    public int getFnInvImageId(String code) {
        int index = getIndex(code);
        if (index==BadIndex) return 0;   // id=0 is an invalid resource
        return ResourceFnInv_imgId[index];
    }

    // If there is new data in internal storage, use them.
    // Else, use the files that come with the resource.
    // Require the file names to be ordered properly.
    private String getData(Context context, int index, int functionType) {
        int id;

        // Get the id based on index
        if (functionType==function_forward) {
            id = ResourceFn_jsonId[index];
        }
        else { // by default, use the inverse
            id = ResourceFnInv_jsonId[index];
        }

        // Get the file name based on id
        String resourceName = context.getResources().getResourceEntryName(id) + DataFile_extension;

        // Get the data from the file
        String data = "";
        try {
            File f = IO.getInternalFile(context, resourceName);
            data = IO.loadFileAsString(context, f);
            Savelog.d(TAG, debug, "Loaded internal file " + f.getAbsolutePath());
        } catch (Exception e) {
            data = "";
        }

        if (data==null || data.equals("")) {
            try {
                data = IO.getRawResourceAsString(context, id);
                Savelog.d(TAG, debug, "Loaded resource file " + resourceName);
            } catch (Exception e1) {
                data = "";
            }
        }
        return data;
    }
}
