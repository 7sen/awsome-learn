package com.shensen.learn.fastjson.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.lang.reflect.Type;

/**
 * FastJson 自定义对象{@code List<Long>} 属性反序列化方式.
 *
 * @author Alwyn
 * @date 2020-06-19 11:20
 */
public class SubjectListDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONObject json = JSON.parseObject(parser.getInput());
        return (T) json.getJSONArray((String) fieldName);
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

}