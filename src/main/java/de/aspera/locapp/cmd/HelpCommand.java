package de.aspera.locapp.cmd;

public class HelpCommand implements CommandRunnable {

    @Override
    public void run() {
        System.out.println("List of commands: \n");
        System.out.println("\t(q)uit: \t\t\t\tQuit the program.");
        System.out.println("\t(ip)import-properties: \t\t\tIterate known properties and save into database.");
        System.out.println("\t(ep)export-properties DIR: \t\tIterate known properties and save into directory.");
        System.out.println("\t(ee)excel-export DIR [L] [E]: \t\tExport properties into an excel file (all or by language ISOCODE[L], search for empty values [E=1])");
        System.out.println("\t(ei)excel-import DIR: \t\t\tImport properties from an excel file");
        System.out.println("\t(ed)export-delta DIR: \t\t\tExport delta (properties vs. excel) into an excel file");
        System.out.println("\t(id)import-delta DIR VERSION: \t\tImport delta and merge with selected version");
        System.out.println("\t(pc)properties-count SRC|XLS [L] [E]: \tCount the amount of properties (all or by language ISOCODE[L], search for empty values [E=1])");
        System.out.println("\t(mp)merge-properties SRC|XLS: \t\tAll known properties will be merged with their latest version to a new data set.");
        System.out.println("\t(ci)check-integrity [L]: \t\tCheck if all SRC properties provided by XLS properties with all or specified languages(all or by language ISOCODE[L]).");
        System.out.println("\t(cl)ear-loc: \t\t\t\tDelete all(!) entries for Localization!");
        System.out.println("\t(f)iles DIR: \t\t\t\tRead recursive down for properties files and save fileinfo.");
        System.out.println("\tcommand options mandatory: \t\tCommand parameters without brackets are mandatory");
        System.out.println("\tcommand options optional: \t\tCommand parameters inside brackets are optional\n");
        System.out.println("\t(h)elp: \t\t\t\tPrint this!\n\n");
    }
}
