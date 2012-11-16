Plot.PMF = function(df, var){
    plot(prop.table(table(df[,var])), ylab = "Probabilidade", main = var)
}

Plot.Boxplot = function(df, var, groupVar = "", horizontal = F){
    if (groupVar != ""){
        boxplot(formula = as.formula(paste(var, "~", groupVar)), data = df, outline=T, 
                horizontal=horizontal, main = var)
    }else{
        boxplot(df[,var], outline=T, horizontal=horizontal,
                main = var)
    }
}

PMFAnalysis.Questions = function(inputDir, outputDir, dataName = "Questions"){
    print(noquote("PMF Analysis - Questions..."))
    questions = read.csv(paste(inputDir, "Questions.csv", sep = ""), header = T)
    
    png(paste(outputDir, "PMF-", dataName, ".png", sep =""), width = 800, height = 1000)    
    par(mfrow = c(3,2))
    Plot.PMF(questions, var = "Score")
    Plot.PMF(questions, var = "ViewCount")
    Plot.PMF(questions, var = "AnswerCount")
    Plot.PMF(questions, var = "CommentCount")
    Plot.PMF(questions, var = "FavoriteCount")
    dev.off()
    
    # PLot boxplots (grouped)
    png(paste(outputDir, "Boxplot-", dataName, ".png", sep = ""), width = 600, height = 800)
    par(mfrow = c(5, 1))
    Plot.Boxplot(questions, var = "Score", horizontal = T)
    Plot.Boxplot(questions, var = "ViewCount", horizontal = T)
    Plot.Boxplot(questions, var = "AnswerCount", horizontal = T)
    Plot.Boxplot(questions, var = "CommentCount", horizontal = T)
    Plot.Boxplot(questions, var = "FavoriteCount", horizontal = T)
    dev.off()
    
    print(noquote("PMF Analysis - Questions: DONE!"))
    print(noquote(""))
}

OutlierAnalysis.Questions = function(inputDir, outputDir, dataName){
    
    print(noquote("Outlier Analysis - Questions..."))
    questions = read.csv(paste(inputDir, "Questions.csv", sep = ""), header = T)
    questions$IsCommunity = NULL
    questions[questions$CommunityOwnedDate != "", "IsCommunity"] = "IsCommunity"
    questions[questions$CommunityOwnedDate == "", "IsCommunity"] = "IsNotCommunity"
    
    png(paste(outputDir, "Boxplot-", dataName, ".png", sep = ""), width = 600, height = 1200)
    par(mfrow = c(5, 1))
    Plot.Boxplot(questions, var = "Score", groupVar = "IsCommunity", horizontal = T)
    Plot.Boxplot(questions, var = "ViewCount", groupVar = "IsCommunity", horizontal = T)
    Plot.Boxplot(questions, var = "AnswerCount", groupVar = "IsCommunity", horizontal = T)
    Plot.Boxplot(questions, var = "CommentCount", groupVar = "IsCommunity", horizontal = T)
    Plot.Boxplot(questions, var = "FavoriteCount", groupVar = "IsCommunity", horizontal = T)
    dev.off()
    
    print(noquote("Outlier Analysis - Questions: DONE!"))
    print(noquote(""))
}


PMFAnalysis.Answers = function(inputDir, outputDir, dataName){
  answers = read.csv(paste(inputDir,"Answers.csv", sep=""),header=T)
  
  png(paste(outputDir,"PMF-", dataName, ".png", sep =""), width = 800, height = 400)    
  par(mfrow = c(1,2))
  Plot.PMF(answers, var = "Score")
  Plot.PMF(answers, var = "CommentCount")
  dev.off()
  
  png(paste(outputDir, "Boxplot-", dataName, ".png", sep = ""), width = 600, height = 400)
  par(mfrow = c(2, 1))
  Plot.Boxplot(answers, var = "Score", horizontal = T)
  Plot.Boxplot(answers, var = "CommentCount", horizontal = T)
  dev.off()
}

OutlierAnalysis.Answers = function(inputDir, outputDir, dataName){
  answers = read.csv(paste(inputDir,"Answers.csv", sep=""),header=T)
  
  answers$IsCommunity = NULL
  answers[answers$CommunityOwnedDate != "", "IsCommunity"] = "IsCommunity"
  answers[answers$CommunityOwnedDate == "", "IsCommunity"] = "IsNotCommunity"
  
  png(paste(outputDir, "Boxplot-", dataName, ".png", sep = ""), width = 600, height = 500)
  par(mfrow = c(2, 1))
  Plot.Boxplot(answers, var = "Score", groupVar = "IsCommunity", horizontal = T)
  Plot.Boxplot(answers, var = "CommentCount", groupVar = "IsCommunity", horizontal = T)
  dev.off()
}

PMFAnalysis.Comments = function(inputDir, comment.type, outputDir, dataName){
  comments = read.csv(paste(inputDir,"Comments-",comment.type,".csv",sep=""),header=T)
  
  png(paste(outputDir,"PMF-Comments-", dataName, ".png", sep =""), width = 400, height = 400)    
  Plot.PMF(comments, var = "Score")
  dev.off()
  
  png(paste(outputDir,"Boxplots-Comments-", dataName, ".png", sep = ""), width = 600, height = 200)
  Plot.Boxplot(comments, var = "Score", horizontal = T)
  dev.off()
}

