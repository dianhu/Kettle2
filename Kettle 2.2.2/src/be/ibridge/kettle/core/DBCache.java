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

 

package be.ibridge.kettle.core;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import be.ibridge.kettle.core.exception.KettleEOFException;
import be.ibridge.kettle.core.exception.KettleFileException;

/**
 * This class caches database queries so that the same query doesn't get called twice.
 * Queries are often launched to the databases to get information on tables etc.
 * 
 * @author Matt
 * @since 15-01-04
 *
 */
public class DBCache
{
	private static DBCache dbCache;
	
	private Hashtable cache;
	private boolean   usecache;
		
	public void setActive()
	{
		setActive(true);
	}
	
	public void setInactive()
	{
		setActive(false);
	}
	
	public void setActive(boolean act)
	{
		usecache=act;
	}
	
	public boolean isActive()
	{
		return usecache;
	}
	
	public void put(DBCacheEntry entry, Row fields)
	{
		if (!usecache) return;
		
		Row copy = new Row(fields);
		cache.put(entry, copy);
		
		//System.out.println("Cache store: "+copy.toStringMeta());
		//System.out.println("Cache store entry="+entry.sql );
	}
	
	/**
	 * Get the fields as a row generated by a database cache entry 
	 * @param entry the entry to look for
	 * @return the fields as a row generated by a database cache entry 
	 */
	public Row get(DBCacheEntry entry)
	{
		if (!usecache) return null;
		
		Row fields = (Row)cache.get(entry);
		if (fields!=null)
		{
			fields = new Row(fields); // Copy it again!
					
			//System.out.println("Cache hit!!, fields="+fields.toStringMeta() );
			//System.out.println("Cache hit entry="+entry.sql );
		}

		return fields;
	}
	
	public int size()
	{
		return cache.size();
	}
	
	/**
	 * Clear out all entries of database with a certain name
	 * @param dbname The name of the database for which we want to clear the cache.
	 */
	public void clear(String dbname)
	{
		if (dbname==null)
		{
			cache = new Hashtable();
			setActive();
		}
		else
		{
			Enumeration keys = cache.keys();
			while (keys.hasMoreElements())
			{
				DBCacheEntry entry = (DBCacheEntry)keys.nextElement();
				if (entry.dbname.equalsIgnoreCase(dbname))
				{
					// Same name: remove it!
					cache.remove(entry);
				}
			}
		}
	}
	
	public String getFilename()
	{
		return Const.getKettleDirectory()+Const.FILE_SEPARATOR+"db.cache";
	}
	
	private DBCache() throws KettleFileException
	{
		try
		{
			LogWriter log = LogWriter.getInstance();
			
			clear(null);
			
			String filename = getFilename();
			File file = new File(filename);
			if (file.canRead())
			{
				log.logBasic("DBCache", "Loading database cache from file: ["+filename+"]");
				
				FileInputStream fis = new FileInputStream(file);
				DataInputStream dis = new DataInputStream(fis);
				
				int counter = 0;
                try
				{
					while (true)
					{
						DBCacheEntry entry = new DBCacheEntry(dis);
						Row row = new Row(dis);
						cache.put(entry, row);
						counter++;
					}
				}
				catch(KettleEOFException eof)
				{
					// System.out.println("We read "+counter+" cached rows from the database cache!");
					log.logDetailed("DBCache", "We read "+counter+" cached rows from the database cache!");
				}
			}
			else
			{
				log.logDetailed("DBCache", "The database cache doesn't exist yet.");
			}
		}
		catch(Exception e)
		{
			throw new KettleFileException("Couldn't read the database cache",e);
		}
	}
	
	public void saveCache(LogWriter log) throws KettleFileException
	{
		try
		{
			String filename = getFilename();
			File file = new File(filename);
			if (!file.exists() || file.canWrite())
			{
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos = new DataOutputStream(fos);
				
				int counter = 0;
				boolean ok = true;

				Enumeration keys = cache.keys();
				while (ok && keys.hasMoreElements())
				{
					// Save the database cache entry
					DBCacheEntry entry = (DBCacheEntry)keys.nextElement();
					entry.write(dos);

					// Save the corresponding row as well.
					Row row = get(entry);
					if (row!=null)
					{
						row.write(dos);
						counter++;
					}
					else
					{
						throw new KettleFileException("The database cache contains an empty row. We can't save this!");
					}
				}

				// System.out.println("We read "+counter+" cached rows from the database cache!");
				log.logDetailed("DBCache", "We wrote "+counter+" cached rows to the database cache!");
			}
			else
			{
				throw new KettleFileException("We can't write to the cache file: "+filename);
			}
		}
		catch(Exception e)
		{
			throw new KettleFileException("Couldn't write to the database cache", e);
		}
	}
	
	/**
	 * Create the database cache instance by loading it from disk
	 * @return the database cache instance.
	 * @throws KettleFileException
	 */
	public static final DBCache getInstance()
	{
		if (dbCache!=null) return dbCache;
		try
		{
			dbCache = new DBCache();
		}
		catch(KettleFileException kfe)
		{
			throw new RuntimeException("Unable to create the database cache: "+kfe.getMessage());
		}
		return dbCache;
	}

}
