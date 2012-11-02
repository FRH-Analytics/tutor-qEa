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
    questions = read.csv(paste(dir, "Questions.csv", sep = ""), header = T)
    
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
}

OutlierAnalysis.Questions = function(inputDir, outputDir, dataName){
    questions = read.csv(paste(dir, "Questions.csv", sep = ""), header = T)
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
}

CorrelationAnalysis = function(inputDir, outputDir){

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
}

########## MAIN ########## 
MainDataAnalysis = function(dir = "../AllData/preprocessed/"){
    
    # 1) Runs the initial PreProcessment - Cleaning Data
    source("DataPreProcessment.R")
    MainPreProcessment()
    
    # 2) Plot the Probabilities Mass Functions
    PMFAnalysis.Questions(inputDir = dir, outputDir = "output/", dataName = "Questions")
    # TODO (Matheus): Add the other plots here...
    
    # 3) Plot the Outlier Analysis Charts
    OutlierAnalysis.Questions(inputDir = dir, outputDir = "output/", dataName = "Questions_IsCommunity")
    # TODO (Matheus): Add the other plots here...
    
    # 4) Runs the final PreProcessment - Removing the Noise and Outliers
    RemoveNoiseAndOutliers(dir)
    
    # 5) Plot the Final Probabilities Mass Functions
    PMFAnalysis.Questions(questions, outputDir = "output/", dataName = "Questions-Final")
    # TODO (Matheus): Add the new plots here...
    
    # 6) Correlation Analysis
    CorrelationAnalysis(dir, outputDir = "output/")
}