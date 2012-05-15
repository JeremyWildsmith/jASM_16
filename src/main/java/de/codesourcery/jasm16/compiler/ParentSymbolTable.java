package de.codesourcery.jasm16.compiler;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import de.codesourcery.jasm16.exceptions.DuplicateSymbolException;
import de.codesourcery.jasm16.parser.Identifier;

public class ParentSymbolTable implements IParentSymbolTable
{
    private final IdentityHashMap<ICompilationUnit,ISymbolTable> tables = new IdentityHashMap<ICompilationUnit,ISymbolTable>();
    
    public ParentSymbolTable() {
    }
    
    @Override
    public ISymbol getSymbol(Identifier identifier)
    {
        for ( ISymbolTable table : tables.values() ) 
        {
            final ISymbol result = table.getSymbol( identifier );
            if ( result != null ) {
                return result;
            }
        }
        return null;
    }
    
    @Override
    public List<ISymbol> getSymbols(Identifier identifier)
    {
        final List<ISymbol>  result = new ArrayList<ISymbol>();
        for ( ISymbolTable table : tables.values() ) 
        {
            ISymbol s = table.getSymbol( identifier );
            if ( s != null ) {
                result.add( s );
            }
        }        
        return result;
    }    
    
    @Override
    public List<ISymbol> getSymbols()
    {
        final List<ISymbol>  result = new ArrayList<ISymbol>();
        for ( ISymbolTable table : tables.values() ) {
            result.addAll( table.getSymbols() );
        }        
        return result;
    }

    @Override
    public void defineSymbol(ISymbol symbol) throws DuplicateSymbolException
    {
        final ICompilationUnit unit = symbol.getCompilationUnit();
        ISymbolTable table = tables.get( unit );
        if ( table == null ) {
            table = unit.getSymbolTable();
            table.setParent( this );
            tables.put( unit  , table );
        }
        
        for ( ISymbolTable tmp : tables.values() ) {
            if ( tmp.containsSymbol( symbol.getIdentifier() ) ) {
                throw new DuplicateSymbolException( tmp.getSymbol( symbol.getIdentifier() ) , symbol );
            }
        }
        table.defineSymbol( symbol );
    }

    @Override
    public boolean containsSymbol(Identifier identifier)
    {
        for ( ISymbolTable table : tables.values() ) 
        {
            if ( table.containsSymbol( identifier ) ) {
                return true;
            }
        }        
        return false;
    }

    @Override
    public void clear()
    {
        for ( ISymbolTable table : tables.values() ) 
        {
            table.clear();
        }              
    }
    
    @Override
    public IParentSymbolTable getParent()
    {
        return null;
    }
    
    @Override
    public void setParent(IParentSymbolTable table)
    {
        throw new UnsupportedOperationException("Parent symbol tables cannot have other parents");
    }

    @Override
    public int getSize()
    {
        int result = 0;
        for ( ISymbolTable table : tables.values() ) 
        {
            result += table.getSize();
        }            
        return result;
    }
}