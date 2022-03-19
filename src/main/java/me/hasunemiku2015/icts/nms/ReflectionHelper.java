package me.hasunemiku2015.icts.nms;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper {
    protected static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    protected static Object createBean(String packageID){
        try{
            Class<?> cls = Class.forName(packageID);
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception ignored){}

        return null;
    }

    protected static Object getField(Object variable, String name){
        try{
            Class<?> cls = variable.getClass();
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(variable);
        } catch (Exception ignored){}
        return null;
    }

    protected static void setFieldValue(Object variable, String name, Object value) {
        try{
            Class<?> cls = variable.getClass();
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.set(variable, value);
        } catch (Exception ignored){}
    }

    protected static Object runMethod(Object object, Object[] args,String name){
        try{
            Class<?> cls = object.getClass();
            Method mth = cls.getDeclaredMethod(name);
            return mth.invoke(object, args);
        } catch (Exception ignored){}
        return null;
    }

    protected static Object castObject(Object object, String packageID){
        try{
            Class.forName(packageID).cast(object);
        } catch (Exception ignored){}
        return null;
    }
}
