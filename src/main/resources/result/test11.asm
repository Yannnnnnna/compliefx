; Generated Assembly Code (ԭʼ�м����)
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

    ; �����ص��ַ����ͻ�����
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

    ; ������ת: JZ t0 -> L1
    CMP t0, 0
    JE L1    ; Ϊ0ʱ��ת

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

    ; ������ת: JZ t1 -> L3
    CMP t1, 0
    JE L3    ; Ϊ0ʱ��ת

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

    ; ������� i
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'i'
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

    ; ������� j
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'j'
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

    ; ������� sum
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'sum'
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
