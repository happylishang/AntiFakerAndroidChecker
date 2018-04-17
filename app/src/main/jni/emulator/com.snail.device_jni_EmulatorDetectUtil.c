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

int detect() {

char code[] =
"\xff\xc3\x00\xd1" 	//\xd1\x00\xc3\xff 	sub	sp, sp, #0x30
"\xfd\x7b\x02\xa9"	//stp	x29, x30, [sp,#32]
"\x00\x00\x80\xd2" 	//mov	x0, #0x0
"\xe1\xff\xff\x10" //	adr	x1, 10 <smc>
"\x02\x00\x80\xd2" // mov	x2, #0x0                   	// #0
"\x00\x04\x00\x91"//	add	x0, x0, #0x1
"\x23\x00\x40\xf9"  //         	ldr	x3, [x1]
"\x42\x04\x00\x91" //          24:	 	add	x2, x2, #0x1
"\xe1\xff\xff\x10" //          28:	 	adr	x1, 24 <code>
"\x21\x30\x00\xd1" //	sub	x1, x1, #0xc
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
    a= asmcheck();
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