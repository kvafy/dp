// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/09

package bna.bnlib.misc;

import java.util.Arrays;
import java.util.LinkedList;


/**
 * Provides a clean interface to produce a nice structured tabular output as a string.
 * Columns of the table are auto-size to accomodate for their content.
 */
public class TextualTable {
    private static final int MIN_COLUMN_WIDTH = 1;
    private static final int CELL_PADDING = 1; // number of spaces on the left and right
    private static char COLUMN_SEPARATOR = '|';
    // internal data
    private int columnCount;
    private LinkedList<Object[]> rows; // header followed by data rows
    // customization
    private int precision;             // number of decimal places
    private boolean separateColumns;   // use extra character (COLUMN_SEPARATOR) between columns?
    
    
    /**
     * Create table with given set of columns, floating point precesion and
     * optionally separated columns.
     */
    public TextualTable(String[] columnHeaders, int precision, boolean separateColumns) {
        this.columnCount = columnHeaders.length;
        this.rows = new LinkedList<Object[]>();
        this.rows.add(columnHeaders);
        this.precision = precision;
        this.separateColumns = separateColumns;
    }
    
    /** How many decimal places should be used for Double and Float instances. */
    public void setNumberPrecision(int prec) {
        this.precision = prec;
    }
    
    /** Should columns be separated by a non-white character? */
    public void setColumnSeparatorFlag(boolean flg) {
        this.separateColumns = flg;
    }
    
    /**
     * Append a row.
     * @throws IllegalArgumentException When length of the data row is different
     *         than was specified in the constructor of this table.
     */
    public void addRow(Object[] data) {
        if(data == null || data.length != this.columnCount)
            throw new IllegalArgumentException("Data row has invalid number of entries.");
        this.rows.addLast(data);
    }
    
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        int[] columnWidths = this.getColumnWidths();
        boolean currentIsHeader = true;
        // line by line
        for(Object[] row : this.rows) {
            if(!currentIsHeader) // finish the previous line
                res.append(System.getProperty("line.separator"));
            if(this.separateColumns) // left-most column separator
                res.append(COLUMN_SEPARATOR);
            for(int i = 0 ; i < this.columnCount ; i++) {
                this.appendNTimes(res, CELL_PADDING, " ");
                res.append(this.objectToString(row[i], columnWidths[i]));
                this.appendNTimes(res, CELL_PADDING, " ");
                if(this.separateColumns)
                    res.append(COLUMN_SEPARATOR);
            }
            // header is underlined by "-" characters
            if(currentIsHeader) {
                res.append(System.getProperty("line.separator"));
                this.appendNTimes(res, this.getTableWidth(columnWidths), "-");
                currentIsHeader = false;
            }
        }
        return res.toString();
    }
    
    private String getFloatFormatstring() {
        return String.format("%%.%df", this.precision);
    }
    
    private int[] getColumnWidths() {
        int[] widths = new int[this.columnCount];
        Arrays.fill(widths, TextualTable.MIN_COLUMN_WIDTH);
        for(Object[] row : this.rows) {
            for(int i = 0 ; i < this.columnCount ; i++)
                widths[i] = Math.max(widths[i], this.objectToString(row[i]).length());
        }
        return widths;
    }
    
    private int getTableWidth(int[] columnWidths) {
        int totalPadding = columnWidths.length * 2;
        int columnSeparators = this.separateColumns ? columnWidths.length + 1 : 0;
        int columnContents = 0;
        for(int columnWidth : columnWidths)
            columnContents += columnWidth;
        return totalPadding + columnSeparators + columnContents;
    }
    
    /** Convert object to string as it will be shown in the resulting table (no padding). */
    private String objectToString(Object obj) {
        if(obj instanceof String || obj instanceof Character)
            return obj.toString();
        else if(obj instanceof Double || obj instanceof Float)
            return String.format(this.getFloatFormatstring(), obj).replace(',', '.');
        else if(obj instanceof Integer || obj instanceof Long || obj instanceof Short || obj instanceof Byte)
            return String.format("%d", obj);
        else
            throw new RuntimeException("Usupported data type (object " + obj.toString() + ")");
    }
    
    /** Convert object to string as it will be shown in the resulting table and with requested padding. */
    private String objectToString(Object obj, int width) {
        String ret = this.objectToString(obj);
        while(ret.length() < width)
            ret = " ".concat(ret);
        return ret;
    }
    
    private void appendNTimes(StringBuilder sb, int n, String what) {
        for(int i = 0 ; i < n ; i++)
            sb.append(what);
    }
}
