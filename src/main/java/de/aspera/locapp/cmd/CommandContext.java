package de.aspera.locapp.cmd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CommandContext {

    private static Map<String, Class<? extends CommandRunnable>> commandMap = new HashMap<>();
    private static Queue<String> argumentStack = new LinkedList<>();
    private static CommandContext instance;
    

    public static synchronized CommandContext getInstance() {
        if (instance == null) {
            instance = new CommandContext();
            instance.loadCommands();
        }
        return instance;
    }

    private CommandContext() {
    }

    public void executeCommand(String command) throws CommandException, InstantiationException, IllegalAccessException {
        ((CommandRunnable) commandMap.get(command).newInstance()).run();
        clearArguments();
    }

    public void addCommand(String key, Class<? extends CommandRunnable> clazz) {
        commandMap.put(key, clazz);
    }

    public void removeCommand(String key) {
        commandMap.remove(key);
    }

    public boolean isCommand(String key) {
        return commandMap.containsKey(key);
    }

    public void addArgument(String arg) {
        argumentStack.add(arg);
    }

    public String nextArgument() {
        return argumentStack.poll();
    }

    public String[] allArguments() {
        return argumentStack.toArray(new String[0]);
    }

    public int sizeOfArguments() {
        return argumentStack.size();
    }

    public void clearArguments() {
        argumentStack.clear();
    }

    /**
     * Register commands on the CommandContext.
     */
    public void loadCommands() {
        addCommand("quit", QuitCommand.class);
        addCommand("q", QuitCommand.class);
        addCommand("h", HelpCommand.class);
        addCommand("help", HelpCommand.class);
        addCommand("clear-loc", ClearLocalizationCommand.class);
        addCommand("cl", ClearLocalizationCommand.class);
        addCommand("import-properties", ImportPropertiesCommand.class);
        addCommand("ip", ImportPropertiesCommand.class);
        addCommand("f", FilesCommand.class);
        addCommand("files", FilesCommand.class);
        addCommand("ee", ExcelExportCommand.class);
        addCommand("excel-export", ExcelExportCommand.class);
        addCommand("ei", ExcelImportCommand.class);
        addCommand("excel-import", ExcelImportCommand.class);
        addCommand("ep", ExportPropertiesCommand.class);
        addCommand("export-properties", ExportPropertiesCommand.class);
        addCommand("init", ConfigInitCommand.class);
        addCommand("i", ConfigInitCommand.class);
        addCommand("ed", ExportDeltaCommand.class);
        addCommand("export-delta", ExportDeltaCommand.class);
        addCommand("id", ImportDeltaCommand.class);
        addCommand("import-delta", ImportDeltaCommand.class);
        addCommand("pc", PropertiesCounterCommand.class);
        addCommand("properties-count", PropertiesCounterCommand.class);
        addCommand("mp", MergeCommand.class);
        addCommand("merge-properties", MergeCommand.class);
        addCommand("ci", CheckIntegrityCommand.class);
        addCommand("check-integrity", CheckIntegrityCommand.class);
        addCommand("iil", ImportIgnoredItemsCommand.class);
        addCommand("import-ignore-list", ImportIgnoredItemsCommand.class);
        addCommand("cil", ClearIgnoreListCommand.class);
        addCommand("clear-ignore-list", ClearIgnoreListCommand.class);
    }
}
