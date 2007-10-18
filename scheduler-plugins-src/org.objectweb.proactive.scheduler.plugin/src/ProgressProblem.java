import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


public class ProgressProblem {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new RowLayout());

        // Text creation
        final Text text = new Text(shell, SWT.BORDER);

        // Button creation
        Button ok = new Button(shell, SWT.PUSH);
        ok.setText("    Delete    ");

        // Table creation
        final Table table = new Table(shell, SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        for (int i = 0; i < 3; i++) {
            new TableColumn(table, SWT.NONE);
        }
        table.getColumn(0).setText("Task");
        table.getColumn(1).setText("Progress");
        for (int i = 0; i < 10; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText("Task " + i);
            ProgressBar bar = new ProgressBar(table, SWT.NONE);
            bar.setSelection(i * 10);
            TableEditor editor = new TableEditor(table);
            editor.grabHorizontal = editor.grabVertical = true;
            editor.setEditor(bar, item, 1);
            item.setData("bar", bar);
            item.setData("editor", editor);
        }
        table.getColumn(0).pack();
        table.getColumn(1).setWidth(128);

        // Button listener
        ok.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    int i = new Integer(text.getText());
                    ((ProgressBar) (table.getItem(i).getData("bar"))).dispose();
                    ((TableEditor) (table.getItem(i).getData("editor"))).layout();
                    ((TableEditor) (table.getItem(i).getData("editor"))).dispose();
                    ((TableEditor) (table.getItem(i).getData("editor"))).layout();
                    //table.remove(i);
                    table.layout(true);

                    /* ???????????????????????????????????? */
                    /* ???????????????????????????????????? */
                    /* ???????????????????????????????????? */
                    /* ???????????????????????????????????? */
                }
            });

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
