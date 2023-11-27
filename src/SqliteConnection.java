import org.json.simple.JSONArray;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;

public class SqliteConnection {
    private Connection connection;
    private Statement statement;
    private final String filename;

    private boolean isConnected;

    public SqliteConnection(String filename) {
        this.filename = filename;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
            statement = connection.createStatement();
            isConnected = true;
            return true;
        } catch (SQLException e) {
            return false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void createTablesIfNotExists(String[] exercisesID, String[] practicesID) throws SQLException {
        createTableStudents();
        createTableThemes();
        createTableExercises();
        createTablePractices();
        createTableExercisesScores(exercisesID);
        createTablePracticesScores(practicesID);
        createTableThemesPracticesAndExercises();
    }

    private void createTableStudents() throws SQLException {
        var query = "CREATE TABLE IF NOT EXISTS 'Students' ('UlearnID' VARCHAR PRIMARY KEY, 'fullname' TEXT, 'email' TEXT, 'studentGroup' TEXT);";
        statement.execute(query);
    }

    private void createTableThemes() throws SQLException {
        var query = "CREATE TABLE IF NOT EXISTS 'Themes' ('id' VARCHAR PRIMARY KEY, 'title' TEXT);";
        statement.execute(query);
    }

    private void createTableExercises() throws SQLException {
        var query = "CREATE TABLE IF NOT EXISTS 'Exercises' ('id' VARCHAR PRIMARY KEY, 'title' TEXT);";
        statement.execute(query);
    }

    private void createTablePractices() throws SQLException {
        var query = "CREATE TABLE IF NOT EXISTS 'Practices' ('id' VARCHAR PRIMARY KEY, 'title' TEXT);";
        statement.execute(query);
    }

    private void createTableExercisesScores(String[] exercises) throws SQLException {
        createTableWithSameTypeColumnsAndUID("ExercisesScores", exercises, "SMALLINT");
    }

    private void createTablePracticesScores(String[] practices) throws SQLException {
        createTableWithSameTypeColumnsAndUID("PracticesScores", practices, "SMALLINT");
    }

    private void createTableWithSameTypeColumnsAndUID(String tableName, String[] columnsNames, String type) throws SQLException {
        var builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE IF NOT EXISTS '%s' ('UlearnID' VARCHAR PRIMARY KEY", tableName));
        for (var id: columnsNames) {
            builder.append(String.format(", '%s' %s", id, type));
        }
        builder.append(");");
        statement.execute(builder.toString());
    }

    private void createTableThemesPracticesAndExercises() throws SQLException {
        var q = "CREATE TABLE IF NOT EXISTS 'ThemesPracticesAndExercises' ('id' VARCHAR PRIMARY KEY, 'practices' TEXT, 'exercises' TEXT);";
        statement.execute(q);
    }

    public void addStudentsToDataBase(ArrayList<Student> students) throws SQLException {
        var count = 0;
        for (var s: students) {
            var query = String.format(
                    "REPLACE INTO Students (UlearnID, fullname, email, studentGroup) VALUES ('%s', '%s', '%s', '%s')",
                    s.ulearnID.isEmpty() ? "None" : s.ulearnID,
                    s.fullName.isEmpty() ? "None" : s.fullName,
                    s.email.isEmpty() ? "None" : s.email,
                    s.group.isEmpty() ? "None" : s.group);
            statement.addBatch(query);
            count++;
            if (count > 500) {
                statement.executeBatch();
                count = 0;
            }
        }
        statement.executeBatch();
    }

    public void addIdThemes(HashMap<String, String> idTheme) throws SQLException {
        addIdTitle(idTheme, "Themes");
    }

    public void addIdExercises(HashMap<String, String> idTheme) throws SQLException {
        addIdTitle(idTheme, "Exercises");
    }

    public void addIdPractices(HashMap<String, String> idTheme) throws SQLException {
        addIdTitle(idTheme, "Practices");
    }

    private void addIdTitle(HashMap<String, String > idTitle, String tableName) throws SQLException {
        var count = 0;
        for (var id: idTitle.keySet()) {
            var title = idTitle.get(id);
            var query = String.format(
                    "REPLACE INTO %s (id, title) VALUES ('%s', '%s')",
                    tableName,
                    id,
                    title);
            statement.addBatch(query);
            count++;
            if (count > 500) {
                statement.executeBatch();
                count = 0;
            }
        }
        statement.executeBatch();
    }

    public void addPracticesScores(HashMap<String, HashMap<String, Integer>> allScores) throws SQLException {
        addUidScores(allScores, "PracticesScores", getPracticesIds());
    }

    public void addExercisesScores(HashMap<String, HashMap<String, Integer>> allScores) throws SQLException {
        addUidScores(allScores, "ExercisesScores", getExercisesIds());
    }

    private void addUidScores(HashMap<String, HashMap<String, Integer>> allScores, String tableName, HashMap<String, String> coulumnsFromScoresMap) throws SQLException {
        var normalized = normalizeScores(allScores);
        var paramsBuilder = new StringBuilder();
        var valuesBuilder = new StringBuilder();
        var count = 0;
        for (var studentID: normalized.keySet()) {
            paramsBuilder.setLength(0);
            valuesBuilder.setLength(0);
            var scores = normalized.get(studentID);
            for (var practiceTitle: scores.keySet()) {
                var column = coulumnsFromScoresMap.get(practiceTitle);
                var score = scores.get(practiceTitle);
                paramsBuilder.append(String.format("'%s', ", column));
                valuesBuilder.append(String.format("'%d', ", score));
            }
            var query = String.format("REPLACE INTO %s ('UlearnID', %s) VALUES ('%s', %s);",
                    tableName,
                    paramsBuilder.substring(0, paramsBuilder.length() - 2),
                    studentID,
                    valuesBuilder.substring(0, valuesBuilder.length() - 2));
            if (studentID.isEmpty()) {
                System.out.println("1");
            }
            statement.addBatch(query);
            count++;

            if (count > 500) {
                statement.executeBatch();
                count = 0;
            }
        }
        statement.executeBatch();
    }

    private HashMap<String, HashMap<String, Integer>> normalizeScores(HashMap<String, HashMap<String, Integer>> allScores) {
        var result = new HashMap<String, HashMap<String, Integer>>();
        for (var practice: allScores.keySet()) {
            var scores = allScores.get(practice);
            for (var studentID: scores.keySet()) {
                var score = scores.get(studentID);
                if (!result.containsKey(studentID)) {
                    var map = new HashMap<String, Integer>();
                    map.put(practice, score);
                    result.put(studentID, map);
                } else {
                    result.get(studentID).put(practice, score);
                }
            }
        }
        return result;
    }

    private HashMap<String, String> getPracticesIds() throws SQLException {
        return getTitlesIds("Practices");
    }

    private HashMap<String, String> getExercisesIds() throws SQLException {
        return getTitlesIds("Exercises");
    }

    private HashMap<String, String> getThemesIds() throws SQLException {
        return getTitlesIds("Themes");
    }

    private HashMap<String, String> getTitlesIds(String tableName) throws SQLException {
        var q = String.format("SELECT id, title FROM %s;", tableName);
        var queryRes = statement.executeQuery(q);
        var result = new HashMap<String, String>();
        while (queryRes.next()){
            var id = queryRes.getString("id");
            var title = queryRes.getString("title");
            result.put(title, id);
        }
        return result;
    }

    public void addThemesPracticesAndExercises(HashMap<String, ArrayList<String>> themesExercises,
                                               HashMap<String, ArrayList<String>> themesPractices) throws SQLException {
        for (var themeID: themesExercises.keySet()) {
            var exercises = themesExercises.get(themeID);
            var practices = themesPractices.get(themeID);
            var jsonArrExercises = new JSONArray();
            jsonArrExercises.addAll(exercises);
            var jsonArrPractices = new JSONArray();
            jsonArrPractices.addAll(practices);
            var q = String.format("REPLACE INTO ThemesPracticesAndExercises (id, exercises, practices) VALUES ('%s', '%s', '%s')",
                    themeID,
                    jsonArrExercises.toJSONString(),
                    jsonArrPractices.toJSONString());
            statement.execute(q);
        }
    }
}
