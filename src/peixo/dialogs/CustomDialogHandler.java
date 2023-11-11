package peixo.dialogs;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.ExportDiagramAsImageOption;
import com.vp.plugin.ProjectManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.diagram.IDiagramElement;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.view.IDialog;
import com.vp.plugin.view.IDialogHandler;
import peixo.VPPlugin;
import peixo.actions.SelectDiagramsToProveController;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class CustomDialogHandler implements IDialogHandler {
    private IDialog _dialog;

    private Component component;
    private JList<ListItem> list;
    private JScrollPane listScroller;
    private JSplitPane splitPane;
    private JEditorPane diagramImg;

    public ViewManager viewManager = VPPlugin.VIEW_MANAGER;
    public ProjectManager pm = VPPlugin.PROJECT_MANAGER;

    SelectDiagramsToProveController SelectDiagramsToProveController = new SelectDiagramsToProveController();

    @Override
    public Component getComponent() {
        // Create All Components
        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(2, 2));
        DefaultListModel<ListItem> model = new DefaultListModel<>();
        list = new JList<>();
        splitPane = new JSplitPane();
        JPanel panelForLabel = new JPanel();
        JLabel label = new JLabel();
        JScrollPane listScroller = new JScrollPane(list);

        // Get All Diagrams of the Project
        ArrayList<IDiagramUIModel> diagramlist = SelectDiagramsToProveController.getDiagrams();


        //Fill Jlist
        list.setModel(model);
        for (IDiagramUIModel m : diagramlist) {
            model.addElement(new ListItem(m.getId(), m.getName(), m) {
            });
        }
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListItem item = list.getSelectedValue();
                label.setText("Dieses Item ist augewählt" + item.DiagramName + "ID: " + item.DiagramId);
            }
        });
        panelForLabel.add(label);

        //Test
        String lDescription = diagramlist.get(0).getHtmlDocumentation();
        ExportDiagramAsImageOption option = new ExportDiagramAsImageOption(ExportDiagramAsImageOption.IMAGE_TYPE_PNG);
        Image image = ApplicationManager.instance().getModelConvertionManager().exportDiagramAsImage(diagramlist.get(0), option);
        viewManager.showMessage(lDescription);


        // Add all Components to pane
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        pane.add(listScroller);
        pane.add(imageLabel);
        pane.add(panelForLabel);
        this.component = pane;


        return pane;
    }


    @Override
    public void prepare(IDialog dialog) {
        this._dialog = dialog;
        _dialog.setModal(true);
        _dialog.setTitle("Test");
        _dialog.setResizable(true);
        _dialog.pack();
    }

    @Override
    public void shown() {
        ViewManager viewManager = VPPlugin.VIEW_MANAGER;
        viewManager.showMessage("Ich befinde mich in shown");

    }

    @Override
    public boolean canClosed() {
        return true;
    }

    public CustomDialogHandler() {
        IDialog dia = new IDialog() {
            @Override
            public void setTitle(String s) {

            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public void setSize(Dimension dimension) {

            }

            @Override
            public void setSize(int i, int i1) {

            }

            @Override
            public Dimension getSize() {
                return null;
            }

            @Override
            public int getWidth() {
                return 0;
            }

            @Override
            public int getHeight() {
                return 0;
            }

            @Override
            public void setLocation(Point point) {

            }

            @Override
            public void setLocation(int i, int i1) {

            }

            @Override
            public Point getLocation() {
                return null;
            }

            @Override
            public int getX() {
                return 0;
            }

            @Override
            public int getY() {
                return 0;
            }

            @Override
            public void setBounds(int i, int i1, int i2, int i3) {

            }

            @Override
            public void setResizable(boolean b) {

            }

            @Override
            public boolean isResizable() {
                return false;
            }

            @Override
            public void setModal(boolean b) {

            }

            @Override
            public boolean isModal() {
                return false;
            }

            @Override
            public void pack() {

            }

            @Override
            public void close() {

            }
        };

        try {
            getComponent();
            prepare(dia);

        } catch (Exception e) {
            viewManager.showMessage("Der Konstruktor ist kaputt");

        }
    }

    static class ListItem {
        private String DiagramId;
        private String DiagramName;

        private IDiagramUIModel ModelObject;

        ListItem(String id, String name, IDiagramUIModel m) {
            this.DiagramId = id;
            this.DiagramName = name;
            this.ModelObject = m;
        }

        @Override
        public String toString() {
            return DiagramName;
        }
    }
}
