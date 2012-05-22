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
package de.codesourcery.jasm16.exceptions;

import java.util.List;

import de.codesourcery.jasm16.compiler.DefaultCompilationOrderProvider.DependencyNode;
import de.codesourcery.jasm16.compiler.ICompilationOrderProvider;
import de.codesourcery.jasm16.compiler.ICompilationUnit;

/**
 * Thrown by {@link ICompilationOrderProvider}s that were unable to determine the compilation order
 * for a given set of {@link ICompilationUnit}s.  
 * 
 * @author tobias.gierke@code-sourcery.de
 * @see ICompilationOrderProvider#determineCompilationOrder(de.codesourcery.jasm16.compiler.ICompiler, List)
 */
public class AmbigousCompilationOrderException extends UnknownCompilationOrderException
{
    private final List<DependencyNode> rootSet;
    
    public AmbigousCompilationOrderException(String message, List<DependencyNode> rootSet) {
        this( message, rootSet , null );
    }
    
    public AmbigousCompilationOrderException(List<DependencyNode> rootSet) {
        this("Unable to determine compilation order ",rootSet);
    }
    
    public AmbigousCompilationOrderException(String message, List<DependencyNode> rootSet,Throwable cause) {
        super( message , cause );
        this.rootSet = rootSet;
    }    
    
    public List<DependencyNode> getDependencyGraph()
    {
        return rootSet;
    }    
}
