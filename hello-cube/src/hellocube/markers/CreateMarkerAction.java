package hellocube.markers;

import hello.cube.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class CreateMarkerAction implements IEditorActionDelegate {

	public CreateMarkerAction() {
		super();
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart editor) {
	}

	/*
	 * This action creates a new marker for the given IFile
	 */
	@Override
	public void run(IAction action) {
		try {
			TextSelection selection = MyMarkerFactory.getTextSelection();
			IFile file = (IFile) Activator.getEditor().getEditorInput().getAdapter(IFile.class);
			IMarker mymarker = MyMarkerFactory.createMarker(file, new Position(selection.getOffset(), selection.getLength()));

			MyMarkerFactory.addAnnotation(mymarker, selection, Activator.getEditor());

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
