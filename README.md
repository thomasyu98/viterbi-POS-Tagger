# viterbi-POS-Tagger
Part-of-speech tagger for texts, trained according to WSJ
To run:
1. from command line, root directory in "viterbi". You should see the readme text file, src and "WSJ_POS_CORPUS_FOR_STUDENTS". 
2. compile all .java files within viterbi package "javac src/viterbi/*.java"
3. run "java -cp src viterbi.WSJPOSTagger WSJ_POS_CORPUS_FOR_STUDENTS/WSJ_02-21.pos FILE_TO_CONVERT 10 10" where the variable FILE_TO_CONVERT is the name of the file you want to convert to .pos file
4. You will find the .pos file within the root directory

OOV Handling:
I handled the OOV varaibles by using a suffix tree that I leanred about through the "TnT - A Statistical Part-of-Speech Tagger". The link to the text is: http://www.coli.uni-saarland.de/~thorsten/publications/Brants-ANLP00.pdf  
How it works is that I try to guess the tag for the word by analyzing the suffix of the word itself. By creating two suffix trees, one suffix tree will keep track of the lower cased words and the other suffix tree will look at upper cased words. Capitalized words will more likely be things like acronyms and hence has less variability. I use 10 for the maximum siffix length and 10 for the maximu word frequency since those are average numbers that I think normal words will not increase by much by. And through testing, 10 10 gave me pretty good accuracy scores. 

I did not go for a trigram implementation because it seems like it would be way too complicated and I couldn't fully wrap my head around it... :D 

