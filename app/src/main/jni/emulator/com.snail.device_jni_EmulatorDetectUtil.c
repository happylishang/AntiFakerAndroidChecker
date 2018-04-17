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

            "smc:\n"

            "mov x0,#0 \n"
            "ADR x1,smc\n"
            "mov x2,#0 \n"
            "add x0,x0,#1 \n"
            "ldr x3,[x1]\n"

            "code:\n"

            "add x2,x2,#1\n"
            "ADR x1,code\n"
            "sub x1,x1,#12\n"
            "str x3,[x1]\n"

            "cmp x2,#10\n"
            "bge out\n"

            "cmp x0,#10\n"
            "bge out\n"
            "b code\n"

            "out:\n"

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

"\x00\x00\x80\xd2" 	//mov	x0, #0x0

;

    void *exec = mmap(NULL, (size_t) getpagesize(), PROT, MAP_ANONYMOUS | MAP_PRIVATE, -1,
                      (off_t) 0);
    if (exec == (void *) -1) {
        int fd = fopen("/dev/zero", "w+");
        exec = mmap(NULL, (size_t) getpagesize(), PROT, MAP_PRIVATE, fd, (off_t) 0);
        if (exec == (void *) -1) {
            return 10;
        }
    }

    memcpy(exec, code, (size_t) getpagesize() );
     LOGI(" mmap sucess exec  %x", exec);
    //如果不是 (size_t) getpagesize() 是sizeof（code），就必须加上LOGI(" mmap sucess exec  %x", exec); ，才能降低崩溃概率，这尼玛操蛋
    asmcheck = (int *) exec;
     LOGI(" start asmcheck " );
    //a= asmcheck();
    munmap(exec, getpagesize());
    detect1() ;
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



//detect_func.o:     file format elf32-littlearm
//
//
//Disassembly of section .text:
//
//00000000 <detect>:
//   0:	e52db004 	push	{fp}		; (str fp, [sp, #-4]!)
//   4:	e28db000 	add	fp, sp, #0
//   8:	e24dd00c 	sub	sp, sp, #12

//   c:	e3a03000 	mov	r3, #0
//  10:	e50b3008 	str	r3, [fp, #-8]

//  14:	e92d41f0 	push	{r4, r5, r6, r7, r8, lr}
//  18:	e3a07000 	mov	r7, #0
//  1c:	e1a0800f 	mov	r8, pc
//  20:	e3a04000 	mov	r4, #0
//  24:	e2877001 	add	r7, r7, #1
//  28:	e5985000 	ldr	r5, [r8]
//
//0000002c <code>:
//  2c:	e2844001 	add	r4, r4, #1
//  30:	e1a0800f 	mov	r8, pc
//  34:	e248800c 	sub	r8, r8, #12
//  38:	e5885000 	str	r5, [r8]
//  3c:	e354000a 	cmp	r4, #10
//  40:	aa000002 	bge	50 <out>
//  44:	e357000a 	cmp	r7, #10
//  48:	aa000000 	bge	50 <out>
//  4c:	eafffff6 	b	2c <code>
//
//00000050 <out>:
//  50:	e1a00004 	mov	r0, r4
//  54:	e8bd81f0 	pop	{r4, r5, r6, r7, r8, pc}

//  58:	e1a03000 	mov	r3, r0
//  5c:	e50b3008 	str	r3, [fp, #-8]
//  60:	e3a03000 	mov	r3, #0
//  64:	e1a00003 	mov	r0, r3
//  68:	e28bd000 	add	sp, fp, #0
//  6c:	e8bd0800 	pop	{fp}
//  70:	e12fff1e 	bx	lr