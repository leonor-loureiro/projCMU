P2Photo
=================


### Mockups
+ https://app.moqups.com/qHCze9X2Ld/view
+ https://drive.google.com/file/d/1lPYT-YBmJK56BbYzyztG7U6YZWZtASkC/view?usp=sharing
+ https://app.moqups.com/1Ydikr9xHi/view

### Requirements
+ Maven https://maven.apache.org/install.html
+ Java 1.8
+ Android Studio https://developer.android.com/studio
         
### Install and run the server
```bash
 $ cd <root-project-folder>/server
 $ mvn clean install
 $ mvn spring-boot run
```

### Generate a signed APK for the android app 
+ Open the project on Android Studio
+ Build > Generate Signed APK
+ Drag the APK <project-root-folder>/app/debug/app-debug.apk to the emulator and drop it
  + Keystore path: <project-root-folder>/p2photo-keystore.jks
  + Key store password: password
  + Key alias: p2photo
  + Key password: password
+ Next > Build type: debug > Finish


### Install the APK and open the app
+ Open the AVD Manager
+ Choose an emulator and launch it
+ Drag the APK <project-root-folder>/app/debug/app-debug.apk to the emulator and drop it
  + If the app is already installed in the emulator, it must be uninstall, or this step will fail
+ Once the APK has been successfully installed, open the app from the android application's list
         
