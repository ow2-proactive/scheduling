import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


/**
 * Creates a tabbed display with four tabs, and a few controls on each page
 */
public class TabComplex {
    public static final String IMAGE_PATH = "images" +
        System.getProperty("file.separator");
    private Image circle;
    private Image square;
    private Image triangle;
    private Image star;

    /**
     * Runs the application
     */
    public void run() {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("Complex Tabs");
        createContents(shell);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    /**
     * Creates the contents
     *
     * @param shell
     *            the parent shell
     */
    private void createContents(Shell shell) {
        // Create the containing tab folder
        final TabFolder tabFolder = new TabFolder(shell, SWT.BOTTOM);

        // Create each tab and set its text, tool tip text,
        // image, and control
        TabItem one = new TabItem(tabFolder, SWT.NONE);
        one.setText("one");
        one.setToolTipText("This is tab one");
        one.setImage(circle);
        one.setControl(getTabOneControl(tabFolder));

        TabItem two = new TabItem(tabFolder, SWT.NONE);
        two.setText("two");
        two.setToolTipText("This is tab two");
        two.setImage(square);
        two.setControl(getTabTwoControl(tabFolder));

        TabItem three = new TabItem(tabFolder, SWT.NONE);
        three.setText("three");
        three.setToolTipText("This is tab three");
        three.setImage(triangle);
        three.setControl(getTabThreeControl(tabFolder));

        TabItem four = new TabItem(tabFolder, SWT.NONE);
        four.setText("four");
        four.setToolTipText("This is tab four");
        four.setImage(star);

        // Select the third tab (index is zero-based)
        tabFolder.setSelection(2);

        // Add an event listener to write the selected tab to stdout
        tabFolder.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent event) {
                    System.out.println(tabFolder.getSelection()[0].getText() +
                        " selected");
                }
            });
    }

    /**
     * Gets the control for tab one
     *
     * @param tabFolder
     *            the parent tab folder
     * @return Control
     */
    private Control getTabOneControl(TabFolder tabFolder) {
        // Create a composite and add four buttons to it
        Composite composite = new Composite(tabFolder, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.VERTICAL));
        new Button(composite, SWT.PUSH).setText("Button one");
        new Button(composite, SWT.PUSH).setText("Button two");
        new Button(composite, SWT.PUSH).setText("Button three");
        new Button(composite, SWT.PUSH).setText("Button four");
        return composite;
    }

    /**
     * Gets the control for tab two
     *
     * @param tabFolder
     *            the parent tab folder
     * @return Control
     */
    private Control getTabTwoControl(TabFolder tabFolder) {
        // Create a multi-line text field
        return new Text(tabFolder, SWT.BORDER | SWT.MULTI | SWT.WRAP);
    }

    /**
     * Gets the control for tab three
     *
     * @param tabFolder
     *            the parent tab folder
     * @return Control
     */
    private Control getTabThreeControl(TabFolder tabFolder) {
        // Create some labels and text fields
        Composite composite = new Composite(tabFolder, SWT.NONE);
        composite.setLayout(new RowLayout());
        new Label(composite, SWT.LEFT).setText("Label One:");
        new Text(composite, SWT.BORDER);
        new Label(composite, SWT.RIGHT).setText("Label Two:");
        new Text(composite, SWT.BORDER);
        return composite;
    }

    /**
     * The entry point for the application
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new TabComplex().run();
    }
}
