package hellocube.actions;

import hello.cube.*;
import hellocube.markers.*;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.*;

public class RunCodeForestAction implements IObjectActionDelegate {

    public static final String ANNOTATION = "br.usp.each.saeg.code.forest.lowRankAnnotation";

    @Override
    public void run(IAction action) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }
        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
        Object firstElement = selection.getFirstElement();
        if (!(firstElement instanceof IAdaptable)) {
            return;
        }
        IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);

        for (List<IResource> files : javaFilesOf(project).values()) {
            for (IResource file : files) {
                parse(file);
            }
        }

        if (xmlFilesOf(project).isEmpty()) {
            MessageDialog.openError(window.getShell(), "Code Forest", "codeforest.xml not found");
        }
        IPath path = project.getFullPath();
        System.out.println(path);
    }

    private void parse(final IResource file) {
        try {
            ASTParser parser = ASTParser.newParser(AST.JLS4);
            parser.setSource(FileUtils.readFileToString(new File(file.getLocationURI())).toCharArray());
            final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            cu.accept(new ASTVisitor() {
                @Override
                public boolean visit(MethodDeclaration node) {
                    System.out.println(node.getBody());
                    System.out.println(node.getName());
                    System.out.println(cu.getLineNumber(node.getStartPosition()) - 1);
                    Position pos = new Position(node.getStartPosition()-1, node.getLength());
                    try {
                        IMarker mymarker = MyMarkerFactory.createMarker(file, pos);
                        addAnnotation(mymarker, pos, Activator.getEditor());

                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, List<IResource>> javaFilesOf(IProject project) {
        try {
            return visit(project, "java");
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_MAP;
        }
    }

    private Map<String, List<IResource>> xmlFilesOf(IProject project) {
        try {
            return visit(project, "xml");
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_MAP;
        }
    }


    private Map<String, List<IResource>> visit(IProject project, final String extension) throws CoreException {
        final Map<String, List<IResource>> result = new HashMap<String, List<IResource>>();
        project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(IResource arg) throws CoreException {
                if (extension.equalsIgnoreCase(arg.getFileExtension())) {
                    if (!result.containsKey(arg.getName())) {
                        result.put(arg.getName(), new ArrayList<IResource>());
                    }
                    result.get(arg.getName()).add(arg);
                }
                return true;
            }
        });
        return result;
    }

    private void addAnnotation(IMarker marker, Position pos, ITextEditor editor) {
        //The DocumentProvider enables to get the document currently loaded in the editor
        IDocumentProvider idp = editor.getDocumentProvider();

        //This is the document we want to connect to. This is taken from the current editor input.
        IDocument document = idp.getDocument(editor.getEditorInput());

        //The IannotationModel enables to add/remove/change annoatation to a Document loaded in an Editor
        IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());

        //Note: The annotation type id specify that you want to create one of your annotations
        SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(ANNOTATION, marker);

        //Finally add the new annotation to the model
        iamf.connect(document);
        iamf.addAnnotation(ma, pos);
        iamf.disconnect(document);
  }

    @Override
    public void selectionChanged(IAction arg0, ISelection arg1) {
    }

    @Override
    public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
    }
}
