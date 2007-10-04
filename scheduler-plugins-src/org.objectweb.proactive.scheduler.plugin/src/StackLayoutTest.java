import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
// Send questions, comments, bug reports, etc. to the authors:
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;


public class StackLayoutTest {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        StackLayout layout = new StackLayout();
        shell.setLayout(layout);
        StackLayoutSelectionAdapter adapter = new StackLayoutSelectionAdapter(shell,
                layout);
        Button one = new Button(shell, SWT.PUSH);
        one.setText("one");
        one.addSelectionListener(adapter);
        Button two = new Button(shell, SWT.PUSH);
        two.setText("two");
        two.addSelectionListener(adapter);
        Button three = new Button(shell, SWT.PUSH);
        three.setText("three");
        three.addSelectionListener(adapter);
        layout.topControl = one;
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}


class StackLayoutSelectionAdapter extends SelectionAdapter {
    Shell shell;
    StackLayout layout;

    public StackLayoutSelectionAdapter(Shell shell, StackLayout layout) {
        this.shell = shell;
        this.layout = layout;
    }

    @Override
    public void widgetSelected(SelectionEvent event) {
        Control control = layout.topControl;
        Control[] children = shell.getChildren();
        int i = 0;
        for (int n = children.length; i < n; i++) {
            Control child = children[i];
            if (child == control) {
                break;
            }
        }
        ++i;
        if (i >= children.length) {
            i = 0;
        }
        layout.topControl = children[i];
        shell.layout();
    }
}
