


int detect (){
    int a=0;    //声明出口参数
    __asm __volatile ( //这段属于self-modifing-code 自修改代码
    "push    {r4, r5, r6, r7, r8, lr} \n"
            "mov r7,#0 \n"
            "mov r4,#0 \n"
     "smc%=:"
            "add r7,#1 \n"
            "adr r8,smc%=\n"
            "ldr r5,[r8]\n"
     "code%=:\n"
            "add r4,#1\n"
            "adr r8,code%=\n"
            "str r5,[r8]\n"
            "cmp r4,#10\n"
            "bge out%=\n"
            "cmp r7,#10\n"
            "bge out%=\n"
            "b code%=\n"
     "out%=:\n"
            "mov r0,r4\n"
            "pop     {r4, r5, r6, r7, r8, pc}\n"
            "mov %0,r0 \n"
             :"=r"(a)
    );


   return a;
}


// 交叉编译后，反编译的二进制可执行代码如下
//arm-none-linux-gnueabi-objdump a.out -d
//1）disassemble
//      -d：反汇编可执行的section；
//      -D：反汇编全部；
 //     -d -j section_name：反汇编指定的section；


//00000000 <detect>:
//        0:	e52db004 	push	{fp}		; (str fp, [sp, #-4]!)
//        4:	e28db000 	add	fp, sp, #0
//        8:	e24dd00c 	sub	sp, sp, #12
//        c:	e3a03000 	mov	r3, #0
//        10:	e50b3008 	str	r3, [fp, #-8]
//        14:	e92d41f0 	push	{r4, r5, r6, r7, r8, lr}
//        18:	e3a07000 	mov	r7, #0
//        1c:	e3a04000 	mov	r4, #0
//
//        00000020 <smc11>:
//        20:	e2877001 	add	r7, r7, #1
//        24:	e24f800c 	sub	r8, pc, #12
//        28:	e5985000 	ldr	r5, [r8]
//
//        0000002c <code11>:
//        2c:	e2844001 	add	r4, r4, #1
//        30:	e24f800c 	sub	r8, pc, #12
//        34:	e5885000 	str	r5, [r8]
//        38:	e354000a 	cmp	r4, #10
//        3c:	aa000002 	bge	4c <out11>
//  40:	e357000a 	cmp	r7, #10
//          44:	aa000000 	bge	4c <out11>
//  48:	eafffff7 	b	2c <code11>
//
//0000004c <out11>:
//        4c:	e1a00004 	mov	r0, r4
//        50:	e8bd81f0 	pop	{r4, r5, r6, r7, r8, pc}
//        54:	e1a03000 	mov	r3, r0
//        58:	e50b3008 	str	r3, [fp, #-8]
//        5c:	e51b3008 	ldr	r3, [fp, #-8]
//        60:	e1a00003 	mov	r0, r3
//        64:	e24bd000 	sub	sp, fp, #0
//        68:	e49db004 	pop	{fp}		; (ldr fp, [sp], #4)
//        6c:	e12fff1e 	bx	lr