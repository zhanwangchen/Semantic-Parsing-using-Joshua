# covert grammars extract by moses to that scfg of WASP.
out = file("newscfg.txt","w")
for line in open("grammar"):
	array = line.split("|||")
	if "[X" in array[1]:
		newline ="*n:Query -> "+"({ *t:Bound "+array[1].replace("[X,1]","*n:X#1").replace("[X,2]","*n:X#2") +" *t:Bound })({ "+array[2].replace("[X,1]","*n:X#1").replace("[X,2]","*n:X#2")  +" }) weight 0.0"
		#*n:Query -> ({ *t:Bound What is *n:River#1 *t:Bound })({ answer ( *n:River#1 ) }) weight 0.0
		#[X] ||| How many citizens [X,1] ||| answer population_1 [X,1] ||| 0 8.66167 1 1.00000 0 4.70953 ||| 0-0 2-1
		#*n:Query -> ({ *t:Bound How many citizens *n:X#1 *t:Bound })({ answer ( *n:X#1 ) }) weight 0.0

		#[X] ||| Arkansas . ||| stateid 'arkansas' |||
		#*n:X -> ({ Arkansas . })({ stateid 'arkansas' }) weight 0.0
		#*n:StateName -> ({ wyoming })({ ' wyoming ' }) weight 0.0
	else:
		newline = "*n:X -> ({ "+ array[1].strip() + " })({ "+array[2].strip()+" }) weight 0.0"

	out.write(newline+"\n")
