import sys
import nltk

def extractPeople(tree):
	entityNames = []

	if (hasattr(tree, 'label')):
		if (tree.label() == 'PERSON'):
			entityNames.append(' '.join([child[0] for child in tree]))
		else:
			for child in tree:
				entityNames.extend(extractPeople(child))

	return entityNames


sentence = "My name is Alexander the Great."
tokens = nltk.word_tokenize(sentence)
taggedTokens = nltk.pos_tag(tokens)
tree = nltk.ne_chunk(taggedTokens)

print(tree)
print(extractPeople(tree))