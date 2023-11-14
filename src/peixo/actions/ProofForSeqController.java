package peixo.actions;
import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.ProjectManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.model.IModelElement;
import com.vp.plugin.model.IProject;
import com.vp.plugin.view.IDialogHandler;
import peixo.VPPlugin;
import peixo.dialogs.SelectDiagramsToProveDialogHandler;

import java.awt.*;

public class ProofForSeqController implements VPActionController {
    DiagramManager diagramManager = VPPlugin.DIAGRAM_MANAGER;
    ViewManager viewManager = VPPlugin.VIEW_MANAGER;
    ProjectManager projectManager = VPPlugin.PROJECT_MANAGER;


    //    public void proof(String id){
//        DiagramManager DiagramManager = ApplicationManager.instance().getDiagramManager();
//        Iterator diagramModelIter = project.modelElementIterator();
//        viewManager.showMessage(id);
//        IDiagramUIModel model = project.getDiagramById(id);
//        model.setName("Hallo es funktioniert");
//
//        IDiagramElement diagramElement = (IDiagramElement) diagramModelIter.next();
//    }
    public String getModelByID() {
        IProject project = ApplicationManager.instance().getProjectManager().getProject();
        DiagramManager modelManager = ApplicationManager.instance().getDiagramManager();
        String id = modelManager.getActiveDiagram().getId();
        return id;
    }
    @Override
    public void performAction(VPAction Action) {
        IModelElement[] modelElements = projectManager.getProject().toAllLevelModelElementArray();
        Component rootframe = viewManager.getRootFrame();
        IDialogHandler handler = new SelectDiagramsToProveDialogHandler();
        viewManager.showMessage("Vor dem Dialog2");
        viewManager.showDialog(handler);
        viewManager.showMessage("Nach dem Dialog");
    }

    @Override
    public void update(VPAction vpAction) {

    }
}
