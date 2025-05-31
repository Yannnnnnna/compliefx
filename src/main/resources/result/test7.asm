; Generated Assembly Code (�Ż����м����)
; Generated at: 2025-05-30T20:14:28.500191700
; Compiler: ComplieFX2
; File: test7.asm
;

.MODEL SMALL
.DATA
    result DW 0    ; int variable (result)
    a DW 0    ; int variable (a)
    b DW 0    ; int variable (b)
    c1 DW 0    ; int variable (c1)
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
    ; a = 3
    MOV AX, 3
    MOV a, AX

    ; b = 4
    MOV AX, 4
    MOV b, AX

    ; c1 = 2
    MOV AX, 2
    MOV c1, AX

    ; t0 = 12
    MOV AX, 12
    MOV t0, AX

    ; t1 = -1
    MOV AX, -1
    MOV t1, AX

    ; t2 = -2
    MOV AX, -2
    MOV t2, AX

    ; t3 = 10
    MOV AX, 10
    MOV t3, AX

    ; t4 = 1.5 (mixed-type assignment)
    MOV AX, 15
    MOV t4, AX

    ; t5 = 11.5 (mixed-type assignment)
    MOV AX, 115
    MOV t5, AX

    ; result = 11.5 (mixed-type assignment)
    MOV AX, 115
    MOV result, AX

    ; t6 = result > 10
    MOV AX, result
    CMP AX, 10
    JG TRUE_0
    MOV t6, 0
    JMP END_1
TRUE_0:
    MOV t6, 1
END_1:

    ; ������ת: JZ t6 -> L0
    CMP t6, 0
    JE L0    ; Ϊ0ʱ��ת

    ; t7 = result - 5 (integer arithmetic)
    MOV AX, result
    SUB AX, 5
    MOV t7, AX

    ; result = t7
    MOV AX, t7
    MOV result, AX

L0:
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

    ; ������� result
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'result'
    MOV DL, 'r'
    MOV AH, 02h
    INT 21h
    MOV DL, 'e'
    MOV AH, 02h
    INT 21h
    MOV DL, 's'
    MOV AH, 02h
    INT 21h
    MOV DL, 'u'
    MOV AH, 02h
    INT 21h
    MOV DL, 'l'
    MOV AH, 02h
    INT 21h
    MOV DL, 't'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, result
    CALL PrintNumber

    LEA DX, msg_newline
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

    ; ������� c1
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'c1'
    MOV DL, 'c'
    MOV AH, 02h
    INT 21h
    MOV DL, '1'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, c1
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
