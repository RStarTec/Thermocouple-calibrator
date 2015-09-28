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

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RefUtil {
	private static String TAG = RefUtil.class.getSimpleName()+"_class";
	private static final boolean debug = false;

	// Generic function to use reflection to get a private field from an object
	public static <T> T getPrivateField(Object object, String fieldname, Class<T> returnType) throws Exception  {
		T returnValue = null;
		Field field;
		try {
			field = object.getClass().getDeclaredField(fieldname);
		} catch (NoSuchFieldException e) {
			if (debug) Log.d(TAG, "Try getting field from superclass");
			field = object.getClass().getSuperclass().getDeclaredField(fieldname);
		}
		field.setAccessible(true);
		returnValue = (T) field.get(object);
		return returnValue;
	}
	
	// Generic function to use reflection to get a private field from a CLASS
	public static <T> T getPrivateField(Class<?> c, String fieldname, Class<T> returnType) throws Exception {
		T returnValue = null;
		Field field;
		try {
			field = c.getDeclaredField(fieldname);
			field.setAccessible(true);
			returnValue = (T) field.get(c);
		} catch (NoSuchFieldException e) {
			if (debug) Log.d(TAG, "Try getting field from superclass");
			field = c.getSuperclass().getDeclaredField(fieldname);
			field.setAccessible(true);
			returnValue = (T) field.get(c.getSuperclass());
		}
		return returnValue;
	}

	
	
	// Get a private field of an object (obj) of an inner class
	public static <T> T getPrivateField(Class<?> outerClass, String innerClassName, Object obj, String fieldname, Class<T> returnType) throws Exception {
		// Get all inner classes
		Class<?> innerClasses[] = outerClass.getDeclaredClasses();
		// find the inner class that matches the order
		Class<?> innerClass = null;
		
		for (int index=0; index<innerClasses.length; index++) {
			if (innerClassName.equals(innerClasses[index].getSimpleName())) {
				innerClass = innerClasses[index];
			}
		}
		T returnValue = null;
		if (innerClass!=null) {
			Field field;
			field = innerClass.getDeclaredField(fieldname);
			field.setAccessible(true);
			returnValue = (T) field.get(obj);
		}
		return returnValue;
	}

	// Get a private static field of an inner class
	public static <T> T getPrivateConstantField(Class<?> outerClass, String innerClassName, String fieldname, Class<T> returnType) throws Exception {
		// Get all inner classes
		Class<?> innerClasses[] = outerClass.getDeclaredClasses();
		// find the inner class that matches the order
		Class<?> innerClass = null;
		
		for (int index=0; index<innerClasses.length; index++) {
			if (innerClassName.equals(innerClasses[index].getSimpleName())) {
				innerClass = innerClasses[index];
			}
		}
		T returnValue = null;
		if (innerClass!=null) {
			Field field;
			field = innerClass.getDeclaredField(fieldname);
			field.setAccessible(true);
			returnValue = (T) field.get(innerClass);
		}
		return returnValue;
	}

	// Create a new object of an inner class, supplying the parameters to the object's constructor on creation.
	public static Object getNewObject(Class<?> outerClass, String innerClassName, Class<?> parameterTypes[], Object parameters[]) throws Exception {
		// Get all inner classes
		Class<?> innerClasses[] = outerClass.getDeclaredClasses();
		// find the inner class that matches the order
		Constructor<?> constructor = null;
		for (int index=0; index<innerClasses.length; index++) {
			if (innerClassName.equals(innerClasses[index].getSimpleName())) {
				constructor = innerClasses[index].getConstructor(parameterTypes);
			}
		}
		if (constructor!=null) {
			constructor.setAccessible(true);
			Object obj = constructor.newInstance(parameters);
			return obj;
		}
		return null;
	}
	
	// Call a private method of an object (obj) in an inner class
	public static <T> T getPrivateMethod(Class<?> outerClass, String innerClassName, Object obj, String methodName,
										Class<?> parameterTypes[], Object parameters[], Class<T> returnType) throws Exception {
		// Get all inner classes
		Class<?> innerClasses[] = outerClass.getDeclaredClasses();
		// find the inner class that matches the order
		Class<?> innerClass = null;
		
		for (int index=0; index<innerClasses.length; index++) {
			if (innerClassName.equals(innerClasses[index].getSimpleName())) {
				innerClass = innerClasses[index];
			}
		}
		T returnValue = null;
		if (innerClass!=null) {
			Method method = innerClass.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			returnValue = (T) method.invoke(obj, parameters);
		}
		return returnValue;
	}



	public static <T> T runPrivateMethod(Class<?> targetClass, String methodName, Class<?> paramClasses[],
										Object params[], Class<T> returnType) throws Exception {
		T returnValue = null;
		Method method = targetClass.getDeclaredMethod(methodName, paramClasses);
		if (!method.isAccessible()) 
			method.setAccessible(true);
		returnValue = (T) method.invoke(targetClass, params);
		return returnValue;
	}



    public static <T> T runPrivateMethod(Class<?> targetClass, Object obj, String methodName, Class<?> paramClasses[],
                                         Object params[], Class<T> returnType) throws Exception {
        T returnValue = null;
        Method method = targetClass.getDeclaredMethod(methodName, paramClasses);
        if (!method.isAccessible())
            method.setAccessible(true);
        returnValue = (T) method.invoke(obj, params);
        return returnValue;
    }

}
