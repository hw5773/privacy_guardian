#!/bin/bash

# parameter validation
if [ $# -ne 1 ]; then
    echo "Missing parameter - you need to specify which apk file to decompile"
    exit 1
fi
if [ ! $1 =~ .apk$ ]; then
    echo "The parameter is not an apk file : $1"
    exit 1
fi
if [ ! -f $1 ]; then
    echo "$1 doesn't exist."
    exit 1
fi

# download required libraries
LIB=lib
CFR=cfr_0_117.jar
DEX2JAR=dex2jar-2.0
APKTOOL=apktool_2.1.1.jar

if [ ! -f $LIB/$CFR ]; then
    wget -P $LIB http://www.benf.org/other/cfr/$CFR
fi
if [ ! -d $LIB/$DEX2JAR ]; then
    wget -P $LIB https://bitbucket.org/pxb1988/dex2jar/downloads/${DEX2JAR}.zip
    unzip -d $LIB ./$LIB/${DEX2JAR}.zip
    chmod +x ./$LIB/$DEX2JAR/*.sh
fi
if [ ! -f $LIB/$APKTOOL ]; then
    wget -P $LIB https://bitbucket.org/iBotPeaches/apktool/downloads/$APKTOOL
fi

# decompile
APK=$1
APP=${APK/.apk/}
JAR=${APP}-dex2jar.jar

java -jar ./$LIB/$APKTOOL d $APK
sh ./$LIB/$DEX2JAR/d2j-dex2jar.sh $APK
java -jar ./$LIB/$CFR $JAR --outputdir ./$APP/java

# cleaning
rm -rf ./$APP/smali # comment out if you need smali files
JARBOX=jar
if [ ! -d $JARBOX ]; then
    mkdir $JARBOX
fi
mv $JAR $JARBOX
