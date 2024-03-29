/**
 * Copyright 2006 Sascha Weinreuter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.xpathView.search;

import consulo.application.util.function.Processor;
import consulo.content.ContentIterator;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.scope.PsiSearchScopeUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.OrderEnumerator;
import consulo.project.Project;
import consulo.util.lang.Comparing;
import consulo.util.lang.function.Condition;
import consulo.util.lang.function.Conditions;
import consulo.util.xml.serializer.annotation.Attribute;
import consulo.util.xml.serializer.annotation.Tag;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class SearchScope {
  public enum ScopeType {
    PROJECT,
    MODULE,
    DIRECTORY,
    CUSTOM
  }

  private ScopeType myScopeType;
  private String myModuleName;
  private String myPath;
  private boolean myRecursive;
  private String myScopeName;

  private consulo.content.scope.SearchScope myCustomScope;

  public SearchScope() {
    myScopeType = ScopeType.PROJECT;
    myRecursive = true;
  }

  public SearchScope(ScopeType scopeType, String directoryName, boolean recursive, String moduleName, String scopeName) {
    myScopeType = scopeType;
    myPath = directoryName;
    myRecursive = recursive;
    myModuleName = moduleName;
    myScopeName = scopeName;
  }

  void setCustomScope(consulo.content.scope.SearchScope customScope) {
    myCustomScope = customScope;
  }

  @NotNull
  public String getName() {
    switch (getScopeType()) {
      case PROJECT:
        return "Project";
      case MODULE:
        return "Module '" + getModuleName() + "'";
      case DIRECTORY:
        return "Directory '" + getPath() + "'";
      case CUSTOM:
        return getScopeName();
    }
    assert false;
    return null;
  }

  @NotNull
  @Attribute("type")
  public ScopeType getScopeType() {
    return myScopeType;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setScopeType(ScopeType scopeType) {
    myScopeType = scopeType;
  }

  @Tag
  public String getModuleName() {
    return myModuleName;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setModuleName(String moduleName) {
    myModuleName = moduleName;
  }

  @Nullable
  @Attribute("scope-name")
  public String getScopeName() {
    return myScopeName;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setScopeName(String scopeName) {
    myScopeName = scopeName;
  }

  @Nullable
  @Tag
  public String getPath() {
    return myPath;
  }

  public void setPath(String path) {
    myPath = path;
  }

  @Attribute
  public boolean isRecursive() {
    return myRecursive;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setRecursive(boolean recursive) {
    myRecursive = recursive;
  }

  public boolean isValid() {
    final String dirName = getPath();
    final String moduleName = getModuleName();

    switch (getScopeType()) {
      case MODULE:
        return moduleName != null && !moduleName.isEmpty();
      case DIRECTORY:
        return dirName != null && !dirName.isEmpty() && findFile(dirName) != null;
      case CUSTOM:
        return myCustomScope != null;
      case PROJECT:
        return true;
    }
    return false;
  }

  void iterateContent(@NotNull final Project project, @NotNull Processor<? super VirtualFile> processor) {
    switch (getScopeType()) {
      case PROJECT:
        //noinspection unchecked
        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(new MyFileIterator(processor, Conditions.alwaysTrue()));
        break;
      case MODULE:
        final Module module = ModuleManager.getInstance(project).findModuleByName(getModuleName());
        assert module != null;
        ModuleRootManager.getInstance(module).getFileIndex().iterateContent(new MyFileIterator(processor, Conditions.alwaysTrue()));
        break;
      case DIRECTORY:
        final String dirName = getPath();
        assert dirName != null;

        final VirtualFile virtualFile = findFile(dirName);
        if (virtualFile != null) {
          iterateRecursively(virtualFile, processor, isRecursive());
        }
        break;
      case CUSTOM:
        assert myCustomScope != null;

        final ContentIterator iterator;
        if (myCustomScope instanceof GlobalSearchScope) {
          final GlobalSearchScope searchScope = (GlobalSearchScope)myCustomScope;
          iterator = new MyFileIterator(processor, virtualFile13 -> searchScope.contains(virtualFile13));
          if (searchScope.isSearchInLibraries()) {
            final OrderEnumerator enumerator = OrderEnumerator.orderEntries(project).withoutModuleSourceEntries().withoutDepModules();
            final Collection<VirtualFile> libraryFiles = new HashSet<>();
            Collections.addAll(libraryFiles, enumerator.getClassesRoots());
            Collections.addAll(libraryFiles, enumerator.getSourceRoots());
            final Processor<VirtualFile> adapter = virtualFile1 -> iterator.processFile(virtualFile1);
            for (final VirtualFile file : libraryFiles) {
              iterateRecursively(file, adapter, true);
            }
          }
        }
        else {
          final PsiManager manager = PsiManager.getInstance(project);
          iterator = new MyFileIterator(processor, virtualFile12 ->
          {
            final PsiFile element = manager.findFile(virtualFile12);
            return element != null && PsiSearchScopeUtil.isInScope(myCustomScope, element);
          });
        }

        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(iterator);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SearchScope scope = (SearchScope)o;
    return myRecursive == scope.myRecursive &&
      Comparing.equal(myCustomScope, scope.myCustomScope) &&
      Comparing.equal(myModuleName, scope.myModuleName) &&
      Comparing.equal(myPath, scope.myPath) &&
      Comparing.equal(myScopeName, scope.myScopeName) &&
      myScopeType == scope.myScopeType;
  }

  @Override
  public int hashCode() {
    int result = myScopeType != null ? myScopeType.hashCode() : 0;
    result = 31 * result + (myModuleName != null ? myModuleName.hashCode() : 0);
    result = 31 * result + (myPath != null ? myPath.hashCode() : 0);
    result = 31 * result + (myRecursive ? 1 : 0);
    result = 31 * result + (myScopeName != null ? myScopeName.hashCode() : 0);
    result = 31 * result + (myCustomScope != null ? myCustomScope.hashCode() : 0);
    return result;
  }

  @Nullable
  private static VirtualFile findFile(String dirName) {
    return LocalFileSystem.getInstance().findFileByPath(dirName.replace('\\', '/'));
  }

  private static void iterateRecursively(VirtualFile virtualFile, final Processor<? super VirtualFile> processor, boolean recursive) {
    VirtualFileUtil.visitChildrenRecursively(virtualFile, new VirtualFileVisitor(recursive ? null : VirtualFileVisitor.ONE_LEVEL_DEEP) {
      @Override
      public boolean visitFile(@NotNull VirtualFile file) {
        if (!file.isDirectory()) {
          processor.process(file);
        }
        return true;
      }
    });
  }

  private static class MyFileIterator implements ContentIterator {
    private final Processor<? super VirtualFile> myProcessor;
    private final Condition<? super VirtualFile> myCondition;

    MyFileIterator(Processor<? super VirtualFile> processor, Condition<? super VirtualFile> condition) {
      myCondition = condition;
      myProcessor = processor;
    }

    @Override
    public boolean processFile(@NotNull VirtualFile fileOrDir) {
      if (!fileOrDir.isDirectory() && myCondition.value(fileOrDir)) {
        myProcessor.process(fileOrDir);
      }
      return true;
    }
  }
}