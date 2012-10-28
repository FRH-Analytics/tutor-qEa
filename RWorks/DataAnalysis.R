########## FUNCTIONS ##########

plotting = function(commentCount, answerCount){
#     hist(commentCount)
#     hist(answerCount)
    
#     boxplot(commentCount)
#     boxplot(answerCount)
    
    plot(prop.table(table(commentCount)))
    plot(prop.table(table(answerCount)))
    
#     plot(ecdf(commentCount))
#     plot(ecdf(answerCount))
}

########## MAIN ########## 
dir = "../AllData/preprocessed/"

questions = read.csv(paste(dir, "Questions.csv", sep = ""), header = T)
questions.community = questions[questions$CommunityOwnedDate != "",]
questions.not.community = questions[questions$CommunityOwnedDate == "",]

## ABOUT Questions
commentCount = questions$CommentCount
answerCount = questions$AnswerCount

plotting(commentCount, answerCount)

## ABOUT Questions.Community
commentCount.commu = questions.community$CommentCount
answerCount.commu = questions.community$AnswerCount
# 
plotting(commentCount.commu, answerCount.commu)
# 
# ## ABOUT Questions.Not.Community
commentCount.not.commu = questions.not.community$CommentCount
answerCount.not.commu = questions.not.community$AnswerCount

plotting(commentCount.not.commu, answerCount.not.commu)


# Data Analysis
RemoveSomeAttributes = function(dir){
    print(noquote("Removing Att: ..."))
    print(noquote("Removing Att: DONE!"))
    print(noquote(""))
    
    # The Foreign keys should be checked again!
    # source("DataPreProcessment.R")
    # CheckForeignKeysBetweenTables(dir)
}
