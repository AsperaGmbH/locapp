package de.aspera.locapp.dao;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.aspera.locapp.dto.IgnoredItem;

public class IgnoredItemFacade extends AbstractFacade<IgnoredItem> {
    private static final Logger logger = Logger.getLogger(IgnoredItemFacade.class.getName());

    public IgnoredItemFacade() {
        super(IgnoredItem.class);
    }

    public Set<String> listIgnoredFileNames() {
        return findAll()
            .stream()
            .map(item -> item.getFileName())
            .collect(Collectors.toSet());
    }
}
