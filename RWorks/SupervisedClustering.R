countWords = function(body){
    return(length(strsplit(body, " ")[[1]]))
}

content.function = function(qId){

    # Content: Selecting the Answers and Ordering by CreationDate...
    ans = answers[answers$ParentId == qId, ]
    ans = ans[order(ans$CreationDate),]
    
    # Content: Selecting the Question Comments...
    qComm = qComments[qComments$PostId == qId,]
    
    # Content: Getting the Accepted Answer Id
    accAnsId = questions[questions$Id == qId,]$AcceptedAnswerId
    
    content = NULL
    
    # Content: Question with positive score comments 
    #          (Poor value... There are bad comments with positive scores)
    QuestionContent = data.frame(Id = qId, Content = sum(qComm$Score))
    AnswerContent = NULL
    
    # Content: Iterating over All Answers and calculating theirs Contents!
    ansAfterAccepted = 0
    if (nrow(ans) > 0){
        for(i in 1:nrow(ans)){
            a = ans[i,]
            # Content: Select the Answer Comments
            aComm = aComments[aComments$PostId == a$Id,]
            
            # Content: Persist the data to posterior analysis
            ansContent = data.frame(Id = a$Id, CreationDate = a$CreationDate,
                                    Score = a$Score, SumCommentScore = sum(aComm$Score), 
                                    BodyWordCount = countWords(as.character(a$Body)), 
                                    AcceptedAnswer = F, AfterAccepted = ansAfterAccepted, 
                                    Content = 0)
            
            # Content: Measure the distance of the Score from First and Third quartiles
            diffScoreQuantil = max(max(a$Score - quantile(ans$Score, .25), a$Score - quantile(ans$Score, .75)), 0)
            
            # Content: Answer Score is high predictive, Sum(AnswerComment$Score)
            ansValue = 2 * a$Score + sum(aComm$Score) 
            # VALUE: BodySize (depends on the score of the answer)
            ansValue = ansValue + sqrt(countWords(as.character(a$Body))) * diffScoreQuantil
            
            if (ansAfterAccepted > 0){
                # TODO: This should be changed to check if the answer ocurred after the VoteEvent, AcceptedByOriginator
                # VALUE: Answers after the AcceptedEvent
                ansValue = ansValue + (ansValue * ansAfterAccepted/2)
                ansAfterAccepted = ansAfterAccepted + 1
            }else{
                if (accAnsId == a$Id){
                    # VALUE: Accepted Answer gain a minimum value
                    ansValue = ansValue + 10
                    ansContent$AcceptedAnswer = T
                    ansAfterAccepted = ansAfterAccepted + 1
                }
            }
            ansContent$Content = ansValue
            AnswerContent = rbind(AnswerContent, ansContent)
        }
    }
    return(list(Question = as.list(QuestionContent), Answers = as.list(AnswerContent)))
}

selfAnswer.function = function(qId, questions){
    
    question = questions[questions$Id == qId,]
    userId = question$OwnerUserId
    if (userId == -1){
        return(data.frame(QuestionValue = 0, AnswersValue = 0))
    }
       
    # Selecting the Answers and Ordering by CreationDate...
    ans = answers[answers$ParentId == qId, ]
    
    # CHECK if there is any answer from the questioner
    hasAnswersFromQuestioner = (ans$OwnerUserId == userId) * (1:nrow(ans))
    answersFromQuestionerIndex = hasAnswersFromQuestioner[hasAnswersFromQuestioner != 0]
    answersFromQuestioner = ans[answersFromQuestionerIndex,]

    if (nrow(answersFromQuestioner) <= 0){
        return(return(data.frame(QuestionValue = 0, AnswersValue = 0)))
    }

    theAnswers = answersFromQuestioner[order(answersFromQuestioner$CreationDate),]
    
    # Getting the Accepted Answer Id
    accAnsId = questions[questions$Id == qId,]$AcceptedAnswerId
    
    # Considers the Questions: SCORE, FAVORITEs (higher weight)
    qValue = question$Score^2 + (2 * question$FavoriteCount^2)
    # Considers the Answers: SCORE, ACCEPTED flag, CREATION_DATE
    aValue = (theAnswers$Score * (theAnswers$Score * sum(theAnswers$Id == accAnsId))) /answersFromQuestionerIndex
 
    return(data.frame(QuestionValue = qValue, AnswersValue = aValue))
}

