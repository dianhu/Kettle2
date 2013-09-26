package be.ibridge.kettle.spoon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.MyGUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.trans.MyStepLoader;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;

public class MySpoon {
	public static final String APP_NAME = "Hcy's ETL Tools";

	private Display disp;
	private Shell shell;

	private LogWriter log;
	private Props props;

	private ToolBar tBar;
	private SashForm sashform;
	private Tree selectionTree;
	private TreeItem tiConn, tiHops, tiStep, tiBase, tiPlug;
	private Tree pluginHistoryTree;
	private CTabFolder tabfolder;

	private static final String STRING_CONNECTIONS = "Connections";
	private static final String STRING_STEPS = "Steps";
	private static final String STRING_HOPS = "Hops";
	private static final String STRING_BASE = "Base step types";
	private static final String STRING_PLUGIN = "Plugin step types";
	private static final String STRING_HISTORY = "Step creation history";

	public MySpoon(Display display,LogWriter log) {
		this.log = log;
		this.disp = display;
		shell = new Shell(disp);
		shell.setText(APP_NAME);
		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = 0;
		formLayout.marginWidth = 0;

		shell.setLayout(formLayout);

		//初始化属性
		if (!Props.isInitialized()) {
			Props.init(disp, Props.TYPE_PROPERTIES_SPOON); // things to
															// remember...
		}
		props = Props.getInstance();

		addMenu();

		addBar();

		// add 一个水平平分的视窗into shell
		sashform = new SashForm(shell, SWT.HORIZONTAL);
		// for sashform attach position
		FormData fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0);
		fdSash.top = new FormAttachment(tBar, 0);
		fdSash.bottom = new FormAttachment(100, 0);
		fdSash.right = new FormAttachment(100, 0);
		sashform.setLayoutData(fdSash);

		addTree();

		setTreeImages();

