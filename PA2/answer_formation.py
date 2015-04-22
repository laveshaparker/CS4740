import nltk
import question_formatter

from nltk.sem import relextract

question = question_formatter.Question(0, "What flower did Vincent Van Gogh paint?")
sentence = "Edmonton Oiler owner Peter Pocklington told reporters that day a year ago that Gretzky had asked to be traded to the Kings because he wanted to spend more time with his bride, Janet Jones, and that he had let Gretzky go with a heavy heart."

tokens = nltk.word_tokenize(sentence)
taggedTokens = nltk.pos_tag(tokens)

tree = nltk.ne_chunk(taggedTokens)

pairs = relextract.tree2semi_rel(tree)
print(pairs[5])
