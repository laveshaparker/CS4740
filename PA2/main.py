import sys
import question_formatter

dataSet = "dev" # sys.argv[1]
questionNumber = int(sys.argv[1])

# This gives you an array of the loaded questions
questions = question_formatter.loadQuestions(dataSet)

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

printQuestion(questionNumber)
