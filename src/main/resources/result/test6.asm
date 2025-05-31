; Generated Assembly Code (�Ż����м����)
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

    ; �����ص��ַ����ͻ�����
    msg_header DB 'Program execution results:', 0Dh, 0Ah, '$'
    msg_separator DB '=', '=', '=', '=', '=', '=', '=', '=', '=', '=', 0Dh, 0Ah, '$'
    msg_success DB 'Program executed successfully!', 0Dh, 0Ah, '$'
    msg_var_prefix DB '  ', '$'
    msg_equals DB ' = ', '$'
    msg_newline DB 0Dh, 0Ah, '$'
    msg_press_key DB 'Press any key to exit...', '$'
    number_buffer DB 8 DUP(0), '$'    ; ����ת�ַ���������
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

    ; ������ת: JZ 1 -> L0
    MOV AX, 1
    CMP AX, 0
    JE L0    ; Ϊ0ʱ��ת

    ; ������ת: JZ true -> L0
    MOV AX, 1
    CMP AX, 0
    JE L0    ; Ϊ0ʱ��ת

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

    ; ������ת: JZ t2 -> L3
    CMP t2, 0
    JE L3    ; Ϊ0ʱ��ת

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

    ; ������ת: JNZ 0 -> L5
    MOV AX, 0
    CMP AX, 0
    JNE L5   ; ��Ϊ0ʱ��ת

    ; ������ת: JNZ true -> L5
    MOV AX, 1
    CMP AX, 0
    JNE L5   ; ��Ϊ0ʱ��ת

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

    ; ������ת: JZ t6 -> L8
    CMP t6, 0
    JE L8    ; Ϊ0ʱ��ת

    ; t7 = 12
    MOV AX, 12
    MOV t7, AX

    ; x = 12
    MOV AX, 12
    MOV x, AX

L8:
    ; ===== �������ִ�н�� =====
    LEA DX, msg_header
    MOV AH, 09h
    INT 21h

    LEA DX, msg_separator
    MOV AH, 09h
    INT 21h

    LEA DX, msg_success
    MOV AH, 09h
    INT 21h

    ; ������� a
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'a'
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

    ; ������� b
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'b'
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

    ; ������� x
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'x'
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

    ; �ȴ�����
    MOV AH, 01h
    INT 21h


    MOV AH, 4Ch    ; DOS�������
    INT 21h

; ===== �������� =====

PrintNumber PROC
    ; ����: AX = Ҫ���������
    ; ����Ĵ���
    PUSH BX
    PUSH CX
    PUSH DX
    PUSH SI

    ; ������
    MOV CX, 0         ; ����λ��������
    MOV BX, 10        ; ����
    CMP AX, 0
    JGE PositiveNumber

    ; �������
    PUSH AX
    MOV DL, '-'
    MOV AH, 02h
    INT 21h
    POP AX
    NEG AX            ; תΪ����

PositiveNumber:
    ; ���⴦��0
    CMP AX, 0
    JNE ConvertLoop
    MOV DL, '0'
    MOV AH, 02h
    INT 21h
    JMP PrintNumberEnd

ConvertLoop:
    ; ������ת��Ϊ�ַ���ѹջ
    CMP AX, 0
    JE PrintLoop
    XOR DX, DX        ; �����λ
    DIV BX            ; AX = AX/10, DX = AX%10
    ADD DL, '0'       ; ת��ΪASCII
    PUSH DX           ; ѹջ����
    INC CX            ; λ��+1
    JMP ConvertLoop

PrintLoop:
    ; ��ջ�е���������ַ�
    CMP CX, 0
    JE PrintNumberEnd
    POP DX
    MOV AH, 02h
    INT 21h
    DEC CX
    JMP PrintLoop

PrintNumberEnd:
    ; �ָ��Ĵ���
    POP SI
    POP DX
    POP CX
    POP BX
    RET
PrintNumber ENDP


END START
