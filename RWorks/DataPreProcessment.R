#################################################
## Data Pre-Processment - before Data Analysis ##
#################################################

CopyAllTables = function(inputDir, outputDir){
    print(noquote("Copying: From inputDir to outputDir..."))
    questions = read.csv(paste(inputDir,"/Questions.csv", sep = ""))
    write.csv(questions, file = paste(outputDir,"/Questions.csv", sep = ""), row.names = F)
    rm(questions)
    
    answers = read.csv(paste(inputDir,"/Answers.csv", sep = ""))
    write.csv(answers, file = paste(outputDir,"/Answers.csv", sep = ""), row.names = F)
    rm(answers)
    
    tags = read.csv(paste(inputDir,"/Tags.csv", sep = ""))
    postTags = read.csv(paste(inputDir,"/PostTags.csv", sep = ""))
    questionComments = read.csv(paste(inputDir,"/Comments-Questions.csv", sep = ""))
    answerComments = read.csv(paste(inputDir,"/Comments-Answers.csv", sep = ""))
    
    write.csv(tags, file = paste(outputDir,"/Tags.csv", sep = ""), row.names = F)      
    write.csv(postTags, file = paste(outputDir,"/PostTags.csv", sep = ""), row.names = F)
    write.csv(questionComments, file = paste(outputDir,"/Comments-Questions.csv", sep = ""), row.names = F)
    write.csv(answerComments, file = paste(outputDir,"/Comments-Answers.csv", sep = ""), row.names = F)
    
    print(noquote("Copying: DONE!"))
    print(noquote(""))
}

RemoveClosedQuestions = function(dir){
    before = 0
    after = 0
    printRowsCountDiff = function(before, after, tableName){
        diff = abs(before - after)
        print(noquote(paste(">> ", tableName, ": ", diff, " removed row(s)", sep = "")))
    }
    
    print(noquote("Removing: Questions.Closed != \"\""))
    questions = read.csv(paste(dir, "/Questions.csv", sep = ""))
    closedIds = questions[questions$ClosedDate != "",]$Id
    questions = questions[!questions$Id %in% closedIds,]
    write.csv(questions, file = paste(dir, "/Questions.csv", sep = ""), row.names = F)
    printRowsCountDiff(length(closedIds), 0, "Questions")
    rm(questions)
    
    # Answers.ParentId %in% Questions.Id
    print(noquote("Removing: Answers.ParentId %in% Questions.Closed"))
    answers = read.csv(paste(dir, "/Answers.csv", sep = ""))
    before = nrow(answers)
    answers = answers[!answers$ParentId %in% closedIds,]
    write.csv(answers, file = paste(dir, "/Answers.csv", sep = ""), row.names = F)
    after = nrow(answers)
    printRowsCountDiff(before, after, "Answers")
    rm(answers)
    
    # PostTags.PostId %in% Questions.Id
    print(noquote("Removing: PostTags.PostId %in% Questions.Closed"))
    postTags = read.csv(paste(dir,"/PostTags.csv", sep = ""))
    before = nrow(postTags)
    postTags = postTags[!postTags$PostId %in% closedIds,]
    write.csv(postTags, file = paste(dir,"/PostTags.csv", sep = ""), row.names = F)
    after = nrow(postTags)
    printRowsCountDiff(before, after, "PostTags")
    rm(postTags)
    
    # Comment-Questions.PostId %in% Questions.Id
    print(noquote("Removing: Comment-Questions.PostId %in% Questions.Closed"))
    commentQ = read.csv(paste(dir,"/Comments-Questions.csv", sep = ""))
    before = nrow(commentQ)
    commentQ = commentQ[!commentQ$PostId %in% closedIds,]
    write.csv(commentQ, file = paste(dir,"/Comments-Questions.csv", sep = ""), row.names = F)
    after = nrow(commentQ)
    printRowsCountDiff(before, after, "Questions-Comments")
    rm(commentQ)
    
    # Comment-Answers.PostId %in% Answers.Id
    print(noquote("Removing: Comment-Answers.PostId %in% Answers.Id"))
    commentA = read.csv(paste(dir,"/Comments-Answers.csv", sep = ""))
    aIds = read.csv(paste(dir,"/Answers.csv", sep = ""))$Id
    before = nrow(commentA)
    commentA = commentA[commentA$PostId %in% aIds,] 
    write.csv(commentA, file = paste(dir,"/Comments-Answers.csv", sep = ""), row.names = F)
    after = nrow(commentA)
    printRowsCountDiff(before, after, "Answers-Comments")
    rm(commentA, aIds)
    
    print(noquote("Removing: DONE!"))
    print(noquote(""))
}

