#include <jni.h>
#include <stddef.h>
#include <unistd.h>
#include <sys/mman.h>
#include <android/log.h>
#include<fcntl.h>
#include <signal.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_ERROR,"lishang",__VA_ARGS__)
#define PROT PROT_EXEC|PROT_WRITE|PROT_READ
//这里其实主要是检测是不是在x86或者在arm上运行


const int handledSignals[] = {
        SIGSEGV, SIGABRT, SIGFPE, SIGILL, SIGBUS
};
const int handledSignalsNum = sizeof(handledSignals) / sizeof(handledSignals[0]);
struct sigaction old_handlers[5];

void my_sigaction(int signal, siginfo_t *info, void *reserved) {
    LOGI("Crash detect  signal %d",signal);
    exit(0);
}

int load(JNIEnv *env) {
    struct sigaction handler;
    memset(&handler, 0, sizeof(sigaction));
    handler.sa_sigaction = my_sigaction;
    handler.sa_flags = SA_RESETHAND;
    int i = 0;
    for ( ; i < handledSignalsNum; ++i) {
        sigaction(handledSignals[i], &handler, &old_handlers[i]);
    }
    return 1;
}




int a = -1;
int b = -1;

int (*asmcheck)(void);

int detectAsm (){
    int a=0;    //声明出口参数
    __asm __volatile ( //这段属于self-modifing-code 自修改代码

            "sub	    sp, sp, #0x30 \n"
            "stp    x29, x30, [sp, #0x20]\n"
            "smc%=:\n"
            "mov x0,#0 \n"
            "ADR x1,smc%=\n"
            "mov x2,#0 \n"
            "add x0,x0,#1 \n"
            "ldr x3,[x1]\n"
            "code%=:\n"
            "add x2,x2,#1\n"
            "ADR x1,code%=\n"
            "sub x1,x1,#12\n"
             "str x3,[x1]\n"
            "cmp x2,#10\n"
            "bge out%=\n"
            "cmp x0,#10\n"
            "bge out%=\n"
            "b code%=\n"
            "out%=:\n"
            "mov x0,x2\n"
            "ldp    x29, x30, [sp, #0x20]  \n"
            "add    sp, sp, #0x30   \n"
            "mov %0,x0 \n"
            :"=r"(a)
    );


   return a;
}

int detect() {
char code[] =
		"\xff\xc3\x00\xd1" 	//\xd1\x00\xc3\xff 	sub	sp, sp, #0x30
		"\xfd\x7b\x02\xa9"	//stp	x29, x30, [sp,#32]
		"\x00\x00\x80\xd2" 	//mov	x0, #0x0
		"\xe1\xff\xff\x10" //	adr	x1, 10 <smc>
		"\x21\x7c\x40\x92"  	       //and	x1, x1, #0xffffffff
		"\x02\x00\x80\xd2" // mov	x2, #0x0                   	// #0
		"\x00\x04\x00\x91"//	add	x0, x0, #0x1
	  	"\x23\x00\x40\xf9"  //         	ldr	x3, [x1]
		"\x42\x04\x00\x91" //          24:	 	add	x2, x2, #0x1
		"\xe1\xff\xff\x10" //          28:	 	adr	x1, 24 <code>
		"\x21\x30\x00\xd1" //	sub	x1, x1, #0xc
		"\x21\x7c\x40\x92"  	       //and	x1, x1, #0xffffffff
	 	"\x23\x00\x00\xf9"//	str	x3, [x1]
		"\x5f\x28\x00\xf1" //      	cmp	x2, #0xa
		"\x8a\x00\x00\x54"//           b.ge	48 <out>
		"\x1f\x28\x00\xf1"  //          3c:	 	cmp	x0, #0xa
		"\x4a\x00\x00\x54"//          40:	 	b.ge	48 <out>
		"\xf8\xff\xff\x17"//          44:	 	b	24 <code>
		"\xe0\x03\x02\xaa"//        48:	 	mov	x0, x2
		"\xfd\x7b\x42\xa9"//        4c:	 	ldp	x29, x30, [sp,#32]
		"\xff\xc3\x00\x91"//        50:	 	add	sp, sp, #0x30
		;

   LOGI(" start  detect");
    void *exec = mmap(NULL, (size_t) getpagesize(), PROT, MAP_ANONYMOUS | MAP_PRIVATE, -1,
                      (off_t) 0);
    LOGI(" mmap sucess exec  %x  %d ", exec,(size_t) getpagesize());
    if (exec == (void *) -1) {
        int fd = fopen("/dev/zero", "w+");
        exec = mmap(NULL, (size_t) getpagesize(), PROT, MAP_PRIVATE, fd, (off_t) 0);
        if (exec == (void *) -1) {
            return 10;
        }
    }

    memcpy(exec, code,  sizeof(code));
    LOGI(" mmap copy  exec  %x", exec);
    //如果不是 (size_t) getpagesize() 是sizeof（code），就必须加上LOGI(" mmap sucess exec  %x", exec); ，才能降低崩溃概率，这尼玛操蛋
    asmcheck = (int *) exec;
      a= asmcheck();
   // a=detectAsm ();
    munmap(exec, getpagesize());
    return a;
}



JNIEXPORT jboolean JNICALL Java_com_snail_device_jni_EmulatorDetectUtil_detect

        (JNIEnv *env, jobject jobject1) {
    //load(env);
    int ret = detect();
     LOGI(" result  %d   " ,ret );
    return ret == 1;
}

//跳转 是相对跳转，不需要添加空指令，也不需要补充指令，各种出栈入栈不需要额外保护





