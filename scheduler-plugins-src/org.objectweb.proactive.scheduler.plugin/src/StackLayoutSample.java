/*******************************************************************************
 * Copyright (c) 1998, 2004 Jackwind Li Guojie All right reserved.
 *
 * Created on Jan 30, 2004 10:59:14 PM by JACK $Id: StackLayoutSample.java,v 1.1 2007/06/25 15:20:31 jfradj Exp $
 *
 * visit: http://www.asprise.com/swt
 ******************************************************************************/
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public class StackLayoutSample {
    Display display = new Display();
    Shell shell = new Shell(display);
    final Button[] buttons = new Button[3];

    public StackLayoutSample() {
        final StackLayout stackLayout = new StackLayout();
        shell.setLayout(stackLayout);

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button(shell, SWT.NULL);
            buttons[i].setText("Button #" + i);

            buttons[i].addSelectionListener(new SelectionListener() {
                    public void widgetSelected(SelectionEvent e) {
                        // Flip to next button.
                        Button nextButton = null;
                        for (int i = 0; i < buttons.length; i++) {
                            if (buttons[i] == e.widget) {
                                if (i == (buttons.length - 1)) {
                                    nextButton = buttons[0];
                                } else {
                                    nextButton = buttons[i + 1];
                                }
                            }
                        }
                        stackLayout.topControl = nextButton;
                        shell.layout();
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {
                    }
                });
        }

        // Initial
        stackLayout.topControl = buttons[0];

        shell.setSize(200, 100);
        shell.open();

        // Set up the event loop.
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                // If no more entries in event queue
                display.sleep();
            }
        }

        display.dispose();
    }

    public static void main(String[] args) {
        new StackLayoutSample();
    }
}
