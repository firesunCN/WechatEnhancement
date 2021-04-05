package me.firesun.wechat.enhancement.util;


import android.util.Log;

import net.dongliu.apk.parser.bean.DexClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;

public final class ReflectionUtil {

    private static final Map classesCache = new HashMap();

    private static boolean xposedExist;

    static {
        try {
            Class.forName("de.robv.android.xposed.XposedBridge");
            xposedExist = true;
        } catch (ClassNotFoundException e) {
            xposedExist = false;
        }
    }

    public static void log(String msg) {
        if (msg == null) {
            return;
        }

        if (xposedExist) {
            XposedBridge.log(msg);
        } else {
            Log.i("Xposed", "[WechatEnhancement] " + msg);
        }
    }

    public static Method findMethodsByExactParameters(Class clazz, Class returnType, Class... parameterTypes) {
        if (clazz == null) {
            return null;
        }

        // List<Method> list = Arrays.asList(XposedHelpers.findMethodsByExactParameters(clazz, returnType, (Class[]) Arrays.copyOf(parameterTypes, parameterTypes.length)));
        List<Method> list = Arrays.asList(findMethodsByExactParametersInner(clazz, returnType, (Class[]) Arrays.copyOf(parameterTypes, parameterTypes.length)));
        if (list.isEmpty())
            return null;
        else if (list.size() > 1) {
            log("find too many methods");
            for (int i = 0; i < list.size(); i++) {
                log("methods" + i + ": " + list.get(i));
            }
        }

        return list.get(0);
    }

    public static final String getClassName(DexClass clazz) {
        String str = clazz.getClassType().replace('/', '.');
        return str.substring(1, str.length() - 1);
    }

    public static Classes findClassesFromPackage(ClassLoader loader, List<String> classes, String packageName, int depth) {
        if (classesCache.containsKey(packageName + depth)) {
            return (Classes) classesCache.get(packageName + depth);
        }

        List<String> classNameList = new ArrayList();
        for (int i = 0; i < classes.size(); i++) {
            String clazz = classes.get(i);
            String currentPackage = clazz.substring(0, clazz.lastIndexOf("."));
            for (int j = 0; j < depth; j++) {
                int pos = currentPackage.lastIndexOf(".");
                if (pos < 0)
                    break;
                currentPackage = currentPackage.substring(0, currentPackage.lastIndexOf("."));
            }
            if (currentPackage.equals(packageName)) {
                classNameList.add(clazz);
            }
        }
        List<Class> classList = new ArrayList();
        for (int i = 0; i < classNameList.size(); i++) {
            String className = classNameList.get(i);
            Class c = findClassIfExists(className, loader);
            if (c != null) {
                classList.add(c);
            }
        }
        Classes cs = new Classes(classList);
        classesCache.put(packageName + depth, cs);
        return cs;
    }

    private static Map<String, Method> methodCache = new HashMap<>();

