package de.aspera.locapp.dao;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Query;

import de.aspera.locapp.dto.Config;
import de.aspera.locapp.util.ValidationHelper;

/**
 *
 * @author Bjoern.Buchholz
 *
 */
public class ConfigFacade extends AbstractFacade<Config> {

	private static final Logger LOGGER = Logger.getLogger(ConfigFacade.class.getName());

	protected static String DEFAULT_LANGUAGE_KEY = "defaultLanguage";
	public static String DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage();

	public ConfigFacade() {
		super(Config.class);
	}

	public String[] getValue(String key) throws DatabaseException {

		String[] values = new String[] {};
		getEntityManager().getTransaction().begin();
		String queryStr = "select target.value from " + Config.class.getSimpleName() + " target where target.key =:key";
		Query query = getEntityManager().createQuery(queryStr);
		query.setParameter("key", key);
		@SuppressWarnings("rawtypes")
		List resultList = query.getResultList();
		if (resultList != null && resultList.size() > 0)
			values = resultList.get(0).toString().split(",");
		// String value = (String) query.getSingleResult();
		getEntityManager().getTransaction().commit();
		return values;
	}

	public void saveConfig(List<Config> configs) throws DatabaseException {
		try {
			getEntityManager().getTransaction().begin();
			for (Config config : configs) {
				ValidationHelper.validateBean(config);
				getEntityManager().merge(config);
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

	public void setDefaultLanguage(Locale locale) throws DatabaseException {
		if (locale == null) {
			throw new IllegalArgumentException("Locale is null");
		}

		var config = getLangConfig();
		config.setValue(new String[] { locale.getLanguage() });

		saveConfig(List.of(config));
	}

	public String getDefaultLanguage() throws DatabaseException {
		var langOpts = getValue(DEFAULT_LANGUAGE_KEY);

		if (langOpts.length == 0) {
			return DEFAULT_LANGUAGE;
		}

		return langOpts[0];
	}

	private Config getLangConfig() {
		getEntityManager().getTransaction().begin();

		Query query = getEntityManager()
			.createQuery("from " + Config.class.getSimpleName() + " target where target.key = :key")
			.setParameter("key", DEFAULT_LANGUAGE_KEY);

		@SuppressWarnings("rawtypes")
		List resultList = query.getResultList();

		getEntityManager().getTransaction().commit();

		if (resultList.size() == 0) {
			return createDefaultLangConfig();
		}
		
		return (Config)resultList.get(0);
	}

	private Config createDefaultLangConfig() {
		var config = new Config();
		config.setKey(DEFAULT_LANGUAGE_KEY);
		config.setValue(new String[] { DEFAULT_LANGUAGE });

		return config;
	}
}
