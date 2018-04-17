
int detect (){
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

int main(){
return detect ();
}
// 交叉编译后，反编译的二进制可执行代码如下
//arm-none-linux-gnueabi-objdump a.out -d
//1）disassemble
//      -d：反汇编可执行的section；
//      -D：反汇编全部；
 //     -d -j section_name：反汇编指定的section；


//
//

//0000000000000000 <detect>:
//        0:	d10043ff 	sub	sp, sp, #0x10
//        4:	b9000fff 	str	wzr, [sp,#12]

//        8:	d100c3ff 	sub	sp, sp, #0x30
//        c:	a9027bfd 	stp	x29, x30, [sp,#32]
//
//        0000000000000010 <smc>:
//           10:	d2800000 	mov	x0, #0x0                   	// #0
//          14:	10ffffe1 	adr	x1, 10 <smc>
//          18:	d2800002 	mov	x2, #0x0                   	// #0
//          1c:	91000400 	add	x0, x0, #0x1
//          20:	f9400023 	ldr	x3, [x1]
//
//          0000000000000024 <code>:
//          24:	91000442 	add	x2, x2, #0x1
//          28:	10ffffe1 	adr	x1, 24 <code>
//          2c:	d1003021 	sub	x1, x1, #0xc
//          30:	f9000023 	str	x3, [x1]
//          34:	f100285f 	cmp	x2, #0xa
//          38:	5400008a 	b.ge	48 <out>
//          3c:	f100281f 	cmp	x0, #0xa
//          40:	5400004a 	b.ge	48 <out>
//          44:	17fffff8 	b	24 <code>
//
//0000000000000048 <out>:
//        48:	aa0203e0 	mov	x0, x2
//        4c:	a9427bfd 	ldp	x29, x30, [sp,#32]
//        50:	9100c3ff 	add	sp, sp, #0x30


//        54:	aa0003e0 	mov	x0, x0
//        58:	b9000fe0 	str	w0, [sp,#12]
//        5c:	b9400fe0 	ldr	w0, [sp,#12]
//        60:	910043ff 	add	sp, sp, #0x10
//        64:	d65f03c0 	ret
