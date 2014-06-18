set ANDROID_HOME=D:\adt-bundle\sdk
set JAVA_HOME=C:\Program Files\Java\jdk1.7
set PATH=%PATH%;%JAVA_HOME%;%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools
gradle clean build
gradle installDebug
adb shell am start -n com.example.autorally/.MainActivity
pause