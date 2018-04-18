

//是否可以用r0-r3 这样不用保存
int detectASM (){
    int a=0;    //声明出口参数
    __asm __volatile ( //这段属于self-modifing-code 自修改代码
    "push    {  lr} \n"
            "mov r2,#0 \n"
            "mov r0,#0 \n"
     "smc%=:"
            "add r2,#1 \n"
            "adr r3,smc%=\n"
            "ldr r1,[r3]\n"
     "code%=:\n"
            "add r0,#1\n"
            "adr r3,code%=\n"
            "str r1,[r3]\n"
            "cmp r0,#10\n"
            "bge out%=\n"
            "cmp r2,#10\n"
            "bge out%=\n"
            "b code%=\n"
     "out%=:\n"
            "pop     { pc}\n"
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


//00000000 <detectASM>:
//        0:	e52db004 	push	{fp}		; (str fp, [sp, #-4]!)
//        4:	e28db000 	add	fp, sp, #0
//        8:	e24dd00c 	sub	sp, sp, #12
//        c:	e3a03000 	mov	r3, #0
//        10:	e50b3008 	str	r3, [fp, #-8]

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
//
//0000004c <out11>:
//        4c:	e49df004 	pop	{pc}		; (ldr pc, [sp], #4)

//        50:	e1a03000 	mov	r3, r0
//        54:	e50b3008 	str	r3, [fp, #-8]
//        58:	e51b3008 	ldr	r3, [fp, #-8]
//        5c:	e1a00003 	mov	r0, r3
//        60:	e24bd000 	sub	sp, fp, #0
//        64:	e49db004 	pop	{fp}		; (ldr fp, [sp], #4)
//        68:	e12fff1e 	bx	lr