RemoveUnusedAttributes = function(dir){
    print(noquote("Removing collumns: Questions..."))
    questions = read.csv(paste(dir,"/Questions.csv", sep = ""))
    questions = questions[,c("Id", "AcceptedAnswerId", "CreationDate", "Score", "ViewCount", 
                             "Body", "OwnerUserId", "LastActivityDate", 
                             "Title", "Tags", "AnswerCount", "CommentCount", "FavoriteCount", 
                             "CommunityOwnedDate")] 
    write.csv(questions, file = paste(dir,"/Questions.csv", sep = ""), row.names = F)
    rm(questions)
    
    print(noquote("Removing collumns: Answers..."))
    answers = read.csv(paste(dir,"/Answers.csv", sep = ""))
    answers = answers[,c("Id", "ParentId", "CreationDate", "Score", "Body", "OwnerUserId",
                         "CommentCount", "CommunityOwnedDate")] 
    write.csv(answers, file = paste(dir,"/Answers.csv", sep = ""), row.names = F)
    rm(answers)
    
    print(noquote("Removing collumns: Tags..."))
    tags = read.csv(paste(dir, "/Tags.csv", sep = ""))
    tags = tags[,c("Id", "TagName", "Count")]
    write.csv(tags, paste(dir, "/Tags.csv", sep = ""), row.names = F)
    rm(tags)
    
    print(noquote("Removing collumns: Comments-Questions..."))
    question.comments = read.csv(paste(dir, "/Comments-Questions.csv", sep = ""))
    question.comments = question.comments[,c("Id", "PostId", "Score", "Text", "CreationDate", "UserId")]
    write.csv(question.comments, paste(dir, "/Comments-Questions.csv", sep = ""), row.names = F)
    rm(question.comments)
    
    print(noquote("Removing collumns: Comments-Answers..."))
    question.answers = read.csv(paste(dir, "/Comments-Answers.csv", sep = ""))
    question.answers = question.answers[,c("Id", "PostId", "Score", "Text", "CreationDate", "UserId")]
    write.csv(question.answers, paste(dir, "/Comments-Answers.csv", sep = ""), row.names = F)
    rm(question.answers)
    
    print(noquote("Removing collumns: DONE!"))
    print(noquote(""))
}

