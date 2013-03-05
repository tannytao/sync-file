package com.tnt.db;

import java.util.List;
import org.hibernate.LockMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object (DAO) providing persistence and search support for
 * MapFromTo entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see com.tnt.db.MapFromTo
 * @author MyEclipse Persistence Tools
 */

public class MapFromToDAO extends HibernateDaoSupport {
	private static final Logger log = LoggerFactory
			.getLogger(MapFromToDAO.class);
	// property constants
	public static final String _SFOLD = "SFold";
	public static final String _DFOLD = "DFold";

	protected void initDao() {
		// do nothing
	}

	public void save(MapFromTo transientInstance) {
		log.debug("saving MapFromTo instance");
		try {
			getHibernateTemplate().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MapFromTo persistentInstance) {
		log.debug("deleting MapFromTo instance");
		try {
			getHibernateTemplate().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MapFromTo findById(java.lang.Integer id) {
		log.debug("getting MapFromTo instance with id: " + id);
		try {
			MapFromTo instance = (MapFromTo) getHibernateTemplate().get(
					"com.tnt.db.MapFromTo", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MapFromTo instance) {
		log.debug("finding MapFromTo instance by example");
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
		log.debug("finding MapFromTo instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MapFromTo as model where model."
					+ propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findBySFold(Object SFold) {
		return findByProperty(_SFOLD, SFold);
	}

	public List findByDFold(Object DFold) {
		return findByProperty(_DFOLD, DFold);
	}

	public List findAll() {
		log.debug("finding all MapFromTo instances");
		try {
			String queryString = "from MapFromTo as mft order by mft.SFold";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MapFromTo merge(MapFromTo detachedInstance) {
		log.debug("merging MapFromTo instance");
		try {
			MapFromTo result = (MapFromTo) getHibernateTemplate().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MapFromTo instance) {
		log.debug("attaching dirty MapFromTo instance");
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MapFromTo instance) {
		log.debug("attaching clean MapFromTo instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public static MapFromToDAO getFromApplicationContext(ApplicationContext ctx) {
		return (MapFromToDAO) ctx.getBean("MapFromToDAO");
	}
}