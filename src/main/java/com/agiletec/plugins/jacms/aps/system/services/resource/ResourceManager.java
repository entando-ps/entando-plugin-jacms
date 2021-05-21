/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.agiletec.plugins.jacms.aps.system.services.resource;

import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.common.AbstractService;
import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.aps.system.common.model.dao.SearcherDaoPaginatedResult;
import org.entando.entando.ent.exception.EntException;
import com.agiletec.aps.system.services.baseconfig.ConfigInterface;
import com.agiletec.aps.system.services.category.CategoryUtilizer;
import com.agiletec.aps.system.services.category.ICategoryManager;
import com.agiletec.aps.system.services.category.ReloadingCategoryReferencesThread;
import com.agiletec.aps.system.services.group.GroupUtilizer;
import com.agiletec.aps.system.services.keygenerator.IKeyGeneratorManager;
import com.agiletec.aps.util.DateConverter;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.resource.cache.IResourceManagerCacheWrapper;
import com.agiletec.plugins.jacms.aps.system.services.resource.event.ResourceChangedEvent;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.*;
import com.agiletec.plugins.jacms.aps.system.services.resource.parse.ResourceHandler;
import org.apache.commons.lang3.StringUtils;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntSafeXmlUtils;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Servizio gestore tipi di risorse (immagini, audio, video, etc..).
 *
 * @author W.Ambu - E.Santoboni
 */
public class ResourceManager extends AbstractService implements IResourceManager, GroupUtilizer, CategoryUtilizer {

    private final EntLogger logger = EntLogFactory.getSanitizedLogger(getClass());

    private IResourceDAO resourceDao;

    private ICategoryManager categoryManager;

    private ConfigInterface configManager;

    private IResourceManagerCacheWrapper cacheWrapper;

    /**
     * Mappa dei prototipi dei tipi di risorsa
     */
    private Map<String, ResourceInterface> resourceTypes;

    /**
     * Restutuisce il dao in uso al manager.
     *
     * @return Il dao in uso al manager.
     */
    protected IResourceDAO getResourceDAO() {
        return resourceDao;
    }

    /**
     * Setta il dao in uso al manager.
     *
     * @param resourceDao Il dao in uso al manager.
     */
    public void setResourceDAO(IResourceDAO resourceDao) {
        this.resourceDao = resourceDao;
    }

    protected ICategoryManager getCategoryManager() {
        return categoryManager;
    }

    public void setCategoryManager(ICategoryManager categoryManager) {
        this.categoryManager = categoryManager;
    }

