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
 
package be.ibridge.kettle.spoon;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.PrintSpool;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.clipboard.ImageDataTransfer;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.CheckResultDialog;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.EnterOptionsDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.dialog.SQLStatementsDialog;
import be.ibridge.kettle.core.dialog.ShowBrowserDialog;
import be.ibridge.kettle.core.dialog.Splash;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPage1;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPage2;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageInformix;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageJDBC;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageOCI;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageODBC;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageOracle;
import be.ibridge.kettle.repository.PermissionMeta;
import be.ibridge.kettle.repository.RepositoriesMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.repository.dialog.RepositoriesDialog;
import be.ibridge.kettle.repository.dialog.RepositoryExplorerDialog;
import be.ibridge.kettle.repository.dialog.SelectObjectDialog;
import be.ibridge.kettle.repository.dialog.UserDialog;
import be.ibridge.kettle.spoon.dialog.AnalyseImpactProgressDialog;
import be.ibridge.kettle.spoon.dialog.CheckTransProgressDialog;
import be.ibridge.kettle.spoon.dialog.GetSQLProgressDialog;
import be.ibridge.kettle.spoon.dialog.ShowCreditsDialog;
import be.ibridge.kettle.spoon.dialog.TipsDialog;
import be.ibridge.kettle.spoon.wizards.CopyTableWizardPage1;
import be.ibridge.kettle.spoon.wizards.CopyTableWizardPage2;
import be.ibridge.kettle.spoon.wizards.CopyTableWizardPage3;
import be.ibridge.kettle.trans.DatabaseImpact;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.dialog.TransDialog;
import be.ibridge.kettle.trans.dialog.TransHopDialog;
import be.ibridge.kettle.trans.dialog.TransLoadProgressDialog;
import be.ibridge.kettle.trans.dialog.TransSaveProgressDialog;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.selectvalues.SelectValuesMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;
import be.ibridge.kettle.trans.step.tableoutput.TableOutputMeta;


/**
 * This class handles the main window of the Spoon graphical transformation editor.
 * 
 * @author Matt
 * @since 16-may-2003
 */
public class Spoon
{
    public static final String APP_NAME = "Spoon";
    
    private LogWriter log;
	private Display disp;
	private Shell shell;
	private boolean destroy;
	private SpoonGraph spoongraph;
	private SpoonLog   spoonlog;
	private SashForm sashform;
	public  CTabFolder tabfolder;
	
	/**
	 * These are the arguments that were given at Spoon launch time...
	 */
	private String[] arguments;
	
	/**
	 * A list of remarks on the current Transformation...
	 */
	private ArrayList remarks;
	
	/**
	 * A list of impacts of the current transformation on the used databases.
	 */
	private ArrayList impact;

	/**
	 * Indicates whether or not an impact analyses has already run.
	 */
	private boolean impactHasRun;
	
	private boolean stopped;
	
	private Cursor cursor_hourglass, cursor_hand;
	
	public  Props props;
	
	public  Repository rep;
		
	public  TransMeta transMeta;

	private ToolBar  tBar;

	private Menu     msFile;
	private MenuItem miFileSep3;
	private MenuItem miEditUndo, miEditRedo;
	
	private Tree selectionTree;
	private TreeItem tiConn, tiHops, tiStep, tiBase, tiPlug;

    private Tree pluginHistoryTree;

	private Listener lsNew, lsEdit, lsDupe, lsCopy, lsDel, lsSQL, lsCache, lsExpl;
	private SelectionAdapter lsEditDef, lsEditSel;
	
	public static final String STRING_CONNECTIONS = "Connections";
	public static final String STRING_STEPS       = "Steps";
	public static final String STRING_HOPS        = "Hops";
	public static final String STRING_BASE        = "Base step types";
	public static final String STRING_PLUGIN      = "Plugin step types";
    public static final String STRING_HISTORY     = "Step creation history";

	private static final String APPL_TITLE         = APP_NAME;
	 		
	public  KeyAdapter defKeys;
	public  KeyAdapter modKeys;

		
	public Spoon(LogWriter l, Repository rep)
	{
		this(l, null, null, rep);
	}

	public Spoon(LogWriter l, Display d, Repository rep)
	{
		this(l, d, null, rep);
	}
	
