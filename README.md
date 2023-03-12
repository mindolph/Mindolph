### Mindolph

![](./DemoWorkspace/app_30.png)

Mindolph is an open source personal knowledge management software for all desktop platforms. 


### Features
* Create and manage your own files in separate workspaces with saving in your local storage, which means you have full control of your knowledge compared to cloud solutions.
* Organize your files as tree in your workspaces.
* Supports mindmap(`*.mmd`), markdown(`*.md`), plantuml(`*.puml`) and plain text(`*.txt`) file formats, more formats will be supported in the future.
* Syntax highlighting for markdown and plantuml files and instantly preview for them.
* Quickly navigate to file and search text in files under specific folder.
* Mindmap:
	* Edit mind map easily and quickly.
	* Supports note, file link, URI link, image and emoicon for node.
	* Import from other mind map formats like Freemind, Mindmup, XMind, Coggle, Novamind.
	* Export to other file formats like Freemind, Markdown, image file, etc.
* PlantUML:
	* Preview result instantly while editing.
	* Templates and code snippets for editing quickly.
	* Export to image file and ascii image.
* Markdown
	* Preview result instantly while editing.
	* Export to PDF and HTML file.
* Supports multiple platforms, including `macOS`, `Windows` and `Linux`.
* Many other features you would find out.

> This project is inspired by `netbeans-mmd-plugin` project, but Mindolph is developed based on JavaFX and provides more features and enhancements to edit and manage your knowledge easily.


### Screenshots
![](docs/main.png)

[see more](docs/screenshots.md)


### Releases

* Unstable: v1.1 RC

	* [MacOS](https://github.com/mindolph/Mindolph/releases/download/1.1-RC/Mindolph-1.1-RC.dmg)
	
	* [Windows x64](https://github.com/mindolph/Mindolph/releases/download/1.1-RC/Mindolph-1.1-RC.msi)
	
	* [Debian](https://github.com/mindolph/Mindolph/releases/download/1.1-RC/Mindolph-1.1-RC.deb)
	
		> for supporting PlantUML, install graphviz first:  
		> `sudo apt install graphviz`
	
	* [Fedora](https://github.com/mindolph/Mindolph/releases/download/1.1-RC/Mindolph-1.1-RC.rpm)
	
	* [Java Executable(.jar)](https://github.com/mindolph/Mindolph/releases/download/1.1-RC/Mindolph-1.1-RC.jar)
	
		> If you are using Linux, run the jar like this:  
		> `java -jar Mindolph-1.1-RC.jar`  
		> If not, Download JavaFX SDK(17+) for your platform and extract to somewhere eg: `/mnt`, run the jar file like this:   
		> `java --module-path /mnt/javafx-sdk-17/lib --add-modules 
		> java.sql,javafx.controls,javafx.fxml,javafx.swing,javafx.web -jar 
		> Mindolph-1.1-RC.jar`


* Stable: v1.0

	* [MacOS](https://github.com/mindolph/Mindolph/releases/download/1.0-stable/Mindolph-1.0.dmg)
	
	* [Windows x64](https://github.com/mindolph/Mindolph/releases/download/1.0-stable/Mindolph-1.0.msi)
	
	* [Linux(.deb)](https://github.com/mindolph/Mindolph/releases/download/1.0-stable/Mindolph_1.0_amd64.deb)
	
		> for supporting PlantUML, install graphviz first:  
		> `sudo apt install graphviz`
	
	* [Java Executable(.jar)](https://github.com/mindolph/Mindolph/releases/download/1.0-stable/Mindolph-1.0.jar)
	
		> Download JavaFX SDK(17+) for your platform and extract to somewhere eg: `/mnt`, run the jar file like this:   
		> `java --module-path /mnt/javafx-sdk-17/lib --add-modules 
		> java.sql,javafx.controls,javafx.fxml,javafx.swing,javafx.web -jar 
		> Mindolph-1.0.jar`

[Release Notes](docs/release_notes.md)


### Development

See [code/README.md](code/README.md)
