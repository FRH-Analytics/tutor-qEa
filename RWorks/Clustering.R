library(fpc, quietly = T)

dir = "../AllData/preprocessed/"

# Select data
print(noquote("Selecting Data..."))
questions = read.csv(paste(dir, "Questions.csv", sep = ""), header = T)
attributes = c("Score", "ViewCount", "CommentCount", "AnswerCount", "FavoriteCount")
data = questions[,attributes]
rm(questions)

# Run Clustering
print(noquote("Clustering..."))
model = kmeansruns(data = data, criterion="ch",
                   krange = 3:5,                     # krange: range of the number of clusters
                   iter.max = 100,                   # iter.max: number of iterations per execution
                   runs = 10,                        # runs: Number of restarts each execution
                   scaledata = T)                    # scaledata: center the data

# Plot Results
print(noquote("Plotting ScatterPlot..."))
png("output/KmeansCluster-Questions.png", width = 850, height = 900)
plot(data, col = (model$cluster + 2), 
     main = paste("KMeans Clusters com", nrow(model$centers), "centro(s)"))
dev.off()
