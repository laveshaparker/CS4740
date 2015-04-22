import nltk

class Descriptor:
    def __init__(self, e, r):
        self.entityType = e
        self.relevantTokens = r

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
        "how many" : "NUMBER",
        "how much" : "NUMBER",
        "what" : "NOUN"
    }

    def __init__(self, n, s):
        self.number = n
        self.questionCaseSensitive = s
        self.questionCaseInsensitive = s.lower()
        self.tokensCaseSensitive = nltk.word_tokenize(self.questionCaseSensitive)
        self.tokensCaseInsensitive = nltk.word_tokenize(self.questionCaseInsensitive)
        self.tokensWithPOS = nltk.pos_tag(self.tokensCaseSensitive)
        self.nerTree = nltk.ne_chunk(self.tokensWithPOS)
        self.contentWordsCaseSensitive = [word for word in self.tokensCaseInsensitive if word not in self.STOPWORDS]
        self.contentWordsCaseInsensitive = [word.lower() for word in self.contentWordsCaseSensitive]
        
        requiredEntity = [self.REQUIREDENTITIES[key] for key in self.REQUIREDENTITIES if key in self.questionCaseInsensitive]
        if (requiredEntity == []):
            requiredEntity = ["OTHER"]

        if ("TIME" in requiredEntity):
            self.getTimeDescriptor()
        if ("PERSON" in requiredEntity):
            self.getPersonDescriptor()
        if ("PLACE" in requiredEntity):
            self.getPlaceDescriptor()
        if ("NUMBER" in requiredEntity):
            self.getNumberDescriptor()
        if ("NOUN" in requiredEntity):
            self.getPlaceDescriptor()

    # Attempts to infer descriptive terms for the time/date required by this question
    def getTimeDescriptor(self):
        i = 1
        relevantTokens = []
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("V")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("V"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1
        self.descriptor = Descriptor("TIME", relevantTokens) 


    # Attempts to infer descriptive terms for the person required by this question
    def getPersonDescriptor(self):
        i = 1
        relevantTokens = []
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("V")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("V"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1
        self.descriptor = Descriptor("PERSON", relevantTokens) 

    # Attempts to infer descriptive terms for the person required by this question
    def getPlaceDescriptor(self):
        i = 1
        relevantTokens = []
        # noun block
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1

        # verb block
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("V")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("V"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1
        self.descriptor = Descriptor("PLACE", relevantTokens) 

    # Attempts to infer descriptive terms for the person required by this question
    def getNumberDescriptor(self):
        i = 2
        relevantTokens = []
        # noun
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1

        # noun
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("N")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("N"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1

        self.descriptor = Descriptor("NUMBER", relevantTokens) 

    # Attempts to infer descriptive terms for the person required by this question
    def getNounDescriptor(self):
        i = 1
        relevantTokens = []
        # noun
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1

        # noun
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("J") or self.tokensWithPOS[i][1].startswith("N"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1

        # verb
        while (i < len(self.tokensWithPOS)):
            if (self.tokensWithPOS[i][1].startswith("V")):
                break;
            i += 1
        while (i < len(self.tokensWithPOS)):
            if (not (self.tokensWithPOS[i][1].startswith("V"))):
                break;
            relevantTokens.append(self.tokensWithPOS[i][0])
            i += 1

        self.descriptor = Descriptor("NOUN", relevantTokens) 

# Returns an array of Question instances
# The input 'dataSet' must be a string can take two values: "dev", "test"
def loadQuestions(dataSet):
    questions = {}
    with open("qadata/" + dataSet + "/questions.txt") as questionsFile:
        counter = 0
        questionNumber = 0
        for line in questionsFile:
            if (counter % 3 == 0):
                questionNumber = [int(s) for s in line.split() if s.isdigit()][0]
            elif (counter % 3 == 1):
                questions[questionNumber] = Question(questionNumber, line.rstrip())
            counter += 1
        return questions
