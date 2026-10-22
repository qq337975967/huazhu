package de.robv.android.xposed;

public abstract class XC_MethodReplacement extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        try {
            Object result = replaceHookedMethod(param);
            param.setResult(result);
        } catch (Throwable t) {
            param.setThrowable(t);
        }
    }
    
    protected abstract Object replaceHookedMethod(MethodHookParam param) throws Throwable;
}