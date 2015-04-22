import nltk
import question_formatter


from nltk.corpus import wordnet
from nltk.sem import relextract

question = question_formatter.Question(0, "When is the Tulip Festival in Michigan?")
sentence = "Then in November, history was made when van Gogh's ``Irises'' a richly-colored composition of purple flowers and green leaves set off by a single white iris _ sold at Sotheby's in New York for $53.9 million to an anonymous buyer. It was the highest price paid at auction for any painting."

taggedTokens = nltk.pos_tag(question.tokensCaseSensitive)
tree = nltk.ne_chunk(taggedTokens)

print(tree)
