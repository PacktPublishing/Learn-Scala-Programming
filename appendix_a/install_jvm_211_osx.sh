wget https://download.java.net/java/ga/jdk11/openjdk-11_osx-x64_bin.tar.gz
tar -xzf openjdk-11_osx-x64_bin.tar.gz
export JAVA_HOME=`pwd`/jdk-11.jdk/Contents/Home
echo 'PATH=$PATH':$JAVA_HOME/bin >> ~/.bashrc
export PATH=$PATH:$JAVA_HOME/bin
java -version
