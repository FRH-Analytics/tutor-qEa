clustering.runKCenters = function(features, kccaFamilyName, outputDir){
    norm.0_1 = function(data){
        (data - min(data))/(max(data) - min(data))
    }
    norm.standardScore = function(data){
        (data - mean(data))/sd(data)
    }
    
    features2 = as.data.frame(sapply(features, norm.standardScore))

    krange = 2:8
    repetitions = 10
    
    stepFlexclust(features2, k = krange, nrep = repetitions, save.data = F,
                  FUN = kcca, family=kccaFamily(kccaFamilyName), multicore =T)
}

clustering.selectCentroidsNum = function(stepFlexclustObj, data, kccaFamilyName){

    getUserInput.intRange = function(msg, grepPattern, intRange){
        n = NA
        while(! n %in% intRange){
            n <- readline(msg)
            n <- ifelse(grepl(grepPattern,n),-1,as.integer(n))
        }
        return(n)
    }
    
    # TODO: Create a PNG with this...
    plot(stepFlexclustObj, main = kccaFamilyName)

    print(noquote("Compare the Clusterings: By its distances in a X-Y scatterplot..."))
    xCol = getUserInput.intRange(paste("Select the X axis collumn (1 to ", ncol(data), "): ", sep = ""), 
                                 paste("[^1-", ncol(data), "]",sep = ""), 1:ncol(data))
    yCol = getUserInput.intRange(paste("Select the Y axis collumn (1 to ", ncol(data), "): ", sep = ""), 
                                 paste("[^1-", ncol(data), "]",sep = ""), 1:ncol(data))
    
    # TODO: Create a huge PNG with this...
    image(getModel(stepFlexclustObj, 1), data = data, which = c(xCol, yCol))
    
    centroidsNum = getUserInput.intRange(paste("Select the Centroids Number (", min(stepFlexclustObj@k), 
                                               " to ", max(stepFlexclustObj@k), "): ", sep = ""), 
                                       paste("[^", min(stepFlexclustObj@k), "-", max(stepFlexclustObj@k), "]",sep = ""), 
                                         min(stepFlexclustObj@k):max(stepFlexclustObj@k))
    
    # TODO: Create a PNG with this...
    barchart(getModel(stepFlexclustObj, centroidsNum-1))
    
    return(centroidsNum)
}

clustering = function(preprocessedDir, clusteringDir, dimensionName, 
                      createFeaturesFunc, outputDir){
    
    print(noquote(paste("Clustering: ", dimensionName, "...", sep = "")))
    
    featuresFile = paste(clusteringDir, tolower(dimensionName), ".csv", sep = "")
    
    if (file.exists(featuresFile)){
        print(noquote("Loading Features..."))
        features = read.csv(featuresFile)
    }else{
        features = createFeaturesFunc(preprocessedDir)
        
        print(noquote("Saving Features..."))
        write.csv(features, file = featuresFile, row.names = F)
    }   

    # Create a Data only Data.Frame, removing ID and possible previous clustering
    onlyData = features
    onlyData[,"Id"] = NULL
    onlyData[,"Cluster.KMeans"] = NULL
    onlyData[,"Cluster.KMedians"] = NULL

    if (! "Cluster.KMeans" %in% colnames(features)){
        allKMeans = clustering.runKCenters(onlyData, kccaFamilyName="kmeans", outputDir)
        
        centroidNum.KMeans = clustering.selectCentroidsNum(allKMeans, onlyData, kccaFamilyName="Kmeans")
        
        features$Cluster.KMeans = getModel(allKMeans, centroidNum.KMeans - 1)@cluster
    }
    if (! "Cluster.KMedians" %in% colnames(features)){
        allKMedians = clustering.runKCenters(onlyData, kccaFamilyName="kmedians", outputDir)
        
        centroidNum.KMedians = clustering.selectCentroidsNum(allKMedians, onlyData, kccaFamilyName="Kmedians")    
        
        features$Cluster.KMedians = getModel(allKMedians, centroidNum.KMedians - 1)@cluster
    }
    
    print(noquote("Clustering: Persisting..."))
    write.csv(features, file = featuresFile, row.names = F)
    
    print(noquote("Clustering: DONE!"))
    print(noquote(""))
}

plot.clustering = function(clusteringDir, outputDir, dimensionName, 
                           kccaFamilyName, clusterCol, nonFeatureCols){
    
    print(noquote(paste("Plotting: ", dimensionName, "...", sep = "")))
    
    features = read.csv(paste(clusteringDir, tolower(dimensionName), ".csv", sep = ""), header = T)
    clusters = features[, clusterCol]
    
    # Exclude the Id and Cluster attributes
    features = features[,-nonFeatureCols]
    
    # Plot Results
    print(noquote("Plotting: ScatterPlot..."))
    png(paste(outputDir, dimensionName, "-", kccaFamilyName, "-Scatteplot.png", sep = ""), width = 850, height = 900)
    plot(features, col = (clusters + 2), 
         main = paste(dimensionName, " clustering - ", kccaFamilyName, 
                      " with ", length(unique(clusters)), " center(s)", sep = ""))
    dev.off()
    
    print(noquote("Plotting: DONE!"))
    print(noquote(""))
}

