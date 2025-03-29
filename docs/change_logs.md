# Change Logs

### V1.11.5 Unstable Release
* Feature:
    * Support Gen-AI summarizing for mind map.
    * New option `UI icon size` to adjust the size of icons globally with 3 grades: small(16), medium(20), large(24).

* Improvement:
    * Optimized the handling of user-initiated stop during Gen-AI generation.
    * Temporarily disable the editor during Gen-AI generation to avoid unexpected situations.

* Bug Fixes:
    * Error handling doesn't work for streaming DeepSeek provider.
    * Exception when generate or summarize by LLM if `Custom` model option is selected but no any custom model is defined and selected.
    * For setting Gen-AI custom model, the `delete` button should be disabled when there is no any custom model exists.



### V1.11.4 Unstable Release
* Feature: add new option `UI font size` to set the global UI font size. (https://github.com/mindolph/Mindolph/issues/12)   
* Improvement: update OpenAI and Google Gemini models; display model's max tokens as '%sK' format.   
* Improvement: remember the latest user input prompt for initializing the prompt of Gen-AI input panel.  
* Improvement: add notification for creating new collection for opened files.
* Improvement: add the number of files in the notification message when saving a collection.  
* Fix: model choice box should be disabled during Gen-AI generating or summarizing.  
* Fix: exception when launching Gen-AI input panel without selecting any text.  
* Fix: the selected default collection is not recovered the selection state on application startup.  
* Fix: Text selection is cleared after Gen-AI summarize panel pops up.   
* Fix: filtering snippets should be case insensitive.  

### V1.11.3 Unstable Release
* Improvement: for applying snippet, automatically wrap up the selected text if the snippet you choose requires that.
* Improvement: add Regex and EBNF support to PlantUML editor.
* Improvement: add max token information after selecting a LLM model in Gen-AI panels.
* Fix: the 'retry' function doesn't work after the Gen-AI generated something.
* Refactored LLM provider implementation.

### V1.10.5 Release
* Improvement: optimized the auto scroll during editing for the Markdown editor, now the scroll sync goes smoothly. 😊
* The .deb installation package is now built on Debian 12 to fix the error during installation. It also works on Ubuntu.

### V1.11.2 Unstable Release
* Improvement: input panel follows the generated text during streaming generation by Gen-AI.
* Fix: exception when launching Gen-AI generating or summarizing panes if no custom models is set up for the provider.  
* Merged bug fixes from stable version 1.10.4.

### V1.10.4 Release
* Fix: Snippet View did not correctly save and restore the expanded state after switching between different file types.  
* Fix: When switching from another tab to a file tab opened from 'find in files', the search keyword in the file will be automatically selected by mistake.  

### V1.11.1 Unstable Release

* New features for Gen-AI include:  
	* Allow user to setup output language for Gen-AI feature, and also provide an option to make the language of generated content consistent with the language of the text entered by the user.
	* redesign Gen-AI popup panels to let user select model and output language just before starting generating.
* Improvement: optimize the topic tree dialog escaping in mind map editor and the search bar escaping.   
* Improvement: optimized the quitting for Gen-AI summary pane by the ESC key press.   
* Improvement: update Qwen models.  
* Fix: unable to load all Gen-AI models on Gen-AI dialog.  
* Fix: some exception cases from HuggingFace API don't be handled.  
* Fix: changing the custom Gen-AI model doesn't take effect.   
* Fix: minor issues for PlantUML templates.   
* Merged improvements and bug fixes from stable version.  


### V1.10.3 Release
* Improvement: automatically remove bullet prefix (if exists) when using the Header button in the Markdown editor's toolbar.   
* Improvement: optimized the closing by ESC for Gen-AI input dialog  
* Fix: files in collections can't be opened since their names were changed or were moved to another folder.   
* Fix: CSV editor doesn't automatically scroll to selected row by searching.   
* Fix: clicking on web links in markdown preview panel should go to external browser.   
* Update JavaFX to 23.0.2.  


### V1.11.0 Unstable Release
* New Features:
	* Support streaming text generation (Markdown, PlantUML, Text file).
	* New Gen-AI Summarize feature to summarize selected text(Markdown, Text file).
	* Add new support for LLM provider DeepSeek.
* Improvement:
	* Select model on the fly in the dialog of generating from AI.
	* Allow user to define multiple custom LLM models.
	* Add max output tokens parameter to both pre-defined LLM models or custom models to maximize the capabilities of models.
	* Minor UI improvement.
* Update plantuml, commons-csv and other dependencies to the latest version.


### V1.10.2 Release

* Feature: new toolbar button to quickly insert comment for Markdown editor.
* Improvement: ask for new file name before cloning a file.
* Improvement: decrease the size of installation package.
* Fix: no file is created when creating a new file with path separator.
* Fix: in emoticon view, click on the border between 2 icons should not activate both icons.
* update dependencies.


### V1.10.1 Unstable Release

* Feature: New custom snippet feature for Mind Map, PlantUML and Markdown. You can define your own text or image snippets, the image type snippet can be applied to Mind Map as image attribute without file link.
* Improvement: add option `open topic attributes by double clicking` for mind map.
* Fix: option `show collapsator when mouse hover` for mind map theme.
* merged bug fixes from stable version 1.9.5.
* update dependencies


### V1.9.5 Release

* Fix: exception when undo editing from an empty row in CSV editor.
* Fix: clicking on web links in markdown preview panel should go to external browser.
* Fix: no default outline text for empty topic in mind map.


### V1.10.0 Unstable Release

* Feature: new snippet panel for all files; move PlantUML snippets to the global snippet panel.
* Feature: add snippet support for mind map by emoticons.
* Improvement: refactored the emoticon dialog with new version emoticon view.
* Fix: locating folder from `go to file` dialog doesn't work if `auto select` option is disabled by user.


### V1.9.4 Release

* Improvement: `remove collection` menu item should be disabled for `default` collection.
* Fix: locating folder from `go to file` dialog doesn't work if `auto select` option is disabled by user.
* update JavaFX to 23.0.1 and other dependencies.


### V1.9.3 Unstable Release

* Improvement: add image quick insert button to the toolbar to Markdown code editor.  
* Improvement: optimize the outline view to do rendering only when it's active.   
* update bundled JRE to 22.0.2  


### V1.9.2 Unstable Release

* Improvement: add toast notification for saving and deleting collection.
* Improvement: resort collections when new one is created.
* Improvement: center the caret in the view-port after locating to any paragraph in code editor.
* Fix: outline view doesn't cleared when all files are closed.
* update JavaFX to 22.0.2 and update other dependencies.
* merged bug fixes from v1.8.6.

### V1.8.6 Release

* Fix: blank chars are not in correct font before code block in Markdown.

### V1.8.5 Release

* Fix: blank chars are not in correct font before comment line/block in PlantUML.
* Fix: unable to open file link with CJK characters.
* Fix: wrong redundant highlighting of '#' in non-header text in Markdown file.

### V1.9.1 Unstable Release

* Feature: new outline view to outline the content for Mind map, Markdown and PlantUML files. 
* Improvement: minimize the displaying of tabs on the left side panel since the outline view is introduced.

### V1.9.0 Unstable Release

* Feature: new file collections functionality that lets you save opened files as a named collection and switch between different collections.

### V1.8.4 Release

* Fix: exceptions when no search keyword is provided in search&replace panel.
* merged bug fixes from v1.7.9

### V1.7.9 Release

* Fix: exception when import mind map from Freemind mm file.(https://github.com/mindolph/Mindolph/issues/9)


### V1.8.3 Unstable Release

* Improvement: optimize the handling of dragging mouse hover on folder. 
* Improvement: auto select moved files either by drag&drop or by `Move to`.
* Fix: inappropriate view mode in keys reference dialog.
* Fix: performance improved by removing redundant style applying for editors.
* Fix: selected file disappear in workspace tree after failed to drag&drop to another folder with same name file exists.
* Fix: unable to use `Move to` to move files to root folder of a workspace. 

### V1.8.2 Unstable Release

* Fix: the order of search results list changes randomly.  
* Fix: duplicate search result items since the breadcrumb was introduced.  
* Fix: unexpected refresh in active editor when shortcuts key reference dialog popup.
* Merge bug fixes from stable versions.  

### V1.7.8 Release

* Fix: exception when locating root topic of mind map from search result.   
* Fix: when locating a mind map topic from the search results, the selected topics are not cleared.  
* Fix: breaking line and moving caret up/down does not work in some cases after IME dialog popup and hide.  

### V1.7.7 Release

* Improvement: optimize the searching reach the beginning or the end in Markdown, PlantUML and plain text editors.    
* Improvement: optimize gen-ai dialog UI.  
* Fix: no response when change current workspace to a no longer existed one.  
* Fix: markdown preview panel is not updated when the view mode is changed from `text only` to `both`.  
* Fix: collapsed topics in mind map can't be located and focused when found by searching.  
* Fix: the custom LLM model input should be disabled when select any pre-set model from the model list.  

### V1.8.1 Unstable Release

* Improvement: optimize preference dialog and markdown preference layout  
* Improvement: text in markdown editor and plantuml editor can be set different font between descriptive content and code.  
* Improvement: add option `include attributes` for exporting mind map (branches) to literal format files.  
* Update dependencies  

### V1.8.0 Unstable Release

* New Features:
	* allows exporting selected topics as a new file as Mind Map, Markdown, AsciiDoc or structured plain text.  
	* copy&paste image from clipboard to selected topics directly by shortcut.  
	* Allow dragging external files to any folder in workspace to copy those files.  
	* add new preference for automatically selecting in workspace after a file is opened.  
	* add `Move to` functionality to workspace view. 
* Improvement:
	* Remembers the last active tab and switches back to it when opening the preferences dialog (until the application is killed).
	* optimized the PDF exported for markdown files;
	* support the CJK characters in code blocks within exported PDF file from markdown.
		> Need to setup the CJK font files for either sans-serif or mono.
	* allow to select any parent folder in `Find in files` and it's result panel.
	* add new `folder` button to filter folders in `Go to file` dialog
	* Improved the user interface for file type options in `Go to file` and `Search in file` dialogs using icon buttons.
	* add blank tail to markdown bullet list.
	* automatically expands node in workspace when dragging any file/folder over it.
	* improved app menu and context menu displaying.
	* optimized button text in confirm dialogs.
	* optimized the scroll sync for markdown editor.
* Bug Fixes:
	* in Mind Map editor, moving any selected topics by shortcut out of viewport doesn't trigger automatically scrolling.
	* Emphasis text doesn't work in markdown preview.
	* external folder should not be able to drag&drop to workspace.
	* blank text can't be replaced by search&replace in code area.
* Others
	* A few of refactoring especially for Mind Map.  

### V1.7.6 Release
* Improvement: add 'use proxy' option for different LLM providers to enable proxy separately.  
* Improvement: add gpt-4o to open-ai provider.  
* Improvement: more pre-set LLM models.  
* Improvement: optimize the HTTP connecting to LLM providers.
* Fix: the gen-ai reframe panel shows out of the viewport if generated topics make the selected topic out of the viewport.   
* Fix: exception when switch proxy type in preference dialog.  
* Fix: the background of mind map doesn't always render correctly when maximize&recover the editor by double-clicking tab of opened file.   
* Fix: in Mind Map, the protected topic note can't be opened by password.   
* Fix: in Mind Map, after canceling the setting password dialog for topic note, a password reset dialog appears.  


### V1.7.5 Unstable Release
* Improvement: add support for ChatGLM to GenAI.
* Improvement: add pre-set model name list for some LLM providers.
* Fix: blank text can't be replaced by search&replace in code area.
* Fix: "select in workspace" does not clear the previous selection before file is located.
* Fix: maven build profile for aarch64.
* update mfx to 1.3.0


### V1.7.4 Unstable Release
* Improvement: support Google Gemini API and Hugging Face API for Gen-AI.
* Improvement: err message displaying for generating content by AI.
* Improvement: preference migration implementation.
* Improvement: limit the width of error message area in Gen-AI panel.
* Improvement: add hint text to input area of Gen-AI panel.
* Fix: missing sub-folders when loading folder in workspace since the commons-io update to version 2.16.1
* Fix: after a new file/folder created under a folder, the folder itself should not be selected automatically.
* some utils classes refactored.
* update dependencies.


### V1.7.3 Unstable Release
* Feature: support Ollama for Gen-AI.
* Improvement: support multi selection operations for nodes in  workspace tree view.
* Merged bug fixes from v1.6.12
* update langchain4j to 0.29.1


### V1.6.12 Release
* Fix: redundant line break at the end of copied text of topics from mind map.
* Fix: the CRLF in editor is not handled well on macOS with M chip.


### V1.7.2 Unstable Release
* Feature: add Ali Qwen as new LLM provider.
* Improvement: optimized AI generated content.
* Fix: buttons are not restored after exceptions in Gen-AI dialog.
* Fix: should show warning dialog instead of exception dialog when try to generate content without Gen-AI provider set up.
* Update langchain4j to 0.28 and other dependencies.


### V1.7.1 Unstable Release
* Improvement: new individual preference tab page for Gen-AI settings.
* Merged bug fixes from v1.6.11
* Update PlantUML to 1.2024.3, langchain4j to 0.27.1 etc.


### V1.6.11 Release
* New installer for macOS with Apple Silicon.
* Fix: unable to quit input helper by `ESC` in code editor when the suggestion list get focused.


### V1.7.0 Unstable Release
* Feature: new experimental support for Gen-AI, you can now generate content by OpenAI API in mind map, Markdown editor, PlantUML editor.
* Feature: proxy support for internet access.


### V1.6.10 Release
* Fix: suggestion list in code editor doesn't disappear in some cases.
* Fix: moving caret in wrapped text doesn't work well since the input helper is introduced.


### V1.6.9 Unstable Release
* Improvement: optimized suggestion list of input helper by removing special characters.
* Improvement: add separator to suggestion list for words from different plugins.
* Fix: the dialog for creating file link after importing image from file to mind map should be the yes/no buttons mode instead of ok/cancel.
* Fix: exception when save a file as a new file with the same path.
* Fix: missing .jpeg file type support.
* Fix: exception when copy&paste some specific indent text to a mind map topic.
* Fix: selection handling for root node is incorrect after undo in mind map.
* Fix: relocating suggestion list to inappropriate point if it exceeds parent pane.
* update JavaFX to 21.0.2, bundled Java to 21.0.2.


### V1.6.8 Unstable Release
* Improvement: take light theme as default selected theme after deleting a customized theme.
* Improvement: enlarge canvas margin of mind map
* Fix: text area in exception dialog has no scroll-bar even the content exceeds.
* merge improvement and bug fixes from stable.

### V1.5.9 Release
* Improvement: make the divergent icons same between context menu and mind map topic extra for 'Jump' link.
* Fix: Unable to delete a folder with only file `.DS_Store` on macOS.
* Fix: exception when right-click on selected root node and one of its child node in mind map.


### V1.6.7 Unstable Release
* Fix: remove build option `jdk.gtk.version` since the JavaFX 21 does no longer support GTK 2.
* Fix: suggestion list of input helper is not in the appropriate position when there are too many items in it.
* Fix: escape from suggestion mode by `ESC` key does not work on Linux.
* Fix: exception when close all tabs with markdown or plantuml file opened but never loaded.

### V1.6.6 Unstable Release
* Improvement: better text selection after quick inserting from toolbar or shortcuts in code editor.
* Fix: exception when insert some text from toolbar or shortcuts to the end of file.
* Fix: exception when close all tabs with any markdown or plantuml file opened but never loaded.
* Fix: exception when click on error of status bar when a syntax error exists for plantuml.
* Fix: wrong behavior when quick inserting markdown code quote while no text selection in code editor.
* Fix: limit the input helper suggestions to at least 3 letters.


### V1.6.5 Unstable Release
* Improvement: optimize quick insert from markdown toolbar.
* Improvement: optimize the displaying of the attributes icons with font icons directly in mind map.
* Fix: exception when moving caret to the top or bottom in editor.
* Fix: `Null` is inserted from markdown toolbar `Link` button in some cases.
* Bundled JRE upgrade to 21.0.1

### V1.5.8 Release
* Improvement: optimize the displaying of the attributes icons with font icons directly in mind map.
* Improvement: use new icon for 'collapse folder'

### V1.6.4 Unstable Release
* Improvement: refactor input helper and optimize the selection from suggestions list.
* Improvement: use new icon for 'collapse folder'.

### V1.6.3 Unstable Release
* Merged bug fixes from v1.5.7

### V1.5.7 Release
* Improvement: optimized recent list view a little bit.
* Fix: exporting markdown to PDF doesn't work.

### V1.6.2 Unstable Release
* Feature: support markdown in note editor for mind map.
* Improvement: the performance of pasting long text to code editor.
* Improvement: add horizontal rule button to markdown toolbar for quick inserting.
* Improvement: use clearable text field for keyword input.
* Improvement: show image resolution in image viewer .
* Improvement: add `Open` menu item to workspace context menu.
* Improvement: add context menu for recent files list view.
* Fix: pressing backspace to delete letters, the input helper should not work when only 1 letter left.
* Update javafx to 21 and other dependencies includes plantuml, slf4j etc.
* Merged bug fixes from v1.5.6.

### V1.5.6 Release
* Improvement: for mind map, use selected text in input box instead of selected topic as default search keyword.
* Fix: file extension be added to the Markdown doc title, which is not appropriate and makes an unnecessary file link to itself.
* Fix: the new created file name extension is missing if inputted name contains dot(.)

### V1.6.1 Unstable Release
* Feature: add new preference option to toggle the input helper for code editor.
* Improvement: make input helper more friendly for programing language code.
* Improvement: add default note to new files.
* Improvement: displaying performance of input helper is slow in a large file.
* Fix: underline between words doesn't display in input helper.
* Fix: letters don't display when using system input method.
* Fix: input help doesn't work while caret is not at the end of line.
* Fix: all candidate words displays in some cases.
* Merged bug fixes from v1.5.5

### V1.5.5 Release
* Fix: the new created folder shouldn't appear in the `recent files` view.
* Fix: the toolbar buttons inserts redundant symbols to the head of empty lines while multi-line selections.
* Fix: mouse hover detection doesn't work well for the last bottom node in the mind map tree.
* Bundled JRE upgrade to 20.0.2

### V1.6.0 Unstable Release
* Feature: new input helper to code editor, it collects words(for English only) from the content in editor as candidates to accelerate your input.
* Feature: add PlantUML syntax keywords to input helper.
* Merged bug fixes from v1.5.4

### V1.5.4 Release
* Improvement: minor improvement of mind map themes.
* Improvement: optimize the logger performance.
* Fix: the reset default preferences doesn't work for theme options.
* Fix: the "show collapsator on mouse over" option is always disabled.
* Fix: the installer is unable to launch on macOS 14 Sonoma.

### V1.5.3 Unstable Release
* Improvement: automatically select the root topic when opening the mind map.
* Improvement: optimize the performance of text drawing in mind map.
* Improvement: optimize the image preview dialog for mind map.
* Fix: the fonts setting doesn't work since the themes was introduced to mind map.

### V1.5.2 Unstable Release
* Improvement: replace theme operation buttons with context menu in preference dialog.
* Improvement: better dark theme of mind map.
* Improvement: add spacing between topic extra icons in mind map.
* merged bug fixes from v1.4.5

### V1.4.5 Release
* Fix: workspace view doesn't be cleared after the last workspace is closed.
* Fix: exception when click on buttons if there is no workspace created or loaded.
* Fix: exception when double-click on blank item in the 'go to file' file list.

### V1.5.1 Unstable Release
* Feature: add theme support to mind map editor.
* Feature: add new Light and Dark themes to mind map, and provide the ability to duplicate them and make your own theme.
* Feature: add Bezier style for topics connector line in mind map and can be chosen in preference dialog.
* Feature: add migration process for data that needs to be fixed when version update.
* Improvement: let the color dialog in mind map be affected by the theme's border type.
* Improvement: support converting multi selected topics to the note of their mutual parent.
* Improvement: better dragging selection area color for mind map.
* Improvement: add new preference option for creating default comment to root node of mind map.
* Improvement: add status bar to image preview dialog for mind map and optimize the UI.
* update plantuml and richtextfx to latest version.
* merged bug fixes from v1.4.4

### V1.4.4 Release
* Improvement: better start position of jump link in mind map.
* Improvement: optimize UI of color dialog for mind map.
* Improvement: show jump link immediately after adding it.
* Fix: dialog is not in center of screen when exporting mind map to image.

### V1.5.0 Unstable Release
* New Feature: add preview and resizing dialog for inserting image to mind map topic from clipboard or disk.
* Improvement: optimized quality of embedded image in mind map topic.
* Merged bug fixes from v1.4.3.

### V1.4.3 Release
* Fix: multi selected topics are not in their original order in some cases.
* Fix: exception when opening an external file link.

### V1.4.2 Unstable Release
* Improvement: auto scroll when dragging mind map topics to any border of viewport.
* Improvement: move caret to input position after insert markdown code spinnet from toolbar.
* Fix: lost focus after insertion to markdown editor.
* update javafx to 20.0.2, plantuml to 1.2023.10, flexmark to 0.64.8

### V1.4.1 Unstalbe Release
* Improvement: run `check for update` in background.
* Improvement: show toast when not updates for `check for updates`.
* Fix: handshake exception for `check for updates`.
* Merge fixes from v1.3.6

### V1.3.6 Release
* Improvement: better undo/redo handling for the note editor of mind map topic.
* Fix: undo/redo doesn't work well in code editor.

### V1.4.0 Unstable Release
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
* Fix: exception when search blank keyword in the search result panel.
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
