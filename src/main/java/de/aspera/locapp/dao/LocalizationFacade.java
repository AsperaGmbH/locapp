/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.aspera.locapp.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import de.aspera.locapp.dto.Localization;
import de.aspera.locapp.dto.Localization.Status;
import de.aspera.locapp.util.HelperUtil;
import de.aspera.locapp.util.ValidationHelper;

/**
 *
 * @author daniel
 */
public class LocalizationFacade extends AbstractFacade<Localization> {

    private static final Logger logger = Logger.getLogger(LocalizationFacade.class.getName());

    private static final String EMPTY_PROPERTIES_HQL = " AND (target.value is null OR target.value = '') ";
    private static final String NOT_EMPTY_PROPERTIES_HQL = " AND target.value != '' ";

    public LocalizationFacade() {
        super(Localization.class);
    }

    /**
     * The method returns the last version by a localization status. The
     * parameter status can be null and returns the latest version without a
     * status process milestone.
     *
     * @param status
     * @return
     * @throws DatabaseException
     */
    public int lastVersion(Status status) throws DatabaseException {
        try {
            getEntityManager().getTransaction().begin();
            final String queryStr;
            if (status == null) {
                queryStr = "select distinct max(target.version) from " + Localization.class.getSimpleName() + " target";
            } else {
                queryStr = "select distinct max(target.version) from " + Localization.class.getSimpleName()
                        + " target where target.status = :status";
            }
            Query query = getEntityManager().createQuery(queryStr);
            if (status != null) {
                query.setParameter("status", status);
            }
            Integer lastVersion = (Integer) query.getSingleResult();
            getEntityManager().getTransaction().commit();
            return lastVersion == null ? 0 : lastVersion;
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }

    }

    public List<String> getLanguages(boolean exportProperties) throws DatabaseException {
        try {
            String queryStr = "select distinct target.locale from " + Localization.class.getSimpleName() + " target ";
            if (exportProperties) {
            	queryStr += " where target.status = :status AND target.version = :version";
            }
            Query query = getEntityManager().createQuery(queryStr);
            if (exportProperties) {
            	query.setParameter("version", lastVersion(Status.XLS));
            	query.setParameter("status", Status.XLS);
            }
            @SuppressWarnings("unchecked")
            List<String> languages = (List<String>) query.getResultList();
            return languages;
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    public List<Localization> getLocalizations(int lastVersion, Status status, boolean emptyProperties, String fullPath)
            throws DatabaseException {
        try {
            StringBuilder queryStr = new StringBuilder();
            String fullPathQuery = " AND target.fullPath = :fullPath ";

            queryStr.append("select target from " + Localization.class.getSimpleName()
                    + " target where target.version = :version" + " AND target.status = :status");
            if (StringUtils.isNotEmpty(fullPath))
                queryStr.append(fullPathQuery);
            if (emptyProperties)
                queryStr.append(EMPTY_PROPERTIES_HQL);
            queryStr.append(" order by target.key, target.locale asc ");

            Query query = getEntityManager().createQuery(queryStr.toString());
            query.setParameter("version", lastVersion);
            query.setParameter("status", status);
            if (StringUtils.isNotEmpty(fullPath))
                query.setParameter("fullPath", fullPath);
            @SuppressWarnings("unchecked")
            List<Localization> locs = (List<Localization>) query.getResultList();
            return locs;
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    public List<Localization> getLocalizationForIntegrityCheck(int lastVersion, Status status, Locale locale, boolean emptyProperties,
            String fullPath) throws DatabaseException {
        try {
            StringBuilder queryStr = new StringBuilder();
            String fullPathQuery = " AND target.fullPath = :fullPath ";
            String languageQuery = " AND target.locale = :locale ";

            queryStr.append("select target from " + Localization.class.getSimpleName()
                    + " target where target.version = :version" + " AND target.status = :status");
            if (StringUtils.isNotEmpty(fullPath))
                queryStr.append(fullPathQuery);

            if (locale != null)
                queryStr.append(languageQuery);

            if (!emptyProperties) {
                queryStr.append(NOT_EMPTY_PROPERTIES_HQL);
            }
            queryStr.append(" order by target.key, target.locale asc ");

            Query query = getEntityManager().createQuery(queryStr.toString());
            query.setParameter("version", lastVersion);
            query.setParameter("status", status);
            if (StringUtils.isNotEmpty(fullPath))
                query.setParameter("fullPath", fullPath);
            if (locale != null)
                query.setParameter("locale", locale.getLanguage().toLowerCase());

            @SuppressWarnings("unchecked")
            List<Localization> locs = (List<Localization>) query.getResultList();
            return locs;
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    public List<Localization> getLocalizationsWithLastVersion(int lastVersion) throws DatabaseException {
        try {
            getEntityManager().getTransaction().begin();
            String queryStr = "select target from " + Localization.class.getSimpleName()
                    + " target where target.version = :version" + " order by target.key asc";
            Query query = getEntityManager().createQuery(queryStr);
            query.setParameter("version", lastVersion);
            @SuppressWarnings("unchecked")
            List<Localization> locs = (List<Localization>) query.getResultList();
            getEntityManager().getTransaction().commit();
            return locs;
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }

    }

    public Set<String> getDefaultFiles(boolean exportProperties) throws DatabaseException {
        try {
            String queryStr = "select distinct target.fullPath from " + Localization.class.getSimpleName()
                    + " target where target.fullPath LIKE :fullPath ";
            if (exportProperties) {
            	queryStr += "AND target.status = :status AND target.version = :version";
            }
            Query query = getEntityManager().createQuery(queryStr);
            query.setParameter("fullPath", "%.properties");
            if (exportProperties) {
            	query.setParameter("version", lastVersion(Status.XLS));
            	query.setParameter("status", Status.XLS);
            }
            	
            @SuppressWarnings("unchecked")
			List<String> fullPaths = (List<String>) query.getResultList();
            Set<String> paths = new HashSet<>();
            for (String path : fullPaths) {
                if (HelperUtil.getLocaleFromPropertyFile(path).equals(Locale.ENGLISH.toString())) {
                    paths.add(path);
                } else {
                	paths.add(HelperUtil.removeLanguageFromPath(path));
                }
            }
            
            return paths;
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }

    }

    public void saveLocalizations(List<Localization> locs) throws DatabaseException {
        try {
            getEntityManager().getTransaction().begin();
            for (Localization loc : locs) {
                ValidationHelper.validateBean(loc);
                getEntityManager().persist(loc);
            }
            getEntityManager().getTransaction().commit();

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().setRollbackOnly();
            }
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    /**
     * The object will evict from the hibernate session.
     *
     * @param localization
     */
    public void detach(Localization localization) {
        getEntityManager().detach(localization);
    }

    public long countOfProperties(Status status, Locale locale, boolean emptyProperties) throws DatabaseException {
        try {
            String queryStr = null;

            if (locale != null) {
                queryStr = "select count(target.id) from " + Localization.class.getSimpleName()
                        + " target where target.status = :status " + " AND target.locale = :locale"
                        + " AND target.version = " + lastVersion(status);

            } else {
                queryStr = "select count(target.id) from " + Localization.class.getSimpleName()
                        + " target where target.status = :status" + " AND target.version = " + lastVersion(status);
            }
            if (emptyProperties)
                queryStr += EMPTY_PROPERTIES_HQL;

            Query query = getEntityManager().createQuery(queryStr);
            query.setParameter("status", status);
            if (locale != null)
                query.setParameter("locale", locale.getLanguage());
            Long countOf = (Long) query.getSingleResult();
            return countOf == null ? 0 : countOf;
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }

    }

}
