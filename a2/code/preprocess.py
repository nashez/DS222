import re
import string
import pickle
import nltk
from nltk.corpus import stopwords
from nltk.stem.porter import *
from nltk.tokenize import RegexpTokenizer

stemmer = PorterStemmer()
tokenizer = RegexpTokenizer(r'\w+')
vocabulary = {}
classes = {}
vocabulary_count = 0
classes_count = 0
stopwordsl = set(stopwords.words('english'))
with open("../data/train.txt") as f:
    for line in f:
        doc = line.strip().split("\t")
        classes_found = doc[0].split(",")
        for y in classes_found:
                if y.strip() not in classes:
                        classes[y.strip()] = classes_count
                        classes_count += 1
        #print(doc)
        doc[1] = (doc[1][doc[1].index('> "')+3:doc[1].index('"@en')]).lower()
        #re.sub('[^A-Za-z0-9]+', ' ', doc[1])
        tokens = tokenizer.tokenize(doc[1])
        filtered = [stemmer.stem(w) for w in tokens if not stemmer.stem(w) in stopwordsl]
        for x in filtered:
                #x = stemmer.stem(elem)
                if x not in vocabulary:
                        vocabulary[x] = vocabulary_count
                        vocabulary_count += 1

print("Vocabulary Size = " + str(vocabulary_count))
print("Number of Classes " + str(classes_count))

'''
with open("../data/Vocabulary.txt", mode="wb") as vocab_file:
    pickle.dump(vocabulary,vocab_file)
vocab_file.close()
with open("../data/Classes.txt", mode="wb") as classesfile:
    pickle.dump(classes,classesfile)
classesfile.close()
'''

write_pointer = open("../data/final_test.txt","w")

with open("../data/test.txt") as f:
    for line in f:
        doc = line.strip().split("\t")
        classes_found = doc[0].split(",")
        doc[1] = (doc[1][doc[1].index('> "')+3:doc[1].index('"@en')]).lower()
        #re.sub('[^A-Za-z0-9]+', ' ', doc[1])
        tokens = tokenizer.tokenize(doc[1])
        filtered = [stemmer.stem(w) for w in tokens if not stemmer.stem(w) in stopwordsl]
        classes_found = doc[0].split(",")
        newline = ""
        for y in classes_found:
                if y.strip() in classes:
                        newline += str(classes[y.strip()]) + ","
                else:
                        newline += str(-1) + ","                        
        newline = newline.rstrip(",")
        newline += "\t"
        for x in filtered:
                #x = stemmer.stem(elem)
                if x in vocabulary:
                        newline += str(vocabulary[x]) + ","
                else:
                        newline += str(-1) + ","
        newline = newline.rstrip(",")
        newline = newline + "\n"
        write_pointer.write(newline)
write_pointer.close()

write_pointer = open("../data/final_train.txt","w")
with open("../data/train.txt") as f:
    for line in f:
        doc = line.strip().split("\t")
        classes_found = doc[0].split(",")
        doc[1] = (doc[1][doc[1].index('> "')+3:doc[1].index('"@en')]).lower()
        #re.sub('[^A-Za-z0-9]+', ' ', doc[1])
        tokens = tokenizer.tokenize(doc[1])
        filtered = [stemmer.stem(w) for w in tokens if not stemmer.stem(w) in stopwordsl]
        classes_found = doc[0].split(",")
        newline = ""
        for y in classes_found:
                if y.strip() in classes:
                        newline += str(classes[y.strip()]) + ","
                else:
                        newline += str(-1) + ","
        newline = newline.rstrip(",")
        newline += "\t"
        for x in filtered:
                #x = stemmer.stem(elem)
                if x in vocabulary:
                        newline += str(vocabulary[x]) + ","
                else:
                        newline += str(-1) + ","
        newline = newline.rstrip(",")
        newline = newline + "\n"
        write_pointer.write(newline)
write_pointer.close()

write_pointer = open("../data/final_devel.txt","w")
with open("../data/devel.txt") as f:
    for line in f:
        doc = line.strip().split("\t")
        classes_found = doc[0].split(",")
        doc[1] = (doc[1][doc[1].index('> "')+3:doc[1].index('"@en')]).lower()
        #re.sub('[^A-Za-z0-9]+', ' ', doc[1])
        tokens = tokenizer.tokenize(doc[1])
        filtered = [stemmer.stem(w) for w in tokens if not stemmer.stem(w) in stopwordsl]
        classes_found = doc[0].split(",")
        newline = ""
        for y in classes_found:
                if y.strip() in classes:
                        newline += str(classes[y.strip()]) + ","
                else:
                        newline += str(-1) + ","                        
		newline = newline.rstrip(",")
        newline += "\t"
        for x in filtered:
                #x = stemmer.stem(elem)
                if x in vocabulary:
                        newline += str(vocabulary[x]) + ","
                else:
                        newline += str(-1) + ","
        newline = newline.rstrip(",")
        newline = newline + "\n"
        write_pointer.write(newline)
write_pointer.close()
