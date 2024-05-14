import java.util.HashSet;
import java.util.Objects;

public class Site {
    private int i;
    private int j;

    // initialize board from file
    public Site(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public int i() { return i; }
    public int j() { return j; }

    // Manhattan distance between invoking Site and w
    public int manhattanTo(Site w) {
        Site v = this;
        int i1 = v.i();
        int j1 = v.j();
        int i2 = w.i();
        int j2 = w.j();
        return Math.abs(i1 - i2) + Math.abs(j1 - j2);
    }

    // does invoking site equal site w?
    public boolean equals(Site w) {
        return (manhattanTo(w) == 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Site site = (Site) obj;
        return i == site.i && j == site.j;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, j);
    }

    @Override
    public String toString() {
        return "i: " + i + ", j: " + j + "\n";
    }

    public static void main(String[] args) {
        Site a = new Site(1,1);
        Site b = new Site(1,1);
        HashSet<Site> set = new HashSet<>();
        set.add(a);
        System.out.println(set.contains(b));
    }
}

