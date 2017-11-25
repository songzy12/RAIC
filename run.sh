HOME=`pwd`
echo $HOME
cd $HOME"/java-cgdk/src/main/java" && javac Runner.java && cd $HOME"/local-runner-en" && ./local-runner.sh && cd $HOME"/java-cgdk/src/main/java" && java Runner