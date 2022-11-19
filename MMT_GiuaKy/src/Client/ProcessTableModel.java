package Client;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Vector;

public class ProcessTableModel extends AbstractTableModel
{

    private final String[] COLUMNS = {"PROCESS NAME", "PID", "SESSION NAME", "SESSION #", "MEMORY USAGE"};

    private final List <String[]> maindata;

    ProcessTableModel(List <String[]> maindata)
    {
        this.maindata = maindata;
    }

    public int getRowCount() { return maindata.size(); }

    public int getColumnCount() { return COLUMNS.length;  }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return( maindata.get(rowIndex)[columnIndex] );
    }

    public String getColumnName(int column)
    {
        return COLUMNS[column];
    }
    public Class<?> getColumnClass(int columnIndex)
    {
        if(getValueAt(0,columnIndex) != null)
            return getValueAt(0,columnIndex).getClass();
        else return Object.class;
    }

}
