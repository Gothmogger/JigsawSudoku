package sk.tuke.gamestudio;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

public class ToJsonUtil {
    @Autowired
    ObjectMapper objectMapper;

    private JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
    public String toJson(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JSONArray toJsonArray(final Object obj) {
        try {
            JSONArray JsonArray = (JSONArray) jsonParser.parse(toJson(obj));
            return JsonArray;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
