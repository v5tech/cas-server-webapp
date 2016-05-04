package org.jasig.services.persondir.support;

import org.jasig.services.persondir.IPersonAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扩展用户自定义属性
 * Created by Administrator on 2016/4/30 0030.
 */
public class MultipleAttributeUserDao extends StubPersonAttributeDao {
    @Override
    public IPersonAttributes getPerson(String uid) {
        Map<String, List<Object>> attributes = new HashMap<String, List<Object>>();
        attributes.put("userid", Collections.singletonList((Object) uid));
        attributes.put("username", Collections.singletonList((Object) "测试username"));
        attributes.put("email", Collections.singletonList((Object) "test@163.com"));
        return new AttributeNamedPersonImpl(attributes);
    }
}
