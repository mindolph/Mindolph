# Mindolph Development

### Prerequisite
* JDK 17+
* JavaFX 21+
* Maven 3.x

### How to setup develop environment

* Install MFX

    ```shell
    git clone https://github.com/mindolph/mfx.git
    cd mfx
    mvn install -Dmaven.test.skip=true
    ```

* Install FontawesomeFX

    ```shell
    git clone https://mindolph@bitbucket.org/mindolph-app/fontawesomefx.git
    cd fontawesomefx
    ./gradlew publishToMavenLocal
    ```

* Mindolph

    ```shell
    git clone https://github.com/mindolph/Mindolph.git
    ```

    Use your favourite IDE to create a new project in folder `Mindolph/code`, after compiling completed, launch the application by executing main method in class `com.mindolph.fx.Launcher`.

### How to build an executable fat jar file

```shell
mvn package -Dmaven.test.skip=true
```

### How to build platform dependent distribution

* Install Packaging Tools:  
    * macOS
      install Xcode command line tools
    * Debian
      install fakeroot package
    * Fedora
      install rpm-build package
    * Windows
      install third party tool WiX 3.0 or later

* Install JavaFX jmods:  

    Download JavaFX 21 jmods package from https://gluonhq.com/products/javafx/ and extract to somewhere like `/mnt/javafx-jmods-21/`

    Set environment variable:
    ```shell
    export JAVAFX_HOME=/mnt/javafx-jmods-21/
    ```

* Build Mindolph distribution for your operating system:  

    ```shell
    mvn install -Dmaven.test.skip=true
    ```

    After building is done, an executable jar file and an installer for your platform can be found in `Mindolph/code/mindolph-desktop/target/`
