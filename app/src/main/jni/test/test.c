
int main (){
int a=0;
    __asm __volatile (
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