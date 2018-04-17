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






int detect1 (){
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


    a =detect1() ;
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





