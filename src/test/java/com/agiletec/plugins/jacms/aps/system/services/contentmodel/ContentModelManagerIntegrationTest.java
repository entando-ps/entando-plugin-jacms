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
package com.agiletec.plugins.jacms.aps.system.services.contentmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SmallContentType;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @version 1.1
 * @author W.Ambu - S.Didaci - C.Siddi
 */
class ContentModelManagerIntegrationTest extends BaseTestCase {
	
    @Test
    public void testGetContentModel() {
    	ContentModel model = this._contentModelManager.getContentModel(1);
    	assertNotNull(model);
    }
    
    @Test
    public void testGetContentModels() {
    	List<ContentModel> models = this._contentModelManager.getContentModels();
    	assertNotNull(models);
    	assertEquals(6, models.size());
    }
    
    @Test
    public void testGetModelsForContentType() {
    	List<ContentModel> models = this._contentModelManager.getModelsForContentType("ART");
    	assertNotNull(models);
    	assertEquals(4, models.size());
    }
    
    @Test
    public void testAddDeleteContentModel() throws Throwable {
    	List<ContentModel> contentModels = this._contentModelManager.getContentModels();
    	int size = contentModels.size();
    	ContentModel contentModel = new ContentModel();
    	contentModel.setId(99);
    	contentModel.setContentType("ART");
    	contentModel.setDescription("Descr_Prova");
    	contentModel.setContentShape("<h2></h2>");
    	try {
    		assertNull(this._contentModelManager.getContentModel(99));
    		this._contentModelManager.addContentModel(contentModel);
    		contentModels = this._contentModelManager.getContentModels();
    		assertEquals((size + 1), contentModels.size());
    		assertNotNull(this._contentModelManager.getContentModel(3));
    		this._contentModelManager.removeContentModel(contentModel);
    		contentModels = this._contentModelManager.getContentModels();
    		assertEquals(size, contentModels.size());
    		assertNull(this._contentModelManager.getContentModel(99));
    	} catch (Throwable t) {
			throw t;
		} finally {
			this._contentModelManager.removeContentModel(contentModel);
		}
    }
    
    @Test
    public void testUpdateContentModel() throws Throwable {
    	List<ContentModel> contentModels = _contentModelManager.getContentModels();
    	int size = contentModels.size();
    	ContentModel contentModel = new ContentModel();
    	contentModel.setId(99);
    	contentModel.setContentType("ART");
    	contentModel.setDescription("Descr_Prova");
    	contentModel.setContentShape("<h2></h2>");
    	try {
    		assertNull(this._contentModelManager.getContentModel(99));
    		this._contentModelManager.addContentModel(contentModel);
    		contentModels = this._contentModelManager.getContentModels();
    		assertEquals((size + 1), contentModels.size());
    		
    		ContentModel contentModelNew = new ContentModel();
			contentModelNew.setId(contentModel.getId());
			contentModelNew.setContentType("RAH");
	    	contentModelNew.setDescription("Descr_Prova");
	    	contentModelNew.setContentShape("<h1></h1>");
	    	this._contentModelManager.updateContentModel(contentModelNew);
    		ContentModel extracted = this._contentModelManager.getContentModel(99);
    		assertEquals(contentModel.getDescription(), extracted.getDescription());
    		
    		this._contentModelManager.removeContentModel(contentModel);
    		contentModels = this._contentModelManager.getContentModels();
    		assertEquals(size, contentModels.size());
    		assertNull(this._contentModelManager.getContentModel(99));
    	} catch (Throwable t) {
			throw t;
		} finally {
			this._contentModelManager.removeContentModel(contentModel);
		}
    }
    
    @Test
    public void testGetReferencingPages() {
    	Map<String, List<IPage>> utilizers = this._contentModelManager.getReferencingPages(2);
    	assertNotNull(utilizers);
    	assertEquals(1, utilizers.size());
    }
    
    @Test
    public void testGetTypeUtilizer() throws Throwable {
    	SmallContentType utilizer = this._contentModelManager.getDefaultUtilizer(1);
    	assertNotNull(utilizer);
    	assertEquals("ART", utilizer.getCode());
    	
    	utilizer = this._contentModelManager.getDefaultUtilizer(11);
    	assertNotNull(utilizer);
    	assertEquals("ART", utilizer.getCode());
    	
    	utilizer = this._contentModelManager.getDefaultUtilizer(126);
    	assertNotNull(utilizer);
    	assertEquals("RAH", utilizer.getCode());
    }
    
    @BeforeEach
    private void init() throws Exception {
    	try {
    		this._contentModelManager = (IContentModelManager) this.getService(JacmsSystemConstants.CONTENT_MODEL_MANAGER);
    	} catch (Throwable t) {
            throw new Exception(t);
        }
    }
    
    private IContentModelManager _contentModelManager;
    
}