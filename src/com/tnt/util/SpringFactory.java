package com.tnt.util;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tnt.db.MapFromToDAO;
import com.tnt.db.SyncDetailDAO;
import com.tnt.db.SyncparaDAO;

public class SpringFactory {


	private static ClassPathXmlApplicationContext appContext = null;
	private static final MapFromToDAO mapFromToDAO = (MapFromToDAO)getBean("MapFromToDAO");
	private static final SyncDetailDAO syncDetailDAO = (SyncDetailDAO)getBean("SyncDetailDAO");
	private static final SyncparaDAO syncparaDAO = (SyncparaDAO)getBean("SyncparaDAO");
	
	public static Object getBean(String beanName) {
		if(appContext == null) {
			appContext = new ClassPathXmlApplicationContext(new String[] {
			        "applicationContext.xml"
			    });
		} 
		
		return appContext.getBean(beanName);
		
	}

	public static MapFromToDAO getMapfromtodao() {
		return mapFromToDAO;
	}

	public static SyncDetailDAO getSyncdetaildao() {
		return syncDetailDAO;
	}

	public static SyncparaDAO getSyncparadao() {
		return syncparaDAO;
	}

}
