package peixo.actions;
import com.vp.plugin.*;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;


class TestFailedException extends Exception
{
    public TestFailedException()
    {
        super("Check FAILED");
    }
}

public class HotReloadController implements VPActionController {

    public void HotReload(){
        ViewManager viewManager = ApplicationManager.instance().getViewManager();
        viewManager.showMessage("Reloading Plugin", "VPPlugin");
        ApplicationManager manager = ApplicationManager.instance();
        manager.reloadPluginClasses("peixo");
    }

    @Override
    public void performAction(VPAction vpAction) {
        ApplicationManager manager = ApplicationManager.instance();
        manager.reloadPluginClasses("peixo");
        HotReload();

    }

    @Override
    public void update(VPAction vpAction) {

    }
}
