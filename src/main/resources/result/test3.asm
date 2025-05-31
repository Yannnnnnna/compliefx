; Generated Assembly Code (原始中间代码)
; Generated at: 2025-05-30T16:40:38.280023400
; Compiler: ComplieFX2
; File: test3.asm
;

.MODEL SMALL
.DATA
    i DW 0    ; int variable (i)
    t0 DW 0    ; temporary variable (t0)
    t1 DW 0    ; temporary variable (t1)

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
    ; i = 0
    MOV AX, 0
    MOV i, AX

L0:
    ; t0 = i < 5
    MOV AX, i
    CMP AX, 5
    JL TRUE_0
    MOV t0, 0
    JMP END_1
TRUE_0:
    MOV t0, 1
END_1:

    ; 条件跳转: JZ t0 -> L1
    CMP t0, 0
    JE L1    ; 为0时跳转

    ; t1 = i + 1
    MOV AX, i
    ADD AX, 1
    MOV t1, AX

    ; i = t1
    MOV AX, t1
    MOV i, AX

    JMP L0
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

    ; 输出变量 i
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; 输出变量名 'i'
    MOV DL, 'i'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, i
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
