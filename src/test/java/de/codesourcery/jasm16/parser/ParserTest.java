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
package de.codesourcery.jasm16.parser;

import de.codesourcery.jasm16.AddressingMode;
import de.codesourcery.jasm16.ast.AST;
import de.codesourcery.jasm16.ast.ASTNode;
import de.codesourcery.jasm16.ast.ASTUtils;
import de.codesourcery.jasm16.ast.CommentNode;
import de.codesourcery.jasm16.ast.InitializedMemoryNode;
import de.codesourcery.jasm16.ast.InstructionNode;
import de.codesourcery.jasm16.ast.InvokeMacroNode;
import de.codesourcery.jasm16.ast.LabelNode;
import de.codesourcery.jasm16.ast.OperandNode;
import de.codesourcery.jasm16.ast.StartMacroNode;
import de.codesourcery.jasm16.ast.StatementNode;
import de.codesourcery.jasm16.ast.UninitializedMemoryNode;
import de.codesourcery.jasm16.ast.UnparsedContentNode;
import de.codesourcery.jasm16.compiler.CompilationUnit;
import de.codesourcery.jasm16.compiler.ICompilationUnit;
import de.codesourcery.jasm16.exceptions.ParseException;
import de.codesourcery.jasm16.utils.Misc;

public class ParserTest extends TestHelper {

	public void testParseEmptyFile() 
	{
		final Parser p = new Parser(this);
		
		AST ast = p.parse( "" );
		assertFalse( ast.hasErrors() );
		assertFalse( ast.hasChildren() );
	}
	
