package peixo;

import com.vp.plugin.*;
import com.vp.plugin.model.IProject;
import peixo.listener.*;

public class VPPlugin implements com.vp.plugin.VPPlugin {
    ViewManager viewManager = ApplicationManager.instance().getViewManager();
    public static ModelListener MODEL_LISTENER = new ModelListener();
    public static DiagramListener DIAGRAM_LISTENER = new DiagramListener();
    public static ProjectModelListener PROJECT_MODEL_LISTENER = new ProjectModelListener();
    public static ProjectDiagramListener PROJECT_DIAGRAM_LISTENER = new ProjectDiagramListener();

    public static ViewManager VIEW_MANAGER = ApplicationManager.instance().getViewManager();
    public static ProjectManager PROJECT_MANAGER = ApplicationManager.instance().getProjectManager();
    public static DiagramManager DIAGRAM_MANAGER = ApplicationManager.instance().getDiagramManager();


    @Override
    public void loaded(VPPluginInfo vpPluginInfo) {
        viewManager.showMessage("Plugin loaded" + "ID: " + vpPluginInfo.getPluginId());
        ProjectListener projectListener = new ProjectListener();
        IProject project = ApplicationManager.instance().getProjectManager().getProject();
        project.addProjectListener(projectListener);
    }

    @Override
    public void unloaded() {

    }
}
