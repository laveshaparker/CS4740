import nltk

class Question:
    '''
    Represents a single question in one of the questions.txt files. 
    (I'll improve this over the next few days)

    You won't need to instantiate this class. Just call the "loadQuestions(str)" function below
    instance.number = the the question number
    instance.questionCaseSensitive = The original question string
    instance.questionCaseInsensitive = The original question string with all lowercase letters
    instance.tokensCaseSensitive = The original question as an array of tokens
    instance.tokensCaseInsensitive = The original question as an array of tokens w/ all lowercase letters
    instance.contentWordsCaseSensitive = Array of tokens with stop-words removed
    instance.contentWordsCaseInsensitive = Self-explanatory
    instance.requiredEntity = My best guess at why type of entity the question is aksing from
                              PERSON, PLACE, TIME, NOUN PHRASE
    '''

    STOPWORDS = nltk.corpus.stopwords.words("english")
    REQUIREDENTITIES = {
        "who": "PERSON",
        "where" : "PLACE",
        "when" : "TIME",
        "what" : "NOUN PHRASE"
    }

    def __init__(self, n, s):
        self.number = n
        self.questionCaseSensitive = s
        self.questionCaseInsensitive = s.lower()
        self.tokensCaseSensitive = nltk.word_tokenize(self.questionCaseSensitive)
        self.tokensCaseInsensitive = nltk.word_tokenize(self.questionCaseInsensitive)
        self.contentWordsCaseSensitive = [word for word in self.tokensCaseInsensitive if word not in self.STOPWORDS]
        self.contentWordsCaseInsensitive = [word.lower() for word in self.contentWordsCaseSensitive]
        self.requiredEntity = [self.REQUIREDENTITIES[key] for key in self.REQUIREDENTITIES if key in self.tokensCaseInsensitive]
        if (self.requiredEntity == []):
            self.requiredEntity = ["NOUN PHRASE"]
        self.getQuestionDescription()

    # Attempts to infer descriptive terms for this question.
    # May or may not be completed.
    def getQuestionDescription(self):
        pass

# Returns an array of Question instances
# The input 'dataSet' must be a string can take two values: "dev", "test"
def loadQuestions(dataSet):
    questions = []
    with open("qadata/" + dataSet + "/questions.txt") as questionsFile:
        counter = 0
        for line in questionsFile:
            if (counter % 3 == 1):
                questions.append(Question(int(counter / 3), line.rstrip()))
            counter += 1
        return questions
