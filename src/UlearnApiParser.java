import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class UlearnApiParser {
    private final String token;
    private JSONObject json;
    private Date lastJsonUpdate = new Date(1);
    private final long timeToUpdate = 5 * 60 * 1000;
    private final URI apiUrI = new URI("https://api.ulearn.me/courses/basicprogramming");
    public UlearnApiParser(String authToken) throws URISyntaxException, IOException, ParseException, InterruptedException {
        token = authToken;
        json = getResponseJSON();
    }

    /**
     * Метод возвращает словарь из тем и списка их практик
     * @return HashMap(themeID, ArrayList(practiceID))
     */
    public HashMap<String, ArrayList<String>> getThemesPracticesIDs() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var ids = getIdThemes();
        var result = new HashMap<String, ArrayList<String>>();
        for (var id: ids.keySet()) {
            result.put(id, getThemePracticesID(id));
        }
        return result;
    }

    /**
     * Метод возвращает словарь из тем и списка их упражнений
     * @return HashMap(themeID, ArrayList(exerciseID))
     */
    public HashMap<String, ArrayList<String>> getThemesExercisesIDs() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var ids = getIdThemes();
        var result = new HashMap<String, ArrayList<String>>();
        for (var id: ids.keySet()) {
            result.put(id, getThemeExercisesID(id));
        }
        return result;
    }

    /**
     * @return ArrayList(exerciseID)
     */
    public ArrayList<String> getThemeExercisesID(String themeID) throws URISyntaxException, IOException, ParseException, InterruptedException {
        var unit = getThemeUnit(themeID);
        var slides = getUnitExercisesSlides(unit);
        var exercises = new ArrayList<String>();
        for (var slideObj: slides) {
            var slide = (JSONObject) slideObj;
            exercises.add((String) slide.get("id"));
        }
        return exercises;
    }

    /**
     * @return ArrayList(practiceId)
     */
    public ArrayList<String> getThemePracticesID(String themeID) throws URISyntaxException, IOException, ParseException, InterruptedException {
        var unit = getThemeUnit(themeID);
        var slides = getUnitPracticesSlides(unit);
        var practices = new ArrayList<String>();
        for (var slideObj: slides) {
            var slide = (JSONObject) slideObj;
            practices.add((String) slide.get("id"));
        }
        return practices;
    }

    /**
     * Метод возвращает словарь id темы - название
     * @return HashMap(themeID, themeTitle)
     */
    public HashMap<String, String> getIdThemes() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var themes = new HashMap<String, String>();
        var units = getUnits();
        for (Object unitObj: units) {
            var unit = (JSONObject) unitObj;
            var title = (String) unit.get("title");
            var id = (String) unit.get("id");
            themes.put(id, title);
        }
        return themes;
    }

    /**
     * Метод возвращает словарь id упражнения - название
     * @return HashMap(exerciseID, exerciseTitle)
     */
    public HashMap<String, String> getIdExercises() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var slides = getAllExerciseSlides();
        var result = new HashMap<String, String>();
        for (var slideObj: slides) {
            var slide = (JSONObject) slideObj;
            result.put((String) slide.get("id"), (String) slide.get("title"));
        }

        return result;
    }

    /**
     * Метод возвращает словарь id практики - название
     * @return HashMap(practiceID, practiceTitle)
     */
    public HashMap<String, String> getIdPractices() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var slides = getAllPracticesSlides();
        var result = new HashMap<String, String>();
        for (var slideObj: slides) {
            var slide = (JSONObject) slideObj;
            result.put((String) slide.get("id"), (String) slide.get("title"));
        }

        return result;
    }

    private JSONObject getThemeUnit(String themeID) throws URISyntaxException, IOException, ParseException, InterruptedException {
        for (var unitObj: getUnits()) {
            var unit = (JSONObject) unitObj;
            if (((String)unit.get("id")).equals(themeID))
                return unit;
        }
        throw new IllegalArgumentException();
    }

    private JSONArray getUnitSlides(JSONObject unit) {
        return (JSONArray) unit.get("slides");
    }

    private JSONArray getUnitPracticesSlides(JSONObject unit) {
        var slides = getUnitSlides(unit);
        var result = new JSONArray();
        for (var slideObj: slides) {
            var slide = (JSONObject) slideObj;
            if (isSlideIsPractice(slide))
                result.add(slide);
        }
        return result;
    }

    private JSONArray getUnitExercisesSlides(JSONObject unit) {
        var slides = getUnitSlides(unit);
        var result = new JSONArray();
        for (var slideObj: slides) {
            var slide = (JSONObject) slideObj;
            if (isSlideIsExercise(slide))
                result.add(slide);
        }
        return result;
    }

    private JSONArray getUnits() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var json = getResponseJSON();
        return (JSONArray)json.get("units");
    }

    private JSONArray getAllSlides() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var units = getUnits();
        var result = new JSONArray();
        for (var unitObj: units) {
            var unit = (JSONObject) unitObj;
            var unitSlides = (JSONArray) getUnitSlides(unit);
            for (var slideObj: unitSlides) {
                var slide = (JSONObject) slideObj;
                result.add(slide);
            }
        }
        return result;
    }

    private JSONArray getAllExerciseSlides() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var allSlides = getAllSlides();
        var result = new JSONArray();
        for (var slideObj: allSlides) {
            var slide = (JSONObject) slideObj;
            if (isSlideIsExercise(slide))
                result.add(slide);
        }
        return result;
    }

    private JSONArray getAllPracticesSlides() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var allSlides = getAllSlides();
        var result = new JSONArray();
        for (var slideObj: allSlides) {
            var slide = (JSONObject) slideObj;
            if (isSlideIsPractice(slide))
                result.add(slide);
        }
        return result;
    }

    private boolean isSlideIsExercise(JSONObject slide) {
        return ((String)slide.get("scoringGroup")).equals("exercise") ;
    }

    private boolean isSlideIsPractice(JSONObject slide) {
        return ((String)slide.get("scoringGroup")).equals("homework");
    }

    private JSONObject getResponseJSON() throws URISyntaxException, IOException, InterruptedException, ParseException {
        if ((new Date()).getTime() - lastJsonUpdate.getTime() < timeToUpdate) {
            return json;
        }
        lastJsonUpdate = new Date();
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(apiUrI)
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        json = (JSONObject) new JSONParser().parse(response.body());
        return json;
    }
}
