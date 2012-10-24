############# FUNCTIONS #############
CreateFakeQuestionFeatures = function(questionsFile, questionFeaturesFile){
    
    # Reading the Questions database
    questions = read.csv(questionsFile)
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
                        "AnswersWithLinks", "AcceptedAnswerCommentsNumber", "CommentsNumberMedian",
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
    c(i, 
      round(rnorm(colNum - 2, mean=200, sd = 150) + i, digits = 1), 
      (round(rpois(1, 100), 0) %% 8)+1)
}
    
    questionFeatures = as.data.frame(newFeatures)
    colnames(questionFeatures) = c("Id", defaultFeatures, derivedFeatures, clusteringApproaches)
    
    # Persisting the data
    print(noquote("Persisting the questionFeatures data..."))
    write.csv(questionFeatures, file = questionFeaturesFile, row.names = F)
}

# Remove useless attributes from the Questions and Answers table
RemoveUnusedCollumns = function(){
    questions = read.csv("../TutorQeA/raw_data/Questions.csv")
    answers = read.csv("../TutorQeA/raw_data/Answers.csv")
    
    print(noquote("Removing unused collumns from Questions and Answers..."))
    questions = questions[,c("Id", "AcceptedAnswerId", "CreationDate", "Score", "ViewCount", 
                             "Body", "OwnerUserId", "OwnerDisplayName", "LastActivityDate", 
                             "Title", "Tags", "AnswerCount", "CommentCount", "FavoriteCount", 
                             "CommunityOwnedDate")]
    answers = answers[,c("Id", "ParentId", "CreationDate", "Score", "Body", "OwnerUserId", 
                         "OwnerDisplayName", "CommentCount", "CommunityOwnedDate")]
    
    write.csv(questions, file = "../TutorQeA/raw_data/Questions.csv", row.names = F)      
    write.csv(answers, file = "../TutorQeA/raw_data/Answers.csv", row.names = F)
}

CheckForeignKeysBetweenTables = function(){
    qIds = read.csv("../TutorQeA/raw_data/Questions.csv")$Id
    
    # Answers.ParentId %in% Questions.Id
    print(noquote("Checking: Answers.ParentId %in% Questions.Id"))
    answers = read.csv("../TutorQeA/raw_data/Answers.csv")
    answers = answers[answers$ParentId %in% qIds,] # 0 rows deleted...
    write.csv(answers, file = "../TutorQeA/raw_data/Answers.csv", row.names = F)
    rm(answers)
    
    # PostTags.PostId %in% Questions.Id
    print(noquote("Checking: PostTags.PostId %in% Questions.Id"))
    postTags = read.csv("../TutorQeA/data/PostTags.csv")
    postTags = postTags[postTags$PostId %in% qIds,] # 19 rows deleted!
    write.csv(postTags, file = "../TutorQeA/data/PostTags.csv", row.names = F)
    rm(postTags)
    
    # Comment-Questions.PostId %in% Questions.Id
    print(noquote("Checking: Comment-Questions.PostId %in% Questions.Id"))
    commentQ = read.csv("../TutorQeA/raw_data/Comments-Questions.csv")
    commentQ = commentQ[commentQ$PostId %in% qIds,] # 0 rows deleted...
    write.csv(commentQ, file = "../TutorQeA/raw_data/Comments-Questions.csv", row.names = F)
    rm(commentQ)
    
    # Comment-Answers.PostId %in% Answers.Id
    print(noquote("Checking: Comment-Answers.PostId %in% Answers.Id"))
    commentA = read.csv("../TutorQeA/raw_data/Comments-Answers.csv")
    aIds = read.csv("../TutorQeA/raw_data/Answers.csv")$Id
    commentA = commentA[commentA$PostId %in% aIds,] # 0 rows deleted...
    write.csv(commentA, file = "../TutorQeA/raw_data/Comments-Answers.csv", row.names = F)
    rm(commentA, aIds)
    
}

