
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains SAP DB specific information through static final members 
 * 
 * @author Matt
 * @since  30-06-2005
 */
public class SAPDBDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public SAPDBDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public SAPDBDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "SAPDB";
	}

	public String getDatabaseTypeDescLong()
	{
		return "MaxDB (SAP DB)";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_SAPDB;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public int getDefaultDatabasePort()
	{
		// if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 3050;
		return -1;
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
			return "com.sap.dbtech.jdbc.DriverSapDB";
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
		    return "jdbc:sapdb://"+getHostname()+"/"+getDatabaseName();
		}
	}

	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return false;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return false;
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
		return "ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		return "ALTER TABLE "+tablename+" ALTER COLUMN "+v.getName()+" TYPE "+getFieldDefinition(v, tk, pk, use_autoinc, false, false);
	}

	public String getFieldDefinition(Value v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) 
		{
			if (Const.indexOfString(fieldname, getReservedWords())>=0)
			{
				retval+=getStartQuote()+fieldname+getEndQuote();
			}
			else
			{
				retval+=fieldname+" ";
			}
		}
		
		int type         = v.getType();
		switch(type)
		{
		case Value.VALUE_TYPE_DATE   : retval+="TIMESTAMP"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER : 
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk) || // Technical key
			    fieldname.equalsIgnoreCase(pk)    // Primary key
			    ) 
			{
				retval+="BIGINT NOT NULL PRIMARY KEY";
			} 
			else
			{
				if (length>0)
				{
					if (precision>0 || length>18)
					{
						retval+="DECIMAL("+length;
						if (precision>0)
						{
							retval+=", "+precision;
						}
						retval+=")";
					}
					else
					{
						if (length>9)
						{
							retval+="INT64";
						}
						else
						{
							if (length<5)
							{
								retval+="SMALLINT";
							}
							else
							{
								retval+="INTEGER";
							}
						}
					}
					
				}
				else
				{
					retval+="DOUBLE";
				}
			}
			break;
		case Value.VALUE_TYPE_STRING:
			if (length<32720)
			{
				retval+="VARCHAR"; 
				if (length>0)
				{
					retval+="("+length+")";
				}
				else
				{
					retval+="(8000)"; // Maybe use some default DB String length?
				}
			}
			else
			{
				retval+="BLOB SUB_TYPE TEXT";
			}
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
	
	public String [] getReservedWords()
	{
		return new String[] {
		        "ABS", "ABSOLUTE", "ACOS", "ADDDATE", "ADDTIME", "ALL", "ALPHA", "ALTER", "ANY",
		        "ASCII", "ASIN", "ATAN", "ATAN2", "AVG", "BINARY", "BIT", "BOOLEAN", "BYTE",
		        "CASE", "CEIL", "CEILING", "CHAR", "CHARACTER", "CHECK", "CHR", "COLUMN", "CONCAT",
		        "CONSTRAINT", "COS", "COSH", "COT", "COUNT", "CROSS", "CURDATE", "CURRENT", "CURTIME",
		        "DATABASE", "DATE", "DATEDIFF", "DAY", "DAYNAME", "DAYOFMONTH", "DAYOFWEEK", "DAYOFYEAR", "DEC",
		        "DECIMAL", "DECODE", "DEFAULT", "DEGREES", "DELETE", "DIGITS", "DISTINCT", "DOUBLE", "EXCEPT",
		        "EXISTS", "EXP", "EXPAND", "FIRST", "FIXED", "FLOAT", "FLOOR", "FOR", "FROM",
		        "FULL", "GET_OBJECTNAME", "GET_SCHEMA", "GRAPHIC", "GREATEST", "GROUP", "HAVING", "HEX", "HEXTORAW",
		        "HOUR", "IFNULL", "IGNORE", "INDEX", "INITCAP", "INNER", "INSERT", "INT", "INTEGER",
		        "INTERNAL", "INTERSECT", "INTO", "JOIN", "KEY", "LAST", "LCASE", "LEAST", "LEFT",
		        "LENGTH", "LFILL", "LIST", "LN", "LOCATE", "LOG", "LOG10", "LONG", "LONGFILE",
		        "LOWER", "LPAD", "LTRIM", "MAKEDATE", "MAKETIME", "MAPCHAR", "MAX", "MBCS", "MICROSECOND",
		        "MIN", "MINUTE", "MOD", "MONTH", "MONTHNAME", "NATURAL", "NCHAR", "NEXT", "NO",
		        "NOROUND", "NOT", "NOW", "NULL", "NUM", "NUMERIC", "OBJECT", "OF", "ON",
		        "ORDER", "PACKED", "PI", "POWER", "PREV", "PRIMARY", "RADIANS", "REAL", "REJECT",
		        "RELATIVE", "REPLACE", "RFILL", "RIGHT", "ROUND", "ROWID", "ROWNO", "RPAD", "RTRIM",
		        "SECOND", "SELECT", "SELUPD", "SERIAL", "SET", "SHOW", "SIGN", "SIN", "SINH",
		        "SMALLINT", "SOME", "SOUNDEX", "SPACE", "SQRT", "STAMP", "STATISTICS", "STDDEV", "SUBDATE",
		        "SUBSTR", "SUBSTRING", "SUBTIME", "SUM", "SYSDBA", "TABLE", "TAN", "TANH", "TIME",
		        "TIMEDIFF", "TIMESTAMP", "TIMEZONE", "TO", "TOIDENTIFIER", "TRANSACTION", "TRANSLATE", "TRIM", "TRUNC",
		        "TRUNCATE", "UCASE", "UID", "UNICODE", "UNION", "UPDATE", "UPPER", "USER", "USERGROUP",
		        "USING", "UTCDATE", "UTCDIFF", "VALUE", "VALUES", "VARCHAR", "VARGRAPHIC", "VARIANCE", "WEEK",
		        "WEEKOFYEAR", "WHEN", "WHERE", "WITH", "YEAR", "ZONED"		
		 };
	}
}