CheckForeignKeysBetweenTables = function(dir){
    before = 0
    after = 0
    printRowsCountDiff = function(before, after, tableName){
        diff = abs(before - after)
        print(noquote(paste(">> ", tableName, ": ", diff, " removed row(s)", sep = "")))
    }
    
    answers = read.csv(paste(dir, "/Answers.csv", sep = ""))
    
    # Questions.AcceptedAnswerId %in% Answers.Id (only the questions with an accepted answer id)
    print(noquote("Checking: Questions.AcceptedAnswerId %in% Answers.Id"))
    questions = read.csv(paste(dir, "/Questions.csv", sep = ""))
    before = nrow(questions)
    quest.without.acc = questions[is.na(questions$AcceptedAnswerId) | questions$AcceptedAnswerId == -1,]
    quest.with.acc = questions[!is.na(questions$AcceptedAnswerId) & questions$AcceptedAnswerId != -1,]
    quest.with.acc = quest.with.acc[quest.with.acc$AcceptedAnswerId %in% answers$Id,]
    questions = rbind(quest.without.acc, quest.with.acc)
    questions = questions[order(questions$Id, decreasing=F),]
    write.csv(questions, file = paste(dir, "/Questions.csv", sep = ""), row.names = F)
    after = nrow(questions)
    printRowsCountDiff(before, after, "Questions")
    qIds = questions$Id
    rm(questions, quest.with.acc, quest.without.acc)    
    
    # Answers.ParentId %in% Questions.Id
    print(noquote("Checking: Answers.ParentId %in% Questions.Id"))
    before = nrow(answers)
    answers = answers[answers$ParentId %in% qIds,]
    write.csv(answers, file = paste(dir, "/Answers.csv", sep = ""), row.names = F)
    after = nrow(answers)
    printRowsCountDiff(before, after, "Answers")
    rm(answers)
    
    # PostTags.PostId %in% Questions.Id
    print(noquote("Checking: PostTags.PostId %in% Questions.Id AND PostTags.TagId %in% Tags.Id"))
    postTags = read.csv(paste(dir,"/PostTags.csv", sep = ""))
    tags = read.csv(paste(dir,"/Tags.csv", sep = ""))
    before = nrow(postTags)
    postTags = postTags[postTags$PostId %in% qIds,]
    postTags = postTags[postTags$TagId %in% tags$Id,]
    write.csv(postTags, file = paste(dir,"/PostTags.csv", sep = ""), row.names = F)
    after = nrow(postTags)
    printRowsCountDiff(before, after, "PostTags")
    rm(postTags)
    
    # Comment-Questions.PostId %in% Questions.Id
    print(noquote("Checking: Comment-Questions.PostId %in% Questions.Id"))
    commentQ = read.csv(paste(dir,"/Comments-Questions.csv", sep = ""))
    before = nrow(commentQ)
    commentQ = commentQ[commentQ$PostId %in% qIds,]
    write.csv(commentQ, file = paste(dir,"/Comments-Questions.csv", sep = ""), row.names = F)
    after = nrow(commentQ)
    printRowsCountDiff(before, after, "Questions-Comments")
    rm(commentQ)
    
    # Comment-Answers.PostId %in% Answers.Id
    print(noquote("Checking: Comment-Answers.PostId %in% Answers.Id"))
    commentA = read.csv(paste(dir,"/Comments-Answers.csv", sep = ""))
    aIds = read.csv(paste(dir,"/Answers.csv", sep = ""))$Id
    before = nrow(commentA)
    commentA = commentA[commentA$PostId %in% aIds,] 
    write.csv(commentA, file = paste(dir,"/Comments-Answers.csv", sep = ""), row.names = F)
    after = nrow(commentA)
    printRowsCountDiff(before, after, "Answers-Comments")
    rm(commentA, aIds)
    
    print(noquote("Checking: DONE!"))
    print(noquote(""))
}

