package de.aspera.locapp.main;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.h2.tools.Server;

import de.aspera.locapp.cmd.CommandContext;
import de.aspera.locapp.dao.H2DatabaseManager;
import de.aspera.locapp.util.Resources;

/**
 * The main class to start the application.
 *
 * @author Daniel.Weiss
 *
 */
public class MainStart {
    private static final String BLANK = " ";
    private static final Scanner scanner = new Scanner(System.in);
    private static Server H2Server;
    private static final Logger logger = Logger.getLogger(MainStart.class.getName());

    public static void main(String[] args) throws ParseException {
        init();
        // After start -> hold the command cli in recursiv mode.
        promptCLI();
    }

    private static void init() {
        try {
            splash();

            CommandContext.getInstance().executeCommand("h");
            Resources.getInstance();
            loadDatabase();
            // Start the program with init parameters (e.g. blacklist for import
            // filenames)
            CommandContext.getInstance().executeCommand("init");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(0);
        }
    }

    /**
     * This method presents the command input (CLI) of the application. The
     * known command can be execute with the command keyword or shortcut. The
     * CLI works in a recursive mode and will stay until the program will quit
     * or broken.
     */
    private static void promptCLI() {
        System.out.print("\n>> command: ");
        String cmdline = scanner.nextLine().trim();
        if (cmdline.contains(BLANK)) {
            String args[] = cmdline.split(BLANK);
            for (int i = 0; i < args.length; i++) {
                CommandContext.getInstance().addArgument(args[i]);
            }
        } else {
            CommandContext.getInstance().addArgument(cmdline);
        }
        String cmd = CommandContext.getInstance().nextArgument();
        if (CommandContext.getInstance().isCommand(cmd)) {
            try {
                CommandContext.getInstance().executeCommand(cmd);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else {
            logger.warning("Sorry! This command is unknown!");
        }
        promptCLI();
    }

    /**
     * Boostrap Handling for the H2 Database
     *
     * @throws SQLException
     */
    private static void loadDatabase() throws SQLException {
        long currentTimeMillis = System.currentTimeMillis();
        H2Server = Server.createTcpServer().start();
        H2DatabaseManager.getInstance().getEntityManager();
        long diff = System.currentTimeMillis() - currentTimeMillis;
        logger.log(Level.INFO, "Start H2 Database and JPA Connection in " + diff + " milliseconds.");
    }

    /**
     * Just a gimmick :)
     *
     * @throws IOException
     */
    private static void splash() throws IOException {
        // need to adjust for width and height
        System.out.println("\n\n");
        BufferedImage image = new BufferedImage(144, 32, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setFont(new Font("Dialog", Font.PLAIN, 15));
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // the banner text may affect width and height
        graphics.drawString("LocApp", 6, 24);
        ImageIO.write(image, "png", File.createTempFile("AsciiBanner.png", null));

        // need to adjust for width and height
        for (int y = 0; y < 32; y++) {
            StringBuilder sb = new StringBuilder();
            // need to adjust for width and height
            for (int x = 0; x < 144; x++)
                sb.append(image.getRGB(x, y) == -16777216 ? BLANK : image.getRGB(x, y) == -1 ? "*" : "*");
            if (sb.toString().trim().isEmpty())
                continue;
            System.out.println(sb);
        }
        System.out.println("\n\n");
    }
}
