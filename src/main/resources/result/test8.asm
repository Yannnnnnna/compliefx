; Generated Assembly Code (�Ż����м����)
; Generated at: 2025-05-31T14:44:04.699556800
; Compiler: ComplieFX2
; File: test8.asm
;

.MODEL SMALL
.DATA
    x DW 0    ; int variable (x)
    y DW 0    ; int variable (y)
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
    ; x = 8
    MOV AX, 8
    MOV x, AX

    ; y = 3
    MOV AX, 3
    MOV y, AX

    ; t0 = true
    MOV AX, 1
    MOV t0, AX

    ; ������ת: JZ true -> L0
    MOV AX, 1
    CMP AX, 0
    JE L0    ; Ϊ0ʱ��ת

    ; t1 = true
    MOV AX, 1
    MOV t1, AX

    ; ������ת: JZ true -> L2
    MOV AX, 1
    CMP AX, 0
    JE L2    ; Ϊ0ʱ��ת

    ; t2 = 11
    MOV AX, 11
    MOV t2, AX

    ; x = 11
    MOV AX, 11
    MOV x, AX

    JMP L3
L2:
    ; t3 = 8
    MOV AX, 8
    MOV t3, AX

    ; x = 8
    MOV AX, 8
    MOV x, AX

L3:
    JMP L1
L0:
    ; t4 = 6
    MOV AX, 6
    MOV t4, AX

    ; y = 6
    MOV AX, 6
    MOV y, AX

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

    ; ������� y
    LEA DX, msg_var_prefix
    MOV AH, 09h
    INT 21h

    ; ��������� 'y'
    MOV DL, 'y'
    MOV AH, 02h
    INT 21h
    LEA DX, msg_equals
    MOV AH, 09h
    INT 21h

    MOV AX, y
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
