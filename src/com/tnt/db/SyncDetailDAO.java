package com.tnt.db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object (DAO) providing persistence and search support for
 * SyncDetail entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see com.tnt.db.SyncDetail
 * @author MyEclipse Persistence Tools
 */

public class SyncDetailDAO extends HibernateDaoSupport {
	private static final Logger log = LoggerFactory
			.getLogger(SyncDetailDAO.class);
	// property constants
	public static final String SOURCE_FILE = "sourceFile";
	public static final String TARGET_FILE = "targetFile";
	public static final String SYNC_COUNT = "syncCount";

	protected void initDao() {
		// do nothing
	}

	public void save(SyncDetail transientInstance) {
		log.debug("saving SyncDetail instance");
		try {
			getHibernateTemplate().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(SyncDetail persistentInstance) {
		log.debug("deleting SyncDetail instance");
		try {
			getHibernateTemplate().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SyncDetail findById(java.lang.Integer id) {
		log.debug("getting SyncDetail instance with id: " + id);
		try {
			SyncDetail instance = (SyncDetail) getHibernateTemplate().get(
					"com.tnt.db.SyncDetail", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(SyncDetail instance) {
		log.debug("finding SyncDetail instance by example");
		try {
			List results = getHibernateTemplate().findByExample(instance);
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	public List findByProperty(String propertyName, Object value) {
		log.debug("finding SyncDetail instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from SyncDetail as model where model."
					+ propertyName + "= ? order by model.id desc";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}
	
	public List findWithoutDelByProperty_bak(String propertyName, Object value,Date date) {
		log.info("find detail without delete by source_file,operTime");
		try {
			
			date.setTime(date.getTime()+24*3600*1000);
			return getSession().createCriteria(SyncDetail.class)
				.add(Restrictions.eq( SOURCE_FILE,value.toString()))
				.add(Restrictions.ne("operDesc", "delete"))
				.add(Restrictions.lt("operTime", date))
				.addOrder(Order.desc("id")).list();
			
			
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}
	
	public List findWithoutDelByProperty(String propertyName, Object value,String date) {
		
		try {
			String queryString = "from SyncDetail as model where model."
					+ propertyName + "= ? and model.operDesc<>'delete' and operTime <= '"+date+" 23:59:59"+"' order by model.id desc";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findBySourceFileWithoutDelFile(Object sourceFile,String date) {
		
		return findWithoutDelByProperty(SOURCE_FILE, sourceFile,date);
	}
	
	public List findBySourceFile(Object sourceFile) {
		return findByProperty(SOURCE_FILE, sourceFile);
	}

	public List findByTargetFile(Object targetFile) {
		return findByProperty(TARGET_FILE, targetFile);
	}

	public List findBySyncCount(Object syncCount) {
		return findByProperty(SYNC_COUNT, syncCount);
	}

	public List findAll() {
		log.debug("finding all SyncDetail instances");
		try {
			String queryString = "from SyncDetail";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public SyncDetail merge(SyncDetail detachedInstance) {
		log.debug("merging SyncDetail instance");
		try {
			SyncDetail result = (SyncDetail) getHibernateTemplate().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(SyncDetail instance) {
		log.debug("attaching dirty SyncDetail instance");
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SyncDetail instance) {
		log.debug("attaching clean SyncDetail instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List findBackedFileList(){
		log.debug("find backed filr list");
		try {
			return getHibernateTemplate().execute(
					new HibernateCallback(){
						@Override
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							StringBuffer sql = new StringBuffer();
							sql.append(" select distinct source_file from sync_detail order by source_file");
							Query query = session.createSQLQuery(sql.toString());
							return query.list();
						}				
			});
		} catch(RuntimeException re) {
			log.error("query failed", re);
			throw re;
		}
	}
	
	/**
	 * 通过备份文件名查找包括自身和子文件的最后一次备份详情
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SyncDetail> findLastBackupDetailByTargetFile(final String targetFile,final String date){
		try {
			return getHibernateTemplate().execute(
					new HibernateCallback(){
						@Override
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							StringBuffer sql = new StringBuffer();
							sql.append("select * from sync_detail where " +
									" id in (select max(id) from sync_detail where oper_time <= '"+date+" 23:59:59' group by source_file) " +
									" and oper_desc <> 'delete'" +									
									" and target_file like '"+targetFile.replace("\\", "\\\\\\\\")+"%'");
							Query query = session.createSQLQuery(sql.toString()).addEntity(SyncDetail.class);
							return query.list();
						}				
			});
		} catch(RuntimeException re) {
			log.error("query failed", re);
			throw re;
		}
	}
	
	/**
	 * 通过备份文件名查找包括自身和子文件的最后一次备份详情
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SyncDetail> findLastBackupDetailBySourceFile(final String sourceFile,final String date){
		try {
			return getHibernateTemplate().execute(
					new HibernateCallback(){
						@Override
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							StringBuffer sql = new StringBuffer();
							sql.append("select * from sync_detail where " +
									" id in (select max(id) from sync_detail where oper_time <= '"+date+" 23:59:59' group by source_file) " +
									" and oper_desc <> 'delete'" +									
									" and source_file like '"+sourceFile.replace("\\", "\\\\\\\\")+"%'");
							Query query = session.createSQLQuery(sql.toString()).addEntity(SyncDetail.class);
							return query.list();
						}				
			});
		} catch(RuntimeException re) {
			log.error("query failed", re);
			throw re;
		}
	}
	
	/**
	 * 获取每个文件的最后一次备份详情，并且过滤掉删除操作
	 * 用于找出删除的文件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SyncDetail> findLastBackupDetailFilterDel(){
		try {
			return getHibernateTemplate().execute(
					new HibernateCallback(){
						@Override
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							StringBuffer sql = new StringBuffer();
							sql.append("select * from sync_detail where id in (select max(id) from sync_detail group by source_file) and oper_desc <> 'delete'");
							Query query = session.createSQLQuery(sql.toString()).addEntity(SyncDetail.class);
							return query.list();
						}				
			});
		} catch(RuntimeException re) {
			log.error("query failed", re);
			throw re;
		}
	}

	public static SyncDetailDAO getFromApplicationContext(ApplicationContext ctx) {
		return (SyncDetailDAO) ctx.getBean("SyncDetailDAO");
	}
}