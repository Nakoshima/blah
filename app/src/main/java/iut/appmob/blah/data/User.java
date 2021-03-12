package iut.appmob.blah.data;

import java.io.Serializable;

public class User implements Serializable {
    private String email;

    public User() {
    }

    public User(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String toString() {
        return this.email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        User user = (User) o;

        return user.getEmail().equals(this.email);
    }
}
