<p>
	<a title="Releases" target="_blank" href="https://github.com/mindolph/Mindolph/releases"><img src="https://img.shields.io/github/release/mindolph/Mindolph.svg?style=flat-square&color=9CF"></a>
	<a title="Downloads" target="_blank" href="https://github.com/mindolph/Mindolph/releases"><img src="https://img.shields.io/github/downloads/mindolph/Mindolph/total.svg?style=flat-square&color=blueviolet"></a>
	<a title="GitHub Commits" target="_blank" href="https://github.com/mindolph/Mindolph/commits/main/"><img src="https://img.shields.io/github/commit-activity/m/mindolph/Mindolph.svg?style=flat-square"></a>
	<a title="Last Commit" target="_blank" href="https://github.com/mindolph/Mindolph/commits/main/"><img src="https://img.shields.io/github/last-commit/mindolph/Mindolph.svg?style=flat-square&color=FF9900"></a>
</p>

### Mindolph

![](./DemoWorkspace/app_30.png)

Mindolph is an open source personal knowledge management software for all desktop platforms. [简体中文](./docs/README_zh_CN.md)


### Features
* Create and manage your own files in separate workspaces with saving in your local storage, which means you have full control of your knowledge compared to cloud solutions.
* Organize your files as tree in your workspaces.
* Multiple tabs for opening files instead of a single file window, making it easy to switch back and forth between files.
* Supports Mind Map(`*.mmd`), Markdown(`*.md`), PlantUML(`*.puml`), CSV sheet(`*.csv`) and plain text(`*.txt`) file formats, more formats will be supported in the future.
* Quickly navigate to file and search text in files under specific folder.
* Mind Map:
	* Edit mind map easily and quickly with key shortcuts.
	* Supports theme and provides some pre-defined themes(`Classic`, `Light` and `Dark`), customizing themes by duplicating existing theme and setup style of any element freely.
	* Supports note, file link, URI link, image and emoticon for topic node.
	* Import from other mind map formats like Freemind, Mindmup, XMind, Coggle, Novamind.
	* Export to other file formats like Freemind, Markdown, image file(png/svg), AsciiDoc, etc.
	* Compatible with files created by `netbeans-mmd-plugin`.
* PlantUML:
	* Syntax highlighting.
	* Preview result instantly while editing.
	* Templates and code snippets for editing quickly.
	* Export to image file and ascii image.
* Markdown
	* Syntax highlighting.
	* Preview result instantly while editing.
	* Export to PDF and HTML file.
* CSV Sheet
	* Show and edit csv file visually.
* Supports multiple desktop platforms, including `macOS`, `Windows` and `Linux`.
* Many other features you would find out.


### Screenshots
<p float="left">
	<img src="docs/screenshots/mindmap_light.jpg" width="45%"/>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<img src="docs/screenshots/mindmap_dark.jpg" width="45%"/>
</p>
<p float="left">
	<img src="docs/screenshots/markdown1.jpg" width="45%"/>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<img src="docs/screenshots/puml_activity.jpg" width="45%"/>
</p>
<p float="left">
	<img src="docs/screenshots/puml_sequence.jpg" width="45%"/>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<img src="docs/screenshots/puml_component2.jpg" width="45%"/>
</p>
<p float="left">
	<img src="docs/screenshots/puml_state.jpg" width="45%"/>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<img src="docs/screenshots/find_in_files.jpg" width="45%"/>
</p>

[See More](docs/screenshots.md)


### Releases

|Platform|Type|Stable|Unstable|Note|
|----|----|----|----|----|
|Release Notes| |[v1.6.x](docs/release-notes/v1.6/v1.6.md)|[v1.7.x](docs/release-notes/v1.7/v1.7.md)| |
|macOS|.dmg|[v1.6.12](https://github.com/mindolph/Mindolph/releases/download/v1.6.12/Mindolph-1.6.12-x86.dmg) |[v1.7.2](https://github.com/mindolph/Mindolph/releases/download/v1.7.2/Mindolph-1.7.2.dmg) | Intel |
|macOS|.dmg|[v1.6.12](https://github.com/mindolph/Mindolph/releases/download/v1.6.12/Mindolph-1.6.12-aarch64.dmg) |[v1.7.2](https://github.com/mindolph/Mindolph/releases/download/v1.7.2/Mindolph-1.7.2-aarch64.dmg) | Apple Silicon </br>for supporting PlantUML, install graphviz first:</br>`brew install graphviz`|
|Windows|.msi|[v1.6.12](https://github.com/mindolph/Mindolph/releases/download/v1.6.12/Mindolph-1.6.12.msi) |[v1.7.2](https://github.com/mindolph/Mindolph/releases/download/v1.7.2/Mindolph-1.7.2.msi) | |
|Debian|.deb|[v1.6.12](https://github.com/mindolph/Mindolph/releases/download/v1.6.12/Mindolph-1.6.12.deb)|[v1.7.2](https://github.com/mindolph/Mindolph/releases/download/v1.7.2/Mindolph-1.7.2.deb)|	for supporting PlantUML, install graphviz first:</br>  `sudo apt install graphviz`|
|Fedora|.rpm|[v1.6.12](https://github.com/mindolph/Mindolph/releases/download/v1.6.12/Mindolph-1.6.12.rpm)|[v1.7.2](https://github.com/mindolph/Mindolph/releases/download/v1.7.2/Mindolph-1.7.2.rpm)| |
|Java Executable|.jar|[v1.6.12](https://github.com/mindolph/Mindolph/releases/download/v1.6.12/Mindolph-1.6.12.jar)|[v1.7.2](https://github.com/mindolph/Mindolph/releases/download/v1.7.2/Mindolph-1.7.2.jar)| Java 17 is the minimum version to run this application. 	</br> If you are using Linux, run the jar like this:  </br> `java -jar Mindolph-1.6.12.jar`  </br> If not, Download latest JavaFX SDK for your platform and extract to somewhere eg: `c:\javafx-sdk-21`, run the jar file like this:   </br> `java --module-path c:\javafx-sdk-21\lib --add-modules  java.sql,javafx.controls,javafx.fxml,javafx.swing,javafx.web,jdk.crypto.ec -jar  Mindolph-1.6.12.jar` |


[Change Logs](docs/change_logs.md)


### Development

Mindolph is developed based on JavaFX, see [code/README.md](code/README.md) for more details.

Future Plan:

* 1.7: Experimental GenAI support.
* 1.8: Improvement and refactor.
