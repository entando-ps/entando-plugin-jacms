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
package com.agiletec.plugins.jacms.aps.system.services.content.widget;

import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;

import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import org.entando.entando.ent.exception.EntException;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.Widget;
import com.agiletec.aps.system.services.pagemodel.IPageModelManager;
import com.agiletec.aps.system.services.pagemodel.PageModel;
import com.agiletec.aps.tags.util.HeadInfoContainer;
import com.agiletec.aps.util.ApsProperties;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.IContentAuthorizationHelper;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.PublicContentAuthorizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.ContentRenderizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.IContentDispenser;
import org.apache.commons.lang3.StringUtils;

/**
 * Classe helper per i Widget di erogazione contenuti singoli.
 *
 * @author W.Ambu - E.Santoboni
 */
public class ContentViewerHelper implements IContentViewerHelper {

    private static final EntLogger logger = EntLogFactory.getSanitizedLogger(ContentViewerHelper.class);

    private IPageModelManager pageModelManager;
    private IContentModelManager contentModelManager;
    private IContentManager contentManager;
    private IContentDispenser contentDispenser;

    private IContentAuthorizationHelper contentAuthorizationHelper;

    @Override
    public String getRenderedContent(String contentId, String modelId, RequestContext reqCtx) throws EntException {
        return this.getRenderedContent(contentId, modelId, false, reqCtx);
    }

    /**
     * Restituisce il contenuto da visualizzare nel widget.
     *
     * @param contentId L'identificativo del contenuto ricavato dal tag.
     * @param modelId Il modello del contenuto ricavato dal tag.
     * @param publishExtraTitle
     * @param reqCtx Il contesto della richiesta.
     * @return Il contenuto da visualizzare nella widget.
     * @throws EntException In caso di errore.
     */
    @Override
    public String getRenderedContent(String contentId, String modelId, boolean publishExtraTitle, RequestContext reqCtx)
            throws EntException {
        String renderedContent = null;
        ContentRenderizationInfo renderInfo = this.getRenderizationInfo(contentId, modelId, publishExtraTitle, reqCtx);
        if (null != renderInfo) {
            renderedContent = renderInfo.getRenderedContent();
        }
        if (null == renderedContent) {
            renderedContent = "";
        }
        return renderedContent;
    }

