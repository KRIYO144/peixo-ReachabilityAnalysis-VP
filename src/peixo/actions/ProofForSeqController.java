package peixo.actions;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.ProjectManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.diagram.IDiagramElement;
import com.vp.plugin.model.IModelElement;
import com.vp.plugin.model.IProject;
import com.vp.plugin.model.IStateMachine;
import peixo.VPPlugin;


import java.awt.*;
import java.util.Arrays;

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
    public String getActiveModelByID() {
        IProject project = ApplicationManager.instance().getProjectManager().getProject();
        DiagramManager modelManager = ApplicationManager.instance().getDiagramManager();
        String id = modelManager.getActiveDiagram().getId();

        return id;
    }

    @Override
    public void performAction(VPAction Action) {
//        IModelElement[] modelElements = projectManager.getProject().toAllLevelModelElementArray();
        DiagramManager diagramManager1 = VPPlugin.DIAGRAM_MANAGER;
        IDiagramElement[] diagramElements = diagramManager1.getActiveDiagram().toDiagramElementArray();
        try {
            IStateMachine stateMachine = (IStateMachine) diagramManager1.getActiveDiagram();
        } catch (Exception e) {
            viewManager.showMessage(e.toString());
        }
        ViewManager vm = VPPlugin.VIEW_MANAGER;
        vm.showMessage(Arrays.toString(diagramElements));

    }

    @Override
    public void update(VPAction vpAction) {

    }
}
