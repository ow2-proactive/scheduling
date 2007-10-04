import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class ComplexGridLayoutExample {
    static Display display;
    static Shell shell;
    static Text dogName;
    static Combo combo;
    static Canvas canvas;
    static Image img;
    static List categoriList;
    static Text name;
    static Text phone;

    public static void main(String[] args) {
        display = new Display();
        shell = new Shell(display);
        shell.setText("Dog Show Entry");

        // GridLayout avec 3 colonnes
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        shell.setLayout(gridLayout);

        // un label tout simple sur une colonne une ligne
        new Label(shell, SWT.NULL).setText("Dog's Name:");

        // un textfield sur une ligne deux colonne qui s'alonge en horizontal
        dogName = new Text(shell, SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 2;
        dogName.setLayoutData(gridData);

        // un label tout simple sur une colonne une ligne
        new Label(shell, SWT.NULL).setText("Breed:");

        // un combo sur une ligne une colonne mais qui s'alonge en horizontal
        combo = new Combo(shell, SWT.NULL);
        combo.setItems(new String[] { "Collie", "Pitbull", "Poodle", "Scottie" });
        combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        // un label qui est aligné au centre sur une ligne une colonne
        Label label = new Label(shell, SWT.NULL);
        label.setText("Categories");
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

        // un label tout simple sur une colonne une ligne
        new Label(shell, SWT.NULL).setText("Photo:");

        canvas = new Canvas(shell, SWT.BORDER);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = 80;
        gridData.heightHint = 80;
        gridData.verticalSpan = 3;
        canvas.setLayoutData(gridData);
        canvas.addPaintListener(new PaintListener() {
                public void paintControl(final PaintEvent event) {
                    System.out.println(".paintControl()");
                    if (img != null) {
                        event.gc.drawImage(img, 0, 0);
                    }
                }
            });

        categoriList = new List(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        categoriList.setItems(new String[] {
                "Best of Breed", "Prettiest Female", "Handsomest Male",
                "Best Dressed", "Fluffiest Ears", "Most Colors",
                "Best Performer", "Loudest Bark", "Best Behaved",
                "Prettiest Eyes", "Most Hair", "Longest Tail", "Cutest Trick"
            });
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
                GridData.VERTICAL_ALIGN_FILL);
        gridData.verticalSpan = 4;
        int listHeight = categoriList.getItemHeight() * 12;
        Rectangle trim = categoriList.computeTrim(0, 0, 0, listHeight);
        gridData.heightHint = trim.height;
        categoriList.setLayoutData(gridData);

        Button browse = new Button(shell, SWT.PUSH);
        browse.setText("Browse...");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalIndent = 5;
        browse.setLayoutData(gridData);
        browse.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    String fileName = new FileDialog(shell).open();
                    if (fileName != null) {
                        img = new Image(display, fileName);
                    }
                }
            });

        Button delete = new Button(shell, SWT.PUSH);
        delete.setText("Delete");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
                GridData.VERTICAL_ALIGN_BEGINNING);
        gridData.horizontalIndent = 5;
        delete.setLayoutData(gridData);
        delete.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    if (img != null) {
                        img.dispose();
                        img = null;
                        canvas.redraw();
                    }
                }
            });

        Group ownerInfo = new Group(shell, SWT.NULL);
        ownerInfo.setText("Owner Info");
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        ownerInfo.setLayout(gridLayout);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 2;
        ownerInfo.setLayoutData(gridData);

        new Label(ownerInfo, SWT.NULL).setText("Name:");
        name = new Text(ownerInfo, SWT.SINGLE | SWT.BORDER);
        name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(ownerInfo, SWT.NULL).setText("Phone:");
        phone = new Text(ownerInfo, SWT.SINGLE | SWT.BORDER);
        phone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button enter = new Button(shell, SWT.PUSH);
        enter.setText("Enter");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 3;
        enter.setLayoutData(gridData);
        enter.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    System.out.println("\nDog Name: " + dogName.getText());
                    System.out.println("Dog Breed: " + combo.getText());
                    System.out.println("Owner Name: " + name.getText());
                    System.out.println("Owner Phone: " + phone.getText());
                    System.out.println("Categories:");
                    String[] cats = categoriList.getSelection();
                    for (int i = 0; i < cats.length; i++) {
                        System.out.println("\t" + cats[i]);
                    }
                }
            });

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        if (img != null) {
            img.dispose();
        }
    }
}
