package peixo.dialogs;

import com.vp.plugin.ViewManager;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.view.IDialog;
import com.vp.plugin.view.IDialogHandler;
import peixo.VPPlugin;
import peixo.actions.SelectDiagramsToProveController;
import peixo.datatypes.peixoDiagram;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class SelectDiagramsToProveDialogHandler implements IDialogHandler {
    private IDialog _dialog;

    private JList<peixoDiagram> list;

    public ViewManager viewManager = VPPlugin.VIEW_MANAGER;

    SelectDiagramsToProveController SelectDiagramsToProveController = new SelectDiagramsToProveController();

    @Override
    public Component getComponent() {

        /*
         *
         * This UI works with a upperPanel and a bottomPanel
         * UpperPanel is Parent of splitPlane
         * splitPlane is Parent of diagramImages and panelForList which contains the ScrollList of the selectable Diagrams
         *
         * BottomPanel is Parent of solveButton and cancelButton
         *
         * */
        // Create All Components
        JPanel mainPanel = new JPanel();
        JPanel upperPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JLabel diagramImages = new JLabel();
        JPanel panelForList = new JPanel();

        DefaultListModel<peixoDiagram> DiagramDataModelForList = new DefaultListModel<>();
        list = new JList<>();
        JScrollPane listScroller = new JScrollPane(list);
        JPanel panelForButtons = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("CANCEL");

        // Options of the Components
        list.setModel(DiagramDataModelForList);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        diagramImages.setPreferredSize(new Dimension(400, 400));

        // Get All Diagrams of the Project
        ArrayList<IDiagramUIModel> diagramArrayList = SelectDiagramsToProveController.getDiagrams();

        //Fill Jlist
        for (IDiagramUIModel m : diagramArrayList) {
            DiagramDataModelForList.addElement(new peixoDiagram(m.getId(), m.getName(), m) {
            });

        }
        // Add Listener
        // Show Selected Diagram as small Icon
        list.getSelectionModel().addListSelectionListener(e -> {
            peixoDiagram item = list.getSelectedValue();
            ImageIcon icon = new ImageIcon(SelectDiagramsToProveController.getDiagramIcons(item.getDiagramId()));
            diagramImages.setIcon(icon);
        });
//         Get the Selected Diagrams to Prove
        okButton.addActionListener(e -> {
            List<peixoDiagram> item = list.getSelectedValuesList();
            SelectDiagramsToProveController.checkReachability(item);
        });

        cancelButton.addActionListener(e -> mainPanel.setVisible(false)); // Does not work as intended!

        // Add all Components to pane

        // Upper
        panelForList.add(listScroller);
        listScroller.setMinimumSize(new Dimension(200, 200));
        // Diagram Images Panel
        diagramImages.setMinimumSize(new Dimension(200, 200));
        diagramImages.setPreferredSize(new Dimension(600, 600));
        diagramImages.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 10));
        // Splitpane
        splitPane.setLeftComponent(listScroller);
        splitPane.setRightComponent(diagramImages);
        splitPane.setDividerLocation(200);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel panelForHeading = new JPanel();
        panelForHeading.add(new JLabel("Choose one or multiple Diagrams to check them"));

        upperPanel.setLayout(new BorderLayout(20, 20));
        upperPanel.add(splitPane, BorderLayout.CENTER);
        upperPanel.add(panelForHeading, BorderLayout.NORTH);
        upperPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 10));

        // Bottom
//        bottomPanel.setLayout(new GridLayout(0, 2));
        panelForButtons.add(okButton);
        panelForButtons.add(cancelButton);
        panelForButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(panelForButtons);

        mainPanel.setLayout(new BorderLayout(25, 25));
        mainPanel.add(panelForHeading, BorderLayout.PAGE_START);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        return mainPanel;
    }


    @Override
    public void prepare(IDialog dialog) {
        this._dialog = dialog;
        _dialog.setModal(true);
        _dialog.setTitle("Select Diagrams to Check");
        _dialog.setResizable(true);
        _dialog.setSize(720, 480);
    }

    @Override
    public void shown() {
    }

    @Override
    public boolean canClosed() {
        return true;
    }

    public SelectDiagramsToProveDialogHandler() {
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


}
