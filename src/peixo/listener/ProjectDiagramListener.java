package peixo.listener;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.model.IProject;
import com.vp.plugin.model.IProjectDiagramListener;
import peixo.VPPlugin;

public class ProjectDiagramListener implements IProjectDiagramListener {

    ViewManager _viewManager = ApplicationManager.instance().getViewManager();

    public ProjectDiagramListener() {
    }

    @Override
    public void diagramAdded(IProject arg0, IDiagramUIModel arg1) {
        _viewManager.showMessage("Diagram " + arg1.getType() + " : " + arg1.getName() + " added.");
        arg1.addDiagramListener(VPPlugin.DIAGRAM_LISTENER);
    }

    @Override
    public void diagramRemoved(IProject arg0, IDiagramUIModel arg1) {
        _viewManager.showMessage("Diagram " + arg1.getType() + " : " + arg1.getName() + " removed.");
    }

}
