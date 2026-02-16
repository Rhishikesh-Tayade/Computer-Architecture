    .data
a:
    10
    .text
main:
    load %x0, $a, %x3
    addi %x0, 2, %x4
	addi %x0, 1, %x5
loop:
    beq %x3, %x0, even
    beq %x3, %x5, odd
    subi %x3, 2, %x3
    jmp loop
even:
    subi %x0, 1, %x10 
    end
odd:
    addi %x0, 1, %x10
    end