package classes;

import classes.Exercise;
import classes.Practice;

import java.util.ArrayList;

public class Theme extends CourseElement{
    private final ArrayList<Practice> practices = new ArrayList<Practice>();
    private final ArrayList<Exercise> exercises = new ArrayList<Exercise>();

    public Theme(String title, String id) {
        super(title, id);
    }

    public Theme(String title, String id, ArrayList<Practice> practices, ArrayList<Exercise> exercises) {
        super(title, id);
        addExercises(exercises);
        addPractices(practices);
    }
        public ArrayList<Practice> getPractices() {
        return practices;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void addPractices(ArrayList<Practice> newPractices) {
        practices.addAll(newPractices);
    }

    public void addExercises(ArrayList<Exercise> newExercises) {
        exercises.addAll(newExercises);
    }
}
