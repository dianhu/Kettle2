 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
/*
 * Created on 18-mei-2003
 *
 */

package be.ibridge.kettle.trans.step.xmlinput;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.dialog.EnterNumberDialog;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.TransPreviewFactory;
import be.ibridge.kettle.trans.dialog.TransPreviewProgressDialog;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class XMLInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wContentTab, wFieldsTab;

	private Composite    wFileComp, wContentComp, wFieldsComp;
	private FormData     fdFileComp, fdContentComp, fdFieldsComp;

	private Label        wlFilename;
	private Button       wbbFilename; // Browse: add file or directory
	private Button       wbvFilename; // Variable
	private Button       wbdFilename; // Delete
	private Button       wbeFilename; // Edit
	private Button       wbaFilename; // Add or change
	private Text         wFilename;
	private FormData     fdlFilename, fdbFilename, fdbvFilename, fdbdFilename, fdbeFilename, fdbaFilename, fdFilename;

	private Label        wlFilenameList;
	private TableView    wFilenameList;
	private FormData     fdlFilenameList, fdFilenameList;

	private Label        wlFilemask;
	private Text         wFilemask;
	private FormData     fdlFilemask, fdFilemask;

	private Button       wbShowFiles;
	private FormData     fdbShowFiles;


	private Label        wlInclFilename;
	private Button       wInclFilename;
	private FormData     fdlInclFilename, fdInclFilename;

	private Label        wlInclFilenameField;
	private Text         wInclFilenameField;
	private FormData     fdlInclFilenameField, fdInclFilenameField;

	private Label        wlInclRownum;
	private Button       wInclRownum;
	private FormData     fdlInclRownum, fdRownum;

	private Label        wlInclRownumField;
	private Text         wInclRownumField;
	private FormData     fdlInclRownumField, fdInclRownumField;
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;

    private Label        wlPosition;
    private TableView    wPosition;
    private FormData     fdlPosition, fdPosition;
    
	private TableView    wFields;
	private FormData     fdFields;

	private XMLInputMeta input;
	
	private static final String STRING_PREVIEW_ROWS    = "  &Preview rows   "; 

	public static final int dateLengths[] = new int[]
		{
			23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
		}
		;

	public XMLInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(XMLInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed         = input.hasChanged();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Text file input");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText("Step name ");
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top  = new FormAttachment(0, margin);
		fdlStepname.right= new FormAttachment(middle, -margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		//////////////////////////
		// START OF FILE TAB   ///
		//////////////////////////
		wFileTab=new CTabItem(wTabFolder, SWT.NONE);
		wFileTab.setText("File");
		
		wFileComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);

		// Filename line
		wlFilename=new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText("File or directory ");
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(0, 0);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbFilename);
		wbbFilename.setText("&Browse");
		wbbFilename.setToolTipText("Browse for a file or directory & add to the list");
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(0, 0);
		wbbFilename.setLayoutData(fdbFilename);

		wbvFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbvFilename);
		wbvFilename.setText("&Variable");
		wbvFilename.setToolTipText("Insert a variable in the filename or directory");
		fdbvFilename=new FormData();
		fdbvFilename.right= new FormAttachment(wbbFilename, -margin);
		fdbvFilename.top  = new FormAttachment(0, 0);
		wbvFilename.setLayoutData(fdbvFilename);

		wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbaFilename);
		wbaFilename.setText("&Add");
		wbaFilename.setToolTipText("Add this entry to the list of files & directories.");
		fdbaFilename=new FormData();
		fdbaFilename.right= new FormAttachment(wbvFilename, -margin);
		fdbaFilename.top  = new FormAttachment(0, 0);
		wbaFilename.setLayoutData(fdbaFilename);

		wFilename=new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbaFilename, -margin);
		fdFilename.top  = new FormAttachment(0, 0);
		wFilename.setLayoutData(fdFilename);

		wlFilemask=new Label(wFileComp, SWT.RIGHT);
		wlFilemask.setText("Regular Expression ");
 		props.setLook(wlFilemask);
		fdlFilemask=new FormData();
		fdlFilemask.left = new FormAttachment(0, 0);
		fdlFilemask.top  = new FormAttachment(wFilename, margin);
		fdlFilemask.right= new FormAttachment(middle, -margin);
		wlFilemask.setLayoutData(fdlFilemask);
		wFilemask=new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilemask);
		wFilemask.addModifyListener(lsMod);
		fdFilemask=new FormData();
		fdFilemask.left = new FormAttachment(middle, 0);
		fdFilemask.top  = new FormAttachment(wFilename, margin);
		fdFilemask.right= new FormAttachment(100, 0);
		wFilemask.setLayoutData(fdFilemask);

		// Filename list line
		wlFilenameList=new Label(wFileComp, SWT.RIGHT);
		wlFilenameList.setText("Selected files: ");
 		props.setLook(wlFilenameList);
		fdlFilenameList=new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top  = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right= new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdFilename);
		wbdFilename.setText("&Delete");
		wbdFilename.setToolTipText("Delete the selected entries from the list of files.");
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbeFilename);
		wbeFilename.setText("&Edit");
		wbeFilename.setToolTipText("Edit the selected file and remove from the list.");
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText("&Show filename(s)...");
		fdbShowFiles=new FormData();
		fdbShowFiles.left   = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo=new ColumnInfo[2];
		colinfo[ 0]=new ColumnInfo("File/Directory",  ColumnInfo.COLUMN_TYPE_TEXT,    false);
		colinfo[ 1]=new ColumnInfo("Wildcard",        ColumnInfo.COLUMN_TYPE_TEXT,    false );
		
		colinfo[ 1].setToolTip("Enter a regular expression here and a directory in the first column.");
		
		
		wFilenameList = new TableView(wFileComp, 
						      SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, 
						      colinfo, 
						      2,  
						      lsMod,
							  props
						      );
 		props.setLook(wFilenameList);
		fdFilenameList=new FormData();
		fdFilenameList.left   = new FormAttachment(middle, 0);
		fdFilenameList.right  = new FormAttachment(wbdFilename, -margin);
		fdFilenameList.top    = new FormAttachment(wFilemask, margin);
		fdFilenameList.bottom = new FormAttachment(wbShowFiles, -margin);
		wFilenameList.setLayoutData(fdFilenameList);

	
		fdFileComp=new FormData();
		fdFileComp.left  = new FormAttachment(0, 0);
		fdFileComp.top   = new FormAttachment(0, 0);
		fdFileComp.right = new FormAttachment(100, 0);
		fdFileComp.bottom= new FormAttachment(100, 0);
		wFileComp.setLayoutData(fdFileComp);
	
		wFileComp.layout();
		wFileTab.setControl(wFileComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF FILE TAB
		/////////////////////////////////////////////////////////////


		//////////////////////////
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText("Content");

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);

		wlInclFilename=new Label(wContentComp, SWT.RIGHT);
		wlInclFilename.setText("Include filename in output? ");
 		props.setLook(wlInclFilename);
		fdlInclFilename=new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top  = new FormAttachment(0, 0);
		fdlInclFilename.right= new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wInclFilename);
		wInclFilename.setToolTipText("Check this to add a field (String) containing the filename.");
		fdInclFilename=new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top  = new FormAttachment(0, 0);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField=new Label(wContentComp, SWT.LEFT);
		wlInclFilenameField.setText("Filename fieldname ");
 		props.setLook(wlInclFilenameField);
		fdlInclFilenameField=new FormData();
		fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
		fdlInclFilenameField.top  = new FormAttachment(0, 0);
		wlInclFilenameField.setLayoutData(fdlInclFilenameField);
		wInclFilenameField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclFilenameField);
		wInclFilenameField.addModifyListener(lsMod);
		fdInclFilenameField=new FormData();
		fdInclFilenameField.left = new FormAttachment(wlInclFilenameField, margin);
		fdInclFilenameField.top  = new FormAttachment(0, 0);
		fdInclFilenameField.right= new FormAttachment(100, 0);
		wInclFilenameField.setLayoutData(fdInclFilenameField);

		wlInclRownum=new Label(wContentComp, SWT.RIGHT);
		wlInclRownum.setText("Rownum in output? ");
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclFilenameField, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText("Check this to add a field (String) containing the filename.");
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclFilenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField=new Label(wContentComp, SWT.RIGHT);
		wlInclRownumField.setText("Rownum fieldname ");
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);

		wlLimit=new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText("Limit ");
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top  = new FormAttachment(wInclRownumField, margin);
		fdlLimit.right= new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wInclRownumField, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

        wlPosition=new Label(wContentComp, SWT.RIGHT);
        wlPosition.setText("Location ");
        props.setLook(wlPosition);
        fdlPosition=new FormData();
        fdlPosition.left = new FormAttachment(0, 0);
        fdlPosition.top  = new FormAttachment(wLimit, margin);
        fdlPosition.right= new FormAttachment(middle, -margin);
        wlPosition.setLayoutData(fdlPosition);
        
        ColumnInfo[] locationColumns = new ColumnInfo[] 
        { 
            new ColumnInfo("Elements", ColumnInfo.COLUMN_TYPE_TEXT, false)
        };
        
        int nrElements = input.getInputPosition()!=null ? input.getInputPosition().length : 0;

        wPosition = new TableView(wContentComp, SWT.FULL_SELECTION | SWT.MULTI, locationColumns, nrElements, lsMod, props);
        wPosition.addModifyListener(lsMod);
        fdPosition=new FormData();
        fdPosition.left   = new FormAttachment(middle, 0);
        fdPosition.top    = new FormAttachment(wLimit, margin);
        fdPosition.bottom = new FormAttachment(100, -50);
        fdPosition.right  = new FormAttachment(100, 0);
        wPosition.setLayoutData(fdPosition);

		fdContentComp = new FormData();
		fdContentComp.left  = new FormAttachment(0, 0);
		fdContentComp.top   = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom= new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);


		// ///////////////////////////////////////////////////////////
		// / END OF CONTENT TAB
		// ///////////////////////////////////////////////////////////


		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText("Fields");
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);
		
		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(" &Get fields ");
		fdGet=new FormData();
		fdGet.left=new FormAttachment(50, 0);
		fdGet.bottom =new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);

		final int FieldsRows=input.getInputFields().length;
		
		// Prepare a list of possible formats...
		String dats[] = Const.dateFormats;
		String nums[] = Const.numberFormats;
		int totsize = dats.length + nums.length;
		String formats[] = new String[totsize];
		for (int x=0;x<dats.length;x++) formats[x] = dats[x];
		for (int x=0;x<nums.length;x++) formats[dats.length+x] = nums[x];
		
		
		ColumnInfo[] colinf=new ColumnInfo[]
            {
			 new ColumnInfo("Name",       ColumnInfo.COLUMN_TYPE_TEXT,    false),
			 new ColumnInfo("Type",       ColumnInfo.COLUMN_TYPE_CCOMBO,  Value.getTypes(), true ),
			 new ColumnInfo("Format",     ColumnInfo.COLUMN_TYPE_CCOMBO,  formats),
			 new ColumnInfo("Length",     ColumnInfo.COLUMN_TYPE_TEXT,    false),
			 new ColumnInfo("Precision",  ColumnInfo.COLUMN_TYPE_TEXT,    false),
			 new ColumnInfo("Currency",   ColumnInfo.COLUMN_TYPE_TEXT,    false),
			 new ColumnInfo("Decimal",    ColumnInfo.COLUMN_TYPE_TEXT,    false),
			 new ColumnInfo("Group",      ColumnInfo.COLUMN_TYPE_TEXT,    false),
			 new ColumnInfo("Trim type",  ColumnInfo.COLUMN_TYPE_CCOMBO,  XMLInputField.trimTypeDesc, true ),
			 new ColumnInfo("Repeat",     ColumnInfo.COLUMN_TYPE_CCOMBO,  new String[] { "Y", "N" }, true ),
             new ColumnInfo("Position",   ColumnInfo.COLUMN_TYPE_TEXT,    false),
            };
		
		wFields=new TableView(wFieldsComp, 
						      SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
		
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(STRING_PREVIEW_ROWS);
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();      } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener     (SWT.Selection, lsOK     );
		wGet.addListener    (SWT.Selection, lsGet    );
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wInclRownumField.addSelectionListener( lsDef );
		wInclFilenameField.addSelectionListener( lsDef );

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFilenameList.add(new String[] { wFilename.getText(), wFilemask.getText() } );
				wFilename.setText("");
				wFilemask.setText("");
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
                wFilenameList.optWidth(true);
			}
		};
		wbaFilename.addSelectionListener(selA);
		wFilename.addSelectionListener(selA);
		
		// Delete files from the list of files...
		wbdFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx[] = wFilenameList.getSelectionIndices();
				wFilenameList.remove(idx);
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
			}
		});

		// Edit the selected file & remove from the list...
		wbeFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx = wFilenameList.getSelectionIndex();
				if (idx>=0)
				{
					String string[] = wFilenameList.getItem(idx);
					wFilename.setText(string[0]);
					wFilemask.setText(string[1]);
					wFilenameList.remove(idx);
				}
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
			}
		});

		// Show the files that are selected at this time...
		wbShowFiles.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
                    try
                    {
    					XMLInputMeta tfii = new XMLInputMeta();
    					getInfo(tfii);
    					String files[] = tfii.getFiles();
    					if (files!=null && files.length>0)
    					{
    						EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, files, "Files read", "Files read:");
    						esd.setViewOnly();
    						esd.open();
    					}
    					else
    					{
    						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    						mb.setMessage("No files found!  Please check the filename/directory and regular expression options.");
    						mb.setText("ERROR");
    						mb.open(); 
    					}
                    }
                    catch(KettleException ex)
                    {
                        new ErrorDialog(shell, props, "Error parsing input data", "An error occurred while parsing the input data on this dialog", ex);
                    }
				}
			}
		);
		// Enable/disable the right fields to allow a filename to be added to each row...
		wInclFilename.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeFilename();
				}
			}
		);
		
		// Enable/disable the right fields to allow a row number to be added to each row...
		wInclRownum.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeRownum();
				}
			}
		);

		// Whenever something changes, set the tooltip to the expanded version of the filename:
		wFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename.setToolTipText(Const.replEnv( wFilename.getText() ) );
				}
			}
		);
		
		// Listen to the Variable... button
		wbvFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					Properties sp = System.getProperties();
					Enumeration keys = sp.keys();
					int size = sp.values().size();
					String key[] = new String[size];
					String val[] = new String[size];
					String str[] = new String[size];
					int i=0;
					while (keys.hasMoreElements())
					{
						key[i] = (String)keys.nextElement();
						val[i] = sp.getProperty(key[i]);
						str[i] = key[i]+"  ["+val[i]+"]";
						i++;
					}
					
					EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, str, "Select an Environment Variable", "Select an Environment Variable");
					if (esd.open()!=null)
					{
						int nr = esd.getSelectionNr();
						wFilename.insert("%%"+key[nr]+"%%");
						wFilename.setToolTipText(Const.replEnv( wFilename.getText() ) );
					}
				}
				
			}
		);


		// Listen to the Browse... button
		wbbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					if (wFilemask.getText()!=null && wFilemask.getText().length()>0) // A mask: a directory!
					{
						DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
						if (wFilename.getText()!=null)
						{
							String fpath = Const.replEnv(wFilename.getText());
							dialog.setFilterPath( fpath );
						}
						
						if (dialog.open()!=null)
						{
							String str= dialog.getFilterPath();
							wFilename.setText(str);
						}
					}
					else
					{
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setFilterExtensions(new String[] {"*.xml;*.XML", "*"});
						if (wFilename.getText()!=null)
						{
							String fname = Const.replEnv(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {"XML files", "All files"});
						
						if (dialog.open()!=null)
						{
							String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
							wFilename.setText(str);
						}
					}
				}
			}
		);
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		input.setChanged(changed);
		wFields.optWidth(true);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	public void setMultiple()
	{
		/*
		wlFilemask.setEnabled(wMultiple.getSelection());
		wFilemask.setEnabled(wMultiple.getSelection());
		wlFilename.setText(wMultiple.getSelection()?"Directory":"Filename ");
		*/
	}
	
	public void setIncludeFilename()
	{
		wlInclFilenameField.setEnabled(wInclFilename.getSelection());
		wInclFilenameField.setEnabled(wInclFilename.getSelection());
	}

	public void setIncludeRownum()
	{
		wlInclRownumField.setEnabled(wInclRownum.getSelection());
		wInclRownumField.setEnabled(wInclRownum.getSelection());
	}

	/**
	 * Read the data from the TextFileInputMeta object and show it in this dialog.
	 * 
	 * @param in The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(XMLInputMeta in)
	{
		if (in.getFileName() !=null) 
		{
			wFilenameList.removeAll();
			for (int i=0;i<in.getFileName().length;i++) 
			{
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i] } );
			}
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);
		}
		wInclFilename.setSelection(in.includeFilename());
		wInclRownum.setSelection(in.includeRowNumber());
		//wMultiple.setSelection(in.wildcard);
		if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		wLimit.setText(""+in.getRowLimit());
		
		log.logDebug(toString(), "getting fields info...");
		for (int i=0;i<in.getInputFields().length;i++)
		{
		    XMLInputField field = in.getInputFields()[i];
		    
            if (field!=null)
            {
    			TableItem item = wFields.table.getItem(i);
    			String name     = field.getName();
    			String type     = field.getTypeDesc();
    			String format   = field.getFormat();
    			String length   = ""+field.getLength();
    			String prec     = ""+field.getPrecision();
    			String curr     = field.getCurrencySymbol();
    			String group    = field.getGroupSymbol();
    			String decim    = field.getDecimalSymbol();
    			String trim     = field.getTrimTypeDesc();
    			String rep      = field.isRepeated()?"Y":"N";
    			
                if (name    !=null) item.setText( 1, name);
    			if (type    !=null) item.setText( 2, type    );
    			if (format  !=null) item.setText( 3, format  );
    			if (length  !=null && !"-1".equals(length  )) item.setText( 4, length  );
    			if (prec    !=null && !"-1".equals(prec    )) item.setText( 5, prec    );
    			if (curr    !=null) item.setText( 6, curr    );
    			if (decim   !=null) item.setText( 7, decim   );
    			if (group   !=null) item.setText( 8, group   );
    			if (trim    !=null) item.setText( 9, trim    );
    			if (rep     !=null) item.setText(10, rep     );
                
                item.setText(11, field.getFieldPositionsCode());
            }
		}
        
        for (int i=0;i<input.getInputPosition().length;i++)
        {
            TableItem item = wPosition.table.getItem(i);
            if (input.getInputPosition()[i]!=null) item.setText(1, input.getInputPosition()[i]);
        }
        
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

        wPosition.removeEmptyRows();
        wPosition.setRowNums();
        wPosition.optWidth(true);

		setMultiple();
		setIncludeFilename();
		setIncludeRownum();

		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
        try
        {
            getInfo(input);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, props, "Error parsing input data", "An error occurred while parsing the input data on this dialog", e);
        }
		dispose();
	}
	
	private void getInfo(XMLInputMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		// copy info to TextFileInputMeta class (input)
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		in.setFilenameField( wInclFilenameField.getText() );
		in.setRowNumberField( wInclRownumField.getText() );
				
		in.setIncludeFilename( wInclFilename.getSelection() );
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		
		int nrFiles     = wFilenameList.getItemCount();
		int nrFields    = wFields.nrNonEmpty();
        int nrPositions = wPosition.nrNonEmpty();
        
		in.allocate(nrFiles, nrFields, nrPositions);

		in.setFileName( wFilenameList.getItems(0) );
		in.setFileMask( wFilenameList.getItems(1) );

		for (int i=0;i<nrFields;i++)
		{
		    XMLInputField field = new XMLInputField();
		    
			TableItem item  = wFields.getNonEmpty(i);
            
			field.setName( item.getText(1) );
			field.setType( Value.getType(item.getText(2)) );
			field.setFormat( item.getText(3) );
			field.setLength( Const.toInt(item.getText(4), -1) );
			field.setPrecision( Const.toInt(item.getText(5), -1) );
			field.setCurrencySymbol( item.getText(6) );
			field.setDecimalSymbol( item.getText(7) );
			field.setGroupSymbol( item.getText(8) );
			field.setTrimType( XMLInputField.getTrimType(item.getText(9)) );
			field.setRepeated( "Y".equalsIgnoreCase(item.getText(10)) );		
			field.setFieldPosition( item.getText(11) );
            
			in.getInputFields()[i] = field;
		}		
        
        for (int i=0;i<nrPositions;i++)
        {
            TableItem item  = wPosition.getNonEmpty(i);
            in.getInputPosition()[i] = item.getText(1);
            // System.out.println("Input Position #"+i+" : "+input.getInputPosition());
        }
	}
	
	private void get()
	{
        try
        {
    		XMLInputMeta meta = new XMLInputMeta();
    		getInfo(meta);
            
            // OK, let's try to walk through the complete tree
            Row row = new Row(); // no fields found...
            
            // Keep the list of positions
            ArrayList path = new ArrayList(); // ArrayList of XMLInputFieldPosition
            
            for (int f=0;f<meta.getFiles().length;f++)
            {
                // Open the file...
                Node rootNode = XMLHandler.loadXMLFile(meta.getFiles()[f]);
                
                // Position to the repeating item
                for (int p=0;rootNode!=null && p<meta.getInputPosition().length-1;p++)
                {
                    rootNode = XMLHandler.getSubNode(rootNode, meta.getInputPosition()[p]);
                }
                
                if (rootNode==null)
                {
                    // Specified node not found: return!
                    return;
                }
                
                // Count the number of rootnodes
                String itemElement = meta.getInputPosition()[meta.getInputPosition().length-1];
                int nrItems = XMLHandler.countNodes(rootNode, itemElement);
                for (int i=0;i<nrItems;i++)
                {
                    Node itemNode = XMLHandler.getSubNodeByNr(rootNode, itemElement, i);
                    getValues(itemNode, row, path);
                }
            }
            
            // System.out.println("Values found: "+row);
            
            // add the values to the grid...
            for (int i=0;i<row.size();i++)
            {
                Value v = row.getValue(i);
                TableItem item = new TableItem(wFields.table, SWT.NONE);
                item.setText(1, v.getName());
                item.setText(2, v.getTypeDesc());
                item.setText(11, v.getOrigin());
            }
            
            wFields.removeEmptyRows();
            wFields.setRowNums();
            wFields.optWidth(true);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, props, "Error parsing input data", "An error occurred while parsing the input data on this dialog", e);
        }
	}
    
    /**
     * Get all the values defined in a Node
     * @param node The node to examine
     * @param row The 
     */
    private void getValues(Node node, Row row, ArrayList path) throws KettleException
    {
        String baseName = "";
        for (int p=0;p<path.size();p++) 
        {
            XMLInputFieldPosition pos = (XMLInputFieldPosition)path.get(p); 
            String elementName = pos.getName()+pos.getElementNr();
            if (!elementName.startsWith("#"))
            {
                baseName+= new Value("p", elementName).initcap().getString();
            }
        }
        
        // First check out the attributes...
        String attributes[] = XMLHandler.getNodeAttributes(node);
        if (attributes!=null)
        {
            for (int i=0;i<attributes.length;i++)
            {
                XMLInputFieldPosition attrPos = new XMLInputFieldPosition(attributes[i], XMLInputFieldPosition.XML_ATTRIBUTE);
                path.add(attrPos);
                   
                String attribute = new Value("a", attributes[i]).initcap().getString();
                String fieldName = baseName+attribute;
                
                // See if this fieldname already exists in Row...
                if (row.searchValueIndex(fieldName)<0)
                {
                    // Add the fieldname...
                    Value field = new Value(fieldName, Value.VALUE_TYPE_STRING);
                    
                    // Add the path to this attribute to the origin of the field...
                    String encoded = XMLInputFieldPosition.encodePath(path);
                    field.setOrigin(encoded);
                    
                    row.addValue(field);
                }
                
                path.remove(path.size()-1);
            }
        }
        
        // Then get the elements
        String elements[] = XMLHandler.getNodeElements(node);
        if (elements!=null)
        {
            for (int e=0;e<elements.length;e++)
            {
                // Count the number of occurences of this element...
                int occurences = XMLHandler.countNodes(node, elements[e]);
                for (int o=0;o<occurences;o++)
                {
                    Node itemNode = XMLHandler.getSubNodeByNr(node, elements[e], o);
                    XMLInputFieldPosition xmlPos = new XMLInputFieldPosition(elements[e], XMLInputFieldPosition.XML_ELEMENT, o+1);
                    
                    path.add(xmlPos);
                    getValues(itemNode, row, path);
                    path.remove(path.size()-1); // remove the last one again
                }
            }
        }
        else  // No child nodes left: this is a value we want to grab
        {
            int idxLast = path.size()-1;
            XMLInputFieldPosition last = (XMLInputFieldPosition) path.get(idxLast);
            path.remove(idxLast);
            
            if (path.size()>0)
            {
                String encoded = XMLInputFieldPosition.encodePath(path);
                if (row.searchValueIndex(baseName)<0)
                {
                    Value value = new Value(baseName, Value.VALUE_TYPE_STRING);
                    value.setOrigin(encoded);
                    
                    row.addValue(value);
                }
            }
            
            path.add(last);
        }
    }
	
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the XML input step
            XMLInputMeta oneMeta = new XMLInputMeta();
            getInfo(oneMeta);

            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props, 500, "Enter preview size", "Enter the number of rows you would like to preview:");
            int previewSize = numberDialog.open();
            if (previewSize>0)
            {
                TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
                progressDialog.open();
                
                if (!progressDialog.isCancelled())
                {
                    PreviewRowsDialog prd =new PreviewRowsDialog(shell, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRows(wStepname.getText()));
                    prd.open();
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, props, "Error displaying preview data", "An error occurred while trying to preview XML data", e);
       }
	}


	public String toString()
	{
		return this.getClass().getName();
	}

}
