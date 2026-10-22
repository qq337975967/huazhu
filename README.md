# HuaxiaozhuHook Xposed 模块

针对 `com.tencent.huaxiaozhu` (版本 3.3) 的会员授权绕过 Xposed 模块。

## 功能

- ✅ Hook `isLoggedIn()` 返回 `true`
- ✅ Hook `currentToken()` 返回伪造 token
- ✅ Hook `nativeIsLoggedIn()` 返回 `true`
- ✅ Hook `login()` 拦截并伪造成功回调
- ✅ Hook `heartbeat()` 拦截并伪造心跳成功
- ✅ 拦截卡密失效回调 `onCardInvalid()`
- ✅ 拦截 SDK 错误回调 `onSdkError()`

## 编译

### 方式 1：GitHub Actions（推荐）

1. Fork 本仓库
2. 在仓库页面点击 **Actions** 标签
3. 启用 Actions（如果需要）
4. 点击 **Build Xposed Module** workflow
5. 点击右侧 **Run workflow** 按钮
6. 等待编译完成（约 2-3 分钟）
7. 在 workflow 运行页面下载 **HuaxiaozhuHook-release** artifact

### 方式 2：本地编译

```bash
git clone <your-repo-url>
cd XposedHookProject
chmod +x gradlew
./gradlew assembleRelease
# APK 位于 app/build/outputs/apk/release/
```

## 安装与使用

1. 安装 Xposed 框架（LSPosed / EdXposed）
2. 在 Xposed 管理器中安装此模块 APK
3. 在模块管理中勾选启用
4. 选择作用域：`com.tencent.huaxiaozhu`
5. 重启目标应用

## 验证

安装并启用模块后，打开 Xposed 日志查看：

```
[XposedHookModule] ✅ Successfully hooked isLoggedIn() -> true
[XposedHookModule] ✅ Successfully hooked currentToken() -> fake_token_xxxxx
[XposedHookModule] ✅ Successfully hooked nativeIsLoggedIn() -> true
[XposedHookModule] ✅ Intercepted login() call
[XposedHookModule] 📝 Login params: cardNo=xxx, cardPwd=xxx, deviceId=xxx
```

## 技术说明

- 目标应用：`com.tencent.huaxiaozhu` (versionCode 303)
- Hook 目标：灵曜卡密 SDK (`com.lingyao.cardsdk.LingyaoCardSdk`)
- 最低 Android 版本：8.0 (API 26)
- Xposed API 版本：82

## 免责声明

本模块仅供学习研究使用，开发者声明为该应用的开发者本人，用于测试自己应用的安全性。请勿用于非法用途。

## License

MIT License