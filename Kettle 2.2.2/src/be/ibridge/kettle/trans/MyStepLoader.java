package be.ibridge.kettle.trans;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.MyBaseStep;

public class MyStepLoader {
	private static MyStepLoader stepLoader = null;

    private String            pluginDirectory[];

    private ArrayList         pluginList;
    
    private Hashtable         classLoaders;

    private MyStepLoader(String pluginDirectory[])
    {
        this.pluginDirectory = pluginDirectory;
        pluginList   = new ArrayList();
        classLoaders = new Hashtable();
    }

    public static final MyStepLoader getInstance(String pluginDirectory[])
    {
        if (stepLoader != null) return stepLoader;

        stepLoader = new MyStepLoader(pluginDirectory);

        return stepLoader;
    }

    public static final MyStepLoader getInstance()
    {
        if (stepLoader != null) return stepLoader;
        
        stepLoader = new MyStepLoader(new String[] { Const.PLUGIN_STEPS_DIRECTORY_PUBLIC, Const.PLUGIN_STEPS_DIRECTORY_PRIVATE } );

        return stepLoader;
    }

    public boolean read()
    {
        if (readNatives()) { return readPlugins(); }
        return false;
    }

    /*
     * 把本地的Step信息封装到StepPlugin类中，再放入到List容器中
     * */
    public boolean readNatives()
    {
        for (int i = 0; i < MyBaseStep.type_desc.length; i++)
        {
            String id = MyBaseStep.type_desc[i];
            String long_desc = MyBaseStep.type_long_desc[i];
            String tooltip = MyBaseStep.type_tooltip_desc[i];
            String iconfile = Const.IMAGE_DIRECTORY + MyBaseStep.image_filename[i];
            String classname = MyBaseStep.type_classname[i].getName(); // TOEVOEGEN!
            String directory = null; // Not used,for customer plugin
            String jarfiles[] = null; // Not used,for customer plugin
            String category = MyBaseStep.category[i];

            StepPlugin sp = new StepPlugin(StepPlugin.TYPE_NATIVE, id, long_desc, tooltip, directory, jarfiles, iconfile, classname, category, null);
            if (id.equalsIgnoreCase("ScriptValues")) sp.setSeparateClassloaderNeeded(true); 

            pluginList.add(sp);
        }

        return true;
    }

    public boolean readPlugins()
    {

        return true;
    }
    
    /**
     * Count's the number of steps with a certain type.
     * 
     * @param type
     *            One of StepPlugin.TYPE_NATIVE, StepPlugin.TYPE_PLUGIN,
     *            StepPlugin.TYPE_ALL
     * @return The number of steps with a certain type.
     */
    public int nrStepsWithType(int type)
    {
        int nr = 0;
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = (StepPlugin) pluginList.get(i);
            if (sp.getType() == type || type == StepPlugin.TYPE_ALL) nr++;
        }
        return nr;
    }
    
    public StepPlugin getStepWithType(int type, int position)
    {
        int nr = 0;
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = (StepPlugin) pluginList.get(i);
            if (sp.getType() == type || type == StepPlugin.TYPE_ALL)
            {
                if (nr == position) return sp;
                nr++;
            }
        }
        return null;
    }
    
    public StepPlugin[] getStepsWithType(int type)
    {
        int nr = nrStepsWithType(type);
        StepPlugin steps[] = new StepPlugin[nr];
        for (int i = 0; i < steps.length; i++)
        {
            StepPlugin sp = getStepWithType(type, i);
            // System.out.println("sp #"+i+" = "+sp.getID());
            steps[i] = sp;
        }
        return steps;
    }
    
    /**
     * Get a unique list of categories. We can use this to display in trees etc.
     * 
     * @param type
     *            The type of step plugins for which we want to categories...
     * @return a unique list of categories
     */
    public String[] getCategories(int type)
    {
        Hashtable cat = new Hashtable();
        for (int i = 0; i < nrStepsWithType(type); i++)
        {
            StepPlugin sp = getStepWithType(type, i);
            if (sp != null)
            {
                cat.put(sp.getCategory(), sp.getCategory());
            }
        }
        Enumeration keys = cat.keys();
        String retval[] = new String[cat.size()];
        int i = 0;
        while (keys.hasMoreElements())
        {
            retval[i] = (String) keys.nextElement();
            i++;
        }

        // Sort the resulting array...
        // It has to be sorted the same way as the String array BaseStep.category_order
        //
        for (int a = 0; a < retval.length; a++)
        {
            for (int b = 0; b < retval.length - 1; b++)
            {
                // What is the index of retval[b] and retval[b+1]?
                int idx1 = Const.indexOfString(retval[b  ], BaseStep.category_order);
                int idx2 = Const.indexOfString(retval[b+1], BaseStep.category_order);
                
                if (idx1>idx2)
                {
                    String dummy = retval[b];
                    retval[b] = retval[b + 1];
                    retval[b + 1] = dummy;
                }
            }
        }
        return retval;
    }
    
    public StepPlugin findStepPluginWithDescription(String description)
    {
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = (StepPlugin) pluginList.get(i);
            if (sp.getDescription().equalsIgnoreCase(description)) return sp;
        }
        return null;
    }

}