CreateTagLinks = function(){
    
    print(noquote("Creating TagLinks Table..."))
    
    postTags = read.csv("../TutorQeA/data/PostTags.csv")
    
    # Aggregate by Tag
    tagQuestionsList = dlply(postTags, .(TagId), function(x) list(Tag = x$TagId[1], 
                                                                  Questions = x$PostId))
    
    # Loop over each Tag and QuestionList 
    tagLinks = foreach(tagQuest = tagQuestionsList, .combine = rbind) %dopar%
{
    # Get the PostTags that are not from this Tag and that contains Questions in common
    relatedPostTags = postTags[postTags$TagId != tagQuest[[1]] & 
        postTags$PostId %in% tagQuest[[2]],]
    if (nrow(relatedPostTags) > 0){
        # Count the Weight of the Tag Links
        linkedTags = count(relatedPostTags, .(TagId))
        # Organize the output data.frame
        linkedTags$TagIdOne = tagQuest[[1]]
        colnames(linkedTags) = c("TagIdTwo", "Weight", "TagIdOne")
        linkedTags = linkedTags[,c("TagIdOne", "TagIdTwo", "Weight")]
        linkedTags
    }else{
        NULL
    }
}
    
    write.csv(tagLinks, file = "../TutorQeA/data/TagLinks.csv", row.names = F)
}

CreateQuestionData = function(){
    print(noquote("Creating the QuestionData table (keeping only the needed collumns from Questions)..."))
    questions = read.csv("../TutorQeA/raw_data/Questions.csv")
    
    print(noquote("Selecting the question collumns..."))
    questions.data = questions[,c("Id", "Title", "Score", "AnswerCount", "CommentCount")]
    questions.data[is.na(questions.data$AnswerCount),]$AnswerCount = 0
    questions.data[is.na(questions.data$CommentCount),]$CommentCount = 0
    
    print(noquote("Adding the Cluster collumn from QuestionFeatures..."))
    questions.features = read.csv("../TutorQeA/raw_data/QuestionFeatures.csv")
    questions.data = merge(questions.data, questions.features[,c("Id", "Cluster")], by = "Id")
    
    write.csv(questions.data, file = "../TutorQeA/data/QuestionData.csv", row.names = F)
}

CreateQuestionAnswers = function(){
    print(noquote("Creating the QuestionAnswers table (ordered by: QuestionId and Answer$CreationDate)..."))
    answers = read.csv("../TutorQeA/raw_data/Answers.csv")
    accepted.answers = read.csv("../TutorQeA/raw_data/Questions.csv")$AcceptedAnswerId
    comments.post.id = read.csv("../TutorQeA/raw_data/Comments-Answers.csv")$PostId
    
    question.answers = answers[,c("ParentId", "Id", "Score", "CreationDate", "CommentCount")]
    question.answers[is.na(question.answers$CommentCount),]$CommentCount = 0
    question.answers$isAcceptedAnswer = question.answers$Id %in% accepted.answers
    colnames(question.answers) = c("QuestionId", "AnswerId", "Score", "CreationDate","AnswerCommentCount","IsAcceptedAnswer")
    
    question.answers = question.answers[order(question.answers$QuestionId, 
                                              strptime(question.answers$CreationDate,"%Y-%m-%d %H:%M:%S"), decreasing=F),]
    
    write.csv(question.answers, file = "../TutorQeA/data/QuestionAnswers.csv", row.names = F)
}

count.comments = function(id, postsIds){
    return(length(which(postsIds == id)))
}

CreateTagNames = function(){
    tags = read.csv("../TutorQeA/raw_data/Tags.csv")
    
    tagNames = tags[,c(1,2)]
    
    write.csv(tagNames, file = "../TutorQeA/data/TagsDictionary.csv",  row.names = F)
}

############# MAIN #############
library(foreach)
library(plyr)

# Register the Cores of the Machine (runs only in Linux)
if (Sys.info()["sysname"] == "Linux"){
    library(doMC)
    registerDoMC()
}

RemoveUnusedCollumns()
CheckForeignKeysBetweenTables()
CreateTagLinks()
CreateFakeQuestionFeatures("../TutorQeA/raw_data/Questions.csv", "../TutorQeA/raw_data/QuestionFeatures.csv")
CreateQuestionData()
CreateQuestionAnswers()
CreateTagNames()