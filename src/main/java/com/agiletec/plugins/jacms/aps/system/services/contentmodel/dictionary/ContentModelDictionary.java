package com.agiletec.plugins.jacms.aps.system.services.contentmodel.dictionary;

import java.util.List;
import java.util.Properties;

import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.model.attribute.AbstractAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.AttributeInterface;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.IEntityModelDictionary;
import com.fasterxml.jackson.annotation.JsonValue;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.entando.entando.ent.exception.EntRuntimeException;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;


public class ContentModelDictionary implements IEntityModelDictionary {

    private final EntLogger logger = EntLogFactory.getSanitizedLogger(getClass());

    private static final String KEY_ROOT = "$content";
    
    private Map<String, Object> data = new LinkedHashMap<>();

    @JsonValue
    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String getEntityRootName() {
        return KEY_ROOT;
    }

    public ContentModelDictionary(List<String> contentConfig, List<String> i18nConfig, List<String> infoConfig, List<String> commonConfig, Properties publicAttributeMethods, IApsEntity prototype) {
        this.putAsMap(getEntityRootName(), contentConfig);
        if (null != prototype) {
            this.addAttributes(prototype, publicAttributeMethods);
        }
        this.putAsMap(KEY_I18N, i18nConfig);
        this.putAsMap(KEY_INFO, infoConfig);
        this.putAsList(commonConfig);
    }

    protected void addAttributes(IApsEntity prototype, Properties publicAttributeMethods) {
        for (AttributeInterface attribute : prototype.getAttributeList()) {
            List<String> attibuteMethodList = this.getAllowedAttributeMethods(attribute, publicAttributeMethods);
            this.putAsMap(attribute.getName(), attibuteMethodList);
        }
    }

    protected List<String> getAllowedAttributeMethods(AttributeInterface attribute, Properties publicAttributeMethods) {
        List<String> methods = new ArrayList<>();
        try {
            String methodsString = publicAttributeMethods.getProperty(attribute.getType());
            if (null != methodsString) {
                String[] methodsArray = methodsString.split(";");
                methods = Arrays.asList(methodsArray);
            } else {
                BeanInfo beanInfo = Introspector.getBeanInfo(attribute.getClass(), AbstractAttribute.class);
                PropertyDescriptor[] prDescrs = beanInfo.getPropertyDescriptors();
                for (int i = 0; i < prDescrs.length; i++) {
                    PropertyDescriptor propertyDescriptor = prDescrs[i];
                    if (null != propertyDescriptor.getReadMethod()) {
                        methods.add(propertyDescriptor.getDisplayName());
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("error loading allowed attribute methods for typeCode {} and attribute {}", attribute.getParentEntity().getTypeCode(), attribute.getName(), t);
            throw new EntRuntimeException("error loading allowed attribute methods for dictionary", t);
        }
        return methods;
    }

    protected void putAsMap(String key, List<String> list) {
        Map<String, String> result = list.stream().collect(HashMap::new, (m, v) -> m.put(v, null), HashMap::putAll);
        this.getData().put(key, result);
    }

    protected void putAsList(List<String> list) {
        Map<String, String> result = list.stream().collect(HashMap::new, (m, v) -> m.put(v, null), HashMap::putAll);
        this.getData().putAll(result);
    }
    
}
