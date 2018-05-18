# LocApp

The application LocApp is a command line interface (CLI) application. LocApp recursively collects
all available * .properties files from a root directory and imports the path and value information into an h2 database.

###  List of commands:

        (q)uit:                                 Quit the program.
        (ip)import-properties:                  Iterate known properties and save into database.
        (ep)export-properties DIR:              Iterate known properties and save into directory.
        (ee)excel-export DIR [L] [E]:           Export properties into an excel file (all or by language ISOCODE[L], search for empty values [E=1])
        (ei)excel-import DIR:                   Import properties from an excel file
        (ed)export-delta DIR:                   Export delta (properties vs. excel) into an excel file
        (id)import-delta DIR VERSION:           Import delta and merge with selected version
        (pc)properties-count SRC|XLS [L] [E]:   Count the amount of properties (all or by language ISOCODE[L], search for empty values [E=1])
        (mp)merge-properties SRC|XLS:           All known properties will be merged with their latest version to a new data set.
        (ci)check-integrity [L]:                Check if all SRC properties provided by XLS properties with all or specified languages(all or by language ISOCODE[L]).
        (cl)ear-loc:                            Delete all(!) entries for Localization!
        (f)iles DIR:                            Read recursive down for properties files and save fileinfo.
        command options mandatory:              Command parameters without brackets are mandatory
        command options optional:               Command parameters inside brackets are optional
        (h)elp:                                 Print this!


### Requirements:

- Java 8.x
- Apache Maven 3.0.5 or higher
- All properties files should be formatted as utf-8 without BOM (byte order mark)

###  Install and run:

- Clone the project
- Build the project with maven
- Start application with: java -jar target/locapp-x.x.x-XYZ-jar-with-dependencies.jar