OutlierAnalysis.Comments = function(inputDir, comment.type, outputDir, dataName){
  comments = read.csv(paste(inputDir,"Comments-",comment.type,".csv",sep=""),header=T)
  reference = read.csv(paste(inputDir,comment.type,".csv",sep=""),header=T)
  
  reference$IsCommunity = NULL
  reference[reference$CommunityOwnedDate != "", "IsCommunity"] = "IsCommunity"
  reference[reference$CommunityOwnedDate == "", "IsCommunity"] = "IsNotCommunity"
  
  join.table = merge(comments[,c(2,3)],reference[,c("Id","IsCommunity")],
                     by.x="PostId",by.y="Id")
  
  png(paste(outputDir,"Boxplots-Comments-", dataName, ".png", sep = ""), width = 600, height = 400)
  Plot.Boxplot(join.table, var = "Score", groupVar = "IsCommunity", horizontal = T)
  dev.off()
}

PMFAnalysis.Tags = function(inputDir, outputDir, dataName){
  tags = read.csv(paste(inputDir,"Tags.csv",sep=""),header=T)
  
  png(paste(outputDir,"PMF-", dataName, ".png", sep =""), width = 400, height = 400)    
  Plot.PMF(tags, var = "Count")
  dev.off()
  
  png(paste(outputDir,"Boxplot-", dataName, ".png", sep = ""), width = 600, height = 200)
  Plot.Boxplot(tags, var = "Count", horizontal = T)
  dev.off()
}

CorrelationAnalysis = function(inputDir, outputDir){

    print(noquote("Correlation Analysis..."))
    
    # panel.smooth function is built in.
    # panel.cor puts correlation in upper panels, size proportional to correlation
    panel.cor <- function(x, y, digits=2, prefix="", cex.cor, ...)
    {
        usr <- par("usr"); on.exit(par(usr))
        par(usr = c(0, 1, 0, 1))
        correlation = cor(x, y, method="pearson")
        txt <- paste(prefix, format(correlation, digits=2), sep="")
        text(0.5, 0.5, txt, cex = max(5 * abs(correlation), 1.5))
    }
    
    questions = read.csv(paste(inputDir, "Questions.csv", sep = ""), header = T)

    png(paste(outputDir, "Correlation-Questions.png", sep = ""), width = 800, height = 850)
    pairs(questions[,c("Score", "ViewCount", "CommentCount", "AnswerCount", "FavoriteCount")],
          lower.panel=panel.smooth, upper.panel=panel.cor, pch=20, main="Questions")
    dev.off()

    rm(questions)
    
    answers = read.csv(paste(inputDir, "Answers.csv", sep = ""), header = T)
    
    png(paste(outputDir, "Correlation-Answers.png", sep = ""), width = 800, height = 850)
    pairs(answers[,c("Score", "CommentCount")],
          lower.panel=panel.smooth, upper.panel=panel.cor, pch=20, main="Answers")
    dev.off()
    
    rm(answers)
    
    print(noquote("Correlation Analysis: DONE!"))
    print(noquote(""))
}

########## MAIN ########## 
MainDataAnalysis = function(dir = "../AllData/preprocessed/"){
    
  # 1) Runs the initial PreProcessment - Cleaning Data
  source("DataPreProcessment.R")
  MainPreProcessment()
  
  # 2) Plot the Probabilities Mass Functions
  PMFAnalysis.Questions(inputDir = dir, outputDir = "output/", dataName = "Questions")
  PMFAnalysis.Answers(inputDir = dir, outputDir = "output/", dataName = "Answers")
  PMFAnalysis.Comments(inputDir = dir, comment.type = "Questions", outputDir = "output/", dataName = "Questions")
  PMFAnalysis.Comments(inputDir = dir, comment.type = "Answers", outputDir = "output/", dataName = "Answers")
  PMFAnalysis.Tags(inputDir = dir, outputDir = "output/", dataName = "Tags")
  
  # 3) Plot the Outlier Analysis Charts
  OutlierAnalysis.Questions(inputDir = dir, outputDir = "output/", dataName = "Questions_IsCommunity")
  OutlierAnalysis.Answers(inputDir = dir, outputDir = "output/", dataName = "Answers_IsCommunity")
  OutlierAnalysis.Comments(inputDir = dir, comment.type = "Questions", outputDir = "output/",
                           dataName = "Questions_IsCommunity")
  OutlierAnalysis.Comments(inputDir = dir, comment.type = "Answers", outputDir = "output/",
                           dataName = "Answers_IsCommunity")
  
  # 4) Runs the final PreProcessment - Removing the Noise and Outliers
  RemoveNoiseAndOutliers(dir)
  
  # 5) Plot the Final Probabilities Mass Functions
  PMFAnalysis.Questions(inputDir = dir, outputDir = "output/", dataName = "Questions-Final")
  PMFAnalysis.Answers(inputDir = dir, outputDir = "output/", dataName = "Answers-Final")
  PMFAnalysis.Comments(inputDir = dir, comment.type = "Questions", outputDir = "output/", dataName = "Questions-Final")
  PMFAnalysis.Comments(inputDir = dir, comment.type = "Answers", outputDir = "output/", dataName = "Answers-Final")
  PMFAnalysis.Tags(inputDir = dir, outputDir = "output/", dataName = "Tags-Final")
  
  # 6) Correlation Analysis
  CorrelationAnalysis(dir, outputDir = "output/")
}