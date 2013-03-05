package com.tnt.db;

import java.util.List;
import org.hibernate.LockMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object (DAO) providing persistence and search support for
 * Syncpara entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see com.tnt.db.Syncpara
 * @author MyEclipse Persistence Tools
 */

public class SyncparaDAO extends HibernateDaoSupport {
	private static final Logger log = LoggerFactory
			.getLogger(SyncparaDAO.class);
	// property constants
	public static final String TYPE = "type";
	public static final String DATA = "data";

	protected void initDao() {
		// do nothing
	}

	public void save(Syncpara transientInstance) {
		log.debug("saving Syncpara instance");
		try {
			getHibernateTemplate().saveOrUpdate(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(Syncpara persistentInstance) {
		log.debug("deleting Syncpara instance");
		try {
			getHibernateTemplate().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public Syncpara findById(java.lang.Integer id) {
		log.debug("getting Syncpara instance with id: " + id);
		try {
			Syncpara instance = (Syncpara) getHibernateTemplate().get(
					"com.tnt.db.Syncpara", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(Syncpara instance) {
		log.debug("finding Syncpara instance by example");
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
		log.debug("finding Syncpara instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from Syncpara as model where model."
					+ propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByType(Object type) {
		return findByProperty(TYPE, type);
	}

	public List findByData(Object data) {
		return findByProperty(DATA, data);
	}

	public List findAll() {
		log.debug("finding all Syncpara instances");
		try {
			String queryString = "from Syncpara";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public Syncpara merge(Syncpara detachedInstance) {
		log.debug("merging Syncpara instance");
		try {
			Syncpara result = (Syncpara) getHibernateTemplate().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(Syncpara instance) {
		log.debug("attaching dirty Syncpara instance");
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(Syncpara instance) {
		log.debug("attaching clean Syncpara instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public static SyncparaDAO getFromApplicationContext(ApplicationContext ctx) {
		return (SyncparaDAO) ctx.getBean("SyncparaDAO");
	}
}