package simulator.view;

import java.util.Hashtable;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import java.awt.Dimension;
import java.awt.Frame;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class ChangeRegionsDialog extends JDialog implements EcoSysObserver {
    private DefaultComboBoxModel<String> _regionsModel;
    private DefaultComboBoxModel<String> _fromRowModel;
    private DefaultComboBoxModel<String> _toRowModel;
    private DefaultComboBoxModel<String> _fromColModel;
    private DefaultComboBoxModel<String> _toColModel;
    private DefaultTableModel _dataTableModel;
    private Controller _ctrl;
    private List<JSONObject> _regionsInfo;
    private String[] _headers = {"Key", "Value", "Description"};
    private int _status;

    public ChangeRegionsDialog(Controller ctrl) {
        super((Frame) null, true);
        _ctrl = ctrl;
        initGUI();
        _ctrl.addObserver(this);
    }

    private void initGUI() {
        setTitle("Change Regions");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        setContentPane(mainPanel);

        // HELP PANEL
        JPanel helpPanel = new JPanel();
        helpPanel.setAlignmentX(CENTER_ALIGNMENT);      // Central alignment is important
        mainPanel.add(helpPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JTextArea helpText = new JTextArea("Select the region type, the rows/cols intervals, and provide values for the parameters in "
                + "the Value column (default values are used for parameters with no values)", 2, 50);
        helpText.setLineWrap(true);
        helpText.setOpaque(false);
        helpText.setEditable(false);
        helpPanel.add(helpText);

        // TABLE PANEL
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        mainPanel.add(tablePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        _regionsInfo = Main._region_factory.get_info();     // _regionsInfo is a list of JSONObjects with the information of the regions
        _dataTableModel = new DefaultTableModel() {       // _dataTableModel es un modelo de tabla que incluye todos los parámetros de la region
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;     // Only the value column is editable
            }
        };
        _dataTableModel.setColumnIdentifiers(_headers);     // _headers is an array of strings with the headers of the table (see line 31)
        JTable table = new JTable(_dataTableModel);     // table is a JTable that uses _dataTableModel as its model
        tablePanel.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));   // JScrollPane is used to add scrollbars to the table


        // COMBOBOX PANEL
        JPanel comboBoxPanel = new JPanel();
        comboBoxPanel.setAlignmentX(CENTER_ALIGNMENT);      // Central alignment is important
        mainPanel.add(comboBoxPanel);

        _regionsModel = new DefaultComboBoxModel<>();
        for (JSONObject region : _regionsInfo) {        // For each region in _regionsInfo, add its type to _regionsModel, what region_factory needs
            _regionsModel.addElement(region.getString("type"));
        }

        JLabel regionsLabel = new JLabel("Region type: ");
        comboBoxPanel.add(regionsLabel);
        JComboBox<String> regionsComboBox = new JComboBox<>(_regionsModel);     // regionsComboBox is a JComboBox that uses _regionsModel as its model
        regionsComboBox.addActionListener((e) -> {
            String regionType = (String) _regionsModel.getSelectedItem();       // Get the selected region type
            for (JSONObject region : _regionsInfo) {    // For each region in _regionsInfo
                if (region.get("type").equals(regionType)) {        // If the region type is the same as the selected one
                    JSONObject data = region.getJSONObject("data");     // Get the data of the region
                    _dataTableModel.setRowCount(0);     // Set the number of rows of the table to 0
                    for (String key : data.keySet()) {      // For each key in the data of the region
                        _dataTableModel.addRow(new String[]{key, null, data.get(key).toString()});      // Add a row to the table with the key, a null value and the description
                    }
                }
            }
        });
        comboBoxPanel.add(regionsComboBox);

        // SELECT THE ROWS AND COLS INTERVALS FOR THE NEW REGION (FROM - TO)
        // FROM ROW COMBOBOX
        _fromRowModel = new DefaultComboBoxModel<>();
        JLabel fromRowLabel = new JLabel("From row: ");
        comboBoxPanel.add(fromRowLabel);
        JComboBox<String> fromRowComboBox = new JComboBox<>(_fromRowModel);
        comboBoxPanel.add(fromRowComboBox);

        // TO ROW COMBOBOX
        _toRowModel = new DefaultComboBoxModel<>();
        JLabel toRowLabel = new JLabel("To row: ");
        comboBoxPanel.add(toRowLabel);
        JComboBox<String> toRowComboBox = new JComboBox<>(_toRowModel);
        comboBoxPanel.add(toRowComboBox);

        // FROM COL COMBOBOX
        _fromColModel = new DefaultComboBoxModel<>();
        JLabel fromColLabel = new JLabel("From col: ");
        comboBoxPanel.add(fromColLabel);
        JComboBox<String> fromColComboBox = new JComboBox<>(_fromColModel);
        comboBoxPanel.add(fromColComboBox);

        // TO COL COMBOBOX
        _toColModel = new DefaultComboBoxModel<>();
        JLabel toColLabel = new JLabel("To col: ");
        comboBoxPanel.add(toColLabel);
        JComboBox<String> toColComboBox = new JComboBox<>(_toColModel);
        comboBoxPanel.add(toColComboBox);


        // BUTTON PANEL
        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(CENTER_ALIGNMENT);     // Central alignment is important
        mainPanel.add(buttonPanel);

        // OK Button
        JButton okButton = new JButton("OK");
        okButton.addActionListener((e) -> {
            okAction();
            if (_status == 1)   // If the status is 1, the regions have been set correctly, so the dialog is closed
                setVisible(false);      // If the status is 0, the regions have not been set correctly, so the dialog is not closed
        });

        // Cancel Button
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> {
            _status = 0;
            setVisible(false);
        });
        buttonPanel.add(cancelButton);

        // DIALOG CONFIGURATION
        setPreferredSize(new Dimension(700, 400)); // puedes usar otro tamaño
        pack();
        setResizable(false);
        setVisible(false);
    }

    public void open(Frame parent) {
        setLocation(parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
        pack();
        setVisible(true);
    }

    private void okAction() {
        JSONObject rs = new JSONObject();       // rs is a JSONObject that will contain the regions

        int row_from = Integer.parseInt((String) _fromRowModel.getSelectedItem());   // Get the selected row from the combobox
        int row_to = Integer.parseInt((String) _toRowModel.getSelectedItem());   // Get the selected row to the combobox
        JSONArray rowData = new JSONArray();
        rowData.put(row_from);
        rowData.put(row_to);
        int col_from = Integer.parseInt((String) _fromColModel.getSelectedItem());   // Get the selected col from the combobox
        int col_to = Integer.parseInt((String) _toColModel.getSelectedItem());   // Get the selected col to the combobox
        JSONArray colData = new JSONArray();
        colData.put(col_from);
        colData.put(col_to);
        JSONObject region_info = new JSONObject();      // region_info is a JSONObject that will contain the selected region info
        region_info.put("type", _regionsModel.getSelectedItem());   // Get the selected region type
        JSONObject region_data = new JSONObject();
        for (int i = 0; i < _dataTableModel.getRowCount(); i++) {
            String key = (String) _dataTableModel.getValueAt(i, 0);   // Get the key of the row
            String value = (String) _dataTableModel.getValueAt(i, 1);   // Get the value of the row
            region_data.put(key, value);
        }
        region_info.put("data", region_data);   // Add the region data to region_info

        JSONObject r = new JSONObject();
        r.put("row", rowData);
        r.put("col", colData);
        r.put("spec", region_info);
        rs.put("regions", new JSONArray().put(r));      // Add the region to rs as a JSONArray
        try {
            _ctrl.set_regions(rs);    // Set the regions in the controller
            _status = 1;
        } catch (Exception e) {
            _status = 0;
            ViewUtils.showErrorMsg(this, e.getMessage());
        }
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        // When the simulation is registered, the comboboxes are updated with the actual rows and cols
        for (int i = 0; i < map.get_rows(); i++) {
            _fromRowModel.addElement(String.valueOf(i));
            _toRowModel.addElement(String.valueOf(i));
        }
        for (int i = 0; i < map.get_cols(); i++) {
            _fromColModel.addElement(String.valueOf(i));
            _toColModel.addElement(String.valueOf(i));
        }
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        // When the simulation is reset, the comboboxes are updated with the new rows and cols
        _fromRowModel.removeAllElements();
        _toRowModel.removeAllElements();
        _fromColModel.removeAllElements();
        _toColModel.removeAllElements();

        for (int i = 0; i < map.get_rows(); i++) {
            _fromRowModel.addElement(String.valueOf(i));
            _toRowModel.addElement(String.valueOf(i));
        }
        for (int i = 0; i < map.get_cols(); i++) {
            _fromColModel.addElement(String.valueOf(i));
            _toColModel.addElement(String.valueOf(i));
        }
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
    }

    @Override
    public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
    }
}
