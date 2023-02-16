# Mindolph Development

### Prerequisite
* JDK 17+
* JavaFX 17+
* Maven 3.x

### How to build
* Install MFX

```shell
git clone https://github.com/mindolph/mfx.git
cd mfx
mvn install -Dmaven.test.skip=true
```

* Build Mindolph
```shell
git clone https://github.com/mindolph/Mindolph.git
cd Mindolph
mvn install -Dmaven.test.skip=true
```

After building is done, an executable jar file and an installer for your platform can be found in `mindolph-desktop/target/`
