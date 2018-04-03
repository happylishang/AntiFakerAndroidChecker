
int detect (){
    int a=0;    //声明出口参数
    __asm __volatile ( //这段属于self-modifing-code 自修改代码
    "push    {r4, r5, r6, r7, r8, lr} \n"
            "mov r7,#0 \n"
            "mov r8,pc \n"
            "mov r4,#0 \n"
            "add r7,#1 \n"
            "ldr r5,[r8]\n"
            "code:\n"
            "add r4,#1\n"
            "mov r8,pc\n"
            "sub r8,#12\n"
            "str r5,[r8]\n"
            "cmp r4,#10\n"
            "bge out\n"
            "cmp r7,#10\n"
            "bge out\n"
            "b code\n"
            "out:\n"
            "mov r0,r4\n"
            "pop     {r4, r5, r6, r7, r8, pc}\n"
            "mov %0,r0 \n"
             :"=r"(a)
    );
    return a;
}
// /Volumes/arm-x/bin/arm-none-linux-gnueabi-gcc detect_func.c -c
// /Volumes/arm-x/bin/arm-none-linux-gnueabi-objdump detect_func.o -d
// 交叉编译后，反编译的二进制可执行代码如下


//1）disassemble
//      -d：反汇编可执行的section；
//      -D：反汇编全部；
//     -d -j section_name：反汇编指定的section；



//snaildeMac-mini:source personal$  /Volumes/arm-x/bin/arm-none-linux-gnueabi-objdump detect_func.o -d
//
//detect_func.o:     file format elf32-littlearm
//
//
//Disassembly of section .text:
//
//00000000 <detect>:

//   这部分是用来初始化局部变量a
//   0:	e52db004 	push	{fp}		; (str fp, [sp, #-4]!)
//   4:	e28db000 	add	fp, sp, #0
//   8:	e24dd00c 	sub	sp, sp, #12
//   栈
//   c:	e3a03000 	mov	r3, #0
//   10:	e50b3008 	str	r3, [fp, #-8]


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


//c参考文档  gcc命令 http://man.linuxde.net/gcc
