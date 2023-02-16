/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.model;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

public class ExtraFile extends Extra<MMapURI> implements ExtraLinkable {

    private final MMapURI fileUri;
    private final boolean mmdFileFlag;
    private final String lowerCasedFileExtension;
    private volatile String cachedString;

    public ExtraFile(MMapURI file) {
        this.fileUri = file;
        this.lowerCasedFileExtension = file.getExtension().toLowerCase(Locale.ENGLISH);
        this.mmdFileFlag = this.lowerCasedFileExtension.equals("mmd");
    }

    public ExtraFile(String text) throws URISyntaxException {
        this(new MMapURI(text));
    }


    private static String ensureFolderPath(String str) {
        if (str.endsWith("/") || str.endsWith("\\")) {
            return str;
        }
        return str + File.separatorChar;
    }

    @Override
    public boolean containsPattern(File baseFolder, Pattern pattern) {
        String filePathAsText =
                FilenameUtils.normalize(this.fileUri.asFile(baseFolder).getAbsolutePath());
        return pattern.matcher(filePathAsText).find();
    }

    @Override
    public int hashCode() {
        return this.fileUri.hashCode() ^ (this.mmdFileFlag ? 1 : 0);
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }

        if (that instanceof ExtraFile thatFile) {
            return this.mmdFileFlag == thatFile.mmdFileFlag && this.fileUri.equals(thatFile.fileUri);
        }
        else {
            return false;
        }
    }

    public boolean isMMDFile() {
        return this.mmdFileFlag;
    }


    public String getLCFileExtension() {
        return this.lowerCasedFileExtension;
    }

    @Override

    public MMapURI getValue() {
        return fileUri;
    }

    @Override

    public ExtraType getType() {
        return ExtraType.FILE;
    }

    @Override

    public String getAsString() {
        if (this.cachedString == null) {
            this.cachedString = this.fileUri.asFile(null).getPath();
        }
        return this.cachedString;
    }

    @Override

    public String provideAsStringForSave() {
        return this.fileUri.asString(false, true);
    }

    @Override

    public MMapURI getAsURI() {
        return this.fileUri;
    }

    public boolean isAbsolute() {
        return this.fileUri.isAbsolute();
    }


    public ExtraFile replaceParentPath(File baseFolder,
                                       MMapURI oldFolder,
                                       MMapURI newFolder) {
        File theFile = this.fileUri.asFile(baseFolder);
        File oldFolderFile = oldFolder.asFile(baseFolder);
        File newFolderFile = newFolder.asFile(baseFolder);

        String theFilePath = FilenameUtils.normalize(theFile.getAbsolutePath());
        String oldFolderFilePath =
                ensureFolderPath(FilenameUtils.normalize(oldFolderFile.getAbsolutePath()));
        String newFolderFilePath =
                ensureFolderPath(FilenameUtils.normalize(newFolderFile.getAbsolutePath()));

        if (theFilePath.startsWith(oldFolderFilePath)) {
            String changedPath =
                    newFolderFilePath + theFilePath.substring(oldFolderFilePath.length());
            return new ExtraFile(new MMapURI(this.isAbsolute() ? null : baseFolder, new File(changedPath),
                    this.fileUri.getParameters()));
        }
        else {
            return null;
        }
    }

    public boolean hasParent(File baseFolder, MMapURI folder) {
        File theFile = this.fileUri.asFile(baseFolder);
        File thatFile = folder.asFile(baseFolder);

        String theFilePath = FilenameUtils.normalize(theFile.getAbsolutePath());
        String thatFilePath =
                ensureFolderPath(FilenameUtils.normalize(thatFile.getAbsolutePath()));

        if (!theFilePath.equals(thatFilePath) && theFilePath.startsWith(thatFilePath)) {
            String diff = theFilePath.substring(thatFilePath.length() - 1);
            return diff.startsWith("\\") || diff.startsWith("/");
        }
        else {
            return false;
        }
    }

    public boolean isSameOrHasParent(File baseFolder, MMapURI file) {
        File theFile = this.fileUri.asFile(baseFolder);
        File thatFile = file.asFile(baseFolder);

        String theFilePath = FilenameUtils.normalize(theFile.getAbsolutePath());
        String thatFilePath = FilenameUtils.normalize(thatFile.getAbsolutePath());

        if (theFilePath.startsWith(thatFilePath)) {
            String diff = theFilePath.substring(thatFilePath.length());
            return diff.isEmpty() || diff.startsWith("\\") || diff.startsWith("/") ||
                    thatFilePath.endsWith("/") || thatFilePath.endsWith("\\");
        }
        else {
            return false;
        }
    }

    public boolean isSame(File baseFolder, MMapURI file) {
        File theFile = this.fileUri.asFile(baseFolder);
        File thatFile = file.asFile(baseFolder);

        String theFilePath = FilenameUtils.normalize(theFile.getAbsolutePath());
        String thatFilePath = FilenameUtils.normalize(thatFile.getAbsolutePath());

        return theFilePath.equals(thatFilePath);
    }

}
