# Android使用FlutterEngineGroup实现混合栈

由同一 FlutterEngineGroup 生成的 FlutterEngine 可共享常用的系统资源，例如 GPU 上下文、字体度量(font metrics)、隔离线程的快照(isolate group snapshot)，的性能优势(performance advantage)，从而加快首次渲染的速度、降低延迟并降低内存占用。

适用场景：

混合路由栈：Flutter 和 native 互跳，即 native -> Flutter -> native -> Flutter
模块化：使用多个 Flutter 实例，每个实例各自维护路由栈、UI 和应用状态
多视图：多个 Flutter View 同时集成在同一个页面上，且同时显示

## 在Application中创建FlutterEngineGroup

```
package com.lgy.flutter_test_1

import android.app.Application
import io.flutter.embedding.engine.FlutterEngineGroup

class MyApplication : Application() {
    lateinit var engineGroup: FlutterEngineGroup // Create a FlutterEngineGroup whose child engines will share resources

    override fun onCreate() {
        super.onCreate()
        engineGroup = FlutterEngineGroup(this)
    }
}
```

## Flutter界面绑定一个Activity

通过这个类，为每个Activity绑定一个Flutter界面，并通过MethodChannel实现.dart中调用原生的方法。从而实现跳转。

```
package com.lgy.flutter_test_1

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

class EngineBindings(context: Context, val delegate: EngineBindingsDelegate, entrypoint: String, val id: Int) {

    val channel: MethodChannel
    val engine: FlutterEngine

    init {
        // Represents a collection of FlutterEngines who share resources to allow them to be created faster and with less memory
        val engineGroup: FlutterEngineGroup = (context.applicationContext as MyApplication).engineGroup // 全局的 FEG
        // The path within the AssetManager where the app will look for assets
        val pathToBundle: String = FlutterInjector.instance().flutterLoader().findAppBundlePath()
        // This has to be lazy to avoid creation before the FlutterEngineGroup
        val dartEntrypoint = DartExecutor.DartEntrypoint(pathToBundle, entrypoint)
        // Creates a FlutterEngine in this group and run its DartExecutor with the specified DartEntrypoint
        engine = engineGroup.createAndRunEngine(context, dartEntrypoint) // 创建 FlutterEngine 并执行指定的 DartEntrypoint
        channel = MethodChannel(engine.dartExecutor.binaryMessenger, "multiple-flutters")
    }
}
```

开始绑定

```
class SingleFlutterActivity : FlutterActivity() {
    // EngineBindings is used to bridge communication between the Flutter instance and the DataModel
    private val engineBindings: EngineBindings by lazy { EngineBindings(this, this, "main", 1) }
    override fun provideFlutterEngine(context: Context): FlutterEngine = engineBindings.engine // 使用指定的 FlutterEngine
}
```
## flutter暴露页面给原生

```
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher.dart' as launcher;

void main() => runApp(const MyApp(color: Colors.red));

@pragma('vm:entry-point')
void topMain() => runApp(const MyApp(color: Colors.green));

@pragma('vm:entry-point')
void bottomMain() => runApp(const MyApp(color: Colors.blue));

class MyApp extends StatelessWidget {
  const MyApp({super.key, required this.color});

  final MaterialColor color;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(primarySwatch: color),
      home: const MyHomePage(title: '演示 MultFlutter'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;
  final _url = Uri.parse('https://www.cnblogs.com/baiqiantao/');
  final MethodChannel _channel = const MethodChannel('multiple-flutters');

  @override
  void initState() {
    super.initState();
    _channel.setMethodCallHandler((call) async {
      if (call.method == "setData") {
        _counter = call.arguments as int;
        setState(() => _counter);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              child: const Text('跳到一个 native 页面'),
              onPressed: () => _channel.invokeMethod<void>("next", _counter),
            ),
            ElevatedButton(
              child: const Text('使用浏览器打开一个 url'),
              onPressed: () async {
                if (await launcher.canLaunchUrl(_url)) {
                  await launcher.launchUrl(_url);
                }
              },
            ),
          ],
        ),
      ),
    );
  }
}
```




# 参考文章

https://www.cnblogs.com/baiqiantao/p/16341100.html