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

package com.rstar.mobile.thermocouple.fields;


public class Fields {
	public static abstract class Constants {
		public Constants() {}
		// Constant fields only need to be copied once
		abstract public void get() throws Exception;
		// Fields acquired in the test must be detached to prevent memory leak during tests
		abstract public void detach();
	}
	public static abstract class StaticVars {
		public StaticVars() {}
		abstract public void refresh() throws Exception;
		// Fields acquired in the test must be detached to prevent memory leak during tests
		abstract public void detach();
	}

	public static abstract class Variables<T> {
		public Variables() {}
		// Variable fields must be refreshed before use.
		abstract public void refresh(T object) throws Exception;
		// Fields acquired in the test must be detached to prevent memory leak during tests
		abstract public void detach();
	}

	public static abstract class Functions<T> {
		public Functions() {}
	}
}
