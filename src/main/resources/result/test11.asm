; Generated Assembly Code (原始中间代码)
; Generated at: 2025-05-31T14:54:23.008799500
; Compiler: ComplieFX2
; File: test11.asm
;

.MODEL SMALL
.DATA
    i DW 0    ; int variable (i)
    j DW 0    ; int variable (j)
    sum DW 0    ; int variable (sum)
    t4 DW 0    ; temporary variable (t4)
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
.STACK 100h

.CODE
START:
    MOV AX, @DATA
    MOV DS, AX

MAIN_PROC:
    ; i = 1
    MOV AX, 1
    MOV i, AX

    ; sum = 0
    MOV AX, 0
    MOV sum, AX

L0:
    ; t0 = i <= 10
    MOV AX, i
    CMP AX, 10
    JLE TRUE_0
    MOV t0, 0
    JMP END_1
TRUE_0:
    MOV t0, 1
END_1:

    ; 条件跳转: JZ t0 -> L1
    CMP t0, 0
    JE L1    ; 为0时跳转

    ; j = 1
    MOV AX, 1
    MOV j, AX

L2:
    ; t1 = j <= i
    MOV AX, j
    CMP AX, i
    JLE TRUE_2
    MOV t1, 0
    JMP END_3
TRUE_2:
    MOV t1, 1
END_3:

    ; 条件跳转: JZ t1 -> L3
    CMP t1, 0
    JE L3    ; 为0时跳转

    ; t2 = sum + j
    MOV AX, sum
    ADD AX, j
    MOV t2, AX

    ; sum = t2
    MOV AX, t2
    MOV sum, AX

    ; t3 = j + 1
    MOV AX, j
    ADD AX, 1
    MOV t3, AX

    ; j = t3
    MOV AX, t3
    MOV j, AX

    JMP L2
L3:
    ; t4 = i + 1
    MOV AX, i
    ADD AX, 1
    MOV t4, AX

    ; i = t4
    MOV AX, t4
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

    ; 输出变量 j
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; 输出变量名 'j'
    MOV DL, 'j'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, j
    CALL PrintNumber

    LEA DX, msg_newline
    MOV AH, 09h
    INT 21h

    ; 输出变量 sum
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; 输出变量名 'sum'
    MOV DL, 's'
    MOV AH, 02h
    INT 21h
    MOV DL, 'u'
    MOV AH, 02h
    INT 21h
    MOV DL, 'm'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, sum
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
