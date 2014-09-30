package hellocube.handlers;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.*;


public class RunCodeForestHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent arg) throws ExecutionException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject();
        //System.out.println(project.getName());
        return null;
    }

}
