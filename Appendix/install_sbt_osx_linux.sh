wget https://piccolo.link/sbt-1.2.3.tgz
tar -xzf sbt-1.2.3.tgz
export SBT_HOME=`pwd`/sbt/bin
echo 'PATH=$PATH':$SBT_HOME >> ~/.bashrc
export PATH=$PATH:$SBT_HOME
sbt version
