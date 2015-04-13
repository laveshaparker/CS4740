import re
import sys

pattern_file = str(sys.argv[1])

response_file = str(sys.argv[2])

pat_dict = dict()
f_p = open(pattern_file, 'r+')

total_number = 0
last_index = -1
patterns = []
for line in f_p : 
    index = line.split("\t")[0]
    if index == last_index : #Add to the current list
        patterns.append(line.split("\t")[1].strip())
    elif last_index == -1 : 
        last_index = index
        patterns.append(line.split("\t")[1].strip())
    else : 
        #start a new list
        pat_dict[last_index] = patterns
        total_number+=1
        patterns = []
        last_index = index
        patterns.append(line.split("\t")[1].strip())

pat_dict[last_index] = patterns




'''
Output format : 
Qid #Qid
1 R1
2 R2
3 R3
4 R4
5 R5
6 R6
7 R7
8 R8
9 R9
10 R10

Up to 10 responses : 
'''


score = 0.0
qid = -1
res_no = -1
matched = 0

f_r = open(response_file, 'r+')

for line in f_r : 
    if "qid" in line : 
        qid = int(line.split(" ")[1].strip())
        print qid
        matched = 0
    elif matched == 0 : 
        res_no = int(line.split(" ")[0])
        #res = line.split(" ")[1].strip()
        res = line[line.index(str(res_no))+len(str(res_no))+1 : ].strip()
        #print "Matching.."+res
        #check if matches the pattern
        patterns = pat_dict[str(qid)]
        for pattern in patterns : 
            p = re.compile(pattern, re.IGNORECASE)
            if p.match(res) : 
                if res_no > 10 : 
                    break
                score += (1.0/res_no)
                print res_no
                print "Response matched\t"+str(res)+"\t at position\t"+str(res_no)
                matched = 1
                break

print score
print score/total_number

