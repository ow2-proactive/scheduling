/*
 * Table example snippet: sort a table by column
 *
 * For a list of all SWT example snippets see
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */
import java.text.Collator;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public class Snippet2 {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        final Table table = new Table(shell, SWT.BORDER);
        table.setHeaderVisible(true);
        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText("Column 1");
        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText("Column 2");
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "a", "3" });
        item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "b", "2" });
        item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "c", "1" });
        column1.pack();
        column2.pack();
        column1.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event e) {
                    // sort column 1
                    TableItem[] items = table.getItems();
                    Collator collator = Collator.getInstance(Locale.getDefault());
                    for (int i = 1; i < items.length; i++) {
                        String value1 = items[i].getText(0);
                        for (int j = 0; j < i; j++) {
                            String value2 = items[j].getText(0);
                            if (collator.compare(value1, value2) < 0) {
                                String[] values = {
                                        items[i].getText(0), items[i].getText(1)
                                    };
                                items[i].dispose();
                                TableItem item = new TableItem(table, SWT.NONE,
                                        j);
                                item.setText(values);
                                items = table.getItems();
                                break;
                            }
                        }
                    }
                }
            });
        column2.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event e) {
                    // sort column 2
                    TableItem[] items = table.getItems();
                    Collator collator = Collator.getInstance(Locale.getDefault());
                    for (int i = 1; i < items.length; i++) {
                        String value1 = items[i].getText(1);
                        for (int j = 0; j < i; j++) {
                            String value2 = items[j].getText(1);
                            if (collator.compare(value1, value2) < 0) {
                                String[] values = {
                                        items[i].getText(0), items[i].getText(1)
                                    };
                                items[i].dispose();
                                TableItem item = new TableItem(table, SWT.NONE,
                                        j);
                                item.setText(values);
                                items = table.getItems();
                                break;
                            }
                        }
                    }
                }
            });
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
