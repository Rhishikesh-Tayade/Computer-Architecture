	.data
a:
	10
	.text
main:
	load %x0, $a, %x4		
	addi %x0, 2, %x3		
loop:
	beq %x3, %x4, success	
	div %x4, %x3, %x5		
	beq %x0, %x31, fail		
	addi %x3, 1, %x3		
	jmp loop				
fail:
	subi %x0, 1, %x10		
	end						
success:
	addi %x0, 1, %x10		
	end						