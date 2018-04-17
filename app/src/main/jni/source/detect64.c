
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

//000000000000099c <detect1@@Base>:
//        99c:	d100c3ff 	sub	sp, sp, #0x30
//        9a0:	a9027bfd 	stp	x29, x30, [sp,#32]
//        9a4:	d2800000 	mov	x0, #0x0                   	// #0
//        9a8:	10ffffe1 	adr	x1, 9a4 <detect1@@Base+0x8>
//        9ac:	d2800002 	mov	x2, #0x0                   	// #0
//        9b0:	91000400 	add	x0, x0, #0x1
//        9b4:	f9400023 	ldr	x3, [x1]
//        9b8:	91000442 	add	x2, x2, #0x1
//        9bc:	10ffffe1 	adr	x1, 9b8 <detect1@@Base+0x1c>
//        9c0:	d1003021 	sub	x1, x1, #0xc
//        9c4:	f9000023 	str	x3, [x1]
//        9c8:	f100285f 	cmp	x2, #0xa
//        9cc:	5400008a 	b.ge	9dc <detect1@@Base+0x40>
//        9d0:	f100281f 	cmp	x0, #0xa
//        9d4:	5400004a 	b.ge	9dc <detect1@@Base+0x40>
//        9d8:	17fffff8 	b	9b8 <detect1@@Base+0x1c>
//        9dc:	aa0203e0 	mov	x0, x2
//        9e0:	a9427bfd 	ldp	x29, x30, [sp,#32]
//        9e4:	9100c3ff 	add	sp, sp, #0x30
//        9e8:	aa0003e0 	mov	x0, x0
//        9ec:	d65f03c0 	ret
