/*
 * Copyright 2006 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.xpathView.search;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.xml.ide.highlighter.XmlLikeFileType;

public abstract class BaseProcessor implements Processor<VirtualFile> {
    private final ProgressIndicator myIndicator = ProgressManager.getInstance().getProgressIndicator();

    public boolean process(VirtualFile t) {
        myIndicator.checkCanceled();

        final FileType fileType = t.getFileType();
        if (fileType instanceof XmlLikeFileType) {
            processXmlFile(t);
        }
        return true;
    }

    protected abstract void processXmlFile(VirtualFile t);
}
