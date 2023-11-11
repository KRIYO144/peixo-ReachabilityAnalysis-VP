package peixo.listener;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.model.IProject;
import com.vp.plugin.model.IProjectListener;
import peixo.VPPlugin;

public class ProjectListener implements IProjectListener {

    ViewManager _viewManager = ApplicationManager.instance().getViewManager();

    public ProjectListener() {
    }

    @Override
    public void projectAfterOpened(IProject arg0) {
    }

    @Override
    public void projectNewed(IProject arg0) {
        _viewManager.showMessage("New project " + arg0.getName() + " created.");
        arg0.addProjectDiagramListener(VPPlugin.PROJECT_DIAGRAM_LISTENER);
        arg0.addProjectModelListener(VPPlugin.PROJECT_MODEL_LISTENER);
    }

    @Override
    public void projectOpened(IProject arg0) {
        _viewManager.showMessage("Project " + arg0.getName() + " opened.");
        arg0.addProjectDiagramListener(VPPlugin.PROJECT_DIAGRAM_LISTENER);
        arg0.addProjectModelListener(VPPlugin.PROJECT_MODEL_LISTENER);


    }

    @Override
    public void projectPreSave(IProject arg0) {
        _viewManager.showMessage("Project " + arg0.getName() + " is going to save.");

    }

    @Override
    public void projectRenamed(IProject arg0) {
        _viewManager.showMessage("Project " + arg0.getName() + " being renamed.");

    }

    @Override
    public void projectSaved(IProject arg0) {
        _viewManager.showMessage("Project " + arg0.getName() + " is saved.");

    }

}
