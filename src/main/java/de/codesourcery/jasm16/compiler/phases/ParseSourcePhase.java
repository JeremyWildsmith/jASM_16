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
package de.codesourcery.jasm16.compiler.phases;

import java.io.IOException;

import org.apache.log4j.Logger;

import de.codesourcery.jasm16.compiler.CompilerPhase;
import de.codesourcery.jasm16.compiler.ICompilationContext;
import de.codesourcery.jasm16.compiler.ICompilationUnit;
import de.codesourcery.jasm16.compiler.ICompiler.CompilerOption;
import de.codesourcery.jasm16.compiler.ICompilerPhase;
import de.codesourcery.jasm16.parser.IParser;
import de.codesourcery.jasm16.parser.IParser.ParserOption;
import de.codesourcery.jasm16.parser.Parser;

/**
 * Compiler phase that transforms the source code into an AST.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class ParseSourcePhase extends CompilerPhase {

    private static final Logger LOG = Logger.getLogger(ParseSourcePhase.class);
    
    public ParseSourcePhase() {
		super(ICompilerPhase.PHASE_PARSE);
	}
    
    @Override
    protected boolean isProcessCompilationUnit(ICompilationUnit unit)
    {
        /*
         * IncludeSourceFileNode may have already parsed a compilation unit
         * from the input set, make sure we don't try again (because this
         * would yield lots of duplicate symbols) 
         */
        return unit.getAST() == null;
    }

	@Override
    protected void run(ICompilationUnit unit , ICompilationContext context) throws IOException
    {
	    LOG.info("run():PARSING: "+unit);
	    
	    final IParser parser = new Parser(context);
	    
	    if ( context.hasCompilerOption( CompilerOption.DEBUG_MODE ) ) {
	        parser.setParserOption(ParserOption.DEBUG_MODE,true);
	    }
	    if ( context.hasCompilerOption( CompilerOption.RELAXED_PARSING ) ) {
	        parser.setParserOption( ParserOption.RELAXED_PARSING , true );
	    }
        unit.setAST( parser.parse( context ) );                
    }
}
