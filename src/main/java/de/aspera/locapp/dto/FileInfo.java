package de.aspera.locapp.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FILEINFO")
// @Cacheable(false)
public class FileInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7172287189095482247L;
    @Id
    @GeneratedValue
    private Integer id;
    private String fileName;
    private String relativePath;
    private String searchPath;
    private String fullPath;

    @Column(name = "FILENAME")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "FULL_PATH")
    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    @Column(name = "RELATIVE_PATH")
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    @Column(name = "SEARCH_PATH")
    public String getSearchPath() {
        return searchPath;
    }

    public void setSearchPath(String searchPath) {
        this.searchPath = searchPath;
    }

}
