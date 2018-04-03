
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

    return 0;
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