ReplaceNAValues = function(dir){
    print(noquote("Replacing: Questions NA's..."))
    questions = read.csv(paste(dir, "/Questions.csv", sep = ""))
    # Set -1 in AcceptedAnswerId with NA values (that means, there is no accepted answer)
    questions[is.na(questions$AcceptedAnswerId),"AcceptedAnswerId"] = -1
    # Set -1 in OwnerUserId with NA values (that means, there is no user... strange)
    questions[is.na(questions$OwnerUserId),"OwnerUserId"] = -1
    # Set 0 in AnswerCount, CommentCount or FavoriteCount (that means, there is no answer, 
    # comment or favorite vote)
    questions[is.na(questions$AnswerCount),"AnswerCount"] = 0
    questions[is.na(questions$CommentCount),"CommentCount"] = 0
    questions[is.na(questions$FavoriteCount),"FavoriteCount"] = 0
    write.csv(questions, file = paste(dir, "/Questions.csv", sep = ""), row.names = F)
    rm(questions)
    
    print(noquote("Replacing: Answer NA's..."))
    answers = read.csv(paste(dir, "/Answers.csv", sep = ""))
    # Set -1 in OwnerUserId with NA values (that means, there is no user... strange)
    answers[is.na(answers$OwnerUserId),"OwnerUserId"] = -1
    # Set 0 in CommentCount (that means, there is no comment)
    answers[is.na(answers$CommentCount),"CommentCount"] = 0
    write.csv(answers, file = paste(dir, "/Answers.csv", sep = ""), row.names = F)
    rm(answers)
    
    print(noquote("Replacing: Comments-Questions NA's..."))
    commentsQ = read.csv(paste(dir, "/Comments-Questions.csv", sep = ""))
    # Set -1 in UserId with NA values (that means, there is no user... strange)
    commentsQ[is.na(commentsQ$UserId),"UserId"] = -1
    # Set 0 in Score (that means, there is no vote, as the majority)
    commentsQ[is.na(commentsQ$Score),"Score"] = 0
    write.csv(commentsQ, file = paste(dir, "/Comments-Questions.csv", sep = ""), row.names = F)
    rm(commentsQ)
    
    print(noquote("Replacing: Comments-Answers NA's..."))
    commentsA = read.csv(paste(dir, "/Comments-Answers.csv", sep = ""))
    # Set -1 in UserId with NA values (that means, there is no user... strange)
    commentsA[is.na(commentsA$UserId),"UserId"] = -1
    # Set 0 in Score (that means, there is no vote, as the majority)
    commentsA[is.na(commentsA$Score),"Score"] = 0
    write.csv(commentsA, file = paste(dir, "/Comments-Answers.csv", sep = ""), row.names = F)
    rm(commentsA)
    
    print(noquote("Replacing: DONE!"))
    print(noquote(""))
}

###############################################
## Data PreProcessment - after Data Analysis ##
###############################################
RemoveNoiseAndOutliers = function(dir){
    print(noquote("Removing Noise And Outliers..."))
    
    before = 0
    after = 0
    printRowsCountDiff = function(before, after, tableName){
        diff = abs(before - after)
        print(noquote(paste(">> ", tableName, ": ", diff, " removed row(s)", sep = "")))
    }
    
    print(noquote("Removing Noise and Outliers: Questions with CommunityOwnedWiki not empty..."))
    
    questions = read.csv(paste(dir, "Questions.csv", sep = ""), header = T)
    before = nrow(questions)
    questions = questions[questions$CommunityOwnedDate == "", ]
    write.csv(questions, file = paste(dir, "/Questions.csv", sep = ""), row.names = F)
    after = nrow(questions)
    printRowsCountDiff(before, after, "Questions")
    
    print(noquote("Removing Noise and Outliers: DONE!"))
    print(noquote(""))
    
    # The Foreign keys should be checked again!
    CheckForeignKeysBetweenTables(dir)
}

################ MAIN ################
# Machine dependent file paths
MainPreProcessment = function(raw.dir = "../AllData/raw/", 
                              preProcessed.dir = "../AllData/preprocessed/"){
    
    # Data Collection Function Calls
    print(noquote(""))
    print(noquote(">>>> Data Collection <<<<"))
    
    print(noquote("Removing old pre-processed directory..."))
    unlink(preProcessed.dir, recursive=T)
    dir.create(preProcessed.dir, showWarnings=F)
    
    CopyAllTables(inputDir=raw.dir, outputDir=preProcessed.dir)
    RemoveClosedQuestions(preProcessed.dir)
    RemoveUnusedAttributes(preProcessed.dir)
    
    # Data Treatment 
    print(noquote(">>>> Data Treatment <<<<"))
    
    CheckForeignKeysBetweenTables(preProcessed.dir)
    ReplaceNAValues(preProcessed.dir)
}