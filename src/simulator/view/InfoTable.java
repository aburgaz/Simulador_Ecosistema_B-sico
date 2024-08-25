package simulator.view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class InfoTable extends JPanel {
    String _title;
    TableModel _tableModel;

    public InfoTable(String title, TableModel tableModel) {
        _title = title;
        _tableModel = tableModel;
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(_title));
        JTable table = new JTable(_tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }
}