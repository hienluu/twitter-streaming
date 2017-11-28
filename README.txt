#
chmod +x submit.sh
chmod +x sbt/sbt

# package
./sbt/sbt assembly

# to generate Eclipse project
./sbt/sbt eclipse


# ========= If you would like to run Twitter Streaming application from command ====

# update env.sh with path to Spark home directory and Twitter application keys

# to submit your streaming app. 
./submit 
