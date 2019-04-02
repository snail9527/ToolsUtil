# ToolsUtil
一些工具类

#### [NetworkUtil](/library/src/main/java/com/lib/NetworkUtil)
监听网络状态变化
使用NetworkUtil.registerCallback() 注册网络监听，在入口Activity.onCreate()中调用
使用NetworkUtil.unRegister()反注册网络监听，在Activity.onDestroy()中调用
使用NetworkUtil.isNetworkAvailable()判断网络状态
