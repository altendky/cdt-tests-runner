/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol;
import org.eclipse.cdt.internal.core.parser.pst.StandardSymbolExtension;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateDeclaration extends ASTSymbol implements IASTTemplateDeclaration
{
	final private List templateParameters;
	private ISymbol owned = null;
	private IASTScope ownerScope;
	private ITemplateFactory factory;
	private NamedOffsets offsets = new NamedOffsets();
	
	private ITemplateSymbol getTemplateSymbol(){
		return (ITemplateSymbol) (( getSymbol() instanceof ITemplateSymbol ) ? getSymbol() : null);
	}
    /**
     * 
     */
    public ASTTemplateDeclaration( ITemplateSymbol template, IASTScope scope, List parameters )
    {
        super( template );
        
        IContainerSymbol container = null;
        if( scope instanceof ASTTemplateDeclaration )
        	container = ((ASTTemplateDeclaration)scope).getContainerSymbol();
        else
        	container = ((ASTScope)scope).getContainerSymbol();
        
        if( container instanceof ITemplateFactory ){
        	factory = (ITemplateFactory) container;
        } else {
        	factory = template.getSymbolTable().newTemplateFactory();
        	factory.setContainingSymbol( container );
        	factory.setASTExtension( new StandardSymbolExtension(factory, this ) );
        }

        factory.pushTemplate( template );
        
        templateParameters = parameters;
        ownerScope = scope;
    }
    
    public IASTScope getOwnerScope(){
    	return ownerScope;
    }
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration#isExported()
     */
    public boolean isExported()
    {
        // TODO Auto-generated method stub
        return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplate#getOwnedDeclaration()
     */
    public IASTDeclaration getOwnedDeclaration()
    {
    	if( owned != null && owned.getASTExtension() != null ){
    		ASTNode node = owned.getASTExtension().getPrimaryDeclaration();
    		return ( node instanceof IASTDeclaration ) ? (IASTDeclaration)node : null;
    	}
    	
    	IContainerSymbol ownedSymbol = getTemplateSymbol().getTemplatedSymbol();
    	if( ownedSymbol != null && ownedSymbol.getASTExtension() != null ){
    		ASTNode node = ownedSymbol.getASTExtension().getPrimaryDeclaration();
    		return ( node instanceof IASTDeclaration ) ? (IASTDeclaration)node : null;
    	}
        return null;
    }
    
	public void setOwnedDeclaration(ISymbol symbol) {
		owned = symbol;
	}
    
    public void releaseFactory(){
    	factory = null;
    }
    
    public IContainerSymbol getContainerSymbol()
	{
       	return factory != null ? (IContainerSymbol) factory : getTemplateSymbol();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameterList#getTemplateParameters()
     */
    public Iterator getTemplateParameters()
    {
    	return templateParameters.iterator();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	offsets.setStartingOffsetAndLineNumber(offset, lineNumber);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	offsets.setEndingOffsetAndLineNumber( offset, lineNumber );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public int getStartingOffset()
    {
    	return offsets.getStartingOffset();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public int getEndingOffset()
    {
        return offsets.getEndingOffset();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.enterTemplateDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.exitTemplateDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
	 */
	public int getStartingLine() {
		return offsets.getStartingLine();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
	 */
	public int getEndingLine() {
		return offsets.getEndingLine();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTemplate#setOwnedDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
	 */
	public void setOwnedDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations() throws ASTNotImplementedException {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}