	public void testParseBlankFile() 
	{
		final Parser p = new Parser(this);
		
		final String source = "    ";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseLineWithInstructionAndLabel() {
		
		final String source = "        :loop         SET [0x2000], [A]      ; 2161 2000\n";
		
		final Parser p = new Parser(this);
		final ICompilationUnit unit = CompilationUnit.createInstance("dummy" , source );
		
		AST ast = p.parse( unit , symbolTable , source , RESOURCE_RESOLVER, null );
		
		Misc.printCompilationErrors( unit , source , true );
		
		assertFalse( ast.hasErrors() );
		assertFalse( unit.hasErrors() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( "        :loop         SET [0x2000], [A]      ; 2161 2000\n" , toSourceCode( root , source ) );		
	}
	
	public void testParseCommentLine() 
	{
		final Parser p = new Parser(this);
		
		final String source = "        ; Try some basic stuff\n";
		final ICompilationUnit unit = CompilationUnit.createInstance("dummy" , source );
		
		AST ast = p.parse( unit , symbolTable , source , RESOURCE_RESOLVER, null );
		
		Misc.printCompilationErrors( unit , source , true );
		
		assertFalse( ast.hasErrors() );
		assertFalse( unit.hasErrors() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( "        ; Try some basic stuff\n" , toSourceCode( root , source ) );
	}	
	
	public void testParseInitializedMemoryWithOneByte() 
	{
		final Parser p = new Parser(this);
		
		final String source = ".dat 1";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( InitializedMemoryNode.class , root.child(0).getClass() );
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseInitializedMemoryWithBinaryValues() throws ParseException 
	{
		final Parser p = new Parser(this);
		
		String source = ".dat b1,b10";
		
        ICompilationUnit unit = CompilationUnit.createInstance("string",source);
        
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( InitializedMemoryNode.class , root.child(0).getClass() );
		final InitializedMemoryNode node = (InitializedMemoryNode) root.child(0);
		resolveSymbols( unit , node );
		final byte[] data = node.getBytes();
		assertEquals( 4 , data.length );
		assertEquals( 0 , data[0] );
		assertEquals( 1 , data[1] );
		assertEquals( 0 , data[2] );
		assertEquals( 2 , data[3] );		
		
		assertEquals( source , toSourceCode( root , source ) );		
	}	
	
	public void testParseInitializedMemoryWithInvalidNumberLiteral() throws Exception 
	{
		ICompilationUnit unit = compile( ".dat 65536" );
		assertTrue( unit.hasErrors() );
	}	
	
	public void testParseInitializedMemoryWithInvalidNumberLiteral2() throws Exception 
	{
		ICompilationUnit unit = compile( ".dat 0xfffff" );
		assertTrue( unit.hasErrors() );
	}	
	
	public void testParseInitializedMemoryWithOneCharacterCharacterLiteral() throws ParseException 
	{
		final Parser p = new Parser(this);
		
		String source = ".dat \"a\"";
		
        ICompilationUnit unit = CompilationUnit.createInstance("string",source);
        
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( InitializedMemoryNode.class , root.child(0).getClass() );
		assertEquals( source , toSourceCode( root , source ) );
		
		final InitializedMemoryNode node = (InitializedMemoryNode) root.child(0);
	    resolveSymbols( unit , node );
		final byte[] data = node.getBytes();
		assertEquals( 2 , data.length );
		assertEquals( 0 , data[0] );
		assertEquals( 97 , data[1] );
	}	
	
	public void testParseInitializedMemoryWithMixedQuotingCharacterLiteral() throws ParseException 
	{
		final Parser p = new Parser(this);
		
        final String source = ".dat \"I'm great\"";
		final ICompilationUnit unit = CompilationUnit.createInstance("string input" , source );
		AST ast = p.parse(  unit , symbolTable , source , RESOURCE_RESOLVER, null );

	    Misc.printCompilationErrors( unit , source , true );
	      
		assertFalse( ast.hasErrors() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( InitializedMemoryNode.class , root.child(0).getClass() );
		
		final InitializedMemoryNode node = (InitializedMemoryNode) root.child(0);
        resolveSymbols( unit , node );		
		final byte[] data = node.getBytes();
		// a = 97
		// b = 98
		// c = 99
		// d = 100
		// e = 101
		// f = 102
	    assertEquals( 0 , data[0] );
		assertEquals( 73 , data[1] );
		
        assertEquals( 0 , data[2] );		
		assertEquals( 39 , data[3] );
		
        assertEquals( 0 , data[4] );
        assertEquals( 109 , data[5] );		
        
        assertEquals( 0 , data[6] );
        assertEquals( 32 , data[7] );
        
        assertEquals( 0 , data[8] );
        assertEquals( 103 , data[9] );  
        
        assertEquals( 0 , data[10] );
        assertEquals( 114 , data[11] );         
        
        assertEquals( 0 , data[12] );
        assertEquals( 101 , data[13] );  
        
        assertEquals( 0 , data[14] );
        assertEquals( 97 , data[15] );           
        
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseInitializedMemoryWithCharacterLiteral() throws ParseException 
	{
		final Parser p = new Parser(this);
		
        final String source = ".dat \"dead\"";
		final ICompilationUnit unit = CompilationUnit.createInstance("string input" , source );
		AST ast = p.parse(  unit , symbolTable , source , RESOURCE_RESOLVER , null );

	    Misc.printCompilationErrors( unit , source , true );
	      
		assertFalse( ast.hasErrors() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( InitializedMemoryNode.class , root.child(0).getClass() );
		
		final InitializedMemoryNode node = (InitializedMemoryNode) root.child(0);
        resolveSymbols( unit , node );		
		final byte[] data = node.getBytes();
		// a = 97
		// b = 98
		// c = 99
		// d = 100
		// e = 101
		// f = 102
	    assertEquals( 0 , data[0] );
		assertEquals( 100 , data[1] );
		
        assertEquals( 0 , data[2] );		
		assertEquals( 101 , data[3] );
		
        assertEquals( 0 , data[4] );
        assertEquals( 97 , data[5] );		
        
        assertEquals( 0 , data[6] );
        assertEquals( 100 , data[7] );
        
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseInitializedMemoryWithTwoBytes() 
	{
		final Parser p = new Parser(this);
		
		String source = ".dat 1,2";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( InitializedMemoryNode.class , root.child(0).getClass() );
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseInitializedMemoryWithTwoBytesAndWhitespace() 
	{
		final Parser p = new Parser(this);
		
		String source = "  .dat 1  , 2  ";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( InitializedMemoryNode.class , root.child(0).getClass() );
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseMacroWithEmptyBody() 
	{
		final Parser p = new Parser(this);
		
		final String macroBody="";
		String source = ".macro brk\n"+
				        ".endmacro";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		assertEquals(2 , ast.getChildCount() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( StartMacroNode.class , root.child(0).getClass() );
		StartMacroNode n = (StartMacroNode) root.child(0);
		assertEquals("brk" , n.getMacroName().getRawValue() );
		assertTrue( n.getArgumentNames().isEmpty() );
		assertEquals( macroBody , n.getMacroBody() );
		assertEquals( source , toSourceCode( ast , source ) );		
	}	
	
	public void testParseMacroWithoutArguments() 
	{
		final Parser p = new Parser(this);
		
		final String macroBody=":loop SET PC , loop";
		final String source = ".macro brk\n"+
		                macroBody+"\n"+
				        ".endmacro";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		assertEquals(2 , ast.getChildCount() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( StartMacroNode.class , root.child(0).getClass() );
		StartMacroNode n = (StartMacroNode) root.child(0);
		assertEquals("brk" , n.getMacroName().getRawValue() );
		assertTrue( n.getArgumentNames().isEmpty() );
		assertEquals( macroBody , n.getMacroBody() );
		assertEquals( source , toSourceCode( ast , source ) );		
		
		ASTUtils.debugPrintTextRegions(ast,source);
	}
	
	public void testParseMacroInvocationWithoutArguments() throws ParseException 
	{
		final Parser p = new Parser(this);
		
		final String macroBody=":loop SET PC , loop";
		String source = ".macro brk\n"+
		                macroBody+"\n"+
				        ".endmacro\n"+
		                "brk\n";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		assertEquals(3 , ast.getChildCount() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( StartMacroNode.class , root.child(0).getClass() );
		StartMacroNode n = (StartMacroNode) root.child(0);
		assertEquals("brk" , n.getMacroName().getRawValue() );
		assertTrue( n.getArgumentNames().isEmpty() );
		assertEquals( macroBody , n.getMacroBody() );
		assertEquals( source , toSourceCode( ast , source ) );
		
		assertTrue( "Expected MacroInvocationNode but got "+ast.child(2).child(0).getClass() , ast.child(2).child(0) instanceof InvokeMacroNode );
		
		final InvokeMacroNode invocation = (InvokeMacroNode) ast.child(2).child(0);
		assertEquals( new Identifier("brk") , invocation.getMacroName() );
		assertEquals( 0 , invocation.getArgumentCount() );
	}	
	
	public void testParseMacroInvocationWithOneArgument() throws ParseException 
	{
		final Parser p = new Parser(this);
		
		final String macroBody="ADD A,arg1";
		String source = ".macro inc(arg1)\n"+
		                macroBody+"\n"+
				        ".endmacro\n"+
		                "inc (1)\n";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		assertEquals(3 , ast.getChildCount() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( StartMacroNode.class , root.child(0).getClass() );
		StartMacroNode n = (StartMacroNode) root.child(0);
		assertEquals("inc" , n.getMacroName().getRawValue() );
		assertEquals( 1, n.getArgumentNames().size() );
		assertEquals( new Identifier("arg1") , n.getArgumentNames().get(0) );
		assertEquals( macroBody , n.getMacroBody() );
		assertEquals( source , toSourceCode( ast , source ) );		
		
		assertTrue( "Expected MacroInvocationNode but got "+ast.child(2).child(0).getClass() , ast.child(2).child(0) instanceof InvokeMacroNode );
		final InvokeMacroNode invocation = (InvokeMacroNode) ast.child(2).child(0);		
		assertEquals( new Identifier("inc") , invocation.getMacroName() );
		assertEquals( 1 , invocation.getArgumentCount() );
	}		
	
	public void testParseMacroWithOneArgument() throws ParseException 
	{
		final Parser p = new Parser(this);
		
		final String macroBody="ADD arg , 1";
		String source = ".macro inc(arg)\n"+
		                macroBody+"\n"+
				        ".endmacro";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		assertEquals( 2 , ast.getChildCount() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );

		assertEquals( StartMacroNode.class , root.child(0).getClass() );
		StartMacroNode n = (StartMacroNode) root.child(0);
		assertEquals("inc" , n.getMacroName().getRawValue() );
		assertEquals( 1 , n.getArgumentNames().size() );
		assertEquals( new Identifier("arg"), n.getArgumentNames().get(0) );
		assertEquals( macroBody , n.getMacroBody() );
		assertEquals( source , toSourceCode( ast , source ) );		
	}
	
	public void testParseMacroWithTwoArguments() throws ParseException 
	{
		final Parser p = new Parser(this);
		
		final String macroBody="ADD arg , 1";
		String source = ".macro inc(arg1,arg2)\n"+
		                macroBody+"\n"+
				        ".endmacro";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		assertEquals( 2 , ast.getChildCount() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( StartMacroNode.class , root.child(0).getClass() );
		StartMacroNode n = (StartMacroNode) root.child(0);
		assertEquals("inc" , n.getMacroName().getRawValue() );
		assertEquals( 2 , n.getArgumentNames().size() );
		assertEquals( new Identifier("arg1"), n.getArgumentNames().get(0) );
		assertEquals( new Identifier("arg2"), n.getArgumentNames().get(1) );
		assertEquals( macroBody , n.getMacroBody() );
		assertEquals( source , toSourceCode( ast , source ) );		
	}		
	
	public void testParseMacroWithTwoArgumentsAndWhitespae() throws ParseException 
	{
		final Parser p = new Parser(this);
		
		final String macroBody="ADD arg , 1";
		String source = ".macro inc ( arg1 , arg2 ) \n"+
		                macroBody+"\n"+
				        ".endmacro";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		assertEquals( 2 , ast.getChildCount() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( StartMacroNode.class , root.child(0).getClass() );
		StartMacroNode n = (StartMacroNode) root.child(0);
		assertEquals("inc" , n.getMacroName().getRawValue() );
		assertEquals( 2 , n.getArgumentNames().size() );
		assertEquals( new Identifier("arg1"), n.getArgumentNames().get(0) );
		assertEquals( new Identifier("arg2"), n.getArgumentNames().get(1) );
		assertEquals( macroBody , n.getMacroBody() );
		assertEquals( source , toSourceCode( ast , source ) );		
	}
	
	public void testParseMacroInvocationWithTwoArguments() throws ParseException 
	{
		final Parser p = new Parser(this);
		
		final String macroBody="ADD arg1,arg2";
		String source = ".macro inc(arg1,arg2)\n"+
		                macroBody+"\n"+
				        ".endmacro\n"+
		                "inc (A,1)\n";
		
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		assertEquals(3 , ast.getChildCount() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		assertEquals( StartMacroNode.class , root.child(0).getClass() );
		StartMacroNode n = (StartMacroNode) root.child(0);
		assertEquals( 2, n.getArgumentNames().size() );
		assertEquals( new Identifier("arg1") , n.getArgumentNames().get(0) );
		assertEquals( macroBody , n.getMacroBody() );
		assertEquals( source , toSourceCode( ast , source ) );		
		
		assertTrue( "Expected MacroInvocationNode but got "+ast.child(2).child(0).getClass() , ast.child(2).child(0) instanceof InvokeMacroNode );
		final InvokeMacroNode invocation = (InvokeMacroNode) ast.child(2).child(0);		
		assertEquals( new Identifier("inc") , invocation.getMacroName() );
		assertEquals( 2 , invocation.getArgumentCount() );
	}		
	
	public void testParseUninitializedMemory() 
	{
		final Parser p = new Parser(this);
		
		String source = ".bss 100";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		
		assertTrue( root.child(0) instanceof UninitializedMemoryNode);
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseUninitializedMemoryWithLabel() 
	{
		final Parser p = new Parser(this);
		
		String source = ":label .bss 100";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 2 , root.getChildCount() );
		
		assertEquals( LabelNode.class , root.child(0).getClass() );
		assertEquals( UninitializedMemoryNode.class , root.child(1).getClass() );	
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseUninitializedMemoryWithLabelAndComment() 
	{
		final Parser p = new Parser(this);
		
		String source = ":label .bss 100 ; do weird stuff   ";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 3 , root.getChildCount() );
		
		assertEquals( LabelNode.class , root.child(0).getClass() );
		assertEquals( UninitializedMemoryNode.class , root.child(1).getClass() );	
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseLineWithLabel() 
	{
		final Parser p = new Parser(this);
		
		String source = ":label";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		assertEquals( 1 , root.getChildCount() );
		
		assertTrue( root.child(0) instanceof LabelNode );
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseLineWithWhitespaceBeforeLabel() 
	{
		final Parser p = new Parser(this);
		
		String source = "   :label";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		
		assertTrue( root.child(0) instanceof LabelNode );
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseLineWithWhitespaceAfterLabel() 
	{
		final Parser p = new Parser(this);
		
		String source = ":label   ";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		
		assertTrue( root.child(0) instanceof LabelNode );		
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseLineWithCommentAfterLabel() 
	{
		final Parser p = new Parser(this);
		
		String source = ":label;comment";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		
		assertTrue( root.child(0) instanceof LabelNode );		
		assertTrue( root.child(1) instanceof CommentNode );		
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseLineWithComment() 
	{
		final Parser p = new Parser(this);
		
		String source = ";comment";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		
		assertTrue( root.child(0) instanceof CommentNode );		
		assertEquals( source , toSourceCode( root , source ) );
	}
	
	public void testParseLineWithOpCode1() 
	{
		final Parser p = new Parser(this);
		
		String source = "SET a,10";
        AST ast = p.parse( source );
		assertFalse( ast.hasErrors() );
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		
		assertEquals( InstructionNode.class , root.child(0).getClass() );
		
		final InstructionNode instruction = (InstructionNode) root.child(0);

		assertEquals( OperandNode.class , instruction.child(0).getClass() );
		assertEquals( OperandNode.class , instruction.child(1).getClass() );
		
		assertEquals( AddressingMode.IMMEDIATE , instruction.getOperand(1).getAddressingMode()  );
		assertEquals( AddressingMode.REGISTER , instruction.getOperand(0).getAddressingMode()  );		
		assertEquals( source , toSourceCode( root , source ) );
	}	
	
	public void testParseLineWithOpCode2() 
	{
		final Parser p = new Parser(this);
		
		String source = "SET 10,a";
        AST ast = p.parse( source ); // invalid addressing mode
		assertTrue( ast.hasErrors() );
		
		ASTNode root = ast.child(0);
		assertNotNull( root );
		assertTrue( root instanceof StatementNode );
		
		assertEquals( InstructionNode.class , root.child(0).getClass() );
	    assertEquals( UnparsedContentNode.class , root.child(0).child(0).getClass() );
		assertEquals(source , toSourceCode( root , source ) );
	}	

}
