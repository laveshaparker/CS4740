import sys
import question_formatter

dataSet = "dev" # sys.argv[1]
questionNumber = 0 #int(sys.argv[2])

# This gives you an array of the loaded questions
questions = question_formatter.loadQuestions(dataSet)

# This is just an example of the fields you can accessssss
def printQuestion(i):
	print(questions[i].number)
	print(questions[i].questionCaseSensitive)
	print(questions[i].questionCaseInsensitive)
	print(questions[i].contentWordsCaseSensitive)
	print(questions[i].contentWordsCaseInsensitive)
	print(questions[i].requiredEntity)

printQuestion(questionNumber)
printQuestion(questionNumber + 1)
printQuestion(questionNumber + 2)