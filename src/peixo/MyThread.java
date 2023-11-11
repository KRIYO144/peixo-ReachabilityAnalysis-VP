package peixo;


import com.vp.plugin.ProjectManager;
import com.vp.plugin.ViewManager;

public class MyThread implements Runnable {
    ViewManager viewManager = VPPlugin.VIEW_MANAGER;
    ProjectManager pm = VPPlugin.PROJECT_MANAGER;

    public void run() {
        pm.getProject().setName("Ich habe es im thread geändert");
        int var = 0;
        for (int i = 0; i < 10; i++) {
            viewManager.showMessage("Thread Hallo");
        }
    }
}

