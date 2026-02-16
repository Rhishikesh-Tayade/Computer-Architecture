	.data
a:
	10
	.text
main:
	load %x0, $a, %x3 
	add %x0, %x0, %x4 
loop:
	beq %x3, %x0, check
	muli %x4, 10, %x4
	divi %x3, 10, %x3 
	add %x4, %x31, %x4
	jmp loop
check:
	load %0, $a, %x5
	beq %x4, %x5, pass
	subi %x0, 1, %x10
	end
pass:
	addi %x0, 1, %x10 
	end