	public Spoon(LogWriter log, Display d, TransMeta ti, Repository rep)
	{
		this.log        = log;
		this.rep = rep;
		
		if (d!=null) 
		{
			disp=d;
			destroy=false;
		} 
		else 
		{
			disp=new Display();
			destroy=true;
		} 
		shell=new Shell(disp);
		shell.setText(APPL_TITLE);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		shell.setLayout (layout);
		
		// INIT Data structure
		if (ti==null)
		{
			this.transMeta = new TransMeta();
		}
		else
		{
			this.transMeta = ti;
		}
		
		if (!Props.isInitialized()) 
		{
			log.logDetailed(toString(), "Load properties for Spoon...");
			Props.init(disp, Props.TYPE_PROPERTIES_SPOON);  // things to remember...
		}
		props=Props.getInstance();
		
		// Load settings in the props
		loadSettings();
		
		remarks = new ArrayList();
		impact  = new ArrayList();
		impactHasRun = false;
		
		// props.setLook(shell);
		
		shell.setImage(GUIResource.getInstance().getImageSpoon());
		
		cursor_hourglass = new Cursor(disp, SWT.CURSOR_WAIT);
		cursor_hand      = new Cursor(disp, SWT.CURSOR_HAND);
		
		// widgets = new WidgetContainer();
		
		defKeys = new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					// ESC --> Unselect All steps
					if (e.keyCode == SWT.ESC)   { spoongraph.clearSettings(); transMeta.unselectAll(); refreshGraph(); };

					// F3 --> createDatabaseWizard
					if (e.keyCode == SWT.F3)    { createDatabaseWizard(); }

					// F4 --> copyTableWizard
					if (e.keyCode == SWT.F4)    { copyTableWizard(); }
					
					// F5 --> refresh
					if (e.keyCode == SWT.F5)    { refreshGraph(); refreshTree(true); }
					
					// F6 --> show last impact analyses
					if (e.keyCode == SWT.F6)    { showLastImpactAnalyses(); }
					
					// F7 --> show last verify results
					if (e.keyCode == SWT.F7)    { showLastTransCheck(); }
					
					// F8 --> show last preview
					if (e.keyCode == SWT.F8)    { spoonlog.showPreview(); }
					
					// F9 --> run
					if (e.keyCode == SWT.F9)    { tabfolder.setSelection(1); spoonlog.startstop(); }
					
					// F10 --> preview
					if (e.keyCode == SWT.F10)   { spoonlog.preview(); }

					// F11 --> Verify
					if (e.keyCode == SWT.F11) { checkTrans(); spoongraph.clearSettings();  }

					// CTRL-A --> Select All steps
					if ((int)e.character ==  1) { transMeta.selectAll(); };
					
					// CTRL-D --> Disconnect from repository
					if ((int)e.character ==  4) { closeRepository(); spoongraph.clearSettings();  };
					
					// CTRL-E --> Explore the repository
					if ((int)e.character ==  5) { exploreRepository(); spoongraph.clearSettings();  };
					
					// CTRL-I --> Import from XML file         && (e.keyCode&SWT.CONTROL)!=0
					if ((int)e.character ==  9 && (( e.stateMask&SWT.CONTROL)!=0) && (( e.stateMask&SWT.ALT)==0) ) { openFile(true); spoongraph.clearSettings(); };
										
					// CTRL-N --> new
					if ((int)e.character == 14) { newFile();     spoongraph.clearSettings(); }
					    
					// CTRL-O --> open
					if ((int)e.character == 15) { openFile(false);  spoongraph.clearSettings();  }
					
					// CTRL-P --> print
					if ((int)e.character == 16) { printFile(); spoongraph.clearSettings(); }
					
					// CTRL-Q --> Impact analyses
					if ((int)e.character == 17) { analyseImpact(); spoongraph.clearSettings();  }
					
					// CTRL-R --> Connect to repository
					if ((int)e.character == 18) { openRepository();  spoongraph.clearSettings(); };
					
					// CTRL-S --> save
					if ((int)e.character == 19) { saveFile();  spoongraph.clearSettings();  }
					
					// CTRL-T --> transformation
					if ((int)e.character == 20) { setTrans();  spoongraph.clearSettings();  }
					
					// CTRL-Y --> redo action
					if ((int)e.character == 25) { redoAction(); spoongraph.clearSettings(); }
					
					// CTRL-Z --> undo action
					if ((int)e.character == 26) { spoongraph.clearSettings(); undoAction();  }
                    
                    // CTRL-SHIFT-I --> Copy Transformation Image to clipboard
                    if ((int)e.character ==  9 && (( e.stateMask&SWT.CONTROL)!=0) && (( e.stateMask&SWT.ALT)!=0))
                    {
                        copyTransformationImage();
                    }
                    
                    // System.out.println("(int)e.character = "+(int)e.character+", keycode = "+e.keyCode+", stateMask="+e.stateMask);
				}
			};
		modKeys = new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					spoongraph.shift = (e.keyCode == SWT.SHIFT  );
					spoongraph.control = (e.keyCode == SWT.CONTROL);					
				}

				public void keyReleased(KeyEvent e) 
				{
					spoongraph.shift = (e.keyCode == SWT.SHIFT  );
					spoongraph.control = (e.keyCode == SWT.CONTROL);					
				}
			};

		
		addBar();
		
		//control the bar position
		FormData fdBar = new FormData();
		fdBar.left = new FormAttachment(0, 0);
		fdBar.top = new FormAttachment(0, 0);
		tBar.setLayoutData(fdBar);

		sashform = new SashForm(shell, SWT.HORIZONTAL);
		// props.setLook(sashform);
		
		FormData fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0);
		fdSash.top = new FormAttachment(tBar, 0);
		fdSash.bottom = new FormAttachment(100, 0);
		fdSash.right  = new FormAttachment(100, 0);
		sashform.setLayoutData(fdSash);

		addMenu();
		addTree();
		addTabs();
		
		setTreeImages();
		
		// In case someone dares to press the [X] in the corner ;-)
		shell.addShellListener(	
			new ShellAdapter() 
			{ 
				public void shellClosed(ShellEvent e) 
				{ 
					e.doit=quitFile(); 
				} 
			} 
		);
		int weights[] = props.getSashWeights();
		sashform.setWeights(weights);
		sashform.setVisible(true);
        		
		shell.layout();
		
		// Set the shell size, based upon previous time...
		WindowProperty winprop = props.getScreen(APPL_TITLE);
		if (winprop!=null) winprop.setShell(shell); 
		else 
		{
		    shell.pack();
		    shell.setMaximized(true); // Default = maximized!
		}
	}
	
	public void clear()
	{
		remarks = new ArrayList();
		impact  = new ArrayList();
		impactHasRun = false;
		transMeta.clear();
		setUndoMenu();
	}
	
	public void open()
	{		
		shell.open();
		
		// Shared database entries to load from repository?
		loadRepositoryObjects();
        
        // What plugins did we use previously?
        refreshPluginHistory();
		
		// Perhaps the transformation contains elements at startup?
		if (transMeta.nrSteps()>0 || transMeta.nrDatabases()>0 || transMeta.nrTransHops()>0)
		{
			refreshTree(true);  // Do a complete refresh then...
		}
        
        transMeta.clearChanged(); // Clear changed: they were artificial (databases loaded, etc.)
        setShellText();
		
		if (props.showTips()) 
		{
			TipsDialog tip = new TipsDialog(shell, props);
			tip.open();
		}
	}
	
	public boolean readAndDispatch ()
	{
		return disp.readAndDispatch();
	}
	
	/**
	 * @return check whether or not the application was stopped.
	 */
	public boolean isStopped()
	{
		return stopped;
	}
	
	/**
	 * @param stopped True to stop this application.
	 */
	public void setStopped(boolean stopped)
	{
		this.stopped = stopped;
	}
	
	/**
	 * @param destroy Whether or not to distroy the display.
	 */
	public void setDestroy(boolean destroy)
	{
		this.destroy = destroy;
	}
	
	/**
	 * @return Returns whether or not we should distroy the display.
	 */
	public boolean doDestroy()
	{
		return destroy;
	}
	
	/**
     * @param arguments The arguments to set.
     */
    public void setArguments(String[] arguments)
    {
        this.arguments = arguments;
    }

    /**
     * @return Returns the arguments.
     */
    public String[] getArguments()
    {
        return arguments;
    }
	
	public synchronized void dispose()
	{
		setStopped(true);
		cursor_hand.dispose();
		cursor_hourglass.dispose();
		
		if (destroy && !disp.isDisposed()) disp.dispose();        
	}

	public boolean isDisposed()
	{
		return disp.isDisposed();
	}

	public void sleep()
	{
		disp.sleep();
	}
	
	public void addMenu()
	{
		Menu mBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mBar);
		
		// main File menu...
		MenuItem mFile = new MenuItem(mBar, SWT.CASCADE); mFile.setText("&File");
		  msFile = new Menu(shell, SWT.DROP_DOWN);
		  mFile.setMenu(msFile);
		  MenuItem miFileNew       = new MenuItem(msFile, SWT.CASCADE); miFileNew.setText("&New \tCTRL-N");
		  MenuItem miFileOpen      = new MenuItem(msFile, SWT.CASCADE); miFileOpen.setText("&Open \tCTRL-O");
		  MenuItem miFileImport    = new MenuItem(msFile, SWT.CASCADE); miFileImport.setText("&Import from an XML file\tCTRL-I");
		  MenuItem miFileExport    = new MenuItem(msFile, SWT.CASCADE); miFileExport.setText("&Export to an XML file");
		  MenuItem miFileSave      = new MenuItem(msFile, SWT.CASCADE); miFileSave.setText("&Save \tCTRL-S");
		  MenuItem miFileSaveAs    = new MenuItem(msFile, SWT.CASCADE); miFileSaveAs.setText("Save &as...");
		  new MenuItem(msFile, SWT.SEPARATOR);
		  MenuItem miFilePrint     = new MenuItem(msFile, SWT.CASCADE); miFilePrint.setText("&Print \tCTRL-P");
		  new MenuItem(msFile, SWT.SEPARATOR);
		  MenuItem miFileQuit      = new MenuItem(msFile, SWT.CASCADE); miFileQuit.setText("&Quit");
		  miFileSep3               = new MenuItem(msFile, SWT.SEPARATOR);
		  addMenuLast();
		
		Listener lsFileOpen       = new Listener() { public void handleEvent(Event e) { openFile(false);    } };
		Listener lsFileImport     = new Listener() { public void handleEvent(Event e) { openFile(true);     } };
		Listener lsFileExport     = new Listener() { public void handleEvent(Event e) { saveXMLFile();      } };
		Listener lsFileNew        = new Listener() { public void handleEvent(Event e) { newFile();          } };
		Listener lsFileSave       = new Listener() { public void handleEvent(Event e) { saveFile();         } };
		Listener lsFileSaveAs     = new Listener() { public void handleEvent(Event e) { saveFileAs();       } };
		Listener lsFilePrint      = new Listener() { public void handleEvent(Event e) { printFile();        } };
		Listener lsFileQuit       = new Listener() { public void handleEvent(Event e) { quitFile();         } };
		
		miFileOpen      .addListener (SWT.Selection, lsFileOpen     );
		miFileImport    .addListener (SWT.Selection, lsFileImport   );
		miFileExport    .addListener (SWT.Selection, lsFileExport   );
		miFileNew       .addListener (SWT.Selection, lsFileNew      );
		miFileSave      .addListener (SWT.Selection, lsFileSave     );
		miFileSaveAs    .addListener (SWT.Selection, lsFileSaveAs   );
		miFilePrint     .addListener (SWT.Selection, lsFilePrint    );
		miFileQuit      .addListener (SWT.Selection, lsFileQuit     );

		// main Edit menu...
		MenuItem mEdit = new MenuItem(mBar, SWT.CASCADE); mEdit.setText("&Edit");
		  Menu msEdit = new Menu(shell, SWT.DROP_DOWN);
		  mEdit.setMenu(msEdit);
		  miEditUndo                  = new MenuItem(msEdit, SWT.CASCADE);
		  miEditRedo                  = new MenuItem(msEdit, SWT.CASCADE);
		  setUndoMenu();
		  new MenuItem(msEdit, SWT.SEPARATOR);
		  MenuItem miEditUnselectAll  = new MenuItem(msEdit, SWT.CASCADE); miEditUnselectAll.setText("&Clear selection \tESC");
		  MenuItem miEditSelectAll    = new MenuItem(msEdit, SWT.CASCADE); miEditSelectAll.setText("&Select all steps \tCTRL-A");
		  new MenuItem(msEdit, SWT.SEPARATOR);
		  MenuItem miEditCopy         = new MenuItem(msEdit, SWT.CASCADE); miEditCopy.setText("Copy selected steps to clipboard\tCTRL-C");
		  MenuItem miEditPaste        = new MenuItem(msEdit, SWT.CASCADE); miEditPaste.setText("Paste steps from clipboard\tCTRL-V");
		  new MenuItem(msEdit, SWT.SEPARATOR);
		  MenuItem miEditRefresh      = new MenuItem(msEdit, SWT.CASCADE); miEditRefresh.setText("&Refresh \tF5");
		  new MenuItem(msEdit, SWT.SEPARATOR);
		  MenuItem miEditOptions      = new MenuItem(msEdit, SWT.CASCADE); miEditOptions.setText("&Options...");
		
		Listener lsEditUndo        = new Listener() { public void handleEvent(Event e) { undoAction(); } };
		Listener lsEditRedo        = new Listener() { public void handleEvent(Event e) { redoAction(); } };
		Listener lsEditUnselectAll = new Listener() { public void handleEvent(Event e) { editUnselectAll(); } };
		Listener lsEditSelectAll   = new Listener() { public void handleEvent(Event e) { editSelectAll();   } };
		Listener lsEditOptions     = new Listener() { public void handleEvent(Event e) { editOptions();     } };

		miEditUndo       .addListener(SWT.Selection, lsEditUndo);
		miEditRedo       .addListener(SWT.Selection, lsEditRedo);
		miEditUnselectAll.addListener(SWT.Selection, lsEditUnselectAll);
		miEditSelectAll  .addListener(SWT.Selection, lsEditSelectAll);
		miEditOptions    .addListener(SWT.Selection, lsEditOptions);

		// main Repository menu...
		MenuItem mRep = new MenuItem(mBar, SWT.CASCADE); mRep.setText("&Repository");
		  Menu msRep = new Menu(shell, SWT.DROP_DOWN);
		  mRep.setMenu(msRep);
		  MenuItem miRepConnect    = new MenuItem(msRep, SWT.CASCADE); miRepConnect.setText("&Connect to repository \tCTRL-R");
		  MenuItem miRepDisconnect = new MenuItem(msRep, SWT.CASCADE); miRepDisconnect.setText("&Disconnect repository \tCTRL-D");
		  MenuItem miRepExplore    = new MenuItem(msRep, SWT.CASCADE); miRepExplore.setText("&Explore repository \tCTRL-E");
		  new MenuItem(msRep, SWT.SEPARATOR);
		  MenuItem miRepUser       = new MenuItem(msRep, SWT.CASCADE); miRepUser.setText("&Edit current user\tCTRL-U");
		
		Listener lsRepConnect     = new Listener() { public void handleEvent(Event e) { openRepository();    } };
		Listener lsRepDisconnect  = new Listener() { public void handleEvent(Event e) { closeRepository();   } };
		Listener lsRepExplore     = new Listener() { public void handleEvent(Event e) { exploreRepository(); } };
		Listener lsRepUser        = new Listener() { public void handleEvent(Event e) { editRepositoryUser();} };
		
		miRepConnect    .addListener (SWT.Selection, lsRepConnect   );
		miRepDisconnect .addListener (SWT.Selection, lsRepDisconnect);
		miRepExplore    .addListener (SWT.Selection, lsRepExplore   );
		miRepUser       .addListener (SWT.Selection, lsRepUser      );
        
		// main Transformation menu...
		MenuItem mTrans = new MenuItem(mBar, SWT.CASCADE); mTrans.setText("&Transformation");
		  Menu msTrans = new Menu(shell, SWT.DROP_DOWN );
		  mTrans.setMenu(msTrans);
		  MenuItem miTransRun       = new MenuItem(msTrans, SWT.CASCADE); miTransRun    .setText("&Run \tF9");
		  MenuItem miTransPreview   = new MenuItem(msTrans, SWT.CASCADE); miTransPreview.setText("&Preview \tF10");
		  MenuItem miTransCheck     = new MenuItem(msTrans, SWT.CASCADE); miTransCheck  .setText("&Verify \tF11");
		  MenuItem miTransImpact    = new MenuItem(msTrans, SWT.CASCADE); miTransImpact .setText("&Impact");
		  MenuItem miTransSQL       = new MenuItem(msTrans, SWT.CASCADE); miTransSQL    .setText("&Get SQL");
		  new MenuItem(msTrans, SWT.SEPARATOR);
		  MenuItem miLastImpact     = new MenuItem(msTrans, SWT.CASCADE); miLastImpact  .setText("Show last impact analyses \tF6");
		  MenuItem miLastCheck      = new MenuItem(msTrans, SWT.CASCADE); miLastCheck   .setText("Show last verify results  \tF7");
		  MenuItem miLastPreview    = new MenuItem(msTrans, SWT.CASCADE); miLastPreview .setText("Show last preview results \tF8");
		  new MenuItem(msTrans, SWT.SEPARATOR);
		  MenuItem miTransCopy      = new MenuItem(msTrans, SWT.CASCADE); miTransCopy   .setText("&Copy transformation to clipboard");
		  MenuItem miTransPaste     = new MenuItem(msTrans, SWT.CASCADE); miTransPaste  .setText("P&aste transformation from clipboard");
          MenuItem miTransImage     = new MenuItem(msTrans, SWT.CASCADE); miTransImage  .setText("Copy the transformation image clipboard \tCTRL-ALT-I");
		  new MenuItem(msTrans, SWT.SEPARATOR);
		  MenuItem miTransDetails   = new MenuItem(msTrans, SWT.CASCADE); miTransDetails.setText("&Settings... \tCTRL-T");

		Listener lsTransDetails   = new Listener() { public void handleEvent(Event e) { setTrans();   } };
		Listener lsTransRun       = new Listener() { public void handleEvent(Event e) { tabfolder.setSelection(1); spoonlog.startstop(); } };
		Listener lsTransPreview   = new Listener() { public void handleEvent(Event e) { spoonlog.preview(); } };
		Listener lsTransCheck     = new Listener() { public void handleEvent(Event e) { checkTrans();       } };
		Listener lsTransImpact    = new Listener() { public void handleEvent(Event e) { analyseImpact();    } };
		Listener lsTransSQL       = new Listener() { public void handleEvent(Event e) { getSQL();           } };
		Listener lsLastPreview    = new Listener() { public void handleEvent(Event e) { spoonlog.showPreview();    } };
		Listener lsLastCheck      = new Listener() { public void handleEvent(Event e) { showLastTransCheck();      } };
		Listener lsLastImpact     = new Listener() { public void handleEvent(Event e) { showLastImpactAnalyses();  } };
		Listener lsTransCopy      = new Listener() { public void handleEvent(Event e) { copyTransformation(); } };
        Listener lsTransImage     = new Listener() { public void handleEvent(Event e) { copyTransformationImage(); } };
		Listener lsTransPaste     = new Listener() { public void handleEvent(Event e) { pasteTransformation(); } };
		
		miTransDetails.addListener(SWT.Selection, lsTransDetails);
		miTransRun    .addListener(SWT.Selection, lsTransRun);
		miTransPreview.addListener(SWT.Selection, lsTransPreview);
		miTransCheck  .addListener(SWT.Selection, lsTransCheck);
		miTransImpact .addListener(SWT.Selection, lsTransImpact);
		miTransSQL    .addListener(SWT.Selection, lsTransSQL);
		miLastPreview .addListener(SWT.Selection, lsLastPreview);
		miLastCheck   .addListener(SWT.Selection, lsLastCheck);
		miLastImpact  .addListener(SWT.Selection, lsLastImpact);
		miTransCopy   .addListener(SWT.Selection, lsTransCopy);
		miTransPaste  .addListener(SWT.Selection, lsTransPaste);
        miTransImage  .addListener(SWT.Selection, lsTransImage);


		// Wizard menu
		MenuItem mWizard = new MenuItem(mBar, SWT.CASCADE); mWizard.setText("&Wizard");
		  Menu msWizard = new Menu(shell, SWT.DROP_DOWN );
		  mWizard.setMenu(msWizard);

		  MenuItem miWizardNewConnection = new MenuItem(msWizard, SWT.CASCADE); 
		  miWizardNewConnection.setText("&Create database connection wizard...\tF3");
		  Listener lsWizardNewConnection= new Listener() { public void handleEvent(Event e) { createDatabaseWizard();  } };
		  miWizardNewConnection.addListener(SWT.Selection, lsWizardNewConnection);

		  MenuItem miWizardCopyTable = new MenuItem(msWizard, SWT.CASCADE); 
		  miWizardCopyTable.setText("&Copy table wizard...\tF4");
		  Listener lsWizardCopyTable= new Listener() { public void handleEvent(Event e) { copyTableWizard();  } };
		  miWizardCopyTable.addListener(SWT.Selection, lsWizardCopyTable);
		  
		
		// main Help menu...
		MenuItem mHelp = new MenuItem(mBar, SWT.CASCADE); mHelp.setText("&Help");
		  Menu msHelp = new Menu(shell, SWT.DROP_DOWN );
		  mHelp.setMenu(msHelp);
		MenuItem miHelpCredit       = new MenuItem(msHelp, SWT.CASCADE); miHelpCredit.setText("&Credits");
		Listener lsHelpCredit = new Listener() { public void handleEvent(Event e) { ShowCreditsDialog scd = new ShowCreditsDialog(shell, props, GUIResource.getInstance().getImageCredits()); scd.open();     } };
		miHelpCredit.addListener (SWT.Selection, lsHelpCredit  );
		MenuItem miHelpTOTD       = new MenuItem(msHelp, SWT.CASCADE); miHelpTOTD.setText("&Tip of the day");
		Listener lsHelpTOTD = new Listener() { public void handleEvent(Event e) 
			{ 
				TipsDialog td = new TipsDialog(shell, props); 
				td.open();
			} 
		};
		miHelpTOTD.addListener (SWT.Selection, lsHelpTOTD  );

		new MenuItem(msHelp, SWT.SEPARATOR);

		MenuItem miHelpAbout       = new MenuItem(msHelp, SWT.CASCADE); miHelpAbout.setText("&About");
		Listener lsHelpAbout = new Listener() { public void handleEvent(Event e) { helpAbout();      } };
		miHelpAbout.addListener (SWT.Selection, lsHelpAbout  );

	}
	
	private void addMenuLast()
	{
		int idx = msFile.indexOf(miFileSep3);
		int max = msFile.getItemCount();
		
		// Remove everything until end... 
		for (int i=max-1;i>idx;i--)
		{
			MenuItem mi = msFile.getItem(i);
			mi.dispose();
		}
		
		// Previously loaded files...
		String  lf[] = props.getLastFiles();
		String  ld[] = props.getLastDirs();
		boolean lt[] = props.getLastTypes();
		String  lr[] = props.getLastRepositories();
		
		for (int i=0;i<lf.length;i++)
		{
		  MenuItem miFileLast = new MenuItem(msFile, SWT.CASCADE);
		  char chr  = (char)('1'+i );
		  int accel =  SWT.CTRL | chr;
		  String repository = ( lr[i]!=null && lr[i].length()>0 ) ? ( "["+lr[i]+"] " ) : ""; 
		  String filename = RepositoryDirectory.DIRECTORY_SEPARATOR + lf[i];
		  if (!lt[i]) filename = lf[i];
		  if (!ld[i].equals(RepositoryDirectory.DIRECTORY_SEPARATOR))
		  {
		  	filename=ld[i]+filename;
		  }
		  
		  if (i<9)
		  {
		  	miFileLast.setAccelerator(accel);
			miFileLast.setText("&"+chr+"  "+repository+filename+ "\tCTRL-"+chr);
		  }
		  else
		  {
		  	miFileLast.setText("   "+repository+filename);
		  }

		  final String  fn = lf[i];   // filename
		  final String  fd = ld[i];   // Repository directory ...
		  final boolean ft = lt[i];   // type: true=repository, false=file
		  final String  fr = lr[i];   // repository name
		  
		  Listener lsFileLast = new Listener() 
			  { 
				  public void handleEvent(Event e) 
				  {
				      if (showChangedWarning())
				      {
				      	// If the file comes from a repository and it's not the same as 
				      	// the one we're connected to, ask for a username/password!
				      	//
				      	boolean noRepository=false;
				      	if (ft && (rep==null || !rep.getRepositoryInfo().getName().equalsIgnoreCase(fr) ))
				      	{
				      		int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
				      		RepositoriesDialog rd = new RepositoriesDialog(disp, SWT.NONE, perms, "Spoon");
				      		rd.setRepositoryName(fr);
				      		if (rd.open())
				      		{
				      			//	Close the previous connection...
				      			if (rep!=null) rep.disconnect();
				      			rep = new Repository(log, rd.getRepository(), rd.getUser());
				      			if (!rep.connect(APP_NAME))
				      			{
				      				rep=null;
				      				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
									mb.setMessage("I was unable to connect to this repository!");
									mb.setText("Error!");
									mb.open();
				      			}
				      		}
				      		else
				      		{
				      			noRepository=true;
				      		}
				      	}
				      	
				      	if (ft)
				      	{
				      		if (!noRepository && rep!=null && rep.getRepositoryInfo().getName().equalsIgnoreCase(fr))
				      		{
			      				// OK, we're connected to the new repository...
			      				// Load the transformation...
		      					TransLoadProgressDialog tlpd = new TransLoadProgressDialog(shell, rep, fn, rep.getDirectoryTree().findDirectory(fd));
		      					TransMeta transInfo = tlpd.open();
		      					if (transInfo!=null)
		      					{
		      						transMeta = transInfo;
		      						transMeta.clearChanged();
		      					}
				      		}
				      		else
				      		{
								clear();
			      				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
								mb.setMessage("Can't load this transformation.  Please connect to the correct repository first.");
								mb.setText("Error!");
								mb.open();
				      		}
				      	}
				      	else
				      		// Load from XML!
				      	{
				      		try
							{
				      			transMeta = new TransMeta(fn);
				      			transMeta.clearChanged();
                                transMeta.setFilename(fn);
							}
				      		catch(KettleException ke)
							{
								clear();
								new ErrorDialog(shell, props, "Error loading transformation", "I was unable to load this transformation from the XML file because of an error", ke);
							}
				      	}
				      	setShellText();
						refreshTree();
						refreshGraph();

					  }
				  } 
			  };
		  miFileLast.addListener(SWT.Selection, lsFileLast);
		}
	}
	
	private void addBar()
	{
		tBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT );
        // props.setLook(tBar);
		
		final ToolItem tiFileNew = new ToolItem(tBar, SWT.PUSH);
		final Image imFileNew = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"new.png")); 
		tiFileNew.setImage(imFileNew);
		tiFileNew.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newFile(); }});
		tiFileNew.setToolTipText("New transformation, clear all settings");

		final ToolItem tiFileOpen = new ToolItem(tBar, SWT.PUSH);
		final Image imFileOpen = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"open.png")); 
		tiFileOpen.setImage(imFileOpen);
		tiFileOpen.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { openFile(false); }});
		tiFileOpen.setToolTipText("Open tranformation");

		final ToolItem tiFileSave = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSave = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"save.png")); 
		tiFileSave.setImage(imFileSave);
		tiFileSave.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { saveFile(); }});
		tiFileSave.setToolTipText("Save current transformation");

		final ToolItem tiFileSaveAs = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSaveAs = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"saveas.png")); 
		tiFileSaveAs.setImage(imFileSaveAs);
		tiFileSaveAs.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { saveFileAs(); }});
		tiFileSaveAs.setToolTipText("Save transformation with different name");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFilePrint = new ToolItem(tBar, SWT.PUSH);
		final Image imFilePrint = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"print.png")); 
		tiFilePrint.setImage(imFilePrint);
		tiFilePrint.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { printFile(); }});
		tiFilePrint.setToolTipText("Print");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFileRun = new ToolItem(tBar, SWT.PUSH);
		final Image imFileRun = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"run.png")); 
		tiFileRun.setImage(imFileRun);
		tiFileRun.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { tabfolder.setSelection(1); spoonlog.startstop(); }});
		tiFileRun.setToolTipText("Run this transformation");

		final ToolItem tiFilePreview = new ToolItem(tBar, SWT.PUSH);
		final Image imFilePreview = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"preview.png")); 
		tiFilePreview.setImage(imFilePreview);
		tiFilePreview.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { spoonlog.preview(); }});
		tiFilePreview.setToolTipText("Preview this transformation");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFileCheck = new ToolItem(tBar, SWT.PUSH);
		final Image imFileCheck = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"check.png")); 
		tiFileCheck.setImage(imFileCheck);
		tiFileCheck.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { checkTrans(); }});
		tiFileCheck.setToolTipText("Verify this transformation");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiImpact = new ToolItem(tBar, SWT.PUSH);
		final Image imImpact = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"impact.png")); 
		// Can't seem to get the transparency correct for this image!
		ImageData idImpact = imImpact.getImageData();
		int impactPixel = idImpact.palette.getPixel(new RGB(255, 255, 255));
		idImpact.transparentPixel = impactPixel;
		Image imImpact2 = new Image(disp, idImpact);
		tiImpact.setImage(imImpact2);
		tiImpact.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { analyseImpact(); }});
		tiImpact.setToolTipText("Analyze the impact of this transformation on the database(s)");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiSQL = new ToolItem(tBar, SWT.PUSH);
		final Image imSQL = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"SQLbutton.png")); 
		// Can't seem to get the transparency correct for this image!
		ImageData idSQL = imSQL.getImageData();
		int sqlPixel= idSQL.palette.getPixel(new RGB(255, 255, 255));
		idSQL.transparentPixel = sqlPixel;
		Image imSQL2= new Image(disp, idSQL);
		tiSQL.setImage(imSQL2);
		tiSQL.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getSQL();  }});
		tiSQL.setToolTipText("Generate the SQL needed to run this transformation");

		tBar.addDisposeListener(new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent e) 
				{
					imFileNew.dispose();
					imFileOpen.dispose();
					imFileSave.dispose();
					imFileSaveAs.dispose();
				}
			}
		);
		tBar.addKeyListener(defKeys);
		tBar.addKeyListener(modKeys);
		tBar.pack();
	}


	private void addTree()
	{
        // Split the left side of the screen in half
        SashForm leftSash = new SashForm(sashform, SWT.VERTICAL);
        
		// Now set up the main CSH tree
		selectionTree = new Tree(leftSash, SWT.MULTI | SWT.BORDER);
        // props.setLook(selectionTree);
		selectionTree.setLayout(new FillLayout());
		
		tiConn   = new TreeItem(selectionTree, SWT.NONE); tiConn.setText(STRING_CONNECTIONS);
		tiStep   = new TreeItem(selectionTree, SWT.NONE); tiStep.setText(STRING_STEPS);
		tiHops   = new TreeItem(selectionTree, SWT.NONE); tiHops.setText(STRING_HOPS);
		tiBase   = new TreeItem(selectionTree, SWT.NONE); tiBase.setText(STRING_BASE);
		tiPlug   = new TreeItem(selectionTree, SWT.NONE); tiPlug.setText(STRING_PLUGIN);
		
		// Fill the base components...
		StepLoader steploader = StepLoader.getInstance();
		StepPlugin basesteps[] = steploader.getStepsWithType(StepPlugin.TYPE_NATIVE);
		String basecat[] = steploader.getCategories(StepPlugin.TYPE_NATIVE);
		TreeItem tiBaseCat[] = new TreeItem[basecat.length];
		for (int i=0;i<basecat.length;i++)
		{
			tiBaseCat[i] = new TreeItem(tiBase, SWT.NONE);
			tiBaseCat[i].setText(basecat[i]);
			
			for (int j=0;j<basesteps.length;j++)
			{
				if (basesteps[j].getCategory().equalsIgnoreCase(basecat[i]))
				{
					TreeItem ti = new TreeItem(tiBaseCat[i], 0);
					ti.setText(basesteps[j].getDescription());
				}
			}
		}

		// Show the plugins...
		StepPlugin plugins[] = steploader.getStepsWithType(StepPlugin.TYPE_PLUGIN);
		String plugcat[] = steploader.getCategories(StepPlugin.TYPE_PLUGIN);
		TreeItem tiPlugCat[] = new TreeItem[plugcat.length];
		for (int i=0;i<plugcat.length;i++)
		{
			tiPlugCat[i] = new TreeItem(tiPlug, SWT.NONE);
			tiPlugCat[i].setText(plugcat[i]);
			
			for (int j=0;j<plugins.length;j++)
			{
				if (plugins[j].getCategory().equalsIgnoreCase(plugcat[i]))
				{
					TreeItem ti = new TreeItem(tiPlugCat[i], 0);
					ti.setText(plugins[j].getDescription());
				}
			}
		}
		
		tiConn.setExpanded(true);
		tiStep.setExpanded(false);
		tiBase.setExpanded(true);
		tiPlug.setExpanded(true);

        
		addToolTipsToTree(selectionTree);

		// Popup-menu selection
		lsNew    = new Listener() { public void handleEvent(Event e) { newSelected();  } };  
		lsEdit   = new Listener() { public void handleEvent(Event e) { editSelected(); } };
		lsDupe   = new Listener() { public void handleEvent(Event e) { dupeSelected(); } };
		lsCopy   = new Listener() { public void handleEvent(Event e) { clipSelected(); } };
		lsDel    = new Listener() { public void handleEvent(Event e) { delSelected();  } };
		lsSQL    = new Listener() { public void handleEvent(Event e) { sqlSelected();  } };
		lsCache  = new Listener() { public void handleEvent(Event e) { clearDBCache(); } };
		lsExpl   = new Listener() { public void handleEvent(Event e) { exploreDB();    } };
		
		// Default selection (double-click, enter)
		lsEditDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e){ editSelected(); } };
		//lsNewDef  = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e){ newSelected();  } };
		lsEditSel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setMenu(e); } };
		
		// Add all the listeners... 
		selectionTree.addSelectionListener(lsEditDef); // double click somewhere in the tree...
		//tCSH.addSelectionListener(lsNewDef); // double click somewhere in the tree...
		selectionTree.addSelectionListener(lsEditSel);
		
		
		// Keyboard shortcuts!
		selectionTree.addKeyListener(defKeys);
		selectionTree.addKeyListener(modKeys);
        
        // Set a listener on the tree
        addDragSourceToTree(selectionTree);
        
        
        
		// OK, now add a list of often-used icons to the bottom of the tree...
        pluginHistoryTree = new Tree(leftSash, SWT.SINGLE );
        
        // Add tooltips for history tree too
        addToolTipsToTree(pluginHistoryTree);
        
        // Set the same listener on this tree
        addDragSourceToTree(pluginHistoryTree);

        leftSash.setWeights(new int[] { 70, 30 } );

    }
    
    private void addToolTipsToTree(Tree tree)
    {
        tree.addListener(SWT.MouseHover, new Listener()
            {
                public void handleEvent(Event e)
                {
                    String tooltip=null;
                    Tree tree = (Tree)e.widget;
                    TreeItem item = tree.getItem(new org.eclipse.swt.graphics.Point(e.x, e.y));
                    if (item!=null)
                    {
                        StepLoader steploader = StepLoader.getInstance();
                        StepPlugin sp = steploader.findStepPluginWithDescription(item.getText());
                        if (sp!=null)
                        {
                            tooltip = sp.getTooltip();
                        }
                        else
                        if (item.getText().equalsIgnoreCase(STRING_BASE) ||
                            item.getText().equalsIgnoreCase(STRING_PLUGIN)
                           )
                        {
                            tooltip="Select one of the step types listed below and"+Const.CR+"drag it onto the graphical view tab to the right.";
                        }
                    }
                    tree.setToolTipText(tooltip);
                }
            }
        );
    }
    
    
    private void addDragSourceToTree(Tree tree)
    {
        final Tree fTree = tree;
        
        // Drag & Drop for steps
        Transfer[] ttypes = new Transfer[] {TextTransfer.getInstance() };
        
        DragSource ddSource = new DragSource(fTree, DND.DROP_MOVE | DND.DROP_COPY);
        ddSource.setTransfer(ttypes);
        ddSource.addDragListener(new DragSourceListener() 
            {
                public void dragStart(DragSourceEvent event){ }
    
                public void dragSetData(DragSourceEvent event) 
                {
                    TreeItem ti[] = fTree.getSelection();
                    
                    String data = new String();
                    for (int i=0;i<ti.length;i++) 
                    {
                        String ts[] = Const.getTreeStrings(ti[i]);
                        
                        if (ts!=null && ts.length > 0 &&
                            ( ts[0].equalsIgnoreCase(STRING_BASE) ||
                              ts[0].equalsIgnoreCase(STRING_PLUGIN) ||
                              ts[0].equalsIgnoreCase(STRING_HISTORY)
                            )
                           )
                        {
                            data+="#BASE STEP#"+ti[i].getText()+Const.CR;
                        }
                        else
                        if (ts!=null && ts.length > 0 && ts[0].equalsIgnoreCase(STRING_CONNECTIONS))
                        {
                            data+="#CONNECTION#"+ti[i].getText()+Const.CR;
                        }
                        else
                        if (ts!=null && ts.length > 0 && ts[0].equalsIgnoreCase(STRING_HOPS))
                        {
                            data+="#HOP#"+ti[i].getText()+Const.CR;;
                        }
                        else
                        {
                            data+=ti[i].getText()+Const.CR;
                        }
                    } 
                    event.data = data;
                }
    
                public void dragFinished(DragSourceEvent event) {}
            }
        );

    }
    
    public void refreshPluginHistory()
    {
        pluginHistoryTree.removeAll();
        
        TreeItem tiMain = new TreeItem(pluginHistoryTree, SWT.NONE);
        tiMain.setText(STRING_HISTORY);
        
        List pluginHistory = props.getPluginHistory();
        for (int i=0;i<pluginHistory.size();i++)
        {
            String pluginID = (String)pluginHistory.get(i);
            StepPlugin stepPlugin = StepLoader.getInstance().findStepPluginWithID(pluginID);
            if (stepPlugin!=null)
            {
                Image image = (Image) GUIResource.getInstance().getImagesSteps().get(pluginID);
    
                TreeItem ti = new TreeItem(tiMain, SWT.NONE);
                ti.setText(stepPlugin.getDescription());
                ti.setImage(image);
            }
        }
        
        tiMain.setExpanded(true);
    }
	
	private void setMenu(SelectionEvent e)
	{
		TreeItem ti = (TreeItem)e.item;
		String strti = ti.getText();
		Tree root = ti.getParent();
		log.logDebug(toString(), "Clicked on  "+ti.getText());
		TreeItem sel[] = root.getSelection();

		Menu mCSH = new Menu(shell, SWT.POP_UP);

		// Find the level we clicked on: Top level (only NEW in the menu) or below (edit, insert, ...)
		TreeItem parent = ti.getParentItem();
		if (parent==null) // Top level
		{
			if (!strti.equalsIgnoreCase(STRING_BASE) && !strti.equalsIgnoreCase(STRING_PLUGIN))
			{
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("New");
				miNew.addListener( SWT.Selection, lsNew );
			}
			if (strti.equalsIgnoreCase(STRING_STEPS))
			{
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("Sort steps");
				miNew.addSelectionListener( new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent arg0)
					{
						transMeta.sortSteps();
						refreshTree(true);
					}
				});
			}
			if (strti.equalsIgnoreCase(STRING_HOPS))
			{
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("Sort hops");
				miNew.addSelectionListener( new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent arg0)
					{
						transMeta.sortHops();
						refreshTree(true);
					}
				});
			}
			if (strti.equalsIgnoreCase(STRING_CONNECTIONS))
			{
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("New Connection Wizard");
				miNew.addSelectionListener( new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent arg0)
                    {
                        createDatabaseWizard();
                    }
                } );
				MenuItem miCache  = new MenuItem(mCSH, SWT.PUSH); miCache.setText("Clear complete DB Cache");
				miCache.addListener( SWT.Selection, lsCache );
			}
		}
		else
		{
			String strparent = parent.getText();
			if (strparent.equalsIgnoreCase(STRING_CONNECTIONS))
			{
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("New");
				MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText("Edit");
				MenuItem miDupe = new MenuItem(mCSH, SWT.PUSH); miDupe.setText("Duplicate");
				MenuItem miCopy = new MenuItem(mCSH, SWT.PUSH); miCopy.setText("Copy to clipboard");
				MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText("Delete");
				new MenuItem(mCSH, SWT.SEPARATOR);
				MenuItem miSQL  = new MenuItem(mCSH, SWT.PUSH); miSQL.setText("SQL Editor");
				MenuItem miCache= new MenuItem(mCSH, SWT.PUSH); miCache.setText("Clear DB Cache of "+ti.getText());
				new MenuItem(mCSH, SWT.SEPARATOR);
				MenuItem miExpl = new MenuItem(mCSH, SWT.PUSH); miExpl.setText("Explore");
                // disable for now if the connection is an SAP R/3 type of database...
                DatabaseMeta dbMeta = transMeta.findDatabase(strti);
                if (dbMeta==null || dbMeta.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_SAPR3) miExpl.setEnabled(false);
                
				miNew.addListener( SWT.Selection, lsNew );   
				miEdit.addListener(SWT.Selection, lsEdit );
				miDupe.addListener(SWT.Selection, lsDupe );
				miCopy.addListener(SWT.Selection, lsCopy );
				miDel.addListener(SWT.Selection, lsDel );
				miSQL.addListener(SWT.Selection, lsSQL );
				miCache.addListener(SWT.Selection, lsCache);
				miExpl.addListener(SWT.Selection, lsExpl);
			}
			if (strparent.equalsIgnoreCase(STRING_STEPS))
			{
				if (sel.length==2)
				{
					MenuItem miNewHop = new MenuItem(mCSH, SWT.PUSH); miNewHop.setText("New Hop");
					miNewHop.addListener(SWT.Selection, lsNew);
				}
				MenuItem miEdit   = new MenuItem(mCSH, SWT.PUSH); miEdit.setText("Edit");
				MenuItem miDupe   = new MenuItem(mCSH, SWT.PUSH); miDupe.setText("Duplicate");
				MenuItem miDel    = new MenuItem(mCSH, SWT.PUSH); miDel.setText("Delete");
				miEdit.addListener(SWT.Selection, lsEdit );
				miDupe.addListener(SWT.Selection, lsDupe );
				miDel.addListener(SWT.Selection, lsDel );
			}
			if (strparent.equalsIgnoreCase(STRING_HOPS))
			{
				MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText("Edit");
				MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText("Delete");
				miEdit.addListener( SWT.Selection, lsEdit );
				miDel.addListener ( SWT.Selection, lsDel  );
			}
			
			TreeItem grandparent = parent.getParentItem();
			if (grandparent!=null)
			{
				String strgrandparent = grandparent.getText();
				if (strgrandparent.equalsIgnoreCase(STRING_BASE) ||
					strgrandparent.equalsIgnoreCase(STRING_PLUGIN))
				{
					MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("New");
					miNew.addListener( SWT.Selection, lsNew );   
				}
			}
		}
		selectionTree.setMenu(mCSH);
	}
	
	private void addTabs()
	{
		Composite child = new Composite(sashform, SWT.BORDER );
        props.setLook(child);
		
		FormLayout childLayout = new FormLayout();
		childLayout.marginWidth  = 0;
		childLayout.marginHeight = 0;
		child.setLayout(childLayout);
		
		tabfolder= new CTabFolder(child, SWT.BORDER);
        props.setLook(tabfolder, Props.WIDGET_STYLE_TAB);
		
		FormData fdTabfolder = new FormData();
		fdTabfolder.left   = new FormAttachment(0, 0);
		fdTabfolder.right  = new FormAttachment(100, 0);
		fdTabfolder.top    = new FormAttachment(0, 0);
		fdTabfolder.bottom = new FormAttachment(100, 0);
		tabfolder.setLayoutData(fdTabfolder);
		
		CTabItem   tiTabsGraph = new CTabItem(tabfolder, SWT.NONE); 
		tiTabsGraph.setText("Graphical view");
		tiTabsGraph.setToolTipText("Displays the transformation graphically.");
		
		CTabItem   tiTabsList  = new CTabItem(tabfolder, SWT.NULL); 
		tiTabsList.setText("Log view");
		tiTabsList.setToolTipText("Displays the log of the running transformation.");
		
		spoongraph = new SpoonGraph(tabfolder, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND, log, this);
		spoonlog   = new SpoonLog(tabfolder, SWT.NONE, this, log, null);
		
		tabfolder.addKeyListener(defKeys);
		tabfolder.addKeyListener(modKeys);
		
		tiTabsGraph.setControl(spoongraph);
		tiTabsList.setControl(spoonlog);
		
		tabfolder.setSelection(0);
		
		sashform.addKeyListener(defKeys);
		sashform.addKeyListener(modKeys);
	}
	
	public String getRepositoryName()
	{
		if (rep==null) return null;
		return rep.getRepositoryInfo().getName();
	}
	
	public void newSelected()
	{
		log.logDebug(toString(), "New Selected");
		// Determine what menu we selected from...
		
		TreeItem ti[] = selectionTree.getSelection();
					
		// Then call newConnection or newTrans
		if (ti.length>=1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent == null)
			{
				log.logDebug(toString(), "Element has no parent");
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) newConnection();
				if (name.equalsIgnoreCase(STRING_HOPS      )) newHop();
				if (name.equalsIgnoreCase(STRING_STEPS     )) 
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage("Please use the 'Base step types' below to create new steps.");
					mb.setText("Tip!");
					mb.open();
				} 
				//refreshTree();
			}
			else
			{
				String strparent = parent.getText();
				log.logDebug(toString(), "Element has parent: "+strparent);
				if (strparent.equalsIgnoreCase(STRING_CONNECTIONS)) newConnection();
				if (strparent.equalsIgnoreCase(STRING_STEPS      )) 
				{
					log.logDebug(toString(), "New hop!");
					StepMeta from = transMeta.findStep( ti[0].getText() );
					StepMeta to   = transMeta.findStep( ti[1].getText() );
					if (from!=null && to!=null) newHop(from, to);
				} 
				
				TreeItem grandparent = parent.getParentItem();
				if (grandparent!=null)
				{
					String strgrandparent = grandparent.getText();
					if (strgrandparent.equalsIgnoreCase(STRING_BASE) ||
						strgrandparent.equalsIgnoreCase(STRING_PLUGIN))
					{
						newStep();
					}
				}
			}
		}
	}
	
	public void editSelected()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = selectionTree.getSelection();
					
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				log.logDebug(toString(), "(EDIT) Element has parent.");
				String strparent = parent.getText();
				if (strparent.equalsIgnoreCase(STRING_CONNECTIONS)) editConnection(name);
				if (strparent.equalsIgnoreCase(STRING_STEPS      )) editStep(name);
				if (strparent.equalsIgnoreCase(STRING_HOPS       )) editHop(name);
				
				TreeItem grandparent = parent.getParentItem();
				if (grandparent!=null)
				{
					String strgrandparent = grandparent.getText();
					
					if (strgrandparent.equalsIgnoreCase(STRING_BASE   ) ||
						strgrandparent.equalsIgnoreCase(STRING_PLUGIN ) 
					) 
					{
						newStep();
					}
				}
			}
			else
			{
				log.logDebug(toString(), "Element has no parent");
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) newConnection();
				if (name.equalsIgnoreCase(STRING_HOPS       )) newHop();
			}
		}
	}

	public void dupeSelected()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = selectionTree.getSelection();
					
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				log.logDebug(toString(), "(DUPE) Element has parent.");
				String type = parent.getText();
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) dupeConnection(name);
				if (type.equalsIgnoreCase(STRING_STEPS      )) dupeStep(name);
			} 
		}
	}
	
	/**
	 * Copy selected tree item to the clipboard in XML format
	 *
	 */
	public void clipSelected()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = selectionTree.getSelection();
					
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				log.logDebug(toString(), "(DUPE) Element has parent.");
				String type = parent.getText();
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) clipConnection(name);
				if (type.equalsIgnoreCase(STRING_STEPS      )) clipStep(name);
			} 
		}
	}

	
	public void delSelected()
	{
		// Determine what menu we selected from...
		int i;
		
		TreeItem ti[] = selectionTree.getSelection();
		
		String name[] = new String[ti.length];
		TreeItem parent[] = new TreeItem[ti.length];
		
		for (i=0;i<ti.length;i++) 
		{
			name[i] = ti[i].getText();
			parent[i] = ti[i].getParentItem();
		} 
		
		// Then call editConnection or editStep or editTrans
		for (i=name.length-1;i>=0;i--)
		{
			log.logDebug(toString(), "(DELETE) Trying to delete #"+i+"/"+(ti.length-1)+" : "+name[i]);
			if (parent[i] != null)
			{
				String type = parent[i].getText();
				log.logDebug(toString(), "(DELETE) Element has parent: "+type);
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) delConnection(name[i]);
				if (type.equalsIgnoreCase(STRING_STEPS      )) delStep(name[i]);
				if (type.equalsIgnoreCase(STRING_HOPS       )) delHop(name[i]);
			} 
		}
	}

	public void sqlSelected()
	{
		// Determine what menu we selected from...
		int i;
		
		TreeItem ti[] = selectionTree.getSelection();
		for (i=0;i<ti.length;i++) 
		{
			String name     = ti[i].getText();
			TreeItem parent = ti[i].getParentItem();
			String type     = parent.getText();
			if (type.equalsIgnoreCase(STRING_CONNECTIONS))
			{
				DatabaseMeta ci = transMeta.findDatabase(name);
				SQLEditor sql = new SQLEditor(shell, SWT.NONE, ci, transMeta.getDbCache(), "");
				sql.open();
			}
			
		} 
	}
	
	public void editConnection(String name)
	{
		DatabaseMeta db = transMeta.findDatabase(name);
		if (db!=null)
		{
			DatabaseMeta before = (DatabaseMeta)db.clone();
			
			DatabaseDialog con = new DatabaseDialog(shell, SWT.NONE, log, db, props);
            con.setDatabases(transMeta.getDatabases());
			String newname = con.open(); 
			if (newname != null && newname.length()>0)  // null: CANCEL
			{
				// Store undo/redo information
				DatabaseMeta after = (DatabaseMeta)db.clone();
				addUndoChange(new DatabaseMeta[] { before }, new DatabaseMeta[] { after }, new int[] { transMeta.indexOfDatabase(db) } );
				
				saveConnection(db);
				
				if (!name.equalsIgnoreCase(newname)) refreshTree();
			}
		}
		setShellText();
	}

	public void dupeConnection(String name)
	{
		DatabaseMeta db = transMeta.findDatabase(name);
		int pos = transMeta.indexOfDatabase(db);				
		if (db!=null)
		{
			DatabaseMeta newdb = (DatabaseMeta)db.clone();
			String dupename = "(copy of) "+name; 
			newdb.setName(dupename);
			transMeta.addDatabase(pos+1, newdb);
			refreshTree();

			DatabaseDialog con = new DatabaseDialog(shell, SWT.NONE, log, newdb, props);
			String newname = con.open(); 
			if (newname != null)  // null: CANCEL
			{
				transMeta.removeDatabase(pos+1);
				transMeta.addDatabase(pos+1, newdb);
				
				if (!newname.equalsIgnoreCase(dupename)) refreshTree();
			}
			else
			{
				addUndoNew(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { pos });
				saveConnection(db);				
			}
		}
	}
	
	public void clipConnection(String name)
	{
		DatabaseMeta db = transMeta.findDatabase(name);
		if (db!=null)
		{
			String xml = XMLHandler.getXMLHeader() + db.getXML();
			toClipboard(xml);
		}
	}


	/**
	 * Delete a database connection
	 * @param name The name of the database connection.
	 */
	public void delConnection(String name)
	{
		DatabaseMeta db = transMeta.findDatabase(name);
		int pos = transMeta.indexOfDatabase(db);				
		if (db!=null)
		{
			addUndoDelete(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { pos });
			transMeta.removeDatabase(pos);

			// Also delete from repository?
			if (rep!=null)
			{
				if (!rep.getUserInfo().isReadonly())
				{
					try
					{
						long id_database = rep.getDatabaseID(db.getName());
						rep.delDatabase(id_database);
					}
					catch(KettleDatabaseException dbe)
					{
						new ErrorDialog(shell, props, "ERROR", "Error deleting connection ["+db+"] from repository!", dbe);
					}
				}
				else
				{
					new ErrorDialog(shell, props, "ERROR", "Error deleting connection ["+db+"] from repository!", new KettleException("This user is read-only!"));
				}
			}

			refreshTree();
		}
		setShellText();
	}
	
	public void editStep(String name)
	{
		log.logDebug(toString(), "Edit step: "+name);
		editStepInfo(transMeta.findStep(name));
	}
	
	public String editStepInfo(StepMeta stepMeta)
	{
		String stepname=null;
		
		if (stepMeta!=null)
		{
			String name = stepMeta.getName();
			
			// Before we do anything, let's store the situation the way it was...
			StepMeta before = (StepMeta)stepMeta.clone();
			StepMetaInterface stepint = stepMeta.getStepMetaInterface();
			try
			{
				StepDialogInterface dialog = stepint.getDialog(shell, stepMeta.getStepMetaInterface(), transMeta, name);
                dialog.setRepository(rep);
				stepname = dialog.open();
			}
			catch(Throwable e)
			{
				new ErrorDialog(shell, props, "Oops!", "Unable to open dialog for this step", new Exception(e));
			}

			if (stepname!=null)
			{
				// OK, so the step has changed...
				//
				// First, backup the situation for undo/redo
				StepMeta after = (StepMeta)stepMeta.clone();
				addUndoChange(new StepMeta[] { before }, new StepMeta[] { after }, new int[] { transMeta.indexOfStep(stepMeta) }  );
	
				// Then, store the size of the 
				// See if the new name the user enter, doesn't collide with another step. 
				// If so, change the stepname and warn the user!
				//
				String newname=stepname;
				StepMeta smeta = transMeta.findStep(newname, stepMeta);
				int nr=2;
				while (smeta!=null)
				{
					newname = stepname+" "+nr;
					smeta = transMeta.findStep(newname);
					nr++;
				}
				if (nr>2)
				{
					stepname=newname;
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage("This stepname already exists.  Spoon changed the stepname to ["+stepname+"]");
					mb.setText("Tip!");
					mb.open();
				}
				stepMeta.setName(stepname);
				refreshTree(true);  // Perhaps new connections were created in the step dialog.				
			}
			else
			{
				// Scenario: change connections and click cancel...
				// Perhaps new connections were created in the step dialog?
				if (transMeta.haveConnectionsChanged())
				{
					refreshTree(true);  				
				}
			}
			refreshGraph();  // name is displayed on the graph too.
			setShellText();
		}
		
		return stepname;
	}

	public void dupeStep(String name)
	{
		log.logDebug(toString(), "Duplicate step: "+name);
		
		StepMeta stMeta = null, stepMeta = null, look=null;
		
		for (int i=0;i<transMeta.nrSteps() && stepMeta==null;i++)
		{
			look = transMeta.getStep(i);
			if (look.getName().equalsIgnoreCase(name))
			{
				stepMeta=look;
			}
		}
		if (stepMeta!=null)
		{
			stMeta = (StepMeta)stepMeta.clone();
			if (stMeta!=null)
			{
				String newname = transMeta.getAlternativeStepname(stepMeta.getName());
				int nr=2;
				while (transMeta.findStep(newname)!=null)
				{
					newname = stepMeta.getName()+" (copy "+nr+")";
					nr++;
				}
				stMeta.setName(newname);
				// Don't select this new step!
				stMeta.setSelected(false);
				Point loc = stMeta.getLocation();
				stMeta.setLocation(loc.x+20, loc.y+20);
				transMeta.addStep(stMeta);
				addUndoNew(new StepMeta[] { (StepMeta)stMeta.clone() }, new int[] { transMeta.indexOfStep(stMeta) });
				refreshTree();
				refreshGraph();
			}
		}
	}

	public void clipStep(String name)
	{
		log.logDebug(toString(), "copy step to clipboard: "+name);
		
		StepMeta stepMeta = transMeta.findStep(name);
		if (stepMeta!=null)
		{
			String xml = stepMeta.getXML();
			toClipboard(xml);
		}
	}

	public void pasteXML(String clipcontent, Point loc)
	{
		try
		{
		    //System.out.println(clipcontent);
		    
			Document doc = XMLHandler.loadXMLString(clipcontent);
			Node transnode = XMLHandler.getSubNode(doc, "transformation");
			// De-select all, re-select pasted steps...
			transMeta.unselectAll();
			
			Node stepsnode = XMLHandler.getSubNode(transnode, "steps");
			int nr = XMLHandler.countNodes(stepsnode, "step");
			log.logDebug(toString(), "I found "+nr+" steps to paste on location: "+loc);
			StepMeta steps[] = new StepMeta[nr];
			
			//Point min = new Point(loc.x, loc.y);
			Point min = new Point(99999999,99999999);

			// Load the steps...
			for (int i=0;i<nr;i++)
			{
				Node stepnode = XMLHandler.getSubNodeByNr(stepsnode, "step", i);
				steps[i] = new StepMeta(log, stepnode, transMeta.getDatabases(), transMeta.getCounters());

				if (loc!=null)
				{
					Point p = steps[i].getLocation();
					
					if (min.x > p.x) min.x = p.x;
					if (min.y > p.y) min.y = p.y;
				}
			}
			
			// Load the hops...
			Node hopsnode = XMLHandler.getSubNode(transnode, "order");
			nr = XMLHandler.countNodes(hopsnode, "hop");
			log.logDebug(toString(), "I found "+nr+" hops to paste.");
			TransHopMeta hops[] = new TransHopMeta[nr];
			
			ArrayList alSteps = new ArrayList();
			for (int i=0;i<steps.length;i++) alSteps.add(steps[i]);
			
			for (int i=0;i<nr;i++)
			{
				Node hopnode = XMLHandler.getSubNodeByNr(hopsnode, "hop", i);
			    hops[i] = new TransHopMeta(hopnode, alSteps);
			}

			// What's the difference between loc and min?
			// This is the offset:
			Point offset = new Point(loc.x-min.x, loc.y-min.y);
			
			// Undo/redo object positions...
			int position[] = new int[steps.length];
			
			for (int i=0;i<steps.length;i++)
			{
				Point p = steps[i].getLocation();
				String name = steps[i].getName();
				
				steps[i].setLocation(p.x+offset.x, p.y+offset.y);
				steps[i].setDraw(true);
				
				// Check the name, find alternative...
				steps[i].setName( transMeta.getAlternativeStepname(name) );
				transMeta.addStep(steps[i]);
				position[i] = transMeta.indexOfStep(steps[i]);
			}
			
			// Add the hops too...
			for (int i=0;i<hops.length;i++)
			{
			    transMeta.addTransHop(hops[i]);
			}

			// Load the notes...
			Node notesnode = XMLHandler.getSubNode(transnode, "notepads");
			nr = XMLHandler.countNodes(notesnode, "notepad");
			log.logDebug(toString(), "I found "+nr+" notepads to paste.");
			NotePadMeta notes[] = new NotePadMeta[nr];
			
			for (int i=0;i<notes.length;i++)
			{
			    Node notenode = XMLHandler.getSubNodeByNr(notesnode, "notepad", i);
			    notes[i] = new NotePadMeta(notenode);
			    Point p = notes[i].getLocation();
				notes[i].setLocation(p.x+offset.x, p.y+offset.y);
				transMeta.addNote(notes[i]);
			}
			
			// Set the source and target steps ...
			for (int i=0;i<steps.length;i++)
			{
			    StepMetaInterface smi = steps[i].getStepMetaInterface();
			    smi.searchInfoAndTargetSteps(transMeta.getSteps());
			}
			
			// Save undo information too...
			addUndoNew(steps, position, false);

			int hoppos[] = new int[hops.length];
			for (int i=0;i<hops.length;i++) hoppos[i] = transMeta.indexOfTransHop(hops[i]);
			addUndoNew(hops, hoppos, true);
			
			int notepos[] = new int[notes.length];
			for (int i=0;i<notes.length;i++) notepos[i] = transMeta.indexOfNote(notes[i]);
			addUndoNew(notes, notepos, true);
	
			if (transMeta.haveStepsChanged())
			{
				refreshTree();
				refreshGraph();
			}
		}
		catch(KettleException e)
		{
		    new ErrorDialog(shell, props, "Error pasting steps...", "I was unable to paste steps to this transformation", e);
		}
	}
	
	public void copySelected(StepMeta stepMeta[], NotePadMeta notePadMeta[])
	{
		if (stepMeta==null || stepMeta.length==0) return;
		
		String xml = XMLHandler.getXMLHeader();
		xml+="<transformation>"+Const.CR;
		xml+=" <steps>"+Const.CR;

		for (int i=0;i<stepMeta.length;i++)
		{
			xml+=stepMeta[i].getXML();
		}
		
		xml+="    </steps>"+Const.CR;
		
		// 
		// Also check for the hops in between the selected steps...
		//
		
		xml+="<order>"+Const.CR;
		if (stepMeta!=null)
		for (int i=0;i<stepMeta.length;i++)
		{
		    for (int j=0;j<stepMeta.length;j++)
		    {
		        if (i!=j)
		        {
		            TransHopMeta hop = transMeta.findTransHop(stepMeta[i], stepMeta[j]); 
		            if (hop!=null) // Ok, we found one...
		            {
		                xml+=hop.getXML()+Const.CR;
		            }
		        }
		    }
		}
		xml+="  </order>"+Const.CR;
		
		xml+="  <notepads>"+Const.CR;
		if (notePadMeta!=null)
		for (int i=0;i<notePadMeta.length;i++)
		{
			xml+= notePadMeta[i].getXML();
		}
		xml+="   </notepads>"+Const.CR;	

		xml+=" </transformation>"+Const.CR;
		
		toClipboard(xml);
	}

	public void delStep(String name)
	{
		log.logDebug(toString(), "Delete step: "+name);
		
		int i, pos=0;
		StepMeta stepMeta = null, look=null;
					
		for (i=0;i<transMeta.nrSteps() && stepMeta==null;i++)
		{
			look = transMeta.getStep(i);
			if (look.getName().equalsIgnoreCase(name))
			{
				stepMeta=look;
				pos=i;
			}
		}
		if (stepMeta!=null)
		{
			for (i=transMeta.nrTransHops()-1;i>=0;i--)
			{
				TransHopMeta hi = transMeta.getTransHop(i);
				if ( hi.getFromStep().equals(stepMeta) || hi.getToStep().equals(stepMeta) )
				{
					addUndoDelete(new TransHopMeta[] { hi }, new int[] { transMeta.indexOfTransHop(hi) }, true);
				    transMeta.removeTransHop(i);
					refreshTree();
				}
			}
			
			transMeta.removeStep(pos);
			addUndoDelete(new StepMeta[] { stepMeta }, new int[] { pos });
			
			refreshTree();
			refreshGraph();
		}
		else
		{
			log.logDebug(toString(), "Couldn't find step ["+name+"] to delete...");
		}
	}	

	public void editHop(String name)
	{
		TransHopMeta hi = transMeta.findTransHop(name);
		if (hi!=null)
		{
			// Backup situation BEFORE edit:
			TransHopMeta before = (TransHopMeta)hi.clone();
			
			TransHopDialog hd = new TransHopDialog(shell, SWT.NONE, hi, transMeta);
			if (hd.open()!=null)
			{
				// Backup situation for redo/undo:
				TransHopMeta after = (TransHopMeta)hi.clone();
				addUndoChange(new TransHopMeta[] { before }, new TransHopMeta[] { after }, new int[] { transMeta.indexOfTransHop(hi) } );
			
				String newname = hi.toString();
				if (!name.equalsIgnoreCase(newname)) 
				{
					refreshTree(); 
					refreshGraph(); // color, nr of copies...
				}
			}
		}
		setShellText();
	}

	public void delHop(String name)
	{
		int i,n;
		
		n=transMeta.nrTransHops();
		
		for (i=0;i<n;i++)
		{
			TransHopMeta hi = transMeta.getTransHop(i);
			if (hi.toString().equalsIgnoreCase(name))
			{
				addUndoDelete(new Object[] { (TransHopMeta)hi.clone() }, new int[] { transMeta.indexOfTransHop(hi) });
				transMeta.removeTransHop(i);
				refreshTree();
				refreshGraph();
				return;
			}
		}
		setShellText();
	}

	public void newHop(StepMeta fr, StepMeta to)
	{
		TransHopMeta hi = new TransHopMeta(fr, to);
		
		TransHopDialog hd = new TransHopDialog(shell, SWT.NONE, hi, transMeta);
		if (hd.open()!=null)
		{
			boolean error=false;

			if (transMeta.findTransHop(hi.getFromStep(), hi.getToStep())!=null)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("This hop already exists!");
				mb.setText("Error!");
				mb.open();
				error=true;
			}
			
			if (transMeta.hasLoop(fr) || transMeta.hasLoop(to))
			{
				refreshTree();
				refreshGraph();
				
				MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
				mb.setMessage("Adding this hop causes a loop in the transformation.  Loops are not allowed!");
				mb.setText("Warning!");
				mb.open();
				error=true;
			}
			
			if (!error)
			{
				transMeta.addTransHop(hi);
				addUndoNew(new TransHopMeta[] { (TransHopMeta)hi.clone() }, new int[] { transMeta.indexOfTransHop(hi) });
				hi.getFromStep().drawStep();
				hi.getToStep().drawStep();
				refreshTree();
				refreshGraph();
			}
		}
	}

	public void newHop()
	{
		newHop(null, null);
	}
	
	public void newConnection()
	{
		DatabaseMeta db = new DatabaseMeta(); 
		DatabaseDialog con = new DatabaseDialog(shell, SWT.APPLICATION_MODAL, log, db, props);
		String con_name = con.open(); 
		if (con_name!=null && con_name.length()>0)
		{
			transMeta.addDatabase(db);
			addUndoNew(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { transMeta.indexOfDatabase(db) });
			saveConnection(db);
			refreshTree();
		}
	}
	
	public void saveConnection(DatabaseMeta db)
	{
		// Also add to repository?
		if (rep!=null)
		{
			if (!rep.userinfo.isReadonly())
			{
				try
				{
					db.saveRep(rep);
					log.logDetailed(toString(), "Saved database connection ["+db+"] to the repository.");
				}
				catch(KettleException ke)
				{
					new ErrorDialog(shell, props, "Can't save...","Error saving connection ["+db+"] to repository!", ke);
				}
			}
			else
			{
				new ErrorDialog(shell, props, "Can't save...","Error saving connection ["+db+"] to repository!", new KettleException("This repository user is read-only!"));
			}
		}
	}
	
	/**
	 * Shows a 'model has changed' warning if required
	 * @return true if nothing has changed or the changes are rejected by the user.
	 */
	public boolean showChangedWarning()
	{
		boolean answer = true;
		if (transMeta.hasChanged())
		{
            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING );
			mb.setMessage("This model has changed.  Do you want to save it?");
			mb.setText("Warning!");
            int reply = mb.open();
            if (reply==SWT.YES)
            {
                saveFile();
                answer=true;
            }
            else
            {
                if (reply==SWT.CANCEL)
                {
                    answer = false;
                }
                else
                {
                    answer = true;
                }
            }
		}
		return answer;
	}
	
	public void openRepository()
	{
		int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
		RepositoriesDialog rd = new RepositoriesDialog(disp, SWT.NONE, perms, APP_NAME);
		rd.getShell().setImage(GUIResource.getInstance().getImageSpoon());
		if (rd.open())
		{
			// Close previous repository...
			
			if (rep!=null) 
			{
				rep.disconnect();
			}
			
			rep = new Repository(log, rd.getRepository(), rd.getUser());
			if (!rep.connect(APP_NAME))
			{
				rep=null;
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("An error occured connecting to the repository!"+Const.CR+"See the log for more information.");
				mb.setText("Error!");
				mb.open();
			}
			
            // Set for the existing databases, the ID's at -1!
            for (int i=0;i<transMeta.nrDatabases();i++) 
            {
                transMeta.getDatabase(i).setID(-1L);
            }
            
            // Set for the existing transformation the ID at -1!
            transMeta.setID(-1L);

            // Keep track of the old databases for now.
            ArrayList oldDatabases = transMeta.getDatabases();
            
            // In order to re-match the databases on name (not content), we need to load the databases from the new repository.
            // NOTE: for purposes such as DEVELOP - TEST - PRODUCTION sycles.
            
            // first clear the list of databases.
            transMeta.setDatabases(new ArrayList());

            // Read them from the new repository.
            readDatabases(); 
            
            /*
            for (int i=0;i<transMeta.nrDatabases();i++)
            {
                System.out.println("NEW REP: ["+transMeta.getDatabase(i).getName()+"]");
            }
            */
			
            // Then we need to re-match the databases at save time...
            for (int i=0;i<oldDatabases.size();i++)
            {
                DatabaseMeta oldDatabase = (DatabaseMeta) oldDatabases.get(i);
                DatabaseMeta newDatabase = Const.findDatabase(transMeta.getDatabases(), oldDatabase.getName());
                
                // If it exists, change the settings...
                if (newDatabase!=null)
                {
                    // System.out.println("Found the new database in the repository ["+oldDatabase.getName()+"]");
                    // A database connection with the same name exists in the new repository.
                    // Change the old connections to reflect the settings in the new repository 
                    oldDatabase.setDatabaseInterface(newDatabase.getDatabaseInterface());
                }
                else
                {
                    // System.out.println("Couldn't find the new database in the repository ["+oldDatabase.getName()+"]");
                    // The old database is not present in the new repository: simply add it to the list.
                    // When the transformation gets saved, it will be added to the repository.
                    transMeta.addDatabase(oldDatabase);
                }
            }
			
			// For the existing transformation, change the directory too:
			// Try to find the same directory in the new repository...
			RepositoryDirectory redi = rep.getDirectoryTree().findDirectory(transMeta.getDirectory().getPath());
			if (redi!=null)
			{
			    transMeta.setDirectory(redi);
			}
			else
			{
			    transMeta.setDirectory(rep.getDirectoryTree()); // the root is the default!
			}
			
			refreshTree(true);
			setShellText();
		}
		else
		{
			// Not cancelled? --> Clear repository...
			if (!rd.isCancelled())
			{
				closeRepository();
			}
		}
	}
	
	public void exploreRepository()
	{
	    if (rep!=null)
	    {
			RepositoryExplorerDialog erd = new RepositoryExplorerDialog(shell, SWT.NONE, rep, rep.getUserInfo());
			String objname = erd.open();
			if (objname!=null)
			{
				String object_type = erd.getObjectType();
				RepositoryDirectory repdir = erd.getObjectDirectory();
				
				// System.out.println("Load ["+object_type+"] --> ["+objname+"] from dir ["+(repdir==null)+"]");
				
				// Try to open it as a transformation.
				if (object_type.equals(RepositoryExplorerDialog.STRING_TRANSFORMATIONS))
				{
					if (showChangedWarning())
					{
						try
						{
							transMeta = new TransMeta(rep, objname, repdir);
							transMeta.clearChanged();
							setFilename(objname);
							refreshTree();
							refreshGraph();
						}
						catch(KettleException e)
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
							mb.setMessage("Error opening : "+objname+Const.CR+e.getMessage());
							mb.setText("Error!");
							mb.open();
						}
					}
				}
			}
	    }
	}
	
	public void editRepositoryUser()
	{
		if (rep!=null)
		{
			UserInfo userinfo = rep.getUserInfo();
			UserDialog ud = new UserDialog(shell, SWT.NONE, log, props, rep, userinfo);
			UserInfo ui = ud.open();
			if (!userinfo.isReadonly())
			{
				if (ui!=null)
				{
					try
					{
						ui.saveRep(rep);
					}
					catch(KettleException e)
					{
						MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
						mb.setMessage("Sorry, I was unable to change this user in the repository: "+Const.CR+e.getMessage());
						mb.setText("Edit user");
						mb.open();
					}
			 	}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				mb.setMessage("Sorry, you are not allowed to change this user.");
				mb.setText("Edit user");
				mb.open();
			}		
		}
	}
	
	public void readDatabases()
	{
		transMeta.readDatabases(rep);
	}

	public void closeRepository()
	{
		if (rep!=null) rep.disconnect();
		rep = null;
		setShellText();
	}

	public void openFile(boolean importfile)
	{
		if (showChangedWarning())
		{
			if (rep==null || importfile)  // Load from XML
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				// dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
				dialog.setFilterExtensions(Const.STRING_TRANS_FILTER_EXT);
				dialog.setFilterNames(Const.STRING_TRANS_FILTER_NAMES);
				String fname = dialog.open();
				if (fname!=null)
				{
					try
					{
						transMeta = new TransMeta(fname);
						props.addLastFile(Props.TYPE_PROPERTIES_MENU, fname, RepositoryDirectory.DIRECTORY_SEPARATOR, false, "");
						addMenuLast();
						if (!importfile) transMeta.clearChanged();
						setFilename(fname);
					}
					catch(KettleException e)
					{
						clear();
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage("Error opening : "+fname+Const.CR+e.getMessage());
						mb.setText("Error!");
						mb.open();
					}

					refreshGraph();
					refreshTree(true);
				}
			}
			else // Read a transformation from the repository!
			{
				SelectObjectDialog sod = new SelectObjectDialog(shell, props, rep, true, false, false);
				String transname            = sod.open();
				RepositoryDirectory repdir = sod.getDirectory();
				if (transname!=null && repdir!=null)
				{
					TransLoadProgressDialog tlpd = new TransLoadProgressDialog(shell, rep, transname, repdir);
					TransMeta transInfo = tlpd.open();
					if (transInfo!=null)
					{
						transMeta = transInfo;
						// transMeta = new TransInfo(log, rep, transname, repdir);
                        log.logDetailed(toString(), "Transformation ["+transname+"] in directory ["+repdir+"] loaded from the repository.");
						//System.out.println("name="+transMeta.getName());
						props.addLastFile(Props.TYPE_PROPERTIES_SPOON, transname, repdir.getPath(), true, rep.getName());
						addMenuLast();
						transMeta.clearChanged();
						setFilename(transname);
					}
					refreshGraph();
					refreshTree(true);
				}
			}
		}
	}
	
	public void newFile()
	{
		if (showChangedWarning())
		{ 
			clear();
			loadRepositoryObjects();    // Add databases if connected to repository
			setFilename(null);
			refreshTree(true);
			refreshGraph();
		}
	}
	
	public void loadRepositoryObjects()
	{
		// Load common database info from active repository...
		if (rep!=null)
		{
			transMeta.readDatabases(rep);
		}
	}
	
	public boolean quitFile()
	{
        boolean exit        = true;
        boolean showWarning = true;
		
		log.logDetailed(toString(), "Quit application.");
		saveSettings();
		if (transMeta.hasChanged())
		{
			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING );
			mb.setMessage("File has changed!  Do you want to save first?");
			mb.setText("Warning!");
			int answer = mb.open();
		
			switch(answer)
			{
			case SWT.YES: saveFile(); exit=true; showWarning=false; break;
			case SWT.NO:  exit=true; showWarning=false; break;
			case SWT.CANCEL: 
                exit=false;
                showWarning=false;
                break;
			}
		}
        
		// System.out.println("exit="+exit+", showWarning="+showWarning+", running="+spoonlog.isRunning()+", showExitWarning="+props.showExitWarning());
        
        // Show warning on exit when spoon is still running
        // Show warning on exit when a warning needs to be displayed, but only if we didn't ask to save before. (could have pressed cancel then!)
        // 
        if ( (exit && spoonlog.isRunning() ) ||
             (exit && showWarning && props.showExitWarning() )
           )
        {
            String message = "Are you sure you want to exit?"; 
            if (spoonlog.isRunning()) message = "There is a running transformation.  Are you sure you want to exit?";
            
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                    "Warning!", 
                    null,
                    message,
                    MessageDialog.WARNING,
                    new String[] { "Yes", "No" },
                    1,
                    "Please, don't show this warning anymore.",
                    !props.showExitWarning()
               );
               int idx = md.open();
               props.setExitWarningShown(!md.getToggleState());
               props.saveProps();
               
               if (idx==1) exit=false; // No selected: don't exit!
               else exit=true;
        }            

        if (exit) dispose();
            
		return exit;
	}
	
	public void saveFile()
	{
		log.logDetailed(toString(), "Save to file or repository...");
		
		if (rep!=null)
		{
			saveRepository();
		}
		else
		{
			if (transMeta.getFilename()!=null)
			{
				save(transMeta.getFilename());
			}
			else
			{
				saveFileAs();
			}
		}
		
		try
		{
			transMeta.getDbCache().saveCache(log);
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "An error occured saving the database cache to disk", e);
		}
	}
	
	public void saveRepository()
	{
		saveRepository(false);
	}

	public void saveRepository(boolean ask_name)
	{
		log.logDetailed(toString(), "Save to repository...");
		if (rep!=null)
		{
			boolean answer = true;
			boolean ask    = ask_name;
			while (answer && ( ask || transMeta.getName()==null || transMeta.getName().length()==0 ) )
			{
				if (!ask)
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					mb.setMessage("Please give this transformation a name before saving it in the database.");
					mb.setText("Transformation has no name.");
					mb.open();
				}
				ask=false;
				answer = setTrans();
				// System.out.println("answer="+answer+", ask="+ask+", transMeta.getName()="+transMeta.getName());
			}
			
			if (answer && transMeta.getName()!=null && transMeta.getName().length()>0)
			{
				if (!rep.getUserInfo().isReadonly())
				{
					int response = SWT.YES;
					if (transMeta.showReplaceWarning(rep))
					{
						MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
						mb.setMessage("There already is a transformation called ["+transMeta.getName()+"] in the repository."+Const.CR+"Do you want to overwrite the transformation?");
						mb.setText("Overwrite?");
						response = mb.open();
					}
					
					if (response == SWT.YES)
					{
						shell.setCursor(cursor_hourglass);

						// Keep info on who & when this transformation was changed...
						transMeta.setModifiedDate( new Value("MODIFIED_DATE", Value.VALUE_TYPE_DATE) ); 				
						transMeta.getModifiedDate().sysdate();
						transMeta.setModifiedUser( rep.getUserInfo().getLogin() );

						TransSaveProgressDialog tspd = new TransSaveProgressDialog(log, props, shell, rep, transMeta);
						if (tspd.open())
						{
							if (!props.getSaveConfirmation())
							{
								MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																						 "Save OK!", 
																						 null,
																						 "This transformation was stored in repository",
																						 MessageDialog.QUESTION,
																						 new String[] { "OK!" },
																						 0,
																						 "Don't show this message again.",
																						 props.getSaveConfirmation()
																						 );
								md.open();
								props.setSaveConfirmation(md.getToggleState());
							}
	
							// Handle last opened files...
							props.addLastFile(Props.TYPE_PROPERTIES_SPOON, transMeta.getName(), transMeta.getDirectory().getPath(), true, getRepositoryName());
							saveSettings();
							addMenuLast();
	
							setShellText();
						}
						shell.setCursor(null);
					}
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.CLOSE | SWT.ICON_ERROR);
					mb.setMessage("Sorry, the user you're logged on with, can only read from the repository");
					mb.setText("Transformation not saved!");
					mb.open();
				}
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage("There is no repository connection available.");
			mb.setText("No repository available.");
			mb.open();
		}
	}

	public void saveFileAs()
	{
		log.logBasic(toString(), "Save as...");

		if (rep!=null)
		{
			transMeta.setID(-1L);
			saveRepository(true);
		}
		else
		{
		    saveXMLFile();
		}
	}
	
    private void saveXMLFile()
    {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
		dialog.setFilterExtensions(Const.STRING_TRANS_FILTER_EXT);
		dialog.setFilterNames(Const.STRING_TRANS_FILTER_NAMES);
		String fname = dialog.open();
		if (fname!=null) 
		{
			// Is the filename ending on .ktr, .xml?
			boolean ending=false;
			for (int i=0;i<Const.STRING_TRANS_FILTER_EXT.length-1;i++)
			{
				if (fname.endsWith(Const.STRING_TRANS_FILTER_EXT[i].substring(1))) 
				{
					ending=true;
				} 
			}
			if (fname.endsWith(Const.STRING_TRANS_DEFAULT_EXT)) ending=true;
			if (!ending)
			{
				fname+=Const.STRING_TRANS_DEFAULT_EXT;
			}
			// See if the file already exists...
			File f = new File(fname);
			int id = SWT.YES;
			if (f.exists())
			{
				MessageBox mb = new MessageBox(shell, SWT.NO | SWT.YES | SWT.ICON_WARNING);
				mb.setMessage("This file already exists.  Do you want to overwrite it?");
				mb.setText("This file already exists!");
				id = mb.open();
			}
			if (id==SWT.YES)
			{
				save(fname);
				setFilename(fname);
			}
		} 
    }
	
	private void save(String fname)
	{
		String xml = XMLHandler.getXMLHeader() + transMeta.getXML();
		try
		{
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fname)));
			dos.write(xml.getBytes(Const.XML_ENCODING));
			dos.close();

			// Handle last opened files...
			props.addLastFile(Props.TYPE_PROPERTIES_SPOON, fname, RepositoryDirectory.DIRECTORY_SEPARATOR, false, "");
			saveSettings();
			addMenuLast();
			
			transMeta.clearChanged();
			setShellText();
			log.logDebug(toString(), "File written to ["+fname+"]");
		}
		catch(Exception e)
		{
			log.logDebug(toString(), "Error opening file for writing! --> "+e.toString());
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage("Error saving file:"+Const.CR+e.toString());
			mb.setText("ERROR");
			mb.open();
		}
	}
	
	public void helpAbout()
	{
		MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION | SWT.CENTER);
		String mess = "Kettle - Spoon version "+Const.VERSION+Const.CR+Const.CR+Const.CR;
		mess+="(c) 2001-2004 i-Bridge bvba"+Const.CR+"         www.kettle.be"+Const.CR;
		
		mb.setMessage(mess);
		mb.setText(APP_NAME);
		mb.open();
	}

	public void editUnselectAll()
	{
		transMeta.unselectAll(); 
		spoongraph.redraw();
	}
	
	public void editSelectAll()
	{
		transMeta.selectAll(); 
		spoongraph.redraw();
	}
	
	public void editOptions()
	{
		EnterOptionsDialog eod = new EnterOptionsDialog(shell, props);
		if (eod.open()!=null)
		{
			props.saveProps();
			loadSettings();
			changeLooks();
		} 
	}
	
	public int getTreePosition(TreeItem ti, String item)
	{
		if (ti!=null)
		{
			TreeItem items[] = ti.getItems();
			for (int x=0;x<items.length;x++)
			{
				if (items[x].getText().equalsIgnoreCase(item))
				{
					return x;
				}
			}
		}
		return -1;
	}

	public void refreshTree()
	{
		refreshTree(false);
        refreshPluginHistory();
	}

	public void refreshTree(boolean complete)
	{
		if (!transMeta.hasChanged() && !complete) return;  // Nothing changed: nothing to do!
		
		int idx;
		TreeItem ti[];
		
		// Refresh the connections...
		//
		if (transMeta.haveConnectionsChanged() || complete)
		{
			// TreeItem tiConn= this.tiConn (TreeItem)widgets.getWidget(STRING_CONNECTIONS);
			ti = tiConn.getItems();

			// In complete refresh: delete all items first
			if (complete)
			{
				for (int i=0;i<ti.length;i++) ti[i].dispose();
				ti = tiConn.getItems();
			}
			
			// First delete no longer used items...
			for (int i=0;i<ti.length;i++)
			{
				String str = ti[i].getText();
				DatabaseMeta inf = transMeta.findDatabase(str);
				if (inf!=null) idx = transMeta.indexOfDatabase(inf); else idx=-1;
				if (idx<0 || idx>i) ti[i].dispose();
			}
			ti = tiConn.getItems();
			
			// Insert missing items in tree...
			int j=0;
			for (int i=0;i<transMeta.nrDatabases();i++)
			{
				DatabaseMeta inf = transMeta.getDatabase(i);
				String con_name = inf.getName();
				String ti_name = "";
				if (j<ti.length) ti_name = ti[j].getText();
				if (!con_name.equalsIgnoreCase(ti_name))
				{
					// insert at position j in tree
					TreeItem newitem = new TreeItem(tiConn, j);
					newitem.setText(inf.getName());
					newitem.setForeground(GUIResource.getInstance().getColorBlack());
					newitem.setImage(GUIResource.getInstance().getImageConnection());
					j++;
					ti = tiConn.getItems();
				}
				else
				{
					j++;
				}
			}
			// tiConn.setExpanded(true);
		}

		//ni.setImage(gv.hop_image);
		//ni.setImage(gv.step_images_small[steptype]);
		
		// Refresh the Steps...
		//
		if (transMeta.haveStepsChanged() || complete)
		{
			ti = tiStep.getItems();

			// In complete refresh: delete all items first
			if (complete)
			{
				for (int i=0;i<ti.length;i++) ti[i].dispose();
				ti = tiStep.getItems();
			}

			// First delete no longer used items...
			log.logDebug(toString(), "check steps");
			for (int i=0;i<ti.length;i++)
			{
				String str = ti[i].getText();
				log.logDebug(toString(), "  check step tree item #"+i+" : ["+str+"]");
				StepMeta inf = transMeta.findStep(str);
				if (inf!=null) idx = transMeta.indexOfStep(inf); else idx=-1;
				if (idx<0 || idx>i) 
				{
					log.logDebug(toString(), "     remove tree item ["+str+"]");
					ti[i].dispose();
				}
			}
			ti = tiStep.getItems();
			
			// Insert missing items in tree...
			int j=0;
			for (int i=0;i<transMeta.nrSteps();i++)
			{
				StepMeta inf = transMeta.getStep(i);
				String step_name = inf.getName();
				String step_desc = inf.getStepID();
				String ti_name = "";
				if (j<ti.length) ti_name = ti[j].getText();
				if (!step_name.equalsIgnoreCase(ti_name))
				{
					// insert at position j in tree
					TreeItem newitem = new TreeItem(tiStep, j);
					newitem.setText(inf.getName());
					// Set the small image...
					Image img = (Image)GUIResource.getInstance().getImagesStepsSmall().get(step_desc);
					newitem.setImage(img);
					j++;
					ti = tiStep.getItems();
				}
				else
				{
					j++;
				}
			}
			
			// See if the colors are still OK!
			for (int i=0;i<ti.length;i++)
			{
				StepMeta inf = transMeta.findStep(ti[i].getText());
				Color col = ti[i].getForeground();
				Color newcol;
				if (transMeta.isStepUsedInTransHops(inf)) newcol=GUIResource.getInstance().getColorBlack(); else newcol=GUIResource.getInstance().getColorGray();
				if (!newcol.equals(col)) ti[i].setForeground(newcol);
			}

			//tiStep.setExpanded(true);
		}
		
		// Refresh the Hops...
		//
		if (transMeta.haveHopsChanged() || complete)
		{
			ti = tiHops.getItems();

			// In complete refresh: delete all items first
			if (complete)
			{
				for (int i=0;i<ti.length;i++) ti[i].dispose();
				ti = tiHops.getItems();
			}

			// First delete no longer used items...
			for (int i=0;i<ti.length;i++)
			{
				String str = ti[i].getText();
				TransHopMeta inf = transMeta.findTransHop(str);
				if (inf!=null) idx = transMeta.indexOfTransHop(inf); else idx=-1;
				if (idx<0 || idx>i) ti[i].dispose();
			}
			ti = tiHops.getItems();
			
			// Insert missing items in tree...
			int j=0;
			for (int i=0;i<transMeta.nrTransHops();i++)
			{
				TransHopMeta inf = transMeta.getTransHop(i);
				String trans_name = inf.toString();
				String ti_name = "";
				if (j<ti.length) ti_name = ti[j].getText();
				if (!trans_name.equalsIgnoreCase(ti_name))
				{
					// insert at position j in tree
					TreeItem newitem = new TreeItem(tiHops, j);
					newitem.setText(inf.toString());
					newitem.setForeground(GUIResource.getInstance().getColorBlack());
					newitem.setImage(GUIResource.getInstance().getImageHop());
					j++;
					ti = tiHops.getItems();
				}
				else
				{
					j++;
				}
			}
			// tiTrns.setExpanded(false);
		}

		selectionTree.setFocus();
		setShellText();
	}
	
	public void refreshGraph()
	{
		spoongraph.redraw();
		setShellText();
	}

	public StepMeta newStep()
	{
		return newStep(true, true);
	}
	
	public StepMeta newStep(boolean openit, boolean rename)
	{
		TreeItem ti[] = selectionTree.getSelection();
		StepMeta inf = null;
		
		if (ti.length==1)
		{
			String steptype = ti[0].getText();
			log.logDebug(toString(), "New step: "+steptype);
			
			inf = newStep(steptype, steptype, openit, rename);
		}

		return inf;
	}

	/**
	 * Allocate new step, optionally open and rename it.
	 * 
	 * @param name Name of the new step
	 * @param description Description of the type of step
	 * @param openit Open the dialog for this step?
	 * @param rename Rename this step?
	 * 
	 * @return The newly created StepMeta object.
	 * 
	 */
	public StepMeta newStep(String name, String description, boolean openit, boolean rename)
	{
		StepMeta inf = null;
		
		// See if we need to rename the step to avoid doubles!
		if (rename && transMeta.findStep(name)!=null)
		{
			int i=2;
			String newname = name+" "+i;
			while (transMeta.findStep(newname)!=null)
			{
				i++;
				newname = name+" "+i;
			}
			name=newname;
		}

        StepLoader steploader = StepLoader.getInstance();
        StepPlugin stepPlugin = null;
        
		try
		{
			stepPlugin = steploader.findStepPluginWithDescription(description);
			if (stepPlugin!=null)
			{
				StepMetaInterface info = BaseStep.getStepInfo(stepPlugin, steploader);
	
				info.setDefault();
				
				if (openit)
				{
					StepDialogInterface dialog = info.getDialog(shell, info, transMeta, name);
					name = dialog.open();
				}
				inf=new StepMeta(log, stepPlugin.getID(), name, info);
	
				if (name!=null) // OK pressed in the dialog: we have a step-name
				{
					String newname=name;
					StepMeta stepMeta = transMeta.findStep(newname);
					int nr=2;
					while (stepMeta!=null)
					{
						newname = name+" "+nr;
						stepMeta = transMeta.findStep(newname);
						nr++;
					}
					if (nr>2)
					{
						inf.setName(newname);
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
						mb.setMessage("This stepname already exists.  Spoon changed the stepname to ["+newname+"]");
						mb.setText("Info!");
						mb.open();
					}
					inf.setLocation(20, 20); // default location at (20,20)
					transMeta.addStep(inf);
		
					// Save for later:
					// if openit is false: we drag&drop it onto the canvas!
					if (openit)
					{	
						addUndoNew(new StepMeta[] { inf }, new int[] { transMeta.indexOfStep(inf) });
					}
                    
                    // Also store it in the pluginHistory list...
                    props.addPluginHistory(stepPlugin.getID());
		
					refreshTree();
				}
				setShellText();
			}	
		}
		catch(KettleException e)
		{
            String filename = stepPlugin.getErrorHelpFile();
            if (stepPlugin!=null && filename!=null)
            {
                // OK, in stead of a normal error message, we give back the content of the error help file... (HTML)
                try
                {
                    StringBuffer content=new StringBuffer();
                    
                    System.out.println("Filename = "+filename);
                    FileInputStream fis = new FileInputStream(new File(filename));
                    int ch = fis.read();
                    while (ch>=0)
                    {
                        content.append( (char)ch);
                        ch = fis.read();
                    }

                    System.out.println("Content = "+content);
                    ShowBrowserDialog sbd = new ShowBrowserDialog(shell, "Error help text", content.toString());
                    sbd.open();
                }
                catch(Exception ex)
                {
                    new ErrorDialog(shell, props, "Error showing help text", "I was unable to display error help text!", ex);
                }
            }
            else
            {
    		    new ErrorDialog(shell, props, "Error creating step", "I was unable to create a new step", e);
            }
    		return null;
		}
        catch(Throwable e)
        {
            new ErrorDialog(shell, props, "Error creating step", "I was unable to create a new step", new Exception(e));
            return null;
        }
				
		return inf;
	}

	private void setTreeImages()
	{
		tiConn.setImage(GUIResource.getInstance().getImageConnection());
		tiHops.setImage(GUIResource.getInstance().getImageHop());
		tiStep.setImage(GUIResource.getInstance().getImageBol());
		tiBase.setImage(GUIResource.getInstance().getImageBol());
		tiPlug.setImage(GUIResource.getInstance().getImageBol());
		
		TreeItem tiBaseCat[]=tiBase.getItems();
		for (int x=0;x<tiBaseCat.length;x++)
		{
			tiBaseCat[x].setImage(GUIResource.getInstance().getImageBol());
			
			TreeItem ti[] = tiBaseCat[x].getItems();
			for (int i=0;i<ti.length;i++)
			{
				TreeItem stepitem = ti[i];
				String description = stepitem.getText();
				
				StepLoader steploader = StepLoader.getInstance();
				StepPlugin sp = steploader.findStepPluginWithDescription(description);
				if (sp!=null)
				{
					Image stepimg = (Image)GUIResource.getInstance().getImagesStepsSmall().get(sp.getID());
					if (stepimg!=null)
					{
						stepitem.setImage(stepimg);
					}
				}
			}
		}
		TreeItem tiPlugCat[]=tiPlug.getItems();
		for (int x=0;x<tiPlugCat.length;x++)
		{
			tiPlugCat[x].setImage(GUIResource.getInstance().getImageBol());
			
			TreeItem ti[] = tiPlugCat[x].getItems();
			for (int i=0;i<ti.length;i++)
			{
				TreeItem stepitem = ti[i];
				String description = stepitem.getText();
				StepLoader steploader = StepLoader.getInstance();
				StepPlugin sp = steploader.findStepPluginWithDescription(description);
				if (sp!=null)
				{
					Image stepimg = (Image)GUIResource.getInstance().getImagesStepsSmall().get(sp.getID());
					if (stepimg!=null)
					{
						stepitem.setImage(stepimg);
					}
				}
			}
		}
	}
	
	public DatabaseMeta getConnection(String name)
	{
		int i;
		
		for (i=0;i<transMeta.nrDatabases();i++)
		{
			DatabaseMeta ci = transMeta.getDatabase(i);
			if (ci.getName().equalsIgnoreCase(name))
			{
				return ci;
			}
		}
		return null;
	}

	public void setShellText()
	{
		String fname = transMeta.getFilename();
		if (shell.isDisposed()) return;

		if (rep!=null)
		{
			String repository = "["+getRepositoryName()+"]";
			String transname  = transMeta.getName();
			if (transname==null) transname="[no name]";
			shell.setText(APPL_TITLE+" - "+repository+"   "+transname+(transMeta.hasChanged()?" (changed)":""));
		}
		else
		{
			String repository = "[no repository]";
			if (fname!=null)
			{
				shell.setText(APPL_TITLE+" - "+repository+"   File: "+fname+(transMeta.hasChanged()?" (changed)":""));
			}
			else
			{
				shell.setText(APPL_TITLE+" - "+repository+"   "+(transMeta.hasChanged()?" (changed)":""));
			}
		}
		
	}
	
	public void setFilename(String fname)
	{
		if (fname!=null) transMeta.setFilename(fname);
		setShellText();
	}
	
	private void printFile()
	{
		PrintSpool ps = new PrintSpool();
		Printer printer = ps.getPrinter(shell);
		
		// Create an image of the screen
		Point max = transMeta.getMaximum();
		
		PaletteData pal = ps.getPaletteData();		
		
		ImageData imd = new ImageData(max.x, max.y, printer.getDepth(), pal);
		Image img = new Image(printer, imd);
		
		GC img_gc = new GC(img);
		
		// Clear the background first, fill with background color...
		if (props.getBackgroundRGB()!=null)
        {
            Color bg = new Color(printer, props.getBackgroundRGB());
            img_gc.setForeground(bg);
            bg.dispose();
        }
        img_gc.fillRectangle(0,0,max.x, max.y);
		
		// Draw the transformation...
		spoongraph.drawTrans(img_gc);
		
		//ShowImageDialog sid = new ShowImageDialog(shell, props, img);
		//sid.open();
		
		ps.printImage(shell, props, img);
		
		img_gc.dispose();
		img.dispose();
		ps.dispose();
	}
	
	private boolean setTrans()
	{
		TransDialog tid = new TransDialog(shell, SWT.NONE, transMeta, rep);
		TransMeta ti = tid.open();
		setShellText();
		return ti!=null;
	}
	
	public void saveSettings()
	{
		WindowProperty winprop = new WindowProperty(shell);
		winprop.setName(APPL_TITLE);
		props.setScreen(winprop);
		
		props.setLogLevel(log.getLogLevelDesc());
		props.setLogFilter(log.getFilter());
		props.setSashWeights(sashform.getWeights());
		props.saveProps();
	}

	public void loadSettings()
	{
		log.setLogLevel(props.getLogLevel());
		log.setFilter(props.getLogFilter());

        transMeta.setMaxUndo(props.getMaxUndo());
		transMeta.getDbCache().setActive(props.useDBCache());
	}
	
	public void changeLooks()
	{
        props.setLook(selectionTree);
        props.setLook(tabfolder, Props.WIDGET_STYLE_TAB);
		
		spoongraph.newProps();

		refreshTree();
		refreshGraph();
	}
	
	public void undoAction()
	{
		spoongraph.forceFocus();
		
		TransAction ta = transMeta.previousUndo();
		if (ta==null) return;
		
		setUndoMenu(); // something changed: change the menu
		switch(ta.getType())
		{
			//
			// NEW
			//

			// We created a new step : undo this...
			case TransAction.TYPE_ACTION_NEW_STEP:
				// Delete the step at correct location:
			    for (int i=ta.getCurrent().length-1;i>=0;i--)
				{
					int idx = ta.getCurrentIndex()[i];
					transMeta.removeStep(idx);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We created a new connection : undo this...
			case TransAction.TYPE_ACTION_NEW_CONNECTION:
				// Delete the connection at correct location:
			    for (int i=ta.getCurrent().length-1;i>=0;i--)
				{
					int idx = ta.getCurrentIndex()[i];
					transMeta.removeDatabase(idx);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We created a new note : undo this...
			case TransAction.TYPE_ACTION_NEW_NOTE:
				// Delete the note at correct location:
			    for (int i=ta.getCurrent().length-1;i>=0;i--)
				{
					int idx = ta.getCurrentIndex()[i];
					transMeta.removeNote(idx);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We created a new hop : undo this...
			case TransAction.TYPE_ACTION_NEW_HOP:
				// Delete the hop at correct location:
			    for (int i=ta.getCurrent().length-1;i>=0;i--)
				{
					int idx = ta.getCurrentIndex()[i];
					transMeta.removeTransHop(idx);
				}
				refreshTree();
				refreshGraph();
				break;

			//
			// DELETE
			//

			// We delete a step : undo this...
			case TransAction.TYPE_ACTION_DELETE_STEP:
				// un-Delete the step at correct location: re-insert
			    for (int i=0;i<ta.getCurrent().length;i++)
				{
					StepMeta stepMeta = (StepMeta)ta.getCurrent()[i];
					int idx = ta.getCurrentIndex()[i];
					transMeta.addStep(idx, stepMeta);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We deleted a connection : undo this...
			case TransAction.TYPE_ACTION_DELETE_CONNECTION:
				// re-insert the connection at correct location:
			    for (int i=0;i<ta.getCurrent().length;i++)
				{
					DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[i];
					int idx = ta.getCurrentIndex()[i];
					transMeta.addDatabase(idx, ci);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We delete new note : undo this...
			case TransAction.TYPE_ACTION_DELETE_NOTE:
				// re-insert the note at correct location:
			    for (int i=0;i<ta.getCurrent().length;i++)
				{
					NotePadMeta ni = (NotePadMeta)ta.getCurrent()[i];
					int idx = ta.getCurrentIndex()[i];
					transMeta.addNote(idx, ni);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We deleted a hop : undo this...
			case TransAction.TYPE_ACTION_DELETE_HOP:
				// re-insert the hop at correct location:
			    for (int i=0;i<ta.getCurrent().length;i++)
				{
					TransHopMeta hi = (TransHopMeta)ta.getCurrent()[i];
					int idx = ta.getCurrentIndex()[i];
					// Build a new hop:
					StepMeta from = transMeta.findStep(hi.getFromStep().getName());
					StepMeta to   = transMeta.findStep(hi.getToStep().getName());
					TransHopMeta hinew = new TransHopMeta(from, to);
					transMeta.addTransHop(idx, hinew);
				}
				refreshTree();
				refreshGraph();
				break;


			//
			// CHANGE
			//

			// We changed a step : undo this...
			case TransAction.TYPE_ACTION_CHANGE_STEP:
				// Delete the current step, insert previous version.
			    for (int i=0;i<ta.getCurrent().length;i++)
				{
					StepMeta prev = (StepMeta)ta.getPrevious()[i];
					int idx = ta.getCurrentIndex()[i];

					transMeta.removeStep(idx);
					transMeta.addStep(idx, prev);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We changed a connection : undo this...
			case TransAction.TYPE_ACTION_CHANGE_CONNECTION:
				// Delete & re-insert
			    for (int i=0;i<ta.getCurrent().length;i++)
				{
					DatabaseMeta prev = (DatabaseMeta)ta.getPrevious()[i];
					int idx = ta.getCurrentIndex()[i];

					transMeta.removeDatabase(idx);
					transMeta.addDatabase(idx, prev);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We changed a note : undo this...
			case TransAction.TYPE_ACTION_CHANGE_NOTE:
				// Delete & re-insert
			    for (int i=0;i<ta.getCurrent().length;i++)
				{
					int idx = ta.getCurrentIndex()[i];
					transMeta.removeNote(idx);
					NotePadMeta prev = (NotePadMeta)ta.getPrevious()[i];
					transMeta.addNote(idx, prev);
				}
				refreshTree();
				refreshGraph();
				break;
	
			// We changed a hop : undo this...
			case TransAction.TYPE_ACTION_CHANGE_HOP:
				// Delete & re-insert
			    for (int i=0;i<ta.getCurrent().length;i++)
				{
					TransHopMeta prev = (TransHopMeta)ta.getPrevious()[i];
					int idx = ta.getCurrentIndex()[i];

					transMeta.removeTransHop(idx);
					transMeta.addTransHop(idx, prev);
				}
				refreshTree();
				refreshGraph();
				break;

			//
			// POSITION
			//
				
			// The position of a step has changed: undo this...
			case TransAction.TYPE_ACTION_POSITION_STEP:
				// Find the location of the step:
				for (int i = 0; i < ta.getCurrentIndex().length; i++) 
				{
					StepMeta stepMeta = transMeta.getStep(ta.getCurrentIndex()[i]);
					stepMeta.setLocation(ta.getPreviousLocation()[i]);
				}
				refreshGraph();
				break;
	
			// The position of a note has changed: undo this...
			case TransAction.TYPE_ACTION_POSITION_NOTE:
			    for (int i=0;i<ta.getCurrentIndex().length;i++)
			    {
					int idx = ta.getCurrentIndex()[i];
					NotePadMeta npi = transMeta.getNote(idx);
					Point prev = ta.getPreviousLocation()[i];
					npi.setLocation(prev);
			    }
				refreshGraph();
				break;
			default: break;
		}
		
		// OK, now check if we need to do this again...
		if (transMeta.viewNextUndo()!=null)
		{
			if (transMeta.viewNextUndo().getNextAlso()) undoAction();
		}
	}
	
	public void redoAction()
	{
		spoongraph.forceFocus();

		TransAction ta = transMeta.nextUndo();
		if (ta==null) return;
		setUndoMenu(); // something changed: change the menu
		switch(ta.getType())
		{
		//
		// NEW
		//
		case TransAction.TYPE_ACTION_NEW_STEP:
			// re-delete the step at correct location:
		    for (int i=0;i<ta.getCurrent().length;i++)
			{
				StepMeta stepMeta = (StepMeta)ta.getCurrent()[i];
				int idx = ta.getCurrentIndex()[i];
				transMeta.addStep(idx, stepMeta);
								
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_CONNECTION:
			// re-insert the connection at correct location:
		    for (int i=0;i<ta.getCurrent().length;i++)
			{
				DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[i];
				int idx = ta.getCurrentIndex()[i];
				transMeta.addDatabase(idx, ci);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_NOTE:
			// re-insert the note at correct location:
		    for (int i=0;i<ta.getCurrent().length;i++)
			{
				NotePadMeta ni = (NotePadMeta)ta.getCurrent()[i];
				int idx = ta.getCurrentIndex()[i];
				transMeta.addNote(idx, ni);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_HOP:
			// re-insert the hop at correct location:
		    for (int i=0;i<ta.getCurrent().length;i++)
			{
				TransHopMeta hi = (TransHopMeta)ta.getCurrent()[i];
				int idx = ta.getCurrentIndex()[i];
				transMeta.addTransHop(idx, hi);
				refreshTree();
				refreshGraph();
			}
			break;
		
		//	
		// DELETE
		//
		case TransAction.TYPE_ACTION_DELETE_STEP:
			// re-remove the step at correct location:
		    for (int i=ta.getCurrent().length-1;i>=0;i--)
			{
				int idx = ta.getCurrentIndex()[i];
				transMeta.removeStep(idx);
			}
			refreshTree();
			refreshGraph();
			break;

		case TransAction.TYPE_ACTION_DELETE_CONNECTION:
			// re-remove the connection at correct location:
		    for (int i=ta.getCurrent().length-1;i>=0;i--)
			{
				int idx = ta.getCurrentIndex()[i];
				transMeta.removeDatabase(idx);
			}
			refreshTree();
			refreshGraph();
			break;

		case TransAction.TYPE_ACTION_DELETE_NOTE:
			// re-remove the note at correct location:
		    for (int i=ta.getCurrent().length-1;i>=0;i--)
			{
				int idx = ta.getCurrentIndex()[i];
				transMeta.removeNote(idx);
			}
			refreshTree();
			refreshGraph();
			break;

		case TransAction.TYPE_ACTION_DELETE_HOP:
			// re-remove the hop at correct location:
		    for (int i=ta.getCurrent().length-1;i>=0;i--)
			{
				int idx = ta.getCurrentIndex()[i];
				transMeta.removeTransHop(idx);
			}
			refreshTree();
			refreshGraph();
			break;

		//
		// CHANGE
		//

		// We changed a step : undo this...
		case TransAction.TYPE_ACTION_CHANGE_STEP:
			// Delete the current step, insert previous version.
		    for (int i=0;i<ta.getCurrent().length;i++)
			{
				StepMeta stepMeta = (StepMeta)ta.getCurrent()[i];
				int idx = ta.getCurrentIndex()[i];
				
				transMeta.removeStep(idx);
				transMeta.addStep(idx, stepMeta);
			}
			refreshTree();
			refreshGraph();
			break;

		// We changed a connection : undo this...
		case TransAction.TYPE_ACTION_CHANGE_CONNECTION:
			// Delete & re-insert
		    for (int i=0;i<ta.getCurrent().length;i++)
			{
				DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[i];
				int idx = ta.getCurrentIndex()[i];

				transMeta.removeDatabase(idx);
				transMeta.addDatabase(idx, ci);
			}
			refreshTree();
			refreshGraph();
			break;

		// We changed a note : undo this...
		case TransAction.TYPE_ACTION_CHANGE_NOTE:
			// Delete & re-insert
		    for (int i=0;i<ta.getCurrent().length;i++)
			{
				NotePadMeta ni = (NotePadMeta)ta.getCurrent()[i];
				int idx = ta.getCurrentIndex()[i];

				transMeta.removeNote(idx);
				transMeta.addNote(idx, ni);
			}
			refreshTree();
			refreshGraph();
			break;

		// We changed a hop : undo this...
		case TransAction.TYPE_ACTION_CHANGE_HOP:
			// Delete & re-insert
		    for (int i=0;i<ta.getCurrent().length;i++)
			{
				TransHopMeta hi = (TransHopMeta)ta.getCurrent()[i];
				int idx = ta.getCurrentIndex()[i];

				transMeta.removeTransHop(idx);
				transMeta.addTransHop(idx, hi);
			}
			refreshTree();
			refreshGraph();
			break;

		//
		// CHANGE POSITION
		//
		case TransAction.TYPE_ACTION_POSITION_STEP:
		    for (int i=0;i<ta.getCurrentIndex().length;i++)
			{
				// Find & change the location of the step:
				StepMeta stepMeta = transMeta.getStep(ta.getCurrentIndex()[i]);
				stepMeta.setLocation(ta.getCurrentLocation()[i]);
			}
			refreshGraph();
			break;
		case TransAction.TYPE_ACTION_POSITION_NOTE:
		    for (int i=0;i<ta.getCurrentIndex().length;i++)
			{
				int idx = ta.getCurrentIndex()[i];
				NotePadMeta npi = transMeta.getNote(idx);
				Point curr = ta.getCurrentLocation()[i];
				npi.setLocation(curr);
			}
			refreshGraph();
			break;
		default: break;
		}
		
		// OK, now check if we need to do this again...
		if (transMeta.viewNextUndo()!=null)
		{
			if (transMeta.viewNextUndo().getNextAlso()) redoAction();
		}
	}
	
	public void setUndoMenu()
	{
		TransAction prev = transMeta.viewThisUndo();
		TransAction next = transMeta.viewNextUndo();
		
		if (prev!=null) 
		{
			miEditUndo.setEnabled(true);
			miEditUndo.setText("Undo : "+prev.toString()+" \tCTRL-Z");
		} 
		else            
		{
			miEditUndo.setEnabled(false);
			miEditUndo.setText("Undo : not available \tCTRL-Z");
		} 

		if (next!=null) 
		{
			miEditRedo.setEnabled(true);
			miEditRedo.setText("Redo : "+next.toString()+" \tCTRL-Y");
		} 
		else            
		{
			miEditRedo.setEnabled(false);
			miEditRedo.setText("Redo : not available \tCTRL-Y");			
		} 
	}


	public void addUndoNew(Object obj[], int position[])
	{
		addUndoNew(obj, position, false);
	}	

	public void addUndoNew(Object obj[], int position[], boolean nextAlso)
	{
		// New object?
		transMeta.addUndo(obj, null, position, null, null, TransMeta.TYPE_UNDO_NEW, nextAlso);
		setUndoMenu();
	}	

	// Undo delete object
	public void addUndoDelete(Object obj[], int position[])
	{
		addUndoDelete(obj, position, false);
	}	

	// Undo delete object
	public void addUndoDelete(Object obj[], int position[], boolean nextAlso)
	{
		transMeta.addUndo(obj, null, position, null, null, TransMeta.TYPE_UNDO_DELETE, nextAlso);
		setUndoMenu();
	}	

	// Change of step, connection, hop or note...
	public void addUndoPosition(Object obj[], int pos[], Point prev[], Point curr[])
	{
		addUndoPosition(obj, pos, prev, curr, false);
	}

	// Change of step, connection, hop or note...
	public void addUndoPosition(Object obj[], int pos[], Point prev[], Point curr[], boolean nextAlso)
	{
		// It's better to store the indexes of the objects, not the objects itself!
		transMeta.addUndo(obj, null, pos, prev, curr, TransMeta.TYPE_UNDO_POSITION, nextAlso);
		setUndoMenu();
	}

	// Change of step, connection, hop or note...
	public void addUndoChange(Object from[], Object to[], int[] pos)
	{
		addUndoChange(from, to, pos, false);
	}

	// Change of step, connection, hop or note...
	public void addUndoChange(Object from[], Object to[], int[] pos, boolean nextAlso)
	{
		transMeta.addUndo(from, to, pos, null, null, TransMeta.TYPE_UNDO_CHANGE, nextAlso);
		setUndoMenu();
	}

	
	
	/**
	 * Checks *all* the steps in the transformation, puts the result in remarks list
	 */
	public void checkTrans()
	{
		checkTrans(false);
	}
	

	/**
	 * Check the steps in a transformation
	 * 
	 * @param only_selected True: Check only the selected steps...
	 */
	public void checkTrans(boolean only_selected)
	{
		CheckTransProgressDialog ctpd = new CheckTransProgressDialog(log, props, shell, transMeta, remarks, only_selected);
		ctpd.open(); // manages the remarks arraylist...
		showLastTransCheck();
	}

	/**
	 * Show the remarks of the last transformation check that was run.
	 * @see #checkTrans()
	 */
	public void showLastTransCheck()
	{
		CheckResultDialog crd = new CheckResultDialog(shell, SWT.NONE, remarks);
		String stepname = crd.open();
		if (stepname!=null)
		{
			// Go to the indicated step!
			StepMeta stepMeta = transMeta.findStep(stepname);
			if (stepMeta!=null)
			{
				editStepInfo(stepMeta);
			}
		}
	}

	public void clearDBCache()
	{
		// Determine what menu we selected from...
	
		TreeItem ti[] = selectionTree.getSelection();
				
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				String type = parent.getText();
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) 
				{
					transMeta.getDbCache().clear(name);
				} 
			}
			else
			{
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) transMeta.getDbCache().clear(null);
			}
		}
	}

	public void exploreDB()
	{
		// Determine what menu we selected from...
		TreeItem ti[] = selectionTree.getSelection();
				
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				String type = parent.getText();
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) 
				{
					DatabaseMeta dbinfo = transMeta.findDatabase(name);
					if (dbinfo!=null)
					{
						DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, props, SWT.NONE, dbinfo, transMeta.getDatabases(), true );
						std.open();
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage("Couldn't find connection, please refresh the tree (F5)!");
						mb.setText("Error!");
						mb.open();
					}
				} 
			}
			else
			{
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) transMeta.getDbCache().clear(null);
			}
		}
	}
	
	public void analyseImpact()
	{
		AnalyseImpactProgressDialog aipd = new AnalyseImpactProgressDialog(log, props, shell, transMeta, impact);
		impactHasRun = aipd.open();
		if (impactHasRun) showLastImpactAnalyses();
	}
	
	public void showLastImpactAnalyses()
	{
		ArrayList rows = new ArrayList();
		for (int i=0;i<impact.size();i++)
		{
			DatabaseImpact ii = (DatabaseImpact)impact.get(i);
			rows.add(ii.getRow());
		}
		
		if (rows.size()>0)
		{
			// Display all the rows...
			PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, "-", rows);
			prd.setTitleMessage("Impact analyses", "Result of analyses:");
			prd.open();
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			if (impactHasRun)
			{
				mb.setMessage("As far as I can tell, this transformation has no impact on any database.");
			}
			else
			{
				mb.setMessage("Please run the impact analyses first on this transformation.");
			}
			mb.setText("Impact");
			mb.open();
		}
	}
	
	/**
	 * Get & show the SQL required to run the loaded transformation...
	 *
	 */
	public void getSQL()
	{
		GetSQLProgressDialog pspd = new GetSQLProgressDialog(log, props, shell, transMeta);
		ArrayList stats = pspd.open();
		if (stats!=null) // null means error, but we already displayed the error
		{
			if (stats.size()>0)
			{
				SQLStatementsDialog ssd = new SQLStatementsDialog(shell, SWT.NONE, stats);
				ssd.open();
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
				mb.setMessage("As far as I can tell, no SQL statements need to be executed before this transformation can run.");
				mb.setText("SQL");
				mb.open();
			}
		}
	}
	
	
	public void toClipboard(String cliptext)
	{
	    props.toClipboard(cliptext);
	}
	
	public String fromClipboard()
	{
	    return props.fromClipboard();
	}
	
	/**
	 * Paste transformation from the clipboard...
	 *
	 */
	public void pasteTransformation()
	{
        log.logDetailed(toString(), "Paste transformation from the clipboard!");
	    if (showChangedWarning())
	    {
	        String xml = fromClipboard();
	        try
	        {
	            Document doc = XMLHandler.loadXMLString(xml);
	            transMeta = new TransMeta(XMLHandler.getSubNode(doc, "transformation"));
	            refreshGraph();
	            refreshTree(true);
	        }
	        catch(KettleException e)
	        {
	            new ErrorDialog(shell, props, "Error pasting transformation", "An error occurred pasting a transformation from the clipboard", e);
	        }
	    }
	}
	
    public void copyTransformation()
    {
        toClipboard(XMLHandler.getXMLHeader()+transMeta.getXML());
    }
    
    public void copyTransformationImage()
    {
        Clipboard clipboard = props.getNewClipboard();
        
        Point area = transMeta.getMaximum();
        Image image = spoongraph.getTransformationImage(Display.getCurrent(), area.x, area.y);
        clipboard.setContents(new Object[] { image.getImageData() }, new Transfer[]{ImageDataTransfer.getInstance()});
        
        
        /**
        System.out.println("image obtained: "+area.x+"x"+area.y);
        
        ShowImageDialog sid = new ShowImageDialog(shell, image);
        sid.open();
        */
    }

	
	/**
	 * Create a transformation that extracts tables & data from a database.<p><p>
	 * 
	 * 0) Select the database to rip<p>
	 * 1) Select the table in the database to copy<p>
	 * 2) Select the database to dump to<p>
	 * 3) Select the repository directory in which it will end up<p>
	 * 4) Select a name for the new transformation<p>
	 * 6) Create 1 transformation for the selected table<p> 
	 */
	private void createDatabaseWizard()
	{
		final DatabaseMeta newDBInfo = new DatabaseMeta();
		
		final CreateDatabaseWizardPage1 page1 = new CreateDatabaseWizardPage1("1", props, newDBInfo, transMeta.getDatabases());
		page1.createControl(shell);
		
		final  CreateDatabaseWizardPageInformix pageifx = new CreateDatabaseWizardPageInformix("ifx", props, newDBInfo);
		pageifx.createControl(shell);
		
		final  CreateDatabaseWizardPageJDBC pagejdbc = new CreateDatabaseWizardPageJDBC("jdbc", props, newDBInfo);
		pagejdbc.createControl(shell);
		
		final  CreateDatabaseWizardPageOCI pageoci = new CreateDatabaseWizardPageOCI("oci", props, newDBInfo);
		pageoci.createControl(shell);
		
		final CreateDatabaseWizardPageODBC pageodbc = new CreateDatabaseWizardPageODBC("odbc", props, newDBInfo);
		pageodbc.createControl(shell);
		
		final CreateDatabaseWizardPageOracle pageoracle = new CreateDatabaseWizardPageOracle("oracle", props, newDBInfo);

		final CreateDatabaseWizardPage2 page2 = new CreateDatabaseWizardPage2("2", props, newDBInfo);
		page2.createControl(shell);

		Wizard wizard = new Wizard() 
		{
			public boolean performFinish() 
			{
				transMeta.addDatabase(newDBInfo);
				refreshTree(true);
				refreshGraph();
				return true;
			}
			
			/**
			 * @see org.eclipse.jface.wizard.Wizard#canFinish()
			 */
			public boolean canFinish()
			{
				return page2.canFinish();
			}
		};
				
		wizard.addPage(page1);
		wizard.addPage(pageoci);
		wizard.addPage(pageodbc);
		wizard.addPage(pagejdbc);
		wizard.addPage(pageoracle);
		wizard.addPage(pageifx);
		wizard.addPage(page2);
				
		WizardDialog wd = new WizardDialog(shell, wizard);
		wd.setMinimumPageSize(700,400);
		wd.open();
	}

	
	/**
	 * Create a transformation that extracts tables & data from a database.<p><p>
	 * 
	 * 0) Select the database to rip<p>
	 * 1) Select the table in the database to copy<p>
	 * 2) Select the database to dump to<p>
	 * 3) Select the repository directory in which it will end up<p>
	 * 4) Select a name for the new transformation<p>
	 * 6) Create 1 transformation for the selected table<p> 
	 */
	private void copyTableWizard()
	{
		if (showChangedWarning())
		{
			final CopyTableWizardPage1 page1 = new CopyTableWizardPage1("1", transMeta.getDatabases());
			page1.createControl(shell);
			final CopyTableWizardPage2 page2 = new CopyTableWizardPage2("2");
			page2.createControl(shell);
			final CopyTableWizardPage3 page3 = new CopyTableWizardPage3 ("3", rep);
			page3.createControl(shell);
	
			Wizard wizard = new Wizard() 
			{
				public boolean performFinish() 
				{
					return copyTable(page3.getTransformationName(), page3.getDirectory(),
						  page1.getSourceDatabase(), page1.getTargetDatabase(),
						  page2.getSelection()
						  );
				}
				
				/**
				 * @see org.eclipse.jface.wizard.Wizard#canFinish()
				 */
				public boolean canFinish()
				{
					return page3.canFinish();
				}
			};
					
			wizard.addPage(page1);
			wizard.addPage(page2);
			wizard.addPage(page3);
					
			WizardDialog wd = new WizardDialog(shell, wizard);
			wd.setMinimumPageSize(700,400);
			wd.open();
		}
	}

	public boolean copyTable( String transname, RepositoryDirectory repdir, 
						      DatabaseMeta sourceDBInfo, DatabaseMeta targetDBInfo,
						      String tablename
						    )
	{
		try
		{
			//
			// Create a new transformation...
			//
			TransMeta ti = new TransMeta();
			ti.setName(transname);
			ti.setDirectory(repdir);
			ti.setDatabases(transMeta.getDatabases());
			
			//
			// Add a note
			//
			String note = "Reads information from table ["+tablename+"] on database ["+sourceDBInfo+"]"+Const.CR;
			note+="After that, it writes the information to table ["+tablename+"] on database ["+targetDBInfo+"]";
			NotePadMeta ni = new NotePadMeta(note, 150, 10, -1, -1);
			ti.addNote(ni);
	
			// 
			// create the source step...
			//
			String fromstepname = "read from ["+tablename+"]";
			TableInputMeta tii = new TableInputMeta();
			tii.setDatabaseMeta(sourceDBInfo);
			tii.setSQL("SELECT * FROM "+tablename);
			
			StepLoader steploader = StepLoader.getInstance();
			
			String fromstepid = steploader.getStepPluginID(tii);
			StepMeta fromstep = new StepMeta(log, fromstepid, fromstepname, (StepMetaInterface)tii );
			fromstep.setLocation(150,100);
			fromstep.setDraw(true);
			fromstep.setDescription("Reads information from table ["+tablename+"] on database ["+sourceDBInfo+"]");
			ti.addStep(fromstep);
			
			//
			// add logic to rename fields in case any of the field names contain reserved words...
			// Use metadata logic in SelectValues, use SelectValueInfo...
			//
			Database sourceDB = new Database(sourceDBInfo);
			sourceDB.connect();
			
			// Get the fields for the input table...
			Row fields = sourceDB.getTableFields(tablename);
			
			// See if we need to deal with reserved words...
			int nrReserved = targetDBInfo.getNrReservedWords(fields); 
			if (nrReserved>0)
			{
				SelectValuesMeta svi = new SelectValuesMeta();
				svi.allocate(0,0,nrReserved);
				int nr = 0;
				for (int i=0;i<fields.size();i++)
				{
					Value v = fields.getValue(i);
					if (targetDBInfo.isReservedWord( v.getName() ) )
					{
						svi.getMetaName()[nr] = v.getName();
						svi.getMetaRename()[nr] = targetDBInfo.quoteField( v.getName() );
						nr++;
					}
				}
				
				String selstepname = "Handle reserved words";
				String selstepid = steploader.getStepPluginID(svi);
				StepMeta selstep = new StepMeta(log, selstepid, selstepname, (StepMetaInterface)svi );
				selstep.setLocation(350,100);
				selstep.setDraw(true);
				selstep.setDescription("Renames reserved words for "+targetDBInfo.getDatabaseTypeDesc());
				ti.addStep(selstep);
				
				TransHopMeta shi = new TransHopMeta(fromstep, selstep);
				ti.addTransHop(shi);
				fromstep = selstep;
			}
			
			// 
			// Create the target step...
			//
			//
			// Add the TableOutputMeta step...
			//
			String tostepname = "write to ["+tablename+"]";
			TableOutputMeta toi = new TableOutputMeta();
			toi.setDatabase( targetDBInfo );
			toi.setTablename( tablename );
			toi.setCommitSize( 200 );
			toi.setTruncateTable( true );
			
			String tostepid = steploader.getStepPluginID(toi);
			StepMeta tostep = new StepMeta(log, tostepid, tostepname, (StepMetaInterface)toi );
			tostep.setLocation(550,100);
			tostep.setDraw(true);
			tostep.setDescription("Write information to table ["+tablename+"] on database ["+targetDBInfo+"]");
			ti.addStep(tostep);
			
			//
			// Add a hop between the two steps...
			//
			TransHopMeta hi = new TransHopMeta(fromstep, tostep);
			ti.addTransHop(hi);
			
			// OK, if we're still here: overwrite the current transformation...
			transMeta = ti;
			refreshGraph();
			refreshTree(true);
		}
		catch(Exception e)
		{
			new ErrorDialog(shell, props, "Unexpected error", "An unexpected error occurred creating the new transformation", new KettleException(e.getMessage(), e)); 
			return false;
		}
		return true;
	}
		
	public String toString()
	{
		return APP_NAME;
	}
	
	/**
	 * This is the main procedure for Spoon.
	 * 
	 * @param a Arguments are available in the "Get System Info" step.
	 */
	public static void main (String [] a) 
	{
	    ArrayList args = new ArrayList();
	    for (int i=0;i<a.length;i++) args.add(a[i]);
	    
		Display display = new Display();
		Splash splash = new Splash(display);
		
		// System.out.println("Welcome to Spoon!");
		String repname   = Const.getCommandlineOption(args, "rep");
		String username  = Const.getCommandlineOption(args, "user");
		String password  = Const.getCommandlineOption(args, "pass");
		String transname = Const.getCommandlineOption(args, "trans");
		String filename  = Const.getCommandlineOption(args, "file");
		String dirname   = Const.getCommandlineOption(args, "dir");
		String logfile   = Const.getCommandlineOption(args, "log");
		String loglevel  = Const.getCommandlineOption(args, "level");

        String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);
        
        if (kettleRepname !=null && kettleRepname .length()>0) repname  = kettleRepname;
        if (kettleUsername!=null && kettleUsername.length()>0) username = kettleUsername;
        if (kettlePassword!=null && kettlePassword.length()>0) password = kettlePassword;
        
		// Before anything else, check the runtime version!!!
		String version = Const.JAVA_VERSION;
		if ("1.4".compareToIgnoreCase(version)>0)
		{
			System.out.println("Spoon is running on Java version "+version);
			System.out.println("Unfortunately, it needs version 1.4 or higher to run.");
			return;
		}

		// Set default Locale:
		Locale.setDefault(Const.DEFAULT_LOCALE);
		
		LogWriter log;
		if (logfile==null)
		{
			log=LogWriter.getInstance(Const.SPOON_LOG_FILE, false, LogWriter.LOG_LEVEL_BASIC);
		}
		else
		{
            log=LogWriter.getInstance( logfile, true, LogWriter.LOG_LEVEL_BASIC );
		}

		if (log.getRealFilename()!=null) log.logBasic(APP_NAME, "Logging goes to "+log.getRealFilename());
		
		if (loglevel!=null) 
		{
			log.setLogLevel(loglevel);
			log.logBasic(APP_NAME, "Logging is at level : "+log.getLogLevelDesc());
		}
		
		/* Load the plugins etc.*/
		StepLoader stloader = StepLoader.getInstance();
		if (!stloader.read())
		{
            log.logError(APP_NAME, "Error loading steps & plugins... halting Spoon!");
			return;
		}

		final Spoon win = new Spoon(log, display, null);
		win.setDestroy(true);
		win.setArguments((String[])args.toArray(new String[args.size()]));
		
        log.logBasic(APP_NAME, "Main window is created.");
        
		RepositoryMeta repinfo = null;
		UserInfo userinfo = null;
		
		if (repname==null && filename==null && win.props.showRepositoriesDialogAtStartup())
		{		
            log.logBasic(APP_NAME, "Asking for repository");

			int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
			splash.hide();
			RepositoriesDialog rd = new RepositoriesDialog(win.disp, SWT.NONE, perms, "Spoon");
			if (rd.open())
			{
				repinfo = rd.getRepository();
				userinfo = rd.getUser();
				if (!userinfo.useTransformations())
				{
					MessageBox mb = new MessageBox(win.shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage("Sorry, this repository user can't work with transformations from the repository.");
					mb.setText("Error!");
					mb.open();
					
					userinfo = null;
					repinfo  = null;
				}
			}
			else
			{
				// Exit point: user pressed CANCEL!
				if (rd.isCancelled()) 
				{
					splash.dispose();
					win.quitFile();
					return;
				}
			}
		}
		
		try
		{
			// Read kettle transformation specified on command-line?
			if (repname!=null || filename!=null)
			{			
			    if (repname!=null)
				{
					RepositoriesMeta repsinfo = new RepositoriesMeta(log);
					if (repsinfo.readData())
					{
						repinfo = repsinfo.findRepository(repname);
						if (repinfo!=null)
						{
							// Define and connect to the repository...
							win.rep = new Repository(log, repinfo, userinfo);
							if (win.rep.connect("Spoon"))
							{
								if (dirname==null) dirname=RepositoryDirectory.DIRECTORY_SEPARATOR;
								
								// Check username, password
                                try
                                {
    								win.rep.userinfo = new UserInfo(win.rep, username, Const.NVL(password, ""));
    								
                                    if (transname!=null && dirname!=null)
                                    {
    									RepositoryDirectory repdir = win.rep.getDirectoryTree().findDirectory(dirname);
    									if (repdir!=null)
    									{
    										win.transMeta = new TransMeta(win.rep, transname, repdir);
    										win.setFilename(repname);
    										win.transMeta.clearChanged();
    									}
    									else
    									{
                                            log.logError(APP_NAME, "Can't find directory ["+dirname+"] in the repository.");
    									}
                                    }
                                }
                                catch(KettleException e)
								{
                                    log.logError(APP_NAME, "Can't verify username and password.");
                                    win.rep.disconnect();
                                    win.rep=null;
                                    MessageBox mb = new MessageBox(win.shell, SWT.OK | SWT.ICON_ERROR);
                                    mb.setMessage("The supplied username or password is incorrect.");
                                    mb.setText("Sorry...");
                                    mb.open();
								}
							}
							else
							{
                                log.logError(APP_NAME, "Can't connect to the repository.");
							}
						}
						else
						{
                            log.logError(APP_NAME, "No repository provided, can't load transformation.");
						}
					}
					else
					{
                        log.logError(APP_NAME, "No repositories defined on this system.");
					}
				}
				else
				if (filename!=null)
				{
					win.transMeta = new TransMeta(filename);
					win.setFilename(filename);
					win.transMeta.clearChanged();
				}
			}
			else // Normal operations, nothing on the commandline...
			{
				// Can we connect to the repository?
				if (repinfo!=null && userinfo!=null)
				{
					win.rep = new Repository(log, repinfo, userinfo);
					if (!win.rep.connect("Spoon"))
					{
						win.rep = null;
					}
				}
	
				if (win.props.openLastFile())
				{
                    log.logDetailed(APP_NAME, "Trying to open the last file used.");
					
					String  lastfiles[] = win.props.getLastFiles();
					String  lastdirs[]  = win.props.getLastDirs();
					boolean lasttypes[] = win.props.getLastTypes();
					String  lastrepos[] = win.props.getLastRepositories();
			
					if (lastfiles.length>0)
					{
						boolean use_repository = repinfo!=null;
						
						// Perhaps we need to connect to the repository?
						if (lasttypes[0])
						{
							if (lastrepos[0]!=null && lastrepos[0].length()>0)
							{
								if (use_repository && !lastrepos[0].equalsIgnoreCase(repinfo.getName()))
								{
									// We just asked...
									use_repository = false;
								}
							}
						}
						
						if (use_repository || !lasttypes[0])
						{
							if (win.rep!=null) // load from repository...
							{
								if (win.rep.getName().equalsIgnoreCase(lastrepos[0]))
								{
									RepositoryDirectory repdir = win.rep.getDirectoryTree().findDirectory(lastdirs[0]);
									if (repdir!=null)
									{
                                        log.logDetailed(APP_NAME, "Auto loading transformation ["+lastfiles[0]+"] from repository directory ["+lastdirs[0]+"]");
										TransLoadProgressDialog tlpd = new TransLoadProgressDialog(win.shell, win.rep, lastfiles[0], repdir);
										TransMeta transInfo = tlpd.open(); // = new TransInfo(log, win.rep, lastfiles[0], repdir);
										if (transInfo != null) 
										{
											win.transMeta = transInfo;
											win.setFilename(lastfiles[0]);
										}
									}
								}
							}
							else // Load from XML?
							{
								win.transMeta = new TransMeta(lastfiles[0]);
								win.setFilename(lastfiles[0]);
							}
						}
						win.transMeta.clearChanged();
					}
				}
			}
		}
		catch(KettleException ke)
		{
            log.logError(APP_NAME, "An error occurred: "+Const.CR+ke.getMessage());
			// ke.printStackTrace();
		}
				
		win.open ();

		splash.dispose();
		
		try
		{
			while (!win.isDisposed ()) 
			{
				if (!win.readAndDispatch ()) win.sleep ();
			}
		}
		catch(Throwable e)
		{
            log.logError(APP_NAME, "An unexpected error occurred in Spoon: probable cause: please close all windows before stopping Spoon! "+Const.CR+e.getMessage());
			e.printStackTrace();
		}
		win.dispose();

        log.logBasic(APP_NAME, APP_NAME+" has ended.");

		// Close the logfile
		log.close();
		
        // Kill all remaining things in this VM!
        System.exit(0);
    }
    
}
