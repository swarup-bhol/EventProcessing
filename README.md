# EventProcessing
# Create pipe using command
mkfifo /tmp/json_pipe 

#verify data comming into pipe
tail -f /tmp/json_pipe

#create folder on your machine where you want to addinpu.json and output.json
#format the path in app config accordingly
