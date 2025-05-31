; Generated Assembly Code (优化后中间代码)
; Generated at: 2025-05-30T16:52:05.632736800
; Compiler: ComplieFX2
; File: test6.asm
;

.MODEL SMALL
.DATA
    a DW 0    ; int variable (a)
    b DW 0    ; int variable (b)
    x DW 0    ; int variable (x)
    t4 DW 0    ; temporary variable (t4)
    t5 DW 0    ; temporary variable (t5)
    t6 DW 0    ; temporary variable (t6)
    t7 DW 0    ; temporary variable (t7)
    t0 DW 0    ; temporary variable (t0)
    t1 DW 0    ; temporary variable (t1)
    t2 DW 0    ; temporary variable (t2)
    t3 DW 0    ; temporary variable (t3)

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
    ; a = 1
    MOV AX, 1
    MOV a, AX

    ; b = 0
    MOV AX, 0
    MOV b, AX

    ; x = 5
    MOV AX, 5
    MOV x, AX

    ; t0 = true
    MOV AX, 1
    MOV t0, AX

    ; 条件跳转: JZ 1 -> L0
    MOV AX, 1
    CMP AX, 0
    JE L0    ; 为0时跳转

    ; 条件跳转: JZ true -> L0
    MOV AX, 1
    CMP AX, 0
    JE L0    ; 为0时跳转

    ; t1 = true
    MOV AX, 1
    MOV t1, AX

    JMP L2
L0:
    ; t1 = false
    MOV AX, 0
    MOV t1, AX

L2:
    ; t2 = false != 0
    MOV AX, 0
    CMP AX, 0
    JNE TRUE_0
    MOV t2, 0
    JMP END_1
TRUE_0:
    MOV t2, 1
END_1:

    ; 条件跳转: JZ t2 -> L3
    CMP t2, 0
    JE L3    ; 为0时跳转

    ; t3 = 6
    MOV AX, 6
    MOV t3, AX

    ; x = 6
    MOV AX, 6
    MOV x, AX

L3:
    ; t4 = true
    MOV AX, 1
    MOV t4, AX

    ; 条件跳转: JNZ 0 -> L5
    MOV AX, 0
    CMP AX, 0
    JNE L5   ; 不为0时跳转

    ; 条件跳转: JNZ true -> L5
    MOV AX, 1
    CMP AX, 0
    JNE L5   ; 不为0时跳转

    ; t5 = false
    MOV AX, 0
    MOV t5, AX

    JMP L7
L5:
    ; t5 = true
    MOV AX, 1
    MOV t5, AX

L7:
    ; t6 = true != 0
    MOV AX, 1
    CMP AX, 0
    JNE TRUE_2
    MOV t6, 0
    JMP END_3
TRUE_2:
    MOV t6, 1
END_3:

    ; 条件跳转: JZ t6 -> L8
    CMP t6, 0
    JE L8    ; 为0时跳转

    ; t7 = 12
    MOV AX, 12
    MOV t7, AX

    ; x = 12
    MOV AX, 12
    MOV x, AX

L8:
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

    ; 输出变量 x
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; 输出变量名 'x'
    MOV DL, 'x'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, x
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
