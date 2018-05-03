#coding:utf-8
import codecs

# lexe2f = codecs.open("lex.e2f","w","utf-8")
# lexf2e = codecs.open("lex.f2e","w","utf-8")
grammarFiltered = codecs.open("lex.f2e","w","utf-8")
scfgInitRules = codecs.open("scfg-init-rules.txt","w","utf-8")
for line in open("scfg-init-rules"):
	nlword = line[line.find("({") + 2 : line.find("})")]
	nlword = nlword.strip()
	line = line[line.find("})") + 2:]

	mrword = line[line.find("({") + 2 : line.find("})")]
	mrword = mrword.replace("' ","'").replace(" '","'").strip()

	# lexe2f.write(nlword+" "+mrword+" 1.0000000\n")
	# lexf2e.write(mrword +" "+nlword+" 1.0000000\n")
	#Arkansas ||| 'arkansas' ||| 0.98525 1 0.98525 1 ||| 0-0 ||| 5 5 5 ||| |||


	gf = mrword +" "+nlword+" 1.0000000\n"
	# grammarFiltered

	#Hireo Grammar
	#[X] ||| Arkansas ||| 'arkansas' ||| 0 0 1 0.13534 0 0 ||| 0-0
	if len(nlword.split(" "))==1:
		scfgInitRules.write("[X] ||| "+nlword+" ||| "+mrword+" ||| 5 5 5 5 0 0 ||| 0-0\n")
	if len(nlword.split(" "))==2:
		scfgInitRules.write("[X] ||| "+nlword+" ||| "+mrword+" ||| 5 5 5 5 0 0 ||| 0-0 1-1\n")
	if len(nlword.split(" "))==3:
		scfgInitRules.write("[X] ||| "+nlword+" ||| "+mrword+" ||| 5 5 5 5 0 0 ||| 0-0 1-1 2-2\n")