analyse.clustering = function(clusteringDir, outputDir, dimensionName, kccaFamilyName, 
                              clusterColName, nonFeatureCols){
    
    print(noquote(paste("Analysing: ", dimensionName, "...", sep = "")))
    
    features = read.csv(paste(clusteringDir, tolower(dimensionName), ".csv", sep = ""), header = T)

    onlyData = features[,-nonFeatureCols]
    onlyCluster = as.data.frame(features[,clusterColName])
    features = cbind(onlyData, onlyCluster)
    colnames(features)[ncol(features)] = clusterColName
    
    print(noquote("Analysing: Creating Decision Tree..."))
    tree = rpart(as.formula(paste(clusterColName, "~ .")), features, method = "class", parms = list(split = "gini"))
    
    png(paste(outputDir, dimensionName, "-", kccaFamilyName,"-DecisionTree.png",sep = ""), width = 700, height = 600)
    plot(tree, uniform = T, branch = .5, margin = .1, 
         main = paste(dimensionName, "clustering - ", kccaFamilyName, 
                      " with ", length(unique(onlyCluster[,1])), " center(s)\nDecision Tree Analysis"))
    text(tree, cex = 1, all = T) 
    dev.off()
    
    print(noquote("Analysing: Detailing Clustering Sizes and Tree..."))
    details.clustering(features[,clusterColName], tree, dimensionName, outputDir, kccaFamilyName)
    
    print(noquote("Analysing: Generating Feature PMFs..."))
    nRows = ceiling(ncol(onlyData)/2)
    png(paste(outputDir, dimensionName, "-PMF.png", sep =""), width = 800, height = (nRows * 300))
    par(mfrow = c(nRows,2))
    for(i in 1:ncol(onlyData)){
        plot(prop.table(table(onlyData[,i])), ylab = "Probabilidade", main = colnames(onlyData)[i])
    }
    dev.off()
    
    print(noquote("Analysing: DONE!"))
    print(noquote(""))
}

details.clustering = function(clusters, tree, dimensionName, outputDir, kccaFamilyName){
    print(noquote(paste("Persisting: ", dimensionName, "...", sep = "")))
    
    # Output to file...
    sink(file = paste(outputDir, dimensionName, "-", kccaFamilyName, "-Analysis.txt", sep = ""))
    write(paste("================ Análise da Dimensão:", dimensionName, "================"), file = "")
    write("", file = "")
    
    write(">> Tamanho dos Clusters:", file = "")
    count = count(clusters)
    colnames(count) = c("Cluster", "Count")
    print(count)
    write("", file = "")
    
    write(">> Resultado da Árvore de Decisão:", file = "")
    print(tree)
    sink()
    # Output to console again...
    
    print(noquote("Persisting: DONE!"))
    print(noquote(""))
}


createFeatures.tutorQeA = function(preprocessedDir){
    
    derivedFeaturesFile = paste(preprocessedDir, "/DerivedFeatures.csv", sep = "")
    if (!file.exists(derivedFeaturesFile)){
        print(noquote("Running the DerivedFeatures script to generate the Debate and Hotness..."))
        source("DerivedFeatures.R")
        MainDerivedFeatures()
    }

    print(noquote("Loading the DerivedFeatures with the Debate and Hotness..."))
    question.features = read.csv(derivedFeaturesFile, header = T)
    
    print(noquote("Reading Data..."))
    questions = read.csv(paste(preprocessedDir, "Questions.csv", sep = ""), header = T)
    
    print(noquote("Merging Default Features and Derived Features by Id..."))
    features = data.frame(Id = questions$Id,
                          Score = questions$Score,
                          AnswerCount = questions$AnswerCount)
    features = merge(features, question.features, by = "Id")
    
    # Normalize the new Features between 0 and 1
    features$Debate = (features$Debate - min(features$Debate))/(max(features$Debate) - min(features$Debate))
    features$Hotness = (features$Hotness - min(features$Hotness))/(max(features$Hotness) - min(features$Hotness))
    
    return(features)
}

####################################################
####################### MAIN #######################
####################################################
MainClustering = function(){
    library(flexclust, quietly = T) 
    library(rpart, quietly=T)
    require(doMC, quietly = T)
    
    if (Sys.info()["sysname"] == "Linux"){
        library(doMC)
        registerDoMC()
    }
    
    dir.create("../AllData/clustering/", showWarnings=F)
    dir.create("output/clustering/", showWarnings=F)
    
    preprocessedDir = "../AllData/preprocessed/"
    clusteringDir = "../AllData/clustering/"
    outputDir = "output/clustering/"
    
    allKcenters = clustering(preprocessedDir, clusteringDir, dimensionName = "TutorQeA",
                             createFeaturesFunc = createFeatures.tutorQeA, outputDir)

    # KMEANS
    plot.clustering(clusteringDir, outputDir, dimensionName = "TutorQeA", kccaFamilyName="KMeans",
                    clusterCol="Cluster.KMeans", nonFeatureCols = c(1, 6, 7))
    analyse.clustering(clusteringDir, outputDir, dimensionName = "TutorQeA", kccaFamilyName="KMeans",
                       clusterColName="Cluster.KMeans", nonFeatureCols = c(1, 6, 7))
    
    # KMEDIANS
    plot.clustering(clusteringDir, outputDir, dimensionName = "TutorQeA", kccaFamilyName="KMedians",
                    clusterCol="Cluster.KMedians", nonFeatureCols = c(1, 6, 7))
    analyse.clustering(clusteringDir, outputDir, dimensionName = "TutorQeA", kccaFamilyName="KMedians",
                       clusterColName="Cluster.KMedians", nonFeatureCols = c(1, 6, 7))
}