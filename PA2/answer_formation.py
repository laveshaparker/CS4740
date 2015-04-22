import sys
import nltk

from question_formatter import *
from passage_retrieval import *
from nltk.corpus import wordnet
from nltk.sem import relextract

i = int(sys.argv[1])

questions = loadQuestions(PassageRetrieval.DEV)
question1 = PassageRetrieval(questions[i], PassageRetrieval.DEV)
TFIDF(question1)

print(questions[i].questionCaseSensitive)
print(questions[i].descriptor.relevantTokens)
print(question1.top_passage)


taggedTokens = nltk.pos_tag(question1.top_passage)
tree = nltk.ne_chunk(taggedTokens)

print(tree)
