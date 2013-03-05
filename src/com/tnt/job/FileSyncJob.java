package com.tnt.job;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.quartz.UnableToInterruptJobException;

import com.tnt.db.MapFromTo;
import com.tnt.db.MapFromToDAO;
import com.tnt.db.SyncDetailDAO;
import com.tnt.util.HibernateSessionFactory;
import com.tnt.util.SpringFactory;
import com.tnt.util.StaticUtil;


public class FileSyncJob implements StatefulJob, InterruptableJob {
	/** logger */
	private static final Log log = LogFactory.getLog(FileSyncJob.class);
	
	public FileSyncJob() {
		//
	}

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
	
		fileSync();
		JobDataMap jobDataMap = ctx.getMergedJobDataMap();
		 
		DefaultTreeModel model = (DefaultTreeModel)jobDataMap.get("model");
		JTree tree = (JTree)jobDataMap.get("tree");
        StaticUtil.refreshBakTreeTop(model,tree);

	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		// super.interrupt();
	}
	
	private void fileSync(){
		MapFromToDAO mapFromToDAO = SpringFactory.getMapfromtodao();
		SyncDetailDAO syncDetailDao = SpringFactory.getSyncdetaildao();
		List<MapFromTo> list = mapFromToDAO.findAll();
		for(MapFromTo mft:list){
			try {
				log.info("Ready for Syncing");
				Calendar calendar = Calendar.getInstance();
				if(calendar.get(Calendar.DAY_OF_WEEK)==3){
					log.info("full bak");
					StaticUtil.fileSync(false,mft.getSFold(),mft.getDFold(),mft.getSFold(),mft.getDFold(),syncDetailDao);	
				}else{
					log.info("not full bak");
					StaticUtil.fileSync(false,mft.getSFold(),mft.getDFold(),mft.getSFold(),mft.getDFold(),syncDetailDao);
				}								
				log.info("Sync over");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.warn(e);
				e.printStackTrace();
			}
		}
		StaticUtil.findDeleteFile(syncDetailDao);
	}
	

}

