
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains DB2 specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class CacheDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connections.  Note that not all these parameters are not allways mandatory.
	 * 
	 * @param name The database name
	 * @param access The type of database access
	 * @param host The hostname or IP address
	 * @param db The database name
	 * @param port The port on which the database listens.
	 * @param user The username
	 * @param pass The password
	 */
	public CacheDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public CacheDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "CACHE";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Intersystems Cache";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_CACHE;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 1972;
		return -1;
	}
	
	public boolean supportsSetCharacterStream()
	{
		return false;
	}
	
	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}
	
	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "com.intersys.jdbc.CacheDriver";
		}
	}

	public String getURL()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+getDatabaseName();
		}
		else
		{
			return "jdbc:Cache://"+getHostname()+":"+getDatabasePortNumber()+"/"+getDatabaseName();
		}
	}

	/**
	 * Generates the SQL statement to add a column to the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" ADD COLUMN ( "+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" ) ";
	}

	/**
	 * Generates the SQL statement to drop a column from the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to drop a column from the specified table
	 */
	public String getDropColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR;
	}
	
	/**
	 * Generates the SQL statement to modify a column in the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to modify a column in the specified table
	 */
	public String getModifyColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" ALTER COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	public String getFieldDefinition(Value v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval+=fieldname+" ";
		
		int type         = v.getType();
		switch(type)
		{
		case Value.VALUE_TYPE_DATE   : retval+="TIMESTAMP"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER :
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk)) // Technical & primary key : see at bottom
			{
				retval+="DECIMAL";
			} 
			else
			{
				if ( length<0 || precision<0 )
				{
					retval+="DOUBLE";
				}
				else if ( precision>0 || length>9)
				{
					retval+="DECIMAL("+length;
					if (precision>0)
					{
						retval+=", "+precision;
					}
					retval+=")";
				}
				else // Precision == 0 && length<=9
				{
					retval+="INT"; 
				}
			}
			break;
		case Value.VALUE_TYPE_STRING: // CLOBs are just VARCHAR in the Cach� database: can be very large!
			retval+="VARCHAR";				
			if (length>0)
			{
				retval+="("+length+")";
			}
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
}
