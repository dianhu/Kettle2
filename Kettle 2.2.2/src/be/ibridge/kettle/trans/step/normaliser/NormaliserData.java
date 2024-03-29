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
 

package be.ibridge.kettle.trans.step.normaliser;

import java.util.ArrayList;

import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class NormaliserData extends BaseStepData implements StepDataInterface
{
	public ArrayList type_occ;
	public int maxlen;
	public ArrayList copy_fieldnrs;
	public int fieldnrs[];
		
	/**
	 * 
	 */
	public NormaliserData()
	{
		super();
		
		type_occ = null;
	}

}
