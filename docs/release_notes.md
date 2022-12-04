# Release Notes

### v1.0 beta9
* Improvement: add icon to preview pages and refine the title extraction.
* Improvement: optimize the exception message dialog.
* Fix: spelling in preference dialog panel.
* Fix: exception when opening an already opened file which was changed from "find in files" search result list.
* Fix: reloading a folder generates duplicate tree items.
* Fix: select an opened file in workspace causes exception.
* Fix: exception when dragging graphic view with non-primary mouse buttons.
* Dependencies update: PlantUML 1.2022.13, RichTextFX 0.11.0.

### v1.0 beta8
* Improvement: the performance of loading workspace tree is improved.
* Fix: the tooltip of opened file doesn't change after any folder in the file path is renamed.
* Fix: unable to clone topic and unable to copy/paste topic with sub-topics since last release.
* Dependencies update: Java 17.0.5, PlantUML 1.2022.12
* Some code refactor.

### v1.0 beta7
* Enhancement: add JSON and YAML support to plantuml editor.
* Enhancement: add auto scroll for markdown editor and preview panel.
* Fix: wrong code snippets for plantuml.
* Fix: plantuml preview image missing when any code error occurred.
* Fix: exception when no matching for searching in plantuml code snippets.
* Fix: exception when dragging a folder and drop to itself.
* Fix: file or folder can't be drag&drop to a workspace node.
* Fix: in mindmap, selection is cleared when a topic collapsed by shortcut.
* Fix: the preview panel can't be refreshed in "Preview Only" mode.
* Fix: the plantuml preview page can't switch in "Preview Only" mode.
* Fix: exception when trying to show context menu by shortcut on non-selection in mind map.
* Fix: the replace in mindmap searches within topics attributes, but only replaces the matching in the topics.
* Some minor improvement and code refactor.

### v1.0 beta6
* Add a new preference for Markdown to choose a ttf font file to export to PDF with non-latin characters like Chinease, Japanese and Korean.
* Add new "Key Reference" dialog to show all shortcuts for editors.
* Improved some status message to be more clear.
* Fix: wrong format of HTML file exported from markdown file.
* Fix: code editor doesn't work for files with Windows line breaks.
* Dependencies update and code refactor.

### v1.0 beta5
* Replace "project" with "workspace".
* Minor UI improvement.
* Fix: missing siblings when multi-select topics under same left side topic.
* Fix: 'go to file' doesn't work when there is any workspace is deleted externally.
* Fix: add alert when 'find in files' on a externally deleted workspace.

### v1.0 beta4
* Fixed the saving on note dialog of mindmap only available one time.
* Fixed the issue that Chinese input method doesn't work on Ubuntu based Linux.
* Fixed the wrong shortcut for moving lines in code editor.
* Fill default search keyword when open up search bar with text selected in editor.
* Default directory and file name for saving a plantuml file.
* For "Go to file", add 'up' button to navigate matched list from bottom, optimized the order of result list and add ESC key handling to close the dialog.
* Add "word wrap" context menu for code based editors.
* Add tooltip to some buttons and menu items.
* Some minor bug fixes and improvements.

### v1.0 beta3
* Open a file from search result list will automatically locate matching content.
* Fix: wrong state of menu items for `save`, `undo`, `redo` etc.
* Fix: find in code editor doesn't work when last line is empty.
* dependencies upgrade.

### v1.0 beta2
* Add feature dragging mind map by mouse with key modifier.
* Center the splitter when open a plantuml or markdown file.
* Close text input field when open a context menu on mindmap.
* Optimized memory usage.
* Added more icons to context menus.
* Code refactored.


### v1.0 beta1
* Initial beta release
