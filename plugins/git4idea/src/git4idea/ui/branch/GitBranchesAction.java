/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
package git4idea.ui.branch;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

/**
 * <p>
 *   Invokes a {@link git4idea.ui.branch.GitBranchPopup} to checkout and control Git branches.
 * </p>
 *
 * @author Kirill Likhodedov
 */
public class GitBranchesAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    assert project != null;

    VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    GitRepositoryManager repositoryManager = GitRepositoryManager.getInstance(project);
    GitRepository repository = (file == null ?
                                repositoryManager.getRepositoryForRoot(GitBranchUiUtil.guessGitRoot(project)) :
                                repositoryManager.getRepositoryForFile(file));
    if (repository == null) {
      return;
    }

    GitBranchPopup.getInstance(project, repository).asListPopup().showInBestPositionFor(e.getDataContext());
  }

}
