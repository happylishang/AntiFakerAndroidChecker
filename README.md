#      								严禁用于商业牟利


# CacheEmulatorChecker

1 检测是否模拟器
2 获取相对真实的IMEI AndroidId 序列号 MAc地址等

## 检测是否模拟器

Android模拟器常常被用来刷单，如何准确的识别模拟器成为App开发中的一个重要模块，目前也有专门的公司提供相应的SDK供开发者识别模拟器。 目前流行的Android模拟器主要分为两种，一种是基于Qemu，另一类是基于Genymotion，网上现在流行用一些模拟器特征进行鉴别，比如：

* 通过判断IMEI是否全部为0000000000格式
* 判断Build中的一些模拟器特征值
* 匹配Qemu的一些特征文件以及属性
* 通过获取cpu信息，将x86的给过滤掉（真机一般都是基于ARM）

等等，不过里面的很多手段都能通过改写ROM或者Xposed作假，让判断的性能打折扣。其实，现在绝大部分手机都是基于ARM架构，可以将其他CPU架构给忽略不计，模拟器全部运行在PC上，因此，只需要判断是运行的设备否是ARM架构即可。

ARM与PC的X86在架构上有很大区别，ARM采用的哈弗架构将指令存储跟数据存储分开，与之对应的，ARM的一级缓存分为I-Cache（指令缓存）与D-Cahce（数据缓存），而X86只有一块缓存，如果我们将一段代码可执行代码动态映射到内存，在执行的时候，X86架构上是可以修改这部分代码的，而ARM的I-Cache可以看做是只读，因为，当我们往PC对应的地址写指令的时候，其实是往D-Cahce写，而I-Cache中的指令并未被更新，这样，两端程序就会在ARM与x86上有不同的表现，根据计算结果便可以知道究竟是还在哪个平台上运行。但是，无论是x86还是ARM，只要是静态编译的程序，都没有**修改代码段**的权限，所以，首先需要将上面的汇编代码翻译成可执行文件，再需要申请一块内存，将可执行代码段映射过去，执行。
以下实现代码是测试代码的核心，主要就是实现指令的替换，这里目标是ARM-V7架构的，要注意它采用的是三级流水，PC值=当前程序执行位置+8，后面要加几个空指令，防止预取指令、预解析指令出错。通过arm交叉编译链编译出的可执行代码如下：

	//        14:	e52de004 	push	{lr}		; (str lr, [sp, #-4]!)
	//        18:	e3a02000 	mov	r2, #0
	//        1c:	e3a00000 	mov	r0, #0
	//
	//        00000020 <smc11>:
	//        20:	e2822001 	add	r2, r2, #1
	//        24:	e24f300c 	sub	r3, pc, #12
	//        28:	e5931000 	ldr	r1, [r3]
	//
	//        0000002c <code11>:
	//        2c:	e2800001 	add	r0, r0, #1
	//        30:	e24f300c 	sub	r3, pc, #12
	//        34:	e5831000 	str	r1, [r3]
	//        38:	e350000a 	cmp	r0, #10
	//        3c:	aa000002 	bge	4c <out11>
	//  40:	e352000a 	cmp	r2, #10
	//          44:	aa000000 	bge	4c <out11>
	//  48:	eafffff7 	b	2c <code11>
	...
		
如果是在ARM上运行，e2844001处指令无法被覆盖，最终执行的是**add r4,#1** ，而在x86平台上，执行的是**add r7,#1** ,代码执行完毕， r0的值在模拟器上是1，而在真机上是10。之后，将上述可执行代码通过mmap，映射到内存并执行即可，具体做法如下：

	JNIEXPORT jboolean JNICALL Java_com_snail_device_jni_EmulatorDetectUtil_detect

		(JNIEnv *env, jobject jobject1) {
	   //load(env); //无感应崩溃

	      char code[] =
			    "\x04\xe0\x2d\xE5"
			    "\x00\x20\xA0\xE3"
			    "\x00\x00\xA0\xE3"
			    "\x01\x20\x82\xE2"
			    "\x0c\x30\x4f\xe2"
			    "\x00\x10\x93\xE5"
			    "\x01\x00\x80\xE2"
			    "\x0c\x30\x4f\xe2"
			    "\x00\x10\x83\xE5"
			    "\x0A\x00\x50\xE3"
			    "\x02\x00\x00\xAA"
			    "\x0A\x00\x52\xE3"
			    "\x00\x00\x00\xAA"
			    ...

	    void *exec = mmap(NULL, (size_t) getpagesize(), PROT, MAP_ANONYMOUS | MAP_PRIVATE, -1,
			      (off_t) 0);
	    memcpy(exec, code,  (size_t) getpagesize() );
            ...
	    __clear_cache(exec, exec+ (size_t) getpagesize() );
	    asmcheck = (int *) exec;
	    int ret=-1;
	    ret= asmcheck();
	    ...
	        return ret == 1;
		}

