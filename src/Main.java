import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws IOException, SQLException, CsvValidationException {
        var dbFilename = "basicprogrammingDB.db";
        var csvFilename = "basicprogramming.csv";

        var db = new SqliteConnection(dbFilename);
        db.connect();
        var parser = new CsvParser(csvFilename);

        var practicesScores = parser.parsePracticesScores();
        db.addPracticesScores(practicesScores);

        var exercisesScores = parser.parseExercisesScores();
        db.addExercisesScores(exercisesScores);

        var students = parser.parseStudents();
        db.addStudentsToDataBase(students);
    }
}