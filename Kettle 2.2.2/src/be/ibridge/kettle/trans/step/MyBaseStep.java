package be.ibridge.kettle.trans.step;

import be.ibridge.kettle.trans.step.constant.ConstantMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;
import be.ibridge.kettle.trans.step.tableoutput.TableOutputMeta;

public class MyBaseStep {
	public static final Class type_classname[] = 
	{
		TableInputMeta.class,
		TableOutputMeta.class,
        ConstantMeta.class
	};
	
	public static final String type_desc[]={
		"TableInput",
		"TableOutput",
		"Constant"
	};
	public static final String type_long_desc[] = 
	{
		"Table input",
		"Table output",		
        "Add constants"
	};
	public static final String type_tooltip_desc[] ={
		"Read information from a database table.",
		"Write information to a database table",
		"Add one or more constants to the input rows"
	};
	public static final String image_filename[] =
	{
		"TIP.png",
		"TOP.png",
        "CST.png"
	};
	public static final String category[] = 
	{
		"Input", 		    // "TableInput"
		"Output", 		    // "TableOutput"
        "Transform"        // "Constant"
	};
	public static final String category_order[] = { "Input", "Output",  "Transform" };


}
