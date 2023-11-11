package peixo.actions;

import com.vp.plugin.*;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.view.IDialogHandler;
import peixo.MyThread;
import peixo.VPPlugin;
import peixo.dialogs.CustomDialogHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class SelectDiagramsToProveController implements VPActionController {
    ViewManager viewManager = VPPlugin.VIEW_MANAGER;

    @Override
    public void performAction(VPAction vpAction) {
        MyThread t = new MyThread();
        t.run();
        try {
            IDialogHandler dialogHandler = new CustomDialogHandler();
            viewManager.showDialog(dialogHandler);
        } catch (Exception e) {
            viewManager.showMessage("ShowDialog kaputt");
        }
    }

    @Override
    public void update(VPAction vpAction) {

    }

    public ArrayList<IDiagramUIModel> getDiagrams() {
        ProjectManager projectManager = VPPlugin.PROJECT_MANAGER;
        Iterator diagramIter = projectManager.getProject().diagramIterator();
        ArrayList<IDiagramUIModel> diagramList = new ArrayList<IDiagramUIModel>() {
        };

        while (diagramIter.hasNext()) {
            IDiagramUIModel model = (IDiagramUIModel) diagramIter.next();
            diagramList.add(model);
        }
        return diagramList;
    }

    public Image getDiagramIcons(String id) {
        ExportDiagramAsImageOption option = new ExportDiagramAsImageOption(ExportDiagramAsImageOption.IMAGE_TYPE_PNG);
        ProjectManager projectManager = VPPlugin.PROJECT_MANAGER;
        DiagramManager diagramManager = VPPlugin.DIAGRAM_MANAGER;
        IDiagramUIModel activeDiagram = projectManager.getProject().getDiagramById(id);

        return ApplicationManager.instance().getModelConvertionManager().exportDiagramAsImage(activeDiagram, option);
    }

}
