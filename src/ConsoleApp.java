import com.opencsv.exceptions.CsvValidationException;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Scanner;

public class ConsoleApp {
    private String filename;
    private String dbFilename;
    private boolean isAppRunning;
    private final Scanner sc;
    private CsvParser parser;
    private SqliteConnection db;

    private String[] commands = new String[] {"HELP", "EXIT", "SUMMARY"};

    public ConsoleApp(Scanner sc, String dbFilename) {
        this.sc = sc;
        parser = new CsvParser(filename);
        db = new SqliteConnection(dbFilename);
        db.connect();
    }

    public void Run() throws URISyntaxException, IOException, ParseException, InterruptedException, CsvValidationException, SQLException {
        isAppRunning = true;
        System.out.println("Введите имя файла .csv");
        //filename = sc.nextLine();
        filename = "basicprogramming.csv";
        onStartUp();
        System.out.println("Доступные команды:");
        System.out.println(String.join(", ", commands));
        System.out.println();
        while (isAppRunning) {
            var command = sc.nextLine();
            switch (command.toUpperCase()) {
                case "EXIT":
                    isAppRunning = false;
                    break;

                case "HELP":
                    System.out.println(String.join(", ", commands));
                    break;

                case "SUMMARY":
                    System.out.println(getSummary());
                    break;
            }
            System.out.println();
        }
    }

    private String getSummary() throws SQLException, ParseException, InterruptedException {
        var studentsCount = db.getStudents().size();
        var themesCount = db.getThemes().size();
        var practicesCount = db.getPractices().size();
        var exercisesCount = db.getExercises().size();
        return String.format(
                "Студентов: %d\n" +
                        "Тем: %d\n" +
                        "Практик: %d\n" +
                        "Упражнений: %d\n",
                studentsCount,
                themesCount,
                practicesCount,
                exercisesCount);
    }


    private void onStartUp() throws CsvValidationException, IOException, SQLException,
            URISyntaxException, ParseException, InterruptedException {
        var students = parser.parseStudents();
        var exercisesScores = parser.parseUsersExercisesScores(exercises);
        var practicesScores = parser.parseUsersPracticesScores(practices);
        System.out.println("Парсинг успешен");
        System.out.println();

        System.out.println("Запись в базу данных...");
        db.createTablesIfNotExists(exercises, practices);
        db.addStudentsToDataBase(students);
        db.addPractices(practices);
        db.addExercises(exercises);
        db.addThemes(themes);
        db.addExercisesScores(exercisesScores);
        db.addPracticesScores(practicesScores);
        System.out.println("База данных сформирована успешно");
        System.out.println();
    }
}
