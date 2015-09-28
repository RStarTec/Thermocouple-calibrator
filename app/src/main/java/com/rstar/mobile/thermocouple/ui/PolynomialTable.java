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

package com.rstar.mobile.thermocouple.ui;

import com.rstar.mobile.thermocouple.R;

public class PolynomialTable {
    public static final int MaxOrder = 16;

    public static final int row_ids[] = {
            R.id.table_tableRow0,
            R.id.table_tableRow1,
            R.id.table_tableRow2,
            R.id.table_tableRow3,
            R.id.table_tableRow4,
            R.id.table_tableRow5,
            R.id.table_tableRow6,
            R.id.table_tableRow7,
            R.id.table_tableRow8,
            R.id.table_tableRow9,
            R.id.table_tableRow10,
            R.id.table_tableRow11,
            R.id.table_tableRow12,
            R.id.table_tableRow13,
            R.id.table_tableRow14,
            R.id.table_tableRow15,
            R.id.table_tableRow16
    };

    public static final int columnSize = 3;

    public static final int cell_ids[][] = {
            {R.id.table_cell_0_0, R.id.table_cell_0_1, R.id.table_cell_0_2},
            {R.id.table_cell_1_0, R.id.table_cell_1_1, R.id.table_cell_1_2},
            {R.id.table_cell_2_0, R.id.table_cell_2_1, R.id.table_cell_2_2},
            {R.id.table_cell_3_0, R.id.table_cell_3_1, R.id.table_cell_3_2},
            {R.id.table_cell_4_0, R.id.table_cell_4_1, R.id.table_cell_4_2},
            {R.id.table_cell_5_0, R.id.table_cell_5_1, R.id.table_cell_5_2},
            {R.id.table_cell_6_0, R.id.table_cell_6_1, R.id.table_cell_6_2},
            {R.id.table_cell_7_0, R.id.table_cell_7_1, R.id.table_cell_7_2},
            {R.id.table_cell_8_0, R.id.table_cell_8_1, R.id.table_cell_8_2},
            {R.id.table_cell_9_0, R.id.table_cell_9_1, R.id.table_cell_9_2},
            {R.id.table_cell_10_0, R.id.table_cell_10_1, R.id.table_cell_10_2},
            {R.id.table_cell_11_0, R.id.table_cell_11_1, R.id.table_cell_11_2},
            {R.id.table_cell_12_0, R.id.table_cell_12_1, R.id.table_cell_12_2},
            {R.id.table_cell_13_0, R.id.table_cell_13_1, R.id.table_cell_13_2},
            {R.id.table_cell_14_0, R.id.table_cell_14_1, R.id.table_cell_14_2},
            {R.id.table_cell_15_0, R.id.table_cell_15_1, R.id.table_cell_15_2},
            {R.id.table_cell_16_0, R.id.table_cell_16_1, R.id.table_cell_16_2}
    };
    public static final int header_ids[] = {R.id.table_cell_header_0, R.id.table_cell_header_1, R.id.table_cell_header_2};
    public static final int result_ids[] = {R.id.table_cell_result_0, R.id.table_cell_result_1, R.id.table_cell_result_2};

}
