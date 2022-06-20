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
package org.entando.entando.plugins.jacms.aps.system.services.api;

import com.agiletec.aps.system.common.entity.IEntityManager;
import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.PageManager;
import com.agiletec.aps.system.services.pagemodel.Frame;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import org.entando.entando.aps.system.common.entity.api.ApiEntityTypeInterface;
import org.entando.entando.aps.system.common.entity.api.JAXBEntityType;
import org.entando.entando.aps.system.services.api.IApiErrorCodes;
import org.entando.entando.aps.system.services.api.model.ApiError;
import org.entando.entando.aps.system.services.api.model.ApiException;
import org.entando.entando.aps.system.services.api.model.StringApiResponse;
import org.entando.entando.plugins.jacms.aps.system.services.api.model.JAXBContentType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Properties;

import com.agiletec.aps.system.services.pagemodel.IPageModelManager;
import com.agiletec.aps.system.services.pagemodel.PageModel;
import org.entando.entando.aps.system.services.widgettype.IWidgetTypeManager;
import org.entando.entando.aps.util.PageUtils;

/**
 * @author E.Santoboni
 */
public class ApiContentTypeInterface extends ApiEntityTypeInterface {

    @Autowired
    private PageManager pageManager;
    
    private IContentManager _contentManager;
    private IContentModelManager _contentModelManager;
    
    private IPageModelManager pageModelManager;
    private IWidgetTypeManager widgetTypeManager;

    public JAXBContentType getContentType(Properties properties) throws ApiException, Throwable {
        return (JAXBContentType) super.getEntityType(properties);
    }

	@Override
	protected JAXBEntityType createJAXBEntityType(IApsEntity masterEntityType) {
		Content masterContentType = (Content) masterEntityType;
		JAXBContentType jaxbContentType = new JAXBContentType(masterContentType);
		jaxbContentType.setDefaultModelId(this.extractModelId(masterContentType.getDefaultModel()));
		jaxbContentType.setListModelId(this.extractModelId(masterContentType.getListModel()));
		jaxbContentType.setViewPage(masterContentType.getViewPage());
        return jaxbContentType;
	}

    private Integer extractModelId(String stringModelId) {
        if (null == stringModelId) return null;
        Integer modelId = null;
        try {
            modelId = Integer.parseInt(stringModelId);
        } catch (Throwable t) {
            //nothing to catch
        }
        return modelId;
    }

    public StringApiResponse addContentType(JAXBContentType jaxbContentType) throws Throwable {
        return super.addEntityType(jaxbContentType);
    }

	@Override
	protected void checkNewEntityType(JAXBEntityType jaxbEntityType, IApsEntity newEntityType, StringApiResponse response) throws ApiException, Throwable {
		JAXBContentType jaxbContentType = (JAXBContentType) jaxbEntityType;
		Content contentType = (Content) newEntityType;
		boolean defaultModelCheck = this.checkContentModel(jaxbContentType.getDefaultModelId(), contentType, response);
		if (defaultModelCheck) {
			contentType.setDefaultModel(String.valueOf(jaxbContentType.getDefaultModelId()));
		}
		boolean listModelCheck = this.checkContentModel(jaxbContentType.getListModelId(), contentType, response);
		if (listModelCheck) {
			contentType.setListModel(String.valueOf(jaxbContentType.getListModelId()));
		}
        String viewPage = jaxbContentType.getViewPage();
        boolean viewPageCheck = this.checkViewPage(viewPage, response);
        if (viewPageCheck) {
            contentType.setViewPage(viewPage);
        }
	}

    public StringApiResponse updateContentType(JAXBContentType jaxbContentType) throws Throwable {
        return super.updateEntityType(jaxbContentType);
    }

