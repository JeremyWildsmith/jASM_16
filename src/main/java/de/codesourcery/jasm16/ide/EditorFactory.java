/**
 * Copyright 2012 Tobias Gierke <tobias.gierke@code-sourcery.de>
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
package de.codesourcery.jasm16.ide;

import de.codesourcery.jasm16.compiler.io.IResource;
import de.codesourcery.jasm16.compiler.io.IResource.ResourceType;
import de.codesourcery.jasm16.compiler.io.IResourceResolver;
import de.codesourcery.jasm16.ide.ui.viewcontainers.ViewContainerManager;
import de.codesourcery.jasm16.ide.ui.views.IEditorView;
import de.codesourcery.jasm16.ide.ui.views.SourceEditorView;

public class EditorFactory {

	private final ViewContainerManager viewContainerManager; 
	private final IWorkspace workspace;
	
	public EditorFactory(IWorkspace workspace,ViewContainerManager viewContainerManager) {
		this.workspace=workspace;
		this.viewContainerManager= viewContainerManager;
	}
	
	public IEditorView createEditor( IAssemblyProject project, IResource resource, IResourceResolver resolver) 
	{
		return new SourceEditorView(resolver,workspace,viewContainerManager);
	}
}
