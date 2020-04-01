###      								严禁用于商业牟利

### 用法 jcenter

       implementation 'com.snail:antifake:1.3'
    
Java代码中

      EmulatorDetectUtil.isEmulator()

###  本库目标：

* 1 检测是否模拟器 其实主要是检测simple x86架构（模拟器采用），真机极少采用，放弃这部分用户并无不可
* 2 获取相对真实的IMEI AndroidId 序列号 MAc地址等

#### 检测是否模拟器原理  [ Android模拟器识别技术](http://www.jianshu.com/p/1db610cc8b84) 

ARM与模拟器采用的Simple X86在架构上有很大区别，可利用SMC（自修改代码+mmap）甄别

 原文链接 [ Android模拟器识别技术](http://www.jianshu.com/p/1db610cc8b84) 


#### 2获取真实的Android设备信息 

* 可以采用一些系统隐藏的接口来获取设备信息，隐藏的接口不太容易被篡改，因为可能或导致整个系统运行不正常
* 可以自己通过Binder通信的方式向服务请求信息，比如IMEI号，就是想Phone服务发送请求获取的，当然如果Phone服务中的Java类被Hook，那么这种方式也是获取不到正确的信息的
* 可以采用Native方式获取设备信息，这种哦方式可以有效的避免被Xposed Hook，不过其实仍然可以被adbi 在本地层Hook。

#### Android8.0之后，序列号的获取跟IMEI权限绑定，如果不授权电话权限，同样获取不到序列号


#### Android10.0之后，序列号、IMEI 非系统APP获取不到
