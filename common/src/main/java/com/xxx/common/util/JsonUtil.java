package com.xxx.common.util;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xxx.common.enums.CodesEnum;
import com.xxx.common.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 Jackson 进行 JSON 序列化和反序列化的工具类。
 */
@Slf4j
public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<Class<?>, ObjectMapper> MIXIN_MAPPERS_CACHE = new ConcurrentHashMap<>();

    static {
        // 配置 ObjectMapper 以忽略在 JSON 中存在但在 Java 对象中不存在的属性
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 注册 JavaTimeModule 以支持 Java 8 的日期和时间 API (LocalDate, LocalDateTime, etc.)
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        // 禁用将日期序列化为时间戳（数组）的行为，而是序列化为 ISO-8601 格式的字符串
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private JsonUtil() {
        // 私有构造函数，防止实例化
    }

    /**
     * 将任何 Java 对象序列化为 JSON 字符串。
     *
     * @param object 要序列化的对象。
     * @return 对象的 JSON 字符串表示。
     * @throws RuntimeException 如果序列化过程中发生错误。
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            log.error("将对象序列化为JSON字符串失败: {},{}", object, ExceptionUtils.getStackTrace(e));
            throw new CommonException(CodesEnum.JSON_EX);
        }
    }

    /**
     * 将任何 Java 对象序列化为 JSON 字符串，并忽略指定的属性。
     *
     * @param object             要序列化的对象。
     * @param propertiesToIgnore 要在序列化中忽略的属性名称。
     * @return 对象的 JSON 字符串表示。
     * @throws RuntimeException 如果序列化过程中发生错误。
     */
    public static String toJsonString(Object object, String... propertiesToIgnore) {
        if (object == null) {
            return null;
        }
        if (propertiesToIgnore == null || propertiesToIgnore.length == 0) {
            return toJsonString(object);
        }
        try {
            ObjectMapper mapperWithMixIn = MIXIN_MAPPERS_CACHE.computeIfAbsent(object.getClass(), clazz -> {
                ObjectMapper newMapper = OBJECT_MAPPER.copy();
                newMapper.addMixIn(clazz, DynamicFilterMixIn.class);
                return newMapper;
            });

            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter("dynamicFilter", SimpleBeanPropertyFilter.serializeAllExcept(propertiesToIgnore));

            return mapperWithMixIn.writer(filters).writeValueAsString(object);
        } catch (Exception e) {
            log.error("将对象序列化为JSON字符串（忽略属性）失败: {},{}", object, ExceptionUtils.getStackTrace(e));
            throw new CommonException(CodesEnum.JSON_EX);
        }
    }

    // 用于动态添加 @JsonFilter 注解的 MixIn 接口
    @JsonFilter("dynamicFilter")
    private interface DynamicFilterMixIn {
    }

    /**
     * 将 JSON 字符串反序列化为指定类的对象。
     *
     * @param text  要反序列化的 JSON 字符串。
     * @param clazz 要反序列化成的对象的类。
     * @param <T>   对象的类型。
     * @return 指定类的对象，如果输入字符串为空则返回 null。
     * @throws RuntimeException 如果反序列化过程中发生错误。
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text) || clazz == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (Exception e) {
            log.error("将JSON字符串反序列化为对象失败: {},{}", text, ExceptionUtils.getStackTrace(e));
            throw new CommonException(CodesEnum.JSON_EX);
        }
    }

    /**
     * 将 JSON 字符串反序列化为复杂泛型类型的对象。
     *
     * @param text          要反序列化的 JSON 字符串。
     * @param typeReference 指定复杂泛型类型的 TypeReference。
     * @param <T>           对象的类型。
     * @return 指定类型的对象，如果输入字符串为空则返回 null。
     * @throws RuntimeException 如果反序列化过程中发生错误。
     */
    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(text) || typeReference == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, typeReference);
        } catch (Exception e) {
            log.error("使用TypeReference将JSON字符串反序列化为对象失败: {},{}", text, ExceptionUtils.getStackTrace(e));
            throw new CommonException(CodesEnum.JSON_EX);
        }
    }
}