经验证， 无论是Android自带的模拟器，还是夜神模拟器，或者Genymotion造假的模拟器，都能准确识别。在32位真机上完美运行，但是在64位的真机上可能会存在兼容性问题，可能跟arm64-v8a的指令集不同有关系，**也希望人能指点**。为了防止在真机上出现崩溃，最好还是单独开一个进程服务，利用Binder实现模拟器鉴别的查询。

另外，对于Qemu的模拟器还有一种任务调度的检测方法，但是实验过程中发现不太稳定，并且仅限Qemu，不做参考，不过这里给出原文链接：
[DEXLabs](http://www.dexlabs.org/blog/btdetect)

**仅供参考，欢迎指正**

作者：看书的小蜗牛
 原文链接 [ Android模拟器识别技术](http://www.jianshu.com/p/1db610cc8b84)

 **[Github链接 CacheEmulatorChecker](https://github.com/happylishang/CacheEmulatorChecker)**   

# 参考文档

[QEMU emulation detection](https://wiki.koeln.ccc.de/images/d/d5/Openchaos_qemudetect.pdf)       
[DEXLabs](http://www.dexlabs.org/blog/btdetect)


## 2获取真实的Android设备信息 

APP开发中常需要获取Android的Deviceid，以应对防刷，目前常用的几个设备识别码主要有IMEI（国际移动设备身份码 International Mobile Equipment Identity）或者MEID（Mobile Equipment IDentifier），这两者也是常说的DeviceId，不过Android6.0之后需要权限才能获取，而且，在Java层这个ID很容易被Hook，可能并不靠谱，另外也可以通过MAC地址或者蓝牙地址，序列号等，暂列如下：

* IMEI : (International Mobile Equipment Identity) 或者MEID :（ Mobile Equipment IDentifier ）
* MAC 或者蓝牙地址 （需要重新刷flash才能更新）
* Serial Number
* AndroidId ANDROID_ID是设备第一次启动时产生和存储的64bit的一个数，手机升级，或者被wipe后该数重置

以上四个是常用的Android识别码，系统也提供了详情的接口让开发者获取，但是由于都是Java层方法，很容易被Hook，尤其是有些专门刷单的，在手机Root之后，利用Xposed框架里的一些插件很容易将获取的数据给篡改。举个最简单的IMEI的获取，常用的获取方式如下：

	TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
    return telephonyManager.getDeviceId()
        
加入Root用户利用Xposed Hook了TelephonyManager类的getDeviceId()方法，如下，在Xposed插件中，在afterHookedMethod方法中，将DeviceId设置为随机数，这样每次获取的DeviceId都是不同的。


	public class XposedModule implements IXposedHookLoadPackage {
	
			try {
				findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getDeviceId", new XC_MethodHook() {
								@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								super.afterHookedMethod(param);
									param.setResult("" + System.currentTimeMillis());
							}
						});
			} catch (Exception e1) {
			}catch (Error e) {
			} }

所以为了获取相对准确的设备信息我们需要采取相应的应对措施，比如：

* 可以采用一些系统隐藏的接口来获取设备信息，隐藏的接口不太容易被篡改，因为可能或导致整个系统运行不正常
* 可以自己通过Binder通信的方式向服务请求信息，比如IMEI号，就是想Phone服务发送请求获取的，当然如果Phone服务中的Java类被Hook，那么这种方式也是获取不到正确的信息的
* 可以采用Native方式获取设备信息，这种哦方式可以有效的避免被Xposed Hook，不过其实仍然可以被adbi 在本地层Hook。

首先看一下看一下如何获取getDeviceId，源码如下

    public String getDeviceId() {
        try {
            return getITelephony().getDeviceId();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    
    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
    }

如果getDeviceId被Hook但是 getITelephony没被Hook，我们就可以直接通过反射获取TelephonyManager的getITelephony方法，进一步通过ITelephony的getDeviceId获取DeviceId，不过这个方法跟ROM版本有关系，比较早的版本压根没有getITelephony方法，早期可能通过IPhoneSubInfo的getDeviceId来获取，不过以上两种方式都很容被Hook，既然可以Hook getDeviceId方法，同理也可以Hook getITelephony方法，这个层次的反Hook并没有多大意义。因此，可以稍微深入一下，ITelephony.Stub.asInterface，这是一个很明显的Binder通信的方式，那么不让我们自己获取Binder代理，进而利用Binder通信的方式向Phone服务发送请求，获取设备DeviceId，Phone服务是利用aidl文件生成的Proxy与Stub，可以基于这个来实现我们的代码，Binder通信比较重要的几点：InterfaceDescriptor+TransactionId+参数，获取DeviceId的几乎不需要什么参数（低版本可能需要）。具体做法是：

* 直接通过ServiceManager的getService方法获取我们需要的Binder服务代理，这里其实就是phone服务
* 利用com.android.internal.telephony.ITelephony$Stub的asInterface方法获取Proxy对象
* 利用反射获取getDeviceId的Transaction id
* 利用Proxy向Phone服务发送请求，获取DeviceId。

具体实现如下，这种做法可以应对代理方的Hook。

	 public static int getTransactionId(Object proxy,
	                                        String name) throws RemoteException, NoSuchFieldException, IllegalAccessException {
	        int transactionId = 0;
	        Class outclass = proxy.getClass().getEnclosingClass();
	        Field idField = outclass.getDeclaredField(name);
	        idField.setAccessible(true);
	        transactionId = (int) idField.get(proxy);
	        return transactionId;
	    }

    //根据方法名，反射获得方法transactionId
    public static String getInterfaceDescriptor(Object proxy) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getInterfaceDescriptor = proxy.getClass().getDeclaredMethod("getInterfaceDescriptor");
        return (String) getInterfaceDescriptor.invoke(proxy);
    }
    

	 static String getDeviceIdLevel2(Context context) {
	
	        String deviceId = "";
	        try {
	            Class ServiceManager = Class.forName("android.os.ServiceManager");
	            Method getService = ServiceManager.getDeclaredMethod("getService", String.class);
	            getService.setAccessible(true);
	            IBinder binder = (IBinder) getService.invoke(null, Context.TELEPHONY_SERVICE);
	            Class Stub = Class.forName("com.android.internal.telephony.ITelephony$Stub");
	            Method asInterface = Stub.getDeclaredMethod("asInterface", IBinder.class);
	            asInterface.setAccessible(true);
	            Object binderProxy = asInterface.invoke(null, binder);
	            try {
	                Method getDeviceId = binderProxy.getClass().getDeclaredMethod("getDeviceId", String.class);
	                if (getDeviceId != null) {
	                    deviceId = binderGetHardwareInfo(context.getPackageName(),
	                            binder, getInterfaceDescriptor(binderProxy),
	                            getTransactionId(binderProxy, "TRANSACTION_getDeviceId"));
	                }
	            } catch (Exception e) {
	            }
	            Method getDeviceId = binderProxy.getClass().getDeclaredMethod("getDeviceId");
	            if (getDeviceId != null) {
	                deviceId = binderGetHardwareInfo("",
	                        binder, BinderUtil.getInterfaceDescriptor(binderProxy),
	                        BinderUtil.getTransactionId(binderProxy, "TRANSACTION_getDeviceId"));
	            }
	        } catch (Exception e) {
	        }
	        return deviceId;
	    }
	
	    private static String binderGetHardwareInfo(String callingPackage,
	                                                IBinder remote,
	                                                String DESCRIPTOR,
	                                                int tid) throws RemoteException {
	
	        android.os.Parcel _data = android.os.Parcel.obtain();
	        android.os.Parcel _reply = android.os.Parcel.obtain();
	        String _result;
	        try {
	            _data.writeInterfaceToken(DESCRIPTOR);
	            if (!TextUtils.isEmpty(callingPackage)) {
	                _data.writeString(callingPackage);
	            }
	            remote.transact(tid, _data, _reply, 0);
	            _reply.readException();
	            _result = _reply.readString();
	        } finally {
	            _reply.recycle();
	            _data.recycle();
	        }
	        return _result;
	    }
    
# 利用Native方法反Xposed Hook

有很多系统参数我们是通过Build来获取的，比如序列号、手机硬件信息等，例如获取序列号，在Java层直接利用Build的feild获取即可

    public static final String SERIAL = getString("ro.serialno");
    
    private static String getString(String property) {
        return SystemProperties.get(property, UNKNOWN);
    }
       
不过SystemProperties的get方法很容被Hook，被Hook之后序列号就可以随便更改，不过好在SystemProperties类是通过native方法来获取硬件信息的，我们可以自己编写native代码来获取硬件参数，这样就避免被Java Hook，

    public static String get(String key) {
        if (key.length() > PROP_NAME_MAX) {
            throw new IllegalArgumentException("key.length > " + PROP_NAME_MAX);
        }
        return native_get(key);
    }

来看一下native源码

	static jstring SystemProperties_getSS(JNIEnv *env, jobject clazz,
	                                      jstring keyJ, jstring defJ)
	{
	    int len;
	    const char* key;
	    char buf[PROPERTY_VALUE_MAX];
	    jstring rvJ = NULL;
	
	    if (keyJ == NULL) {
	        jniThrowNullPointerException(env, "key must not be null.");
	        goto error;
	    }
	    key = env->GetStringUTFChars(keyJ, NULL);
	    len = property_get(key, buf, "");
	    if ((len <= 0) && (defJ != NULL)) {
	        rvJ = defJ;
	    } else if (len >= 0) {
	        rvJ = env->NewStringUTF(buf);
	    } else {
	        rvJ = env->NewStringUTF("");
	    }
	
	    env->ReleaseStringUTFChars(keyJ, key);
	
	error:
	    return rvJ;
	}

参考这部分源码，自己实现.so库即可，这样既可以避免被Java层Hook。

[Github连接 CacheEmulatorChecker](https://github.com/happylishang/CacheEmulatorChecker)     


#### 8.0之后，序列号的获取跟IMEI权限绑定，如果不授权电话权限，同样获取不到序列号
