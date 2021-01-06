package de.aspera.locapp.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(
    name = "IGNORED_ITEM",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ID", "FILE_NAME"})
)
public class IgnoredItem implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private String fileName;
    
    @Column(name = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "FILE_NAME")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return "".hashCode();
        }

        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof IgnoredItem)) {
            return false;
        }

        if (id == null) {
            return false;
        }

        return id.equals(((IgnoredItem)obj).id);
    }
}