commentDialogueValue = function(comments, questionerId, value, commentType, 
                                questionersEvent, othersEvent){
    
    if (nrow(comments) <= 0){
        return (NA)
    }
    
    ownerIds = comments[comments$UserId != -1,]$UserId
    if (length(ownerIds) <= 0){
        return (NA)
    }

    commValue = 1
    lastUserWasQuestioner = T
    
    for(i in 1:length(ownerIds)){
        ownerId = ownerIds[i]
        newValue = if (lastUserWasQuestioner){
            if (ownerId == questionerId){
                commValue + i
            }else{
                commValue + (2 * i) 
            }
        }else{
            if (ownerId == questionerId){
                commValue + (2 * i)
            }else{
                commValue + i
            }
        }

        if(ownerId == questionerId){
            lastUserWasQuestioner = T
            value = rbind(value, data.frame(Event = questionersEvent, User = "Questioner", 
                                            Post = commentType, PostId = comments$Id[i],
                                            DialoguePoints = (newValue - commValue)))
        }else{
            lastUserWasQuestioner = F
            value = rbind(value, data.frame(Event = othersEvent, User = "Other", 
                                            Post = commentType, PostId = comments$Id[i],
                                            DialoguePoints = (newValue - commValue)))
        }
        commValue = newValue
    }
    return(value)
}

dialogue.function = function(qId, questions, comments, answers){
    question = questions[questions$Id == qId,]
    userId = question$OwnerUserId
    if (userId == -1){
        return(data.frame(Event = "USER_UNIDENTIFIED", User = "Questioner", 
                          Post = "Question", PostId = qId, DialoguePoints = 0))
    }

    # Value from the Question
    value = data.frame(Event = "QUESTION_CREATED", User = "Questioner", 
                       Post = "Question", PostId = qId, DialoguePoints = 1)

    # Selecting the Question Comments...
    qComments = comments[comments$PostId == qId,]
    
    # Value from the Question Comments...
    qCommentValue = commentDialogueValue(comments=qComments, questionerId=userId, value, commentType="Question_Comment", 
                                         questionersEvent="QUESTIONERS_Q_COMMENT", othersEvent="OTHERS_Q_COMMENT")    
    if(class(qCommentValue) == "data.frame") value = qCommentValue
    
    # Selecting the Answers and Ordering by CreationDate...
    ans = answers[answers$ParentId == qId, ]
    ans = ans[order(ans$CreationDate),]

    # Getting the Accepted Answer Id
    acceptedAnswerId = questions[questions$Id == qId,]$AcceptedAnswerId
    
    # Value from the Answers
    if (nrow(ans) > 0){
        for(i in 1:nrow(ans)){
            a = ans[i,]
            
            # Value from the Answer Creation (not such a big dialogue)
            value = rbind(value, data.frame(Event = "ANSWER_CREATED", User = "Answerer", 
                                            Post = "Answer", PostId = a$Id, DialoguePoints = 1))
            
            if (a$Id == acceptedAnswerId){
                # Value from the Answer Acceptance (considers that a big dialogue, 
                # the Questioner accepted the arguments, the value is as high as the Score)
                value = rbind(value, data.frame(Event = "ANSWER_ACCEPTED", User = "Questioner", 
                                                Post = "Answer", PostId = a$Id, 
                                                DialoguePoints = a$Score))    
            }
            
            # Selecting the Answer Comment Users...
            aComments = qComments[qComments$PostId == qId,]
            
            # Value from the Question Comments...
            answerValue = commentDialogueValue(aComments, userId, value, commentType="Answer_Comment", 
                                               questionersEvent="QUESTIONERS_A_COMMENT", othersEvent="OTHERS_A_COMMENT")    
            
            if(class(answerValue) == "data.frame") value = answerValue
        }
    }
    return(value)    
}

MainSupervisedClustering = function(){
    library(foreach)
    
    # Register the Cores of the Machine (runs only in Linux)
    if (Sys.info()["sysname"] == "Linux"){
        library(doMC)
        registerDoMC()
    }
    
    preprocessedDir = "AllData/preprocessed/"
    dir.create("output/", showWarnings=F)
    
    print(noquote("Reading Data..."))
    questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
    answers = read.csv(paste(preprocessedDir, "Answers.csv", sep = ""), header = T)
    qComments = read.csv(paste(preprocessedDir, "Comments-Questions.csv", sep = ""), header = T)
    aComments = read.csv(paste(preprocessedDir, "Comments-Answers.csv", sep = ""), header = T)
    
    print(noquote("Calculating: New Features...")) 
    QuestionFeatures = foreach(id = questions$Id, .combine = rbind) %dopar%{
        dialogue = dialogue.function(id,questions,qComments, answers)
        data.frame(Id = id, 
                   Dialogue = sum(dialogue$DialoguePoints))
    }
    
    print(noquote("Persisting: QuestionFeatures..."))
    write.csv(QuestionFeatures, file = "output/QuestionFeatures.csv", row.names = F)   
}