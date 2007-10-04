import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;


public class FormLayoutFormAttachment {

    /**
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        FormLayout layout = new FormLayout();
        //		layout.marginHeight = 5;
        //		layout.marginWidth = 10;
        //		shell.setLayout(layout);
        //		Button button = new Button(shell, SWT.PUSH);
        //		button.setText("Button");
        //		FormData data = new FormData();
        ////		data.height = 50;
        //		data.right = new FormAttachment(100, -10);
        //		data.left = new FormAttachment(0, 10);
        //		data.top = new FormAttachment(0, 10);
        //		button.setLayoutData(data);
        //
        //		Button button2 = new Button(shell, SWT.PUSH);
        //		button2.setText("Button 2");
        //		data = new FormData();
        //		button2.setLayoutData(data);
        //		data.bottom = new FormAttachment(100, 0);
        //		data.top = new FormAttachment(button, 5);
        //		data.left = new FormAttachment(button, 0, SWT.LEFT);
        //		data.right = new FormAttachment(button, 0, SWT.RIGHT);
        shell.setLayout(layout);
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(new GridLayout());
        Label label = new Label(comp, SWT.CENTER);
        label.setText("salut");
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        label.setLayoutData(gd);
        FormData data = new FormData();
        data.bottom = new FormAttachment(100, -10);
        data.left = new FormAttachment(0, 10);
        data.top = new FormAttachment(0, 10);
        comp.setLayoutData(data);

        Button button2 = new Button(shell, SWT.PUSH);
        button2.setText("Button 2");
        data = new FormData();
        button2.setLayoutData(data);
        data.bottom = new FormAttachment(100, -10);
        data.top = new FormAttachment(0, 10);
        data.left = new FormAttachment(comp, 10);
        data.right = new FormAttachment(100, -10);

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