	@Override
	protected void checkEntityTypeToUpdate(JAXBEntityType jaxbEntityType, IApsEntity entityTypeToUpdate, StringApiResponse response) throws ApiException, Throwable {
		JAXBContentType jaxbContentType = (JAXBContentType) jaxbEntityType;
		Content contentType = (Content) entityTypeToUpdate;
		boolean defaultModelCheck = this.checkContentModel(jaxbContentType.getDefaultModelId(), contentType, response);
		if (defaultModelCheck) {
			contentType.setDefaultModel(String.valueOf(jaxbContentType.getDefaultModelId()));
		}
		boolean listModelCheck = this.checkContentModel(jaxbContentType.getListModelId(), contentType, response);
		if (listModelCheck) {
			contentType.setListModel(String.valueOf(jaxbContentType.getListModelId()));
		}
        String viewPage = jaxbContentType.getViewPage();
        boolean viewPageCheck = this.checkViewPage(viewPage, response);
        if (viewPageCheck) {
            contentType.setViewPage(viewPage);
        }
	}

    private boolean checkViewPage(String viewPageCode, StringApiResponse response) {
        if (null != viewPageCode) {
            IPage viewPage =  this.getPageManager().getOnlinePage(viewPageCode);
            if (null == viewPage) {
                ApiError error = new ApiError(IApiErrorCodes.API_VALIDATION_ERROR,
                        "View Page with id '" + viewPageCode + "' does not exist", Response.Status.ACCEPTED);
                response.addError(error);
                return false;
            }
            PageModel pageModel = this.getPageModelManager().getPageModel(viewPage.getModelCode());
            final Frame[] configuration = pageModel.getConfiguration();
            final boolean mainFramePresent = Arrays.stream(configuration).anyMatch(Frame::isMainFrame);
            if (!mainFramePresent) {
                ApiError error = new ApiError(IApiErrorCodes.API_VALIDATION_ERROR,
                        "Main frame for Page with id '" + viewPage.getCode() + "' not present", Response.Status.ACCEPTED);
                response.addError(error);
                return false;
            }
            return PageUtils.isOnlineFreeViewerPage(viewPage, pageModel, null, this.getWidgetTypeManager());
        }
        return true;
    }

    private boolean checkContentModel(Integer modelId, Content contentType, StringApiResponse response) {
        if (null == modelId) {
			return true;
		}
        ContentModel contentModel = this.getContentModelManager().getContentModel(modelId);
        if (null == contentModel) {
            ApiError error = new ApiError(IApiErrorCodes.API_VALIDATION_ERROR,
					"Content model with id '" + modelId + "' does not exist", Response.Status.ACCEPTED);
            response.addError(error);
            return false;
        }
        if (!contentType.getTypeCode().equals(contentModel.getContentType())) {
            ApiError error = new ApiError(IApiErrorCodes.API_VALIDATION_ERROR,
					"Content model with id '" + modelId + "' is for contents of type '" + contentModel.getContentType() + "'", Response.Status.ACCEPTED);
            response.addError(error);
            return false;
        }
        return true;
    }

    public void deleteContentType(Properties properties) throws ApiException, Throwable {
        super.deleteEntityType(properties);
    }

	@Override
	protected String getTypeLabel() {
		return "Content type";
	}

	@Override
	protected String getTypeCodeParamName() {
		return "code";
	}

	@Override
	protected IEntityManager getEntityManager() {
		return this.getContentManager();
	}

    protected IContentManager getContentManager() {
        return _contentManager;
    }

    public void setContentManager(IContentManager contentManager) {
        this._contentManager = contentManager;
    }

    protected PageManager getPageManager() {
        return pageManager;
    }
    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    protected IContentModelManager getContentModelManager() {
        return _contentModelManager;
    }
    public void setContentModelManager(IContentModelManager contentModelManager) {
        this._contentModelManager = contentModelManager;
    }

    protected IPageModelManager getPageModelManager() {
        return pageModelManager;
    }
    public void setPageModelManager(IPageModelManager pageModelManager) {
        this.pageModelManager = pageModelManager;
    }

    protected IWidgetTypeManager getWidgetTypeManager() {
        return widgetTypeManager;
    }
    public void setWidgetTypeManager(IWidgetTypeManager widgetTypeManager) {
        this.widgetTypeManager = widgetTypeManager;
    }

}
