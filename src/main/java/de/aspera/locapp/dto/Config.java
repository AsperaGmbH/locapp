package de.aspera.locapp.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Bjoern.Buchholz
 *
 */
@Entity
@Table(name = "CONFIG")
// @Cacheable(false)
public class Config implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -494661175045403646L;
    @Id
    @GeneratedValue
    private String key;
    private String value;

    @Column(name = "KEY")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "VALUE")
    public String getValue() {
        return value;
    }

    public void setValue(String[] values) {
        String value = String.join(",", values);
        this.value = value;
    }

}
