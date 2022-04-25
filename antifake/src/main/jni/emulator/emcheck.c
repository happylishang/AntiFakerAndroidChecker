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


int (*asmcheck)(void);
JNIEXPORT jboolean JNICALL Java_com_snail_antifake_jni_EmulatorDetectUtil_detect
        (JNIEnv *env, jobject jobject1) {
    load(env);

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
                    "\xf7\xff\xff\xea"
                    "\x04\xf0\x9d\xE4"
                    "\x00\x00\xa0\xe1"
                    "\x00\x00\xa0\xe1"
                    "\x00\x00\xa0\xe1"
                    "\x00\x00\xa0\xe1"
                    "\x00\x00\xa0\xe1"
                    "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1"
                     "\x00\x00\xa0\xe1";

    void *exec = mmap(NULL, (size_t) getpagesize(), PROT, MAP_ANONYMOUS | MAP_PRIVATE, -1,
                      (off_t) 0);
    memcpy(exec, code,  (size_t) getpagesize() );
    __clear_cache(exec, exec+ (size_t) getpagesize() );
    asmcheck = (int *) exec;
    int ret=-1;
    ret= asmcheck();
    LOGI(" result  %d   " ,ret );
    munmap(exec,(size_t) getpagesize());
    return ret == 1;
}