		addTabs();
		// sashfrom分成左右两边 tree占据了左边，tabs占据了右边，setWeight对左右两边设置相对宽度
		sashform.setWeights(new int[] { 25, 75 });
	}

	private void addMenu() {

		Menu mBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mBar);

		MenuItem mFile = new MenuItem(mBar, SWT.CASCADE);
		mFile.setText("&File");

		Menu msFile = new Menu(shell, SWT.DROP_DOWN);
		mFile.setMenu(msFile);

		MenuItem miFileNew = new MenuItem(msFile, SWT.CASCADE);
		miFileNew.setText("&New \tCTRL-N");
		MenuItem miFileOpen = new MenuItem(msFile, SWT.CASCADE);
		miFileOpen.setText("&Open \tCTRL-O");
		MenuItem miFileImport = new MenuItem(msFile, SWT.CASCADE);
		miFileImport.setText("&Import from an XML file\tCTRL-I");
		MenuItem miFileExport = new MenuItem(msFile, SWT.CASCADE);
		miFileExport.setText("&Export to an XML file");
		MenuItem miFileSave = new MenuItem(msFile, SWT.CASCADE);
		miFileSave.setText("&Save \tCTRL-S");
		MenuItem miFileSaveAs = new MenuItem(msFile, SWT.CASCADE);
		miFileSaveAs.setText("&SaveAs");
		new MenuItem(msFile, SWT.SEPARATOR);
		MenuItem miFilePrint = new MenuItem(msFile, SWT.CASCADE);
		miFilePrint.setText("&Print");
		new MenuItem(msFile, SWT.SEPARATOR);
		MenuItem miFileQuit = new MenuItem(msFile, SWT.CASCADE);
		miFileQuit.setText("&Quit");

		/**
		 * Calss ActionListener implement Listener{public void handelEvent(Event
		 * e){...}}; ActionListener aListener = new ActionListener();
		 * miFileOpen.addListener(SWT.Selection, aListener);
		 */
		Listener lsFileOpen = new Listener() {
			public void handleEvent(Event e) {
				openFile();
			}
		};
		miFileOpen.addListener(SWT.Selection, lsFileOpen);
	}

	private void openFile() {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterExtensions(Const.STRING_TRANS_FILTER_EXT);
		dialog.setFilterNames(Const.STRING_TRANS_FILTER_NAMES);
		String fname = dialog.open();

	}

	private void addBar() {
		tBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT);
		// props.setLook(tBar);

		final ToolItem tiFileNew = new ToolItem(tBar, SWT.PUSH);
		final Image imFileNew = new Image(disp, getClass().getResourceAsStream(
				Const.IMAGE_DIRECTORY + "new.png"));
		tiFileNew.setImage(imFileNew);
		// tiFileNew.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent e) { newFile(); }});
		tiFileNew.setToolTipText("New transformation, clear all settings");

		final ToolItem tiFileOpen = new ToolItem(tBar, SWT.PUSH);
		final Image imFileOpen = new Image(disp, getClass()
				.getResourceAsStream(Const.IMAGE_DIRECTORY + "open.png"));
		tiFileOpen.setImage(imFileOpen);
		// tiFileOpen.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent e) { openFile(false); }});
		tiFileOpen.setToolTipText("Open tranformation");

		final ToolItem tiFileSave = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSave = new Image(disp, getClass()
				.getResourceAsStream(Const.IMAGE_DIRECTORY + "save.png"));
		tiFileSave.setImage(imFileSave);
		// tiFileSave.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent e) { saveFile(); }});
		tiFileSave.setToolTipText("Save current transformation");

		final ToolItem tiFileSaveAs = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSaveAs = new Image(disp, getClass()
				.getResourceAsStream(Const.IMAGE_DIRECTORY + "saveas.png"));
		tiFileSaveAs.setImage(imFileSaveAs);
		// tiFileSaveAs.addSelectionListener(new SelectionAdapter() { public
		// void widgetSelected(SelectionEvent e) { saveFileAs(); }});
		tiFileSaveAs.setToolTipText("Save transformation with different name");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFilePrint = new ToolItem(tBar, SWT.PUSH);
		final Image imFilePrint = new Image(disp, getClass()
				.getResourceAsStream(Const.IMAGE_DIRECTORY + "print.png"));
		tiFilePrint.setImage(imFilePrint);
		// tiFilePrint.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent e) { printFile(); }});
		tiFilePrint.setToolTipText("Print");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFileRun = new ToolItem(tBar, SWT.PUSH);
		final Image imFileRun = new Image(disp, getClass().getResourceAsStream(
				Const.IMAGE_DIRECTORY + "run.png"));
		tiFileRun.setImage(imFileRun);
		// tiFileRun.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent e) { tabfolder.setSelection(1);
		// spoonlog.startstop(); }});
		tiFileRun.setToolTipText("Run this transformation");

		final ToolItem tiFilePreview = new ToolItem(tBar, SWT.PUSH);
		final Image imFilePreview = new Image(disp, getClass()
				.getResourceAsStream(Const.IMAGE_DIRECTORY + "preview.png"));
		tiFilePreview.setImage(imFilePreview);
		// tiFilePreview.addSelectionListener(new SelectionAdapter() { public
		// void widgetSelected(SelectionEvent e) { spoonlog.preview(); }});
		tiFilePreview.setToolTipText("Preview this transformation");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFileCheck = new ToolItem(tBar, SWT.PUSH);
		final Image imFileCheck = new Image(disp, getClass()
				.getResourceAsStream(Const.IMAGE_DIRECTORY + "check.png"));
		tiFileCheck.setImage(imFileCheck);
		// tiFileCheck.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent e) { checkTrans(); }});
		tiFileCheck.setToolTipText("Verify this transformation");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiImpact = new ToolItem(tBar, SWT.PUSH);
		final Image imImpact = new Image(disp, getClass().getResourceAsStream(
				Const.IMAGE_DIRECTORY + "impact.png"));
		// Can't seem to get the transparency correct for this image!
		ImageData idImpact = imImpact.getImageData();
		int impactPixel = idImpact.palette.getPixel(new RGB(255, 255, 255));
		idImpact.transparentPixel = impactPixel;
		Image imImpact2 = new Image(disp, idImpact);
		tiImpact.setImage(imImpact2);
		// tiImpact.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent e) { analyseImpact(); }});
		tiImpact.setToolTipText("Analyze the impact of this transformation on the database(s)");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiSQL = new ToolItem(tBar, SWT.PUSH);
		final Image imSQL = new Image(disp, getClass().getResourceAsStream(
				Const.IMAGE_DIRECTORY + "SQLbutton.png"));
		// Can't seem to get the transparency correct for this image!
		ImageData idSQL = imSQL.getImageData();
		int sqlPixel = idSQL.palette.getPixel(new RGB(255, 255, 255));
		idSQL.transparentPixel = sqlPixel;
		Image imSQL2 = new Image(disp, idSQL);
		tiSQL.setImage(imSQL2);
		// tiSQL.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent e) { getSQL(); }});
		tiSQL.setToolTipText("Generate the SQL needed to run this transformation");

		tBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imFileNew.dispose();
				imFileOpen.dispose();
				imFileSave.dispose();
				imFileSaveAs.dispose();
			}
		});
		// tBar.addKeyListener(defKeys);
		// tBar.addKeyListener(modKeys);
		tBar.pack();
	}

	private void addTree() {
		// Split the left side of the screen in half
		SashForm leftSash = new SashForm(sashform, SWT.VERTICAL);

		// Now set up the main CSH tree
		selectionTree = new Tree(leftSash, SWT.MULTI | SWT.BORDER);
		// props.setLook(selectionTree);
		selectionTree.setLayout(new FillLayout());

		tiConn = new TreeItem(selectionTree, SWT.NONE);
		tiConn.setText(STRING_CONNECTIONS);
		tiStep = new TreeItem(selectionTree, SWT.NONE);
		tiStep.setText(STRING_STEPS);
		tiHops = new TreeItem(selectionTree, SWT.NONE);
		tiHops.setText(STRING_HOPS);
		tiBase = new TreeItem(selectionTree, SWT.NONE);
		tiBase.setText(STRING_BASE);
		tiPlug = new TreeItem(selectionTree, SWT.NONE);
		tiPlug.setText(STRING_PLUGIN);

		// Fill the base components...
		MyStepLoader steploader = MyStepLoader.getInstance();
		StepPlugin basesteps[] = steploader
				.getStepsWithType(StepPlugin.TYPE_NATIVE);
		// 得到本地Step所属的所有类型，如input,output...
		String basecat[] = steploader.getCategories(StepPlugin.TYPE_NATIVE);
		TreeItem tiBaseCat[] = new TreeItem[basecat.length];
		for (int i = 0; i < basecat.length; i++) {
			tiBaseCat[i] = new TreeItem(tiBase, SWT.NONE);
			tiBaseCat[i].setText(basecat[i]);

			for (int j = 0; j < basesteps.length; j++) {
				if (basesteps[j].getCategory().equalsIgnoreCase(basecat[i])) {
					// 在category下创建step节点
					TreeItem ti = new TreeItem(tiBaseCat[i], 0);
					ti.setText(basesteps[j].getDescription());
				}
			}
		}

		// // Show the plugins...
		// StepPlugin plugins[] =
		// steploader.getStepsWithType(StepPlugin.TYPE_PLUGIN);
		// String plugcat[] = steploader.getCategories(StepPlugin.TYPE_PLUGIN);
		// TreeItem tiPlugCat[] = new TreeItem[plugcat.length];
		// for (int i=0;i<plugcat.length;i++)
		// {
		// tiPlugCat[i] = new TreeItem(tiPlug, SWT.NONE);
		// tiPlugCat[i].setText(plugcat[i]);
		//
		// for (int j=0;j<plugins.length;j++)
		// {
		// if (plugins[j].getCategory().equalsIgnoreCase(plugcat[i]))
		// {
		// TreeItem ti = new TreeItem(tiPlugCat[i], 0);
		// ti.setText(plugins[j].getDescription());
		// }
		// }
		// }

		tiConn.setExpanded(true);
		tiStep.setExpanded(false);
		tiBase.setExpanded(true);
		tiPlug.setExpanded(true);

		// OK, now add a list of often-used icons to the bottom of the tree...
		//pluginHistoryTree = new Tree(leftSash, SWT.SINGLE);

		// Add tooltips for history tree too
		// addToolTipsToTree(pluginHistoryTree);

		// Set the same listener on this tree
		// addDragSourceToTree(pluginHistoryTree);

		//leftSash.setWeights(new int[] { 70, 30 });
	}

	private void setTreeImages() {
		tiConn.setImage(MyGUIResource.getInstance().getImageConnection());
		tiHops.setImage(MyGUIResource.getInstance().getImageHop());
		tiStep.setImage(MyGUIResource.getInstance().getImageBol());
		tiBase.setImage(MyGUIResource.getInstance().getImageBol());
		tiPlug.setImage(MyGUIResource.getInstance().getImageBol());

		TreeItem tiBaseCat[] = tiBase.getItems();
		for (int x = 0; x < tiBaseCat.length; x++) {
			tiBaseCat[x].setImage(MyGUIResource.getInstance().getImageBol());

			TreeItem ti[] = tiBaseCat[x].getItems();
			for (int i = 0; i < ti.length; i++) {
				TreeItem stepitem = ti[i];
				String description = stepitem.getText();

				MyStepLoader steploader = MyStepLoader.getInstance();
				StepPlugin sp = steploader
						.findStepPluginWithDescription(description);
				if (sp != null) {
					Image stepimg = (Image) MyGUIResource.getInstance()
							.getImagesStepsSmall().get(sp.getID());
					if (stepimg != null) {
						stepitem.setImage(stepimg);
					}
				}
			}
		}
	}

	private void addTabs() {
		// 占据tree的右边一块视窗
		Composite child = new Composite(sashform, SWT.BORDER);
		// props.setLook(child);

		FormLayout childLayout = new FormLayout();
		childLayout.marginWidth = 0;
		childLayout.marginHeight = 0;
		child.setLayout(childLayout);

		tabfolder = new CTabFolder(child, SWT.BORDER);
		// props.setLook(tabfolder, Props.WIDGET_STYLE_TAB);

		FormData fdTabfolder = new FormData();
		fdTabfolder.left = new FormAttachment(0, 0);
		fdTabfolder.right = new FormAttachment(100, 0);
		fdTabfolder.top = new FormAttachment(0, 0);
		fdTabfolder.bottom = new FormAttachment(100, 0);
		tabfolder.setLayoutData(fdTabfolder);

		CTabItem tiTabsGraph = new CTabItem(tabfolder, SWT.NONE);
		tiTabsGraph.setText("Graphical view");
		tiTabsGraph.setToolTipText("Displays the transformation graphically.");

		CTabItem tiTabsList = new CTabItem(tabfolder, SWT.NULL);
		tiTabsList.setText("Log view");
		tiTabsList
				.setToolTipText("Displays the log of the running transformation.");

		// spoongraph = new SpoonGraph(tabfolder, SWT.V_SCROLL | SWT.H_SCROLL |
		// SWT.NO_BACKGROUND, log, this);
		// spoonlog = new SpoonLog(tabfolder, SWT.NONE, this, log, null);
		//
		// tabfolder.addKeyListener(defKeys);
		// tabfolder.addKeyListener(modKeys);
		//
		// tiTabsGraph.setControl(spoongraph);
		// tiTabsList.setControl(spoonlog);

		tabfolder.setSelection(0);

		// sashform.addKeyListener(defKeys);
		// sashform.addKeyListener(modKeys);
	}

	public void open() {
		shell.open();
	}

	public boolean isDisposed() {
		return disp.isDisposed();
	}

	public boolean readAndDispatch() {
		return disp.readAndDispatch();
	}

	public void dispose() {
		disp.dispose();
	}

	public void sleep() {
		disp.sleep();
	}

	public static void main(String[] args) {
		Display display = new Display();

		String logfile = null;
		// set log
		LogWriter log=LogWriter.getInstance(Const.SPOON_LOG_FILE, false,
				LogWriter.LOG_LEVEL_BASIC);
		/*
		 * Load the plugins etc.
		 * 
		 * 把所有的Step信息set到StepPlugin对象，然后把StepPlugin对象放入pluginList
		 */
		MyStepLoader stloader = MyStepLoader.getInstance();
		if (!stloader.read()) {
			// log.logError(APP_NAME,
			// "Error loading steps & plugins... halting Spoon!");
			return;
		}

		final MySpoon win = new MySpoon(display,log);
		win.open();

		while (!win.isDisposed()) {
			if (!win.readAndDispatch())
				win.sleep();
		}

		win.dispose();

		System.exit(0);
	}

}
