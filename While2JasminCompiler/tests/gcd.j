.class public gcd
.super java/lang/Object
;
; standard initializer
.method public <init>()V
  aload_0
  invokenonvirtual java/lang/Object/<init>()V
  return
.end method

.method public static main([Ljava/lang/String;)V
  ; set limits used by this method
  .limit locals 100
  .limit stack 100
ldc 0
istore 1
ldc 0
istore 2
; int n = Integer.parseInt(System.console().readLine());
; Console c = System.console();
invokestatic java/lang/System/console()Ljava/io/Console;
; Reads one line and stores in a String
invokevirtual java/io/Console/readLine()Ljava/lang/String;
; Parse String to int, do not handle exceptions
invokestatic java/lang/Integer/parseInt(Ljava/lang/String;)I
istore 1
; int n = Integer.parseInt(System.console().readLine());
; Console c = System.console();
invokestatic java/lang/System/console()Ljava/io/Console;
; Reads one line and stores in a String
invokevirtual java/io/Console/readLine()Ljava/lang/String;
; Parse String to int, do not handle exceptions
invokestatic java/lang/Integer/parseInt(Ljava/lang/String;)I
istore 2
while0:
iload 1
iload 2
if_icmpne reltrue0
ldc 0
goto endrel0
reltrue0:
ldc 1
endrel0:
ifeq done0
iload 1
iload 2
if_icmple reltrue1
ldc 0
goto endrel1
reltrue1:
ldc 1
endrel1:
iload 1
iload 2
if_icmple reltrue2
ldc 0
goto endrel2
reltrue2:
ldc 1
endrel2:
iand
ifeq else0
iload 2
iload 1
isub
istore 2
goto endif0
else0:
iload 1
iload 2
isub
istore 1
endif0:
goto while0
done0:
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "GCD: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
iload 1
invokestatic java/lang/String/valueOf(I)Ljava/lang/String;
; begin syso
astore 0 	; store string object in register 0
getstatic java/lang/System/out Ljava/io/PrintStream;
aload 0   ; load the string
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
; end syso
; done
return

.end method
