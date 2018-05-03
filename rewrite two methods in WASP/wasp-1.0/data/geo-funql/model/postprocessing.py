#coding:utf-8
import codecs,sys
###
###
def getdict():
	NLdict = set()
	MRdict = set()
	for line in open("grammar"):
		array = line.replace("[X]","").replace("[X,1]","").replace("[X,2]","").replace("[X,3]","").split("|||")
		for c in array[1].split(" "):
			#print c.strip()
			NLdict.add(c.strip())
		for c in array[2].split(" "):
			#print c.strip()
			MRdict.add(c.strip())
			MRdict.add(c.strip().replace("'",""))
		#

	NLonly = NLdict - MRdict
	return NLonly


NLonly = getdict()
print list(NLonly)

resoutput  = "testMRRes.txt"
transResFromDecoder = "output"

if len(sys.argv)==3:
	transResFromDecoder = sys.argv[1]
	resoutput  = sys.argv[2]

else:
	print "useage: "+sys.argv[0]+"evlFileName"
	print "using default parameter: " + transResFromDecoder + " " +  resoutput

testRes = codecs.open(resoutput,"w","utf-8")
for line in open(transResFromDecoder):
	line = line.strip()

	MrSent=[]
	for e in line.split(" "):
		if e not in NLonly:
			MrSent.append(e)
		else:
			print e
	line = ' '.join(MrSent)
	line = "answer(" + line
	leftPNum = line.count("(")
	line += ")" * leftPNum
	testRes.write(line+"\n")

