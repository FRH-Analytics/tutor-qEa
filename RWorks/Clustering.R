run.Kmedians = function(features){
    # Normalize data
    norm0_1 = function(data){
        (data - min(data))/(max(data) - min(data))
    }
    
    features2 = as.data.frame(sapply(features, function(x) norm0_1(x), USE.NAMES=T))
    
    kcca(x = features2, k = 4, family=kccaFamily("kmedians"))
    
    # image(cl)
    # barplot(cl)
}

run.Kmeans = function(features){
    kmeansruns(data = features, criterion="ch",
               krange = 3:5,                     # krange: range of the number of clusters
               iter.max = 100,                   # iter.max: number of iterations per execution
               runs = 10,                        # runs: Number of restarts each execution
               scaledata = T)                    # scaledata: center the data
}

clustering = function(preprocessedDir, clusteringDir, dimensionName, createFeaturesFunc){
    
    print(noquote(paste("Clustering: ", dimensionName, "...", sep = "")))
    
    featuresFile = paste(clusteringDir, "/", tolower(dimensionName), ".csv", sep = "")
    
    if (file.exists(featuresFile)){
        print(noquote("Loading Features..."))
        features = read.csv(featuresFile)
    }else{
        features = createFeaturesFunc(preprocessedDir)
        
        print(noquote("Saving Features..."))
        write.csv(features, file = featuresFile, row.names = F)
    }   
    
    # Excluding the ID and Cluster.KMeans collumns
    if ("Cluster.KMeans" %in% colnames(features)){
        features = features[,-ncol(features)]
    }
    
    print(noquote("Clustering..."))
    model = run.Kmeans(features[,-1])
    
    print(noquote("Persisting Clusters..."))
    features$Cluster.KMeans = model$cluster
    write.csv(features, file = featuresFile, row.names = F)
    
    print(noquote("Clustering: DONE!"))
    print(noquote(""))
}

plot.cluster = function(clusteringDir, dimensionName){
    
    print(noquote(paste("Plotting: ", dimensionName, "...", sep = "")))
    
    features = read.csv(paste(clusteringDir, tolower(dimensionName), ".csv", sep = ""), header = T)
    clusters = features$Cluster.KMeans
    
    # Exclude the Id and Cluster attributes
    features = features[,-c(1, ncol(features))]
    
    # Plot Results
    print(noquote("Plotting ScatterPlot..."))
    png(paste("output/Cluster-Kmeans-", dimensionName, ".png", sep = ""), width = 850, height = 900)
    plot(features, col = (clusters + 2), 
         main = paste(toupper(dimensionName), " clustering - KMeans with ", length(unique(clusters)), " center(s)", sep = ""))
    dev.off()
    
    print(noquote("Plotting: DONE!"))
    print(noquote(""))
}

analyse.cluster = function(clusteringDir, dimensionName){
    # TODO!
    # Decision Tree
}

createFeatures.popularity = function(preprocessedDir){
    print(noquote("Reading Data..."))
    questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
    
    print(noquote("Creating Attributes..."))
    features = questions[,c("Id", "Score", "ViewCount", "CommentCount", 
                            "AnswerCount", "FavoriteCount")]
    
    return(features)
}
    
createFeatures.difficulty = function(preprocessedDir){
    print(noquote("Reading Data..."))
    questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
    answers = read.csv(paste(preprocessedDir, "Answers.csv", sep = ""), header = T)
    
    print(noquote("Creating Attributes..."))
    features = data.frame(Id = questions$Id,
                          AnswerCount = questions$AnswerCount,
                          CommentCount = questions$CommentCount,
                          HasAcceptedAnswer = (questions$AcceptedAnswerId != -1))
    
    features$TimeToMaxScoreAnswer = foreach(i = 1:nrow(features), .combine = rbind) %dopar%{
        ans = answers[answers$ParentId == features$Id[i],]
        
        if (nrow(ans) > 0){
            ans = ans[order(ans$Score, ans$CreationDate, decreasing=T),][1,]
            val = as.numeric(difftime(ans$CreationDate , questions$CreationDate[i], units="hours"))
            
            # PROBLEM 1: Some answers have been created before the question!!! 
            # (Cause: Question Merging...)
            val = if (val < 0){0}else{val}
        } else {
            # PROBLEM 2: No answer! (Probably very difficult! We set -1...)
            -1
        }
    }
    
    return(features)
}

createFeatures.longevity = function(preprocessedDir){
    # TODO!
    # List Attributes...
}

####################################################
####################### MAIN #######################
####################################################
library(fpc, quietly = T)
library(flexclust, quietly = T) 
require(foreach, quietly = T)
require(doMC, quietly = T)
library(scatterplot3d, quietly=T)

registerDoMC()

dir.create("../AllData/clustering", showWarnings=F)
preprocessedDir = "../AllData/preprocessed/"
clusteringDir = "../AllData/clustering/"

clustering(preprocessedDir, clusteringDir, "Difficulty", createFeatures.difficulty)
clustering(preprocessedDir, clusteringDir, "Popularity", createFeatures.popularity)

plot.cluster(clusteringDir, "Difficulty")
plot.cluster(clusteringDir, "Popularity")