library(foreach)
library(doMC)

############# FUNCTIONS #############
FakeQuestionFeatures = function(){
    
    # Reading the Questions database
    questions = read.csv("../TutorQeA/data/Questions.csv")
    questionIds = questions$Id
    rm(questions)
    
    # Creating the collumn names
    print(noquote("Creating the collumn names..."))
    defaultFeatures = c("Score", "ViewCount", "AnswerCount", "CommentCount", "FavoriteCount")
    derivedFeatures = c("HasAcceptedAnswer", "QuestionAgeInDays", "DaysRetired", "BodyWordsNumber", 
                        "HyperlinksNumber", "BlockquoteLinesNumber", "CodeLinesNumber", "ImagesNumber", 
                        "TitleSize", "TagsNumber", "IsCommunityOwned", "CommunityOverCreationDaysRatio", 
                        "WeigthedScore", "FirstAnswerTimeDelay", "AcceptedAnswerTimeDelay", "LastAnswerTimeDelay", 
                        "FirstAnswerScore", "AcceptedAnswerScore", "LastAnswerScore", "MaxScore", 
                        "AcceptedAnswerBodyWordsNumber", "MaxBodyWordsNumber", "AnswersWithCode", "AnswersWithImages",
                        "AnswersWithLinks", "AcceptedAnswerCommentsNumber", "CommentsNumberMedian", "AnswersWithComments", 
                        "CommunityAndUserAnswersRatio", "QuestionCommentsScoreAverage", "MaxQuestionCommentsScore",
                        "QuestionComnentWordsNumber", "FirstCommentAge", "LastCommentAge", "AskerQuestionComments", 
                        "HasCommentAfterAnswer", "TotalAnswerComments", "AnswersWithComments", "MaxCommentsScore", 
                        "AskerAnswerComments")
    clusteringApproaches = c("Cluster")
    
    colNum = sum(1, length(defaultFeatures), length(derivedFeatures), length(clusteringApproaches))
    
    ## Generating random Features for all questions
    print(noquote("Generating random feature values for all questions..."))
    newFeatures = foreach(i = questionIds, .combine = rbind) %dopar%
{
    c(i, round(runif(colNum - 2, min=0, max = 100), digits = 1), sample(1:8, size = 1))
}
    
    QuestionFeatures = as.data.frame(newFeatures)
    colnames(QuestionFeatures) = c("Id", defaultFeatures, derivedFeatures, clusteringApproaches)
    
    # Persisting the data
    print(noquote("Persisting the QuestionFeatures data..."))
    write.csv(QuestionFeatures, file = "../TutorQeA/data/QuestionFeatures.csv", row.names = F)
}

# Remove useless attributes from the Questions table
QuestionsClean = function(){
    
    questions = read.csv("../TutorQeA/data/Questions.csv")
    
    questions = questions[,c("Id", "AcceptedAnswerId", "CreationDate", "Score", "ViewCount", 
                             "Body", "OwnerUserId", "OwnerDisplayName", "LastActivityDate", 
                             "Title", "Tags", "AnswerCount", "CommentCount", "FavoriteCount", 
                             "CommunityOwnedDate")]
    
    write.csv(questions, file = "../TutorQeA/data/Questions.csv", row.names = F)    
    
}


############# MAIN #############
# Register the Cores of the Machine (runs only in Unix)
registerDoMC()

# FakeQuestionFeatures()
# QuestionsClean()