/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.cdt.debug.core.IState;
import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 
 * Responsible for providing labels, images, and editors associated 
 * with debug elements in the CDT debug model.
 * 
 * @since Jul 22, 2002
 */
public class CDTDebugModelPresentation extends LabelProvider
									   implements IDebugModelPresentation
{
	/**
	 * Qualified names presentation property (value <code>"org.eclipse.debug.ui.displayQualifiedNames"</code>).
	 * When <code>DISPLAY_QUALIFIED_NAMES</code> is set to <code>True</code>,
	 * this label provider should use fully qualified type names when rendering elements.
	 * When set to <code>False</code>,this label provider should use simple names
	 * when rendering elements.
	 * @see #setAttribute(String, Object)
	 */
	public final static String DISPLAY_QUALIFIED_NAMES = "DISPLAY_QUALIFIED_NAMES"; //$NON-NLS-1$
	
	protected HashMap fAttributes = new HashMap(3);

	private static CDTDebugModelPresentation fInstance = null;

	/**
	 * Constructor for CDTDebugModelPresentation.
	 */
	public CDTDebugModelPresentation()
	{
		super();
		fInstance = this;
	}

	public static CDTDebugModelPresentation getDefault()
	{
		return fInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute( String attribute, Object value )
	{
		if ( value != null )
		{
			fAttributes.put( attribute, value );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail( IValue value, IValueDetailListener listener )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput( Object element )
	{
		IFile file = null;
		if ( element instanceof IMarker )
		{
			IResource resource = ((IMarker)element).getResource();
			if ( resource instanceof IFile )
				file = (IFile)resource; 
		}
		if ( element instanceof IFile )
			file = (IFile)element;
		if ( file != null ) 
			return new FileEditorInput( file );
/*
		if ( element instanceof IFileStorage )
		{
			return new FileStorageEditorInput( (IFileStorage)element );
		}
		if ( element instanceof IDisassemblyStorage )
		{
			return new DisassemblyEditorInput( (IStorage)element );
		}
*/
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId( IEditorInput input, Object element )
	{
		if ( input != null )
		{
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor descriptor = registry.getDefaultEditor( input.getName() );
			if ( descriptor != null )
				return descriptor.getId();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILabelProvider#getImage(Object)
	 */
	public Image getImage( Object element )
	{
		return super.getImage( element );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILabelProvider#getText(Object)
	 */
	public String getText( Object element )
	{
		boolean showQualified= isShowQualifiedNames();
		StringBuffer label = new StringBuffer();
		try
		{
			if ( element instanceof IVariable )
			{
				label.append( getVariableText( (IVariable)element ) );
				return label.toString();
			}

			if ( element instanceof IStackFrame )
			{
				label.append( getStackFrameText( (IStackFrame)element, showQualified ) );
				return label.toString();
			}

			if ( element instanceof IDebugTarget )
				label.append( getTargetText( (IDebugTarget)element, showQualified ) );
			else if ( element instanceof IThread )
				label.append( getThreadText( (IThread)element, showQualified ) );
			
			if ( element instanceof ITerminate )
			{
				if ( ((ITerminate)element).isTerminated() )
				{
					label.insert( 0, "<terminated>" );
					return label.toString();
				}
			}
			if ( element instanceof IDisconnect )
			{
				if ( ((IDisconnect)element).isDisconnected() )
				{
					label.insert( 0, "<disconnected>" );
					return label.toString();
				}
			}

			if ( label.length() > 0 )
			{
				return label.toString();
			}
		}
		catch ( DebugException e )
		{		
			return "<not_responding>";
		}

		return null;
	}

	protected boolean isShowQualifiedNames()
	{
		Boolean showQualified = (Boolean)fAttributes.get( DISPLAY_QUALIFIED_NAMES );
		showQualified = showQualified == null ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}

	protected boolean isShowVariableTypeNames()
	{
		Boolean show = (Boolean)fAttributes.get( DISPLAY_VARIABLE_TYPE_NAMES );
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}
	
	protected String getTargetText( IDebugTarget target, boolean qualified ) throws DebugException
	{
		if ( target instanceof IState )
		{
			IState state = (IState)target;
			switch( state.getCurrentStateId() )
			{
				case IState.EXITED:
				{
					Object info = state.getCurrentStateInfo();
					String label = target.getName() + " (Exited";
					if ( info != null && info instanceof ICDIExitInfo )
					{
						label += ". Exit code = " + ((ICDIExitInfo)info).getCode();
					}
					return label + ")";
				}
			}
		}
		return target.getName();
	}
	
	protected String getThreadText( IThread thread, boolean qualified ) throws DebugException
	{
		if ( thread.isTerminated() )
		{
			return getFormattedString( "Thread [{0}] (Terminated)", thread.getName() );
		}
		if ( thread.isStepping() )
		{
			return getFormattedString( "Thread [{0}] (Stepping)", thread.getName());
		}
		if ( !thread.isSuspended() )
		{
			return getFormattedString( "Thread [{0}] (Running)", thread.getName() );
		}
		return getFormattedString( "Thread [{0}] (Suspended)", thread.getName() );
	}

	protected String getStackFrameText( IStackFrame stackFrame, boolean qualified ) throws DebugException 
	{
		IStackFrameInfo info = (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class );
		if ( info != null )
		{
			String label = new String();
			label += info.getLevel() + " ";
			if ( info.getFunction() != null )
				label += info.getFunction() + "() ";

			if ( info.getFile() != null )
			{
				IPath path = new Path( info.getFile() );
				if ( !path.isEmpty() )
					label += "at " + ( qualified ? path.toOSString() : path.lastSegment() ) + ":";
			}
			if ( info.getFrameLineNumber() != 0 )
				label += info.getFrameLineNumber();
			return label;
		}
		return stackFrame.getName();
	}

	protected String getVariableText( IVariable var ) throws DebugException
	{
		// temporary
		String label = new String();
		if ( var != null )
		{
			label += var.getName();
			IValue value = var.getValue();
			label += "= " + value.getValueString();
		}
		return label;
	}

	/**
	 * Plug in the single argument to the resource String for the key to 
	 * get a formatted resource String.
	 * 
	 */
	public static String getFormattedString( String key, String arg )
	{
		return getFormattedString( key, new String[]{ arg } );
	}

	/**
	 * Plug in the arguments to the resource String for the key to get 
	 * a formatted resource String.
	 * 
	 */
	public static String getFormattedString(String string, String[] args)
	{
		return MessageFormat.format( string, args );
	}
}
