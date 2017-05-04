package cn.leeffee.feige.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by lhfei on 2017/3/15.
 */

public class GsonUtil {
    private GsonUtil() {
    }

    public static final Gson gson = new GsonBuilder()
            // .registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
           // .registerTypeAdapter(Double.class, new DoubleAdapter())
            .create();

    static class DoubleAdapter implements JsonSerializer<Double> {
        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == src.longValue())
                return new JsonPrimitive(src.longValue());
            return new JsonPrimitive(src);
        }
    }

    /**
     * Gson TypeAdapter
     * 实现了 Timestamp 类的 json 化
     *
     * @author lvhf
     */
    static class TimestampAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {

        @Override
        public Timestamp deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            if (json == null) {
                return null;
            } else {
                try {
                    return new Timestamp(json.getAsLong());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        @Override
        public JsonElement serialize(Timestamp src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            String value = "";
            if (src != null) {
                value = String.valueOf(src.getTime());
            }
            return new JsonPrimitive(value);
        }
    }

    /**
     * Date和字符串日期毫秒数适配
     * 将日期类型序列化和反序列化长整数字符串(串中以长整数毫秒数表示日期)
     */
    static class LongDateTypeAdapter extends TypeAdapter<Date> {
        @Override
        public void write(JsonWriter out, Date value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.getTime());
            }
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            if (in.peek() == null) {
                return null;
            }
            String str = in.nextString();
            Date d = new Date(Long.parseLong(str));
            return d;
        }
    }
}
