package be.ibridge.kettle.swttest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
/*
 * FillLayout  自动填充布局
 * 
 * 当为容器attach这一布局时，在容器中的widget会随着容器的resize全部填满整个容器
 */
public class LayoutExample {
    Display d;
    Shell s;
    LayoutExample( )    {
        d = new Display( );
        s = new Shell(d);
        s.setSize(500,500);
        s.setText("A Shell Menu Example");
        s.setLayout(new FillLayout( ));
        final Button b = new Button(s,SWT.BORDER);
        b.setText("BT1");
        final Button b1 = new Button(s,SWT.BORDER);
        b1.setText("BT2");
        final Button b2 = new Button(s,SWT.BORDER);
        b2.setText("BT3");
        s.open( );
        while(!s.isDisposed( )){
            if(!d.readAndDispatch( ))
                d.sleep( );
        }
        d.dispose( );
    }
    public static void main(String[] args) {
		LayoutExample le = new LayoutExample();
	}
}