    @Override
    public ContentRenderizationInfo getRenderizationInfo(String contentId, String modelId, boolean publishExtraTitle, RequestContext reqCtx)
            throws EntException {
        ContentRenderizationInfo renderizationInfo = null;
        try {
            Lang currentLang = (Lang) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_LANG);
            String langCode = currentLang.getCode();
            Widget widget = (Widget) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET);
            ApsProperties widgetConfig = (null != widget) ? widget.getConfig() : null;
            contentId = this.extractContentId(contentId, widgetConfig, reqCtx);
            modelId = this.extractModelId(contentId, modelId, widgetConfig, reqCtx);
            if (contentId != null && modelId != null) {
                long longModelId = Long.parseLong(modelId);
                this.setStylesheet(longModelId, reqCtx);
                renderizationInfo = this.getContentDispenser().getRenderizationInfo(contentId, longModelId, langCode, reqCtx, true);
                if (null == renderizationInfo) {
                    logger.info("Null Renderization informations: content={}", contentId);
                    return null;
                }
                this.getContentDispenser().resolveLinks(renderizationInfo, reqCtx);
                String cspToken = (String) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CSP_NONCE_TOKEN);
                renderizationInfo.replacePlaceholder(JacmsSystemConstants.CSP_NONCE_PLACEHOLDER, cspToken);
                this.manageAttributeValues(renderizationInfo, publishExtraTitle, reqCtx);
            } else {
                logger.warn("Incomplete content visualization parameters: contentId={} modelId={}", contentId, modelId);
            }
        } catch (Throwable t) {
            logger.error("Error extracting renderization info", t);
            throw new EntException("Error extracting renderization info", t);
        }
        return renderizationInfo;
    }

    @Override
    public PublicContentAuthorizationInfo getAuthorizationInfo(String contentId, RequestContext reqCtx) throws EntException {
        PublicContentAuthorizationInfo authInfo = null;
        try {
            Widget widget = (Widget) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET);
            contentId = this.extractContentId(contentId, widget.getConfig(), reqCtx);
            if (null == contentId) {
                logger.info("Null contentId");
                return null;
            }
            authInfo = this.getContentAuthorizationHelper().getAuthorizationInfo(contentId, true);
            if (null == authInfo) {
                logger.info("Null authorization info by content '" + contentId + "'");
            }
        } catch (Throwable t) {
            logger.error("Error extracting content authorization info by content {}", contentId, t);
            throw new EntException("Error extracting content authorization info by content '" + contentId + "'", t);
        }
        return authInfo;
    }

    protected void manageAttributeValues(ContentRenderizationInfo renderInfo, boolean publishExtraTitle, RequestContext reqCtx) {
        if (!publishExtraTitle) {
            return;
        }
        IPage page = (IPage) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_PAGE);
        if (null == page || !page.isUseExtraTitles()) {
            return;
        }
        Integer currentFrame = (Integer) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_FRAME);
        if (null == currentFrame) {
            return;
        }
        PageModel pageModel = this.getPageModelManager().getPageModel(page.getMetadata().getModelCode());
        if (currentFrame == pageModel.getMainFrame() && null != renderInfo) {
            Object extraTitle = renderInfo.getAttributeValues().get(JacmsSystemConstants.ATTRIBUTE_ROLE_TITLE);
            if (null != extraTitle) {
                reqCtx.addExtraParam(SystemConstants.EXTRAPAR_EXTRA_PAGE_TITLES, extraTitle);
            }
        }
    }

    /**
     * Metodo che determina con che ordine viene ricercato l'identificativo del
     * contenuto. L'ordine con cui viene cercato è questo: 1) Nel parametro
     * specificato all'interno del tag. 2) Tra i parametri di configurazione del
     * widget 3) Nella Request.
     *
     * @param contentId L'identificativo del contenuto specificato nel tag. Può
     * essere null o una Stringa alfanumerica.
     * @param widgetConfig I parametri di configurazione del widget corrente.
     * @param reqCtx Il contesto della richiesta.
     * @return L'identificativo del contenuto da erogare.
     */
    protected String extractContentId(String contentId, ApsProperties widgetConfig, RequestContext reqCtx) {
        if (null == contentId) {
            if (null != widgetConfig) {
                contentId = (String) widgetConfig.get("contentId");
            }
            if (null == contentId) {
                contentId = reqCtx.getRequest().getParameter(SystemConstants.K_CONTENT_ID_PARAM);
            }
        }
        if (null != contentId && contentId.trim().length() == 0) {
            contentId = null;
        }
        return contentId;
    }

    /**
     * Restituisce l'identificativo del modello con il quale renderizzare il
     * contenuto. Metodo che determina con che ordine viene ricercato
     * l'identificativo del modello di contenuto. L'ordine con cui viene cercato
     * è questo: 1) Nel parametro specificato all'interno del tag. 2) Tra i
     * parametri di configurazione del widget Nel caso non venga trovato nessun
     * ideentificativo, viene restituito l'identificativo del modello di default
     * specificato nella configurazione del tipo di contenuto.
     *
     * @param contentId L'identificativo del contenuto da erogare. Può essere
     * null, un numero in forma di stringa, o un'identificativo della tipologia
     * del modello 'list' (in tal caso viene restituito il modello per le liste
     * definito nella configurazione del tipo di contenuto) o 'default' (in tal
     * caso viene restituito il modello di default definito nella configurazione
     * del tipo di contenuto).
     * @param modelId L'identificativo del modello specificato nel tag. Può
     * essere null.
     * @param widgetConfig La configurazione del widget corrente nel qual è
     * inserito il tag erogatore del contenuti.
     * @param reqCtx Il contesto della richiesta.
     * @return L'identificativo del modello con il quale renderizzare il
     * contenuto.
     */
    protected String extractModelId(String contentId, String modelId, ApsProperties widgetConfig, RequestContext reqCtx) {
        modelId = this.extractConfiguredModelId(contentId, modelId, widgetConfig);
        if (null == modelId) {
            modelId = reqCtx.getRequest().getParameter("modelId");
        }
        if (null == modelId && null != contentId) {
            modelId = this.getContentManager().getDefaultModel(contentId);
        }
        modelId = this.checkModelId(contentId, modelId);
        return modelId;
    }

    protected String extractModelId(String contentId, String modelId, ApsProperties widgetConfig) {
        modelId = this.extractConfiguredModelId(contentId, modelId, widgetConfig);
        if (null == modelId && null != contentId) {
            modelId = this.getContentManager().getDefaultModel(contentId);
        }
        return modelId;
    }

    private String extractConfiguredModelId(String contentId, String modelId, ApsProperties widgetConfig) {
        modelId = this.checkModelId(contentId, modelId);
        if (null == modelId && null != widgetConfig) {
            String modelIdParamValue = (String) widgetConfig.get("modelId");
            modelId = this.checkModelId(contentId, modelIdParamValue);
        }
        return modelId;
    }
    
    private String checkModelId(String contentId, String modelIdToCheck) {
        if (null != modelIdToCheck && !ContentModel.isValidModelIdParam(modelIdToCheck.trim())) {
            logger.warn("Invalid content template '{}'", modelIdToCheck);
            return null;
        }
        if (!StringUtils.isEmpty(modelIdToCheck) && !StringUtils.isEmpty(contentId)) {
            if (modelIdToCheck.trim().equals(ContentModel.MODEL_ID_LIST)) { //NOSONAR
                return this.getContentManager().getListModel(contentId);
            } else if (modelIdToCheck.trim().equals(ContentModel.MODEL_ID_DEFAULT)) {
                return this.getContentManager().getDefaultModel(contentId);
            }
        }
        return modelIdToCheck;
    }

    protected void setStylesheet(long modelId, RequestContext reqCtx) {
        ContentModel model = this.getContentModelManager().getContentModel(modelId);
        if (model != null) {
            String stylesheet = model.getStylesheet();
            if (null != stylesheet && stylesheet.trim().length() > 0) {
                HeadInfoContainer headInfo = (HeadInfoContainer) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_HEAD_INFO_CONTAINER);
                if (headInfo != null) {
                    headInfo.addInfo("CSS", stylesheet);
                }
            }
        }
    }

    protected IPageModelManager getPageModelManager() {
        return pageModelManager;
    }
    
    public void setPageModelManager(IPageModelManager pageModelManager) {
        this.pageModelManager = pageModelManager;
    }

    protected IContentModelManager getContentModelManager() {
        return contentModelManager;
    }

    public void setContentModelManager(IContentModelManager contentModelManager) {
        this.contentModelManager = contentModelManager;
    }

    protected IContentManager getContentManager() {
        return contentManager;
    }

    public void setContentManager(IContentManager contentManager) {
        this.contentManager = contentManager;
    }

    protected IContentDispenser getContentDispenser() {
        return contentDispenser;
    }

    public void setContentDispenser(IContentDispenser contentDispenser) {
        this.contentDispenser = contentDispenser;
    }

    protected IContentAuthorizationHelper getContentAuthorizationHelper() {
        return contentAuthorizationHelper;
    }

    public void setContentAuthorizationHelper(IContentAuthorizationHelper contentAuthorizationHelper) {
        this.contentAuthorizationHelper = contentAuthorizationHelper;
    }

}
