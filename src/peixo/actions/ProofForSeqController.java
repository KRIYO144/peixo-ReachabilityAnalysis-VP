package peixo.actions;
import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.ProjectManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.model.IModelElement;
import com.vp.plugin.model.factory.IModelElementFactory;

import javax.swing.*;
import java.awt.*;

public class ProofForSeqController implements VPActionController {

    public void proof(){
        ViewManager viewManager = ApplicationManager.instance().getViewManager();
        Component parentFrame = viewManager.getRootFrame();
        viewManager.showMessage("Proof2", "VPPlugin");

    }

    @Override
    public void performAction(VPAction Action) {
        proof();
    }

    @Override
    public void update(VPAction vpAction) {

    }
}
