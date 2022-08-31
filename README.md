## 非商业牟利项目

##  本库目标：

* 1 检测运行设备是否模拟器  
* 2 获取相对真实的IMEI AndroidId 序列号 MAc地址等


       
### 用法 MavenCentral

       implementation 'io.github.happylishang:antifake:1.7.0'
    
Java代码中：

模拟器检测激进做法
     
     EmulatorDetectUtil.isEmulatorFromAll(context)

模拟器保守做法

    AndroidDeviceIMEIUtil.isRunOnEmulator(context) //特征值判断

非UI进程启动检测，回调上报

    EmuCheckUtil.checkEmulatorFromCache(getApplicationContext(),
                new EmuCheckUtil.CheckEmulatorCallBack() {
                    @Override
                    public void onCheckSuccess(boolean isEmulator) {
                        
                    }

                    @Override
                    public void onCheckFaild() {
                        
                    }
                })



#### 1、检测是否模拟器原理  [ Android模拟器识别技术](http://www.jianshu.com/p/1db610cc8b84) 

ARM与模拟器采用的Simple X86在架构上有很大区别，可利用SMC（自修改代码+mmap）甄别

 原文链接 [ Android模拟器识别技术](http://www.jianshu.com/p/1db610cc8b84) 


#### 2、获取真实的Android设备信息 

* 可以采用一些系统隐藏的接口来 
* 可以自己实现通过Binder通信
* 可以采用Native方式获取设备信息，避免被Xposed Hook


#### Android8.0之后，序列号的获取跟IMEI权限绑定，如果不授权电话权限，同样获取不到序列号

#### Android 10.0之后，序列号、IMEI 非系统APP获取不到

#### Android 11.0之后，序列号、IMEI MAC 非系统APP获取不到
