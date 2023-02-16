package com.mindolph.fx.preference;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import javafx.geometry.Rectangle2D;
import org.swiftboot.util.pref.StringConverter;

/**
 * @author mindolph.com@gmail.com
 */
public class Rectangle2DStringConverter extends StringConverter<Rectangle2D> {

    @Override
    public Rectangle2D deserialize(String prefValue) {
        JsonObject jo = JsonParser.parseString(prefValue).getAsJsonObject();
//        log.info("Deserialize saved json value %s for rectangle.".formatted(jo));
        return new Rectangle2D(jo.get("minX").getAsDouble(), jo.get("minY").getAsDouble(),
                jo.get("width").getAsDouble(), jo.get("height").getAsDouble());
    }

    @Override
    public String serialize(Rectangle2D winRect) {
        JsonObject jo = new JsonObject();
        jo.add("minX", new JsonPrimitive(winRect.getMinX()));
        jo.add("minY", new JsonPrimitive(winRect.getMinY()));
        jo.add("width", new JsonPrimitive(winRect.getWidth()));
        jo.add("height", new JsonPrimitive(winRect.getHeight()));
        return jo.toString();
    }
}
