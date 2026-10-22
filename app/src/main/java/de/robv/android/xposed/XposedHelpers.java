package de.robv.android.xposed;

import java.lang.reflect.Method;

public class XposedHelpers {
    public static Class<?> findClass(String className, ClassLoader classLoader) {
        return null;
    }
    
    public static Method findMethodExact(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return null;
    }
    
    public static void findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {}
    public static void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {}
    
    public static Object callMethod(Object obj, String methodName, Object... args) {
        return null;
    }
}
