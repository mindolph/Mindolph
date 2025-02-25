# Mindolph Development

### Prerequisites
* JDK 21+
* JavaFX 23+
* Maven 3.x

### How to setup develop environment

* Install MFX

    ```shell
    git clone https://github.com/mindolph/mfx.git
    cd mfx
    mvn install -Dmaven.test.skip=true
    ```
  > If there is something wrong with the accessing Maven central repository, just use the `aliyun` profile:  
  > `mvn install -Dmaven.test.skip=true -Paliyun`

* Install FontawesomeFX

    ```shell
    git clone https://mindolph@bitbucket.org/mindolph-app/fontawesomefx.git
    cd fontawesomefx/fontawesomefx
    ./gradlew publishToMavenLocal
    ```
    > for JDK 17, switch git branch:  
`git switch fontawesomefx-17.0.0`

* Mindolph

    ```shell
    git clone https://github.com/mindolph/Mindolph.git
    ```

    Use your favorite IDE to create a new project in the folder `Mindolph/code`. after compiling is completed, launch the application by executing main method in class `com.mindolph.fx.Launcher`.

### How to build an executable fat jar file

```shell
mvn package -Dmaven.test.skip=true
```
or
```shell
mvn package -Dmaven.test.skip=true -Paliyun
```

### How to build platform-dependent distribution

* Install Packaging Tools:  
    * macOS  
      install Xcode command line tools
    * Debian/Ubuntu  
      install fakeroot package
    * Fedora  
      install rpm-build package
    * Windows  
      install third-party tool WiX 3.0 or latest

* Install JavaFX jmods:  

    Download the latest JavaFX 23 jmods package from https://gluonhq.com/products/javafx/ and extract to somewhere like `/mnt/javafx-jmods-23/`

    Set environment variable:  
    ```shell
    export JAVAFX_HOME=/mnt/javafx-jmods-23/
    ```

* Build Mindolph distribution for your operating system:  

    ```shell
    mvn install -Dmaven.test.skip=true
    ```
    or 
    ```shell
    mvn install -Dmaven.test.skip=true -Paliyun
    ```
    After building is done, an executable jar file and an installer for your platform can be found in `Mindolph/code/mindolph-desktop/target/`
