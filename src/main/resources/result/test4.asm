; Generated Assembly Code (优化后中间代码)
; Generated at: 2025-05-30T16:44:07.285598500
; Compiler: ComplieFX2
; File: test4.asm
;

.MODEL SMALL
.DATA
    a DW 0    ; int variable (a)
    b DW 0    ; int variable (b)
    t0 DW 0    ; temporary variable (t0)
    t1 DW 0    ; temporary variable (t1)
    t2 DW 0    ; temporary variable (t2)

    ; 输出相关的字符串和缓冲区
    msg_header DB 'Program execution results:', 0Dh, 0Ah, '$'
    msg_separator DB '=', '=', '=', '=', '=', '=', '=', '=', '=', '=', 0Dh, 0Ah, '$'
    msg_success DB 'Program executed successfully!', 0Dh, 0Ah, '$'
    msg_var_prefix DB '  ', '$'
    msg_equals DB ' = ', '$'
    msg_newline DB 0Dh, 0Ah, '$'
    msg_press_key DB 'Press any key to exit...', '$'
    number_buffer DB 8 DUP(0), '$'    ; 数字转字符串缓冲区
.STACK 100h

.CODE
START:
    MOV AX, @DATA
    MOV DS, AX

MAIN_PROC:
    ; a = 10
    MOV AX, 10
    MOV a, AX

    ; b = 5
    MOV AX, 5
    MOV b, AX

    ; t0 = true
    MOV AX, 1
    MOV t0, AX

    ; 条件跳转: JZ true -> L0
    MOV AX, 1
    CMP AX, 0
    JE L0    ; 为0时跳转

    ; t1 = 11
    MOV AX, 11
    MOV t1, AX

    ; a = 11
    MOV AX, 11
    MOV a, AX

    JMP L1
L0:
    ; t2 = 6
    MOV AX, 6
    MOV t2, AX

    ; b = 6
    MOV AX, 6
    MOV b, AX

L1:
    ; ===== 输出程序执行结果 =====
    LEA DX, msg_header
    MOV AH, 09h
    INT 21h

    LEA DX, msg_separator
    MOV AH, 09h
    INT 21h

    LEA DX, msg_success
    MOV AH, 09h
    INT 21h

    ; 输出变量 a
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; 输出变量名 'a'
    MOV DL, 'a'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, a
    CALL PrintNumber

    LEA DX, msg_newline
    MOV AH, 09h
    INT 21h

    ; 输出变量 b
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; 输出变量名 'b'
    MOV DL, 'b'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, b
    CALL PrintNumber

    LEA DX, msg_newline
    MOV AH, 09h
    INT 21h

    LEA DX, msg_separator
    MOV AH, 09h
    INT 21h

    LEA DX, msg_press_key
    MOV AH, 09h
    INT 21h

    ; 等待按键
    MOV AH, 01h
    INT 21h


    MOV AH, 4Ch    ; DOS程序结束
    INT 21h

; ===== 辅助函数 =====

PrintNumber PROC
    ; 输入: AX = 要输出的数字
    ; 保存寄存器
    PUSH BX
    PUSH CX
    PUSH DX
    PUSH SI

    ; 处理负数
    MOV CX, 0         ; 数字位数计数器
    MOV BX, 10        ; 除数
    CMP AX, 0
    JGE PositiveNumber

    ; 输出负号
    PUSH AX
    MOV DL, '-'
    MOV AH, 02h
    INT 21h
    POP AX
    NEG AX            ; 转为正数

PositiveNumber:
    ; 特殊处理0
    CMP AX, 0
    JNE ConvertLoop
    MOV DL, '0'
    MOV AH, 02h
    INT 21h
    JMP PrintNumberEnd

ConvertLoop:
    ; 将数字转换为字符并压栈
    CMP AX, 0
    JE PrintLoop
    XOR DX, DX        ; 清除高位
    DIV BX            ; AX = AX/10, DX = AX%10
    ADD DL, '0'       ; 转换为ASCII
    PUSH DX           ; 压栈保存
    INC CX            ; 位数+1
    JMP ConvertLoop

PrintLoop:
    ; 从栈中弹出并输出字符
    CMP CX, 0
    JE PrintNumberEnd
    POP DX
    MOV AH, 02h
    INT 21h
    DEC CX
    JMP PrintLoop

PrintNumberEnd:
    ; 恢复寄存器
    POP SI
    POP DX
    POP CX
    POP BX
    RET
PrintNumber ENDP


END START