    private static String getParametersString(Class<?>... clazzes) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (Class<?> clazz : clazzes) {
            if (first)
                first = false;
            else
                sb.append(",");

            if (clazz != null)
                sb.append(clazz.getCanonicalName());
            else
                sb.append("null");
        }
        sb.append(")");
        return sb.toString();
    }

    public static Method findMethodExact(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        String fullMethodName = clazz.getName() + '#' + methodName + getParametersString(parameterTypes) + "#exact";

        if (methodCache.containsKey(fullMethodName)) {
            Method method = methodCache.get(fullMethodName);
            if (method == null)
                throw new NoSuchMethodError(fullMethodName);
            return method;
        }

        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            methodCache.put(fullMethodName, method);
            return method;
        } catch (NoSuchMethodException e) {
            methodCache.put(fullMethodName, null);
            throw new NoSuchMethodError(fullMethodName);
        }
    }

    public static Method[] findMethodsByExactParametersInner(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
        List<Method> result = new LinkedList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (returnType != null && returnType != method.getReturnType())
                continue;

            Class<?>[] methodParameterTypes = method.getParameterTypes();
            if (parameterTypes.length != methodParameterTypes.length)
                continue;

            boolean match = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] != methodParameterTypes[i]) {
                    match = false;
                    break;
                }
            }

            if (!match)
                continue;

            method.setAccessible(true);
            result.add(method);
        }
        return result.toArray(new Method[result.size()]);
    }

    public static final Method findMethodExactIfExists(Class clazz, String methodName, Class... parameterTypes) {
        Method method = null;
        try {
            method = findMethodExact(clazz, methodName, (Class[]) Arrays.copyOf(parameterTypes, parameterTypes.length));
        } catch (Error | Exception e) {
        }
        return method;
    }

    public static final Class findClassIfExists(String className, ClassLoader classLoader) {
        Class c = null;
        try {
            c = Class.forName(className, false, classLoader);
        } catch (Error | Exception e) {
        }
        return c;
    }

    public static final Field findFieldIfExists(Class clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }
        Field field = null;
        try {
            field = clazz.getField(fieldName);
        } catch (Error | Exception e) {
        }
        return field;
    }

    public static final List<Field> findFieldsWithType(Class clazz, String typeName) {
        List<Field> list = new ArrayList<Field>();
        if (clazz == null) {
            return list;
        }
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                Class fieldType = field.getType();
                if (fieldType.getName().equals(typeName)) {
                    list.add(field);
                }
            }
        }
        return list;
    }

    public static final class Classes {
        private final List<Class> classes;

        public Classes(List<Class> list) {
            this.classes = (List<Class>) list;
        }

        public final Classes filterByNoMethod(Class<?> cls, Class<?>... clsArr) {
            List arrayList = new ArrayList();
            for (Object next : this.classes) {
                if (ReflectionUtil.findMethodsByExactParameters((Class) next, cls, (Class[]) Arrays.copyOf(clsArr, clsArr.length)) == null) {
                    arrayList.add(next);
                }
            }
            return new Classes(arrayList);
        }

        public final Classes filterByMethod(Class<?> cls, Class<?>... clsArr) {
            List arrayList = new ArrayList();
            for (Object next : this.classes) {
                if (ReflectionUtil.findMethodsByExactParameters((Class) next, cls, (Class[]) Arrays.copyOf(clsArr, clsArr.length)) != null) {
                    arrayList.add(next);
                }
            }

            return new Classes(arrayList);
        }

        public final Classes filterByNoField(String fieldType) {
            List arrayList = new ArrayList();
            for (Object next : this.classes) {
                if (ReflectionUtil.findFieldsWithType((Class) next, fieldType).isEmpty()) {
                    arrayList.add(next);
                }
            }

            return new Classes(arrayList);
        }

        public final Classes filterByField(String fieldType) {
            List arrayList = new ArrayList();
            for (Object next : this.classes) {
                if (!ReflectionUtil.findFieldsWithType((Class) next, fieldType).isEmpty()) {

                    arrayList.add(next);
                }
            }

            return new Classes(arrayList);
        }

        public final Classes filterByField(String fieldName, String fieldType) {
            List arrayList = new ArrayList();
            for (Object next : this.classes) {
                Field field = ReflectionUtil.findFieldIfExists((Class) next, fieldName);

                if (field != null && field.getType().getCanonicalName().equals(fieldType)) {
                    arrayList.add(next);
                }
            }


            return new Classes(arrayList);
        }

        public final Classes filterByMethod(Class returnType, String methodName, Class... parameterTypes) {
            List arrayList = new ArrayList();
            for (Object next : this.classes) {
                Method method = ReflectionUtil.findMethodExactIfExists((Class) next, methodName, (Class[]) Arrays.copyOf(parameterTypes, parameterTypes.length));
                if (method != null && method.getReturnType().getName().equals(returnType.getName())) {
                    arrayList.add(next);
                }
            }

            return new Classes(arrayList);
        }

        public final Classes filterByMethod(String methodName, Class... parameterTypes) {
            List arrayList = new ArrayList();
            for (Object next : this.classes) {
                Method method = ReflectionUtil.findMethodExactIfExists((Class) next, methodName, (Class[]) Arrays.copyOf(parameterTypes, parameterTypes.length));
                if (method != null) {
                    arrayList.add(next);
                }
            }

            return new Classes(arrayList);
        }

        public final Class<?> firstOrNull() {
            if (this.classes.isEmpty())
                return null;
            else if (this.classes.size() > 1) {
                log("find too many classes");
                for (int i = 0; i < this.classes.size(); i++) {
                    log("class" + i + ": " + this.classes.get(i));
                }
            }
            return this.classes.get(0);
        }
    }
}
