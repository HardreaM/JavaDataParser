import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.Charset;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

public class CsvParser {
    private String fileName;

    public CsvParser(String fileName) {
        setFileName(fileName);
    }

    public void setFileName(String fileName) {
        var file = new File(fileName);
        if (file.exists()) {
            this.fileName = fileName;
            return;
        }
        throw new IllegalArgumentException();
    }

    public ArrayList<Student> parseStudents() throws IOException, CsvValidationException {
        var students = new ArrayList<Student>();
        var reader = getReader(3);
        var headers = getHeaders();
        var fullNameIndex = ArrayUtils.indexOf(headers, "Фамилия Имя");
        var ulearnIdIndex = ArrayUtils.indexOf(headers, "Ulearn id");
        var emailIndex = ArrayUtils.indexOf(headers, "Эл. почта");
        var groupIndex = ArrayUtils.indexOf(headers, "Группа");

        String[] row = null;
        while ((row = reader.readNext()) != null) {
            var name = row[fullNameIndex];
            var id = row[ulearnIdIndex];
            var email = row[emailIndex];
            var group = row[groupIndex];
            students.add(new Student(name, id, email, group));
        }
        return students;
    }

    public ArrayList<String> parseThemes() throws IOException, CsvValidationException {
        var row = getReader().readNext();
        var result = new ArrayList<String>();
        for (var s : row) {
            if (!s.isEmpty() &&
                    !s.equals("За весь курс") &&
                    !s.equals("Преподавателю о курсе"))
                result.add(s);
        }
        return result;
    }

    public ArrayList<String> parseExercises() throws IOException, CsvValidationException {
        var row = getReader(1).readNext();
        var excercises = new ArrayList<String>();
        for (var s: row) {
            if (s.startsWith("Упр: ")) {
                excercises.add(s.substring(5));
            }
        }
        return excercises;
    }

    public ArrayList<String> parsePractices() throws IOException, CsvValidationException {
        var row = getReader(1).readNext();
        var practices = new ArrayList<String>();
        for (var s: row) {
            if (s.startsWith("ДЗ: ")) {
                practices.add(s.substring(4));
            }
        }
        return practices;

    }

    public HashMap<String, HashMap<String, Integer>> parseExercisesScores() throws CsvValidationException, IOException {
        var colIndexes = getIndexes(parseExercises(), getReader(1).readNext(), "Упр: ");
        return getUidScores(colIndexes);
    }

    public HashMap<String, HashMap<String, Integer>> parsePracticesScores() throws CsvValidationException, IOException {
        var colIndexes = getIndexes(parsePractices(), getReader(1).readNext(), "ДЗ: ");
        return getUidScores(colIndexes);
    }

    private String[] getHeaders() throws IOException, CsvValidationException {
        String[] row = null;
        var reader = getReader(1);
        return reader.readNext();
    }

    private CSVReader getReader() throws IOException {
        var parser = new CSVParserBuilder().withSeparator(';').build();
        var fileReader = new FileReader(fileName, Charset.forName("windows-1251"));
        return new CSVReaderBuilder(fileReader).withCSVParser(parser).build();
    }

    private CSVReader getReader(int skip) throws IOException {
        var parser = new CSVParserBuilder().withSeparator(';').build();
        var fileReader = new FileReader(fileName, Charset.forName("windows-1251"));
        return new CSVReaderBuilder(fileReader).withCSVParser(parser).withSkipLines(skip).build();
    }

    private HashMap<String, Integer> getIndexes(ArrayList<String> values, String[] array, String headerPrefix) {
        var indexes = new HashMap<String, Integer>();
        for (var e: values) {
            indexes.put(e, ArrayUtils.indexOf(array, headerPrefix + e));
        }
        return indexes;
    }

    private HashMap<String, HashMap<String, Integer>> getUidScores(HashMap<String, Integer> colIndexes) throws IOException, CsvValidationException {
        var result = new HashMap<String, HashMap<String, Integer>>();
        var reader = getReader(1);
        var headerRow = reader.readNext();
        reader.readNext();
        for (var key: colIndexes.keySet()) {
            result.put(key, new HashMap<String, Integer>());
        }
        var uidIndex = ArrayUtils.indexOf(headerRow, "Ulearn id");
        String[] row;
        while ((row = reader.readNext()) != null) {
            for (var header: colIndexes.keySet()) {
                var index = colIndexes.get(header);
                result.get(header).put(row[uidIndex], Integer.valueOf(row[index]));
            }
        }
        return result;
    }
}
