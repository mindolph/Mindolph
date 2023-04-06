### Mindolph

![](./DemoWorkspace/app_30.png)

Mindolph is an open source personal knowledge management software for all desktop platforms. 


### Features
* Create and manage your own files in separate workspaces with saving in your local storage, which means you have full control of your knowledge compared to cloud solutions.
* Organize your files as tree in your workspaces.
* Multiple tabs for opening files instead of a single file window, making it easy to switch back and forth between files.
* Supports Mind Map(`*.mmd`), Markdown(`*.md`), PlantUML(`*.puml`), CSV sheet(`*.csv`) and plain text(`*.txt`) file formats, more formats will be supported in the future.
* Syntax highlighting for Markdown and PlantUML files and instantly preview for them.
* Quickly navigate to file and search text in files under specific folder.
* Mind Map:
	* Edit mind map easily and quickly with key shortcuts.
	* Setup style of any element freely. 
	* Supports note, file link, URI link, image and emoticon for node.
	* Import from other mind map formats like Freemind, Mindmup, XMind, Coggle, Novamind.
	* Export to other file formats like Freemind, Markdown, image file(png/svg), AsciiDoc, etc.
* PlantUML:
	* Preview result instantly while editing.
	* Templates and code snippets for editing quickly.
	* Export to image file and ascii image.
* Markdown
	* Preview result instantly while editing.
	* Export to PDF and HTML file.
* CSV Sheet
	* Show and edit csv file visually.
* Supports multiple desktop platforms, including `macOS`, `Windows` and `Linux`.
* Many other features you would find out.

> This project is inspired by `netbeans-mmd-plugin` project, but Mindolph is developed based on JavaFX and provides more features and enhancements to edit and manage your knowledge easily.


### Screenshots
![](docs/main.png)

[see more](docs/screenshots.md)


### Releases

* MacOS [v1.1.2 stable](https://github.com/mindolph/Mindolph/releases/download/1.1.2-stable/Mindolph-1.1.2.dmg) &nbsp;&nbsp;&nbsp;&nbsp;[v1.2 beta](https://github.com/mindolph/Mindolph/releases/download/1.2-beta/Mindolph-1.2-beta.dmg)

* Windows x64 [v1.1.2 stable](https://github.com/mindolph/Mindolph/releases/download/1.1.2-stable/Mindolph-1.1.2.msi)&nbsp;&nbsp;&nbsp;&nbsp;[v1.2 beta](https://github.com/mindolph/Mindolph/releases/download/1.2-beta/Mindolph-1.2-beta.msi)

* Debian [v1.1.2 stable](https://github.com/mindolph/Mindolph/releases/download/1.1.2-stable/Mindolph-1.1.2.deb)&nbsp;&nbsp;&nbsp;&nbsp;[v1.2 beta](https://github.com/mindolph/Mindolph/releases/download/1.2-beta/Mindolph-1.2-beta.deb)

	> for supporting PlantUML, install graphviz first:  
	> `sudo apt install graphviz`

* Fedora [v1.1.2 stable](https://github.com/mindolph/Mindolph/releases/download/1.1.2-stable/Mindolph-1.1.2.rpm)&nbsp;&nbsp;&nbsp;&nbsp;[v1.2 beta](https://github.com/mindolph/Mindolph/releases/download/1.2-beta/Mindolph-1.2-beta.rpm)

* Java Executable(.jar) [v1.1.2 stable](https://github.com/mindolph/Mindolph/releases/download/1.1.2-stable/Mindolph-1.1.2.jar)&nbsp;&nbsp;&nbsp;&nbsp;[v1.2 beta](https://github.com/mindolph/Mindolph/releases/download/1.2-beta/Mindolph-1.2-beta.jar)

	> Java 17 is the minimum requirement to run this application.  
	> If you are using Linux, run the jar like this:  
	> `java -jar Mindolph-1.1.2.jar`  
	> If not, Download JavaFX SDK(20+) for your platform and extract to somewhere eg: `c:\javafx-sdk-20`, run the jar file like this:   
	> `java --module-path c:\javafx-sdk-20\lib --add-modules 
	> java.sql,javafx.controls,javafx.fxml,javafx.swing,javafx.web -jar 
	> Mindolph-1.1.2.jar`



[Release Notes](docs/release_notes.md)


### Development

See [code/README.md](code/README.md)
