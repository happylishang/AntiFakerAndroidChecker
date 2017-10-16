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


void (*asmcheck)(void);

int a = -1;
int b = -1;

int detect() {
    char code[] =
            "\xF0\x41\x2D\xE9"
                    "\x00\x70\xA0\xE3"
                    "\x0F\x80\xA0\xE1"
                    "\x00\x40\xA0\xE3"
                    "\x01\x70\x87\xE2"
                    "\x00\x50\x98\xE5"
                    "\x01\x40\x84\xE2"
                    "\x0F\x80\xA0\xE1"
                    "\x0C\x80\x48\xE2"
                    "\x00\x50\x88\xE5"
                    "\x0A\x00\x54\xE3"
                    "\x02\x00\x00\xAA"
                    "\x0A\x00\x57\xE3"
                    "\x00\x00\x00\xAA"
                    "\xF6\xFF\xFF\xEA"
                    "\x04\x00\xA0\xE1"
                    "\xF0\x81\xBD\xE8";

    void *exec = mmap(NULL, (size_t) getpagesize(), PROT, MAP_ANONYMOUS | MAP_PRIVATE, -1,
                      (off_t) 0);
    if (exec == (void *) -1) {
        LOGI(" mmap faild ");
        int fd = fopen("/dev/zero", "w+");
        exec = mmap(NULL, (size_t) getpagesize(), PROT, MAP_SHARED, fd, (off_t) 0);
        LOGI(" mmap zero %x %x %x", fd, exec, exec);
        if (exec == (void *) -1) {
            return 10;
        }
    }
    memcpy(exec, code, sizeof(code) + 1);
    LOGI(" mmap sucess exec  %x", exec);
    asmcheck = (void *) exec;
    asmcheck();
    __asm __volatile (
    "mov %0,r0 \n"
    :"=r"(a)
    );
    LOGI("a= %d  ", a);
    munmap(exec, getpagesize());
    return a;
}

JNIEXPORT jboolean JNICALL Java_com_snail_antifake_jni_EmulatorDetectUtil_detect
        (JNIEnv *env, jobject jobject1) {
    load(env);
    int ret = detect();
    LOGI("%d detect ", ret);
    return ret != 10;
}



//对应关系 arm 指令集 32位

//char code[] =
//                "\xF0\x41\x2D\xE9"
//                "\x00\x60\xA0\xE3"
//                "\x00\x70\xA0\xE3"
//                "\x0F\x80\xA0\xE1"
//                "\x00\x40\xA0\xE3"
//                "\x01\x70\x87\xE2"
//                "\x00\x50\x98\xE5"
//                "\x01\x40\x84\xE2"
//                "\x0F\x80\xA0\xE1"
//                "\x0C\x80\x48\xE2"
//                "\x00\x50\x88\xE5"
//                "\x01\x60\x86\xE2"
//                "\x0A\x00\x54\xE3"
//                "\x02\x00\x00\xAA"
//                "\x0A\x00\x57\xE3"
//                "\x00\x00\x00\xAA"
//                "\xF5\xFF\xFF\xEA"
//                "\x04\x00\xA0\xE1"
//                "\xF0\x81\xBD\xE8";

// 8404 main :

//    8404:       e92d41f0        push    {r4, r5, r6, r7, r8, lr}
//    840c:       e3a07000        mov     r7, #0
//    8410:       e1a0800f        mov     r8, pc
//    8414:       e3a04000        mov     r4, #0
//    8418:       e2877001        add     r7, r7, #1
//    841c:       e5985000        ldr     r5, [r8]

//8420<code:>

//    8420:       e2844001        add     r4, r4, #1
//    8424:       e1a0800f        mov     r8, pc
//    8428:       e248800c        sub     r8, r8, #12
//    842c:       e5885000        str     r5, [r8]
//    8434:       e354000a        cmp     r4, #10
//    8438:       aa000002        bge     8448<out>
//    843c:       e357000a        cmp     r7, #10
//    8440:       aa000000        bge     8448 <out>
//    8444:       eafffff5        b       8420 <code>

//8448<out>:

//    8448:       e1a00004        mov     r0, r4
//    844c:       e8bd81f0        pop     {r4, r5, r6, r7, r8, pc}