    protected ConfigInterface getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigInterface configManager) {
        this.configManager = configManager;
    }

    protected IResourceManagerCacheWrapper getCacheWrapper() {
        return cacheWrapper;
    }

    public void setCacheWrapper(IResourceManagerCacheWrapper cacheWrapper) {
        this.cacheWrapper = cacheWrapper;
    }

    @Override
    public int getStatus() {
        return this.getCacheWrapper().getStatus();
    }

    protected void setStatus(int status) {
        this.getCacheWrapper().updateStatus(status);
    }

    public void setResourceTypes(Map<String, ResourceInterface> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    @Override
    public void init() throws Exception {
        this.getCacheWrapper().initCache();
        logger.debug("{} ready. Initialized {} resource types", this.getClass().getName(), this.resourceTypes.size());
    }

    /**
     * Crea una nuova istanza di un tipo di risorsa del tipo richiesto. Il nuovo tipo di risorsa è istanziato mediante
     * clonazione del prototipo corrispondente.
     *
     * @param typeCode Il codice del tipo di risorsa richiesto, come definito in configurazione.
     * @return Il tipo di risorsa istanziato (vuoto).
     */
    @Override
    public ResourceInterface createResourceType(String typeCode) {
        ResourceInterface resource = (ResourceInterface) resourceTypes.get(typeCode);
        return resource.getResourcePrototype();
    }

    /**
     * Restituisce la lista delle chiavi dei tipi risorsa presenti nel sistema.
     *
     * @return La lista delle chiavi dei tipi risorsa esistenti.
     */
    @Override
    public List<String> getResourceTypeCodes() {
        return new ArrayList<>(this.resourceTypes.keySet());
    }

    @Override
    public ResourceInterface addResource(ResourceDataBean bean) throws EntException {
        return addResource(bean, false);
    }

    @Override
    public ResourceInterface addResource(ResourceDataBean bean, boolean instancesAlreadySaved) throws EntException {
        ResourceInterface newResource = this.createResource(bean);
        try {
            this.generateAndSetResourceId(newResource, bean.getResourceId());
            newResource.setCorrelationCode(newResource.getCorrelationCode() == null ?
                    newResource.getId() : newResource.getCorrelationCode());
            newResource.saveResourceInstances(bean, getIgnoreMetadataKeysForResourceType(bean.getResourceType()), instancesAlreadySaved);
            this.getResourceDAO().addResource(newResource);
        } catch (Throwable t) {
            newResource.deleteResourceInstances();
            logger.error("Error adding resource", t);
            throw new EntException("Error adding resource", t);
        }
        return newResource;
    }

    private List<String> getIgnoreMetadataKeysForResourceType(String resourceType) {
        ResourceInterface resourcePrototype = createResourceType(resourceType);

        String ignoreKeysConf = resourcePrototype.getMetadataIgnoreKeys();
        String[] ignoreKeys = ignoreKeysConf.split(",");
        return Arrays.asList(ignoreKeys);
    }

    /**
     * Salva una lista dirisorse nel db con incluse nel filesystem, indipendentemente dal tipo.
     *
     * @param beans L'oggetto detentore dei dati della risorsa da inserire.
     * @throws EntException in caso di errore.
     */
    @Override
    public List<ResourceInterface> addResources(List<BaseResourceDataBean> beans) throws EntException {
        List<ResourceInterface> newResource = new ArrayList<>();
        beans.forEach(b -> {
            try {
                newResource.add(this.addResource(b));
            } catch (EntException ex) {
                logger.error("Error adding resources", ex);
            }
        });
        return newResource;
    }

    /**
     * Cancella una lista di risorse dal db ed i file di ogni istanza dal filesystem.
     *
     * @param resources La lista di risorse da cancellare.
     * @throws EntException in caso di errore nell'accesso al db.
     */
    @Override
    public void deleteResources(List<ResourceInterface> resources) throws EntException {
        resources.forEach(resourceDelete -> {
            try {
                deleteResource(resourceDelete);
            } catch (EntException ex) {
                logger.error("Error deleting resources", ex);
            }
        });
    }

    /**
     * Salva una lista di risorse nel db, indipendentemente dal tipo.y
     *
     * @param resource La risorsa da salvare.
     * @throws EntException in caso di errore.
     */
    @Override
    public void addResource(ResourceInterface resource) throws EntException {
        try {
            this.generateAndSetResourceId(resource, resource.getId());
            this.getResourceDAO().addResource(resource);
        } catch (Throwable t) {
            logger.error("Error adding resource", t);
            throw new EntException("Error adding resource", t);
        }
    }

    protected void generateAndSetResourceId(ResourceInterface resource, String id) throws EntException {
        if (null == id || id.trim().length() == 0) {
            IKeyGeneratorManager keyGenerator
                    = (IKeyGeneratorManager) this.getBeanFactory().getBean(SystemConstants.KEY_GENERATOR_MANAGER);
            int newId = keyGenerator.getUniqueKeyCurrentValue();
            resource.setId(String.valueOf(newId));
        }
    }

    @Override
    public void updateResource(ResourceDataBean bean) throws EntException {
        ResourceInterface oldResource = this.loadResource(bean.getResourceId());
        try {
            if (null == bean.getInputStream()) {
                oldResource.setDescription(bean.getDescr());
                oldResource.setCategories(bean.getCategories());
                oldResource.setMetadata(bean.getMetadata());
                oldResource.setMainGroup(bean.getMainGroup());
                oldResource.setFolderPath(bean.getFolderPath());
                this.getResourceDAO().updateResource(oldResource);
                this.notifyResourceChanging(oldResource);
            } else {
                ResourceInterface updatedResource = this.createResource(bean);
                updatedResource
                        .saveResourceInstances(bean, getIgnoreMetadataKeysForResourceType(bean.getResourceType()));
                this.getResourceDAO().updateResource(updatedResource);
                if (!updatedResource.getMasterFileName().equals(oldResource.getMasterFileName())) {
                    oldResource.deleteResourceInstances();
                }
                this.notifyResourceChanging(updatedResource);
            }
        } catch (Throwable t) {
            logger.error("Error updating resource", t);
            throw new EntException("Error updating resource", t);
        }
    }

    /**
     * Aggiorna una risorsa nel db.
     *
     * @param resource Il contenuto da aggiungere o modificare.
     * @throws EntException in caso di errore nell'accesso al db.
     */
    @Override
    public void updateResource(ResourceInterface resource) throws EntException {
        try {
            this.getResourceDAO().updateResource(resource);
            this.notifyResourceChanging(resource);
        } catch (Throwable t) {
            logger.error("Error updating resource", t);
            throw new EntException("Error updating resource", t);
        }
    }

    protected ResourceInterface createResource(ResourceDataBean bean) throws EntException {
        ResourceInterface resource = this.createResourceType(bean.getResourceType());
        resource.setDescription(bean.getDescr());
        resource.setMainGroup(bean.getMainGroup());
        resource.setCategories(bean.getCategories());
        resource.setMasterFileName(bean.getFileName());
        resource.setId(bean.getResourceId());
        resource.setMetadata(bean.getMetadata());
        resource.setOwner(bean.getOwner());
        resource.setFolderPath(bean.getFolderPath());
        resource.setCorrelationCode(bean.getCorrelationCode());
        return resource;
    }

    protected void notifyResourceChanging(ResourceInterface resource) throws EntException {
        ResourceChangedEvent event = new ResourceChangedEvent();
        event.setResource(resource);
        this.notifyEvent(event);
    }

    /**
     * Carica una lista di identificativi di risorse in base al tipo, ad una parola chiave e dalla categoria della
     * risorsa.
     *
     * @param type         Tipo di risorsa da cercare.
     * @param text         Testo immesso per il raffronto con la descrizione della risorsa. null o stringa vuota nel
     *                     caso non si voglia ricercare le risorse per parola chiave.
     * @param categoryCode Il codice della categoria delle risorse. null o stringa vuota nel caso non si voglia
     *                     ricercare le risorse per categoria.
     * @param groupCodes   I codici dei gruppi consentiti tramite il quale filtrare le risorse.
     * @return La lista di identificativi di risorse.
     * @throws EntException In caso di errore.
     */
    @Override
    public List<String> searchResourcesId(String type, String text,
            String categoryCode, Collection<String> groupCodes) throws EntException {
        return this.searchResourcesId(type, text, null, categoryCode, groupCodes);
    }

    @Override
    public List<String> searchResourcesId(String type, String text,
            String filename, String categoryCode, Collection<String> groupCodes) throws EntException {
        if (null == groupCodes || groupCodes.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> resourcesId = null;
        try {
            resourcesId = this.getResourceDAO().searchResourcesId(type, text, filename, categoryCode, groupCodes);
        } catch (Throwable t) {
            logger.error("Error searching resources id", t);
            throw new EntException("Error searching resources id", t);
        }
        return resourcesId;
    }

    @Override
    public List<String> searchResourcesId(FieldSearchFilter[] filters, List<String> categories) throws EntException {
        this.checkFilterKeys(filters);
        List<String> resourcesId = null;
        try {
            resourcesId = this.getResourceDAO().searchResourcesId(filters, categories);
        } catch (Throwable t) {
            logger.error("Error searching resources id", t);
            throw new EntException("Error searching resources id", t);
        }
        return resourcesId;
    }

    @Override
    public List<String> searchResourcesId(FieldSearchFilter[] filters, String categoryCode,
            Collection<String> groupCodes) throws EntException {
        List<String> categories = (StringUtils.isBlank(categoryCode)) ? null : Arrays.asList(categoryCode);
        return this.searchResourcesId(filters, categories, groupCodes);
    }

    @Override
    public List<String> searchResourcesId(FieldSearchFilter[] filters, List<String> categories,
            Collection<String> groupCodes) throws EntException {
        this.checkFilterKeys(filters);
        List<String> resourcesId = null;
        try {
            resourcesId = this.getResourceDAO().searchResourcesId(filters, categories, groupCodes);
        } catch (Throwable t) {
            logger.error("Error searching resources id", t);
            throw new EntException("Error searching resources id", t);
        }
        return resourcesId;
    }

    @Override
    public SearcherDaoPaginatedResult<String> getPaginatedResourcesId(FieldSearchFilter[] filters,
            List<String> categories, Collection<String> userGroupCodes) throws EntException {
        SearcherDaoPaginatedResult<String> pagedResult = null;
        try {
            int count = this.getResourceDAO().countResources(filters, categories, userGroupCodes);
            List<String> resourcesId = this.getResourceDAO().searchResourcesId(filters, categories, userGroupCodes);
            pagedResult = new SearcherDaoPaginatedResult<>(count, resourcesId);
        } catch (Throwable t) {
            logger.error("Error searching paginated resources id", t);
            throw new EntException("Error searching paginated resources id", t);
        }
        return pagedResult;
    }

    protected void checkFilterKeys(FieldSearchFilter[] filters) {
        if (null != filters && filters.length > 0) {
            String[] allowedFilterKeys = {RESOURCE_ID_FILTER_KEY, RESOURCE_TYPE_FILTER_KEY, RESOURCE_DESCR_FILTER_KEY,
                    RESOURCE_MAIN_GROUP_FILTER_KEY, RESOURCE_FILENAME_FILTER_KEY, RESOURCE_CREATION_DATE_FILTER_KEY,
                    RESOURCE_MODIFY_DATE_FILTER_KEY, RESOURCE_OWNER_FILTER_KEY, RESOURCE_FOLDER_PATH_FILTER_KEY};
            List<String> allowedFilterKeysList = Arrays.asList(allowedFilterKeys);
            for (int i = 0; i < filters.length; i++) {
                FieldSearchFilter filter = filters[i];
                if (!allowedFilterKeysList.contains(filter.getKey())) {
                    throw new RuntimeException("Invalid filter key - '" + filter.getKey() + "'");
                }
            }
        }
    }

    /**
     * Restituisce la risorsa con l'id specificato.
     *
     * @param id L'identificativo della risorsa da caricare.
     * @return La risorsa cercata. null se non vi è nessuna risorsa con l'identificativo immesso.
     * @throws EntException in caso di errore.
     */
    @Override
    public ResourceInterface loadResource(String id) throws EntException {
        return loadResource(id, null);
    }

    @Override
    public ResourceInterface loadResource(String id, String correlationCode) throws EntException {
        ResourceInterface resource = null;
        try {
            ResourceRecordVO resourceVo = loadResourceVo(id,
                    correlationCode);
            if (null != resourceVo) {
                resource = this.createResource(resourceVo);
                resource.setMasterFileName(resourceVo.getMasterFileName());
            }
        } catch (Throwable t) {
            logger.error("Error loading resource : id {}", id, t);
            throw new EntException("Error loading resource : id " + id, t);
        }
        return resource;
    }

    protected ResourceRecordVO loadResourceVo(String id, String correlationCode) {
        return correlationCode == null
                ? this.getResourceDAO().loadResourceVo(id)
                : this.getResourceDAO().loadResourceVoByCorrelationCode(correlationCode);
    }

    public boolean exists(String id, String correlationCode) {
        return loadResourceVo(id, correlationCode) != null;
    }

    /**
     * Metodo di servizio. Restituisce una risorsa in base ai dati del corrispondente record.
     *
     * @param resourceVo Il vo relativo al record.
     * @return La risorsa valorizzata.
     * @throws EntException in caso di errore.
     */
    protected ResourceInterface createResource(ResourceRecordVO resourceVo) throws EntException {
        String resourceType = resourceVo.getResourceType();
        String resourceXML = resourceVo.getXml();
        ResourceInterface resource = this.createResourceType(resourceType);
        this.fillEmptyResourceFromXml(resource, resourceXML);
        resource.setMainGroup(resourceVo.getMainGroup());
        resource.setCreationDate(resourceVo.getCreationDate());
        resource.setLastModified(resourceVo.getLastModified());
        resource.setOwner(resourceVo.getOwner());
        resource.setFolderPath(resourceVo.getFolderPath());
        resource.setCorrelationCode(resourceVo.getCorrelationCode());
        return resource;
    }

    /**
     * Valorizza una risorsa prototipo con gli elementi dell'xml che rappresenta una risorsa specifica.
     *
     * @param resource Il prototipo di risorsa da specializzare con gli attributi dell'xml.
     * @param xml      L'xml della risorsa specifica.
     * @throws EntException
     */
    protected void fillEmptyResourceFromXml(ResourceInterface resource, String xml) throws EntException {
        try {
            SAXParser parser = EntSafeXmlUtils.newSafeSAXParser();
            InputSource is = new InputSource(new StringReader(xml));
            ResourceHandler handler = new ResourceHandler(resource, this.getCategoryManager());
            parser.parse(is, handler);
        } catch (Throwable t) {
            logger.error("Error loading resource", t);
            throw new EntException("Error loading resource", t);
        }
    }

    /**
     * Cancella una risorsa dal db ed i file di ogni istanza dal filesystem.
     *
     * @param resource La risorsa da cancellare.
     * @throws EntException in caso di errore nell'accesso al db.
     */
    @Override
    public void deleteResource(ResourceInterface resource) throws EntException {
        try {
            this.getResourceDAO().deleteResource(resource.getId(), resource.getCorrelationCode());
            resource.deleteResourceInstances();
        } catch (Throwable t) {
            logger.error("Error deleting resource", t);
            throw new EntException("Error deleting resource", t);
        }
    }

    @Override
    public void refreshMasterFileNames() throws EntException {
        this.startResourceReloaderThread(null, ResourceReloaderThread.RELOAD_MASTER_FILE_NAME);
    }

    @Override
    public void refreshResourcesInstances(String resourceTypeCode) throws EntException {
        this.startResourceReloaderThread(resourceTypeCode, ResourceReloaderThread.REFRESH_INSTANCE);
    }

    protected void startResourceReloaderThread(String resourceTypeCode, int operationCode) throws EntException {
        if (this.getStatus() != STATUS_READY) {
            logger.info("Service not ready : status {}", this.getStatus());
            return;
        }
        String threadName =
                this.getName() + "_resourceReloader_" + DateConverter.getFormattedDate(new Date(), "yyyyMMdd");
        try {
            List<String> resources = this.getResourceDAO().searchResourcesId(resourceTypeCode, null, null, null);
            ResourceReloaderThread thread = new ResourceReloaderThread(this, operationCode, resources);
            thread.setName(threadName);
            thread.start();
            logger.info("Reloader started");
        } catch (Throwable t) {
            logger.error("Error refreshing Resource of type {} - Thread Name '{}'", resourceTypeCode, threadName, t);
        }
    }

    protected void refreshMasterFileNames(String resourceId) {
        try {
            ResourceInterface resource = this.loadResource(resourceId);
            if (resource.isMultiInstance()) {
                ResourceInstance instance
                        = ((AbstractMultiInstanceResource) resource).getInstance(0, null);
                String filename = instance.getFileName();
                int index = filename.lastIndexOf("_d0.");
                String masterFileName = filename.substring(0, index) + filename.substring(index + 3);
                resource.setMasterFileName(masterFileName);
            } else {
                ResourceInstance instance
                        = ((AbstractMonoInstanceResource) resource).getInstance();
                resource.setMasterFileName(instance.getFileName());
            }
            this.updateResource(resource);
        } catch (Throwable t) {
            logger.error("Error reloading master file name of resource {}", resourceId, t);
        }
    }

    protected void refreshResourceInstances(String resourceId) {
        try {
            ResourceInterface resource = this.loadResource(resourceId);
            resource.reloadResourceInstances();
            this.updateResource(resource);
        } catch (Throwable t) {
            logger.error("Error refreshing resource instances of resource {}", resourceId, t);
        }
    }

    @Override
    public List<String> getGroupUtilizers(String groupName) throws EntException {
        List<String> resourcesId = null;
        try {
            List<String> allowedGroups = new ArrayList<>(1);
            allowedGroups.add(groupName);
            resourcesId = this.getResourceDAO().searchResourcesId(null, null, null, null, allowedGroups);
        } catch (Throwable t) {
            logger.error("Error searching group utilizers : group '{}'", groupName, t);
            throw new EntException("Error searching group utilizers : group '" + groupName + "'", t);
        }
        return resourcesId;
    }

    @Override
    public List getCategoryUtilizers(String categoryCode) throws EntException {
        List<String> resourcesId = null;
        try {
            resourcesId = this.getResourceDAO().searchResourcesId(null, null, null, categoryCode, null);
        } catch (Throwable t) {
            logger.error("Error searching category utilizers : category code '{}'", categoryCode, t);
            throw new EntException("Error searching category utilizers : category code '" + categoryCode + "'", t);
        }
        return resourcesId;
    }

    @Override
    public void reloadCategoryReferences(String categoryCode) throws EntException {
        try {
            List<String> resources = this.getCategoryUtilizersForReloadReferences(categoryCode);
            logger.info("start reload category references for {} resources", resources.size());
            ReloadingCategoryReferencesThread th = null;
            Thread currentThread = Thread.currentThread();
            if (currentThread instanceof ReloadingCategoryReferencesThread) {
                th = (ReloadingCategoryReferencesThread) Thread.currentThread();
                th.setListSize(resources.size());
            }
            if (null != resources && !resources.isEmpty()) {
                Iterator<String> it = resources.iterator();
                while (it.hasNext()) {
                    String code = it.next();
                    ResourceInterface resource = this.loadResource(code);
                    this.getResourceDAO().updateResourceRelations(resource);
                    if (null != th) {
                        th.setListIndex(th.getListIndex() + 1);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Error searching category utilizers : category code '{}'", categoryCode, t);
            throw new EntException("Error searching category utilizers : category code '" + categoryCode + "'", t);
        }
    }

    @Override
    public List getCategoryUtilizersForReloadReferences(String categoryCode) throws EntException {
        List<String> resourcesId = null;
        try {
            resourcesId = this.getCategoryUtilizers(categoryCode);
        } catch (Throwable t) {
            throw new EntException("Error searching category utilizers : category code '" + categoryCode + "'", t);
        }
        return resourcesId;
    }

    @Override
    public Map<String, List<String>> getMetadataMapping() {
        Map<String, List<String>> cachedMapping = this.getCacheWrapper().getMetadataMapping();
        if (null != cachedMapping) {
            return cachedMapping;
        }
        Map<String, List<String>> mapping = new HashMap<>();
        try {
            String xmlConfig = this.getConfigManager()
                    .getConfigItem(JacmsSystemConstants.CONFIG_ITEM_RESOURCE_METADATA_MAPPING);
            InputStream stream = new ByteArrayInputStream(xmlConfig.getBytes(StandardCharsets.UTF_8));
            JAXBContext context = JAXBContext.newInstance(JaxbMetadataMapping.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JaxbMetadataMapping jaxbMapping = (JaxbMetadataMapping) unmarshaller.unmarshal(stream);
            jaxbMapping.getFields().stream().forEach(m -> {
                String key = m.getKey();
                String csv = m.getValue();
                List<String> metadatas =
                        (!StringUtils.isBlank(csv)) ? Arrays.asList(csv.split(",")) : new ArrayList<>();
                mapping.put(key, metadatas);
            });
            this.getCacheWrapper().updateMetadataMapping(mapping);
        } catch (Exception e) {
            logger.error("Error Extracting resource metadata mapping", e);
            throw new RuntimeException("Error Extracting resource metadata mapping", e);
        }
        return mapping;
    }

    @Override
    public void updateMetadataMapping(Map<String, List<String>> mapping) throws EntException {
        try {
            JAXBContext context = JAXBContext.newInstance(JaxbMetadataMapping.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(new JaxbMetadataMapping(mapping), writer);
            String config = writer.toString();
            this.getConfigManager()
                    .updateConfigItem(JacmsSystemConstants.CONFIG_ITEM_RESOURCE_METADATA_MAPPING, config);
            this.getCacheWrapper().updateMetadataMapping(mapping);
        } catch (Exception e) {
            logger.error("Error Updating resource metadata mapping", e);
            throw new EntException("Error Updating resource metadata mapping", e);
        }
    }

}
