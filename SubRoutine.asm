MAKRO:JMP(A)
   BNZ(t1,A)
t1: 1
END

MAKRO:CLR(A)
Start:BNZ(A,t1)
      JMP(End)
#ist DEC ein Grundbefehl, so muss die Zeile t1: DEC ein und die
#Zeile t1: INC auskommentiert werden, wegen Rekursion
#t1:   DEC(A)
t1:   INC(A)
      JMP(Start)
END

#Existiert in der RAMonTM schon
#MAKRO:INC(A)
#      JMP(Start)
#RLen: 0
#Start:CLR(RLen)
#      DEC(Rlen)
#S1:   DEC(A)
#      DEC(Rlen)
#      BNZ(RLen,S1)
#END

MAKRO:DEC(A)
      JMP(Start)
RLen: 0
Start:CLR(RLen)
      INC(Rlen)
S1:   INC(A)
      INC(Rlen)
      BNZ(RLen,S1)
END

#springt an Adresse B, wenn A==0 ist
MAKRO:BZ(A,B)
      BNZ(A,End)
      JMP(B)
END

MAKRO:CPY(A,B)
      JMP(Start) 
t1:   0
Start:CLR(B)
      BZ(A,End)
S1:   DEC(A)
      INC(t1)
      INC(B)
      BNZ(A,S1)
S2:   DEC(t1)
      INC(A)
      BNZ(t1,S2)
END

MAKRO:ADD(A,B,C)
      JMP(Start)
t1:   0
t2:   0
Start:CPY(A,C)
      CPY(B,t2)
      BZ(t2,End)
S1:   INC(C)
      DEC(t2)
      BNZ(t2,S1)
END

MAKRO:SUB(A,B,C)
      JMP(Start)
t1:   0
t2:   0
Start:CPY(A,C)
      CPY(B,t2)
      BZ(t2,End)
S1:   DEC(C)
      DEC(t2)
      BNZ(t2,S1)
END

#springt an Adresse C, wenn A==B ist
MAKRO:BEQ(A,B,C)
      JMP(Start)
t1:   0
Start:SUB(A,B,t1)
      BZ(t1,C)
END

#schreibt Adress C 1 rein, wenn A==B ist, sonst 0
MAKRO:EQU(A,B,C)
      JMP(Start)
t1:   0
t2:   1
Start:SUB(A,B,t1)
      BZ(t1,Ret1)
      CPY(t0,C)
      JMP(End)
Ret1: CPY(t1,C)
END

#schreibt Adress C 1 rein, wenn A!=B ist, sonst 0
MAKRO:NEQ(A,B,C)
      JMP(Start)
t1:   0
t2:   1
Start:SUB(A,B,t1)
      BNZ(t1,Ret1)
      CPY(t0,C)
      JMP(End)
Ret1: CPY(t1,C)
END

# springt an Adresse C, wenn A>B ist
MAKRO:BGT(A,B,C)
      JMP(Start)
t1:   0
t2:   0
Start:BEQ(A,B,End)
      CPY(A,t1)
      CPY(B,t2)
S1:   BZ(t1,End)
      BZ(t2,C)
      DEC(t1)
      DEC(t2)
      JMP(S1)
END

#schreibt in Adresse C 1, wenn A>B ist, sonst 0
MAKRO:GT(A,B,C)
      JMP(Start)
t0:   0
t1:   1
Start:BGT(A,B,Ret1)
      CPY(t0,C)
      JMP(End)
Ret1: CPY(t1,C)
END

MAKRO:BGE(A,B,C)
      JMP(Start)
t1:   0
t2:   0
Start:BEQ(A,B,C)
      CPY(A,t1)
      CPY(B,t2)
S1:   BZ(t1,End)
      BZ(t2,C)
      DEC(t1)
      DEC(t2)
      JMP(S1)
END

#schreibt in Adresse C 1, wenn A>=B ist, sonst 0
MAKRO:GTE(A,B,C)
      JMP(Start)
t0:   0
t1:   1
Start:BGE(A,B,Ret1)
      CPY(t0,C)
      JMP(End)
Ret1: CPY(t1,C)
END

MAKRO:MUL(A,B,C)
      JMP(Start)
t1:   0
t2:   0
t3:   0
Start:CLR(C)
      CLR(t2)
      BZ(A,End)
      BZ(B,End)
      CPY(B,t1)
S1:   ADD(A,t2,t3)
      CPY(t3,t2)
      DEC(t1)
      BNZ(t2,C)
      CPY(t2,C)
END


MAKRO:DIV(A,B,C)
      JMP(Start)
t1:   0
t2:   0
t3:   0
Start:CLR(C)
      CLR(t3)
      BZ(A,End)
      BZ(B,End)
      BGT(B,A,End)
      CPY(A,t1)
S1:   SUB(t1,B,t2)
      CPY(t2,t1)
      INC(C)
      BGT(B,t1,End)
      JMP(S1)
END

#
# logische Operatoren
#

#wenn A!=0 und B!=0, dann C=1,sonst C=0
MAKRO:AND(A,B,C)
      JMP(Start)
t0:   0
t1:   1
Start:BZ(A,Ret0)
      BZ(B,Ret0)
      CPY(t1,C)
      JMP(End)
Ret0: CPY(t0,C)
END

#wenn A!=0 oder B!=0, dann C=1,sonst C=0
MAKRO:OR(A,B,C)
      JMP(Start)
t0:   0
t1:   1
Start:BNZ(A,Ret1)
      BNZ(B,Ret1)
      CPY(t0,C)
      JMP(End)
Ret1: CPY(t1,C)
END

#wenn A!=0, dann B=0,sonst B=1
MAKRO: NOT(A,B)
       JMP(Start)
t0:    0
t1:    1
Start: BNZ(A,Ret0)
       CPY(t1,B)
       JMP(End)
Ret0:  CPY(t0,B)
END

#
#Speicherzugriffe
#

#kopiert Inhalt von A nach Adresse B
MAKRO:Var2Adr(A,B)
      INC(R1:A)
      DEC(A)
      CPY(B,R1)
END
#kopiert Adresse nach B
MAKRO:Adr2Var(A,B)
      INC(R1:A)
      DEC(A)
      CPY(R1,B)
END

#C = A[B]
MAKRO:LD(A,B,C)
      JMP(Start)
t1:   0
r0:   0
Start:Adr2Var(A,r0)
      ADD(r0,B,t1)
      Var2Adr(A,t1)
      CPY(A,C)
      Var2Adr(A,r0)
END      

#A[B] = C
MAKRO:ST(A,B,C)
      JMP(Start)
t1:   0
r0:   0
Start:Adr2Var(A,r0)
      ADD(r0,B,t1)
      Var2Adr(A,t1)
      CPY(C,A)
      Var2Adr(A,r0)
END      

#speichert inhalt von Adresse Stk+Pos in Val
MAKRO:POP(Stk,Pos,Val)
      ST(Stk,Pos,Val)
      DEC(Pos)      
END

# legt Val auf Adresse Stk+Pos ab
MAKRO:PSH(Stk,Pos,Val)
      INC(Pos)      
      LD(Stk,Pos,Val)
END

MAKRO:JSR(Stk,Pos,Next,Jump)
      JMP(Start)
Adr:  0
t  :  0
Start:LD(Next,t,Adr)
      PSH(Stk,Pos,Adr)
      JMP(Jump)
END

MAKRO:RTS(Stk,Pos)
      JMP(Start)
Adr:  0
t:    1
Start:POP(Stk,Pos,Adr)
      CPY(Adr,R1)
      BNZ(t,R1:Adr)
END
