# Release Notes

### V1.4.0 Unstable Relase
* Feature: Add new toolbar for markdown editor to quickly insert or format Markdown code, including
	* bold and italic
	* heading from h1 to h6
	* bullet list
	* link URL
	* quote block
	* code block
	* table
* Improvement: 
	* add syntax highlight of emphasis text block, list, numbered list, table for Markdown editor.
	* improved syntax highlighting for PlantUML editor.

### V1.3.5 Release
* Fix: Context menu for file tab doesn't need `Open in System` for mind map, like menu for workspace tree item.
* Fix: Search in editor doesn't work well in some cases.
* Some UI fix and code refactor.

### V1.3.4 Unstable Release
* Improvement: `Find in Files` functionality is improved a lot
	* the search result displays every matched text with context instead of only the first one.
	* highlight all matching text in result items.
	* use graphical line breaker instead in result items for code based file type(*.md, *.puml, *.txt).
	* use graphical connector in result items for Mind Map topics.
	* precisely locating keyword in editor for opening file from search result.
* Fix: exception when search blank keyword in the search result panel 
* update demo workspace.
* Some other refactors.

### V1.3.3 Unstable Release
* New Feature: add context menu for editing to note editor in mind map.
* Fix: exception when dragging a file to the blank space in the workspace tree.
* Fix: hidden left side panel will be forced to show when open a web link from markdown preivew.
* Fix: the workspace list selection works not well when create a workspace in a sub-dir of another workspace, and also the file navigating goes with duplicate files in that situation.
* update flexmark-java to 0.64.6.

### V1.3.2 Unstable Release
* Improvement: new icons for workspace, folder and all kinds of files.
* Improvement: add more icons to editors and some dialogs.
* Improvement: changed some icons for contexts menu and dialogs.
* Fix: menu items to create folder or files doesn't work in some cases. 
* Fix: the relative file link is unable to be located. 
* Fix: paste doesn't work in note editor of mind map.
* update plantuml to 1.2023.8 and other dependencies.

### V1.2.7 Release
* Fix: menu items to create folder or files doesn't work in some cases. 
* Fix: the relative file link is unable to be located in mind map.

### V1.3.1 Unstable Release
* Merged bug fixes from v1.2.5 and v1.2.6

### V1.2.6 Release
* Fix: copy/paste/cut in search bar text field causes same actions in code editor.
* Fix: exception when select text to the end in note editor of mind map.

### V1.2.5 Release
* Fix: paste shortcut has a conflict with code editor, which causes redundant text pasted.

### V1.3.0 Unstable Release
* New Feature: create relative file link by dragging&dropping file to Mind Map, Markdown, CSV or plain text files.
* New Feature: find file links to any file or folder in workspace.
* New Feature: show context text with search word highlighted in search result from "find in files".
* Improvement: set default button to "Cancel" for closing application dialog and deleting file dialog.
* Update mfx to 1.2.

### V1.2.4 Release
* Improvement: add round corner for topic selection as well as topic.
* Improvement: add missing context menu operations(cut, copy, paste, delete) to code editor.
* Improvement: font configurable for CSV editor.
* Fix: exception when moving a file, which is loaded to a tab but not instantiated , to other folder.
* Update JavaFX to 20.0.1.

### V1.1.5 Release
* Fix: files created from external in sub-folders of expanded folder don't be loaded.
* Fix: exception when right-click on a new created file in a new created folder.

### V1.2.3 Unstable Release
* Improvement: add round corner (and settings) to mind map topics.
* Fix: files created from external in sub-folders of expanded folder don't be loaded.
* Fix: exception when right-click on a new created file in a new created folder.
* Fix: disable "paste" menu item for CSV editor if clipboard is empty.

### V1.2.2 Unstable Release
* Feature: add icons to context menu items for csv editor.
* Feature: add cut and paste for selected rows.
* Fix: occasionally can't select rows.
* Refactor select row(s).
* Update PlantUML to 1.2023.6.
* Fixed some minor bugs.

### V1.2.1 Unstable Release
* Feature: unable to locate matched cell correctly in searching.
* Feature: paste csv format text from clipboard to multiple cells as it is.
* Feature: disable cells navigation by keys when editing.
* fix: index column shouldn't be selected by left arrow key.
* merge bug fixes from v1.1.4.

### V1.1.4 Release
* Fix: zooming on image viewer stops when width&height are both smaller than viewport.
* Fix: dragging topic to make link doesn't work since it has a conflict with dragging mindmap operation.
* Fix: cancel unsaved file during closing application doesn't stop that.
* Fix: copy and paste topics that has ancestor-descendant relationship gets redundant topics. https://github.com/mindolph/Mindolph/issues/4

