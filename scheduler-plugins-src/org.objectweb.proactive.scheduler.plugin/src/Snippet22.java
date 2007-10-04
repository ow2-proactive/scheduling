import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Snippet22 {
    public static void main(String[] args) {
        Matcher m = Pattern.compile("ab").matcher("ab");
        System.out.println(m.group());

        //		Display display = new Display();
        //		Shell shell = new Shell(display);
        //		shell.setLayout(new FillLayout());
        //		Table table = new Table(shell, SWT.BORDER);
        //		table.setHeaderVisible(true);
        //		table.setLinesVisible(true);
        //		for (int i = 0; i < 2; i++) {
        //			new TableColumn(table, SWT.NONE);
        //		}
        //		table.getColumn(0).setText("Task");
        //		table.getColumn(1).setText("Progress");
        //		for (int i = 0; i < 40; i++) {
        //			TableItem item = new TableItem(table, SWT.NONE);
        //			item.setText("Task " + i);
        //			if (i % 5 == 0) {
        //				ProgressBar bar = new ProgressBar(table, SWT.NONE);
        //				bar.setMaximum(2*i);
        //				bar.setSelection(i);
        //				TableEditor editor = new TableEditor(table);
        //				editor.grabHorizontal = editor.grabVertical = true;
        //				editor.setEditor(bar, item, 1);
        //			}
        //		}
        //		table.getColumn(0).pack();
        //		table.getColumn(1).setWidth(128);
        //		shell.pack();
        //		shell.open();
        //		while (!shell.isDisposed()) {
        //			if (!display.readAndDispatch()) {
        //				display.sleep();
        //			}
        //		}

        // Display display = new Display();
        // Shell shell = new Shell(display);
        // shell.setLayout(new FillLayout());
        // final Table table = new Table(shell, SWT.BORDER);
        // table.setHeaderVisible(true);
        // final TableColumn column1 = new TableColumn(table, SWT.NONE);
        // column1.setText("Column 1");
        // final TableColumn column2 = new TableColumn(table, SWT.NONE);
        // column2.setText("Column 2");
        //
        // TableItem item = new TableItem(table, SWT.NONE);
        // item.setText(new String[] { "a", "3" });
        // item.setData(new IntWrapper(1));
        // item.setData("bool", new BooleanWrapper(true));
        //
        // item = new TableItem(table, SWT.NONE);
        // item.setText(new String[] { "b", "2" });
        // item.setData(new IntWrapper(2));
        // item.setData("bool", new BooleanWrapper(false));
        //
        // item = new TableItem(table, SWT.NONE);
        // item.setText(new String[] { "c", "1" });
        // item.setData(new IntWrapper(3));
        // item.setData("bool", new BooleanWrapper(true));
        //
        // column1.setWidth(100);
        // column2.setWidth(100);
        // Listener sortListener = new Listener() {
        // public void handleEvent(Event e) {
        // TableItem[] items = table.getItems();
        // Collator collator = Collator.getInstance(Locale.getDefault());
        // TableColumn column = (TableColumn) e.widget;
        // int index = column == column1 ? 0 : 1;
        // for (int i = 1; i < items.length; i++) {
        // String value1 = items[i].getText(index);
        // for (int j = 0; j < i; j++) {
        // String value2 = items[j].getText(index);
        // if (collator.compare(value1, value2) < 0) {
        // String[] values = { items[i].getText(0), items[i].getText(1) };
        // items[i].dispose();
        // TableItem item = new TableItem(table, SWT.NONE, j);
        // item.setText(values);
        // items = table.getItems();
        // break;
        // }
        // }
        // }
        // table.setSortColumn(column);
        // }
        // };
        // column1.addListener(SWT.Selection, sortListener);
        // column2.addListener(SWT.Selection, sortListener);
        // table.setSortColumn(column1);
        // table.setSortDirection(SWT.UP);
        // shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, 300);
        // shell.open();
        // for (TableItem it : table.getItems()) {
        // System.out.println(it.getData().getClass() + " ==> " + it.getData());
        // System.out.println(it.getData("boola").getClass() + " ==> " +
        // it.getData("bool"));
        // }
        // while (!shell.isDisposed()) {
        // if (!display.readAndDispatch())
        // display.sleep();
        // }
        // display.dispose();
    }
}
