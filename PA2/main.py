import sys
from question_formatter import *
from passage_retrieval import *

dataSet = "dev" # sys.argv[1]
questionNumber = int(sys.argv[1])

# This gives you an array of the loaded questions
questions = loadQuestions(dataSet)

# This is just an example of the fields you can accessssss
def printQuestion(i):
	print(questions[i].number)
	print(questions[i].questionCaseSensitive)
	print(questions[i].questionCaseInsensitive)
	print(questions[i].tokensWithPOS)
	print(questions[i].nerTree)
	print(questions[i].contentWordsCaseSensitive)
	print(questions[i].contentWordsCaseInsensitive)
	print(questions[i].descriptor.entityType)
	print(questions[i].descriptor.relevantTokens)

def main(i):
    questions = loadQuestions(PassageRetrieval.DEV)
    question1 = PassageRetrieval(questions[i], PassageRetrieval.DEV)
    tfidf = TFIDF(question1)
    print(questions[i].questionCaseSensitive)
    print(questions[i].descriptor.relevantTokens)
    print(question1.top_passage)

main(int(sys.argv[1]))
