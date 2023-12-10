package classes;

import java.util.ArrayList;

public class Course {
    private final ArrayList<Theme> themes;
    private final String title;
    private final String id;

    public Course(ArrayList<Theme> themes, String title, String id) {
        this.themes = themes;
        this.title = title;
        this.id = id;
    }
}
