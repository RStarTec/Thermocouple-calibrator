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

public class ExponentialTable {

    public static final int columnSize = 2;

    public static final int cell_ids[][] = {
            {R.id.table_cell_0_0, R.id.table_cell_0_1},
            {R.id.table_cell_1_0, R.id.table_cell_1_1},
            {R.id.table_cell_2_0, R.id.table_cell_2_1},
            {R.id.table_cell_3_0, R.id.table_cell_3_1}
    };
    public static final int result_ids[] = {R.id.table_cell_result_0, R.id.table_cell_result_1};
}
