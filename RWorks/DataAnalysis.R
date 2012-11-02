########## FUNCTIONS ##########
# require(ggplot2)
# require(gridExtra)
# 
# plot.pdf.group = function(df, var, groupVar = "", color = "gray"){
#     if (groupVar != ""){
#         ggplot(df, aes_string(x = var, col = groupVar, fill = groupVar)) + 
#             geom_density() + facet_grid(paste("~", groupVar, sep = "")) + 
#             theme(legend.position="none")
#     }else{
#         ggplot(df, aes_string(x = var)) + geom_histogram(binwidth = 1, fill = color, colour = color)
#     }
# }

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
    
    png(paste(outputDir, dataName, "-PMFs.png", sep =""), width = 800, height = 1000)    
    par(mfrow = c(3,2))
    Plot.PMF(questions, var = "Score")
    Plot.PMF(questions, var = "ViewCount")
    Plot.PMF(questions, var = "AnswerCount")
    Plot.PMF(questions, var = "CommentCount")
    Plot.PMF(questions, var = "FavoriteCount")
    dev.off()
    
    # PLot boxplots (grouped)
    png(paste(outputDir, dataName, "-Boxplots.png", sep = ""), width = 600, height = 800)
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
    
    png(paste(outputDir, dataName, "-Boxplots.png", sep = ""), width = 600, height = 1200)
    par(mfrow = c(5, 1))
    Plot.Boxplot(questions, var = "Score", groupVar = "IsCommunity", horizontal = T)
    Plot.Boxplot(questions, var = "ViewCount", groupVar = "IsCommunity", horizontal = T)
    Plot.Boxplot(questions, var = "AnswerCount", groupVar = "IsCommunity", horizontal = T)
    Plot.Boxplot(questions, var = "CommentCount", groupVar = "IsCommunity", horizontal = T)
    Plot.Boxplot(questions, var = "FavoriteCount", groupVar = "IsCommunity", horizontal = T)
    dev.off()
}

CorrelationAnalysis = function(dir){
    
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
    # ...
}