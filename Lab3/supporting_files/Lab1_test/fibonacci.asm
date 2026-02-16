	.data
n:
	25
	.text
main:
	load %x0, $n, %x3
	addi %x0, 65535, %x7
	addi %x0, 0, %x4
	store %x0, 0, %x7
	subi %x7, 1, %x7 
	addi %x4, 1, %x4
	store %x4, 0, %x7
	subi %x7, 1, %x7 
	subi %x3, 2, %x3
loop:
	beq %x3, %x0, done
	addi %x7, 1, %x6 
	load %x6, 0, %x5 
	addi %x6, 1, %x6
	load %x6, 0, %x6  
	add %x5, %x6, %x4
	store %x4, 0, %x7
	subi %x7, 1, %x7 
	subi %x3, 1, %x3
	jmp loop
done:
	end