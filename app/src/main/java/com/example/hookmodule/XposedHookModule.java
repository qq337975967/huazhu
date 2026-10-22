package com.example.hookmodule;

import android.app.Application;
import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Xposed 模块 - Hook LingyaoCardSdk
 * 用途：持久化绕过会员验证
 * 
 * 配置方法：
 * 1. 编译为 Xposed 模块 APK
 * 2. 在 LSPosed 中勾选目标应用 com.tencent.huaxiaozhu
 * 3. 重启目标应用
 */
public class XposedHookModule implements IXposedHookLoadPackage {

    private static final String TAG = "VipHook";
    private static final String TARGET_PACKAGE = "com.tencent.huaxiaozhu";
    private static final String SDK_CLASS = "com.lingyao.cardsdk.LingyaoCardSdk";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 只 Hook 目标应用
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log(TAG + ": 开始 Hook " + TARGET_PACKAGE);

        // Hook Application 的 attach 方法，确保在应用启动时执行
        XposedHelpers.findAndHookMethod(
                Application.class,
                "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.args[0];
                        ClassLoader classLoader = context.getClassLoader();

                        XposedBridge.log(TAG + ": Application attach, ClassLoader: " + classLoader);

                        // Hook LingyaoCardSdk
                        hookLingyaoCardSdk(classLoader);

                        // Hook 失效回调
                        hookCardInvalidListener(classLoader);
                    }
                }
        );
    }

    /**
     * Hook LingyaoCardSdk 核心方法
     */
    private void hookLingyaoCardSdk(ClassLoader classLoader) {
        try {
            Class<?> sdkClass = XposedHelpers.findClass(SDK_CLASS, classLoader);

            // 1. Hook isLoggedIn() - 强制返回 true
            XposedHelpers.findAndHookMethod(
                    sdkClass,
                    "isLoggedIn",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": isLoggedIn() 被调用，返回 true");
                            return true;
                        }
                    }
            );
            XposedBridge.log(TAG + ": ✓ Hook isLoggedIn() 成功");

            // 2. Hook currentToken() - 返回伪造 token
            XposedHelpers.findAndHookMethod(
                    sdkClass,
                    "currentToken",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            String fakeToken = "FAKE_TOKEN_" + System.currentTimeMillis();
                            XposedBridge.log(TAG + ": currentToken() 返回: " + fakeToken);
                            return fakeToken;
                        }
                    }
            );
            XposedBridge.log(TAG + ": ✓ Hook currentToken() 成功");

            // 3. Hook nativeIsLoggedIn() - Native 层方法
            try {
                XposedHelpers.findAndHookMethod(
                        sdkClass,
                        "nativeIsLoggedIn",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                XposedBridge.log(TAG + ": nativeIsLoggedIn() 被拦截，返回 true");
                                return true;
                            }
                        }
                );
                XposedBridge.log(TAG + ": ✓ Hook nativeIsLoggedIn() 成功");
            } catch (Exception e) {
                XposedBridge.log(TAG + ": nativeIsLoggedIn() Hook 失败: " + e.getMessage());
            }

            // 4. Hook login() - 伪造登录成功
            XposedHelpers.findAndHookMethod(
                    sdkClass,
                    "login",
                    String.class,
                    String.class,
                    String.class,
                    XposedHelpers.findClass(SDK_CLASS + "$ResultCallback", classLoader),
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String card = (String) param.args[0];
                            String pass = (String) param.args[1];
                            String deviceId = (String) param.args[2];
                            Object callback = param.args[3];

                            XposedBridge.log(TAG + ": login() 被调用");
                            XposedBridge.log(TAG + ":   卡号: " + card);
                            XposedBridge.log(TAG + ":   卡密: " + pass);
                            XposedBridge.log(TAG + ":   设备ID: " + deviceId);

                            // 伪造登录成功回调
                            if (callback != null) {
                                XposedHelpers.callMethod(
                                        callback,
                                        "onResult",
                                        0,
                                        "登录成功(Xposed伪造)",
                                        "{\"token\":\"FAKE_TOKEN\"}"
                                );
                            }

                            // 阻止原方法执行
                            param.setResult(null);
                        }
                    }
            );
            XposedBridge.log(TAG + ": ✓ Hook login() 成功");

            // 5. Hook heartbeat() - 阻止心跳检测
            XposedHelpers.findAndHookMethod(
                    sdkClass,
                    "heartbeat",
                    XposedHelpers.findClass(SDK_CLASS + "$ResultCallback", classLoader),
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object callback = param.args[0];
                            XposedBridge.log(TAG + ": heartbeat() 被拦截，伪造成功");

                            if (callback != null) {
                                XposedHelpers.callMethod(
                                        callback,
                                        "onResult",
                                        0,
                                        "心跳成功",
                                        "{\"status\":\"ok\"}"
                                );
                            }

                            // 阻止原方法执行
                            param.setResult(null);
                        }
                    }
            );
            XposedBridge.log(TAG + ": ✓ Hook heartbeat() 成功");

        } catch (Exception e) {
            XposedBridge.log(TAG + ": Hook LingyaoCardSdk 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hook 卡密失效监听器
     */
    private void hookCardInvalidListener(ClassLoader classLoader) {
        try {
            Class<?> obClass = XposedHelpers.findClass("defpackage.Ob", classLoader);

            // Hook onCardInvalid() - 阻止卡密失效提示
            XposedHelpers.findAndHookMethod(
                    obClass,
                    "onCardInvalid",
                    String.class,
                    String.class,
                    String.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            String reason = (String) param.args[0];
                            String msg = (String) param.args[1];
                            String extra = (String) param.args[2];

                            XposedBridge.log(TAG + ": onCardInvalid() 被拦截");
                            XposedBridge.log(TAG + ":   原因: " + reason);
                            XposedBridge.log(TAG + ":   消息: " + msg);
                            XposedBridge.log(TAG + ": ✓ 已阻止卡密失效提示");

                            // 不执行原方法，直接返回
                            return null;
                        }
                    }
            );
            XposedBridge.log(TAG + ": ✓ Hook onCardInvalid() 成功");

            // Hook onSdkError() - 阻止 SDK 错误提示
            XposedHelpers.findAndHookMethod(
                    obClass,
                    "onSdkError",
                    int.class,
                    String.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            int code = (int) param.args[0];
                            String msg = (String) param.args[1];

                            XposedBridge.log(TAG + ": onSdkError() 被拦截");
                            XposedBridge.log(TAG + ":   错误码: " + code);
                            XposedBridge.log(TAG + ":   消息: " + msg);

                            return null;
                        }
                    }
            );
            XposedBridge.log(TAG + ": ✓ Hook onSdkError() 成功");

        } catch (Exception e) {
            XposedBridge.log(TAG + ": Hook 失效回调失败: " + e.getMessage());
        }
    }
}
