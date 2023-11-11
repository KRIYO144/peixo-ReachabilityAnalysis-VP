package peixo.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.model.IModel;
import com.vp.plugin.model.IModelElement;

public class ModelListener implements PropertyChangeListener {

    ViewManager _viewManager = ApplicationManager.instance().getViewManager();

    public ModelListener() {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object changeSource = evt.getSource();
        if (changeSource instanceof IModelElement) {
            IModelElement model = (IModelElement) changeSource;
            _viewManager.showMessage("Model Element " + model.getModelType() + " : " + model.getName() + " modified.");
        }
    }

}
