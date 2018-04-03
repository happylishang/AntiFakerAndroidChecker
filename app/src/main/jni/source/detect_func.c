
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