### V1.2.0 Unstable Release
* Feature: add 'Edit' to context menu in csv editor.
* Feature: index column background and border.
* Fix: paste from multiple empty cells turns out comma(s).
* Fix: paste empty content to a cell causes file changed.
* merge bug fixes from v1.1.2 and v1.1.3

### V1.1.3 Release
* Fix: selecting file in workspace expends the folder with same name.
* Fix: "save as" a file to the root of current workspace doesn't show immediately.
* Fix: exception when popup context menu on new created file and pasted file.
* Fix: wrong displaying path in context menu from file in workspace.

### V1.1.2 Release
* Fix: rename an opened file and edit the file, the file name in tab will be recovered to the original. 
* Fix: exception occasionally when locate file in workspace.
* Fix: exception when exporting mindmap to freemind.
* Fix: exception when popup menu on a renamed file.

### V1.2 Beta
* New Feature: add WYSIWYG CSV file editor. 
* Fix: exception when popup menu on a renamed file.
* Update JavaFX to 20, PlantUML to 1.2023.5 and some other dependencies.

### V1.1.1 Release
* Improvement: always open mmd file in Mindolph.
* Improvement: enlarge code editor line spacing to make reading more comfortable.
* Fix: copy relative path missing file name.
* Fix: exception when replacing text without searching first.
* Fix: searching stops when replacing fail.

### V1.1 Stable
* Improvement: better merging for undo/redo history in code editor, it won't redo a lot of inputs now.
* Fix: 'collapse all' does not really collapse all tree nodes for both workspace and folder.
* Fix: exception occurred when mouse pressed after releasing out of mind map view port.
* Fix: the change of font for editors doesn't work unless restarting app.

### V1.1 RC
* Feature: add copy/paste for file in workspace.
* Feature: add copy file path(absolute or relative).
* Fix: auto-scroll for workspace doesn't work well.
* Fix: exception after "find in files" results showed.


### V1.1 Beta
* Re-designed the workspace panel to be more convenient to manage and locate your files.
* Add new workspace management dialog to manage workspaces.
* Redesign UI of mind map preference dialog
* Improvement: optimized the auto scroll for locating file in workspace.
* Improvement: optimized the searching in go to file dialog
* Improvement: recent file list update when files moved to another folder.
* Fix: opened file will be closed when moving in workspace.
* Fix: empty PlantUML file causes exception.
* Some minor UI optimization and fix.
* Fixed some minor bugs.
* Update JavaFX to 17.0.6
* Release bundled installer package for Fedora based distro.

### V1.0 Stable
* Improvement: add shortcut for quick comment for markdown.
* Fix: closed workspace will be restored after restarting.
* Fix: some functionality breaks after closing a workspace.
* Fix: "save as" failed to overwrite existing file if user choose "overwrite".
* Fix: for mindmap, convert a topic which has siblings at the top place will result as new sub-topics created at the end of siblings.
* Fix: the first opened file can't get message shows on status bar.
* Some minor UI/UX optimization, eg: context menu 'find files for text" renamed to 'find in files'.


### V1.0 RC3
* Fix: sub folders don't load during startup in some cases.
* Fix: the mind map doesn't center in some cases.
* Fix: wrong initial editor size for starting to edit mind map topic after dragging another larger topic.
* Fix: "go to file" doesn't trim blank head and tail in keyword.
* Dependencies update: PlantUML 1.2023.1.

### V1.0 RC2
* Improvement: add more emotions.
* Improvement: optimize the performance of opening a file.
* Improvement: consistent extension icons drawing on mind map.
* Improvement: optimize the mind map editor's performance.
* Fix: the order of workspaces on startup does not apply to what it was last time.
* Fix: find/search bar shouldn't available when opening a unsearchable file.
* Fix: undo doesn't work after import to a mind map.
* Fix: project path in recent files view.
* Some dependencies updates and code refactor.

### v1.0 RC1
* Improvement: auto scroll code editor to show the caret when it is out of view-port.
* Improvement: take original file name as default export file name for mind map file. 
* Improvement: add sorting toggle button for result list of "go to file".
* Improvement: add status message for zoom in/out mind map, preview content or image file.
* Improvement: use new icon for search bar, "find in files" dialog and result panel.
* Fix: exception if none is selected from markdown font dialog.
* Fix: code editor scrolls to bottom after file is loaded
* Optimize performance of dragging element in mind map.
* Some code refactor.


### v1.0 beta10
* Improvement: optimize "Go To File" and "Find in Files" by asynchronously searching.
* Improvement: add preference for max size of undo/redo history of code editor.
* Improvement: loading recent files lazily after clicked tab at the first time.
* Fix: no topics focused after deleting one or more topics;
* Fix: empty topic with children will be deleted when hits ESC during editing.
* Performance optimizing and refactor.

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
